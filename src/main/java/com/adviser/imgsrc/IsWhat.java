package com.adviser.imgsrc;

import java.awt.Color;
import java.util.Random;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class IsWhat {
  private Integer width = null;
  private Integer height = null;
  private Color color = null;
  private String text = null;
  private boolean redirect = false;

  private static final int FOURBITS = 0xf;
  private static final int ALPHAMASK = 0xff000000;
  private static final int RGBMASK = 0xffffff;
  private static final int ALPHABIT = 12;
  private static final int RBIT = 8;
  private static final int GBIT = 4;
  private static final int BBIT = 0;

  public boolean assignBackColor(Image img) {
    if (getColor() != null) {
      img.setBackcolor(getColor());
      img.setTextcolor(asInvertColor(getColor()));
      img.orRedirect(this.isRedirect());
      return true;
    }
    return false;
  }

  public Step<Image> assignText(Image img) {
    if (getText() != null) {
      img.setText(getText());
      img.orRedirect(this.isRedirect());
    }
    return null;
  }

  private static Color asInvertColor(Color c) {
    int rgb = c.getRGB();
    int alpha = rgb & ALPHAMASK;
    rgb = alpha | ((~rgb & RGBMASK) & RGBMASK);
    // System.out.println("IN:"+Integer.toHexString(c.getRGB())+" OUT:"+Integer.toHexString(rgb));
    return new Color(rgb);
  }

  private Color asRGBColor(String s) {
    int rgb = 0xCCCCC;
    final long val = Long.parseLong(s.toUpperCase(), 16);
    if (s.length() == 3) {
      int r = (int) ((val >> RBIT) & FOURBITS);
      int g = (int) ((val >> GBIT) & FOURBITS);
      int b = (int) ((val >> BBIT) & FOURBITS);
      rgb = (r | r << 4) << 16 | (g | g << 4) << 8 | (b | b << 4) << 0;
    } else if (s.length() == 6) {
      rgb = (int) val;
    }
    return new Color(rgb);
  }

  private Color asABGRColor(String s) {
    // format.setColorSpace(BufferedImage.TYPE_4BYTE_ABGR);
    int rgb = 0xFFCCCCC;
    final long val = Long.parseLong(s.toUpperCase(), 16);
    if (s.length() == 4) {
      int a = (int) ((val >> ALPHABIT) & FOURBITS);
      int r = (int) ((val >> RBIT) & FOURBITS);
      int g = (int) ((val >> GBIT) & FOURBITS);
      int b = (int) ((val >> BBIT) & FOURBITS);
      rgb = (a | a << 4) << 24 | (r | r << 4) << 16 | (g | g << 4) << 8
          | (b | b << 4) << 0;
      // System.out.println("RGBA:" + Integer.toHexString(val) + ":"
      // + Integer.toHexString(rgb));
      return new Color(rgb, true);
    } else if (s.length() == 8) {
      rgb = (int) val; // (int) (((val >> 0) & 0xff) << 0 | ((val >> 8) & 0xff)
                       // << 8
      // | ((val >> 16) & 0xff) << 16 | ((val >> 24) & 0xff) << 24);
      return new Color(rgb, true);
    }
    return new Color(rgb);
  }

  private static final Random RAND = new Random(System.currentTimeMillis());

  private static final Pattern RE4ER = Pattern.compile("\\p{XDigit}{4}");
  private static final Pattern RE8ER = Pattern.compile("\\p{XDigit}{8}");
  private static final Pattern RE3ER = Pattern.compile("\\p{XDigit}{3}");
  private static final Pattern RE6ER = Pattern.compile("\\p{XDigit}{6}");

  private Color asColor(String s) {
    final char first = s.charAt(0);
    int len = s.length();
    boolean random = false;
    if (len > 0 && (first == 'r' || first == 'R')) {
      this.setRedirect(true);
      s = s.substring(1);
      len = s.length();
      random = true;
    }
    Color ret = null;
    if ((len == 4 && RE4ER.matcher(s).matches())
        || (len == 8 && RE8ER.matcher(s).matches())) {
      ret = asABGRColor(s);
    } else if ((len == 3 && RE3ER.matcher(s).matches())
        || (len == 6 && RE6ER.matcher(s).matches())) {
      ret = asRGBColor(s);
    }
    if (random && ret != null) {
      int r = RAND.nextInt() & FOURBITS;
      for (int i = 0; i < r; ++i) {
        if ((r & 0x8) != 0) {
          ret = ret.brighter();
        } else {
          ret = ret.darker();
        }
      }
    }
    return ret;
  }


  public IsWhat(String data) {
    this.color = asColor(data);
    if (this.color == null) {
      setText(data);
    }

  }

}
