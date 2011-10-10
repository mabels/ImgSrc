package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".gif")
@Data
@EqualsAndHashCode(callSuper=false)
class Format_gif extends Format {
  private String format = "GIF";
  private String mime = "image/gif";
  private int colorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".gif";

}
