package com.adviser.imgsrc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;


public class Steps<T> {
  private LinkedList<Step<T>> steps = new LinkedList<Step<T>>();
  private Map<String, Step<T>> byName = new HashMap<String, Step<T>>();

  public void add(Step<T> step) {
    steps.add(step);
    step.setSteps(this);
    byName.put(step.getName(), step);
  }

  public Step<T> getStepByName(String name) {
    return byName.get(name);
  }

  private static final Pattern reSplit = Pattern.compile("/+");
  
  public static LinkedList<String> splitPath(String path) {
    String[] paths = reSplit.split(path, 0);
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
    Step<T> step = steps.getFirst();
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
