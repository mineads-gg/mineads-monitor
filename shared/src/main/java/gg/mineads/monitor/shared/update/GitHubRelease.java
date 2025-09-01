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

import lombok.Data;

/**
 * Represents a GitHub release from the GitHub API.
 */
@Data
public class GitHubRelease {
  private String tag_name;
  private String name;
  private String body;
  private boolean prerelease;
  private boolean draft;
  private String html_url;
  private String published_at;
}
