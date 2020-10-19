package com.radiusnetworks.ibeaconreference;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.ibeaconreference.R;
import com.radiusnetworks.ibeaconreference.R.id;
import com.radiusnetworks.ibeaconreference.R.layout;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class MonitoringActivity extends Activity implements IBeaconConsumer  {
	
	protected static final String TAG = "MonitoringActivity";
	
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
	    iBeaconManager.bind(this);			
	}
	
	// --- helper methods --- //
	
	public void onRangingClicked(View view) {
		Intent myIntent = new Intent(this, RangingActivity.class);
		this.startActivity(myIntent);
	}
	public void onBackgroundClicked(View view) {
		Intent myIntent = new Intent(this, BackgroundActivity.class);
		this.startActivity(myIntent);
	}

	private void verifyBluetooth() {
		try {
			if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");			
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
			            System.exit(0);					
					}					
				});
				builder.show();
			}			
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");			
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
		            System.exit(0);					
				}		
			});
			builder.show();
			
		}
	}
	
    private void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.append(line + "\n");            	    	    		
    	    }
    	});
    }
	
    // --- activity overrides --- //
    
    @Override 
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    
    @Override 
    protected void onPause() {
    	super.onPause();
    	if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);    		
    }
    
    @Override 
    protected void onResume() {
    	super.onResume();
    	if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);    		
    }
    
    // --- ibeacon overrides --- //
    
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
	        @Override
	        public void didEnterRegion(Region region) {
	          logToDisplay("Enter Region " + region.getMinor()+ ", " + region.getMajor() +" first time");       
	        }
	
	        @Override
	        public void didExitRegion(Region region) {
	        	logToDisplay("I no longer see an iBeacon");
	        }
	
	        @Override
	        public void didDetermineStateForRegion(int state, Region region) {
	        	logToDisplay("stateDetermine: "+state + " , in " +  + region.getMinor()+ ", " + region.getMajor());     
	        }
	
	
	        });
        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, 0, 1));
            logToDisplay("Monitoring starts Region: " + iBeaconManager.getMonitoredRegions().iterator().next().getMinor()+ " first time");  
        } catch (RemoteException e) {   }
    }
	
}
