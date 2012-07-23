package com.adviser.imgsrc;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FitBox {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  private final Graphics2D g;
  private final Font font;
  private String[] lines;
  private Rectangle box;
  protected static final int MINIMUMFONTSIZE = 4;
  protected static final int TEXTTOOSMALL = -1;

  public FitBox(Graphics2D g, Font font) {
    this.g = g;
    this.font = font;
  }

  protected static class Aligner {

    private final FontMetrics fm;
    private int startY;
    private final FitBox fitbox;

    private Align currentAlign;

    public Aligner(FitBox fitbox) {
      this.fitbox = fitbox;
      fm = fitbox.g.getFontMetrics();

      startY = fitbox.box.y
          + ((fitbox.box.height / 2) - (fitbox.lines.length
              * (fm.getAscent() + fm.getDescent() + fm.getLeading()) / 2))
          + fm.getAscent();
      currentAlign = getAlign("="); // default center align
    }

    protected abstract class Align {
      private String line;

      public abstract int getStartX();

      public String getLine() {
        return line;
      }
      /*
      public void setLine(String line) {
        this.line = line;
      }
      */
    }

    private final Align rightAlign = new Align() {
      public int getStartX() {
        return fitbox.box.x + fitbox.box.width - fm.stringWidth(getLine());
      }
    };
    private final Align centerAlign = new Align() {
      public int getStartX() {
        return fitbox.box.x
            + ((fitbox.box.width / 2) - (fm.stringWidth(getLine()) / 2));
      }
    }; 
    private final Align leftAlign = new Align() {
      public int getStartX() {
        return fitbox.box.x;
      }
    };
    
    public Align getAlign(String line) {
      if (line == null || line.length() == 0) {
        return currentAlign;
      }

      switch (line.charAt(0)) {
      case '=':
        currentAlign = centerAlign;
        currentAlign.line = line.substring(1);
        break;
      case '<':
        currentAlign = leftAlign;
        currentAlign.line = line.substring(1);
        break;
      case '>':
        currentAlign = rightAlign;
        currentAlign.line = line.substring(1);
        break;
      default:
        currentAlign.line = line;
        break;
      }
      return currentAlign;
    }

    public void draw(String line) {
      final Align align = getAlign(line);

      fitbox.g.drawString(align.getLine(), align.getStartX(), startY);
      startY += fm.getAscent() + fm.getDescent() + fm.getLeading();
    }
  }

  public void draw() {
    final int fontSize = findBoxFontsize();
    if (fontSize < 0) {
      LOGGER.debug("text too small");
      return;
    }
    final Font myFont = font.deriveFont((float) fontSize);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setFont(myFont);
    Aligner aligner = new Aligner(this);
    for (String line : lines) {
      aligner.draw(line);
    }
  }

  private abstract class Orientation {

    public Orientation() throws NotBoxSet {
      if (box == null) {
        throw new NotBoxSet();
      }
    }

    public int findFontsize(int startSize) {
      if (lines == null || lines.length == 0) {
        return TEXTTOOSMALL;
      }
      if (startSize < MINIMUMFONTSIZE) {
        return TEXTTOOSMALL;
      }
      return findFontsizeFrom(MINIMUMFONTSIZE, startSize,
          ((startSize - MINIMUMFONTSIZE) / 2) + MINIMUMFONTSIZE);
    }

    private int findFontsizeFrom(int minFontSize, int maxFontSize, int fontSize) {
      final boolean fits = fitText(fontSize);
      if (fits) {
        int nextFontSize = ((maxFontSize - fontSize) / 2) + fontSize;
        if (fontSize == nextFontSize) {
          return nextFontSize;
        }
        // LOGGER.debug("try upper half=" + nextFontSize);
        return findFontsizeFrom(fontSize, maxFontSize, nextFontSize);
      } else {
        if (fontSize <= MINIMUMFONTSIZE) {
          LOGGER.debug("can not display text");
          return TEXTTOOSMALL;
        }
        int nextFontSize = ((fontSize - minFontSize) / 2) + minFontSize;
        return findFontsizeFrom(minFontSize, fontSize, nextFontSize);
      }
    }

    protected abstract boolean fitText(int fontSize);
  }

  public static class NotBoxSet extends NullPointerException {
    private static final long serialVersionUID = 1L;
  }

  public int findBoxFontsize() {
    int fontSize = findVerticalFontsize();
    return findHorizontalFontsize(fontSize);
  }

  public int findVerticalFontsize() {
    if (box == null) {
      throw new NotBoxSet();
    }
    return findVerticalFontsize(box.height);
  }

  public int findVerticalFontsize(int maxFontSize) {
    return (new Orientation() {
      public boolean fitText(int fontSize) {
        final Font myFont = font.deriveFont((float) fontSize);
        g.setFont(myFont);
        final FontMetrics fm = g.getFontMetrics();
        final int lineHeight = fm.getAscent() + fm.getDescent()
            + fm.getLeading();
        // LOGGER.debug("lineHeight="+fm.getAscent()+"+"+fm.getDescent()+"+"+fm.getLeading());
        final int height = lineHeight * lines.length;
        return box.height >= height;
      }
    }).findFontsize(maxFontSize);
  }

  public int findHorizontalFontsize() {
    if (box == null) {
      throw new NotBoxSet();
    }
    return findHorizontalFontsize(box.height);
  }

  public int findHorizontalFontsize(int maxFontsize) {
    if (lines == null) {
      return TEXTTOOSMALL;
    }
    boolean foundString = false;
    for (String line : lines) {
      foundString |= line.length() > 0;
    }
    if (!foundString) {
      return TEXTTOOSMALL;
    }
    return (new Orientation() {
      protected boolean fitText(int fontSize) {
        final Font myFont = font.deriveFont((float) fontSize);
        g.setFont(myFont);
        final FontMetrics fm = g.getFontMetrics();

        for (String line : lines) {
          if (fm.stringWidth(line) > box.width) {
            return false;
          }
        }
        return true;
      }
    }).findFontsize(maxFontsize);
  }

  public void setLines(String lines) {
    this.lines = lines.split("[\\n\\r]+");
  }

  public String[] getLines() {
    return lines;
  }
  
  public void setLines(String[] lines) {
    this.lines = lines;
  }
  
  public Rectangle getBox() {
    return box;
  }

  public void setBox(Rectangle box) {
    this.box = box;
  }

}
