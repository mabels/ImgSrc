package com.adviser.imgsrc;

import lombok.Data;

@Data
public abstract class Step<T> {
  public Step(String _name) {
    name = _name;
  }
  private Steps<T> steps = null;
  private String name = null;
  public abstract Step<T> parse(T ref, String path);
}
