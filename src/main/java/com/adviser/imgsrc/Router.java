package com.adviser.imgsrc;

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
  public class ListenAddress {
    private String addr = "127.0.0.1";
    private String port = "1147";
    public ListenAddress() {      
    }
    public ListenAddress(String port, String addr) {
      this.port = port;
      if (addr != null) this.addr = addr;
    }
    public String toString() {
      return addr + ":" + port;
    }
  }
  
  private ListenAddress listenaddress = null;
  public Router(String[] args) {
    if (args.length >= 1) {
      listenaddress = new ListenAddress(args[0], null);      
    } else if (args.length >= 2) {
      listenaddress = new ListenAddress(args[0], args[1]);
    } else {
      listenaddress = new ListenAddress();
    }
    init();
  }
  private Cache ehcache;
  private void init() {
    final CacheManager manager = CacheManager.create();
    final Cache memoryOnlyCache = new Cache("ImageCache", 1000, false, false, 300, 10);
    manager.addCache(memoryOnlyCache);
    ehcache = manager.getCache("ImageCache");
  }

  public void configure() {
    System.out.println("Listen On:"+listenaddress.toString());
    from("jetty:http://" + listenaddress.toString() + "?matchOnUriPrefix=true")
        .bean(this, "Imager");
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
