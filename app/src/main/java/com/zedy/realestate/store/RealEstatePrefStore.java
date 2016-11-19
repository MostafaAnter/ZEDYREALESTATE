package com.zedy.realestate.store;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mostafa_anter on 9/26/16.
 */

public class RealEstatePrefStore {
    private static final String PREFKEY = "RealEstatePreferencesStore";
    private SharedPreferences realEstatePreferences;

    public RealEstatePrefStore(Context context){
        realEstatePreferences = context.getSharedPreferences(PREFKEY, Context.MODE_PRIVATE);
    }

    public void clearPreference(){
        SharedPreferences.Editor editor = realEstatePreferences.edit();
        editor.clear().apply();
    }

    public void addPreference(String key, String value){
        SharedPreferences.Editor editor = realEstatePreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void addPreference(String key, int value){
        SharedPreferences.Editor editor = realEstatePreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void removePreference(String key){
        SharedPreferences.Editor editor = realEstatePreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public String getPreferenceValue(String key){
        return realEstatePreferences.getString(key, "");
    }

    public int getIntPreferenceValue(String key){
        return realEstatePreferences.getInt(key, 1);
    }
}
