/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
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

import com.google.gson.Gson;
import gg.mineads.monitor.shared.skin.model.MojangProfileResponse;
import gg.mineads.monitor.shared.skin.model.MojangProfileTextureMeta;
import gg.mineads.monitor.shared.skin.property.SkinProperty;
import gg.mineads.monitor.shared.skin.property.SkinVariant;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for retrieving information from profile properties related to skins.
 */
public class PropertyUtils {
  private static final Gson GSON = new Gson();

  private PropertyUtils() {}

  /**
   * Returns a {@code textures.minecraft.net} URL for the skin.
   *
   * @param base64 Profile value
   * @return full textures.minecraft.net url
   */
  public static String getSkinTextureUrl(@NotNull String base64) {
    return getSkinProfileData(base64).getTextures().getSKIN().getUrl();
  }

  public static String getSkinTextureUrl(@NotNull SkinProperty property) {
    return getSkinTextureUrl(property.getValue());
  }

  public static SkinVariant getSkinVariant(@NotNull String base64) {
    MojangProfileTextureMeta meta = getSkinProfileData(base64).getTextures().getSKIN().getMetadata();
    if (meta == null) {
      return SkinVariant.CLASSIC;
    }

    return meta.getModel().equalsIgnoreCase("slim") ? SkinVariant.SLIM : SkinVariant.CLASSIC;
  }

  public static SkinVariant getSkinVariant(@NotNull SkinProperty property) {
    return getSkinVariant(property.getValue());
  }

  /**
   * Only returns the id at the end of the url.
   *
   * @param base64 Profile value
   * @return textures.minecraft.net id
   * @see #getSkinTextureUrl(String)
   */
  public static String getSkinTextureHash(@NotNull String base64) {
    return getSkinProfileData(base64).getTextures().getSKIN().getTextureHash();
  }

  public static String getSkinTextureHash(@NotNull SkinProperty property) {
    return getSkinTextureHash(property.getValue());
  }

  /**
   * @deprecated Use {@link #getSkinTextureHash(SkinProperty)} instead.
   */
  @Deprecated(forRemoval = true)
  public static String getSkinTextureUrlStripped(@NotNull SkinProperty property) {
    return getSkinTextureHash(property);
  }

  /**
   * Returns the decoded profile data from the profile property.
   *
   * @param base64 Profile value
   * @return Decoded profile data as java object
   */
  public static MojangProfileResponse getSkinProfileData(@NotNull String base64) {
    return GSON.fromJson(Base64Utils.decode(base64), MojangProfileResponse.class);
  }

  public static MojangProfileResponse getSkinProfileData(@NotNull SkinProperty property) {
    return getSkinProfileData(property.getValue());
  }
}
