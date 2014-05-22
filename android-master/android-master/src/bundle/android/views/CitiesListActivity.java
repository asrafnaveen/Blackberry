package bundle.android.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.CitiesListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.CityVO;
import bundle.android.model.vo.LocationVO;
import bundle.android.model.vo.WidgetVO;
import bundle.android.service.LocationListenerAgent;
import bundle.android.utils.CurrentLocation;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class CitiesListActivity extends PsActivity {
    private PublicStuffApplication app;
    private ListView cityListView;
    private EditText editCity;
    private CitiesListViewAdapter citiesListViewAdapter;
    private final ArrayList<CityVO> citiesList = new ArrayList<CityVO>();
    private int selected = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citieslist);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        
        FlurryAgent.logEvent("Cities List");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Cities List");
        
        getCitiesList(initialSearchTerms());
    }

    /**
     * set up initial elements and listeners
     */
    private void initElements(){
       cityListView = (ListView)this.findViewById(R.id.cityListView);
       cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                selected = position;
                for(int i=0; i<citiesList.size(); i++){
                    CityVO cityVO = citiesList.get(i);
                    if(i==position)cityVO.setSelected(true);
                    else cityVO.setSelected(false);
                    citiesList.set(i, cityVO);
                }
                populateCitiesListView();
            }
        });
       editCity = (EditText)this.findViewById(R.id.editCity);
       editCity.setOnEditorActionListener(new EditTextListener());
    }

    /**
     * Initialize the search terms based on current location
     * @return
     */
    private HashMap<String, String> initialSearchTerms(){
        DataStore store = new DataStore(this);
        Location pastLocation = store.getCurrLocation();
        HashMap<String, String> searchTerms = new HashMap<String, String>();
        Location currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);

        if(!CurrentLocation.isBetterLocation(currLocation, pastLocation)){
            currLocation = pastLocation;
        }
        if(currLocation!=null && currLocation.getLatitude()!=0.0){
            LocationVO currentLocationVO = new LocationVO(currLocation, this);

            searchTerms.put("lat", String.valueOf(currentLocationVO.getLatitude()));
            searchTerms.put("lon", String.valueOf(currentLocationVO.getLongitude()));
        }
        return searchTerms;
    }

    /**
     * Retrieve List of cities based on search terms
     * @param searchTerms
     */
    private void getCitiesList(HashMap<String, String> searchTerms){
        final CustomProgressDialog dialog = new CustomProgressDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.cityList));
        dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("cities_list"), Utils.isNetworkAvailable(CitiesListActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result)
            {
                if(!CitiesListActivity.this.isFinishing() && dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
                if(result.length()>0){
                try {
                    JSONParser parser = new JSONParser(result);
                    if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                        JSONArray citiesArray = parser.getResponse().getJSONArray("cities");
                        populateCitiesList(citiesArray);
                        populateCitiesListView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                } else Utils.noConnection(CitiesListActivity.this);
            }
        };
        Set<Map.Entry<String, String>> set = searchTerms.entrySet();

        for (Map.Entry<String, String> entry : set) {
            clientGet.addParameter(entry.getKey(), entry.getValue());
        }
        clientGet.addParameter("locale", this.getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }

    private void populateCitiesList(JSONArray cities){
        citiesList.clear();
        if(cities.length()==0){
            final CustomAlertDialog dialog = new CustomAlertDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.locationFail), CitiesListActivity.this.getString(R.string.locationFailVerbose), CitiesListActivity.this.getString(R.string.OkButton), null);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int id = v.getId();
                    switch(id){
                        case R.id.alertConfirm:
                            Intent intent = new Intent(CitiesListActivity.this, CityDashboardActivity.class);
                            startActivityForResult(intent, 0);
                            dialog.dismiss();
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        else{
            for(int i=0; i<cities.length(); i++){
                try {
                    JSONObject request = cities.getJSONObject(i).getJSONObject("city");
                    CityVO cityVO = new CityVO(request.getString("name"), request.getString("state"), request.getInt("id"), (request.getInt("client_id")!=0), false);
                    citiesList.add(cityVO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void populateCitiesListView(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        citiesListViewAdapter = new CitiesListViewAdapter(CitiesListActivity.this, citiesList, mLInflater);
        cityListView.setAdapter(citiesListViewAdapter);
        cityListView.invalidateViews();
        ((BaseAdapter)((ListView)findViewById(R.id.cityListView)).getAdapter()).notifyDataSetChanged();
    }

    private void getCityData(int id){
        final CustomProgressDialog dialog = new CustomProgressDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.cityData));
        dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("city_view"), Utils.isNetworkAvailable(CitiesListActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
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
                            populateCityData(parser.getResponse());
                            //if the city is a client, get widgets
                            if(parser.getResponse().getInt("client_id")!=0){
                                getWidgetsList(parser.getResponse().getInt("client_id"));
                            }
                            else{
                                if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                                    Intent intent = new Intent(CitiesListActivity.this, CityDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Intent intent = new Intent(CitiesListActivity.this, CreateAccountActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else Utils.noConnection(CitiesListActivity.this);
            }
        };
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("space_id", String.valueOf(id));
        clientGet.addParameter("locale", this.getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }

    private void populateCityData(JSONObject city) throws JSONException {
        app.setCitySpaceId(city.getInt("space_id"));
        app.setCityClientId(city.getInt("client_id"));
        app.setCityName(city.getString("name")+", "+city.getString("state"));
        app.setCityAppName((city.has("app_name") && city.get("app_name")!=null && !city.getString("app_name").equals(""))?city.getString("app_name"):app.getCityName());
        if(city.has("color") && city.get("color")!=null && !city.getString("color").equals(""))app.setCityBaseColor("#" + city.getString("color").trim());
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
        final CustomProgressDialog dialog = new CustomProgressDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.cityData));
        dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("widgets_list"), Utils.isNetworkAvailable(CitiesListActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
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
                            populateWidgetsList(parser.getResponse().getJSONArray("widgets"));
                            if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                                Intent intent = new Intent(CitiesListActivity.this, CityDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Intent intent = new Intent(CitiesListActivity.this, CreateAccountActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.noConnection(CitiesListActivity.this);
                    if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) {
                        Intent intent = new Intent(CitiesListActivity.this, CityDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(CitiesListActivity.this, CreateAccountActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("client_id", String.valueOf(id));
        clientGet.addParameter("locale", this.getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }
    private void populateWidgetsList(JSONArray widgets){
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
     * When user is done editing text of search box
     */
    private class EditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event !=null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                HashMap<String, String> searchTerms = new HashMap<String, String>();
                String findLocation = editCity.getText().toString();
                String locationComponents[] = findLocation.split(", ");
                searchTerms.put("city", locationComponents[0]);
                if(locationComponents.length >1)
                    searchTerms.put("state", locationComponents[1]);
                getCitiesList(searchTerms);
                return true;
            }
            return false;
        }
    }

    /**
     * Click on find current location button
     * @param v
     */
    public void findLocation(View v){
        DataStore store = new DataStore(this);
        Location pastLocation = store.getCurrLocation();

        Location currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);

        if(!CurrentLocation.isBetterLocation(currLocation, pastLocation)){
            currLocation = pastLocation;
        }
        if(currLocation!=null){
            LocationVO currentLocationVO = new LocationVO(currLocation, this);
            if(currentLocationVO.getCityName() != null && currentLocationVO.getStateAbbrev() !=null){
                editCity.setText(currentLocationVO.getCityName() + ", " + currentLocationVO.getStateAbbrev());
                HashMap<String, String> searchTerms = new HashMap<String, String>();
                searchTerms.put("city", currentLocationVO.getCityName());
                searchTerms.put("state", currentLocationVO.getStateAbbrev());
                getCitiesList(searchTerms);
            }
            else{
                final CustomAlertDialog dialog = new CustomAlertDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.locationFail), CitiesListActivity.this.getString(R.string.findCity), CitiesListActivity.this.getString(R.string.OkButton), null);
                View.OnClickListener listener = new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int id = v.getId();
                        switch(id){
                            case R.id.alertConfirm:
                                dialog.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                };
                dialog.setListener(listener);
                dialog.show();
            }
        }
        else{
        	final CustomAlertDialog dialog = new CustomAlertDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.locationFail), CitiesListActivity.this.getString(R.string.findCity), CitiesListActivity.this.getString(R.string.OkButton), null);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int id = v.getId();
                    switch(id){
                        case R.id.alertConfirm:
                            dialog.dismiss();
                            break;
                        default:
                            break;
                    }
                }
            };
            dialog.setListener(listener);
            dialog.show();
        }
    }
    /**
     *
     * @param v
     */
    public void back(View v){
        finish();
    }
    /**
     * Click DONE button
     * @param v
     */
    public void clickSubmit(View v){
        if(selected>-1 && citiesList.size()> selected){
            CityVO cityVO = citiesList.get(selected);
            getCityData(cityVO.getId());
        }
        else{
            final CustomAlertDialog dialog = new CustomAlertDialog(CitiesListActivity.this, CitiesListActivity.this.getString(R.string.selectCityTime), CitiesListActivity.this.getString(R.string.selectCityVerbose), CitiesListActivity.this.getString(R.string.OkButton), null);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int id = v.getId();
                    switch(id){
                        case R.id.alertConfirm:
                            dialog.dismiss();
                            break;
                        default:
                            break;
                    }
                }
            };
            dialog.setListener(listener);
            dialog.show();
        }
    }
    
    @Override
	protected void onPause() {
		 //save this to prefs
		app.setLocale(getResources().getConfiguration().locale.getLanguage()); //will return default of '' if not set
		
		//System.gc();
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!app.getLocale().contentEquals(getResources().getConfiguration().locale.getLanguage()) && !app.getLocale().contentEquals(""))
		{
			app.setLocale(getResources().getConfiguration().locale.getLanguage());
			//cityListView.
			initElements();
	        
	        getCitiesList(initialSearchTerms());
		}
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
