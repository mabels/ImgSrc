package com.adviser.imgsrc;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultHeaderFilterStrategy;

public class MyFilterStrategy extends DefaultHeaderFilterStrategy {
  public MyFilterStrategy() {
    initialize();
  }

  protected void initialize() {
    getOutFilter().add("content-length");
    getOutFilter().add("content-type");
    getOutFilter().add(Exchange.HTTP_RESPONSE_CODE);
    setLowerCase(true);
  }

}
