package com.adviser.imgsrc;

public abstract class Step<T> {
  public Step(String inName) {
    this.name = inName;
  }

  private Steps<T> steps = null;
  private String name = null;

  public abstract Step<T> parse(T ref, String path);

  public Steps<T> getSteps() {
    return steps;
  }

  public void setSteps(Steps<T> steps) {
    this.steps = steps;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


}
