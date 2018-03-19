package com.adviser.imgsrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RenderPixel implements Render {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderPixel.class);

  public final Format format;
  public BufferedImage image;

  RenderPixel(Format format) {
    this.format = format;
  }

  public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
//    LOGGER.info("PixelSize:{}x{}", width, height);
    this.image = new BufferedImage(width, height, colorSpace);
    return image.createGraphics();
  }

  public ByteArrayOutputStream getStream() throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(this.image, format.getFormat(), ret);
    return ret;
  }
}


