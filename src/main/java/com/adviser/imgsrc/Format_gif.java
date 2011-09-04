package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;

//@Suffix(".gif")
@Data
class Format_gif extends Format {
  @SuppressWarnings("unused")
  private String Format = "GIF";
  @SuppressWarnings("unused")
  private String Mime = "image/gif";
  @SuppressWarnings("unused")
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  @SuppressWarnings("unused")
  private String suffix = ".gif";

}
