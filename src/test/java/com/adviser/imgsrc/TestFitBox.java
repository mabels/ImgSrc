package com.adviser.imgsrc;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;


public class TestFitBox {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestFitBox.class);

  private BufferedImage image;
  private Graphics2D graph;
  private Font font;

  TestFitBox() {
    this.imageContext();
  }

  public void imageContext() {
    this.image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    this.graph = image.createGraphics();
    this.font = new Font("Sans-Serif", Font.PLAIN, 10);
    // System.err.println("XXXXXXXX"+font);
    if (this.font == null) {
      throw new RuntimeErrorException(new Error("Font not found:"));
    }
  }

  private static final int[] refSizes1Line = { 299, 258, 179, 130, 112, 100,
      76, 66, 60, 53, 49, 47, 43, 39, 36, 35, 33, 31, 30, 28, 26, 26, 25, 25,
      23, 22, 22, 21, 20, 20, 19, 18, 18, 17, 17, 17, 16, 16, 15, 15, 14, 14,
      14, 13, 13, 13, 13, 12, 12, 12, 12, 12, 11, 11, 11, 11, 11, 10, 10, 10,
      10, 10, 9, 9, 9, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5,
      5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, -1, -1, -1, -1, -1 };

  // private static final int referenceSizes[][] =
  @Test
  public void testHorizontal1Line() {
    final String text = "Sucht macht doof arbeit der sack ist doof oder vielleicht auch nicht macht doof leben heut ist kein guter tag macht doof und es ist nicht langweilig warum wird da bloss nix drauss";
    System.err.println("UUUUUUU"+font);
    FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    StringBuilder sb = new StringBuilder(); // generate reference array
    sb.append("{");
    String comma = "";
    for (int i = 1; i < text.length(); ++i) {
      fitbox.setLines(new String[] { text.substring(0, i) });
      int fontSize = fitbox.findHorizontalFontsize();
      // LOGGER.debug("*********** Tried length:"+i+" got " + fontSize);
      sb.append(comma + fontSize);
      comma = ",";
      assertEquals((double) refSizes1Line[i - 1], (double) fontSize, 5 + (refSizes1Line[i - 1] * 0.1));
      // System.err.println("RESULT:"+fontSize);
      // assertEquals(10, fontSize);
    }
    sb.append("}");
    LOGGER.debug(sb.toString());
  }

  private static final int refSizes2Line[] = { 299, 208, 139, 104, 83, 69, 58,
      51, 46, 42, 38, 35, 32, 29, 28, 25, 24, 22, 21, 21, 20, 18, 18, 17, 17,
      15, 15, 14, 14, 14, 13, 13, 13, 11, 11, 11, 11, 10, 10, 10, 10, 10, 8, 8,
      8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

  @Test
  public void testHorizontal1EmptyString() {
    FitBox fitbox = new FitBox(graph, this.font);
    fitbox.setLines(new String[] { "", "" });
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    int fontSize = fitbox.findHorizontalFontsize();
    assertEquals(FitBox.TEXTTOOSMALL, fontSize);
  }

  @Test
  public void testHorizontal2Line() {
    final String line1 = "Sucht macht doof arbeit der sack ist doof oder vielleicht auch nicht macht doof leben heut ist kein guter tag macht doof und es ist nicht langweilig warum wird da bloss nix drauss";
    final StringBuilder widestLine = new StringBuilder();
    for (int i = 0; i < line1.length(); ++i) {
      widestLine.append("G"); // uppercase G are usally the widest chars
    }
    FitBox fitbox = new FitBox(graph, this.font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    StringBuilder sb = new StringBuilder(); // generate reference array
    sb.append("{");
    String comma = "";
    for (int i = 1; i < line1.length(); ++i) {
      fitbox.setLines(new String[] { line1.substring(0, i),
          widestLine.substring(0, i) });
      int fontSize = fitbox.findHorizontalFontsize();
      // LOGGER.debug("*********** Tried length:"+i+" got " + fontSize);
      sb.append(comma + fontSize);
      comma = ",";
      assertEquals((double) refSizes2Line[i - 1], (double) fontSize, 1 + (refSizes2Line[i - 1] * 0.1));
      // System.err.println("RESULT:"+fontSize);
      // assertEquals(10, fontSize);
    }
    sb.append("}");
    LOGGER.debug(sb.toString());
  }

  @Test
  public void testLinesNotSet() {
    FitBox fitbox = new FitBox(graph, this.font);
    fitbox.setBox(new Rectangle(30, 30, FitBox.MINIMUMFONTSIZE, 300));
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findBoxFontsize());
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findHorizontalFontsize());
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findVerticalFontsize());
  }

  @Test
  public void testLinesEmpty() {
    FitBox fitbox = new FitBox(graph, this.font);
    fitbox.setBox(new Rectangle(30, 30, FitBox.MINIMUMFONTSIZE, 300));
    fitbox.setLines(new String[0]);
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findBoxFontsize());
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findHorizontalFontsize());
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findVerticalFontsize());
  }

  @Test
  public void testBoxNotSet() {
    FitBox fitbox = new FitBox(graph, this.font);
    fitbox.setLines(new String[] { "" });
    try {
      fitbox.findHorizontalFontsize();
      assertNotNull(fitbox); // should never reached
    } catch (FitBox.NotBoxSet e) {
      assertNotNull(e);
    }
    try {
      fitbox.findVerticalFontsize();
      assertNotNull(fitbox); // should never reached
    } catch (FitBox.NotBoxSet e) {
      assertNotNull(e);
    }
    try {
      fitbox.findBoxFontsize();
      assertNotNull(fitbox); // should never reached
    } catch (FitBox.NotBoxSet e) {
      assertNotNull(e);
    }
  }

  private static final int refVerticalSizes[] = { -1, 254, 127, 84, 63, 50, 42,
      35, 31, 27, 24, 22, 20, 19, 17, 16, 14, 14, 13, 12, 12, 11, 10, 10, 9, 9,
      9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

  @Test
  public void testVerticalLines() {
    final FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    final List<String> lines = new ArrayList<String>();
    final StringBuilder sb = new StringBuilder();
    sb.append("{");
    String comma = "";
    for (int i = 0; i < 100; ++i) {
      String[] strArray = new String[lines.size()];
      fitbox.setLines(lines.toArray(strArray));
      final int fontSize = fitbox.findVerticalFontsize();
      assertEquals((double) refVerticalSizes[i], (double) fontSize, 1 + (refVerticalSizes[i]*0.1));
//      assertThat((double)fontSize, allOf(
//          greaterThen(refVerticalSizes[i] - (refVerticalSizes[i] * 0.1)),
//          (double) fontSize, 1);
      // LOGGER.debug("testVerticalLines="+i+":"+fontSize);
      lines.add("Huh");
      sb.append(comma + fontSize);
      comma = ",";
    }
    sb.append("}");
    LOGGER.debug(sb.toString());
  }

  private static final int refBoxSizes[] = { -1, 126, 83, 62, 49, 41, 34, 30,
      26, 23, 21, 19, 18, 16, 15, 13, 13, 12, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7,
      7, 7, 6, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

  @Test
  public void testFixBoxToBig() {
    final FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    final int count = 61;
    final String lines[] = new String[count];
    final StringBuilder line = new StringBuilder();
    for (int chars = 0; chars < count; ++chars) {
      lines[chars] = line.toString();
      line.append("G");
    }
    fitbox.setLines(lines);
    final int fontSize = fitbox.findBoxFontsize();
    assertEquals(FitBox.TEXTTOOSMALL, fontSize);
  }

  @Test
  public void testFitBox() {
    final StringBuilder line = new StringBuilder();
    final List<String> lines = new ArrayList<String>();
    final StringBuilder sb = new StringBuilder();
    sb.append("{");
    String comma = "";
    final FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));

    for (int chars = 0; chars < 100; ++chars) {
      lines.add(line.toString());
      String[] strArray = new String[lines.size()];
      fitbox.setLines(lines.toArray(strArray));
      line.append("G");
      final int fontSize = fitbox.findBoxFontsize();
      assertEquals((double) refBoxSizes[chars], (double) fontSize, 1);
      sb.append(comma + fontSize);
      comma = ",";
    }
    sb.append("}");
    LOGGER.debug(sb.toString());
  }

  @Test
  public void testBoxSmallerThanMINIMUM() {
    FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, FitBox.MINIMUMFONTSIZE, 300));
    fitbox.setLines(new String[] { "Meno" });
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findBoxFontsize());
    fitbox.setBox(new Rectangle(30, 30, 300, FitBox.MINIMUMFONTSIZE));
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findBoxFontsize());
    fitbox.setBox(new Rectangle(30, 30, FitBox.MINIMUMFONTSIZE,
        FitBox.MINIMUMFONTSIZE));
    assertEquals(FitBox.TEXTTOOSMALL, fitbox.findBoxFontsize());
   }


  @Test
  public void testLeftAlign() {
    FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    fitbox.setLines(new String[] { "Meno" });
    FitBox.Aligner aligner = new FitBox.Aligner(fitbox);
    FitBox.Aligner.Align align = aligner.getAlign("<Meno");
    assertEquals("Meno", align.getLine());
    assertEquals(30, align.getStartX());
    aligner.getAlign("Uhu");
    assertEquals("Uhu", align.getLine());
    assertEquals(30, align.getStartX());
    aligner.getAlign("<Uhu");
    assertEquals("Uhu", align.getLine());
    assertEquals(30, align.getStartX());
  }


  @Test
  public void testRightAlign() {
    FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    fitbox.setLines(new String[] { "Meno" });
    FitBox.Aligner aligner = new FitBox.Aligner(fitbox);
    FitBox.Aligner.Align align = aligner.getAlign(">Meno");
    assertEquals("Meno", align.getLine());
    assertThat(align.getStartX(), allOf(greaterThan(290), lessThan(300)));
    aligner.getAlign("oneM");
    assertEquals("oneM", align.getLine());
    assertThat(align.getStartX(), allOf(greaterThan(290), lessThan(300)));
    aligner.getAlign(">oneM");
    assertEquals("oneM", align.getLine());
    assertThat(align.getStartX(), allOf(greaterThan(290), lessThan(300)));
  }


  @Test
  public void testCenterAlign() {
    FitBox fitbox = new FitBox(graph, font);
    fitbox.setBox(new Rectangle(30, 30, 300, 300));
    fitbox.setLines(new String[] { "Meno" });
    FitBox.Aligner aligner = new FitBox.Aligner(fitbox);
    FitBox.Aligner.Align align = aligner.getAlign("Meno");
    assertEquals("Meno", align.getLine());
    assertEquals(165, align.getStartX(), 165 * 0.1);
    aligner.getAlign("=oneM");
    assertEquals("oneM", align.getLine());
    assertEquals(165, align.getStartX(), 165 * 0.1);
    aligner.getAlign("uneM");
    assertEquals("uneM", align.getLine());
    assertEquals(165, align.getStartX(), 165 * 0.1);
  }

}
