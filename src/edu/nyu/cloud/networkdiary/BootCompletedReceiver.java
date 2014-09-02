package edu.nyu.cloud.networkdiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootCompletedReceiver extends BroadcastReceiver {
  @Override
    public void onReceive(Context context, Intent intent) {
      MyLog.d("Received broadcast: " + intent.getAction());

      if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        SharedPreferences prefs = context.getSharedPreferences("com.googlecode.networklog_preferences", Context.MODE_PRIVATE);

        if(prefs.getBoolean("startServiceAtBoot", false) == true) {
          MyLog.d("Starting service at boot");
          Intent i = new Intent(context, NetworkLogService.class);

          i.putExtra("logfile", prefs.getString("logfile", null));

          context.startService(i);
        } else {
          MyLog.d("Not starting service at boot");
        }
      }
    }
}
