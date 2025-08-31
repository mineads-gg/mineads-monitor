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
package gg.mineads.monitor.shared.batch;

import com.google.gson.Gson;
import gg.mineads.monitor.shared.event.EventCollector;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Queue;

public class BatchProcessor implements Runnable {

  private static final String API_ENDPOINT = "https://ingest.mineads.gg/event";
  private static final int BATCH_SIZE_THRESHOLD = 100;

  private final EventCollector eventCollector;
  private final String apiKey;
  private final HttpClient httpClient;
  private final Gson gson = new Gson();

  public BatchProcessor(EventCollector eventCollector, String apiKey) {
    this.eventCollector = eventCollector;
    this.apiKey = apiKey;
    this.httpClient = HttpClient.newHttpClient();
  }

  @Override
  public void run() {
    if (eventCollector.getQueueSize() > 0) {
      processQueue();
    }
  }

  public void processIfNecessary() {
    if (eventCollector.getQueueSize() >= BATCH_SIZE_THRESHOLD) {
      processQueue();
    }
  }

  private void processQueue() {
    Queue<Object> events = eventCollector.getEvents();
    if (events.isEmpty()) {
      return;
    }

    try {
      byte[] messagePack = serializeToMessagePack(events);
      sendBatch(messagePack);
    } catch (IOException e) {
      // Handle exception
      e.printStackTrace();
    }
  }

  private byte[] serializeToMessagePack(Queue<Object> events) throws IOException {
    String json = gson.toJson(events);

    MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
    packer.packString(json);
    packer.close();

    return packer.toByteArray();
  }

  private void sendBatch(byte[] batch) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(API_ENDPOINT))
      .header("X-API-KEY", apiKey)
      .header("Content-Type", "application/msgpack")
      .PUT(HttpRequest.BodyPublishers.ofByteArray(batch))
      .build();

    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenAccept(response -> {
        // Handle response
      });
  }
}
