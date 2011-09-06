package com.adviser.imgsrc;

import java.awt.image.BufferedImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".png")
@Data
@EqualsAndHashCode(callSuper=false)

class Format_ico extends Format {
  private String Format = "ICO";
  private String Mime = "image/x-icon";
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".ico";

}
