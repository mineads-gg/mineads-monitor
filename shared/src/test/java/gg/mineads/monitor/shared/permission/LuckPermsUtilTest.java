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
package gg.mineads.monitor.shared.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LuckPermsUtilTest {

  @AfterEach
  void tearDown() throws Exception {
    // Reset the static state for testing
    resetLuckPermsState();
  }

  private void resetLuckPermsState() throws Exception {
    Field luckPermsField = LuckPermsUtil.class.getDeclaredField("luckPerms");
    Field initializedField = LuckPermsUtil.class.getDeclaredField("initialized");

    luckPermsField.setAccessible(true);
    initializedField.setAccessible(true);

    luckPermsField.set(null, null);
    initializedField.set(null, false);
  }

  @Test
  void testInitializeWithValidLuckPerms() {
    LuckPerms mockLuckPerms = mock(LuckPerms.class);

    LuckPermsUtil.initialize(() -> mockLuckPerms);

    assertTrue(LuckPermsUtil.isAvailable());
  }

  @Test
  void testInitializeWithNullLuckPerms() {
    LuckPermsUtil.initialize(() -> null);

    assertFalse(LuckPermsUtil.isAvailable());
  }

  @Test
  void testInitializeWithException() {
    LuckPermsUtil.initialize(() -> {
      throw new RuntimeException("Test exception");
    });

    assertFalse(LuckPermsUtil.isAvailable());
  }

  @Test
  void testInitializeWithNoClassDefFoundError() {
    LuckPermsUtil.initialize(() -> {
      throw new NoClassDefFoundError("Test NoClassDefFoundError");
    });

    assertFalse(LuckPermsUtil.isAvailable());
  }

  @Test
  void testInitializeOnlyOnce() {
    LuckPerms mockLuckPerms1 = mock(LuckPerms.class);
    LuckPerms mockLuckPerms2 = mock(LuckPerms.class);

    LuckPermsUtil.initialize(() -> mockLuckPerms1);
    assertTrue(LuckPermsUtil.isAvailable());

    // Second initialization should be ignored
    LuckPermsUtil.initialize(() -> mockLuckPerms2);
    // Should still be available (first initialization succeeded)
    assertTrue(LuckPermsUtil.isAvailable());
  }

  @Test
  void testGetPrimaryGroupWhenAvailable() {
    UUID playerUuid = UUID.randomUUID();
    String expectedGroup = "vip";

    LuckPerms mockLuckPerms = mock(LuckPerms.class);
    UserManager mockUserManager = mock(UserManager.class);
    User mockUser = mock(User.class);

    when(mockLuckPerms.getUserManager()).thenReturn(mockUserManager);
    when(mockUserManager.getUser(playerUuid)).thenReturn(mockUser);
    when(mockUser.getPrimaryGroup()).thenReturn(expectedGroup);

    LuckPermsUtil.initialize(() -> mockLuckPerms);

    String result = LuckPermsUtil.getPrimaryGroup(playerUuid);
    assertEquals(expectedGroup, result);
  }

  @Test
  void testGetPrimaryGroupWhenUserNotFound() {
    UUID playerUuid = UUID.randomUUID();

    LuckPerms mockLuckPerms = mock(LuckPerms.class);
    UserManager mockUserManager = mock(UserManager.class);

    when(mockLuckPerms.getUserManager()).thenReturn(mockUserManager);
    when(mockUserManager.getUser(playerUuid)).thenReturn(null);

    LuckPermsUtil.initialize(() -> mockLuckPerms);

    String result = LuckPermsUtil.getPrimaryGroup(playerUuid);
    assertNull(result);
  }

  @Test
  void testGetPrimaryGroupWhenExceptionOccurs() {
    UUID playerUuid = UUID.randomUUID();

    LuckPerms mockLuckPerms = mock(LuckPerms.class);
    UserManager mockUserManager = mock(UserManager.class);

    when(mockLuckPerms.getUserManager()).thenReturn(mockUserManager);
    when(mockUserManager.getUser(playerUuid)).thenThrow(new RuntimeException("Test exception"));

    LuckPermsUtil.initialize(() -> mockLuckPerms);

    String result = LuckPermsUtil.getPrimaryGroup(playerUuid);
    assertNull(result);
  }

  @Test
  void testGetPrimaryGroupWhenLuckPermsNotAvailable() {
    UUID playerUuid = UUID.randomUUID();

    LuckPermsUtil.initialize(() -> null);

    String result = LuckPermsUtil.getPrimaryGroup(playerUuid);
    assertNull(result);
  }

  @Test
  void testIsAvailableInitially() {
    // Before initialization, should be false
    assertFalse(LuckPermsUtil.isAvailable());
  }
}
