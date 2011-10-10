package com.adviser.imgsrc;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.HeaderFilterStrategy;

public class Server {

  public static void main(String[] args) {
    final CamelContext camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    final JettyHttpComponent jhc = new JettyHttpComponent();
    final HeaderFilterStrategy hfs = new MyFilterStrategy();
    jhc.setHeaderFilterStrategy(hfs);
    camelContext.addComponent("jetty", jhc);
    try {
      camelContext.addRoutes(new Router(args));
      camelContext.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
