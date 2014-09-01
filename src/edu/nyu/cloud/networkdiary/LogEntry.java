package edu.nyu.cloud.networkdiary;

public class LogEntry {
  int uid;
  String uidString;
  String in;
  String out;
  String proto;
  String src;
  String dst;
  int len;
  int spt;
  int dpt;
  long timestamp;
  boolean validated;
  boolean valid;

  public boolean isValid() {
    if(validated) {
      return valid;
    }

    validated = true;
    if(StringUtils.contains(in, "{}:=")) {
      valid = false;
      return false;
    }

    if(StringUtils.contains(out, "{}:=")) {
      valid = false;
      return false;
    }

    if(StringUtils.contains(proto, "{}:=")) {
      valid = false;
      return false;
    }

    if(StringUtils.contains(src, "{}:=")) {
      valid = false;
      return false;
    }

    if(StringUtils.contains(dst, "{}:=")) {
      valid = false;
      return false;
    }

    valid = true;
    return true;
  }
}
