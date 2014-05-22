package bundle.android.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import bundle.android.utils.DataStore;

import java.util.Iterator;

public class LocationListenerAgent extends Service implements LocationListener {

    public static LocationManager lmgrNet;
    public static LocationManager lmgrGps;
    private Context thisContext;
    private boolean gps_enabled = false;
    private final IBinder mBinder = new LocationListenerBinder();
    private int counts    = 0;
    private int sat_count = 0;
    private static final int min_gps_sat_count = 3;
    private Thread locationThread;
    private NewRunnable locationRunnable;
    private static final int ONE_MINUTE = 1000 * 60;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void onCreate() {
        thisContext = this;
        // Register ourselves for location updates

        lmgrNet = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        lmgrGps = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lmgrGps.isProviderEnabled(LocationManager.GPS_PROVIDER);
        lmgrNet.requestLocationUpdates("network", ONE_MINUTE, 10, this);
        lmgrGps.requestLocationUpdates("gps", ONE_MINUTE, 10, this);
        //lmgr.addGpsStatusListener(gpsStatusListener);
        
        //run thread in here

    	//locationThread = new Thread(locationRunnable);
    	//locationThread.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
        // Unregister listener
    	if(lmgrNet!=null){
    		lmgrNet.removeUpdates(this);
            lmgrNet = null;
    	}

    	if(lmgrGps!=null){
    		lmgrGps.removeUpdates(this);
    		lmgrGps = null;
    	}
        //locationThread.interrupt();

        
    }


    @Override
    public void onLocationChanged(Location location) {
        DataStore store = new DataStore(this);
        store.setCurrLocation(location);
        if(lmgrNet!=null)
        	lmgrNet.removeUpdates(this);
        if(lmgrGps!=null)
        	lmgrGps.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onProviderDisabled(String provider) {
        DataStore store = new DataStore(this.thisContext);
        store.saveToPrefs("log.networkStatus", provider + " disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        DataStore store = new DataStore(this.thisContext);
        store.saveToPrefs("log.networkStatus", provider + " enabled");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    /**
     * GpsStatus listener. OnChanged counts connected satellites count.
     */
    public final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {

            if(event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                try {
                    // Check number of satellites in list to determine fix state
                    GpsStatus status = lmgrGps.getGpsStatus(null);
                    Iterable<GpsSatellite>satellites = status.getSatellites();

                    sat_count = 0;

                    Iterator<GpsSatellite> satI = satellites.iterator();
                    while(satI.hasNext()) {
                        sat_count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sat_count = min_gps_sat_count + 1;
                }

            }
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocationListenerBinder extends Binder {
        public LocationListenerAgent getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationListenerAgent.this;
        }
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
       yourThread = new Thread(yourRunnable);
       return startId;
    }*/

    private final class NewRunnable implements Runnable
    {
       @Override
       public void run()
       {
           thisContext = LocationListenerAgent.this;
           // Register ourselves for location updates

           lmgrGps = (LocationManager)thisContext.getSystemService(Context.LOCATION_SERVICE);
           lmgrNet = (LocationManager)thisContext.getSystemService(Context.LOCATION_SERVICE);
           gps_enabled = lmgrGps.isProviderEnabled(LocationManager.GPS_PROVIDER);
           lmgrNet.requestLocationUpdates("network", ONE_MINUTE, 10, LocationListenerAgent.this);
           lmgrGps.requestLocationUpdates("gps", ONE_MINUTE, 10, LocationListenerAgent.this);
       }
    }

    
}

