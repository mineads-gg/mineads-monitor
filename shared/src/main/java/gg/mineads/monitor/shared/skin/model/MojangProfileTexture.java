package gg.mineads.monitor.shared.skin.model;

import lombok.Data;

import java.util.regex.Pattern;

@Data
public class MojangProfileTexture {
  private static final Pattern URL_STRIP_PATTERN = Pattern.compile("^https?://textures\\.minecraft\\.net/texture/");
  private String url;
  private MojangProfileTextureMeta metadata;

  public String getTextureHash() {
    return URL_STRIP_PATTERN.matcher(url).replaceAll("");
  }

  /**
   * @deprecated Use {@link #getTextureHash()} instead.
   */
  @Deprecated(forRemoval = true)
  public String getStrippedUrl() {
    return getTextureHash();
  }
}
