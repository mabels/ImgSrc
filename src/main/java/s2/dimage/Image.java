package s2.dimage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.management.RuntimeErrorException;

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
    } else if (s.length() == 8) {
      rgb = ((val >> 0) & 0xff) << 24 | ((val >> 8) & 0xff) << 0
          | ((val >> 16) & 0xff) << 8 | ((val >> 24) & 0xff) << 16;
    }
    System.out.println("RGBA:" + Integer.toHexString(val) + ":"
        + Integer.toHexString(rgb));
    return new Color(rgb);
  }

  private Color asColor(String s) {
    if ((s.length() == 4 && s.matches("\\p{XDigit}{4}"))
        || (s.length() == 8 && s.matches("\\p{XDigit}{8}"))) {
      return asABGRColor(s);
    }
    if ((s.length() == 3 && s.matches("\\p{XDigit}{3}"))
        || (s.length() == 6 && s.matches("\\p{XDigit}{6}"))) {
      return asRGBColor(s);
    }
    return null;
  }

  private void setTextNoFormat(String s) {
    text = s.replaceFirst("\\.\\S{3,4}$", "");
  }

  public static Image fromPath(String path) {
    final val img = new Image();
    img.setFormat(Format.fromPath(path));
    /*
     * /height/width/backcolor/textcolor/text<.format>
     */
    path = path.trim().replaceFirst("^/+", "");
    String[] paths = path.split("[/]+", 0);
    if (paths.length == 1 && paths[0].length() == 0) {
      paths = new String[0];
    }
    if (paths.length >= 1) {
      img.setWidth(Integer.parseInt(paths[0]));
      img.setHeight(img.getWidth());
    }
    if (paths.length >= 2) {
      try {
        img.setWidth(Integer.parseInt(paths[1]));
      } catch (Exception e) {
        img.setTextNoFormat(paths[1]);
      }
    }
    if (paths.length >= 3) {
      val color = img.asColor(paths[2]);
      if (color != null)
        img.setBackcolor(color);
      else
        img.setTextNoFormat(paths[2]);
    }
    if (paths.length >= 4) {
      val color = img.asColor(paths[3]);
      if (color != null)
        img.setTextcolor(color);
      else
        img.setTextNoFormat(paths[3]);
    }
    if (paths.length >= 5) {
      img.setTextNoFormat(paths[4]);
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
