package s2.dimage;

import java.awt.image.BufferedImage;

import lombok.Data;
import lombok.val;

@Data
class Format {
  private String Format;
  private String Mime;
  private int ColorSpace;

  public static Format fromPath(String path) {
    /* ugly */
    if (path.indexOf(".jpg") >= 0) {
      final val ret = new Format();
      ret.setFormat("JPEG");
      ret.setMime("image/jpeg");
      ret.setColorSpace(BufferedImage.TYPE_INT_RGB);
      return ret;
    }
    if (path.indexOf(".png") >= 0) {
      final val ret = new Format();
      ret.setFormat("PNG");
      ret.setMime("image/png");
      ret.setColorSpace(BufferedImage.TYPE_INT_RGB);
      return ret;
    }
    final val ret = new Format();
    ret.setFormat("GIF");
    ret.setMime("image/gif");
    ret.setColorSpace(BufferedImage.TYPE_INT_RGB);
    return ret;
  }
}
