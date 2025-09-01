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
package gg.mineads.monitor.shared.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

  @Test
  void testDefaultPluginKey() {
    Config config = new Config();
    assertEquals("", config.getPluginKey());
  }

  @Test
  void testConfigObjectCreation() {
    Config config = new Config();

    // Verify the object can be created
    assertNotNull(config);
  }

  @Test
  void testConfigAnnotation() {
    Config config = new Config();
    // Test that the class has the Configuration annotation
    assertTrue(config.getClass().isAnnotationPresent(de.exlll.configlib.Configuration.class));
  }
}
