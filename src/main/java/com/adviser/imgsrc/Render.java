package com.adviser.imgsrc;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface Render {
  Graphics2D getGraphics2D(int width, int height, int colorSpace);
  ByteArrayOutputStream getStream(Format format) throws IOException;
}
