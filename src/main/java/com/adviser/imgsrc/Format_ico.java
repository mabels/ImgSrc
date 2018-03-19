package com.adviser.imgsrc;

import net.sf.image4j.codec.ico.ICOEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//@Suffix(".png")
class Format_ico extends Format {

  public static class RenderIco implements Render {
    private final RenderPixel render;

    RenderIco(Format format) {
      this.render = new RenderPixel(format);
    }

    @Override
    public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
      return this.render.getGraphics2D(width, height, colorSpace);
    }

    @Override
    public ByteArrayOutputStream getStream() throws IOException {
      ByteArrayOutputStream ret = new ByteArrayOutputStream();
      ICOEncoder.write(this.render.image, ret);
      return ret;
    }
  }

  private final String format = "ICO";
  private final String mime = "image/x-icon";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".ico";

  public String getFormat() {
    return format;
  }

  public String getMime() {
    return mime;
  }

  public int getColorSpace() {
    return colorSpace;
  }

  @Override
  public Render createRender() {
    return new RenderIco(this);
  }

  public String getSuffix() {
    return suffix;
  }


}
