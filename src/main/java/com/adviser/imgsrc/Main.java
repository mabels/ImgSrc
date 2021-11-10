package com.adviser.imgsrc;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.bouncycastle.util.io.pem.PemReader;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.util.io.pem.PemFile;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImgSrc.class);


  public static class ServletHandler extends AbstractHandler {
    private final ImgSrc imgSrc;

    public ServletHandler() {
      this.imgSrc = new ImgSrc();
      try {
        imgSrc.init();
      } catch (Exception e) {
        LOGGER.error("Init failed");
      }
      LOGGER.info("Version:" + ImgSrc.getServerVersion());
    }

    @Override
    public void handle(final String path,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) 
                       throws IOException, ServletException {
      this.imgSrc.handleRequest(new SimpleRequest() {
        @Override
        public String getHeader(String key) {
          return request.getHeader(key);
        }

        @Override
        public String getPath() {
          return path;
        }
      }, new SimpleResponse() {

        @Override
        public void setHeader(String k, String v) {
          response.setHeader(k, v);
        }

        @Override
        public void setStatus(int code) {
          response.setStatus(code);
        }

//        @Override
//        public Writer getWriter() throws IOException {
//          return response.getWriter();
//        }

        @Override
        public OutputStream getOutputStream() throws IOException {
          return response.getOutputStream();
        }

        @Override
        public void done(boolean state) {
          baseRequest.setHandled(state);
        }
      });
    }

    
  }

  public static class ImgSrcConfig {
    @Parameter(names = {"-listen", "-L"}, description = "listen address")
    public String addr = "127.0.0.1";
    @Parameter(names = {"-port", "-P"}, description = "listen port")
    public int port = 1147;
    @Parameter(names = {"-ssl", "-S"}, description = "ssl")
    public String schema = "http";
    @Parameter(names = {"-min"}, description = "min Threads")
    public int minThreads = 10;
    @Parameter(names = {"-maxThread"}, description = "max Threads")
    public int maxThreads = 100;
    @Parameter(names = {"-fullChain"}, description = "path to fullChain File")
    public String fullChainFile = "./fullchain.pem";
    @Parameter(names = {"-privKey"}, description = "path to privKey File")
    public String privKeyFile = "./privkey.pem";
    @Parameter(names = {"-cert"}, description = "path to cert File")
    public String certFile = null;

    public static ImgSrcConfig from(String[] args) {
      ImgSrcConfig cfg = new ImgSrcConfig();
      JCommander.newBuilder()
          .addObject(cfg)
          .build()
          .parse(args);
      return cfg;
    }

    public ImgSrcConfig() {
    }

//    public ImgSrcConfig(String port, String addr) {
//      this.port = Integer.valueOf(port);
//      if (addr != null) {
//        this.addr = addr;
//      }
//    }

    public String toString() {
      return addr + ":" + port;
    }

    public boolean isHttp() {
      return this.schema.equals("http");
    }
  }

  private static KeyStore createKeyStorePem(ImgSrcConfig imgSrcConfig) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException, KeyStoreException {
    KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // might not be RSA
    LOGGER.info("PrivKeyFile:{}", imgSrcConfig.privKeyFile);
    PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(imgSrcConfig.privKeyFile)));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pemReader.readPemObject().getContent());
    PrivateKey privateKey = keyFactory.generatePrivate(spec);

    final CertificateFactory factory = CertificateFactory.getInstance("X.509");
    final List<X509Certificate> certs = new ArrayList<>();
    if (imgSrcConfig.certFile != null) {
      LOGGER.info("CertFile:{}", imgSrcConfig.certFile);
      FileInputStream certInputStream = new FileInputStream(imgSrcConfig.certFile);
      X509Certificate cert = (X509Certificate) factory.generateCertificate(certInputStream);
      certs.add(cert);
      LOGGER.info("Cert:{}:{}", cert.getSubjectDN(), cert.getIssuerDN());
    }

    LOGGER.info("FullChainFile:{}", imgSrcConfig.fullChainFile);
    FileInputStream fullChainStream = new FileInputStream(imgSrcConfig.fullChainFile);
    while (true) {
      try {
        X509Certificate chain = (X509Certificate) factory.generateCertificate(fullChainStream);
        LOGGER.info("Cert:{}:{}", chain.getSubjectDN(), chain.getIssuerDN());
        certs.add(chain);
      } catch (Exception e) {
        break;
      }
    }
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(null, null);
    keystore.setKeyEntry("imgsrc", privateKey, "imgSrcGeheim".toCharArray(),
        certs.toArray(new X509Certificate[certs.size()]));
    return keystore;
  }

  private static Server http(ImgSrcConfig imgSrcConfig, Server server) throws Exception {
    ServerConnector connector = new ServerConnector(server);
    connector.setHost(imgSrcConfig.addr);
    connector.setPort(imgSrcConfig.port);
    server.addConnector(connector);
    return server;
  }

  private static Server httpx(ImgSrcConfig imgSrcConfig, Server server)
      throws InvalidKeySpecException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    HttpConfiguration http = new HttpConfiguration();
    http.addCustomizer(new SecureRequestCustomizer());
    http.setSecurePort(imgSrcConfig.port);
    http.setSecureScheme("https");

    HttpConfiguration https = new HttpConfiguration();
    https.addCustomizer(new SecureRequestCustomizer());
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    sslContextFactory.setKeyStore(createKeyStorePem(imgSrcConfig));
    sslContextFactory.setKeyStorePassword("imgSrcGeheim");

    ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
    sslConnector.setPort(imgSrcConfig.port);
    sslConnector.setHost(imgSrcConfig.addr);
    server.setConnectors(new Connector[]{sslConnector});
    return server;
  }

  private static Server https(ImgSrcConfig imgSrcConfig, Server server)
      throws NoSuchAlgorithmException, InvalidKeySpecException,
      IOException, CertificateException, KeyStoreException {
//    Security.addProvider(new BouncyCastleProvider());

    // HTTP Configuration
    HttpConfiguration http_config = new HttpConfiguration();
    http_config.setSecureScheme("https");
    http_config.setSecurePort(imgSrcConfig.port);

    // SSL Context Factory for HTTPS and HTTP/2
//    sslContextFactory.set
//    sslContextFactory.setKeyStoreResource(newClassPathResource("keystore"));
//    sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
//    sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
//    sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

//    FileOutputStream fos = new FileOutputStream("keyStore.ks");
//    keystore.store(fos, "imgSrcGeheim".toCharArray());
//    keystore.load(null, null);

    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    sslContextFactory.setKeyStore(createKeyStorePem(imgSrcConfig));
    sslContextFactory.setKeyStorePassword("imgSrcGeheim");

//// add it to the keystore
//    store.setKeyEntry(alias, privateKey, password, new X509Certificate[] { cert });

    // HTTPS Configuration
    HttpConfiguration https_config = new HttpConfiguration(http_config);
    https_config.addCustomizer(new SecureRequestCustomizer());

    // HTTP/2 Connection Factory
    HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(https_config);
    // NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
    ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
    alpn.setDefaultProtocol("h2");

    // SSL Connection Factory
    SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

    // HTTP/2 Connector
    ServerConnector http2Connector =
        new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(https_config));
    http2Connector.setPort(imgSrcConfig.port);
    http2Connector.setHost(imgSrcConfig.addr);
    server.addConnector(http2Connector);

//    ALPN.debug = false;
    return server;
  }

  public static void main(String[] args) throws Exception {
//    PropertyConfigurator.configure(Slf4j_log4j_main.class.getClassLoader().getResource("basic/log4j.properties"));

    ImgSrcConfig imgSrcConfig = ImgSrcConfig.from(args);

    LOGGER.info("Listen On:" + imgSrcConfig.toString());

    QueuedThreadPool threadPool = new QueuedThreadPool(imgSrcConfig.maxThreads, imgSrcConfig.minThreads);
    Server server = new Server(threadPool);
    server.setHandler(new ServletHandler());
    if (imgSrcConfig.isHttp()) {
      http(imgSrcConfig, server);
    } else {
      httpx(imgSrcConfig, server);
    }
    server.start();
    server.join();
  }
}
