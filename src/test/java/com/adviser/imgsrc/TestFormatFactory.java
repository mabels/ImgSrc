package com.adviser.imgsrc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class TestFormatFactory {

  @Test
  public void testPng() {
    Image img = new Image();
    assertEquals(".png", Format.fromPath("/meno/doof.png/lala", img).getSuffix());  
    assertEquals(".png", Format.fromPath("/meno/doof.png", img).getSuffix());  
  }
  @Test
  public void testIco() {
    Image img = new Image();
    assertEquals(".ico", Format.fromPath("/meno/doof.ico/lala", img).getSuffix());  
    assertEquals(".ico", Format.fromPath("/meno/doof.ico", img).getSuffix());  
  }
  @Test
  public void testGif() {
    Image img = new Image();
    assertEquals(".gif", Format.fromPath("/meno/doof.gif/lala", img).getSuffix());  
    assertEquals(".gif", Format.fromPath("/meno/doof.gif", img).getSuffix());  
  }
  @Test
  public void testJpg() {
    Image img = new Image();
    assertEquals(".jpg", Format.fromPath("/meno/doof.jpg/lala", img).getSuffix());  
    assertEquals(".jpg", Format.fromPath("/meno/doof.jpg", img).getSuffix());  
  }
  @Test
  public void testSvg() {
    Image img = new Image();
    assertEquals(".svg", Format.fromPath("/meno/doof.svg/lala", img).getSuffix());
    assertEquals(".svg", Format.fromPath("/meno/doof.svg", img).getSuffix());
  }
}
