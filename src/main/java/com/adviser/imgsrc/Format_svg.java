package com.adviser.imgsrc;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

//@Suffix(".gif")
class Format_svg extends Format {

  public static class RenderSvg implements Render {
    //    private final RenderPixel render;
    private SVGGraphics2D svgGenerator;

    RenderSvg(Format format) {
//      this.render = new RenderPixel(format);
    }

    public Graphics2D getGraphics2D(int width, int height, int colorSpace) {
      DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
      // Create an instance of org.w3c.dom.Document.
      String svgNS = "http://www.w3.org/2000/svg";
      Document document = domImpl.createDocument(svgNS, "svg", null);
      // Create an instance of the SVG Generator.
      this.svgGenerator = new SVGGraphics2D(document);
      Element root = svgGenerator.getRoot();
      root.setAttributeNS(null, "viewBox", "0 0 " + width + " " + height);
      svgGenerator.setSVGCanvasSize(new Dimension(width, height));
      return svgGenerator;
    }

    public ByteArrayOutputStream getStream() throws IOException {
      ByteArrayOutputStream ret = new ByteArrayOutputStream();
      Writer out = new OutputStreamWriter(ret, "UTF-8");
      svgGenerator.stream(out, true);
      return ret;
    }
  }

  private final String format = "SVG";
  private final String mime = "image/svg+xml";
  private final int colorSpace = BufferedImage.TYPE_INT_RGB;
  private final String suffix = ".svg";

  @Override
  public Render createRender() {
    return new RenderSvg(this);
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
