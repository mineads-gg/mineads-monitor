package gg.mineads.monitor.shared.skin.model;

import lombok.Data;

@Data
public class MojangProfileResponse {
  private long timestamp;
  private String profileId;
  private String profileName;
  private boolean signatureRequired;
  private MojangProfileTextures textures;
}
