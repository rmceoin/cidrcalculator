/*
 *
 * Copyright (C) 2014 Randy McEoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package us.lindanrandy.cidrcalculator;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Preferences extends PreferenceActivity {

    public static final String PREFERENCE_HISTORY_ENTRIES = "history_entries";
    public static final String PREFERENCE_AUTOCOMPLETE = "autocomplete";
    public static final String PREFERENCE_CURRENTIPv6 = "CurrentIPv6";
    public static final String PREFERENCE_CURRENTBITSIPv6 = "CurrentBitsIPv6";
    public static final String PREFERENCE_INPUT_KEYBOARD = "input_keyboard";
    public static final String PREFERENCE_NOTIFICATION = "notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        Prefs1Fragment mPrefsFragment = new Prefs1Fragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
    }

    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();

            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            String input_keyboard=sp.getString(Preferences.PREFERENCE_INPUT_KEYBOARD,
                    getString(R.string.custom_hex));

            if (input_keyboard.contentEquals(getString(R.string.custom_hex))) {
                // disable autocomplete choice if custom hex keyboard is in use
                getPreferenceScreen().findPreference(PREFERENCE_AUTOCOMPLETE).setEnabled(false);
            } else {
                getPreferenceScreen().findPreference(PREFERENCE_AUTOCOMPLETE).setEnabled(true);
            }
        }
    }

}
