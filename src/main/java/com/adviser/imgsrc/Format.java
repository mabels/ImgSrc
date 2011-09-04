package com.adviser.imgsrc;

import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import lombok.Data;
import lombok.val;

@Data
class Format {
  private String format;
  private String mime;
  private int colorSpace;
  private String cleanPath;
  private String suffix;
  
  /*
  public void setCleanPath(String path) {
    cleanPath = path.replaceAll(formatPattern() , "");
  }

  private String _formatPattern = null;
  public String formatPattern() {
    if (_formatPattern != null) return _formatPattern;
    
    String pattern = "(";
    Iterator<Format> formats = factory().values().iterator();
    String or = "";
    while(formats.hasNext()) {
      String suffix = formats.next().getSuffix();
      pattern = pattern + Pattern.quote(suffix) + or;
      or = "|";
    }
    pattern += ")";
    _formatPattern = pattern;
    return _formatPattern;
  }
  */
  private static HashMap<String, Format> _factory = null;
  
  private static HashMap<String, Format> factory() {
    if (_factory == null) {
      _factory = new HashMap<String, Format>();
      Format tmp;
      tmp = new Format_png();
      _factory.put(tmp.getSuffix(), tmp);
      tmp = new Format_jpg();
      _factory.put(tmp.getSuffix(), tmp);
      tmp = new Format_gif();
      _factory.put(tmp.getSuffix(), tmp);
    }
    return _factory;
  }
  private static Pattern _suffix = Pattern.compile("(\\.\\p{Alnum}{3})");
  public static Format fromPath(String path) {
    
    Format ret = null;
    Matcher suffix = _suffix.matcher(path);
    if (suffix.matches()) {
      ret = factory().get(suffix.group(1));
      if (ret != null) {
        path = suffix.replaceFirst("");
      }
    }
    if (ret == null) {
      ret = factory().get(".gif");
    }
    ret.setCleanPath(path);
    return ret;
  }
}