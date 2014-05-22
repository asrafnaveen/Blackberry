package bundle.android.utils;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


public class CurrentLocation {
    private static final int ONE_MINUTE = 1000 * 60;
    /**
     * Returns best location using LocationManager.getBestProvider()
     * also looks at best location between network and GPS
     *
     * @LocationManager manager
     * @return Location|null
     */
    public static Location getLocation(LocationManager manager){

        // fetch last known location and update it
        try {

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            String strLocationProvider = manager.getBestProvider(criteria, true);

            Location locationGPS_best = manager.getLastKnownLocation(strLocationProvider);
            Location location = getNetLocation(manager);
            		
            if (isBetterLocation(locationGPS_best, location)) 
            {
            	location = locationGPS_best;
            	Log.v("GPSBetter","GPS is better and fresh");
            }
            
            if(location != null){
                return location;
            }
            else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

        
    /**
     * Returns location explicitly from the network (Wifi/Cell)
     *
     * @LocationManager manager
     * @return Location|null
     */
    
    public static Location getNetLocation(LocationManager manager){

        // fetch last known location and update it
        try {

            Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null){
                return location;
            }
            else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The last best location, usually from DataStore
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        else if(location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        Log.v("Time_Delta", String.valueOf(timeDelta));
        boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than one minute since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than one minute older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
