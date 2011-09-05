package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".gif")
@Data
@EqualsAndHashCode(callSuper=false)
class Format_gif extends Format {
  private String Format = "GIF";
  private String Mime = "image/gif";
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".gif";

}
