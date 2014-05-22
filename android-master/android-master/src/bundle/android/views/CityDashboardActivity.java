package bundle.android.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import bundle.android.PublicStuff;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.ImageListViewAdapter;
import bundle.android.adapters.WidgetViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.ImageDialogVO;
import bundle.android.model.vo.WidgetVO;
import bundle.android.service.LocationListenerAgent;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshRelativeLayout;
import com.wassabi.psmobile.R;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CityDashboardActivity extends Activity {
	private LocationListenerAgent mService;
	private boolean mBound = false;

	private ArrayList<WidgetVO> widgets;
	private WidgetViewAdapter widgetViewAdapter;
	private PublicStuffApplication app;
	private ViewPager widgetPager;
	private TextView cityName;
	private TextView cityTagline;
	private RelativeLayout headerLayoutView;
	private ImageView cityImage;
	private LinearLayout indicatorLayout;
	private LogOutReceiver logOutReceiver;
	public static int ICON_CONSTANT = 6;
	RelativeLayout mRelativeLayout;
	PullToRefreshRelativeLayout widgetView;
	boolean localeSwitch = false;
	CustomProgressDialog dialog;

	private ArrayList<WidgetVO> generateDefaultWidgets() {
		ArrayList<WidgetVO> widgets = new ArrayList<WidgetVO>();
		widgets.add(new WidgetVO(R.drawable.widget_new_request, getResources().getString(R.string.newRequest), 0));
		widgets.add(new WidgetVO(R.drawable.widget_map_pin, getResources().getString(R.string.nearbyRequest), 0));
		for(int i=0; i<app.getCityWidgets().size(); i++){
			widgets.add(app.getCityWidgets().get(i));
		}
		return widgets;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.package.ACTION_LOGOUT");
		logOutReceiver = new LogOutReceiver();
		registerReceiver(logOutReceiver, intentFilter);
		super.onCreate(savedInstanceState);
		app = (PublicStuffApplication)getApplicationContext();
		doStartListenLocation();
		setContentView(R.layout.dashboard);

		//determine screen size constant
		int screenSize = getResources().getConfiguration().screenLayout &
				Configuration.SCREENLAYOUT_SIZE_MASK;

		if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
			ICON_CONSTANT = 16;
		else if(screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
			ICON_CONSTANT = 16;
		else if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL)
			ICON_CONSTANT = 8;
		else if(screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL)
			ICON_CONSTANT = 6;

		//edge cases
		if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && getResources().getDisplayMetrics().density<=1.0)
			ICON_CONSTANT = 6;

		if(!app.getUserGCM().contentEquals(""))
		{
			//send to server with api_key
			setGCM();
		}
		
		
		initElements();

		FlurryAgent.logEvent("Dashboard");
		DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Dashboard");

		Utils.useLatoInView(this, this.findViewById(android.R.id.content));
	}
	private class LogOutReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("onReceive", "Logout in progress");
			//At this point you should start the login activity and finish this one
			finish();
		}
	}
	private void initElements(){
		widgets = generateDefaultWidgets();
		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final int pages = (widgets.size()/ICON_CONSTANT) + ((widgets.size()%ICON_CONSTANT==0)?0:1);
		cityName = (TextView) findViewById(R.id.cityName);
		cityTagline = (TextView) findViewById(R.id.cityTagline);
		headerLayoutView = (RelativeLayout) findViewById(R.id.headerLayout);
		cityImage = (ImageView) findViewById(R.id.cityIcon);
		headerLayoutView.setBackgroundColor(app.getNavColor());
		cityName.setText(app.getCityAppName());
		if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
			AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityImage);
			imageLoader.execute(new Void[0]);
		}
		else{
			cityImage.setImageResource(R.drawable.ic_dark_publicstuff_icon);
		}
		if(app.getCityTagline()!=null && !app.getCityTagline().equals("")){
			cityTagline.setText(app.getCityTagline());
		}
		else{
			cityTagline.setVisibility(View.GONE);
		}
		
		widgetView = (PullToRefreshRelativeLayout)this.findViewById(R.id.widgetView);
		widgetView.setOnRefreshListener(new OnRefreshListener<RelativeLayout>(){

			@Override
			public void onRefresh(PullToRefreshBase<RelativeLayout> refreshView) {
				// TODO Auto-generated method stub
				getWidgetsList(app.getCityClientId());
				
			}	
		});
		mRelativeLayout = widgetView.getRefreshableView();
		

		widgetPager = (ViewPager)this.findViewById(R.id.widgetPager);
		widgetViewAdapter = new WidgetViewAdapter(this, widgets,mLInflater);
		widgetPager.setAdapter(widgetViewAdapter);
		
		widgetPager.setOnTouchListener(new View.OnTouchListener() {

	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            v.getParent().requestDisallowInterceptTouchEvent(true);
	            return false;
	        }
	    });
		
		
		widgetPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				widgetPager.getParent().requestDisallowInterceptTouchEvent(true);
				if(pages>1){
					for(int j = 0; j<pages; j++){
						ImageView iv =  (ImageView)indicatorLayout.getChildAt(j);
						Drawable pager;
						if(j == i) 
							pager = CityDashboardActivity.this.getResources().getDrawable(R.drawable.ic_pagination_open);
						else  
							pager = CityDashboardActivity.this.getResources().getDrawable(R.drawable.ic_pagination_closed);
						Utils.addColorFilter(pager, app.getCityBaseColor());
						iv.setImageDrawable(pager);
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int i) {
			}
		});
		indicatorLayout = (LinearLayout)this.findViewById(R.id.widgetPagerIndicatorLayout);

		if(pages>1){
			indicatorLayout.setVisibility(View.VISIBLE);
			for(int i = 0; i<pages; i++){
				ImageView iv = new ImageView(this);
				iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				iv.setPadding(8,8, 8, 8);
				Drawable pager;
				if(widgetPager.getCurrentItem() == i) 
					pager = this.getResources().getDrawable(R.drawable.ic_pagination_open);
				else  
					pager = this.getResources().getDrawable(R.drawable.ic_pagination_closed);
				Utils.addColorFilter(pager, app.getCityBaseColor());
				iv.setImageDrawable(pager);
				indicatorLayout.addView(iv);
			}
		}
		else{
			indicatorLayout.setVisibility(View.GONE);
		}

	}
	
	public void changeCities(){
        app.setCityAbout("");
        app.setCityBanner("");
        app.setCityBaseColor("#232323");
        app.setCityClientId(0);
        app.setCityCompletedRequests(0);
        app.setCityIcon("");
        app.setCityLat(0.0);
        app.setCityLon(0.0);
        app.setCityName("");
        app.setCityAppName("");
        app.setCitySpaceId(0);
        app.setCitySubmittedRequests(0);
        app.setCityTagline("");
        app.setCityUrl("");
        app.setCityUserFollowing(false);
        ArrayList<WidgetVO> widgetVOs = app.getCityWidgets();
        widgetVOs.clear();
        app.setCityWidgets(widgetVOs);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        sendBroadcast(broadcastIntent);
        Intent mainActivityIntent = new Intent(CityDashboardActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivityIntent);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

	/**
	 *  Call when user click submit button
	 * @param v
	 */
	public void menuDialog(View v){
		ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
		ImageDialogVO cityInfo = new ImageDialogVO(R.drawable.holo_light2_action_about, String.format(CityDashboardActivity.this.getString(R.string.aboutapp), app.getCityAppName()), null, false);
		listToReturn.add(cityInfo);
		ImageDialogVO settings = new ImageDialogVO(R.drawable.holo_light2_action_settings, CityDashboardActivity.this.getString(R.string.mystuff), null, false);
		listToReturn.add(settings);
		ImageDialogVO publicstuff = new ImageDialogVO(R.drawable.ic_light_about_publicstuff, CityDashboardActivity.this.getString(R.string.aboutps), null, false);
		listToReturn.add(publicstuff);

		if(PublicStuff.IS_CLIENT_APP==false)
		{
			listToReturn.add(new ImageDialogVO(R.drawable.holo_light10_device_access_location_found, CityDashboardActivity.this.getString(R.string.changeCity), null, false));
			ImageDialogVO refreshWidgets = new ImageDialogVO(R.drawable.ic_light_following, CityDashboardActivity.this.getString(R.string.refreshps), null, false);
			listToReturn.add(refreshWidgets);
		}


		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(CityDashboardActivity.this.getString(R.string.more));
		dialog.show();

		final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToReturn, mLInflater);
		ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent;
				switch(position){
				case 0:
					//add code to open city details
					intent = new Intent(CityDashboardActivity.this, CityDetailsActivity.class);
					startActivity(intent);
					dialog.dismiss();
					break;
				case 1:
					if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")){
						intent = new Intent(CityDashboardActivity.this, UserDetailsActivity.class);
						startActivity(intent);
					}
					else{
						intent = new Intent(CityDashboardActivity.this, CreateAccountActivity.class);
						startActivity(intent);
						finish();
					}
					dialog.dismiss();
					break;
				case 2:
					intent = new Intent(CityDashboardActivity.this, AboutPublicStuffActivity.class);
					startActivity(intent);
					dialog.dismiss();
					break;
				case 3:
					//change cities
					changeCities();
					dialog.dismiss();
					break;
				case 4:
					//refresh widgets
					getWidgetsList(app.getCityClientId());
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
	}

	private void getWidgetsList(int id){
		//final CustomProgressDialog dialog = new CustomProgressDialog(CityDashboardActivity.this, CityDashboardActivity.this.getString(R.string.cityData));
		//if(PublicStuff.IS_CLIENT_APP==false)dialog.show();
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("widgets_list"), Utils.isNetworkAvailable(CityDashboardActivity.this)){
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
			}
			@Override
			protected void onPostExecute(String result)
			{
				if(localeSwitch==true)
				{
					if(dialog!=null && dialog.isShowing())
						dialog.dismiss();
					
					localeSwitch = false;
				}
				
				if(result.length()>0){
					try {
						JSONParser parser = new JSONParser(result);
						if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
							populateWidgetsList(parser.getResponse().getJSONArray("widgets"));
							indicatorLayout.removeAllViews();
							initElements();
							Toast.makeText(CityDashboardActivity.this, CityDashboardActivity.this.getString(R.string.widgetsUpdated), Toast.LENGTH_SHORT).show();

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Utils.noConnection(CityDashboardActivity.this);
				}
				widgetView.onRefreshComplete();
			}
		};
		if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.addParameter("client_id", String.valueOf(id));
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		clientGet.execute("");
	}
	
	
	private void setGCM(){
		//final CustomProgressDialog dialog = new CustomProgressDialog(CityDashboardActivity.this, CityDashboardActivity.this.getString(R.string.cityData));
		//if(PublicStuff.IS_CLIENT_APP==false)dialog.show();
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("register_device"), Utils.isNetworkAvailable(CityDashboardActivity.this)){
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
			}
			@Override
			protected void onPostExecute(String result)
			{
				if(result.length()>0){
					try {
						JSONParser parser = new JSONParser(result);
						if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
							

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					//no connection, add to queue for fetching later
				}
				
			}
		};
		if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))
		{
			clientGet.addParameter("api_key", app.getUserApiKey());
			clientGet.addParameter("push_system", "android");
			clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
			clientGet.addParameter("device_token", app.getUserGCM());
			clientGet.addParameter("bundle_id", app.getPackageName());
			clientGet.addParameter("device_uid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
			clientGet.execute("");
		}
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


	public void newRequestActivity(View v){

		Intent intent = new Intent(CityDashboardActivity.this, NewRequestActivity.class);
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
		doStartListenLocation();
		FlurryAgent.onStartSession(CityDashboardActivity.this, "2C3QVVZMX8Q5M6KF3458");
		//System.gc();
		super.onStart();
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
			
			Intent mainActivityIntent = new Intent(CityDashboardActivity.this, MainActivity.class);
	        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(mainActivityIntent);
	        finish();
		}
	}
	@Override
	protected void onStop() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		//System.gc();
		FlurryAgent.endTimedEvent(this.getSharedPreferences("publicStuffPrefs", MODE_PRIVATE).getString("currentEvent", ""));
		FlurryAgent.onEndSession(CityDashboardActivity.this);
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		//System.gc();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		stopService(new Intent(this, LocationListenerAgent.class));
		unregisterReceiver(logOutReceiver);
		super.onDestroy();
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}