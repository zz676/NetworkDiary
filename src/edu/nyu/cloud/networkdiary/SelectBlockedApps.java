package edu.nyu.cloud.networkdiary;

import android.content.Context;

import java.io.File;

public class SelectBlockedApps extends AppsSelector
{
  public SelectBlockedApps() {
    name = "blocked apps";  // TODO: use string resource
  }

  public File getSaveFile(Context context) {
    return new File(context.getDir("data", Context.MODE_PRIVATE), "blockedapps.txt");
  }

  public void negativeButton() {
    NetworkLog.selectBlockedApps = null;
  }

  public void positiveButton() {
    NetworkLogService.blockedApps = apps;
    NetworkLog.selectBlockedApps = null;
  }
}
