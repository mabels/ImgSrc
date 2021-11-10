package com.adviser.imgsrc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestDimension {

  @Test
  public void test10() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "10");
    assertEquals(10, img.getWidth());
    assertEquals(10, img.getHeight());
  }
  
  @Test
  public void test10x20() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "10x20");
    assertEquals(10, img.getWidth());
    assertEquals(20, img.getHeight());
  }

  @Test
  public void test2_1x20() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "2:1x20");
    assertEquals(40, img.getWidth());
    assertEquals(20, img.getHeight());
  }
  @Test
  public void test20x2_1() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "20x2:1");
    assertEquals(20, img.getWidth());
    assertEquals(10, img.getHeight());
  }
  
  @Test
  public void testverticalbanner() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "verticalbanner");
    assertEquals(120, img.getWidth());
    assertEquals(240, img.getHeight());
  }

  @Test
  public void testvertban() {
    Image img = new Image();
    Step<Image> si = Image.getSteps().getStepByName("Dimension");
    si.parse(img, "vertban");
    assertEquals(120, img.getWidth());
    assertEquals(240, img.getHeight());
  }
 
  
}
