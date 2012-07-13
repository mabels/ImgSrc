package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.image4j.codec.ico.ICOEncoder;
//@Suffix(".png")
class Format_ico extends Format {
  private final String format = "ICO";
  private final String mime = "image/x-icon";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".ico";

  public ByteArrayOutputStream getStream(BufferedImage img) throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    ICOEncoder.write(img, ret);
    return ret;
  }

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
