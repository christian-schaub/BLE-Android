package com.radiusnetworks.ibeaconreference;

import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeaconreference.R;
import com.radiusnetworks.ibeaconreference.R.id;
import com.radiusnetworks.ibeaconreference.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;

public class RangingActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
	private final int EDIT_FIELD_LINES = 10;
	
	private int check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranging2);
        iBeaconManager.bind(this);
        EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
        editText.setLines(EDIT_FIELD_LINES );
    }
    
    private void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
    	    	editText.append(line+"\n");            	
    	    }
    	});
    }
    
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
    
    /**
     * 
     * Region
     * 
     * 
     * This class represents a criteria of fields used to match iBeacons. 
     * The strange name comes from the iOS implementation, where the idea of a "Region" is also used for a geofence. 
     * The idea is that a grouping of one or more iBeacons are analogous to a geofence region. 
     * The uniqueId field is used to distinguish this Region in the system. 
     * When you set up monitoring or ranging based on a Region and later want to stop monitoring or ranging, you must do so by passing a Region object that has the same uniqueId field value. 
     * If it doesn't match, you can't cancel the operation. There is no other purpose to this field. 
     * The other fields: proximityUuid, major and minor are a three part unique identifier for a single iBeacon. 
     * When constructing a range, any or all of these fields may be set to null, which indicates that they are a wildcard 
     * and will match any value. Note that this differs from the iOS implementation that does not let you set a wildcard on the proximityUuid field. 
     *
       major

		    Part 2 of 3 of an iBeacon identifier. A 16 bit integer typically used to identify a common grouping of iBeacons.
		
		minor
		
		    Part 3 of 3 of an iBeacon identifier. A 16 bit integer typically used to identify an individual iBeacon within a group.
		
		proximityUuid
		
		
		    Part 1 of 3 of an iBeacon identifier. A 26 byte UUID typically used to identify the company that owns a set of iBeacons.
		
		uniqueId


    		A unique identifier set by the class that constructs the Region so it can cancel Ranging and Monitoring actions 
     *
     */
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
	        @Override 
	        public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
	        	
	            if (iBeacons.size() > 0) {
	            //	logToDisplay("new range noti set: " + region.getMajor() + " - " + region.getMinor()); 
	            	for (int i = 0; i< iBeacons.size();i++) {
	            		EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
	            		int dis = 0;
	            		IBeacon tempBeacon = iBeacons.iterator().next();
	            		dis = calcDistance(tempBeacon);
	            		logToDisplay((i+1) + ". iBeacon my algo " + dis + " meters");
	            		logToDisplay((i+1) + ". iBeacon lib algo " + String.valueOf(tempBeacon.getAccuracy()).substring(0, 4) + " meters");            	
	            	}
	            }
	        }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", "1438-0001-b43a-11e3-98e5-0002a5d5c51b", 1, 1));
            logToDisplay("starts ranging ibeacons");  
        } catch (RemoteException e) {   }
    }
    
    /**
     * Calc the distance to beacon x
     * @author chris
     * @param beacon
     * @return
     */
	private int calcDistance(IBeacon beacon) {
		int val = 0;
		val = (beacon.getRssi() * -(1)) - (beacon.getTxPower() * (-1));
		if (val <= beacon.getTxPower()) return 0;
		val = val / 10;
		return (val==0)? 1 : val;
	}
}
