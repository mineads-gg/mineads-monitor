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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventCollector {

  private final Queue<Object> events = new ConcurrentLinkedQueue<>();

  public void addEvent(Object event) {
    events.add(event);
  }

  public Queue<Object> getEvents() {
    Queue<Object> currentEvents = new ConcurrentLinkedQueue<>(events);
    events.clear();
    return currentEvents;
  }

  public int getQueueSize() {
    return events.size();
  }

}
