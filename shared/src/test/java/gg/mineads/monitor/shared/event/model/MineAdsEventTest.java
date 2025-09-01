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
package gg.mineads.monitor.shared.event.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MineAdsEventTest {

  @Test
  void testConstructorWithEventTypeAndData() {
    String eventType = "purchase";
    Object data = new Object();

    MineAdsEvent event = new MineAdsEvent(eventType, data);

    assertEquals(eventType, event.getEventType());
    assertEquals(data, event.getData());
    assertTrue(event.getTime() > 0);
    assertTrue(event.getTime() <= System.currentTimeMillis());
  }

  @Test
  void testTimeIsSetToCurrentTime() {
    long beforeCreation = System.currentTimeMillis();
    MineAdsEvent event = new MineAdsEvent("test", null);
    long afterCreation = System.currentTimeMillis();

    assertTrue(event.getTime() >= beforeCreation);
    assertTrue(event.getTime() <= afterCreation);
  }

  @Test
  void testDifferentEventTypes() {
    Object data = "test data";

    MineAdsEvent purchaseEvent = new MineAdsEvent("purchase", data);
    MineAdsEvent chatEvent = new MineAdsEvent("chat", data);
    MineAdsEvent commandEvent = new MineAdsEvent("command", data);
    MineAdsEvent joinEvent = new MineAdsEvent("join", data);
    MineAdsEvent leaveEvent = new MineAdsEvent("leave", data);

    assertEquals("purchase", purchaseEvent.getEventType());
    assertEquals("chat", chatEvent.getEventType());
    assertEquals("command", commandEvent.getEventType());
    assertEquals("join", joinEvent.getEventType());
    assertEquals("leave", leaveEvent.getEventType());

    assertEquals(data, purchaseEvent.getData());
    assertEquals(data, chatEvent.getData());
    assertEquals(data, commandEvent.getData());
    assertEquals(data, joinEvent.getData());
    assertEquals(data, leaveEvent.getData());
  }

  @Test
  void testNullData() {
    MineAdsEvent event = new MineAdsEvent("test", null);
    assertNull(event.getData());
    assertEquals("test", event.getEventType());
  }

  @Test
  void testComplexDataObject() {
    TestDataObject data = new TestDataObject("test", 42);
    MineAdsEvent event = new MineAdsEvent("complex", data);

    assertEquals("complex", event.getEventType());
    assertEquals(data, event.getData());
    assertTrue(event.getData() instanceof TestDataObject);

    TestDataObject retrievedData = (TestDataObject) event.getData();
    assertEquals("test", retrievedData.name);
    assertEquals(42, retrievedData.value);
  }

  private static class TestDataObject {
    private final String name;
    private final int value;

    public TestDataObject(String name, int value) {
      this.name = name;
      this.value = value;
    }
  }
}
