package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import lombok.Data;

//@Suffix(".png")
@Data
class Format_png extends Format {
  @SuppressWarnings("unused")
  private String Format = "PNG";
  @SuppressWarnings("unused")
  private String Mime = "image/png";
  @SuppressWarnings("unused")
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  @SuppressWarnings("unused")
  private String suffix = ".png";

}
