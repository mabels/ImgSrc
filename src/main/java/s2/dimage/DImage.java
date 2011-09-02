package s2.dimage;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class DImage {

    
  
 /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    CamelContext camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    camelContext.addComponent("jetty", new JettyHttpComponent());  
    try {
      camelContext.addRoutes(new Router());
      camelContext.start();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 }

}
