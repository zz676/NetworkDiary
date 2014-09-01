package edu.nyu.cloud.networkdiary;

import android.util.Log;
import edu.nyu.cloud.networkdiary.R;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ThroughputTracker {
  public static String throughputString = "";

  static class ThroughputData {
    ApplicationsTracker.AppEntry app;
    String address;
    long upload;
    long download;
    long clearTime;
    boolean displayed;
  }

  public static HashMap<String, ThroughputData> throughputMap = new HashMap<String, ThroughputData>();
  public static HashMap<String, ThroughputData> resetMap = new HashMap<String, ThroughputData>();
  
  private static ApplicationsTracker.AppEntry appEntry;

  public static void updateEntry(LogEntry entry) {
    appEntry = ApplicationsTracker.uidMap.get(entry.uidString);

    if(appEntry == null) {
      Log.w("NetworkLog", "[ThroughputTracker] No app entry for uid " + entry.uidString);
      return;
    }

    synchronized(throughputMap) {
      ThroughputData throughput = throughputMap.get(appEntry.packageName);

      if(throughput == null) {
        throughput = new ThroughputData();
        throughput.app = appEntry;
        throughputMap.put(appEntry.packageName, throughput);
      }

      if(throughput.displayed == true) {
        throughput.download = 0;
        throughput.upload = 0;
      }

      if(entry.in != null && entry.in.length() > 0) {
        if(NetworkLogService.throughputBps) {
          throughput.download += entry.len * Byte.SIZE;
        } else {
          throughput.download += entry.len;
        }
        throughput.address = entry.src + ":" + entry.spt;
      } else {
        if(NetworkLogService.throughputBps) {
          throughput.upload += entry.len * Byte.SIZE;
        } else {
          throughput.upload += entry.len;
        }
        throughput.address = entry.dst + ":" + entry.dpt;
      }

      throughput.clearTime = System.currentTimeMillis() + NetworkLogService.toastDuration;
      throughput.displayed = false;
    }
  }

  static class ThroughputUpdater implements Runnable {
    boolean running = false;
    long totalUpload;
    long totalDownload;
    StringBuilder toastString = new StringBuilder(512);

    public void stop() {
      running = false;
    }

    public void run() {
      String throughput;
      boolean isDirty = false;
      running = true;
      String newline;

      while(running) {
        synchronized(throughputMap) {
          if(isDirty) {
            isDirty = false;
            if(!resetMap.isEmpty()) {
              for(ThroughputData entry : resetMap.values()) {
                if(NetworkLog.appFragment != null) {
                  NetworkLog.appFragment.updateAppThroughput(entry.app.uid, 0, 0);
                }
              }
              resetMap.clear();
            }
            updateThroughput(0, 0);
            totalUpload = 0;
            totalDownload = 0;
          }

          if(!throughputMap.isEmpty()) {
            isDirty = true;
            toastString.setLength(0);
            newline = "";
            long currentTime = System.currentTimeMillis();
            boolean showToast = false;

            Iterator entries = throughputMap.entrySet().iterator();
            while(entries.hasNext()) {
              Map.Entry entry = (Map.Entry) entries.next();
              ThroughputData value = (ThroughputData) entry.getValue();

              if(value.displayed == false) {
                totalUpload += value.upload;
                totalDownload += value.download;

                if(NetworkLog.appFragment != null) {
                  NetworkLog.appFragment.updateAppThroughput(value.app.uid, value.upload, value.download);
                  resetMap.put(value.app.packageName, value);
                }
              }

              if(NetworkLogService.toastBlockedApps.get(value.app.packageName) == null) {
                showToast = true;

                if(NetworkLogService.invertUploadDownload) {
                  throughput = StringUtils.formatToBytes(value.download) + (NetworkLogService.throughputBps ? "bps/" : "B/")  + StringUtils.formatToBytes(value.upload) + (NetworkLogService.throughputBps ? "bps" : "B");
                } else {
                  throughput = StringUtils.formatToBytes(value.upload) + (NetworkLogService.throughputBps ? "bps/" : "B/") + StringUtils.formatToBytes(value.download) + (NetworkLogService.throughputBps ? "bps" : "B");
                }

                if(MyLog.enabled && MyLog.level >= 2 && value.displayed == false) {
                  MyLog.d(2, value.app.name + " throughput: " + throughput);
                }

                if(NetworkLogService.toastShowAddress) {
                  toastString.append(newline + "<b>" +  value.app.name + "</b>: <u>" + value.address + "</u> <i>" + throughput + "</i>");
                } else {
                  toastString.append(newline + "<b>" +  value.app.name + "</b>: " + throughput);
                }

                newline = "<br>";
              }

              value.displayed = true;

              if(currentTime >= value.clearTime) {
                entries.remove();
              }
            }

            if(showToast) {
              NetworkLogService.showToast(toastString);
            }

            updateThroughput(totalUpload, totalDownload);

            totalUpload = 0;
            totalDownload = 0;
          }
        }

        try { Thread.sleep(1000); } catch (Exception e) { Log.d("NetworkLog", "ThroughputUpdater", e); }
      }
    }
  }

  static ThroughputUpdater updater;

  public static void startUpdater() {
    if(updater != null) {
      stopUpdater();
    }

    updateThroughput(0, 0);
    updater = new ThroughputUpdater();
    new Thread(updater, "ThroughputUpdater").start();
  }

  public static void stopUpdater() {
    if(updater == null) {
      return;
    }

    updateThroughput(-1, -1);
    updater.stop();
    updater = null;
  }

  public static void updateThroughput(long upload, long download) {
    if(NetworkLogService.instance == null || upload == -1 || download == -1) {
      throughputString = "";
    } else {
      if(NetworkLogService.invertUploadDownload) {
        throughputString = StringUtils.formatToBytes(download) + (NetworkLogService.throughputBps ? "bps/" : "B/") + StringUtils.formatToBytes(upload) + (NetworkLogService.throughputBps ? "bps" : "B");
      } else {
        throughputString = StringUtils.formatToBytes(upload) + (NetworkLogService.throughputBps ? "bps/" : "B/") + StringUtils.formatToBytes(download) + (NetworkLogService.throughputBps ? "bps" : "B");
      }

      if(MyLog.enabled && MyLog.level >= 2) {
        MyLog.d(2, "Throughput: " + throughputString);
      }
    }

    int icon;
    if(upload > 0 && download > 0) {
      icon = R.drawable.up1_down1;
    } else if(upload > 0 && download == 0) {
      icon = R.drawable.up1_down0;
    } else if(upload == 0 && download > 0) {
      icon = R.drawable.up0_down1;
    } else {
      icon = R.drawable.up0_down0;
    }

    NetworkLogService.updateNotification(icon);
    NetworkLog.updateStatus(icon);
  }

  public static void updateThroughputBps() {
    if(updater != null) {
      synchronized(throughputMap) {
        for(ThroughputData item : throughputMap.values()) {
          if(item.displayed == false) {
            if(NetworkLogService.throughputBps) {
              item.upload *= Byte.SIZE;
              item.download *= Byte.SIZE;
            } else {
              item.upload /= Byte.SIZE;
              item.download /= Byte.SIZE;
            }
          }
        }

        if(NetworkLogService.throughputBps) {
          updater.totalUpload *= Byte.SIZE;
          updater.totalDownload *= Byte.SIZE;
        } else {
          updater.totalUpload /= Byte.SIZE;
          updater.totalDownload /= Byte.SIZE;
        }

        updateThroughput(updater.totalUpload, updater.totalDownload);
      }
    }

    if(NetworkLog.appFragment != null) {
      NetworkLog.appFragment.updateAppThroughputBps();
    }
  }
}
