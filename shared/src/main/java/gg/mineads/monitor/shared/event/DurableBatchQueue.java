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

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Durable queue for outbound batches using Chronicle Queue.
 */
final class DurableBatchQueue implements AutoCloseable {

  private static final long RETENTION_MILLIS = java.util.concurrent.TimeUnit.DAYS.toMillis(3);

  private final ChronicleQueue queue;
  private final ExcerptAppender appender;
  private final ExcerptTailer tailer;
  private final Path queueDir;

  DurableBatchQueue(Path queueDir) {
    this.queueDir = queueDir;
    try {
      Files.createDirectories(queueDir);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create queue directory " + queueDir, e);
    }

    this.queue = ChronicleQueue.singleBuilder(queueDir)
      .rollCycle(RollCycles.DEFAULT)
      .wireType(WireType.BINARY_LIGHT)
      .build();

    this.appender = queue.createAppender();
    this.tailer = queue.createTailer("outbound");
  }

  void append(byte[] payload, int eventCount) {
    appender.writeDocument(wire -> {
      wire.write("attempt").int32(0);
      wire.write("eventCount").int32(eventCount);
      wire.write("createdAt").int64(System.currentTimeMillis());
      wire.write("payload").bytes(payload);
    });
  }

  BatchRecord readNext() {
    DocumentContext context = tailer.readingDocument();
    if (!context.isPresent()) {
      context.close();
      return null;
    }

    var wire = context.wire();
    int attempt = wire.read("attempt").int32();
    int eventCount = wire.read("eventCount").int32();
    long createdAt = wire.read("createdAt").int64();
    byte[] payload = wire.read("payload").bytes();
    long index = context.index();
    return new BatchRecord(context, index, attempt, eventCount, createdAt, payload);
  }

  boolean hasMore() {
    try (DocumentContext context = tailer.readingDocument()) {
      if (!context.isPresent()) {
        return false;
      }
      context.rollbackOnClose(); // keep pointer at current entry
      return true;
    }
  }

  @Override
  public void close() {
    queue.close();
  }

  void runRetention() {
    long cutoff = System.currentTimeMillis() - RETENTION_MILLIS;
    try {
      Files.list(queueDir)
        .filter(path -> path.getFileName().toString().endsWith(".cq4"))
        .filter(path -> {
          try {
            return Files.getLastModifiedTime(path).toMillis() < cutoff;
          } catch (IOException e) {
            return false;
          }
        })
        .forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException ignored) {
            // If we fail to delete, we'll try again on next retention pass.
          }
        });
    } catch (IOException ignored) {
      // If listing fails, skip this round; next run will try again.
    }
  }

  static final class BatchRecord implements AutoCloseable {
    private final DocumentContext context;
    private final long index;
    private final int attempt;
    private final int eventCount;
    private final long createdAt;
    private final byte[] payload;
    private boolean closed = false;

    BatchRecord(DocumentContext context, long index, int attempt, int eventCount, long createdAt, byte[] payload) {
      this.context = context;
      this.index = index;
      this.attempt = attempt;
      this.eventCount = eventCount;
      this.createdAt = createdAt;
      this.payload = payload;
    }

    long index() {
      return index;
    }

    int attempt() {
      return attempt;
    }

    int eventCount() {
      return eventCount;
    }

    long createdAt() {
      return createdAt;
    }

    byte[] payload() {
      return payload;
    }

    void rollback() {
      context.rollbackOnClose();
      close();
    }

    @Override
    public void close() {
      if (!closed) {
        context.close();
        closed = true;
      }
    }
  }
}
