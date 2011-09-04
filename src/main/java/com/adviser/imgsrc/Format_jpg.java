package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;

//@Suffix(".jpg")
@Data
class Format_jpg extends Format {
  @SuppressWarnings("unused")
  private String Format = "JPEG";
  @SuppressWarnings("unused")
  private String Mime = "image/jpeg";
  @SuppressWarnings("unused")
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  @SuppressWarnings("unused")
  private String suffix = ".jpg";

}
