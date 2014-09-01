package edu.nyu.cloud.networkdiary;

import android.os.SystemClock;

public class PacketGraphItem {
  double timestamp;
  double len;

  public PacketGraphItem(double len) {
    this.timestamp = System.currentTimeMillis();
    this.len = len;
  }

  public PacketGraphItem(double timestamp, double len) {
    this.timestamp = timestamp;
    this.len = len;
  }

  public String toString() {
    return "(" + timestamp + ", " + len + ")";
  }
}
