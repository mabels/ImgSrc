package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@Suffix(".jpg")
@Data
@EqualsAndHashCode(callSuper=false)

class Format_jpg extends Format {
  private String format = "JPEG";
  private String mime = "image/jpeg";
  private int colorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".jpg";

}
