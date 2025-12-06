package gg.mineads.monitor.shared.skin.property;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Easy way of interacting with properties across multiple platforms.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
public class SkinProperty {
  public static final String TEXTURES_NAME = "textures";
  @NonNull
  private final String value;
  @NonNull
  private final String signature;

  @ApiStatus.Internal
  public static Optional<SkinProperty> tryParse(String name, String value, String signature) {
    if (!TEXTURES_NAME.equals(name) || value == null || signature == null || value.isEmpty() || signature.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new SkinProperty(value, signature));
  }
}
