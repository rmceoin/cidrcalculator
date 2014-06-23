package us.lindanrandy.cidrcalculator;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	public static final String PREFERENCE_HISTORY_ENTRIES = "history_entries";
	public static final String PREFERENCE_AUTOCOMPLETE = "autocomplete";
	public static final String PREFERENCE_CURRENTIPv6 = "CurrentIPv6";
	public static final String PREFERENCE_CURRENTBITSIPv6 = "CurrentBitsIPv6";
	public static final String PREFERENCE_INPUT_KEYBOARD = "input_keyboard";
	public static final String PREFERENCE_INPUT_KEYBOARD_DEFAULT = "Text";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
