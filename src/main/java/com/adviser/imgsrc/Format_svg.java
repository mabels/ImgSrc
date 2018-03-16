package com.adviser.imgsrc;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Element;

//@Suffix(".gif")
class Format_svg extends Format {
  private final String format = "SVG";
  private final String mime = "image/svg+xml";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".svg";
  private final PixelRender render;
  private SVGGraphics2D svgGenerator;

  Format_svg() {
    this.render = new PixelRender();
  }

  public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    // Create an instance of org.w3c.dom.Document.
    String svgNS = "http://www.w3.org/2000/svg";
    Document document = domImpl.createDocument(svgNS, "svg", null);
    // Create an instance of the SVG Generator.
    this.svgGenerator = new SVGGraphics2D(document);
    Element root = svgGenerator.getRoot();
    root.setAttributeNS(null, "viewBox", "0 0 "+width+" "+height);
    svgGenerator.setSVGCanvasSize(new Dimension(width,height));
    return svgGenerator;
  }

  public ByteArrayOutputStream getStream() throws IOException {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(ret, "UTF-8");
    svgGenerator.stream(out, true);
    return ret;
  }
  
  public String getFormat() {
    return format;
  }
  public String getMime() {
    return mime;
  }
  public int getColorSpace() {
    return colorSpace;
  }
  public String getSuffix() {
    return suffix;
  }

}
