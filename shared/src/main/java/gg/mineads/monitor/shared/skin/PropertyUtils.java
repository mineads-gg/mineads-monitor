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
