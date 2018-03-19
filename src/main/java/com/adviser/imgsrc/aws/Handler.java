package com.adviser.imgsrc.aws;

import com.adviser.imgsrc.ImgSrc;
import com.adviser.imgsrc.SimpleRequest;
import com.adviser.imgsrc.SimpleResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);

  private final ImgSrc imgSrc;

  public Handler() {
    this.imgSrc = new ImgSrc();
    try {
      imgSrc.init();
      LOGGER.info("Version:" + imgSrc.getServerVersion());
    } catch (Exception e) {
      LOGGER.error("ErrorVersion:" + imgSrc.getServerVersion());
    }
  }

  @Override
  public ApiGatewayResponse handleRequest(final Map<String, Object> input, Context context) {
    BasicConfigurator.configure();
    final ApiGatewayResponse.Builder response = ApiGatewayResponse.builder();
    final Map<String, String> headers = new HashMap<>();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    this.imgSrc.handleRequest(new SimpleRequest() {
      @Override
      public String getHeader(String key) {
        return (String) input.get(key);
      }

      @Override
      public String getPath() {
        return (String) input.get("path");
      }
    }, new SimpleResponse() {

      @Override
      public void setHeader(String k, String v) {
        headers.put(k, v);
      }

      @Override
      public void setStatus(int code) {
        response.setStatusCode(code);
      }

      @Override
      public OutputStream getOutputStream() throws IOException {
        return baos;
      }

      @Override
      public void done(boolean state) {
      }
    });
    return response.setStatusCode(200)
        .setRawBody(baos.toByteArray())
        .setBase64Encoded(false)
        .setHeaders(headers)
        .build();
  }

}
