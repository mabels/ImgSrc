package com.adviser.imgsrc;

import java.io.IOException;
import java.io.OutputStream;

public interface SimpleResponse {
  void setHeader(String k, String v);

  void setStatus(int code);

  //    Writer getWriter() throws IOException;
  OutputStream getOutputStream() throws IOException;

  void done(boolean state);
}
