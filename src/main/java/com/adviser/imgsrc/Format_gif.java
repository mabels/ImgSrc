package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

//@Suffix(".gif")
class Format_gif extends Format {
  private final String format = "GIF";
  private final String mime = "image/gif";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".gif";

  public String getFormat() {
    return format;
  }
  public String getMime() {
    return mime;
  }
  public int getColorSpace() {
    return colorSpace;
  }
  public String getSuffix() {
    return suffix;
  }

}
