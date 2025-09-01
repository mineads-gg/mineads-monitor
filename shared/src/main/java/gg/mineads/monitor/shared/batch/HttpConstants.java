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

/**
 * Constants for HTTP communication with the MineAds API.
 */
public final class HttpConstants {

  // API endpoints
  public static final String API_ENDPOINT = "https://ingest.mineads.gg/event";

  // HTTP headers
  public static final String HEADER_API_KEY = "X-API-KEY";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";

  // Content types
  public static final String CONTENT_TYPE_MSGPACK = "application/msgpack";

  private HttpConstants() {
    // Utility class
  }
}
