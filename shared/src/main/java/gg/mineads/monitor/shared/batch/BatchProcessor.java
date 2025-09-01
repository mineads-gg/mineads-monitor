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

import com.google.gson.*;
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
  private final String pluginKey;
  private final HttpClient httpClient;
  private static final Gson GSON = new Gson();

  public BatchProcessor(EventCollector eventCollector, String pluginKey) {
    this.eventCollector = eventCollector;
    this.pluginKey = pluginKey;
    this.httpClient = HttpClient.newHttpClient();
    this.eventCollector.setBatchProcessor(this);
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

  private static void packJson(MessageBufferPacker packer, JsonElement data) throws IOException {
    switch (data) {
      case JsonPrimitive primitive -> {
        if (primitive.isBoolean()) {
          packer.packBoolean(primitive.getAsBoolean());
        } else if (primitive.isNumber()) {
          Number num = primitive.getAsNumber();
          switch (num) {
            case Integer ignored -> packer.packInt(num.intValue());
            case Long ignored -> packer.packLong(num.longValue());
            case Double ignored -> packer.packDouble(num.doubleValue());
            case Float ignored -> packer.packDouble(num.floatValue());
            case Short ignored -> packer.packShort(num.shortValue());
            case Byte ignored -> packer.packByte(num.byteValue());
            default -> throw new IOException("Unknown number type: " + num.getClass().getName());
          }
        } else if (primitive.isString()) {
          packer.packString(primitive.getAsString());
        } else {
          throw new IOException("Unknown JsonPrimitive type");
        }
      }
      case JsonArray jsonArray -> {
        packer.packArrayHeader(jsonArray.size());
        for (JsonElement element : jsonArray) {
          packJson(packer, element);
        }
      }
      case JsonObject jsonObject -> {
        packer.packMapHeader(jsonObject.size());
        for (String key : jsonObject.keySet()) {
          packer.packString(key);
          packJson(packer, jsonObject.get(key));
        }
      }
      case JsonNull ignored -> packer.packNil();
      default -> throw new IOException("Invalid packing value of type " + data.getClass().getName());
    }
  }

  private byte[] serializeToMessagePack(Queue<Object> events) throws IOException {
    JsonElement json = GSON.toJsonTree(events);

    MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
    packJson(packer, json);
    packer.close();

    return packer.toByteArray();
  }

  private void sendBatch(byte[] batch) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(API_ENDPOINT))
      .header("X-API-KEY", pluginKey)
      .header("Content-Type", "application/msgpack")
      .PUT(HttpRequest.BodyPublishers.ofByteArray(batch))
      .build();

    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenAccept(response -> {
        // Handle response
      });
  }
}
