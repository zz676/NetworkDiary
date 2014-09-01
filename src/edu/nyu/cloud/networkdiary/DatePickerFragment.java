package edu.nyu.cloud.networkdiary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.Calendar;

import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {
  int year;
  int month;
  int day;
  DatePickerDialog.OnDateSetListener listener;

  public DatePickerFragment() {
    Calendar cal = Calendar.getInstance();
    this.year = cal.get(Calendar.YEAR);
    this.month = cal.get(Calendar.MONTH);
    this.day = cal.get(Calendar.DAY_OF_MONTH);
  }

  public DatePickerFragment(int year, int month, int day, DatePickerDialog.OnDateSetListener listener) {
    this(year, month, day);
    this.listener = listener;
  }

  public DatePickerFragment(int year, int month, int day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  public void setListener(DatePickerDialog.OnDateSetListener listener) {
    this.listener = listener;
  }

  @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new DatePickerDialog(getActivity(), this.listener, this.year, this.month, this.day);
    }
}

