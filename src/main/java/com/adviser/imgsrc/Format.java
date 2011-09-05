package com.adviser.imgsrc;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
abstract class Format {
	 abstract public String getFormat();
	 abstract public String getMime();
	 abstract public int getColorSpace();
	 abstract public void setColorSpace(int s);
	 abstract public String getSuffix();
	 
	 private String cleanPath;
  /*
  private String format;
  private String mime;
  private int colorSpace;
  private String suffix;
  */
  //private String cleanPath;
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
  private static Pattern _suffix = Pattern.compile("(.*)(\\.\\w{3})(.*)");
  public static Format fromPath(String path) {
    
    Format ret = null;
    Matcher suffix = _suffix.matcher(path);
    if (suffix.matches()) {
      ret = factory().get(suffix.group(2));
      if (ret != null) {
        path = suffix.group(1)+suffix.group(3);
      }
    }
    if (ret == null) {
      ret = factory().get(".gif");
    }
    ret.setCleanPath(path);
    return ret;
  }
}