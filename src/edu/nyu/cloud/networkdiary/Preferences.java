package edu.nyu.cloud.networkdiary;

import android.content.Context;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.ListPreference;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import java.util.ArrayList;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.robobunny.SeekBarPreference;
import com.samsung.sprc.fileselector.*;
import edu.nyu.cloud.networkdiary.R;

public class Preferences extends SherlockPreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
  private InstanceData data = null;
  private AlertDialog warnStartForegroundDialog = null;

  private class InstanceData {
    boolean history_dialog_showing;
    boolean start_foreground_dialog_showing;
    boolean clearlog_dialog_showing;
    boolean clearlog_progress_dialog_showing;
    boolean selectToastApps_dialog_showing;
    ArrayList<AppsSelector.AppItem> selectToastApps_appData;
    boolean selectBlockedApps_dialog_showing;
    ArrayList<AppsSelector.AppItem> selectBlockedApps_appData;

    InstanceData() {
      history_dialog_showing = NetworkLog.history.dialog_showing;
      start_foreground_dialog_showing = (warnStartForegroundDialog == null) ? false : true;
      clearlog_dialog_showing = NetworkLog.clearLog.dialog != null && NetworkLog.clearLog.dialog.isShowing();
      clearlog_progress_dialog_showing = NetworkLog.clearLog.progressDialog != null && NetworkLog.clearLog.progressDialog.isShowing();

      if(NetworkLog.selectToastApps != null && NetworkLog.selectToastApps.dialog != null && NetworkLog.selectToastApps.dialog.isShowing()) {
        selectToastApps_dialog_showing = true;
        selectToastApps_appData = NetworkLog.selectToastApps.appData;
      }

      if(NetworkLog.selectBlockedApps != null && NetworkLog.selectBlockedApps.dialog != null && NetworkLog.selectBlockedApps.dialog.isShowing()) {
        selectBlockedApps_dialog_showing = true;
        selectBlockedApps_appData = NetworkLog.selectBlockedApps.appData;
      }
    }
  }

  @Override
    public void onDestroy() {
      MyLog.d("Destroying preferences activity");

      if(warnStartForegroundDialog != null) {
        warnStartForegroundDialog.dismiss();
      }

      if(NetworkLog.history.dialog_showing) {
        NetworkLog.history.dialog.dismiss();
        NetworkLog.history.dialog = null;
      }

      if(NetworkLog.clearLog.dialog != null && NetworkLog.clearLog.dialog.isShowing()) {
        NetworkLog.clearLog.dialog.dismiss();
        NetworkLog.clearLog.dialog = null;
      }

      if(NetworkLog.clearLog.progressDialog != null && NetworkLog.clearLog.progressDialog.isShowing()) {
        NetworkLog.clearLog.progressDialog.dismiss();
        NetworkLog.clearLog.progressDialog = null;
      }

      if(NetworkLog.selectBlockedApps != null && NetworkLog.selectBlockedApps.dialog != null && NetworkLog.selectBlockedApps.dialog.isShowing()) {
        NetworkLog.selectBlockedApps.dialog.dismiss();
        NetworkLog.selectBlockedApps = null;
      }

      if(NetworkLog.selectToastApps != null && NetworkLog.selectToastApps.dialog != null && NetworkLog.selectToastApps.dialog.isShowing()) {
        NetworkLog.selectToastApps.dialog.dismiss();
        NetworkLog.selectToastApps = null;
      }

      super.onDestroy();
    }

  @Override
    public Object onRetainNonConfigurationInstance() {
      MyLog.d("Saving preference run");
      data = new InstanceData();
      return data;
    }

  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      MyLog.d("Creating preferences activity");

      addPreferencesFromResource(R.xml.preferences);

      findPreference("logfile").setOnPreferenceClickListener(this);
      findPreference("filter_dialog").setOnPreferenceClickListener(this);
      findPreference("log_method").setOnPreferenceClickListener(this);
      findPreference("manage_apps_dialog").setOnPreferenceClickListener(this);
      findPreference("notifications_toast_apps_dialog").setOnPreferenceClickListener(this);
      findPreference("clear_log").setOnPreferenceClickListener(this);
      findPreference("presort_by").setOnPreferenceChangeListener(this);
      findPreference("sort_by").setOnPreferenceChangeListener(this);

      CheckBoxPreference foreground = (CheckBoxPreference) findPreference("start_foreground");
      foreground.setOnPreferenceClickListener(this);
      foreground.setChecked(NetworkLog.settings.getStartForeground());

      String entries[] = getResources().getStringArray(R.array.interval_entries);
      String values[] = getResources().getStringArray(R.array.interval_values);

      final Context context = this;
      OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, final Object newValue) {
          if(preference.getKey().equals("history_size")) {
            NetworkLog.appFragment.clear();
            NetworkLog.logFragment.clear();
            new Thread(new Runnable() {
              public void run() {
                NetworkLog.history.loadEntriesFromFile(context, (String)newValue);
              }
            }).start();
            return true;
          }

          if(preference.getKey().equals("interval_placeholder")) {
            NetworkLog.settings.setGraphInterval(Long.parseLong((String) newValue));
            return true;
          }
          
          if(preference.getKey().equals("viewsize_placeholder")) {
            NetworkLog.settings.setGraphViewsize(Long.parseLong((String) newValue));
            return true;
          }

          if(preference.getKey().equals("notifications_toast")) {
            Boolean toast_enabled = (Boolean) newValue;
            ListPreference pref = (ListPreference) findPreference("notifications_toast_position");
            String value = pref.getValue();
            SeekBarPreference sbpref = (SeekBarPreference) findPreference("notifications_toast_yoffset");

            if(toast_enabled && (value.equals("1") || value.equals("2"))) {
              sbpref.setEnabled(true);
            } else {
              sbpref.setEnabled(false);
            }
          }

          if(preference.getKey().equals("notifications_toast_position")) {
            String value = (String) newValue;
            SeekBarPreference sbpref = (SeekBarPreference) findPreference("notifications_toast_yoffset");

            if(value.equals("1") || value.equals("2")) {
              sbpref.setEnabled(true);
            } else {
              sbpref.setEnabled(false);
            }
            return true;
          }

          return true;
        }
      };

      ListPreference pref = (ListPreference) findPreference("interval_placeholder");
      pref.setEntries(entries);
      pref.setEntryValues(values);
      pref.setValue(String.valueOf(NetworkLog.settings.getGraphInterval()));
      pref.setOnPreferenceChangeListener(changeListener);

      pref = (ListPreference) findPreference("viewsize_placeholder");
      pref.setEntries(entries);
      pref.setEntryValues(values);
      pref.setValue(String.valueOf(NetworkLog.settings.getGraphViewsize()));
      pref.setOnPreferenceChangeListener(changeListener);

      pref = (ListPreference) findPreference("history_size");
      pref.setOnPreferenceChangeListener(changeListener);

      CheckBoxPreference cbpref = (CheckBoxPreference) findPreference("notifications_toast");
      cbpref.setOnPreferenceChangeListener(changeListener);
      boolean toast_enabled = cbpref.isChecked();

      pref = (ListPreference) findPreference("notifications_toast_position");
      pref.setOnPreferenceChangeListener(changeListener);
      String value = pref.getValue();

      SeekBarPreference sbpref = (SeekBarPreference) findPreference("notifications_toast_yoffset");
      if(toast_enabled && (value.equals("1") || value.equals("2"))) {
        sbpref.setEnabled(true);
      } else {
        sbpref.setEnabled(false);
      }

      data = (InstanceData) getLastNonConfigurationInstance();

      if(data != null) {
        MyLog.d("Restoring preferences run");

        if(data.start_foreground_dialog_showing == true) {
          warnStartForegroundDialog = toggleWarnStartForeground(this, foreground);
        }

        if(data.history_dialog_showing) {
          NetworkLog.history.createProgressDialog(this);
        }

        if(data.clearlog_dialog_showing) {
          NetworkLog.clearLog.showClearLogDialog(this);
        }

        if(data.clearlog_progress_dialog_showing) {
          NetworkLog.clearLog.showProgressDialog(this);
        }

        if(data.selectBlockedApps_dialog_showing) {
          NetworkLog.selectBlockedApps = new SelectBlockedApps();
          NetworkLog.selectBlockedApps.showDialog(this, data.selectBlockedApps_appData);
        }

        if(data.selectToastApps_dialog_showing) {
          NetworkLog.selectToastApps = new SelectToastApps();
          NetworkLog.selectToastApps.showDialog(this, data.selectToastApps_appData);
        }

        MyLog.d("Restored preferences run");
      }
    }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String value = (String) newValue;

    if(preference.getKey().equals("presort_by")) {
      if(NetworkLog.appFragment != null) {
        NetworkLog.appFragment.preSortBy = Sort.forValue(value);
        NetworkLog.appFragment.setPreSortMethod();
      }
      return true;
    }

    if(preference.getKey().equals("sort_by")) {
      if(NetworkLog.appFragment != null) {
        NetworkLog.appFragment.sortBy = Sort.forValue(value);
        NetworkLog.appFragment.setSortMethod();
      }

      if(NetworkLog.menu != null) {
        com.actionbarsherlock.view.MenuItem item = null;

        if(value.equals("UID")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_uid);
        } else if(value.equals("NAME")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_name);
        } else if(value.equals("THROUGHPUT")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_throughput);
        } else if(value.equals("PACKETS")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_packets);
        } else if(value.equals("BYTES")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_bytes);
        } else if(value.equals("TIMESTAMP")) {
          item = NetworkLog.menu.findItem(R.id.sort_by_timestamp);
        }

        if(item != null) {
          item.setChecked(true);
        }
      }
      return true;
    }

    return true;
  }

  @Override
    public boolean onPreferenceClick(Preference preference) {
      MyLog.d("Preference [" + preference.getKey() + "] clicked");

      if(preference.getKey().equals("log_method")) {
        if(Iptables.getTargets(this)) {
          if(Iptables.targets.get("LOG") == null) {
            Log.w("NetworkLog", "Logging method preference not applicable to this device");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.log_method_not_applicable_title))
              .setMessage(getString(R.string.log_method_not_applicable_text))
              .setCancelable(true)
              .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  dialog.dismiss();
                }
              });
            AlertDialog alert = builder.create();
            alert.show();

            return true;
          }
        }
      }

      if(preference.getKey().equals("manage_apps_dialog")) {
        NetworkLog.selectBlockedApps = new SelectBlockedApps();
        NetworkLog.selectBlockedApps.showDialog(this);
        return true;
      }

      if(preference.getKey().equals("notifications_toast_apps_dialog")) {
        NetworkLog.selectToastApps = new SelectToastApps();
        NetworkLog.selectToastApps.showDialog(this);
        return true;
      }

      if(preference.getKey().equals("logfile")) {
        OnHandleFileListener saveListener = new OnHandleFileListener() {
          public void handleFile(final String filePath) {
            MyLog.d("Set logfile path to: " + filePath);
            NetworkLog.settings.setLogFile(filePath);
          }
        };
        new FileSelector(this, FileOperation.SAVE, saveListener, "networklog.txt", new String[] { "*.*", "*.txt" }).show();
        return true;
      }

      if(preference.getKey().equals("filter_dialog")) {
        new FilterDialog(this);
        return true;
      }

      if(preference.getKey().equals("start_foreground")) {
        warnStartForegroundDialog = toggleWarnStartForeground(this, (CheckBoxPreference) preference);
        return true;
      }

      if(preference.getKey().equals("clear_log")) {
        NetworkLog.clearLog.showClearLogDialog(this);
        return true;
      }

      return false;
    }

  public AlertDialog toggleWarnStartForeground(final Context context, final CheckBoxPreference preference) {
    if(NetworkLog.settings.getStartForeground() == false) {
      // don't warn when enabling
      preference.setChecked(true);
      NetworkLog.settings.setStartForeground(true);
      NetworkLog.toggleServiceForeground(true);
      return null;
    } else {
      preference.setChecked(true);

      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.warning_disabling_notification))
        .setCancelable(true)
        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            preference.setChecked(true);
            NetworkLog.settings.setStartForeground(true);
            NetworkLog.toggleServiceForeground(true);
            warnStartForegroundDialog = null;
            dialog.dismiss();
          }
        })
      .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          preference.setChecked(false);
          NetworkLog.settings.setStartForeground(false);
          NetworkLog.toggleServiceForeground(false);
          warnStartForegroundDialog = null;
          dialog.dismiss();
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
      return alert;
    }
  }

  public class ComingSoonDialog {
    public ComingSoonDialog(Context context) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(getString(R.string.coming_soon_title))
        .setMessage(getString(R.string.coming_soon_text))
        .setCancelable(true)
        .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        });
      AlertDialog alert = builder.create();
      alert.show();
    }
  }
}
