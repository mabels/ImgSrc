package com.adviser.imgsrc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//@Suffix(".jpg")
class Format_jpg extends Format {
  private final String format = "JPEG";
  private final String mime = "image/jpeg";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".jpg";
  private final PixelRender render;

  Format_jpg() {
    this.render = new PixelRender();
  }

  public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
    return this.render.getGraphics2D(width, height, colorSpace);
  }

  public ByteArrayOutputStream getStream() throws IOException {
    return this.render.getStream(this);
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
