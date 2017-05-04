package utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class PreferenceUtils<T> {

    private Context _context;
    public T _object;

    public void setObject(T object) {
        this._object = object;
    }

    public PreferenceUtils(Context context) {
        _context = context;
    }

    private SharedPreferences GetSharedPreferences(String preferenceKey) {
        return _context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE);
    }

    public void AddToPreferences(String preferenceKey, T object) {
        RemoveFromPreferences(preferenceKey);
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = _context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(preferenceKey);
        editor.apply();
        editor.putString(preferenceKey, gson.toJson(object));
        editor.apply();
    }

    public void RemoveFromPreferences(String preferenceKey) {
        SharedPreferences sharedPreferences = GetSharedPreferences(preferenceKey);
        if (sharedPreferences != null && sharedPreferences.contains(preferenceKey)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(preferenceKey);
            editor.apply();
        }

    }

    public boolean IsInPreferences(String preferenceKey) {
        SharedPreferences sharedPreferences = GetSharedPreferences(preferenceKey);
        return sharedPreferences != null && sharedPreferences.contains(preferenceKey);
    }

    public T GetFromPreferences(String preferenceKey, Type type) {
        SharedPreferences sharedPreferences = GetSharedPreferences(preferenceKey);
        String value = sharedPreferences.getString(preferenceKey, "");
        if (value.equals(""))
            return null;
        Gson gson = new Gson();
        return gson.fromJson(value, type);
    }
}
