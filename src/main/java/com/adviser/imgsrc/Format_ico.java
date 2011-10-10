package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.image4j.codec.ico.ICOEncoder;
//@Suffix(".png")
@Data
@EqualsAndHashCode(callSuper=false)

class Format_ico extends Format {
  private String format = "ICO";
  private String mime = "image/x-icon";
  private int colorSpace = BufferedImage.TYPE_INT_RGB;
  private String suffix = ".ico";

  public ByteArrayOutputStream getStream(BufferedImage img) throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    ICOEncoder.write(img, ret);
    return ret;
  }
 
  
}
