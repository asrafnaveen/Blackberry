package bundle.android.views;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import bundle.android.PublicStuff;
import bundle.android.PublicStuffApplication;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.LocationVO;
import bundle.android.model.vo.WidgetVO;
import bundle.android.service.LocationListenerAgent;
import bundle.android.utils.CurrentLocation;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.wassabi.psmobile.R;
import com.wassabi.psmobile.GCMCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class MainActivity extends Activity {
    private LocationListenerAgent mService;
    private boolean mBound = false;
    private Location currLocation;
    private String cityButtonText = "Finding location...";
    private Button cityButton;
    private LinearLayout selectCityLayout;
    private RelativeLayout splash;
    private LinearLayout infoLayout;
    private DataStore store;
    private PublicStuffApplication app;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new DataStore(this);
        app = (PublicStuffApplication)getApplicationContext();
        doStartListenLocation();
        setContentView(R.layout.main);
        
        FlurryAgent.logEvent("App Started");
		store.saveToPrefs("currentEvent", "App Started");

        GCMCalls.registrationCall(app);
        
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        loadStoredData();
        if(app.getCitySpaceId()>0){
        	splash.setBackgroundResource(R.drawable.splash);
        	infoLayout.setVisibility(View.INVISIBLE);
            getCityData(app.getCitySpaceId());
        }
        else{
            selectCityLayout.setVisibility(View.VISIBLE);
        }

    }
    private void initElements(){
        cityButton = (Button) findViewById(R.id.currCity);
        cityButton.setText(MainActivity.this.getString(R.string.findingLocation));
        cityButton.setEnabled(false);
        selectCityLayout = (LinearLayout)findViewById(R.id.selectCityLayout);
        selectCityLayout.setVisibility(View.GONE);
        splash = (RelativeLayout)findViewById(R.id.splashbg);
        infoLayout = (LinearLayout)findViewById(R.id.infoLayout);
    }
    private void loadStoredData(){

        try
        {
            PublicStuff.VERSION = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e){}


        Calendar startCal = Calendar.getInstance();
        startCal.set(2000,0, 1, 0, 0);

        app.setUserLastLogin(startCal.getTime().getTime());
        store.saveToPrefs("USER_LAST_LOGIN", new Date().getTime());
    }
    private void populateLocationData(){
        currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);
        cityButtonText = MainActivity.this.getString(R.string.unableCity);
        cityButton.setEnabled(false);
        if(currLocation!=null){
            LocationVO locationVO = new LocationVO(currLocation, this);
            if(locationVO.getCityName()!=null)
                cityButtonText = locationVO.getCityName() + ", " + locationVO.getStateAbbrev();
            cityButton.setEnabled(true);
        }
        cityButton.setText(cityButtonText);
    }
     private void getCityList(){
         final CustomProgressDialog dialog = new CustomProgressDialog(MainActivity.this, MainActivity.this.getString(R.string.cityData));
         if(PublicStuff.IS_CLIENT_APP==false)
         {
        	 if(!dialog.isShowing())
        		 dialog.show();
         }
         LocationVO locationVO = new LocationVO(currLocation, this);
         HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("cities_list"), Utils.isNetworkAvailable(MainActivity.this)){
             @Override
             protected void onPreExecute()
             {
                 super.onPreExecute();
             }
             @Override
             protected void onPostExecute(String result)
             {
                 if(!MainActivity.this.isFinishing() && dialog != null && dialog.isShowing()){
                 dialog.dismiss();
                 }
                if(result.length()>0){
                 try {
                     JSONParser parser = new JSONParser(result);
                     if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                        JSONArray citiesArray = parser.getResponse().getJSONArray("cities");
                        JSONObject city = citiesArray.getJSONObject(0).getJSONObject("city");
                        getCityData(city.getInt("id"));
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                } else Utils.noConnection(MainActivity.this);
             }
         };
         if(locationVO.getZipcode() !=null) clientGet.addParameter("zipcode", locationVO.getZipcode());
         if(locationVO.getCityName() !=null) clientGet.addParameter("city", locationVO.getCityName());
         if(locationVO.getStateAbbrev() !=null) clientGet.addParameter("state", locationVO.getStateAbbrev());
         clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
         clientGet.execute("");
     }
    private void getCityData(int id){
        final CustomProgressDialog dialog = new CustomProgressDialog(MainActivity.this, MainActivity.this.getString(R.string.cityData));
        if(PublicStuff.IS_CLIENT_APP==false)dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("city_view"), Utils.isNetworkAvailable(MainActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result)
            {
                if(!MainActivity.this.isFinishing() && dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                if(result.length()>0){
                try {
                    JSONParser parser = new JSONParser(result);
                    if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                       populateCityData(parser.getResponse());
                        //if the city is a client, get widgets
                       if(parser.getResponse().getInt("client_id")!=0){
                          getWidgetsList(parser.getResponse().getInt("client_id"));
                       }
                       else{
                        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                            Intent intent = new Intent(MainActivity.this, CityDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                       }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                    if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                        Intent intent = new Intent(MainActivity.this, CityDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("space_id", String.valueOf(id));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }
    private void populateCityData(JSONObject city) throws JSONException {
        app.setCitySpaceId(city.getInt("space_id"));
        app.setCityClientId(city.getInt("client_id"));
        app.setCityName(city.getString("name")+", "+city.getString("state"));
        app.setCityAppName((city.has("app_name") && city.get("app_name")!=null && !city.getString("app_name").equals(""))?city.getString("app_name"):app.getCityName());
        if(city.has("color") && city.get("color")!=null && !city.getString("color").equals(""))app.setCityBaseColor("#" + city.getString("color"));
        if(city.has("user_is_following")) app.setCityUserFollowing((city.getInt("user_is_following") == 1));
        if(city.has("logo") && city.get("logo")!=null && !city.getString("logo").equals(""))app.setCityIcon(city.getString("logo"));
        if(city.has("about_text") && city.get("about_text")!=null && !city.getString("about_text").equals(""))app.setCityAbout(city.getString("about_text"));
        if(city.has("skyline") && city.get("skyline")!=null && !city.getString("skyline").equals("")) app.setCityBanner(city.getString("skyline"));
        app.setCityLat(city.getDouble("latitude"));
        app.setCityLon(city.getDouble("longitude"));
        if(city.has("tagline") && city.get("tagline")!=null && !city.getString("tagline").equals(""))app.setCityTagline(city.getString("tagline"));
        if(city.has("website") && city.get("website")!=null && !city.getString("website").equals(""))app.setCityUrl(city.getString("website"));
        app.setCitySubmittedRequests(city.getInt("submitted_requests"));
        app.setCityCompletedRequests(city.getInt("completed_requests"));
    }

    private void getWidgetsList(int id){
        final CustomProgressDialog dialog = new CustomProgressDialog(MainActivity.this, MainActivity.this.getString(R.string.cityData));
        if(PublicStuff.IS_CLIENT_APP==false)dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("widgets_list"), Utils.isNetworkAvailable(MainActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result)
            {
                if(!MainActivity.this.isFinishing() && dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                if(result.length()>0){
                    try {
                        JSONParser parser = new JSONParser(result);
                        if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                            populateWidgetsList(parser.getResponse().getJSONArray("widgets"));
                            if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                                Intent intent = new Intent(MainActivity.this, CityDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.noConnection(MainActivity.this);
                    if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                        Intent intent = new Intent(MainActivity.this, CityDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("client_id", String.valueOf(id));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }
    private void populateWidgetsList(JSONArray widgets) {
        ArrayList<WidgetVO> widgetVOs = new ArrayList<WidgetVO>();
        for(int i=0; i<widgets.length(); i++){
            try {
                JSONObject widget = widgets.getJSONObject(i).getJSONObject("widget");
                String iconName = widget.getString("logo");
                iconName = "drawable/widget_" + iconName.replace("-", "_");
                int icon = getResources().getIdentifier(iconName, null, getPackageName());
                WidgetVO widgetVO = new WidgetVO(icon, widget.getString("name"), widget.getInt("id"));
                widgetVO.setUrl(widget.getString("url"));
                if(widget.has("description") && widget.get("description")!=null) widgetVO.setDescription(widget.getString("description"));
                widgetVOs.add(widgetVO);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        app.setCityWidgets(widgetVOs);
    }
    /**
     *  Call when user chooses to use current city
     * @param v
     */
    public void selectCurrCity(View v){
        getCityList();
    }

    /**
     *  Call when user chooses to search for different city
     * @param v
     */
    public void searchCity(View v){
        Intent intent = new Intent(MainActivity.this, CitiesListActivity.class);
        startActivityForResult(intent, 0);
    }
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
    protected  void onStart(){
    	FlurryAgent.onStartSession(MainActivity.this, "2C3QVVZMX8Q5M6KF3458");
        //System.gc();
        super.onStart();
    }
    @Override
    protected void onPause() {
       //System.gc();
        super.onPause();
    }
    @Override
    protected void onResume() {
        doStartListenLocation();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                populateLocationData();
            }
        }, 2250);
        super.onResume();
    }
    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        FlurryAgent.onEndSession(MainActivity.this);
       //System.gc();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
       //System.gc();
        super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
//      setContentView(R.layout.main);
//      
//      if(app.getCitySpaceId()>0){
//      	splash.setBackgroundResource(R.drawable.splash);
//      	infoLayout.setVisibility(View.INVISIBLE);
//      }
//      else{
//          selectCityLayout.setVisibility(View.VISIBLE);
//      }
    }    
    
}