package com.adviser.imgsrc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;


public class Steps<T> {
  private LinkedList<Step<T>> _steps = new LinkedList<Step<T>>();
  private Map<String, Step<T>> _byName = new HashMap<String, Step<T>>();

  public void add(Step<T> step) {
    _steps.add(step);
    step.setSteps(this);
    _byName.put(step.getName(), step);
  }

  public Step<T> getStepByName(String name) {
    return _byName.get(name);
  }

  private static Pattern _split = Pattern.compile("/+");
  
  public static LinkedList<String> splitPath(String path) {
    String[] paths = _split.split(path, 0);
    LinkedList<String> datas = new LinkedList<String>();
    for (int i = 0; i < paths.length; ++i) {
      String data = paths[i];
      if (data.trim().isEmpty()) {
        continue;
      }
      datas.add(data);    
    }
    return datas;
  }

  public void parse(T ref, LinkedList<String> datas) {
    Step<T> step = _steps.getFirst();
    Iterator<String> i = datas.iterator();
    while(step != null && i.hasNext()) {
      String path = i.next();
      if (path == null) {
        break;
      }
      if (path.isEmpty()) {
        continue;
      }
      step = step.parse(ref, path);
    }
  }
}
