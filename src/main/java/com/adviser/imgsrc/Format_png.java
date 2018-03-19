package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

//@Suffix(".png")

class Format_png extends Format {
  private final String format = "PNG";
  private final String mime = "image/png";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".png";

  @Override
  public Render createRender() {
    return new RenderPixel(this);
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
