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
package gg.mineads.monitor.shared.skin;

import gg.mineads.monitor.shared.skin.model.MojangProfileResponse;
import gg.mineads.monitor.shared.skin.model.MojangProfileTexture;
import gg.mineads.monitor.shared.skin.property.SkinProperty;
import gg.mineads.monitor.shared.skin.property.SkinVariant;

import java.util.Optional;

public record SkinData(String skinTextureHash, String capeTextureHash, SkinVariant skinVariant) {
  public static Optional<SkinData> fromProperty(SkinProperty property) {
    if (property == null) {
      return Optional.empty();
    }

    MojangProfileResponse response;
    try {
      response = PropertyUtils.getSkinProfileData(property);
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
    if (response == null || response.getTextures() == null || response.getTextures().getSKIN() == null) {
      return Optional.empty();
    }

    MojangProfileTexture skinTexture = response.getTextures().getSKIN();
    String skinTextureHash = skinTexture.getTextureHash();
    String capeTextureHash = null;
    if (response.getTextures().getCAPE() != null) {
      capeTextureHash = response.getTextures().getCAPE().getTextureHash();
    }

    SkinVariant variant = PropertyUtils.getSkinVariant(property);

    return Optional.of(new SkinData(skinTextureHash, capeTextureHash, variant));
  }
}
