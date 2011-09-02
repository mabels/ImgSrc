package s2.dimage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;

public class Router extends RouteBuilder {
  public void configure() {
    final String base = "127.0.0.1:1147";
    from("jetty:http://" + base + "?matchOnUriPrefix=true")
        .bean(this, "Imager");
  }

  private Cache ehcache;

  public Router() {
    final CacheManager manager = CacheManager.create();
    final Cache memoryOnlyCache = new Cache("ImageCache", 1000, false, false, 300, 10);
    manager.addCache(memoryOnlyCache);
    ehcache = manager.getCache("ImageCache");
  }

  public ByteArrayOutputStream cachedProcessing(Image image) throws IOException {
    Element element = ehcache.get(image);
    if (element == null) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      javax.imageio.ImageIO.write(image.drawImage(), image.getFormat()
          .getFormat(), out);
      element = new Element(image, out);
      ehcache.put(element);
    }
    return (ByteArrayOutputStream) element.getObjectValue();
  }

  public void Imager(Exchange exchange) {
    Message _in = exchange.getIn();
    final String path = _in.getHeader(Exchange.HTTP_PATH, String.class);
    try {
      final Image image = Image.fromPath(path);
      exchange.getOut().setHeader("Content-type", image.getFormat().getMime());
      exchange.getOut().setHeader("Cache-Control", "max-age=315360000");
      exchange.getOut().setHeader("Expires", "Thu, 31 Dec 2037 23:55:55 GMT");
      exchange.getOut().setBody(cachedProcessing(image).toByteArray());
    } catch (IOException e) {
      exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      exchange.getOut().setBody(sw.toString());
      e.printStackTrace();
    }
  }
}
