package com.adviser.imgsrc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class Router extends RouteBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);
  public class ListenAddress {
    private String addr = "127.0.0.1";
    private String port = "1147";

    public ListenAddress() {
    }

    public ListenAddress(String port, String addr) {
      this.port = port;
      if (addr != null) {
        this.addr = addr;
      }
    }

    public String toString() {
      return addr + ":" + port;
    }
  }

  private ListenAddress listenaddress = null;

  public Router(String[] args) {
    if (args.length == 1) {
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
    final Cache memoryOnlyCache = new Cache("ImageCache", 1000, false, false,
        300, 10);
    manager.addCache(memoryOnlyCache);
    ehcache = manager.getCache("ImageCache");
  }

  public void configure() {
    LOGGER.info("Version:" + getServer());
    LOGGER.info("Listen On:" + listenaddress.toString());
    from("jetty:http://" + listenaddress.toString() + "?matchOnUriPrefix=true")
        .bean(this, "Imager");
  }

  public ByteArrayOutputStream cachedProcessing(Image image) throws IOException {
    Element element = ehcache.get(image);
    if (element == null) {
      ByteArrayOutputStream out = image.getStream();
      element = new Element(image, out);
      ehcache.put(element);
    }
    return (ByteArrayOutputStream) element.getObjectValue();
  }

  // private static CharsetDecoder decoder =
  // Charset.forName("ISO-8859-1").newDecoder();
  private String version = null;

  private String getServer() {
    synchronized (this) {
      if (this.version != null) {
        return this.version;
      }
      String tmpVersion = "ImgSrv(development)";
      final InputStream is = Router.class.getClassLoader().getResourceAsStream(
          "META-INF/maven/com.adviser.imgsrc/imgsrc/pom.xml");
      if (is != null) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          tmpVersion = "ImgSrv("
              + doc.getElementsByTagName("version").item(0).getTextContent()
              + ")";
        } catch (Exception e) {
          // System.out.println("IS:"+e.getMessage());
        }
      }
      this.version = tmpVersion;
    }
    return this.version;
  }

  private String testHtml = null;

  private void testHtml(Exchange exchange) throws IOException {
    synchronized (this) {
      if (testHtml == null) {
        final InputStream is = Router.class.getClassLoader()
            .getResourceAsStream("test.html");
        testHtml = IOUtils.toString(is);
        LOGGER.debug("FETCH test.html");
      }
    }
    exchange.getOut().setHeader("Content-Type", "text/html");
    exchange.getOut().setBody(testHtml);
  }

  private void setNoCacheHeaders(Message out) {
    out.setHeader("cache-control", constant("no-cache"));
    out.setHeader("pragma", constant("no-cache"));
  }
  private void setCacheHeaders(Message out) {
    out.setHeader("cache-control", constant("max-age=315360000"));
    out.setHeader("expires", "Thu, 31 Dec 2037 23:55:55 GMT");
  }
  public void Imager(Exchange exchange) {
    final Message in = exchange.getIn();
    try {
      final String path = in.getHeader(Exchange.HTTP_PATH, String.class);
      final Message out = exchange.getOut();
      // System.out.println("Path:" + path);
      out.setHeader("Server", getServer());
      if (path.startsWith("/test.html")) {
        testHtml(exchange);
        return;
      }
      Image image = Image.fromPath(path);
      image.setWait(in.getHeaders().get("wait"));
      if (path.startsWith("/favicon.ico")) {
        image = Image.fromPath("/16/16/a00/000/favicon.ico");
      }
      if (image.isRedirect()) {
        final String location = image.getFullPath();
        out.setHeader("location", location);
        setNoCacheHeaders(out);
        out.setHeader(Exchange.HTTP_RESPONSE_CODE, 302);
        out.setBody("redirect to:" + location + "\n");
      } else {
        out.setHeader("content-type", image.getFormat().getMime());
        if (image.shouldWait()) {  
          LOGGER.debug("Wait:"+image.getWait());
          Thread.sleep(image.getWait());
          setNoCacheHeaders(out); 
        } else {  
          setCacheHeaders(out);
        }
        byte[] img = cachedProcessing(image).toByteArray();
        out.setHeader("content-length", img.length);
        out.setBody(img);
      }
    } catch (Exception e) {
      exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      exchange.getOut().setBody(sw.toString());
      e.printStackTrace();
    }
  }
}
