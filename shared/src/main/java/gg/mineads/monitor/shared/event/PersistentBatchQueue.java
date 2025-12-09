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

import gg.mineads.monitor.shared.scheduler.MineAdsScheduler;
import lombok.extern.java.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Log
final class PersistentBatchQueue implements AutoCloseable {
  private static final long RETENTION_MILLIS = TimeUnit.DAYS.toMillis(3);
  private static final long PERSIST_INTERVAL_SECONDS = 15;
  private static final long RETENTION_INTERVAL_SECONDS = 5;

  private final Path queueFile;
  private final int persistThreshold;
  private final Deque<QueueEntry> queue = new ArrayDeque<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final AtomicLong nextIndex = new AtomicLong();
  private volatile boolean closed = false;

  PersistentBatchQueue(Path queueDir, MineAdsScheduler scheduler, int persistThreshold) {
    this.persistThreshold = persistThreshold;
    this.queueFile = queueDir.resolve("durable-queue.dat");

    try {
      Files.createDirectories(queueDir);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create queue directory " + queueDir, e);
    }

    restoreFromDisk();
    persistQueueIfNeeded();
    scheduler.scheduleAsync(this::persistQueueIfNeeded, PERSIST_INTERVAL_SECONDS, PERSIST_INTERVAL_SECONDS, TimeUnit.SECONDS);
    scheduler.scheduleAsync(this::runRetention, RETENTION_INTERVAL_SECONDS, RETENTION_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  void append(byte[] payload, int eventCount) {
    int sizeAfter;
    lock.lock();
    try {
      queue.addLast(new QueueEntry(nextIndex.getAndIncrement(), 0, eventCount, System.currentTimeMillis(), payload));
      sizeAfter = queue.size();
    } finally {
      lock.unlock();
    }

    if (sizeAfter > persistThreshold) {
      persistQueueIfNeeded();
    }
  }

  BatchRecord readNext() {
    lock.lock();
    try {
      QueueEntry entry = queue.pollFirst();
      if (entry == null) {
        return null;
      }
      return new BatchRecord(entry, this);
    } finally {
      lock.unlock();
    }
  }

  boolean hasMore() {
    lock.lock();
    try {
      return !queue.isEmpty();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    persistQueueIfNeeded();
    closed = true;
  }

  private void runRetention() {
    if (closed) {
      return;
    }

    boolean mutated = false;
    lock.lock();
    try {
      long cutoff = System.currentTimeMillis() - RETENTION_MILLIS;
      while (!queue.isEmpty() && queue.peekFirst().createdAt() < cutoff) {
        queue.pollFirst();
        mutated = true;
      }
    } finally {
      lock.unlock();
    }

    if (mutated) {
      persistQueueIfNeeded();
    }
  }

  private void restoreFromDisk() {
    if (!Files.exists(queueFile)) {
      return;
    }

    long highestIndex = -1;
    try (InputStream fileStream = Files.newInputStream(queueFile, StandardOpenOption.READ);
         BufferedInputStream buffered = new BufferedInputStream(fileStream);
         GZIPInputStream gzipInputStream = new GZIPInputStream(buffered);
         DataInputStream dataInput = new DataInputStream(gzipInputStream)) {
      int count = dataInput.readInt();
      lock.lock();
      try {
        for (int i = 0; i < count; i++) {
          long index = dataInput.readLong();
          int attempt = dataInput.readInt();
          int eventCount = dataInput.readInt();
          long createdAt = dataInput.readLong();
          int payloadLength = dataInput.readInt();
          byte[] payload = new byte[payloadLength];
          dataInput.readFully(payload);
          queue.addLast(new QueueEntry(index, attempt, eventCount, createdAt, payload));
          highestIndex = Math.max(highestIndex, index);
        }
      } finally {
        lock.unlock();
      }
    } catch (IOException e) {
      log.warning("Failed to restore durable queue: " + e.getMessage());
      lock.lock();
      try {
        queue.clear();
      } finally {
        lock.unlock();
      }
    } finally {
      deletePersistedFileSilently();
    }

    if (highestIndex >= 0) {
      nextIndex.set(highestIndex + 1);
    }
  }

  private void persistQueueIfNeeded() {
    if (closed) {
      return;
    }

    lock.lock();
    try {
      if (closed) {
        return;
      }

      if (queue.size() > persistThreshold) {
        writeQueueToFile();
      } else {
        deletePersistedFile();
      }
    } catch (IOException e) {
      log.warning("Failed to persist durable queue: " + e.getMessage());
    } finally {
      lock.unlock();
    }
  }

  private void writeQueueToFile() throws IOException {
    try (OutputStream fileStream = Files.newOutputStream(queueFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
         BufferedOutputStream buffered = new BufferedOutputStream(fileStream);
         GZIPOutputStream gzipOutputStream = new GZIPOutputStream(buffered);
         DataOutputStream dataOutput = new DataOutputStream(gzipOutputStream)) {
      dataOutput.writeInt(queue.size());
      for (QueueEntry entry : queue) {
        dataOutput.writeLong(entry.index());
        dataOutput.writeInt(entry.attempt());
        dataOutput.writeInt(entry.eventCount());
        dataOutput.writeLong(entry.createdAt());
        dataOutput.writeInt(entry.payload().length);
        dataOutput.write(entry.payload());
      }
    }
  }

  private void deletePersistedFile() throws IOException {
    Files.deleteIfExists(queueFile);
  }

  private void deletePersistedFileSilently() {
    try {
      Files.deleteIfExists(queueFile);
    } catch (IOException ignored) {
      // best effort cleanup
    }
  }

  private void requeueFirst(QueueEntry entry) {
    if (closed) {
      return;
    }

    lock.lock();
    try {
      if (!closed) {
        queue.addFirst(entry);
      }
    } finally {
      lock.unlock();
    }
  }

  static final class BatchRecord implements AutoCloseable {
    private final QueueEntry entry;
    private final PersistentBatchQueue parent;
    private boolean rolledBack = false;

    BatchRecord(QueueEntry entry, PersistentBatchQueue parent) {
      this.entry = entry;
      this.parent = parent;
    }

    long index() {
      return entry.index();
    }

    int attempt() {
      return entry.attempt();
    }

    int eventCount() {
      return entry.eventCount();
    }

    long createdAt() {
      return entry.createdAt();
    }

    byte[] payload() {
      return entry.payload();
    }

    void rollback() {
      if (rolledBack) {
        return;
      }
      rolledBack = true;
      parent.requeueFirst(entry);
    }

    @Override
    public void close() {
      // no resources to release
    }
  }

  private static final record QueueEntry(long index, int attempt, int eventCount, long createdAt, byte[] payload) {}
}
