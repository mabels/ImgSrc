package com.adviser.imgsrc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.regex.Pattern;

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
  private static final int UP4BIT = 4;
  private static final int RBIT = 8;
  private static final int GBIT = 4;
  private static final int BBIT = 0;
  private static final int AABIT = 24; // AlphaBits
  private static final int RRBIT = 16;
  private static final int GGBIT = 8;
  private static final int BBBIT = 0;

  private Integer colorSpace = null;

  public boolean assignBackColor(Image img) {
    if (getColor() != null) {
      if (colorSpace != null) {
        img.setColorSpace(colorSpace);
      }
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
    if (s.length() == 1) {
      int r = (int) (val & FOURBITS);
      int g = (int) (val & FOURBITS);
      int b = (int) (val & FOURBITS);
      rgb = (r | r << UP4BIT) << RRBIT | (g | g << UP4BIT) << GGBIT | (b | b << UP4BIT) << BBBIT;
    } else if (s.length() == 3) {
      int r = (int) ((val >> RBIT) & FOURBITS);
      int g = (int) ((val >> GBIT) & FOURBITS);
      int b = (int) ((val >> BBIT) & FOURBITS);
      rgb = (r | r << UP4BIT) << RRBIT | (g | g << UP4BIT) << GGBIT | (b | b << UP4BIT) << BBBIT;
    } else if (s.length() == 6) {
      rgb = (int) val;
    }
    return new Color(rgb);
  }

  private Color asRGBAColor(String s) {
    String[] parts = s.split(",");
    int precend = (int) ((Integer.parseInt(parts[1], 10) / 100f) * 255f);
    final Color temp = asRGBColor(parts[0]);
    colorSpace = BufferedImage.TYPE_4BYTE_ABGR;
    return new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), precend);
  }

  private Color asABGRColor(String s) {
    colorSpace = BufferedImage.TYPE_4BYTE_ABGR;
    //System.err.println("asABGRColor:");
    int rgb = 0xFFCCCCC;
    long val = Long.parseLong(s.toUpperCase(), 16);
    if (s.length() == 2 || s.length() == 4) {
      if (s.length() == 2) {
        // Blow up the rgb value;
        val = (((val >> 4) & FOURBITS) << ALPHABIT)
            | (((val >> 0) & FOURBITS) << RBIT)
            | (((val >> 0) & FOURBITS) << GBIT)
            | (((val >> 0) & FOURBITS) << BBIT);
      }
      int a = (int) ((val >> ALPHABIT) & FOURBITS);
      int r = (int) ((val >> RBIT) & FOURBITS);
      int g = (int) ((val >> GBIT) & FOURBITS);
      int b = (int) ((val >> BBIT) & FOURBITS);
      rgb = (a | a << UP4BIT) << AABIT | (r | r << UP4BIT) << RRBIT | (g | g << UP4BIT) << GGBIT
          | (b | b << UP4BIT) << BBBIT;
      // System.out.println("RGBA:" + Integer.toHexString(val) + ":"
      // + Integer.toHexString(rgb));
      return new Color(rgb, true);
    } else if (s.length() == 8) {
      rgb = (int) val;
      return new Color(rgb, true);
    }
    return new Color(rgb);
  }

  private static final Random RAND = new Random(System.currentTimeMillis());

  private static final Pattern RE1ER = Pattern.compile("\\p{XDigit}{1}");
  private static final Pattern RE2ER = Pattern.compile("\\p{XDigit}{2}");
  private static final Pattern RE4ER = Pattern.compile("\\p{XDigit}{4}");
  private static final Pattern RE8ER = Pattern.compile("\\p{XDigit}{8}");
  private static final Pattern RE3ER = Pattern.compile("\\p{XDigit}{3}");
  private static final Pattern RE6ER = Pattern.compile("\\p{XDigit}{6}");
  private static final Pattern RE1RGBA = Pattern
      .compile("^\\p{XDigit}{1},\\p{Digit}{1,2}$");
  private static final Pattern RE3RGBA = Pattern
      .compile("^\\p{XDigit}{3},\\p{Digit}{1,2}$");
  private static final Pattern RE6RGBA = Pattern
      .compile("^\\p{XDigit}{6},\\p{Digit}{1,2}$");

  private Color asColor(String param) {
    String s = param; //sonar
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
    if ((len == 2 && RE2ER.matcher(s).matches())
        || (len == 4 && RE4ER.matcher(s).matches())
        || (len == 8 && RE8ER.matcher(s).matches())) {
      ret = asABGRColor(s);
    } else if ((len == 3 && RE3ER.matcher(s).matches())
        || (len == 6 && RE6ER.matcher(s).matches())
        || (len == 1 && RE1ER.matcher(s).matches())) {
      ret = asRGBColor(s);
    } else if (RE1RGBA.matcher(s).matches() || RE3RGBA.matcher(s).matches()
        || RE6RGBA.matcher(s).matches()) {
      ret = asRGBAColor(s);
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

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public String getText() {
    return text;
  }

  public final void setText(String text) {
    this.text = text;
  }

  public boolean isRedirect() {
    return redirect;
  }

  public void setRedirect(boolean redirect) {
    this.redirect = redirect;
  }

}
