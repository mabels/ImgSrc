package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

//@Suffix(".jpg")
class Format_jpg extends Format {
  private final String format = "JPEG";
  private final String mime = "image/jpeg";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".jpg";
  
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
