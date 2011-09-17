package com.adviser.imgsrc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import lombok.Data;
import lombok.val;

@Data
public class Image {
  private boolean redirect = false;
  private int width = 100;
  private int height = 100;
  private Color backcolor = new Color(0x444444);
  private Color textcolor = new Color(0xffffff);
  private String text = null;
  @SuppressWarnings("unused")
  private String path;

  private Format format = null;


  public String getPath() {
    final StringBuffer sb = new StringBuffer();
    sb.append("/");
    sb.append(Integer.toString(this.getWidth()));
    sb.append("/");
    sb.append(Integer.toString(this.getHeight()));
    sb.append("/");
    sb.append(Integer.toHexString(this.getBackcolor().getRGB()));
    sb.append("/");
    sb.append(Integer.toHexString(this.getTextcolor().getRGB()));
    sb.append("/");
    sb.append(this.getText());
    sb.append(this.getFormat().getSuffix());
    return sb.toString();
  }

  private boolean frame = false;
  private static Pattern _frame = Pattern.compile("(.*)\\.[xX](.*)");

  private String hasFrame(String path) {
    Matcher match = _frame.matcher(path);
    if (match.matches()) {
      frame = true;
      return match.group(1) + match.group(2);
    }
    return path;
  }

  private static Steps<Image> _steps = null;
  

  private static Steps<Image> getSteps() {
    if (_steps != null) { return _steps; }
    _steps = new Steps<Image>();
    
    _steps.add(new Step<Image>("Width") {
      public Step<Image> parse(Image img, String data) {
        IsWhat iw = new IsWhat(data);
        if (iw.getDim() != null) {
          img.setWidth(iw.getDim().intValue());
          img.setHeight(img.getWidth());
          return _steps.getStepByName("Height");
        }
        if (iw.assignBackColor(img)) {
          return _steps.getStepByName("TextColor");
        }
        return iw.assignText(img);
      }       
    });
    _steps.add(new Step<Image>("Height") {
      public Step<Image> parse(Image img, String data) {
        IsWhat iw = new IsWhat(data);
        if (iw.getDim() != null) {
          img.setHeight(iw.getDim().intValue());
          return _steps.getStepByName("BackColor");
        }
        if (iw.assignBackColor(img)) {
          return _steps.getStepByName("TextColor");
        }
        return iw.assignText(img);
      }       
    });
    _steps.add(new Step<Image>("BackColor") {
      public Step<Image> parse(Image img, String data) {
        IsWhat iw = new IsWhat(data);
        if (iw.assignBackColor(img)) {
          return _steps.getStepByName("TextColor");
        }
        return iw.assignText(img);
      }       
    });
    _steps.add(new Step<Image>("TextColor") {
      public Step<Image> parse(Image img, String data) {
        IsWhat iw = new IsWhat(data);
        if (iw.getColor() != null) {
          img.setTextcolor(iw.getColor());
          return _steps.getStepByName("Text");
        }
        return iw.assignText(img);
      }       
    });
    _steps.add(new Step<Image>("Text") {
      public Step<Image> parse(Image img, String data) {
       img.setText(data);
       return null;
      }       
    });
    return _steps;
  }

  public static Image fromPath(String path) {
    final val img = new Image();
    /*
     * /height/width/backcolor/textcolor/text<.format>
     */
    img.setFormat(Format.fromPath(path, img));
    path = img.getPath();
    path = img.hasFrame(path);

    LinkedList<String> datas = Steps.splitPath(path);
    getSteps().parse(img, datas);
    return img;
  }

  private static Pattern _space = Pattern.compile("^[\\._\\-\\*\\+ ]$");

  public String getText() {
    if (text != null) {
      if (_space.matcher(text).matches()) {
        return "";
      }
      return text;
    }
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

  public ByteArrayOutputStream getStream() throws IOException {
    return getFormat().getStream(drawImage());
  }

  public BufferedImage drawImage() {
    if (width > 2048 || height > 2048) {
      throw new RuntimeErrorException(new Error("Image too big max 2048x2048:"
          + width + "x" + height));
    }
    final BufferedImage image = new BufferedImage(width, height,
        this.getBackcolor().getColorSpace().getType());
    final Graphics2D graph = image.createGraphics();
    if (this.isFrame()) {
      graph.setPaint(textcolor);
      graph.fillRect(0, 0, width, height);
      graph.setPaint(backcolor);
      graph.fillRect(5, 5, width - 10, height - 10);
      graph.setPaint(textcolor);
      graph.drawLine(0, 0, width, height);
      graph.drawLine(width, 0, 0, height);

    } else {
      graph.setPaint(backcolor);
      graph.fillRect(0, 0, width, height);
    }
    graph.setColor(textcolor);
    drawCenteredString(this.getText(), width, height, graph);
    return image;
  }
}
