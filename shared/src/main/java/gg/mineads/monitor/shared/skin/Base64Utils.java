package gg.mineads.monitor.shared.skin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class Base64Utils {
  private Base64Utils() {}

  public static String encode(String data) {
    return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String decode(String base64) {
    return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
  }

  public static String encodePNGAsUrl(File pngFile) throws IOException {
    return encodePNGAsUrl(pngFile.toPath());
  }

  public static String encodePNGAsUrl(Path pngPath) throws IOException {
    return encodePNGAsUrl(Files.readAllBytes(pngPath));
  }

  public static String encodePNGAsUrl(BufferedImage image) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);

    return encodePNGAsUrl(baos.toByteArray());
  }

  public static String encodePNGAsUrl(byte[] pngData) {
    String base64 = Base64.getEncoder().encodeToString(pngData);
    return "data:image/png;base64," + base64;
  }
}
