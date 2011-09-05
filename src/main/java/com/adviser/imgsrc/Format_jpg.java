package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".jpg")
@Data
@EqualsAndHashCode(callSuper=false)

class Format_jpg extends Format {
  private String Format = "JPEG";
  private String Mime = "image/jpeg";
  private int ColorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".jpg";

}
