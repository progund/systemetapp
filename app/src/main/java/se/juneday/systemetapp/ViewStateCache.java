package se.juneday.systemetapp;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public class ViewStateCache {

  private static final String LOG_TAG = ViewStateCache.class.getSimpleName();

  Map<Integer, Object> values;

  public ViewStateCache() {
    values = new HashMap<>();
  }

  public void add(Integer id, Object value) {
    values.put(id, value);
  }

  public void useValue(View inflater) {
    for (Map.Entry<Integer, Object> entry : values.entrySet()) {
      int viewId = entry.getKey();

      View view = inflater.findViewById(viewId);
      Log.d(LOG_TAG, "useValue()");
      if (view instanceof Spinner) {
        Spinner spinner = (Spinner) view;
        Long id = (Long) values.get(viewId);
        if (id == null) {
          return;
        }
        Log.d(LOG_TAG, "useValue() spinner: " + spinner + " " + id);
        spinner.setSelection(id.intValue());
      } else if (view instanceof TextView) {
        TextView tv = (TextView) view;
        String s = (String) values.get(viewId);
        if (s == null) {
          return;
        }
        Log.d(LOG_TAG, "useValue() textview: " + tv + " " + s);
        tv.setText(s);
      } else if (view instanceof EditText) {
        EditText ev = (EditText) view;
        String s = (String) values.get(viewId);
        if (s == null) {
          return;
        }
        Log.d(LOG_TAG, "useValue() edittext: " + ev + " " + s);
        ev.setText(s);
      }
    }
  }

  public void cacheValues(View inflater) {
    Log.d(LOG_TAG, "cacheValues()   ");

    for (Map.Entry<Integer, Object> entry : values.entrySet()) {
      Log.d(LOG_TAG, "cacheValues()   * " + entry);
      int viewId = entry.getKey();
      View view = inflater.findViewById(viewId);
      Log.d(LOG_TAG, "cacheValues()   * view: " + view.getClass().getName());
      if (view instanceof Spinner) {
        Spinner spinner = (Spinner) view;
        Object o = spinner.getSelectedItemId();
        add(viewId, o);
        Log.d(LOG_TAG, " * cacheValues() spinner: " + o);
      } else if (view instanceof TextView) {
        TextView tv = (TextView) view;
        String t = (String) tv.getText().toString();
        add(viewId, t);
        Log.d(LOG_TAG, " * cacheValues() textview: " + t);
      } else if (view instanceof EditText) {
        EditText ev = (EditText) view;
        String t = (String) ev.getText().toString();
        add(viewId, t);
        Log.d(LOG_TAG, " * cacheValues() edittext: " + t);
      } else {
        Log.d(LOG_TAG, " *** uh oh, not supporting type: " + view.getClass().getCanonicalName());
      }
    }
  }

}
