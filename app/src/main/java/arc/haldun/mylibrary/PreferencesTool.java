package arc.haldun.mylibrary;

import android.content.SharedPreferences;

public class PreferencesTool {

    public static String NAME = "preferences";

    private SharedPreferences sharedPreferences;

    public PreferencesTool(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setValue(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setValue(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setValue(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "null");
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public static class Keys {

        public static String REMEMBER_ME = "remember_me";
    }
}
