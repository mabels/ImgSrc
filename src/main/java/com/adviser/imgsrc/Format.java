package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Format {
  public abstract String getFormat();
  public abstract String getMime();
  public abstract String getSuffix();
  public abstract int getColorSpace();  
  //private String cleanPath;

  private static Map<String, Format> formatFactory = null;

  private static Map<String, Format> factory() {
    if (formatFactory == null) {
      formatFactory = new java.util.concurrent.ConcurrentHashMap<String, Format>();
      Format tmp;
      tmp = new Format_png();
      formatFactory.put(tmp.getSuffix(), tmp);
      tmp = new Format_jpg();
      formatFactory.put(tmp.getSuffix(), tmp);
      tmp = new Format_gif();
      formatFactory.put(tmp.getSuffix(), tmp);
      tmp = new Format_ico();
      formatFactory.put(tmp.getSuffix(), tmp);
    }
    return formatFactory;
  }

  public ByteArrayOutputStream getStream(BufferedImage img) throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(img, getFormat(), ret);
    return ret;
  }
  private final static Pattern RESUFFIX = Pattern.compile("(.*)(\\.\\w{3})(.*)");

  public static Format fromPath(String paramPath, Image img) {
    String path = paramPath;
    Format ret = null;
    Matcher suffix = RESUFFIX.matcher(path);
    if (suffix.matches()) {
      ret = factory().get(suffix.group(2));
      if (ret != null) {
        path = suffix.group(1) + suffix.group(3);
      }
    }
    if (ret == null) {
      ret = factory().get(".png");
    }
    img.setPath(path.trim());
    return ret;
  }
}