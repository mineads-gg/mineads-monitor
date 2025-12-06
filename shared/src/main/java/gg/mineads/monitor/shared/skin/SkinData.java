/*
 * MineAdsMonitor
 * Utility wrapper for parsed skin information.
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
