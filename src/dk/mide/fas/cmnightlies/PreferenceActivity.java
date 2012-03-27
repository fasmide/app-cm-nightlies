package dk.mide.fas.cmnightlies;

import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {     
	    super.onCreate(savedInstanceState);        
	    addPreferencesFromResource(R.xml.preferences);        
	}
}
