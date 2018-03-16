package com.adviser.imgsrc;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;


public final class ImgSrc extends AbstractHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImgSrc.class);

  public class ListenAddress {
    private String addr = "127.0.0.1";
    private int port = 1147;

    public ListenAddress() {
    }

    public ListenAddress(String port, String addr) {
      this.port = Integer.valueOf(port);
      if (addr != null) {
        this.addr = addr;
      }
    }

    public String toString() {
      return addr + ":" + port;
    }
  }

  private ListenAddress listenaddress = null;
  private Cache<String, ByteArrayOutputStream> ehcache;
  private String version = null;
  private String testHtml = null;

  private void init() throws Exception {
    // final CacheManager manager = CacheManager.create();

    final CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("ImageCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, ByteArrayOutputStream.class, ResourcePoolsBuilder.heap(10)))
        .build();
    manager.init();

//    Cache<String, Image> mem = manager.getCache("preConfigured", Long.class, String.class);

//    Cache<Long, String> myCache = cacheManager.createCache("myCache",
//        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)));


//    final Cache memoryOnlyCache = new Cache("ImageCache", 1000, false, false,
//        300, 10);
//    manager.addCache(memoryOnlyCache);
    ehcache = manager.getCache("ImageCache", String.class, ByteArrayOutputStream.class);

    Server server = new Server(this.listenaddress.port);
    server.setHandler(this);
    server.start();
    server.join();
  }

  public ImgSrc(String[] args) {
    if (args.length == 1) {
      listenaddress = new ListenAddress(args[0], null);
    } else if (args.length >= 2) {
      listenaddress = new ListenAddress(args[0], args[1]);
    } else {
      listenaddress = new ListenAddress();
    }
  }

  public void configure() {
    LOGGER.info("Version:" + getServer());
    LOGGER.info("Listen On:" + listenaddress.toString());
//    from("jetty:http://" + listenaddress.toString() + "?matchOnUriPrefix=true")
//        .bean(this, "imager");
  }

  public ByteArrayOutputStream cachedProcessing(Image image) throws IOException {
    final String fp = image.getFullPath();
    ByteArrayOutputStream os = ehcache.get(fp);
    if (os == null) {
      image.drawImage();
      os = image.getStream();
//      element = new Element(image, out);
      ehcache.put(fp, os);
    }
    return os;
  }


  private String getServerVersion() {
    synchronized (this) {
      if (this.version != null) {
        return this.version;
      }
      String tmpVersion = "ImgSrv(development)";
      final InputStream is = ImgSrc.class.getClassLoader().getResourceAsStream(
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


  public void handle(String path, Request baseRequest, HttpServletRequest request,
                     HttpServletResponse response) throws IOException, ServletException {
//    response.setContentType("text/html;charset=utf-8");
//    response.setStatus(HttpServletResponse.SC_OK);
//    baseRequest.setHandled(true);
//    response.getWriter().println("<h1>Hello World</h1>"+target+":"+baseRequest.getPathTranslated());
    try {
      response.setHeader("X-ImgSrc-Version", getServerVersion());
      if (path.startsWith("/test.html")) {
        testHtml(response);
        baseRequest.setHandled(true);
        return;
      }
      Image image = Image.fromPath(path);
//      LOGGER.error(image.getFullPath());
      image.setWait(request.getHeaders("wait"));
      if (path.startsWith("/favicon.ico")) {
        image = Image.fromPath("/16/16/a00/000/favicon.ico");
      }
      if (image.isRedirect()) {
        final String location = image.getFullPath();
        response.setHeader("location", location);
        setNoCacheHeaders(response);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.getWriter().write("redirect to:" + location + "\n");
      } else {
        response.setHeader("content-type", image.getFormat().getMime());
        if (image.shouldWait()) {
          LOGGER.debug("Wait:" + image.getWait());
          try {
            Thread.sleep(image.getWait());
          } catch (Exception e) {
            LOGGER.error("Thread.sleep:", e);
          }
          setNoCacheHeaders(response);
        } else {
          setCacheHeaders(response);
        }
        ByteArrayOutputStream img = cachedProcessing(image);
        response.setHeader("content-length", String.valueOf(img.size()));
        img.writeTo(response.getOutputStream());
      }
      baseRequest.setHandled(true);
    } catch (Exception e) {
      LOGGER.error("handler:", e);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      response.getWriter().write(sw.toString());
      baseRequest.setHandled(true);
//      e.printStackTrace();
    }

  }


  private void testHtml(HttpServletResponse resp) throws IOException {
    synchronized (this) {
      if (testHtml == null) {
        final InputStream is = ImgSrc.class.getClassLoader()
            .getResourceAsStream("test.html");
        testHtml = IOUtils.toString(is, "utf-8");
        LOGGER.debug("FETCH test.html");
      }
    }
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader("Content-Type", "text/html;charset=utf-8");
    resp.getWriter().write(testHtml);
  }

  private void setNoCacheHeaders(HttpServletResponse out) {
    out.setHeader("cache-control", "no-cache");
    out.setHeader("pragma", "no-cache");
  }

  private void setCacheHeaders(HttpServletResponse out) {
//    out.setHeader("cache-control", "max-age=315360000");
//    out.setHeader("expires", "Thu, 31 Dec 2037 23:55:55 GMT");
  }


  public static void main(String[] args) throws Exception {
    final ImgSrc imgSrc = new ImgSrc(args);
    imgSrc.init();
  }

}
