package com.adviser.imgsrc.azure;


import com.adviser.imgsrc.ImgSrc;
import com.adviser.imgsrc.SimpleRequest;
import com.adviser.imgsrc.SimpleResponse;
import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.HttpRequestMessage;
import com.microsoft.azure.serverless.functions.HttpResponseMessage;
import com.microsoft.azure.serverless.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.serverless.functions.annotation.FunctionName;
import com.microsoft.azure.serverless.functions.annotation.HttpOutput;
import com.microsoft.azure.serverless.functions.annotation.HttpTrigger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Handler {

  private static ImgSrc imgSrc;

//  public Handler() {
//    this.imgSrc = null;
//  }


  @FunctionName("imgsrc")
  public
  HttpResponseMessage<String> imgsrc(
      @HttpTrigger(
//          dataType = "binary",
          name = "req",
          methods = {"get"},
          route = "{*path}",
          authLevel = AuthorizationLevel.ANONYMOUS)
          HttpRequestMessage<Optional<String>> request,
      final ExecutionContext context) {

    if (this.imgSrc == null) {
      synchronized (this) {
        this.imgSrc = new ImgSrc();
        try {
          imgSrc.init();
          context.getLogger().info("Version:" + imgSrc.getServerVersion());
        } catch (Exception e) {
          context.getLogger().severe("ErrorVersion:" + imgSrc.getServerVersion());
        }
      }
    }

    final Map<String, String> headers = new HashMap<>();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final int[] statusCode = { 200 };
    Handler.imgSrc.handleRequest(new SimpleRequest() {
      @Override
      public String getHeader(String key) {
        return request.getHeaders().get(key);
      }

      @Override
      public String getPath() {
        context.getLogger().info(request.getQueryParameters().get("path"));
        return request.getUri().getPath();
      }
    }, new SimpleResponse() {

      @Override
      public void setHeader(String k, String v) {
        headers.put(k, v);
      }

      @Override
      public void setStatus(int code) {
        statusCode[0] = code;
      }

      @Override
      public OutputStream getOutputStream() throws IOException {
        return baos;
      }

      @Override
      public void done(boolean state) {
      }
    });
    context.getLogger().info("Path:" + request.getUri().getPath() + ":" + statusCode[0] + ":" + baos.size());
    //@HttpOutput(name = "body", dataType = "binary")
    final HttpResponseMessage<String> ret = request.createResponse(statusCode[0], "meno");
    headers.entrySet().forEach((e) -> {
//      context.getLogger().info("addHeader:"+e.getKey()+":"+e.getValue());
      ret.addHeader(e.getKey(), e.getValue());
    });
    return request.createResponse(400, "Please pass a name on the query string or in the request body");
//    return ret;

//    return response.setStatusCode(200)
//        .setRawBody(baos.toByteArray())
//        .setBase64Encoded(false)
//        .setHeaders(headers)
//        .build();
////    context.getLogger().info("Java HTTP trigger processed a request.");
//
//    // Parse query parameter
////    String query = request.getQueryParameters().get("name");
////    String name = request.getBody().orElse(query);
//
//    if (name == null) {
//      return request.createResponse(400, "Please pass a name on the query string or in the request body");
//    } else {
//    }
  }
}
