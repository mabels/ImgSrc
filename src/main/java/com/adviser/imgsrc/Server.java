package com.adviser.imgsrc;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Server {

  public static void main(String[] args) {
    final CamelContext camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    camelContext.addComponent("jetty", new JettyHttpComponent());
    try {
      camelContext.addRoutes(new Router(args));
      camelContext.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
