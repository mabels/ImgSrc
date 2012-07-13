package com.adviser.imgsrc;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;


public class Steps<T> {
  private Deque<Step<T>> steps = new LinkedList<Step<T>>();
  private Map<String, Step<T>> byName = new HashMap<String, Step<T>>();

  public void add(Step<T> step) {
    steps.add(step);
    step.setSteps(this);
    byName.put(step.getName(), step);
  }

  public Step<T> getStepByName(String name) {
    return byName.get(name);
  }

  private static final Pattern RESPLIT = Pattern.compile("/+");
  
  public static Deque<String> splitPath(String path) {
    String[] paths = RESPLIT.split(path, 0);
    Deque<String> datas = new LinkedList<String>();
    for (int i = 0; i < paths.length; ++i) {
      String data = paths[i];
      if (data.trim().isEmpty()) {
        continue;
      }
      datas.add(data);    
    }
    return datas;
  }

  public void parse(T ref, Deque<String> datas) {
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
