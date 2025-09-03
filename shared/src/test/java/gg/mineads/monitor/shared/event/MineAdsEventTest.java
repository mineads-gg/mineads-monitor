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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import gg.mineads.monitor.shared.event.model.MineAdsEvent;
import gg.mineads.monitor.shared.event.model.data.EventType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MineAdsEventTest {
  @Test
  void testJsonRoundTripDeserialization() throws IOException {
    // Test complete round-trip: JSON file -> JsonElement -> List<MineAdsEvent> -> JsonElement -> equals

    // Step 1: Read JSON file and parse to JsonElement
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("event-batch-examples.json")) {
      assertNotNull(inputStream, "JSON test file should exist");

      String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

      // Parse original JSON to JsonElement
      JsonElement originalJsonElement = gson.fromJson(jsonContent, JsonElement.class);
      assertNotNull(originalJsonElement, "Original JSON should parse successfully");

      // Step 2: Deserialize JsonElement to List<MineAdsEvent>
      Type eventListType = new TypeToken<List<MineAdsEvent>>() {}.getType();
      List<MineAdsEvent> events = gson.fromJson(originalJsonElement, eventListType);
      assertNotNull(events, "Events list should not be null");
      assertNotEquals(0, events.size(), "Should deserialize at least one event");

      var testedEvents = events.stream().map(MineAdsEvent::getEventType).distinct().toList();
      assertEquals(testedEvents.size(), EventType.values().length,
        "Test payload should cover all event types: " + List.of(EventType.values()));

      // Step 3: Serialize List<MineAdsEvent> back to JsonElement
      JsonElement serializedJsonElement = gson.toJsonTree(events);

      // Step 4: Compare the original and round-trip JsonElements
      assertEquals(gson.toJson(originalJsonElement), gson.toJson(serializedJsonElement),
        "Original and round-trip JSON should be identical");
    }
  }
}
