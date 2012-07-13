package com.adviser.imgsrc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

public class Image {
  // private static final Logger LOGGER = LoggerFactory.getLogger(Image.class);

  private static final int MAXDIM = 4096;
  private boolean redirect = false;
  private static final int DEFAULTDIM = 100;
  private int width = DEFAULTDIM;
  private int height = DEFAULTDIM;
  private Color backcolor = new Color(0x444444);
  private Color textcolor = new Color(0xffffff);
  private String text = null;
  private String path = null;
  private int wait = 0;
  private int colorSpace;

  private Format format = null;

  public void setWait(Object o) {
    // LOGGER.debug("setWait:"+o);
    if (o == null) {
      return;
    }
    try {
      wait = Integer.parseInt((String) o);
    } catch (Exception e) {
      wait = (int) (Math.random() * 1000);
    }
  }

  public boolean shouldWait() {
    return wait != 0;
  }

  public boolean orRedirect(boolean val) {
    redirect |= val;
    return redirect;
  }

  public String getFullPath() {
    final StringBuffer sb = new StringBuffer();
    sb.append("/");
    sb.append(Integer.toString(this.getWidth()));
    sb.append("x");
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
  private static final Pattern REFRAME = Pattern.compile("(.*)\\.[xX](.*)");

  private String hasFrame(String path) {
    Matcher match = REFRAME.matcher(path);
    if (match.matches()) {
      frame = true;
      return match.group(1) + match.group(2);
    }
    return path;
  }

  private static Steps<Image> steps = null;

  private static final Pattern RENUMBER = Pattern.compile("\\p{Digit}{1,4}");
  private static final Pattern REWIDTHHEIGHT = Pattern
      .compile("(\\p{Digit}{1,4})[xX](\\p{Digit}{1,4})");
  private static final Pattern REASPECTHEIGHT = Pattern
      .compile("(\\p{Digit}{1,4}):(\\p{Digit}{1,4})[xX](\\p{Digit}{1,4})");
  private static final Pattern REWIDTHASPECT = Pattern
      .compile("(\\p{Digit}{1,4})[xX](\\p{Digit}{1,4}):(\\p{Digit}{1,4})");

  private static class AspectRatio {
    private final String fullName;
    private final String shortName;
    private final Pattern regEx;
    private final String ratio;

    public AspectRatio(String fullName, String shortName, String ratio,
        String regEx) {
      this.fullName = fullName;
      this.shortName = shortName;
      this.regEx = Pattern.compile(regEx);
      this.ratio = ratio;
    }

    public String getFullName() {
      return fullName;
    }

    @SuppressWarnings("unused")
    public String getShortName() {
      return shortName;
    }

    @SuppressWarnings("unused")
    public Pattern getRegEx() {
      return regEx;
    }

    public String getRatio() {
      return ratio;
    }
  }

  private static List<AspectRatio> standardAspectRatios() {
    final List<AspectRatio> ret = new ArrayList<AspectRatio>();
    ret.add(new AspectRatio("mediumrectangle", "medrect", "300x250",
        "^(med)\\w+(rec\\w+)$"));

    ret.add(new AspectRatio("squarepopup", "sqrpop", "250x250", "^(s\\w+pop)$"));
    ret.add(new AspectRatio("verticalrectangle", "vertrec", "240x400",
        "^(ver)\\w+(rec)$"));
    ret.add(new AspectRatio("largerectangle", "lrgrec", "336x280",
        "^(large|lrg)(rec)$"));
    ret.add(new AspectRatio("rectangle", "rec", "180x150", "^(rec)$"));
    ret.add(new AspectRatio("popunder", "pop", "720x300", "^(pop)$"));
    ret.add(new AspectRatio("fullbanner", "fullban", "468x60", "^(f\\w+ban)$"));
    ret.add(new AspectRatio("halfbanner", "halfban", "234x60", "^(h\\w+ban)$"));
    ret.add(new AspectRatio("microbar", "mibar", "88x31", "^(m\\w+bar)$"));
    ret.add(new AspectRatio("button1", "but1", "120x90", "^(b\\w+1)$"));
    ret.add(new AspectRatio("button2", "but2", "120x60", "^(b\\w+2)$"));
    ret.add(new AspectRatio("verticalbanner", "vertban", "120x240",
        "^(ver\\w+ban)$"));
    ret.add(new AspectRatio("squarebutton", "sqrbut", "125x125", "^(s\\w+but)$"));
    ret.add(new AspectRatio("leaderboard", "leadbrd", "728x90", "^(lea\\w+rd)$"));
    ret.add(new AspectRatio("wideskyscraper", "wiskyscrpr", "160x600",
        "^(w\\w+sk\\w+r)$"));
    ret.add(new AspectRatio("skyscraper", "skyscrpr", "120x600", "^(sk\\w+r)$"));
    ret.add(new AspectRatio("halfpage", "hpge", "300x600", "^(h\\w+g)$"));
    return ret;
  }

  private static final List<AspectRatio> ASPECTRATIOS = standardAspectRatios();

  private static float getAspectRatio(String zaehler, String nenner) {
    try {
      return (Integer.parseInt(zaehler) * 1.0f) / Integer.parseInt(nenner);
    } catch (Exception e) {
      return 1;
    }
  }

  private static final String translateStandardRatios(String data, Image img) {
    for (final AspectRatio ar : ASPECTRATIOS) {
      if (ar.getFullName().equalsIgnoreCase(data)
          || ar.getShortName().equalsIgnoreCase(data)) {
        img.setText(data);
        return ar.getRatio();
      }
    }
    return data;
  }

  protected static Steps<Image> getSteps() {
    if (steps != null) {
      return steps;
    }
    steps = new Steps<Image>();

    steps.add(new Step<Image>("Dimension") {

      public Step<Image> parse(Image img, String paramData) {
        String data = translateStandardRatios(paramData, img);
        if (RENUMBER.matcher(data).matches()) {
          Integer dim = Integer.valueOf(Integer.parseInt(data));
          if (0 < dim.intValue() && dim.intValue() < MAXDIM) {
            img.setWidth(dim);
            img.setHeight(dim);
            return steps.getStepByName("BackColor");
          }
        }
        final Matcher aspectheight = REASPECTHEIGHT.matcher(data);
        if (aspectheight.matches()) {
          final int tmpWidth = (int) (Integer.parseInt(aspectheight.group(3)) * getAspectRatio(
              aspectheight.group(1), aspectheight.group(2)));
          data = Integer.toString(tmpWidth) + "x" + aspectheight.group(3);
        } else {
          final Matcher widthaspect = REWIDTHASPECT.matcher(data);
          if (widthaspect.matches()) {
            final int tmpHeight = (int) (Integer.parseInt(widthaspect.group(1)) * getAspectRatio(
                widthaspect.group(3), widthaspect.group(2)));
            data = widthaspect.group(1) + "x" + Integer.toString(tmpHeight);
          }
        }

        final Matcher widthheight = REWIDTHHEIGHT.matcher(data);
        if (widthheight.matches()) {
          img.setWidth(Integer.valueOf(Integer.parseInt(widthheight.group(1))));
          img.setHeight(Integer.valueOf(Integer.parseInt(widthheight.group(2))));
          return steps.getStepByName("BackColor");
        }
        final IsWhat iw = new IsWhat(data);
        if (iw.assignBackColor(img)) {
          return steps.getStepByName("TextColor");
        }
        return iw.assignText(img);
      }
    });
    steps.add(new Step<Image>("BackColor") {
      public Step<Image> parse(Image img, String data) {
        final IsWhat iw = new IsWhat(data);
        if (iw.assignBackColor(img)) {
          return steps.getStepByName("TextColor");
        }
        return iw.assignText(img);
      }
    });
    steps.add(new Step<Image>("TextColor") {
      public Step<Image> parse(Image img, String data) {
        final IsWhat iw = new IsWhat(data);
        if (iw.getColor() != null) {
          img.setTextcolor(iw.getColor());
          return steps.getStepByName("Text");
        }
        return iw.assignText(img);
      }
    });
    steps.add(new Step<Image>("Text") {
      public Step<Image> parse(Image img, String data) {
        img.setText(data);
        return null;
      }
    });
    return steps;
  }

  public static Image fromPath(String paramPath) {
    String path = paramPath;
    final Image img = new Image();
    /*
     * /height/width/backcolor/textcolor/text<.format>
     */
    img.setFormat(Format.fromPath(path, img));
    path = img.getPath();
    path = img.hasFrame(path);

    Deque<String> datas = Steps.splitPath(path);
    getSteps().parse(img, datas);
    return img;
  }

  private static final Pattern RESPACE = Pattern.compile("^[\\._\\-\\*\\+ ]$");

  public String getText() {
    if (text != null) {
      if (RESPACE.matcher(text).matches()) {
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
      if (fheight < 3) {
        return;
      }
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

  private static final int BORDER = 5;

  public BufferedImage drawImage() {
    if (width > MAXDIM || height > MAXDIM) {
      throw new RuntimeErrorException(new Error("Image too big max 4096x4096:"
          + width + "x" + height));
    }
    // System.err.println("XXXX:"+this.getColorSpace()+":"+this.getFormat().getMime());
    final BufferedImage image = new BufferedImage(width, height,
        this.getColorSpace());
    final Graphics2D graph = image.createGraphics();
    if (this.isFrame()) {
      graph.setPaint(textcolor);
      graph.fillRect(0, 0, width, height);
      graph.setPaint(backcolor);
      graph.fillRect(BORDER, BORDER, width - 2 * BORDER, height - 2 * BORDER);
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

  public Color getBackcolor() {
    return backcolor;
  }

  public void setBackcolor(Color backcolor) {
    this.backcolor = backcolor;
  }

  public Color getTextcolor() {
    return textcolor;
  }

  public void setTextcolor(Color textcolor) {
    this.textcolor = textcolor;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
    this.colorSpace = format.getColorSpace();
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWait() {
    return wait;
  }

  public void setWait(int wait) {
    this.wait = wait;
  }

  public boolean isFrame() {
    return frame;
  }

  public void setFrame(boolean frame) {
    this.frame = frame;
  }

  public boolean isRedirect() {
    return redirect;
  }

  public void setRedirect(boolean redirect) {
    this.redirect = redirect;
  }

  public int getColorSpace() {
    return colorSpace;
  }

  public void setColorSpace(int colorSpace) {
    this.colorSpace = colorSpace;
  }
}
