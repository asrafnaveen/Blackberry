package bundle.android.views;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.RequestGalleryViewAdapter;
import bundle.android.adapters.RequestListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.RequestListVO;
import bundle.android.service.LocationListenerAgent;
import bundle.android.utils.CurrentLocation;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomProgressDialog;
import bundle.android.views.layouts.BalloonLayout;

import com.flurry.android.FlurryAgent;
/*import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
*/import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.wassabi.psmobile.R;


public class RequestListActivity extends android.support.v4.app.FragmentActivity implements 
LocationListener {
    private LocationListenerAgent mService;
    private boolean mBound = false;
    private PublicStuffApplication app;
    //top bars
    private View listHighlight;
    private View galleryHighlight;
    private View mapHighlight;

    //request list
    private ListView requestListView;
    private RequestListViewAdapter listAdapter;
    private final ArrayList<RequestListVO> requestsList = new ArrayList<RequestListVO>();

    //request gallery
    private GridView requestGalleryView;
    private RequestGalleryViewAdapter galleryAdapter;
    private final ArrayList<RequestListVO> imageList = new ArrayList<RequestListVO>();

    //request map
    private RelativeLayout requestMapLayout;
 
   // private HashMap<Marker, RequestListVO> markerMap = new HashMap<Marker, RequestListVO>();
    BalloonLayout mapBalloon;
   // private PSRequestListItemizedOverlay itemizedOverlay;
    private Button refreshMap;
    private final Handler mapHandler = new Handler();
    Location maplocation;

    //data
    private HashMap<String, String> filters;

    private final int FILTER_ACTIVITY = 1;
    private final int REQUEST_DETAILS_ACTIVITY = 2;
    private String originator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        setContentView(R.layout.requestlist);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();

        FlurryAgent.logEvent("Nearby Requests");
        
        //get request_id
        Bundle b;
        b = getIntent().getExtras();
        filters = (HashMap<String, String>) b.getSerializable("filters");
        originator = b.getString("originator");
        //preloadList(originator);
        getRequestList(filters, false);
    }

    /**
     * Initialize all elements in view
     */
    private void initElements(){
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
       listHighlight = this.findViewById(R.id.listHighlight);
       galleryHighlight = this.findViewById(R.id.galleryHighlight);
       mapHighlight = this.findViewById(R.id.mapHighlight);
       requestListView = (ListView)this.findViewById(R.id.requestListView);
        requestListView.setClickable(true);
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                //System.gc();
                Bundle b = new Bundle();
                b.putInt("request_id", requestsList.get(position).getId());
                Intent intent = new Intent(RequestListActivity.this, RequestDetailsActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, REQUEST_DETAILS_ACTIVITY);
            }
        });
       requestGalleryView = (GridView)this.findViewById(R.id.requestGalleryView);
        requestGalleryView.setClickable(true);
        requestGalleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                //System.gc();
                Bundle b = new Bundle();
                b.putInt("request_id", imageList.get(position).getId());
                Intent intent = new Intent(RequestListActivity.this, RequestDetailsActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, REQUEST_DETAILS_ACTIVITY);
            }
        });
        requestMapLayout = (RelativeLayout)this.findViewById(R.id.requestMapLayout);
      
        refreshMap = (Button)this.findViewById(R.id.refreshMap);
    }



    /**
     * Get request list from api based on filters
     * @param filters
     */
    private void getRequestList(HashMap<String, String> filters, boolean fromFilter){
        final boolean filtered = fromFilter;
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("requests_list"), Utils.isNetworkAvailable(RequestListActivity.this)){
            final CustomProgressDialog dialog = new CustomProgressDialog(RequestListActivity.this, RequestListActivity.this.getString(R.string.getRequestList));
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                dialog.show();
                dialog.setCancelable(false);
            }
            @Override
            protected void onPostExecute(String result)
            {
            	if(dialog != null && dialog.isShowing()){
					dialog.dismiss();
				}
               if(result.length()>0){
                    try {
                        JSONParser parser = new JSONParser(result);
                        if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                            populateRequestListArray(parser.getResponse().getJSONArray("requests"), filtered);
                            if(requestListView.getVisibility()==View.VISIBLE)populateListView();
                            else if(requestGalleryView.getVisibility()==View.VISIBLE)populateGalleryView();
                            else if(requestMapLayout.getVisibility()==View.VISIBLE)populateMapView();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally{
                        if(!RequestListActivity.this.isFinishing() && dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
               } else {
                   if(dialog != null && dialog.isShowing()){
                       dialog.dismiss();
                   }
                   Utils.noConnection(RequestListActivity.this);
               }
            }
        };
        if(filters.containsKey("page")) clientGet.addParameter("page", filters.get("page"));
        if(filters.containsKey("limit")) clientGet.addParameter("limit", filters.get("limit"));
        if(filters.containsKey("radius")) clientGet.addParameter("radius", filters.get("radius"));
        if(filters.containsKey("requestTypeId")) clientGet.addParameter("request_type_id", filters.get("requestTypeId"));
        if(filters.containsKey("sortBy")) clientGet.addParameter("sort_by", filters.get("sortBy"));
        if(filters.containsKey("status")) clientGet.addParameter("status", filters.get("status"));
        if(filters.containsKey("afterTimestamp")) clientGet.addParameter("after_timestamp", filters.get("afterTimestamp"));
        if(filters.containsKey("beforeTimestamp")) clientGet.addParameter("before_timestamp", filters.get("beforeTimestamp"));
        if(filters.containsKey("nearby")){
            clientGet.addParameter("nearby", filters.get("nearby"));
            if(filters.containsKey("latitude")) clientGet.addParameter("lat", filters.get("latitude"));
            if(filters.containsKey("longitude")) clientGet.addParameter("lon", filters.get("longitude"));
        }
        if(filters.containsKey("userRequests")) clientGet.addParameter("user_requests", filters.get("userRequests"));
        if(filters.containsKey("followedRequests")) clientGet.addParameter("followed_requests", filters.get("followedRequests"));
        if(filters.containsKey("votedRequests")) clientGet.addParameter("voted_requests", filters.get("votedRequests"));
        if(filters.containsKey("commentedRequests")) clientGet.addParameter("commented_requests", filters.get("commentedRequests"));

        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }

    private void populateRequestListArray(JSONArray requests, boolean fromFilter){
        requestsList.clear();
        imageList.clear();
        DataStore store = new DataStore(this);
        Location currLocation;
        currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);
        Location pastLocation = store.getCurrLocation();

        if(!CurrentLocation.isBetterLocation(currLocation, pastLocation)){
            currLocation = pastLocation;
        }
        if(currLocation==null){
            currLocation = new Location("Default City");
            currLocation.setLatitude(app.getCityLat());
            currLocation.setLongitude(app.getCityLon());
        }

        for(int i=0; i<requests.length(); i++){
            try {
                JSONObject request = requests.getJSONObject(i).getJSONObject("request");
                RequestListVO requestListVO = new RequestListVO();
                requestListVO.setId(request.getInt("id"));
                if(!request.getString("image_thumbnail").equals("")){
                	requestListVO.setImage(request.getString("image_thumbnail"));
                	UrlImageViewHelper.loadUrlDrawable(RequestListActivity.this, request.getString("image_thumbnail"));
                }
                requestListVO.setTitle(request.getString("title"));
                requestListVO.setAddress(request.getString("address")+ ", " + request.getString("location") + " " + request.getString("zipcode"));
                requestListVO.setStatus(request.getString("status"));
                requestListVO.setLatitude(request.getDouble("lat"));
                requestListVO.setLongitude(request.getDouble("lon"));
                 Location requestLocation = new Location("request");
                requestLocation.setLatitude(requestListVO.getLatitude());
                requestLocation.setLongitude(requestListVO.getLongitude());
                requestListVO.setDistance(getDistance(currLocation, requestLocation));
                requestListVO.setFollowersCount(request.getInt("count_followers"));
                requestListVO.setCommentsCount(request.getInt("count_comments"));
                if(request.has("user_follows")) requestListVO.setUserFollowing(request.getInt("user_follows")==1);
                if(request.has("user_comments")) requestListVO.setUserComment(request.getInt("user_comments")==1);
                if(request.has("user_request")) requestListVO.setUserRequest(request.getInt("user_request")==1);
                requestsList.add(requestListVO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
       // if(!fromFilter) saveData(requestsList, originator);
    }

    private void populateListView(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        listAdapter = new RequestListViewAdapter(RequestListActivity.this, requestsList, mLInflater, (ListView)findViewById(R.id.requestListView));
        requestListView.setAdapter(listAdapter);
        requestListView.invalidateViews();
        ((BaseAdapter)((ListView)findViewById(R.id.requestListView)).getAdapter()).notifyDataSetChanged();
    }

    private void populateGalleryView(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(RequestListVO requestListVO : requestsList){
           if(!requestListVO.getImage().equals("")){
              imageList.add(requestListVO);
           }
        }
        galleryAdapter = new RequestGalleryViewAdapter(RequestListActivity.this, imageList, mLInflater, (GridView)findViewById(R.id.requestGalleryView));
        requestGalleryView.setAdapter(galleryAdapter);
        
        //galleryAdapter.notifyDataSetChanged();
        //requestGalleryView.invalidateViews();
        //((BaseAdapter)((GridView)findViewById(R.id.requestGalleryView)).getAdapter()).notifyDataSetChanged();
    }
    private void populateMapView(){
    	/*
        
        int minLatitude = Integer.MAX_VALUE;
        int maxLatitude = Integer.MIN_VALUE;
        int minLongitude = Integer.MAX_VALUE;
        int maxLongitude = Integer.MIN_VALUE;
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		//if (savedInstanceState == null) {
			// First incarnation of this activity.
			//mapFragment.setRetainInstance(true);
		//} else {
			// Reincarnated activity. The obtained map is the same map instance in the previous
			// activity life cycle. There is no need to reinitialize it.
        	mapFragment.setRetainInstance(true);
			//requestMapView = mapFragment.getMap();
			//setUpMap();
		//}
        for(RequestListVO requestListVO : requestsList){

            int lat =  (int)(requestListVO.getLatitude()*1E6);
            int lon = (int)(requestListVO.getLongitude()*1E6);
            maxLatitude = Math.max(lat, maxLatitude);
            minLatitude = Math.min(lat, minLatitude);
            maxLongitude = Math.max(lon, maxLongitude);
            minLongitude = Math.min(lon, minLongitude);

            Marker marker = requestMapView.addMarker(new MarkerOptions().position(new LatLng(requestListVO.getLatitude(), requestListVO.getLongitude()))
            		.title(requestListVO.getTitle())
            		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange_shadow)));
            
            markerMap.put(marker, requestListVO);
            
            //GeoPoint point = new GeoPoint(lat, lon);
            PSRequestListOverlayItem overlayItem = new PSRequestListOverlayItem(point, "", "", requestListVO);
			

            itemizedOverlay = new PSRequestListItemizedOverlay(this.getResources().getDrawable(R.drawable.ic_map_pin_orange), requestMapView);
            itemizedOverlay.setSnapToCenter(false);
            itemizedOverlay.setOnSingleTapListener(new MapViewTapListener());
            itemizedOverlay.setBalloonBottomOffset(16);
            itemizedOverlay.addOverlay(overlayItem);
            overlays.add(itemizedOverlay);
            
            
        }
        if(!requestsList.isEmpty())
        	setUpMap(requestsList.get(0).getLatitude(),  requestsList.get(0).getLongitude());
        requestMapView.setOnMarkerClickListener(new OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub
				return false;
			}
        
        });
        	.setOnInfoWindowClickListener(
        }
        		  new OnInfoWindowClickListener(){
        		    public void onInfoWindowClick(Marker marker){
        		      Intent nextScreen = new Intent(MapsActivity.this,EventActivity.class);
        		        
        		      //build request object, maybe just id or something
        		      //nextScreen.putExtra("userId", "" + userId);
        		        //nextScreen.putExtra("eventId", "" + eventId);

        		        startActivityForResult(nextScreen, 0);
        		    }
        		  }
        		)
        if(!overlays.isEmpty()){
            mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math.abs(maxLongitude - minLongitude));
            mapController.animateTo(new GeoPoint((maxLatitude + minLatitude)/2,(maxLongitude + minLongitude)/2 ));
        }
       // refreshMap.setVisibility(View.GONE);
    */}

    private final Runnable mOnMapZoomPan = new Runnable()
    {
        public void run()
        {
            refreshMap.setVisibility(View.VISIBLE);
        }
    };

   
    private String getDistance(Location l1, Location l2){
      float distance =  l1.distanceTo(l2);
      double distanceInMiles = distance * 0.000621371192;
      return String.format("%.2f", distanceInMiles) + " mi";
    }
    /**
     * Click on show list button
     * @param v
     */
    public void showList (View v){
        //System.gc();
        requestListView.setVisibility(View.VISIBLE);
        listHighlight.setVisibility(View.VISIBLE);
        requestGalleryView.setVisibility(View.GONE);
        galleryHighlight.setVisibility(View.INVISIBLE);
        requestMapLayout.setVisibility(View.GONE);
        refreshMap.setVisibility(View.GONE);
        mapHighlight.setVisibility(View.INVISIBLE);
        populateListView();

    }

    /**
     * Click on show gallery button
     * @param v
     */
    public void showGallery (View v){
        //System.gc();
        CustomProgressDialog dialog = new CustomProgressDialog(RequestListActivity.this, "");
            dialog.show();
            dialog.setCancelable(false);
        requestListView.setVisibility(View.GONE);
        listHighlight.setVisibility(View.INVISIBLE);
        requestGalleryView.setVisibility(View.VISIBLE);
        galleryHighlight.setVisibility(View.VISIBLE);
        requestMapLayout.setVisibility(View.GONE);
        refreshMap.setVisibility(View.GONE);
        mapHighlight.setVisibility(View.INVISIBLE);
        dialog.dismiss();
        populateGalleryView();

    }

    /**
     * Click on show map button
     * @param v
     */
    public void showMap (View v){
        //System.gc();
        requestListView.setVisibility(View.GONE);
        listHighlight.setVisibility(View.INVISIBLE);
        requestGalleryView.setVisibility(View.GONE);
        galleryHighlight.setVisibility(View.INVISIBLE);
        requestMapLayout.setVisibility(View.VISIBLE);
        refreshMap.setVisibility(View.VISIBLE);
        mapHighlight.setVisibility(View.VISIBLE);
        populateMapView();
    }

    public void refreshMap(View v){/*
        //GeoPoint center = requestMapView.moveCamera(arg0).getMapCenter();
        //double distance = (requestMapView.getLatitudeSpan()/1E6) *69;
       // LatLng currentPosition = requestMapView.getCameraPosition().target;
    	//currentPosition.
    	filters.put("latitude", String.valueOf(currentPosition.latitude));
        filters.put("longitude", String.valueOf(currentPosition.longitude));
        //filters.put("radius", String.valueOf(distance)); //what is radius and how do i calculate it
        filters.put("nearby", "1");
        getRequestList(filters, true);
    */}
    /**
     * Go back to previous view
     * @param v
     */
    public void back(View v){
        finish();
    }

    /**
     * Open filter request activity
     * @param v
     */
    public void filterRequest(View v){
        Intent intent = new Intent(RequestListActivity.this, RequestListFilterActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("filters", filters);
        intent.putExtras(b);
        startActivityForResult(intent, FILTER_ACTIVITY);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        switch(requestCode){
            case FILTER_ACTIVITY:
                if(resultCode == RESULT_CANCELED){
                    return;
                }
                Bundle extras = data.getExtras();
                filters = (HashMap<String, String>) extras.getSerializable("filters");
                getRequestList(filters, true);
                break;
            case REQUEST_DETAILS_ACTIVITY:
                Bundle requestExtras = data.getExtras();
                boolean updatedRequest = requestExtras.getBoolean("updatedRequest");
                if(updatedRequest){
                    getRequestList(filters, true);
                }
                break;
        }
    }

    /**
     * Open new request activity
     * @param v
     */
    public void newRequest(View v){
        Intent intent = new Intent(RequestListActivity.this, NewRequestActivity.class);
        startActivity(intent);
    }
    ///necessary abstract class for MapActivity
   
    private void doStartListenLocation()
    {
        Intent intent = new Intent(this, LocationListenerAgent.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationListenerAgent.LocationListenerBinder binder = (LocationListenerAgent.LocationListenerBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    @Override
    protected void onPause() {
        //System.gc();
        super.onPause();
    }
    @Override
    protected  void onRestart(){
        //System.gc();
        super.onRestart();
    }
    @Override
    protected  void onStart(){

        //System.gc();
        super.onStart();
        
        FlurryAgent.onStartSession(this, "2C3QVVZMX8Q5M6KF3458");
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        

    }

    @Override
    protected void onResume() {
        doStartListenLocation();
        super.onResume();
    }
    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        //System.gc();
        FlurryAgent.endTimedEvent("Nearby Requests");
        FlurryAgent.onEndSession(this);
        
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        //System.gc();
        super.onDestroy();
    }

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
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

		
	private void setUpMap(double lat, double lon) {/*
		LatLng passedLatLng = new LatLng(lat, lon);
		Location userLocation = new Location("passedLocation");
		userLocation.setLatitude(latitude);
		userLocation.setLongitude(longitude);
		userLocation.setAccuracy(100);
		mListener.onLocationChanged(userLocation);
		//requestMapView = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		requestMapView.setMyLocationEnabled(false);		
		requestMapView.setLocationSource(this);
		requestMapView.setOnCameraChangeListener(this);
		//requestMapView.addMarker(new MarkerOptions().position(passedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange_shadow)));
		requestMapView.moveCamera(CameraUpdateFactory.newLatLngZoom(passedLatLng, requestMapView.getMaxZoomLevel()-4));
		//requestMapView.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
		requestMapView.setOnInfoWindowClickListener(this);
	*/}
	
	
}