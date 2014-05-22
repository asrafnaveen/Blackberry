package bundle.android.views;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import bundle.android.PublicStuffApplication;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
/*import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
*/import com.wassabi.psmobile.R;

public class RequestDetailsMapActivity extends android.support.v4.app.FragmentActivity implements
LocationListener {
	private PublicStuffApplication app;
	private Bundle b;
	private double  latitude;
	private double longitude;
	//private GoogleMap mMap;
	//private OnLocationChangedListener mapLocationListener=null;
	public static LocationManager lmgrNet;
    public static LocationManager lmgrGps;
    private Criteria crit=new Criteria();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//get initializing data from newRequestActivity
		b = getIntent().getExtras();
		latitude = b.getDouble("latitude");
		longitude = b.getDouble("longitude");

		FlurryAgent.logEvent("Request Details Maps View");

		//set content view and fonts
		setContentView(R.layout.requestdetailsmap);
		app = (PublicStuffApplication)getApplicationContext();
		DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Request Details Maps View");
		Utils.useLatoInView(this, this.findViewById(android.R.id.content));

		lmgrNet = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        lmgrGps = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		
		RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
		headerLayoutView.setBackgroundColor(app.getNavColor());

		ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
		if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
			AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
			imageLoader.execute(new Void[0]);
		}
		else{
			cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
		}

		//Map
	//	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
		//		.findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			// First incarnation of this activity.
		//	mapFragment.setRetainInstance(true);


		} else {
			// Reincarnated activity. The obtained map is the same map instance in the previous
			// activity life cycle. There is no need to reinitialize it.
			//mMap = mapFragment.getMap();
		}
		//setUpMap();
	}

	public void back(View v){
		finish();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();

		FlurryAgent.onStartSession(this, "2C3QVVZMX8Q5M6KF3458");
	}
	
	@Override
	protected void onStop() {
		//if(currLocationOverlay!=null)
		//	currLocationOverlay.disableMyLocation();
		//deactivate();
		FlurryAgent.endTimedEvent(this.getSharedPreferences("publicStuffPrefs", MODE_PRIVATE).getString("currentEvent", ""));
		FlurryAgent.onEndSession(this);
		//System.gc();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		
	}

	/*@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub

	}
*/
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		/*if(mapLocationListener != null) {
		      mapLocationListener.onLocationChanged(location);
		}*/
		 		//geocode api and update text
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

/*	@Override
	public void activate(OnLocationChangedListener listener) {
		this.mapLocationListener=listener;
		if(lmgrNet!=null)
			lmgrNet.requestLocationUpdates("network", 5000, 10, this);
        if(lmgrGps!=null)
        	lmgrGps.requestLocationUpdates("gps", 5000, 10, this);
	}

	@Override
	  public void deactivate() {
	    lmgrNet.removeUpdates(this);
	    lmgrGps.removeUpdates(this);
	    this.mapLocationListener=null;
	}

	private void setUpMap() {
		LatLng passedLatLng = new LatLng(latitude, longitude);
		Location userLocation = new Location("passedLocation");
		userLocation.setLatitude(latitude);
		userLocation.setLongitude(longitude);
		userLocation.setAccuracy(100);
		mListener.onLocationChanged(userLocation);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setLocationSource(this);
		mMap.setOnCameraChangeListener(this);
		mMap.addMarker(new MarkerOptions().position(passedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange_shadow)));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passedLatLng, mMap.getMaxZoomLevel()-4));
	}
*/	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }

}
