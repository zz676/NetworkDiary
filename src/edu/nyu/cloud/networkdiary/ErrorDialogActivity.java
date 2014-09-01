package edu.nyu.cloud.networkdiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import edu.nyu.cloud.networkdiary.R;

public class ErrorDialogActivity extends Activity {
  @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle extras = getIntent().getExtras();

      String title = getString(R.string.error_default_title);
      String message = getString(R.string.error_default_text);

      if(extras != null) {
        title = extras.getString("title");
        message = extras.getString("message");
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setNeutralButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            finish();
          }
        });
      AlertDialog alert = builder.create();
      alert.show();
    }
}
