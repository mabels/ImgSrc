package com.adviser.imgsrc;

import org.apache.commons.io.IOUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;


public final class ImgSrc {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImgSrc.class);

  private Cache<String, ByteArrayOutputStream> ehcache;

  public void init() throws Exception {
    final CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("ImageCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
                ByteArrayOutputStream.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(4096, EntryUnit.ENTRIES)
//                    .with(new ResourcePool() {
//                      @Override
//                      public ResourceType<?> getType() {
//                        return ResourceType.Core.HEAP;
//                      }
//
//                      @Override
//                      public boolean isPersistent() {
//                        return false;
//                      }
//
//                      @Override
//                      public void validateUpdate(ResourcePool resourcePool) {
//
//                      }
//                    })
            ))
        .build();
    manager.init();
    ehcache = manager.getCache("ImageCache", String.class, ByteArrayOutputStream.class);
  }


  public ByteArrayOutputStream cachedProcessing(Image image) throws IOException {
    final String fp = image.getFullPath();
    ByteArrayOutputStream os = ehcache.get(fp);
    if (os == null) {
      LOGGER.info("create cache Entry:{}", fp);
      final Render render = image.getFormat().createRender();
      image.drawImage(render);
      os = render.getStream();
//      element = new Element(image, out);
      ehcache.put(fp, os);
    }
    return os;
  }


  private static String[] version = {null};

  public static String getServerVersion() {
    synchronized (version) {
      if (version[0] != null) {
        return version[0];
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
      version[0] = tmpVersion;
    }
    return version[0];
  }


  public void handleRequest(SimpleRequest request, SimpleResponse response) {
    try {
      response.setHeader("X-ImgSrc-Version", ImgSrc.getServerVersion());
      if (request.getPath().startsWith("/test.html")) {
        testHtml(response);
        response.done(true);
        return;
      }
      Image image = Image.fromPath(request.getPath());
//      LOGGER.error(image.getFullPath());
      image.setWait(request.getHeader("wait"));
      if (request.getPath().startsWith("/favicon.ico")) {
        image = Image.fromPath("/16/16/a00/000/favicon.ico");
      }
      if (image.isRedirect()) {
        final String location = image.getFullPath();
        response.setHeader("location", location);
        setNoCacheHeaders(response);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.getOutputStream().write(("redirect to:" + location + "\n").getBytes());
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
      response.done(true);
    } catch (Exception e) {
      LOGGER.error("error:{}", e);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      try {
        response.getOutputStream().write(sw.toString().getBytes());
      } catch (Exception j) {
        LOGGER.error("double error:{}", j);
      }
      response.done(true);
//      e.printStackTrace();
    }
  }

  private static byte[][] testHtmlCache = {null};

  public static void testHtml(SimpleResponse resp) {
    synchronized (testHtmlCache) {
      if (testHtmlCache[0] == null) {
        final InputStream is = ImgSrc.class.getClassLoader()
            .getResourceAsStream("test.html");
        try {
          testHtmlCache[0] = IOUtils.toString(is, "utf-8").getBytes();
        } catch (Exception e) {
          testHtmlCache[0] = "VersionError".getBytes();
        }
//        LOGGER.debug("FETCH test.html");
      }
    }
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader("Content-Type", "text/html;charset=utf-8");
    try {
      resp.getOutputStream().write(testHtmlCache[0]);
    } catch (Exception e) {
      LOGGER.error("OutputStreamWriteError:{}", e);
    }
  }

  private void setNoCacheHeaders(SimpleResponse out) {
    out.setHeader("cache-control", "no-cache");
    out.setHeader("pragma", "no-cache");
  }

  private void setCacheHeaders(SimpleResponse out) {
//    out.setHeader("cache-control", "max-age=315360000");
//    out.setHeader("expires", "Thu, 31 Dec 2037 23:55:55 GMT");
  }


}
