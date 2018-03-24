package com.adviser.imgsrc.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApiGatewayResponse {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayResponse.class);

  private final int statusCode;
  private final String body;
  private final Map<String, String> headers;
  private final boolean isBase64Encoded;

  public ApiGatewayResponse(int statusCode, String body, Map<String, String> headers, boolean isBase64Encoded) {
    this.statusCode = statusCode;
    this.body = body;
    this.headers = headers;
    this.isBase64Encoded = isBase64Encoded;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getBody() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  // API Gateway expects the property to be called "isBase64Encoded" => isIs
  public boolean isIsBase64Encoded() {
    return isBase64Encoded;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    //    private static final Logger LOG = Logger.getLogger(ApiGatewayResponse.Builder.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayResponse.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private int statusCode = 200;
    private Map<String, String> headers = Collections.emptyMap();
    // private byte[] rawBody;
    // private Object objectBody;
    private String body;
    private boolean base64Encoded;

    public Builder setStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Builds the {@link ApiGatewayResponse} using the passed raw body string.
     */
    // public Builder setRawBody(byte[] rawBody) {
    //   this.rawBody = rawBody;
    //   return this;
    // }

    /**
     * Builds the {@link ApiGatewayResponse} using the passed object body
     * converted to JSON.
     */
   // public Builder setObjectBody(Object objectBody) {
   //   this.objectBody = objectBody;
   //   return this;
   // }

    /**
     * Builds the {@link ApiGatewayResponse} using the passed binary body
     * encoded as base64. {@link #setBase64Encoded(boolean)
     * setBase64Encoded(true)} will be in invoked automatically.
     */
    public Builder setBinaryBody(byte[] binaryBody) {
      this.body = new String(Base64.getEncoder().encode(binaryBody), StandardCharsets.UTF_8);
      setBase64Encoded(true);
      return this;
    }

    public Builder setStringBody(String body) {
      this.body = body;
      setBase64Encoded(false);
      return this;
    }

    /**
     * A binary or rather a base64encoded responses requires
     * <ol>
     * <li>"Binary Media Types" to be configured in API Gateway
     * <li>a request with an "Accept" header set to one of the "Binary Media
     * Types"
     * </ol>
     */
    public Builder setBase64Encoded(boolean base64Encoded) {
      this.base64Encoded = base64Encoded;
      return this;
    }

    private String getServerTime() {
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat dateFormat = new SimpleDateFormat(
              "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(calendar.getTime());
    }

    public ApiGatewayResponse build() {
//      return this;+
//      byte[] body = null;
//      if (rawBody != null) {
//        body = rawBody;
//      }
//      else if (objectBody != null) {
//        try {
//          body = objectMapper.writeValueAsString(objectBody);
//        } catch (JsonProcessingException e) {
//          LOGGER.error("failed to serialize object", e);
//          throw new RuntimeException(e);
//        }
//      } else if (binaryBody != null) {
//        body = new String(Base64.getEncoder().encode(binaryBody), StandardCharsets.UTF_8);
//      }
      headers.put("date", this.getServerTime());
      // headers.put("etag", "4711");

      // LOGGER.info("is this:{}:{}:{}:{}", statusCode, headers, base64Encoded, body);
      return new ApiGatewayResponse(statusCode, body, headers, base64Encoded);
    }
  }
}
