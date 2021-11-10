package com.adviser.imgsrc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TestIsWhat {

  @Test
  public void testRE1RGBA() {
    IsWhat iw = new IsWhat("A,50");
    assertEquals(0x7faaaaaa, iw.getColor().getRGB());
  }
  
  @Test
  public void testRE3RGBA() {
    IsWhat iw = new IsWhat("ABC,50");
    assertEquals(0x7faabbcc, iw.getColor().getRGB());
  }
  
  @Test
  public void testRE6RGBA() {
    IsWhat iw = new IsWhat("DEADBE,50");
    assertEquals(0x7fdeadbe, iw.getColor().getRGB());
  }
  
  
  @Test
  public void testRE1ER() {
    IsWhat iw = new IsWhat("A");
    assertEquals(0xaaaaaa, iw.getColor().getRGB()&0x00ffffff);
  }

  @Test
  public void testRE2ER() {
    IsWhat iw = new IsWhat("BA");
    assertEquals(0xbbaaaaaa, iw.getColor().getRGB());
  }
  
  @Test
  public void testRE3ER() {
    IsWhat iw = new IsWhat("ABC");
    assertEquals(0xaabbcc, iw.getColor().getRGB()&0x00ffffff);
  }

  @Test
  public void testRE4ER() {
    IsWhat iw = new IsWhat("8ABC");
    assertEquals(0x88aabbcc, iw.getColor().getRGB());
 }
  
  @Test
  public void testRE6ER() {
    IsWhat iw = new IsWhat("DEADBE");
    assertEquals(0xdeadbe, iw.getColor().getRGB()&0x00ffffff);
  }
  
  @Test
  public void testRE8ER() {
    IsWhat iw = new IsWhat("DEADBEEF");
    assertEquals(0xdeadbeef, iw.getColor().getRGB());
  }
}
