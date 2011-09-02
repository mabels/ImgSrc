package s2.dimage;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class DImage {

  public static void main(String[] args) {
    // TODO Auto-generated method stub

    final CamelContext camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    camelContext.addComponent("jetty", new JettyHttpComponent());
    try {
      camelContext.addRoutes(new Router());
      camelContext.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
