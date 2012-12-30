package dk.mide.fas.cmnightlies;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import dk.mide.fas.cmnightlies.model.Change;
import dk.mide.fas.cmnightlies.model.Device;
import dk.mide.fas.cmnightlies.model.Device.Build;
import dk.mide.fas.cmnightlies.model.ListItem;
import dk.mide.fas.cmnightlies.model.Section;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    public static final String TAG = "app-cm-nightlies";
    private static final String CM9_SECTION_HEADER = "==== CM9 devices ====";
    private static final String CM10_SECTION_HEADER = "==== CM10 devices ====";    
    
    private LayoutInflater mInflater;
    private SharedPreferences prefs;
    private Device currentDevice;

    private ProgressDialog dialog = null;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // then you use
        currentDevice = Device.restore(prefs);
        load(currentDevice);
    }

    public void load(Device device)
    {
    	currentDevice = device;
        new GetChanges().execute(device);
        getSupportActionBar().setSubtitle(device.name);
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);    	
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
        			//ex cm-10-20121229-NIGHTLY-ville.zip cm-9-20121223-NIGHTLY-tenderloin.zip 
                    ((TextView) row.findViewById(R.id.list_item_section_text)).setText("cm-"
                            + currentDevice.getBuildVersion() + "-" + section.getDate() + "-NIGHTLY-"
                            + currentDevice.name);
        		}
        		
        		return row;
        	}
        });
    }
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ListItem li = (ListItem)l.getItemAtPosition(position);
		String url = "";
		if(!li.isSection())
		{
			Change c = (Change)li;
			url = "http://review.cyanogenmod.com/" + c.id;
		} else {
			url = "http://download.cyanogenmod.com/?device=" + currentDevice.name;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
    private class GetChanges extends AsyncTask<Device, Void, ArrayList<ListItem>>{

		@Override
		protected ArrayList<ListItem> doInBackground(Device... params) {
			try
			{
				ArrayList<ListItem> liste = Service.getChanges(params[0]);
				return liste;
			} catch(Exception e)
			{
				Log.d(TAG, "getChanges exception", e);
			}
			return null;
		}
	     protected void onPostExecute(ArrayList<ListItem> result) {
	    	 if(result == null)
	    	 {
	    		 Toast.makeText(NightliesActivity.this, "Problem loading data", Toast.LENGTH_LONG).show();
	    		 setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
	    		 
	    	 } else {
	    		 NightliesActivity.this.gotDataEvent(result);
	    	 }
	     }
    	
    }
    private class GetDevices extends AsyncTask<Void, Void, ArrayList<Device>> {
        
		@Override
		protected ArrayList<Device> doInBackground(Void... params) {
			ArrayList<Device> liste = Service.getCm9Devices();
			liste.add(0, new Device(CM9_SECTION_HEADER, Build.CM9));
			liste.add(new Device(CM10_SECTION_HEADER, Build.CM10));
            liste.addAll(Service.getCm10Devices());
			return liste;
		}
		protected void onPostExecute(ArrayList<Device> result)
		{
    	    if(NightliesActivity.this.dialog != null)
    	    {
    	    	NightliesActivity.this.dialog.hide();
    	    	NightliesActivity.this.dialog = null;
    	    }
	    	 if(result == null)
	    	 {
	    		 Toast.makeText(NightliesActivity.this, "Problem loading data", Toast.LENGTH_LONG).show();
	    	 } else {
	    		 NightliesActivity.this.gotDevices(result);
	    	 }
		}
    }
    public void gotDevices(ArrayList<Device> liste)
    {

    	openDeviceSelector(liste);
    	
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
	    
	    // Configure the search info and add any event listeners
	  
	    return super.onCreateOptionsMenu(menu);
	}
	public void openDeviceSelector(final ArrayList<Device> items)
	{

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose device");
		builder.setItems(Service.convertToArray(items), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        Device device = items.get(item);
		        if (!CM9_SECTION_HEADER.equals(device.name) && !CM10_SECTION_HEADER.equals(device.name)) {
		            device.save(NightliesActivity.this.prefs);
	                NightliesActivity.this.load(device);    
		        }
		    }
		});
		AlertDialog alert = builder.create();		
		alert.show();
		
	}
	public void getDevices()
	{
		
		dialog = ProgressDialog.show(this, "", 
                "Getting device list. \nHold on...", true);
		dialog.show();
		new GetDevices().execute();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_pref:
	        	getDevices();
	            return true;
	        case R.id.menu_refresh:
	        	load(currentDevice);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}