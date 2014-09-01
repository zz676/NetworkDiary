package edu.nyu.cloud.networkdiary;

import java.lang.Runnable;

public abstract class NetworkResolverUpdater implements Runnable {
  protected String resolved;

  public void setResolved(String resolved) {
    this.resolved = resolved;
  }
}
