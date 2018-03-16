package com.adviser.imgsrc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//@Suffix(".png")

class Format_png extends Format {
  private final String format = "PNG";
  private final String mime = "image/png";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".png";
  private final PixelRender render;

  Format_png() {
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
