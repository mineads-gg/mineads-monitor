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

import gg.mineads.monitor.shared.config.Config;
import gg.mineads.monitor.shared.event.generated.EventBatch;
import gg.mineads.monitor.shared.event.generated.MineAdsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@RequiredArgsConstructor
@Log
public class BatchProcessor implements Runnable {
  private static final int BATCH_SIZE_THRESHOLD = 100;
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second
  private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
  private final Queue<MineAdsEvent> events = new ConcurrentLinkedQueue<>();
  private final Config config;
  private final HttpClient httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();
  private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r, "BatchProcessor-Retry");
    t.setDaemon(true);
    return t;
  });
  private final ReentrantLock processingLock = new ReentrantLock();
  private final AtomicBoolean isProcessing = new AtomicBoolean(false);

  private static byte[] serializeToProtobuf(Queue<MineAdsEvent> events) {
    return compress(EventBatch.newBuilder()
      .addAllEvents(events)
      .build()
      .toByteArray());
  }

  @Override
  public void run() {
    if (config.isDebug()) {
      log.info("[DEBUG] BatchProcessor run() called, events in queue: " + events.size());
    }
    if (!events.isEmpty()) {
      processQueueAsync();
    }
  }

  private void processQueueAsync() {
    if (!processingLock.tryLock()) {
      // Another thread is already processing
      if (config.isDebug()) {
        log.info("[DEBUG] Another thread is already processing batch, skipping");
      }
      return;
    }

    try {
      if (isProcessing.getAndSet(true)) {
        // Already processing
        if (config.isDebug()) {
          log.info("[DEBUG] Batch processing already in progress, skipping");
        }
        return;
      }

      if (config.isDebug()) {
        log.info("[DEBUG] Starting async batch processing");
      }

      // Process in background to avoid blocking
      CompletableFuture.runAsync(this::processQueueSafely)
        .whenComplete((result, throwable) -> {
          isProcessing.set(false);
          if (throwable != null) {
            // Log error but don't rethrow to avoid crashing the executor
            log.severe("Error processing batch: " + throwable.getMessage());
          }
        });
    } finally {
      processingLock.unlock();
    }
  }

  public void processIfNecessary() {
    if (events.size() >= BATCH_SIZE_THRESHOLD && !isProcessing.get()) {
      processQueueAsync();
    }
  }

  public void addEvent(MineAdsEvent event) {
    events.add(event);
    if (config.isDebug()) {
      log.info("[DEBUG] Added event to queue, new size: " + events.size());
    }
    processIfNecessary();
  }

  public int getQueueSize() {
    return events.size();
  }

  public void shutdown() {
    retryExecutor.shutdown();
    try {
      if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        retryExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      retryExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  void processQueueSafely() {
    Queue<MineAdsEvent> currentEvents = new ConcurrentLinkedQueue<>();
    int drained = 0;

    // Drain events safely
    while (!events.isEmpty() && drained < BATCH_SIZE_THRESHOLD) {
      MineAdsEvent event = events.poll();
      if (event != null) {
        currentEvents.add(event);
        drained++;
      }
    }

    if (currentEvents.isEmpty()) {
      if (config.isDebug()) {
        log.info("[DEBUG] No events to process");
      }
      return;
    }

    if (config.isDebug()) {
      log.info("[DEBUG] Processing batch of " + drained + " events");

      // Log event type breakdown
      Map<String, Long> eventTypes = currentEvents.stream()
        .collect(Collectors.groupingBy(
          event -> event.getEventType().name(),
          Collectors.counting()
        ));
      log.info("[DEBUG] Event types in batch: " + eventTypes);
    }

    try {
      byte[] protobuf = serializeToProtobuf(currentEvents);
      if (config.isDebug()) {
        log.info("[DEBUG] Serialized batch to " + protobuf.length + " bytes");
      }
      sendBatchWithRetry(protobuf, 0);
    } catch (Exception e) {
      log.severe("Failed to process batch: " + e.getMessage());
      // Re-queue events on failure
      events.addAll(currentEvents);
      if (config.isDebug()) {
        log.info("[DEBUG] Re-queued " + currentEvents.size() + " events due to processing failure");
      }
    }
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

  private void sendBatchWithRetry(byte[] payload, int attempt) {
    HttpRequest request = HttpRequest.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .uri(URI.create("https://ingest.mineads.gg/event"))
      .header("X-API-KEY", config.getPluginKey())
      .header("Content-Type", "application/x-protobuf")
      .header("Content-Encoding", "gzip")
      .timeout(REQUEST_TIMEOUT)
      .PUT(HttpRequest.BodyPublishers.ofByteArray(payload))
      .build();

    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenAccept(response -> handleResponse(response, payload, attempt))
      .exceptionally(throwable -> {
        handleSendError(throwable, payload, attempt);
        return null;
      });
  }

  private void handleResponse(HttpResponse<String> response, byte[] batch, int attempt) {
    int statusCode = response.statusCode();

    if (statusCode >= 200 && statusCode < 300) {
      // Success
      log.info("Successfully sent batch of " + batch.length + " bytes");
      if (config.isDebug()) {
        log.info("[DEBUG] Batch sent successfully with status " + statusCode);
      }
    } else if (shouldRetry(statusCode) && attempt < MAX_RETRY_ATTEMPTS) {
      // Retry on server errors or rate limiting
      long delayMs = calculateRetryDelay(attempt);
      log.warning("Batch send failed with status " + statusCode + ", retrying in " + delayMs + "ms (attempt " + (attempt + 1) + ")");
      if (config.isDebug()) {
        log.info("[DEBUG] Scheduling retry attempt " + (attempt + 1) + " in " + delayMs + "ms");
        String responseBody = response.body();
        if (responseBody != null && !responseBody.trim().isEmpty()) {
          log.info("[DEBUG] Error response body: " + responseBody);
        }
      }
      retryExecutor.schedule(() -> sendBatchWithRetry(batch, attempt + 1), delayMs, TimeUnit.MILLISECONDS);
    } else {
      // Final failure
      log.severe("Batch send failed with status " + statusCode + " after " + (attempt + 1) + " attempts");
      if (config.isDebug()) {
        log.info("[DEBUG] Final failure for batch after " + (attempt + 1) + " attempts");
        String responseBody = response.body();
        if (responseBody != null && !responseBody.trim().isEmpty()) {
          log.info("[DEBUG] Error response body: " + responseBody);
        }
      }
    }
  }

  private void handleSendError(Throwable throwable, byte[] batch, int attempt) {
    if (shouldRetryOnException(throwable) && attempt < MAX_RETRY_ATTEMPTS) {
      long delayMs = calculateRetryDelay(attempt);
      log.warning("Batch send failed with exception: " + throwable.getMessage() + ", retrying in " + delayMs + "ms (attempt " + (attempt + 1) + ")");
      if (config.isDebug()) {
        log.info("[DEBUG] Scheduling retry attempt " + (attempt + 1) + " in " + delayMs + "ms due to exception");
        log.info("[DEBUG] Exception details: " + throwable.toString());
      }
      retryExecutor.schedule(() -> sendBatchWithRetry(batch, attempt + 1), delayMs, TimeUnit.MILLISECONDS);
    } else {
      log.severe("Batch send failed after " + (attempt + 1) + " attempts: " + throwable.getMessage());
      if (config.isDebug()) {
        log.info("[DEBUG] Final failure for batch after " + (attempt + 1) + " attempts due to exception");
        log.info("[DEBUG] Exception details: " + throwable.toString());
      }
    }
  }

  private boolean shouldRetry(int statusCode) {
    return statusCode == 429 || // Too Many Requests
      statusCode >= 500;   // Server errors
  }

  private boolean shouldRetryOnException(Throwable throwable) {
    return throwable instanceof IOException;
  }

  private long calculateRetryDelay(int attempt) {
    long delay = INITIAL_RETRY_DELAY_MS * (1L << attempt); // Exponential backoff
    return Math.min(delay, MAX_RETRY_DELAY_MS);
  }
}
