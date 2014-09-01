package edu.nyu.cloud.networkdiary;

import android.content.Context;

import java.io.File;

public class SelectToastApps extends AppsSelector
{
  public SelectToastApps() {
    name = "blocked notifications";  // TODO: use string resource
  }

  public File getSaveFile(Context context) {
    return new File(context.getDir("data", Context.MODE_PRIVATE), "blockedtoasts.txt");
  }

  public void negativeButton() {
    NetworkLog.selectToastApps = null;
  }

  public void positiveButton() {
    NetworkLogService.toastBlockedApps = apps;
    NetworkLog.selectToastApps = null;
  }
}
