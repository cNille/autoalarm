package se.shapeapp.autoalarm.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nille on 22/06/15.
 */
public class Settings {

    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private static final String MY_PREFS_NAME = "SleepyPreferences";
    public int default_sleep = 8;
    public int default_awakening = 45;
    public int default_maxHour = 12;
    public int default_maxMinute = 0;
    public int default_minHour = 6;
    public int default_minMinute = 0;
    public String password = "";

    public Settings(Context c){
        this.context = c;
    }

    public void saveStringSetting(String key, String value){
        editor = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void saveIntSetting(String key, int value){
        editor = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public String loadStringSetting(String key){
        prefs = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public int loadIntSetting(String key, int default_value){
        prefs = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_PRIVATE);
        return prefs.getInt(key, default_value);
    }
}
