package edu.nyu.cloud.networkdiary;

import android.content.Context;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jjoe64.graphview.LineGraphView;

public class MyGraphView extends LineGraphView {
  private SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat time_formatter = new SimpleDateFormat("HH:mm:ss.SSS");

  public MyGraphView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MyGraphView(Context context, String title) {
    super(context, title);
  }

  @Override
    protected String formatLabel(double value, boolean isValueX)
    {
      if(isValueX)
      {
        Date d = new Date((long) value);
        String date = date_formatter.format(d);
        String time = time_formatter.format(d);
        return date + "\n" + time;
      }
      else
      {
        // y-axis
        String result;
        if(value >= 1000000000) {
          result = String.format("%.1f", value / 1000000000.0f) + "G";
        } else if(value >= 1000000) {
          result = String.format("%.1f", value / 1000000.0f) + "M";
        } else if(value >= 1000) {
          result = String.format("%.1f", value / 1000.0f) + "K";
        } else {
          int v = (int)value;
          if(v == 1) {
            v = 0;
          }
          result = String.valueOf(v);
        }

        return result;
      }
    }
}
