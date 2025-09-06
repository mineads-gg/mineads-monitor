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
package gg.mineads.monitor.shared.update;

import com.google.gson.Gson;
import gg.mineads.monitor.data.BuildData;
import gg.mineads.monitor.shared.MineAdsMonitorPlugin;
import lombok.extern.java.Log;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Handles checking for plugin updates from GitHub releases.
 */
@Log
public class UpdateChecker {

  private static final String GITHUB_API_URL = "https://api.github.com/repos/mineads-gg/mineads-monitor/releases/latest";
  private static final Gson GSON = new Gson();
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build();

  /**
   * Checks for updates asynchronously and logs results to console.
   */
  public static CompletableFuture<Void> checkForUpdates(MineAdsMonitorPlugin plugin) {
    log.info("[MineAdsMonitor] Checking for updates...");
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(GITHUB_API_URL))
      .header("Accept", "application/vnd.github.v3+json")
      .header("User-Agent", "MineAdsMonitor/" + BuildData.VERSION)
      .timeout(Duration.ofSeconds(10))
      .GET()
      .build();

    return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(HttpResponse::body)
      .thenAccept(response -> processUpdateResponse(response, plugin))
      .exceptionally(throwable -> {
        log.warning("[MineAdsMonitor] Failed to check for updates: " + throwable.getMessage());
        return null;
      });
  }

  /**
   * Processes the JSON response from GitHub API.
   */
  private static void processUpdateResponse(String jsonResponse, MineAdsMonitorPlugin plugin) {
    try {
      log.info("[MineAdsMonitor] Processing update response...");
      GitHubRelease latestRelease = GSON.fromJson(jsonResponse, GitHubRelease.class);

      if (latestRelease == null) {
        log.warning("[MineAdsMonitor] Failed to parse update response");
        return;
      }

      String latestVersion = latestRelease.getTag_name();
      if (latestVersion == null || latestVersion.isEmpty()) {
        log.warning("[MineAdsMonitor] No version information found in update response");
        return;
      }

      // Remove 'v' prefix if present
      if (latestVersion.startsWith("v")) {
        latestVersion = latestVersion.substring(1);
      }

      String currentVersion = BuildData.VERSION;

      log.info("[MineAdsMonitor] Current version: " + currentVersion);
      log.info("[MineAdsMonitor] Latest version: " + latestVersion);

      if (isNewerVersion(latestVersion, currentVersion)) {
        plugin.setOutdated(true);
        log.info("[MineAdsMonitor] =========================================");
        log.info("[MineAdsMonitor] A new version is available!");
        log.info("[MineAdsMonitor] Current: " + currentVersion);
        log.info("[MineAdsMonitor] Latest: " + latestVersion);
        if (latestRelease.getHtml_url() != null) {
          log.info("[MineAdsMonitor] Download: " + latestRelease.getHtml_url());
        }
        if (latestRelease.getBody() != null && !latestRelease.getBody().isEmpty()) {
          log.info("[MineAdsMonitor] Release notes:");
          log.info("[MineAdsMonitor] " + latestRelease.getBody().replace("\n", "\n[MineAdsMonitor] "));
        }
        log.info("[MineAdsMonitor] =========================================");
      } else if (latestVersion.equals(currentVersion)) {
        log.info("[MineAdsMonitor] You are running the latest version!");
      } else {
        log.info("[MineAdsMonitor] You are running a development/pre-release version.");
      }

    } catch (Exception e) {
      log.severe("[MineAdsMonitor] Failed to process update response: " + e.getMessage());
    }
  }

  /**
   * Compares two version strings to determine if the first is newer than the second.
   * Supports semantic versioning (major.minor.patch).
   */
  private static boolean isNewerVersion(String version1, String version2) {
    if (version1 == null || version2 == null) {
      return false;
    }

    try {
      String[] parts1 = version1.split("\\.");
      String[] parts2 = version2.split("\\.");

      int length = Math.max(parts1.length, parts2.length);

      for (int i = 0; i < length; i++) {
        int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
        int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

        if (v1 > v2) {
          return true;
        } else if (v1 < v2) {
          return false;
        }
      }

      return false; // versions are equal

    } catch (NumberFormatException e) {
      // If we can't parse versions, fall back to string comparison
      return version1.compareTo(version2) > 0;
    }
  }
}
