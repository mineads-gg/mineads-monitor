/*
 * MineAdsMonitor
 * Copyright (C) 2025  MineAds
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.mineads.monitor.shared.event;

import com.google.protobuf.InvalidProtocolBufferException;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.generated.EventBatch;
import gg.mineads.monitor.shared.event.generated.FailedEvent;
import gg.mineads.monitor.shared.event.generated.IngestResponse;
import gg.mineads.monitor.shared.event.generated.MineAdsEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Log
@SuppressFBWarnings(value = "EI2", justification = "BatchProcessor must retain the plugin reference to read configuration and schedule work.")
public class BatchProcessor implements Runnable {
  private static final int BATCH_SIZE_THRESHOLD = 100;
  private static final int MAX_RETRY_ATTEMPTS = 5;
  private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second
  private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final Queue<MineAdsEvent> stagingEvents = new ConcurrentLinkedQueue<>();
  private final MineAdsMonitorPlugin plugin;
  private final MineAdsScheduler scheduler;
  private final PersistentBatchQueue durableQueue;
  private final HttpClient httpClient = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
    .connectTimeout(Duration.ofSeconds(10))
    .build();

  private final ReentrantLock stagingLock = new ReentrantLock();
  private final AtomicBoolean stagingProcessing = new AtomicBoolean(false);
  private final AtomicBoolean sendLoopRunning = new AtomicBoolean(false);
  private final Map<Long, Integer> attemptOverrides = new ConcurrentHashMap<>();
  private final Map<Long, byte[]> payloadOverrides = new ConcurrentHashMap<>();

  public BatchProcessor(MineAdsMonitorPlugin plugin, MineAdsScheduler scheduler, Path dataFolder) {
    this.plugin = plugin;
    this.scheduler = scheduler;
    this.durableQueue = new PersistentBatchQueue(dataFolder.resolve("queue"), scheduler, BATCH_SIZE_THRESHOLD);
  }

  @Override
  public void run() {
    if (plugin.hasConfigIssues()) {
      if (plugin.getConfig().isDebug()) {
        log.info("[DEBUG] Skipping batch processing due to configuration issues");
      }
      return;
    }

    if (!stagingEvents.isEmpty()) {
      processStagingAsync();
    } else {
      kickSendLoop();
    }
  }

  public void shutdown() {
    try {
      processStagingAsync();
    } finally {
      durableQueue.close();
    }
  }

  public void addEvent(MineAdsEvent event) {
    stagingEvents.add(event);
    Config config = plugin.getConfig();
    if (config != null && config.isDebug()) {
      log.info("[DEBUG] Added event to staging queue, new size: " + stagingEvents.size());
    }
    processIfNecessary();
  }

  public void processIfNecessary() {
    if (stagingEvents.size() >= BATCH_SIZE_THRESHOLD && !stagingProcessing.get()) {
      processStagingAsync();
    }
  }

  private void processStagingAsync() {
    Config config = plugin.getConfig();
    if (!stagingLock.tryLock()) {
      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Staging lock held, skipping flush");
      }
      return;
    }

    try {
      if (stagingProcessing.getAndSet(true)) {
        return;
      }

      scheduler.runAsync(() -> {
        try {
          flushStaging();
        } finally {
          stagingProcessing.set(false);
        }
      });
    } finally {
      stagingLock.unlock();
    }
  }

  private void flushStaging() {
    Queue<MineAdsEvent> currentEvents = new ConcurrentLinkedQueue<>();
    int drained = 0;

    while (!stagingEvents.isEmpty() && drained < BATCH_SIZE_THRESHOLD) {
      MineAdsEvent event = stagingEvents.poll();
      if (event != null) {
        currentEvents.add(event);
        drained++;
      }
    }

    Config config = plugin.getConfig();
    if (currentEvents.isEmpty()) {
      if (config != null && config.isDebug()) {
        log.info("[DEBUG] No events to flush to durable queue");
      }
      return;
    }

    if (config != null && config.isDebug()) {
      log.info("[DEBUG] Flushing batch of " + drained + " events to durable queue");
      Map<String, Long> eventTypes = currentEvents.stream()
        .collect(Collectors.groupingBy(
          event -> event.getDataCase().name(),
          Collectors.counting()
        ));
      log.info("[DEBUG] Event types in batch: " + eventTypes);
    }

    try {
      byte[] payload = serializeToProtobuf(currentEvents);
      durableQueue.append(payload, drained);
      if (config != null && config.isDebug()) {
        log.info("[DEBUG] Appended batch (" + drained + " events, " + payload.length + " bytes) to the durable queue");
      }
      kickSendLoop();
      // If more remain, schedule another flush
      processIfNecessary();
    } catch (Exception e) {
      log.severe("Failed to flush batch to durable queue: " + e.getMessage());
      stagingEvents.addAll(currentEvents);
    }
  }

  private void kickSendLoop() {
    if (sendLoopRunning.compareAndSet(false, true)) {
      scheduler.runAsync(this::drainDurableQueue);
    }
  }

  private void drainDurableQueue() {
    try {
      while (true) {
        boolean continueLoop = processNextStoredBatch();
        if (!continueLoop) {
          break;
        }
      }
    } finally {
      sendLoopRunning.set(false);
      if (durableQueue.hasMore()) {
        kickSendLoop();
      }
    }
  }

  private boolean processNextStoredBatch() {
    try (PersistentBatchQueue.BatchRecord record = durableQueue.readNext()) {
      if (record == null) {
        return false;
      }

      if (record.eventCount() > BATCH_SIZE_THRESHOLD) {
        log.warning("Stored batch has " + record.eventCount() + " events, exceeding max threshold; sending anyway");
      }

      long index = record.index();
      byte[] payload = payloadOverrides.getOrDefault(index, record.payload());
      int attempt = Math.max(record.attempt(), attemptOverrides.getOrDefault(index, record.attempt()));

      SendResult result = sendBatchSync(payload, record.eventCount(), attempt);

      switch (result.type()) {
        case SUCCESS -> {
          attemptOverrides.remove(index);
          payloadOverrides.remove(index);
          return true;
        }
        case DROP -> {
          attemptOverrides.remove(index);
          payloadOverrides.remove(index);
          return true;
        }
        case RETRY -> {
          int nextAttempt = attempt + 1;
          attemptOverrides.put(index, nextAttempt);
          if (result.payloadOverride() != null) {
            payloadOverrides.put(index, result.payloadOverride());
          }
          long delayMs = calculateRetryDelay(attempt);
          record.rollback(); // keep pointer on this batch
          log.warning("Retrying batch in " + delayMs + "ms (attempt " + nextAttempt + ")");
          scheduler.scheduleAsyncDelayed(this::kickSendLoop, delayMs, TimeUnit.MILLISECONDS);
          return false;
        }
        default -> {
          return true;
        }
      }
    }
  }

  private SendResult sendBatchSync(byte[] payload, int eventCount, int attempt) {
    Config config = plugin.getConfig();
    if (config == null) {
      log.warning("Cannot send batch because configuration was not loaded");
      return SendResult.drop();
    }
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("https://ingest.mineads.gg/event"))
      .header("X-API-KEY", config.getPluginKey())
      .header("Content-Type", "application/x-protobuf")
      .header("Content-Encoding", "gzip")
      .header("Accept-Encoding", "gzip")
      .header("Accept", "application/x-protobuf")
      .timeout(REQUEST_TIMEOUT)
      .PUT(HttpRequest.BodyPublishers.ofByteArray(payload))
      .build();

    try {
      HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      byte[] responseBytes;
      try (InputStream bodyStream = getDecodedInputStream(response)) {
        responseBytes = bodyStream.readAllBytes();
      }

      IngestResponse ingestResponse = parseResponse(responseBytes);
      int statusCode = response.statusCode();
      boolean succeeded = statusCode >= 200 && statusCode < 300 && ingestResponse != null && ingestResponse.getSuccess();

      if (succeeded) {
        log.info("Successfully sent batch of " + eventCount + " events (" + payload.length + " bytes)");
        if (config.isDebug()) {
          log.info("[DEBUG] Batch sent successfully with status " + statusCode);
        }
        return SendResult.success();
      }

      byte[] nextPayload = determineFailedPayload(ingestResponse, payload);
      if (attempt + 1 >= MAX_RETRY_ATTEMPTS) {
        log.severe("Batch send failed after " + (attempt + 1) + " attempts (status " + statusCode + ")");
        if (config.isDebug()) {
          logDebugResponse(ingestResponse, responseBytes);
        }
        return SendResult.drop();
      }

      if (config.isDebug()) {
        logDebugResponse(ingestResponse, responseBytes);
      }
      return SendResult.retry(nextPayload);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      if (attempt + 1 >= MAX_RETRY_ATTEMPTS) {
        log.severe("Batch send interrupted after " + (attempt + 1) + " attempts: " + interrupted.getMessage());
        return SendResult.drop();
      }
      log.warning("Batch send interrupted: " + interrupted.getMessage());
      return SendResult.retry(null);
    } catch (IOException ioException) {
      if (attempt + 1 >= MAX_RETRY_ATTEMPTS) {
        log.severe("Batch send failed after " + (attempt + 1) + " attempts: " + ioException.getMessage());
        return SendResult.drop();
      }
      log.warning("Batch send failed with exception: " + ioException.getMessage());
      return SendResult.retry(null);
    }
  }

  private byte[] determineFailedPayload(IngestResponse ingestResponse, byte[] currentPayload) {
    if (ingestResponse == null || ingestResponse.getFailedEventsList().isEmpty()) {
      // No per-event detail: retry everything
      return currentPayload;
    }

    Set<String> failedIds = ingestResponse.getFailedEventsList().stream()
      .map(FailedEvent::getEventId)
      .collect(Collectors.toSet());

    EventBatch eventBatch = parseEventBatch(currentPayload);
    if (eventBatch == null) {
      return currentPayload;
    }

    EventBatch.Builder builder = EventBatch.newBuilder()
      .setServerId(eventBatch.getServerId());

    for (MineAdsEvent event : eventBatch.getEventsList()) {
      if (failedIds.contains(event.getEventId())) {
        builder.addEvents(event);
      }
    }

    if (builder.getEventsCount() == 0) {
      // Nothing matched; retry the full batch
      builder.addAllEvents(eventBatch.getEventsList());
    }

    return compress(builder.build().toByteArray());
  }

  private EventBatch parseEventBatch(byte[] payload) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new java.io.ByteArrayInputStream(payload))) {
      byte[] decompressed = gzipInputStream.readAllBytes();
      return EventBatch.parseFrom(decompressed);
    } catch (IOException e) {
      log.severe("Failed to parse stored batch: " + e.getMessage());
      return null;
    }
  }

  private byte[] serializeToProtobuf(Queue<MineAdsEvent> events) {
    Config config = plugin.getConfig();
    return compress(EventBatch.newBuilder()
      .addAllEvents(events)
      .setServerId(config.getServerId())
      .build()
      .toByteArray());
  }

  private static byte[] compress(byte[] data) {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
         GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
      gzipStream.write(data);
      gzipStream.close();
      return byteStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to gzip data", e);
    }
  }

  public static InputStream getDecodedInputStream(HttpResponse<InputStream> httpResponse) {
    String encoding = determineContentEncoding(httpResponse);
    try {
      return switch (encoding) {
        case "" -> httpResponse.body();
        case "gzip" -> new GZIPInputStream(httpResponse.body());
        default -> throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
      };
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

  public static String determineContentEncoding(HttpResponse<?> httpResponse) {
    return httpResponse.headers().firstValue("Content-Encoding").orElse("");
  }

  private IngestResponse parseResponse(byte[] responseBytes) {
    try {
      return IngestResponse.parseFrom(responseBytes);
    } catch (InvalidProtocolBufferException e) {
      log.severe("Failed to parse protobuf ingest response: " + e.getMessage());
      return null;
    }
  }

  private void logDebugResponse(IngestResponse ingestResponse, byte[] rawBytes) {
    if (ingestResponse != null) {
      log.info("[DEBUG] IngestResponse success=" + ingestResponse.getSuccess() + " error=" + ingestResponse.getError());
    } else if (rawBytes != null && rawBytes.length > 0) {
      String fallback = new String(rawBytes, StandardCharsets.UTF_8);
      log.info("[DEBUG] Raw response (unparsed): " + fallback);
    } else {
      log.info("[DEBUG] Empty response body");
    }
  }

  private long calculateRetryDelay(int attempt) {
    long delay = INITIAL_RETRY_DELAY_MS * (1L << attempt);
    return Math.min(delay, MAX_RETRY_DELAY_MS);
  }

  private enum ResultType {
    SUCCESS,
    RETRY,
    DROP
  }

  private record SendResult(ResultType type, byte[] payloadOverride) {
    static SendResult success() {
      return new SendResult(ResultType.SUCCESS, null);
    }

    static SendResult retry(byte[] payloadOverride) {
      return new SendResult(ResultType.RETRY, payloadOverride);
    }

    static SendResult drop() {
      return new SendResult(ResultType.DROP, null);
    }
  }
}
