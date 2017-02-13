package com.github.ridkins.rud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.ImageView;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

        TokenSQLiteOpenHelper helper=new TokenSQLiteOpenHelper(getContext());
        SQLiteDatabase database=helper.getReadableDatabase();
        Cursor c=database.query("AccountTokenList",new String[]{"userName","userId","token","tokenSecret"},null,null,null,null,null);

        CharSequence[] entries=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];
        CharSequence[] entryValues=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];

        while (c.moveToNext()){
            entries[c.getPosition()]=c.getString(0);
            entryValues[c.getPosition()]=String.valueOf(c.getPosition());
        }

        entries[entries.length-1]=getString(R.string.add_account);
        entryValues[entryValues.length-1]="-1";

        c.close();
        database.close();

        ListPreference nowAccountList=(ListPreference) findPreference("AccountPoint");
        nowAccountList.setEntries(entries);
        nowAccountList.setEntryValues(entryValues);
        nowAccountList.setDefaultValue(defaultSharedPreferences.getString("AccountPoint","-1"));
        nowAccountList.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    if (newValue.equals("-1")){
                        getActivity().finish();
                        startActivity(new Intent(getContext(),OAuthActivity.class));
                    }
                    return true;
                }
        );

       


    }
}
