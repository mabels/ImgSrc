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

import jakarta.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;


public final class ImgSrc {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImgSrc.class);

  public static class CachedImage {
    public final String etag;
    public final byte[] bytes;
    public CachedImage(byte[] bytes) {
      String etag = null;
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(bytes);
        BigInteger bigInt = new BigInteger(1, thedigest);
        etag = bigInt.toString(62);
      } catch (Exception e) {
      }
      this.etag = etag;
      this.bytes = bytes;
    }
  }

  private Cache<String, CachedImage> ehcache;

  public void init() throws Exception {
    final CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("ImageCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
                CachedImage.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(4096, EntryUnit.ENTRIES)
            )).build();
    manager.init();
    ehcache = manager.getCache("ImageCache", String.class, CachedImage.class);
  }


  public CachedImage cachedProcessing(Image image) throws IOException {
    final String fp = image.getFullPath();
    CachedImage os = ehcache.get(fp);
    if (os == null) {
      synchronized (ehcache) {
        LOGGER.info("create cache Entry:{}", fp);
        final Render render = image.getFormat().createRender();
        image.drawImage(render);
        os = new CachedImage(render.getStream().toByteArray());
//      element = new Element(image, out);
        ehcache.put(fp, os);
      }
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
        CachedImage cimg = cachedProcessing(image);
        response.setHeader("content-length", String.valueOf(cimg.bytes.length));
        if (cimg.etag != null) {
          response.setHeader("etag", cimg.etag);
        }
        response.getOutputStream().write(cimg.bytes);
      }
      response.done(true);
    } catch (Exception e) {
      // LOGGER.error("error:{}", e.m);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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


  public void testHtml(SimpleResponse resp) {
    CachedImage os = ehcache.get("/test.html");
    if (os == null) {
      final InputStream is = ImgSrc.class.getClassLoader().getResourceAsStream("test.html");
      synchronized (ehcache) {
          os = ehcache.get("/test.html");
          if (os == null) {
            LOGGER.info("create cache Entry:/test.html");
            byte[] text;
            try {
              text = IOUtils.toString(is, "utf-8").getBytes();
            } catch (Exception e) {
              text = "VersionError".getBytes();
            }
            os = new CachedImage(text);
            ehcache.put("/test.html", os);
          }
      }
    }
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader("content-type", "text/html;charset=utf-8");
    resp.setHeader("etag", os.etag);
    try {
      resp.getOutputStream().write(os.bytes);
    } catch (Exception e) {
      LOGGER.error("OutputStreamWriteError:{}", e);
    }
  }

  private void setNoCacheHeaders(SimpleResponse out) {
    out.setHeader("cache-control", "public, max-age=0");
    out.setHeader("last-modified", "Tue, 01 Jan 1980 00:00:00 GMT");
  }

  private void setCacheHeaders(SimpleResponse out) {
    out.setHeader("cache-control", "public, max-age=0");
    out.setHeader("last-modified", "Tue, 01 Jan 1980 00:00:00 GMT");
//    out.setHeader("cache-control", "max-age=315360000");
//    out.setHeader("expires", "Thu, 31 Dec 2037 23:55:55 GMT");
  }


}
