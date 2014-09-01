package edu.nyu.cloud.networkdiary;

import android.util.Log;

public class MyLog {
  public static boolean enabled = true;
  public static int level = 0;
  public static String tag = "NetworkLog";

  public static void d(String msg) {
    d(0, tag, msg);
  }

  public static void d(String tag, String msg) {
    d(0, tag, msg);
  }

  public static void d(int level, String msg) {
    d(level, tag, msg);
  }

  public static void d(int level, String tag, String msg) {
    if(!enabled || level > MyLog.level) {
      return;
    }

    for(String line : msg.split("\n")) {
      Log.d(tag, line);
    }
  }
}
