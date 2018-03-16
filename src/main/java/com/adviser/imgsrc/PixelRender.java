package com.adviser.imgsrc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PixelRender implements  Render {

  private static final Logger LOGGER = LoggerFactory.getLogger(PixelRender.class);

  public BufferedImage image;

  public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
    LOGGER.info("PixelSize:{}x{}", width, height);
    this.image = new BufferedImage(width, height, colorSpace);
    return image.createGraphics();
  }

  public ByteArrayOutputStream getStream(Format format) throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(this.image, format.getFormat(), ret);
    return ret;
  }
}


