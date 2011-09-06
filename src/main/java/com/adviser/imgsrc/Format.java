package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
abstract class Format {
  abstract public String getFormat();

  abstract public String getMime();

  abstract public int getColorSpace();

  abstract public void setColorSpace(int s);

  abstract public String getSuffix();

  private String cleanPath;

  private static HashMap<String, Format> _factory = null;

  private static HashMap<String, Format> factory() {
    if (_factory == null) {
      _factory = new HashMap<String, Format>();
      Format tmp;
      tmp = new Format_png();
      _factory.put(tmp.getSuffix(), tmp);
      tmp = new Format_jpg();
      _factory.put(tmp.getSuffix(), tmp);
      tmp = new Format_gif();
      _factory.put(tmp.getSuffix(), tmp);
      tmp = new Format_ico();
      _factory.put(tmp.getSuffix(), tmp);
    }
    return _factory;
  }

  public ByteArrayOutputStream getStream(BufferedImage img) throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(img, getFormat(), ret);
    return ret;
  }
  private static Pattern _suffix = Pattern.compile("(.*)(\\.\\w{3})(.*)");

  public static Format fromPath(String path) {

    Format ret = null;
    Matcher suffix = _suffix.matcher(path);
    if (suffix.matches()) {
      ret = factory().get(suffix.group(2));
      if (ret != null) {
        path = suffix.group(1) + suffix.group(3);
      }
    }
    if (ret == null) {
      ret = factory().get(".gif");
    }
    ret.setCleanPath(path);
    return ret;
  }
}