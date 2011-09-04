package com.adviser.imgsrc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.management.RuntimeErrorException;

import java.util.HashMap;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.val;

@Data
public class Image {
  private int width = 100;
  private int height = 100;
  private Color backcolor = new Color(0x444444);
  private Color textcolor = new Color(0xffffff);
  private String text = null;

  private Format format = null;

  private Color asRGBColor(String s) {
    int rgb = 0xCCCCC;
    final int val = Integer.parseInt(s, 16);
    if (s.length() == 3) {
      rgb = (((val >> 0) & 0xf) | (((val >> 0) & 0xf) << 4)) << 0
          | (((val >> 4) & 0xf) | (((val >> 4) & 0xf) << 4)) << 8
          | (((val >> 8) & 0xf) | (((val >> 8) & 0xf) << 4)) << 16;
    } else if (s.length() == 6) {
      rgb = ((val >> 0) & 0xff) << 0 | ((val >> 8) & 0xff) << 8
          | ((val >> 16) & 0xff) << 16;
    }
    return new Color(rgb);
  }

  private Color asABGRColor(String s) {
    format.setColorSpace(BufferedImage.TYPE_4BYTE_ABGR);
    int rgb = 0xFFCCCCC;
    final int val = Integer.parseInt(s, 16);
    if (s.length() == 4) {
      rgb = (((val >> 0) & 0xf) | (((val >> 0) & 0xf) << 4)) << 24
          | (((val >> 4) & 0xf) | (((val >> 4) & 0xf) << 4)) << 0
          | (((val >> 8) & 0xf) | (((val >> 8) & 0xf) << 4)) << 8
          | (((val >> 12) & 0xf) | (((val >> 12) & 0xf) << 4)) << 16;
      System.out.println("RGBA:" + Integer.toHexString(val) + ":"
          + Integer.toHexString(rgb));
      return new Color(rgb, true);
    } else if (s.length() == 8) {
      rgb = ((val >> 0) & 0xff) << 24 | ((val >> 8) & 0xff) << 0
          | ((val >> 16) & 0xff) << 8 | ((val >> 24) & 0xff) << 16;
      return new Color(rgb);
    }
    return new Color(rgb); 
  }

  private static Pattern _4er = Pattern.compile("\\p{XDigit}{4}");
  private static Pattern _8er = Pattern.compile("\\p{XDigit}{8}");
  private static Pattern _3er = Pattern.compile("\\p{XDigit}{3}");
  private static Pattern _6er = Pattern.compile("\\p{XDigit}{6}");
  private Color asColor(String s) {
    final char first = s.charAt(0);
    int len = s.length();
    if (len > 0 && (first == 'r' && first == 'R')) {
      s = s.substring(1);
      len = s.length();
    }
    if ((len== 4 && _4er.matcher(s).matches())        
        || (len == 8 && _8er.matcher(s).matches())) {
      return asABGRColor(s);
    }
    if ((len == 3 && _3er.matcher(s).matches())
        || (len == 6 && _6er.matcher(s).matches())) {
      return asRGBColor(s);
    }
    return null;
  }


  public static Image fromPath(String path) {
    final val img = new Image();
    /*
     * /height/width/backcolor/textcolor/text<.format>
     */
    img.setFormat(Format.fromPath(path));
    path = img.getFormat().getCleanPath().trim();
    String[] paths = path.split("/+", 0);
    int ofs = 0;
    if (paths.length > 0 && paths[0].isEmpty()) {
    	ofs = 1;
    	if (paths.length == 1 && paths[0].isEmpty()) {
	      paths = new String[0];
    	}
   }
     if (paths.length >= ofs + 1) {
         try {
        	 img.setWidth(Integer.parseInt(paths[ofs + 0]));
        	 img.setHeight(img.getWidth());
         } catch (Exception e) {
        	 img.setText(paths[ofs + 0]);
         }
    }
    if (paths.length >= ofs + 2) {
      try {
        img.setWidth(Integer.parseInt(paths[ofs + 1]));
      } catch (Exception e) {
        img.setText(paths[ofs + 1]);
      }
    }
    if (paths.length >= ofs + 3) {
      val color = img.asColor(paths[ofs + 2]);
      if (color != null)
        img.setBackcolor(color);
      else
        img.setText(paths[ofs + 2]);
    }
    if (paths.length >= ofs + 4) {
      val color = img.asColor(paths[ofs + 3]);
      if (color != null)
        img.setTextcolor(color);
      else
        img.setText(paths[ofs + 3]);
    }
    if (paths.length >= ofs + 5) {
      img.setText(paths[ofs + 4]);
    }

    return img;
  }

  public String getText() {
    if (text != null)
      return text;
    return "" + width + "x" + height;
  }

  public void drawCenteredString(String s, int w, int h, Graphics2D g) {

    int proz = 80;
    while (proz > 10) {
      final int fheight = (Math.min(h, w) * proz) / 100;
      if (fheight < 3)
        return;
      final Font font = new Font("Sans-Serif", Font.PLAIN, fheight);
      g.setFont(font);
      final FontMetrics fm = g.getFontMetrics();
      final int sw = fm.stringWidth(s);
      if (sw < w) {
        final int x = (w - sw) / 2;
        final int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(s, x, y);
        break;
      }
      proz -= 5;
    }
  }

  public BufferedImage drawImage() {
    if (width > 2048 || height > 2048) {
      throw new RuntimeErrorException(new Error("Image too big max 2048x2048:"
          + width + "x" + height));
    }
    final BufferedImage image = new BufferedImage(width, height,
        this.format.getColorSpace());
    final Graphics2D graph = image.createGraphics();
    graph.setPaint(backcolor);
    graph.fillRect(0, 0, width, height);
    graph.setColor(textcolor);
    drawCenteredString(this.getText(), width, height, graph);
    return image;
  }
}
