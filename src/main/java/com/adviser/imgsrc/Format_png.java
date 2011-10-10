package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".png")
@Data
@EqualsAndHashCode(callSuper=false)

class Format_png extends Format {
  private String format = "PNG";
  private String mime = "image/png";
  private int colorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".png";

}
