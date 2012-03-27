package dk.mide.fas.cmnightlies;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import dk.mide.fas.cmnightlies.model.Change;
import dk.mide.fas.cmnightlies.model.ListItem;
import dk.mide.fas.cmnightlies.model.Section;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NightliesActivity extends SherlockListActivity {
    private LayoutInflater mInflater;
    private SharedPreferences prefs;
    private String currentDevice;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // then you use
        currentDevice = prefs.getString("device", "galaxys2");
        load(currentDevice);
    }

    public void load(String device)
    {
        new GetChanges().execute(device);
        getSupportActionBar().setSubtitle(device);
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);    	
    }
    @Override
    public void onResume()
    {
    	if(currentDevice != prefs.getString("device", "galaxys2"))
    	{
    		currentDevice = prefs.getString("device", "galaxys2");
    		//device changed
            load(currentDevice);  		
    	}
    	super.onResume();
    	
    }
    
    public void gotDataEvent(ArrayList<ListItem> changes)
    {
    	setSupportProgressBarIndeterminateVisibility (Boolean.FALSE);
    	setListAdapter(new ArrayAdapter<ListItem>(this, R.layout.list_item, changes) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View row;
        		ListItem li = getItem(position);
 
        		if(!li.isSection()) {
        			row = convertView;
        			if(convertView == null || convertView.getId() != R.layout.list_item)
        			{
        				row = mInflater.inflate(R.layout.list_item, null);
        			}
        			Change change = (Change)li;
        			((TextView) row.findViewById(R.id.subject)).setText(change.subject);
            		((TextView) row.findViewById(R.id.project)).setText(
            				"("+change.project + ")"
            		);        			
        		} else {
        			row = convertView;
        			if(convertView == null || convertView.getId() != R.layout.list_section)
        			{
        				row = mInflater.inflate(R.layout.list_section, null);
        			}
        			Section section = (Section)li;
        			row = mInflater.inflate(R.layout.list_section, null);
        			//ex update-cm-9-20120324-NIGHTLY-crespo-signed.zip
        			((TextView) row.findViewById(R.id.list_item_section_text)).setText(
        					"update-cm-9-" + section.getDate() + "-NIGHTLY-" + currentDevice
        			);
        		}
        		
         
        		return row;
        	}
        });
    }
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ListItem li = (ListItem)l.getItemAtPosition(position);
		if(!li.isSection())
		{
			Change c = (Change)li;
			String url = "http://review.cyanogenmod.com/" + c.id;
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}
	}
    private class GetChanges extends AsyncTask<String, Void, ArrayList<ListItem>>{

		@Override
		protected ArrayList<ListItem> doInBackground(String... params) {
			Service s = new Service();
			try
			{
				ArrayList<ListItem> liste = s.getChanges(params[0]);
				return liste;
			} catch(Exception e)
			{
				Log.d("cm-nightlies", "getChanges exception", e);
			}
			return null;
		}
	     protected void onPostExecute(ArrayList<ListItem> result) {
	    	 if(result == null)
	    	 {
	    		 Toast.makeText(NightliesActivity.this,"Problem loading data",1000).show();
	    		 setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
	    		 
	    	 } else {
	    		 NightliesActivity.this.gotDataEvent(result);
	    	 }
	     }
    	
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
	    
	    // Configure the search info and add any event listeners
	  
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_pref:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, PreferenceActivity.class);
	            startActivity(intent);
	            return true;
	        case R.id.menu_refresh:
	        	load(currentDevice);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}