package bundle.android.views;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import bundle.android.PublicStuff;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.ImageListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.model.vo.*;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class NewRequestActivity extends Activity{
	private LocationListenerAgent mService;
	private boolean mBound = false;
	private PublicStuffApplication app;

	private TextView requestTypeText;
	private EditText titleEditText;
	private EditText descriptionEditText;
	private ImageButton privacyButton;
	private ImageButton attachmentButton;
	private Button locationButton;

	private boolean titleChanged = false;
	private final ArrayList<RequestTypeVO> requestTypes = new ArrayList<RequestTypeVO>();
	private final ArrayList<CategoryTypeVO> categoryTypes = new ArrayList<CategoryTypeVO>();
	private RequestTypeVO selectedRequestType = null;
	private int spaceId;
	private int clientId;
	private boolean isPrivate = false;
	private boolean hasImage = false;
	private boolean hasVideo = false;
	private boolean hasAudio = false;
	private String title;
	private String description;
	private String zipcode;
	private String address;
	private double latitude;
	private double longitude;
	private String city;
	private String state;
	private String pathToImage;
	private Bitmap thumbnailBmp;

	private Location currLocation;
	private String locationString = "";
	private LocationVO initialLocation;
	private boolean didFindLocation = false;
	
	HashMap<Integer, ArrayList<CustomFieldVO>> idCustomFieldVO;
	
	private static final int ACTIVITY_SELECT_PHOTO = 0;
	private static final int ACTIVITY_SELECT_VIDEO= 1;
	private static final int ACTIVITY_SELECT_AUDIO= 2;
	
	private static final int ACTIVITY_TAKE_PHOTO = 3;
	private static final int ACTIVITY_TAKE_VIDEO= 4;
	private static final int ACTIVITY_TAKE_AUDIO= 5;
	
	private static final int ACTIVITY_EDIT_LOCATION = 6;
	private static final int ACTIVITY_CUSTOM_FIELDS = 7;
	private static final int ACTIVITY_REQUEST_TYPES = 8;
	
	private static final String TAG = "NewRequestActivity";

	private ArrayList<CustomFieldVO> draftCustomFields;
	private int requestDraft = -1;
	
	HashMap <String, ArrayList<RequestTypeVO>> categoryMap = new HashMap<String, ArrayList<RequestTypeVO>>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newrequest);
		app = (PublicStuffApplication)getApplicationContext();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		Utils.useLatoInView(this, this.findViewById(android.R.id.content));
		initElements();
		
		FlurryAgent.logEvent("New Request");
		
		DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "New Request");
		 idCustomFieldVO = new HashMap<Integer, ArrayList<CustomFieldVO>>();
		
		if(savedInstanceState == null){
			Bundle b;
			b = getIntent().getExtras();
			if(b!=null && b.containsKey("requestDraft")) requestDraft = b.getInt("requestDraft");
			if(requestDraft>-1){
				RequestDraftVO requestDraftVO = app.getUserDrafts().get(requestDraft);
				populateRequestFromDraft(requestDraftVO);
			}
			else{
				final CustomProgressDialog dialog = new CustomProgressDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.findingLocation));
				dialog.show();
				initialLocation = new LocationVO(app.getCityLat(), app.getCityLon(), this);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						dialog.dismiss();
						parseGeoLocationData();

					}
				}, 1000);

			}
		}

	}

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
		privacyButton = (ImageButton)this.findViewById(R.id.requestPrivacy);
		privacyButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clickPrivacy();
			}
			
		});
		attachmentButton = (ImageButton)this.findViewById(R.id.requestAttachment);
		locationButton = (Button)this.findViewById(R.id.requestLocation);
		descriptionEditText = (EditText)this.findViewById(R.id.txtEditDescription);
		titleEditText = (EditText)this.findViewById(R.id.txtEditTitle);
		//set up edittext listener
		titleEditText.addTextChangedListener(new EditTextListener());
		requestTypeText = (TextView)this.findViewById(R.id.txtRequestType);
	}

	private void parseGeoLocationData(){
		captureGeoLocationData();
		if(initialLocation.getCityName()!=null && didFindLocation){
			setLocationData(initialLocation);
			getCityData(initialLocation);
		}
		else{
			final CustomAlertDialog noLocationDialog = new CustomAlertDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.locationFail), NewRequestActivity.this.getString(R.string.locationFailGPS), NewRequestActivity.this.getString(R.string.chooseLocation), NewRequestActivity.this.getString(R.string.tryAgain));

			View.OnClickListener listener = new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int id = v.getId();
					switch(id){
					case R.id.alertConfirm:
						latitude = app.getCityLat();
						longitude = app.getCityLon();
						noLocationDialog.dismiss();
						clickLocation(v);
						break;
					case R.id.alertCancel:
						noLocationDialog.dismiss();
						parseGeoLocationData();
						break;
					default:
						break;
					}
				}
			};
			noLocationDialog.setListener(listener);
			noLocationDialog.show();
		}
	}
	/**
	 * capturing geo location data for debugging.
	 * displays data for gps and network provided location
	 * all in variable locationString that can be sent in
	 * request submission call
	 */
	private void captureGeoLocationData(){
		
		currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);

		if(currLocation!=null){
			didFindLocation = true;
			initialLocation = new LocationVO(currLocation, this);
		}
		else{
			didFindLocation = false;
		}
		/***for geo location debugging***/
		Location gLoc=LocationListenerAgent.lmgrGps.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location nLoc=LocationListenerAgent.lmgrNet.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if(null !=gLoc)
		{
			locationString += "Geo Lat: " +gLoc.getLatitude()+"\n";
			locationString += "Geo Lon: " +gLoc.getLongitude()+"\n";
			long timeBefore=System.currentTimeMillis()-gLoc.getTime();
			locationString += "Geo Time Before: " +timeBefore+"\n";

			if(gLoc.hasAccuracy())
				locationString += "Geo Accuracy: " +gLoc.getAccuracy()+"\n";
			else
				locationString += "Geo Accuracy: N_A \n";
		}else{
			locationString += "Geo Lon: N_A \n";
			locationString += "Geo Time Before: N_A \n";
			locationString += "Geo Accuracy: N_A \n";
			locationString += "Geo Time Before: N_A \n";
		}

		if(null !=nLoc)
		{
			locationString += "Network Lat: " +nLoc.getLatitude()+"\n";
			locationString += "Network Lon: " +nLoc.getLongitude()+"\n";
			long timeBefore=System.currentTimeMillis()-nLoc.getTime();
			locationString += "Network Time Before: " +timeBefore+"\n";

			if(nLoc.hasAccuracy())
				locationString += "Network Accuracy: " +nLoc.getAccuracy()+"\n";
			else
				locationString += "Network Accuracy: N_A \n";
		}else{
			locationString += "Network Lon: N_A \n";
			locationString += "Network Time Before: N_A \n";
			locationString += "Network Accuracy: N_A \n";
			locationString += "Network Time Before: N_A \n";
		}
		if(null != currLocation){
			locationString += "Provider Used : " + currLocation.getProvider();
		}
		/***end for geo location debugging***/
	}

	private void setLocationData(LocationVO locationVO){

		zipcode = locationVO.getZipcode();
		latitude = locationVO.getLatitude();
		longitude = locationVO.getLongitude();
		address = locationVO.getAddress();
		state = locationVO.getStateAbbrev();
		city = locationVO.getCityName();
		locationButton.setText(address + ", " + city + ", " + state + " " + zipcode);
	}
	private void getCityData(LocationVO locationVO){
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("cities_list"), Utils.isNetworkAvailable(NewRequestActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.cityData));
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
							JSONArray citiesArray = parser.getResponse().getJSONArray("cities");
							JSONObject city = citiesArray.getJSONObject(0).getJSONObject("city");
							if(city.has("id") && city.get("id")!=null){
								spaceId = city.getInt("id");
								clientId = city.getInt("client_id");
								getRequestTypes(city.getInt("client_id"), false);
							}
						}
						else{
							Utils.noConnection(NewRequestActivity.this);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

					finally{
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				} else {
					Utils.noConnection(NewRequestActivity.this);
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
				}

			}
		};
		if(locationVO.getZipcode()!=null) clientGet.addParameter("zipcode", locationVO.getZipcode());
		if(locationVO.getCityName()!=null) clientGet.addParameter("city", locationVO.getCityName());
		if(locationVO.getStateAbbrev()!=null) clientGet.addParameter("state", locationVO.getStateAbbrev());
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		clientGet.execute("");
	}

	private void getRequestTypes(int id, boolean draft){
		final boolean fromDraft = draft;
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("requesttypes_list"), Utils.isNetworkAvailable(NewRequestActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.requestTypes));
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
							requestTypes.clear();
							JSONArray requestTypesArray = parser.getResponse().getJSONArray("request_types");
							
							populateRequestTypesList(requestTypesArray);
							
							JSONArray categoriesTypesArray = parser.getResponse().getJSONArray("categories");
							
							populateCategoryRequestTypesList(categoriesTypesArray);
							
							///reset title and request type if necessary
							if(!titleChanged && !titleEditText.getText().toString().equals("") && !fromDraft){
								titleEditText.setText("");
							}
							if(!requestTypeText.equals(getString(R.string.select_request_type_title)) && !fromDraft){
								requestTypeText.setText(R.string.select_request_type_title);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					finally{
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss(); //this is problem sometimes
						}
					}
				}else {
					Utils.noConnection(NewRequestActivity.this);
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
				}
			}
		};
		clientGet.addParameter("client_id", String.valueOf(id));
		if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.addParameter("lat", String.valueOf(latitude));
		clientGet.addParameter("lon", String.valueOf(longitude));
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());

		clientGet.execute("");

	}

	private void populateRequestTypesList(JSONArray requestTypes) throws JSONException{
		for(int i=0; i<requestTypes.length(); i++){
			JSONObject request_type= requestTypes.getJSONObject(i).getJSONObject("request_type");
			RequestTypeVO requestTypeVO = new RequestTypeVO(request_type.getInt("id"), 
															request_type.getString("name"), 
															(request_type.getInt("has_custom_fields")==1)); //==1? means default value if not set?
			requestTypeVO.setSelected(false);
			if(request_type.has("description") && request_type.get("description")!=null){ 
				requestTypeVO.setDescription(request_type.getString("description"));
			}
			
			if(request_type.has("force_private"))
				requestTypeVO.setForcePrivate(request_type.getString("force_private"));
			else
				requestTypeVO.setForcePrivate("0");
			
			if(request_type.has("category_id"))
				requestTypeVO.setCategoryId(request_type.getString("category_id"));
				
			if(request_type.has("disable_title"))
				requestTypeVO.setDisableTitle(request_type.getString("disable_title"));
			else
				requestTypeVO.setDisableTitle("0");			
			
			if(request_type.has("disable_description"))
				requestTypeVO.setDisableDescription(request_type.getString("disable_description"));
			else
				requestTypeVO.setDisableDescription("0");
			
				
			requestTypeVO.setIsCategory(false);
			
			if(request_type.has("custom_fields"))
			{
				JSONArray customFieldsJsonArray = request_type.getJSONArray("custom_fields");
				
				populateCustomFields(request_type.getInt("id"), customFieldsJsonArray);
                //populateCustomFieldsList();
				
				
			}
			
			/*
			 * if request_type does not have a category id, ie. category id = 0, then add it to the general list of request types
			 * 
			 * else add the request type to its category
			 */
			
			if(!request_type.has("category_id"))
				this.requestTypes.add(requestTypeVO);
			else
			{
				/*
				 * adds each request to an arraylist tied to its category id. so category 1 could have arraylist of request types, category 2 could have arraylist as well
				 */
				
				if(!this.categoryMap.containsKey(request_type.getString("category_id")))
				{
					//create arraylist
					ArrayList<RequestTypeVO> requestList = new ArrayList<RequestTypeVO>();
					
					/*RequestTypeVO mRequestType = new RequestTypeVO(requestTypeVO.getId(), requestTypeVO.getName(), requestTypeVO.getHasCustomField());
					mRequestType.getDisableDescription();
					mRequestType.getDisableTitle();
					mRequestType.getForcePrivate();
					mRequestType.getDescription();
					
					
					requestList.add(mRequestType);*/
					
					requestList.add(requestTypeVO);
					
					this.categoryMap.put(request_type.getString("category_id"), requestList);
				}
				else{
					ArrayList <RequestTypeVO> requestList = this.categoryMap.get(request_type.getString("category_id"));
					/*RequestTypeVO mRequestType = new RequestTypeVO(requestTypeVO.getId(), requestTypeVO.getName(), requestTypeVO.getHasCustomField());
					mRequestType.getDisableDescription();
					mRequestType.getDisableTitle();
					mRequestType.getForcePrivate();
					mRequestType.getDescription();
					
					
					requestList.add(mRequestType);*/
					requestList.add(requestTypeVO);
					this.categoryMap.put(request_type.getString("category_id"), requestList); //replaces existing arrayList at that category_id with longer arrayList
				}
			}//end conditions
		}//end for loop
	}
	
	private void populateCustomFields(int id, JSONArray customFields) throws JSONException{
        for(int i = 0; i<customFields.length(); i++){
            JSONObject customField = customFields.getJSONObject(i).getJSONObject("custom_field");
            
            ArrayList<CustomFieldOptionsVO> mOptionsArray = new ArrayList<CustomFieldOptionsVO>();
            
            //create JSONArray for options into ArrayList<CustomOptions> here;
            if(customField.has("options")){
            	if(customField.getJSONArray("options").length()>0){

            		for(int j = 0; j < customField.getJSONArray("options").length(); j++)
            		{
            			CustomFieldOptionsVO mOptions = new CustomFieldOptionsVO(customField.getInt("id"),customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getInt("id"), 
            					customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getString("name"),
            					customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getString("description"),
            					false);

            			mOptionsArray.add(mOptions);
            		}
            	}
            }//has options
            
            CustomFieldVO customFieldVO = new CustomFieldVO(customField.getInt("id"),
                                                               customField.getString("name"),
                                                               customField.getString("type"),
                                                               (customField.getInt("required")==1),
                                                               (customField.getInt("is_public")==1),
                                                               mOptionsArray);
            if(customField.has("description") && customField.get("description")!=null) 
           	 customFieldVO.setDescription(customField.getString("description"));
            
            ArrayList<CustomFieldVO> currentFields = new ArrayList<CustomFieldVO>();
            if(idCustomFieldVO.containsKey(id))
            {
            	currentFields = idCustomFieldVO.get(id);
            }
            
            currentFields.add(customFieldVO);
            idCustomFieldVO.put(id, currentFields);
            
            //what does this do
            /*if(requestData.getCustomFields()!=null){
                ArrayList<CustomFieldVO> tempFields =  requestData.getCustomFields().getCustFields();
                for(CustomFieldVO tempField : tempFields){
                    if(tempField.getId() == customFieldVO.getId() && tempField.getValue()!=null){
                        customFieldVO.setValue(tempField.getValue());
                    }
                }
            }
            this.customFields.add(customFieldVO);*/
        }//end for loop
   }
	
	private void populateCategoryRequestTypesList(JSONArray categoryTypes) throws JSONException{

		//parse the category thing

		//then populate that with stuff from the hashmap O2n complexity
		for(int i=0; i<categoryTypes.length(); i++)
		{
			JSONObject category_type = categoryTypes.getJSONObject(i).getJSONObject("category");
			CategoryTypeVO categoryTypeVO = new CategoryTypeVO(category_type.getInt("id"),
															  category_type.getInt("client"),
															  category_type.getString("name"),
															  category_type.getString("gov_creator"),
															  category_type.getString("description"),
															  category_type.getString("parent"),
															  category_type.getString("date_created"));
			if(categoryTypeVO.getParent().contentEquals("null") || categoryTypeVO.getParent().contentEquals("0")) //this is toplevel
			{
				RequestTypeVO categoryRequestType = new RequestTypeVO(category_type.getInt("id"), categoryTypeVO.getName(), false);
				categoryRequestType.setDescription(categoryTypeVO.getDescription());
				categoryRequestType.setIsCategory(true);
				this.requestTypes.add(categoryRequestType);
			}
			else //if categoryTypeVO.getParent()==1 or something
			{
				this.categoryTypes.add(categoryTypeVO);
				//remove entry from requestTypes list
				/*this.categoryMap.get(categoryTypeVO.g)
				this.requestTypes.get*/
			}
				
		}
		
		//sorts categories within the top level of request types
		Collections.sort(requestTypes, new Comparator<RequestTypeVO>() {
            public int compare(RequestTypeVO result1, RequestTypeVO result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });


	}

	private void populateRequestFromDraft(RequestDraftVO requestDraftVO){

		if(requestDraftVO.getCustomFields()!=null){
			draftCustomFields = requestDraftVO.getCustomFields().getCustFields();
		}
		if(requestDraftVO.getRequestType()!=null) {
			selectedRequestType = requestDraftVO.getRequestType();
			requestTypeText.setText(requestDraftVO.getRequestType().getName());
		}
		else{
			requestTypeText.setText(R.string.select_request_type_title);
			selectedRequestType = null;
		}
		if(requestDraftVO.getTitle()!=null){
			titleEditText.setText(requestDraftVO.getTitle());
			title = requestDraftVO.getTitle();
		}
		titleChanged = requestDraftVO.getTitleChanged();

		if(requestDraftVO.getDescription()!=null) descriptionEditText.setText(requestDraftVO.getDescription());

		if(requestDraftVO.getPathToImage()!=null){
			pathToImage =  requestDraftVO.getPathToImage();
			File selFile=new File(pathToImage);
			thumbnailBmp = Utils.decodeFile(selFile);
			attachmentButton.setImageBitmap(thumbnailBmp);
			attachmentButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
			hasImage = true;
		}
		isPrivate = requestDraftVO.getPrivate();
		if(isPrivate){
			privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_secure);
		}
		else privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_not_secure);

		zipcode = requestDraftVO.getZipcode();
		latitude = requestDraftVO.getLatitude();
		longitude = requestDraftVO.getLongitude();
		address = requestDraftVO.getAddress();
		state = requestDraftVO.getState();
		city = requestDraftVO.getCity();
		locationButton.setText(address + ", " + city + ", " + state + " " + zipcode);
		if(requestDraftVO.getSpaceId()>0){
			spaceId = requestDraftVO.getSpaceId();
			clientId = requestDraftVO.getClientId();
			getRequestTypes(clientId, true);
		}
		if(requestDraftVO.getLocationString()!=null) locationString = requestDraftVO.getLocationString();

	}


	/**
	 * Click on request type selector, opens list dialog.
	 * @param v
	 */
	public void clickRequestType(View v){
		Intent intent = new Intent(NewRequestActivity.this, RequestTypeListActivity.class);
		Bundle b = new Bundle();
		b.putSerializable("requestTypes", requestTypes);
		b.putSerializable("categoryMap", categoryMap);
		b.putSerializable("categoryTypes", categoryTypes);
		
		intent.putExtras(b);
		startActivityForResult(intent, ACTIVITY_REQUEST_TYPES);
	}

	/**
	 *
	 * @param v
	 */
	public void clickAttachment(View v){
		ArrayList<ImageDialogVO> listToChoose = new ArrayList<ImageDialogVO>();
		listToChoose.add(new ImageDialogVO(R.drawable.holo_light10_device_access_camera, NewRequestActivity.this.getString(R.string.newMedia), null, false));
		listToChoose.add(new ImageDialogVO(R.drawable.holo_light5_content_picture, NewRequestActivity.this.getString(R.string.browseMedia), null, false));
		if(hasImage || hasVideo || hasAudio){
			ImageDialogVO deleteImage = new ImageDialogVO(R.drawable.holo_light5_content_discard, NewRequestActivity.this.getString(R.string.deleteMedia), null, false);
			listToChoose.add(deleteImage);
		}
		
		ArrayList<ImageDialogVO> listNewMedia = new ArrayList<ImageDialogVO>();
		ImageDialogVO takeImage = new ImageDialogVO(R.drawable.holo_light10_device_access_camera, NewRequestActivity.this.getString(R.string.takePhoto), null, false);
		listNewMedia.add(takeImage);
		ImageDialogVO takeVideo = new ImageDialogVO(R.drawable.ic_light_video_capture, NewRequestActivity.this.getString(R.string.takeVideo), null, false);
		listNewMedia.add(takeVideo);
		ImageDialogVO takeSound = new ImageDialogVO(R.drawable.ic_light_audio_capture, NewRequestActivity.this.getString(R.string.takeAudio), null, false);
		listNewMedia.add(takeSound);
		
		ArrayList<ImageDialogVO> listGalleries = new ArrayList<ImageDialogVO>();
		ImageDialogVO selectImage = new ImageDialogVO(R.drawable.holo_light5_content_picture, NewRequestActivity.this.getString(R.string.selectPhoto), null, false);
		listGalleries.add(selectImage);
		ImageDialogVO selectVideo = new ImageDialogVO(R.drawable.ic_light_video_gallery, NewRequestActivity.this.getString(R.string.selectVideo), null, false);
		listGalleries.add(selectVideo);
		ImageDialogVO selectSound = new ImageDialogVO(R.drawable.ic_light_audio_gallery, NewRequestActivity.this.getString(R.string.selectAudio), null, false);
		listGalleries.add(selectSound);
		

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(NewRequestActivity.this.getString(R.string.addAttachment));
		dialog.show();

		final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToChoose/*listToReturn*/, mLInflater);
		ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				
				PackageManager pm = NewRequestActivity.this.getPackageManager();
				
				switch(position){
				case 0:
					//load new dialog with newlist
					clickAttachment(0); //new 
					dialog.dismiss();
					break;
				case 1:
					//load new dialog with newlist.. maybe do all that in here
					clickAttachment(1); //existing
					dialog.dismiss();
					break;
				case 2:
					pathToImage = null;
					hasImage = false;
					hasAudio = false;
					hasVideo = false;
					attachmentButton.setImageResource(R.drawable.holo_light5_content_new_picture);
					attachmentButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});

	}
	
	public void clickAttachment(final int i){
		
		ArrayList<ImageDialogVO> mediaChoice = null;
		
		if(i == 0) //user selected new media
		{
			mediaChoice = new ArrayList<ImageDialogVO>();
			ImageDialogVO takeImage = new ImageDialogVO(R.drawable.holo_light10_device_access_camera, NewRequestActivity.this.getString(R.string.takePhoto), null, false);
			mediaChoice.add(takeImage);
			/*ImageDialogVO takeVideo = new ImageDialogVO(R.drawable.ic_light_video_capture, NewRequestActivity.this.getString(R.string.takeVideo), null, false);
			mediaChoice.add(takeVideo);
			ImageDialogVO takeSound = new ImageDialogVO(R.drawable.ic_light_audio_capture, NewRequestActivity.this.getString(R.string.takeAudio), null, false);
			mediaChoice.add(takeSound);*/
		}

		if(i == 1){
			mediaChoice = new ArrayList<ImageDialogVO>();
			ImageDialogVO selectImage = new ImageDialogVO(R.drawable.holo_light5_content_picture, NewRequestActivity.this.getString(R.string.selectPhoto), null, false);
			mediaChoice.add(selectImage);
			/*ImageDialogVO selectVideo = new ImageDialogVO(R.drawable.ic_light_video_gallery, NewRequestActivity.this.getString(R.string.selectVideo), null, false);
			mediaChoice.add(selectVideo);
			ImageDialogVO selectSound = new ImageDialogVO(R.drawable.ic_light_audio_gallery, NewRequestActivity.this.getString(R.string.selectAudio), null, false);
			mediaChoice.add(selectSound);*/
		}

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(NewRequestActivity.this.getString(R.string.addAttachment));
		dialog.show();

		final ImageListViewAdapter adapter = new ImageListViewAdapter(this, mediaChoice/*listToReturn*/, mLInflater);
		ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				
				PackageManager pm = NewRequestActivity.this.getPackageManager();
				
				if(i == 0) // new
				{
					switch(position){
					case 0:
						

						if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature("android.hardware.camera.front")) {
							final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(getApplicationContext())));
							startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);

							dialog.dismiss();
							break;
						}
						else
						{
							//no cameras detected
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.noCamera), duration);
							toast.show();
							
							toast.setGravity(Gravity.BOTTOM, 0, 0);
							break;
						}
					case 1:
						if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature("android.hardware.camera.front")) {
							final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(getApplicationContext())));
							startActivityForResult(intent, ACTIVITY_TAKE_VIDEO);
						}
						break;
					case 2:
						final Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
						intent2.setType("audio/*");
						startActivityForResult(intent2, ACTIVITY_SELECT_AUDIO);
						dialog.dismiss();
						break;
					default:
						break;
					}
				}
				
				else if(i == 1) // existing
				{
					switch(position){
						case 0:
							final Intent intent = new Intent();
							intent.setAction(Intent.ACTION_PICK);
							intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(intent, ACTIVITY_SELECT_PHOTO);
							dialog.dismiss();
							break;
						case 1:
							//this video selector works on samsung tab, htc evo4g
							
							final Intent intent1 = new Intent(Intent.ACTION_PICK);
							intent1.setType("video/*");
							startActivityForResult(intent1, ACTIVITY_SELECT_VIDEO);
							dialog.dismiss();
							break;
						case 2:
							//this audio selector works on evo 4g, samsung tab, 
							
							final Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
							intent2.setType("audio/*");
							//intent2.setDataAndType(android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "audio/*");
							startActivityForResult(intent2, ACTIVITY_SELECT_AUDIO);
							dialog.dismiss();
							break;
						default:
							break;
					}
				}
				
				/*switch(position){
				case 0:
					

					if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature("android.hardware.camera.front")) {
						final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(getApplicationContext())));
						startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);

						dialog.dismiss();
						break;
					}
					else
					{
						//no cameras detected
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.noCamera), duration);
						toast.show();
						
						toast.setGravity(Gravity.BOTTOM, 0, 0);
					}
					
					
				
				case 2:
					if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature("android.hardware.camera.front")) {
						final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Utils.getTempFile(getApplicationContext())));
						startActivityForResult(intent, ACTIVITY_TAKE_VIDEO);
						dialog.dismiss();
						break;
					}
				case 6:
					pathToImage = null;
					hasImage = false;
					attachmentButton.setImageResource(R.drawable.holo_light5_content_new_picture);
					attachmentButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}*/
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_CANCELED){
			return;
		}
		switch(requestCode){
		case ACTIVITY_SELECT_PHOTO:
			if(data != null){
				String filePath = Utils.getPath(data.getData(), getContentResolver());

				Log.e(TAG, "filePath = " + filePath);

				pathToImage = filePath;

				//Drawable drawable = Drawable.createFromPath(pathToImage);
				//imgSelected.setImageDrawable(drawable);
				File selFile=new File(pathToImage);

				thumbnailBmp = Utils.decodeFile(selFile);
				attachmentButton.setImageBitmap(thumbnailBmp);
				attachmentButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
				hasImage = true;
				////System.gc();

			}else{
				Log.e("SubmitRequestActivity", "data == null");
			}
			break;
		case ACTIVITY_SELECT_VIDEO:
			if(data != null){
				String filePath = Utils.getPath(data.getData(), getContentResolver());

				Log.e(TAG, "filePath = " + filePath);

				pathToImage = filePath;

				//Drawable drawable = Drawable.createFromPath(pathToImage);
				//imgSelected.setImageDrawable(drawable);
				File selFile=new File(pathToImage);

				thumbnailBmp = ThumbnailUtils.createVideoThumbnail(selFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
				attachmentButton.setImageBitmap(thumbnailBmp);
				attachmentButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
				hasVideo = true;
				////System.gc();

			}else{
				Log.e("SubmitRequestActivity", "data == null");
			}
			break;
		case ACTIVITY_SELECT_AUDIO:
			break;
		case ACTIVITY_TAKE_PHOTO:
			try{
				pathToImage=Utils.getTempFile(getApplicationContext()).getAbsolutePath();
				Log.e(TAG, "filePath = " + pathToImage);
				File takenFile=Utils.getTempFile(getApplicationContext());
				thumbnailBmp = Utils.decodeFile(takenFile);
				attachmentButton.setImageBitmap(thumbnailBmp);
				attachmentButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
				hasImage = true;
				//System.gc();

			}
			catch(Exception exp){
				Log.e("TakePhoto", exp.getMessage());
			}
			break;
		case ACTIVITY_TAKE_VIDEO:
			break;
		case ACTIVITY_EDIT_LOCATION:
			Bundle extras = data.getExtras();
			double returnedLat = extras.getDouble("latitude");
			double returnedLon = extras.getDouble("longitude");
			String oldCity = city;
			if(returnedLat!=latitude || returnedLon!=longitude){
				LocationVO locationVO = new LocationVO(returnedLat, returnedLon, this);
				if(locationVO.getCityName()!=null){
					setLocationData(locationVO);
					if(!city.equals(oldCity)){
						getCityData(locationVO);
					}
				}
				else{
					final CustomAlertDialog noLocationDialog = new CustomAlertDialog(this, NewRequestActivity.this.getString(R.string.geoError), NewRequestActivity.this.getString(R.string.googleGeoError), NewRequestActivity.this.getString(R.string.OkButton), null);

					View.OnClickListener listener = new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							int id = v.getId();
							switch(id){
							case R.id.alertConfirm:
								noLocationDialog.dismiss();
								break;
							default:
								break;
							}
						}
					};
					noLocationDialog.setListener(listener);
					noLocationDialog.show();
				}
			}
			break;
		case ACTIVITY_CUSTOM_FIELDS:
			if (resultCode == RESULT_OK){
				Bundle customFieldsExtras = data.getExtras();
				if(customFieldsExtras!=null && customFieldsExtras.getSerializable("requestData")!=null){
					RequestDraftVO draftVO = (RequestDraftVO)  customFieldsExtras.getSerializable("requestData");
					draftCustomFields = draftVO.getCustomFields().getCustFields();
				}
				else {
					finish();
				}
			}
			break;
		case ACTIVITY_REQUEST_TYPES:
			Bundle requestTypeExtras = data.getExtras();
			int selected = requestTypeExtras.getInt("request_type_position");
			if(requestTypeExtras.containsKey("category_id"))
			{
				ArrayList<RequestTypeVO> currentList = requestTypeExtras.getParcelableArrayList("nestedRequest");
				//if contains category, use the appropriate arraylist
				selectedRequestType = currentList.get(selected);
			}
			else
				selectedRequestType = requestTypes.get(selected);

			requestTypeText.setText(selectedRequestType.getName());
			for(int i=0; i<requestTypes.size(); i++){
				RequestTypeVO requestTypeVO = requestTypes.get(i);
				if(i==selected)requestTypeVO.setSelected(true);
				else requestTypeVO.setSelected(false);
				requestTypes.set(i, requestTypeVO);
			}
			
			//toggle privacy switch
			if(selectedRequestType.getForcePrivate().contentEquals("1")){
				isPrivate = true;
				privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_secure);
				privacyButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.forcePrivate), Toast.LENGTH_SHORT).show();
					}
					
				});
				
			}
			else if(isPrivate == true) //don't reset privacy thing basically
			{
				privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_secure);
			}
			else
			{
				isPrivate = false;
				privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_not_secure);
				privacyButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						clickPrivacy();
					}
					
				});
			}
			
			if(selectedRequestType.getDisableTitle().contentEquals("1"))
			{
				titleEditText.setFocusable(false);
			}
			else
				titleEditText.setFocusableInTouchMode(true);
			
			if(selectedRequestType.getDisableDescription().contentEquals("1"))
			{
				descriptionEditText.setFocusable(false);
			}
			else
				descriptionEditText.setFocusableInTouchMode(true);
			
			if(selectedRequestType.getHasCustomField())
			{
				
				//they are all array object
				
				
				
				//determine if customfield is string or arraylist<object>
				/*JSONArray customFieldsArray = parser.getResponse().getJSONArray("custom_fields");
                populateCustomFields(customFieldsArray);
                populateCustomFieldsList();*/
			}
			
			// if not title was modified by user - use request type as title=
			if(!titleChanged){
				if(PublicStuff.IS_CLIENT_APP) titleEditText.setText(selectedRequestType.getName());
				else  titleEditText.setText(selectedRequestType.getName() + " in " + city);
			}
			break;

		}
	}

	/**
	 *
	 * @param v
	 */
	public void clickPrivacy(){
		ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
		ImageDialogVO publicR = new ImageDialogVO(R.drawable.holo_light6_social_group, NewRequestActivity.this.getString(R.string.publicSelector), NewRequestActivity.this.getString(R.string.publicComment), !isPrivate);
		listToReturn.add(publicR);
		ImageDialogVO privateR = new ImageDialogVO(R.drawable.holo_light6_social_person, NewRequestActivity.this.getString(R.string.privateSelector), NewRequestActivity.this.getString(R.string.privateComment), isPrivate);
		listToReturn.add(privateR);

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(NewRequestActivity.this.getString(R.string.setPrivacy));
		dialog.show();

		final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToReturn, mLInflater);
		ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch(position){
				case 0:
					isPrivate = false;
					privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_not_secure);
					dialog.dismiss();
					break;
				case 1:
					isPrivate = true;
					privacyButton.setBackgroundResource(R.drawable.holo_light10_device_access_secure);
					dialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 *
	 * @param v
	 */
	public void clickLocation(View v){
		//start intent with Bundle parameters
		Intent intent = new Intent(NewRequestActivity.this, NewRequestMapActivity.class);		
		Bundle b = new Bundle();
		b.putDouble("latitude", latitude);
		b.putDouble("longitude", longitude);
		intent.putExtras(b);
		startActivityForResult(intent, ACTIVITY_EDIT_LOCATION);		
	}

	private class EditTextListener implements TextWatcher {
		public void afterTextChanged(Editable s) {
			titleChanged = !(titleEditText.getText().toString().equals("") || (selectedRequestType!=null && titleEditText.getText().toString().equals(selectedRequestType.getName() + " in " + city)));
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		public void onTextChanged(CharSequence s, int start, int before, int count){}
	}

	/**
	 *
	 * @param v
	 */
	public void back(View v){
		ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
		ImageDialogVO saveDraft = new ImageDialogVO(R.drawable.holo_light5_content_save, NewRequestActivity.this.getString(R.string.saveDraft), null, false);
		listToReturn.add(saveDraft);
		ImageDialogVO deleteDraft = new ImageDialogVO(R.drawable.holo_light5_content_discard, (requestDraft>-1)?NewRequestActivity.this.getString(R.string.deleteDraft):NewRequestActivity.this.getString(R.string.exitSave), null, false);
		listToReturn.add(deleteDraft);
		ImageDialogVO backToDraft = new ImageDialogVO(R.drawable.holo_light5_content_remove, NewRequestActivity.this.getString(R.string.resumeDraft), null, false);
		listToReturn.add(backToDraft);

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		//Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(NewRequestActivity.this.getString(R.string.exitScreen));
		
		dialog.show();

		final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToReturn, mLInflater);
		ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch(position){
				case 0:
					if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))saveAsDraft();
					else promptLoginToSaveDraft();
					dialog.dismiss();
					break;
				case 1:
					if(requestDraft>-1){
						ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
						requestDraftVOs.remove(requestDraft);
						app.setUserDrafts(requestDraftVOs);
					}
					finish();
					dialog.dismiss();
					break;
				case 2:
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
	}

	private void promptLoginToSaveDraft(){
		final CustomAlertDialog alert = new CustomAlertDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.login), NewRequestActivity.this.getString(R.string.loginDraft), NewRequestActivity.this.getString(R.string.login), NewRequestActivity.this.getString(R.string.cancel));

		View.OnClickListener listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int id = v.getId();
				switch(id){
				case R.id.alertConfirm:

					Intent intent = new Intent(NewRequestActivity.this, CreateAccountActivity.class);
					startActivity(intent);
					saveAsDraft();
					alert.dismiss();
					break;
				case R.id.alertCancel:
					alert.dismiss();
					break;
				default:
					break;
				}
			}
		};
		alert.setListener(listener);
		alert.show();
	}
	private RequestDraftVO populateDraft(){
		RequestDraftVO requestDraftVO = new RequestDraftVO();
		if(spaceId>0){
			requestDraftVO.setSpaceId(spaceId);
			requestDraftVO.setClientId(clientId);
		}

		if(selectedRequestType!=null) {
			requestDraftVO.setRequestType(selectedRequestType);
		}
		if(!titleEditText.getText().toString().equals("")){
			requestDraftVO.setTitle(titleEditText.getText().toString());
		}
		requestDraftVO.setTitleChanged(titleChanged);

		if(!descriptionEditText.getText().toString().equals("")) requestDraftVO.setDescription(descriptionEditText.getText().toString());
		if(locationString!=null) requestDraftVO.setLocationString(locationString);
		if(pathToImage!=null){
			requestDraftVO.setPathToImage(pathToImage);
		}
		requestDraftVO.setPrivate(isPrivate);

		requestDraftVO.setZipcode(zipcode);
		requestDraftVO.setLatitude(latitude);
		requestDraftVO.setLongitude(longitude);
		requestDraftVO.setAddress(address);
		requestDraftVO.setState(state);
		requestDraftVO.setCity(city);
		if(draftCustomFields!=null) requestDraftVO.setCustomFields(new CustomFieldArrayVO(draftCustomFields));
		return requestDraftVO;
	}
	private void saveAsDraft(){
		RequestDraftVO requestDraftVO = populateDraft();

		ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
		if(requestDraft>-1){
			requestDraftVOs.set(requestDraft, requestDraftVO);
		}
		else{
			requestDraftVOs.add(requestDraftVO);
		}
		app.setUserDrafts(requestDraftVOs);
		Toast.makeText(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.draftSaved), Toast.LENGTH_LONG).show();
		finish();
	}

	/**
	 *  Call when user click submit button
	 * @param v
	 */
	public void clickSubmit(View v){
		String alertTitle = NewRequestActivity.this.getString(R.string.errorRequest);
		String alertMessage = "";
		title =  titleEditText.getText().toString();
		description =descriptionEditText.getText().toString();
		if(title.equals("")) alertMessage += NewRequestActivity.this.getString(R.string.titleRequired);
		if(selectedRequestType==null) alertMessage += ((alertMessage.length()>0)?"\n":"") + NewRequestActivity.this.getString(R.string.requestTypeRequired) ;
		if(zipcode==null || address==null || (latitude == app.getCityLat() && longitude == app.getCityLon()))alertMessage += ((alertMessage.length()>0)?"\n":"") + NewRequestActivity.this.getString(R.string.errorLocationConfirm);
		if(!alertMessage.equals("")){

			final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, NewRequestActivity.this.getString(R.string.OkButton), null);

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
		else if(PublicStuff.IS_CLIENT_APP && spaceId!=app.getCitySpaceId()){
			String title =  "Request outside "+app.getCityAppName();
			String message = "Because of the geographic location of this request, the request will be submitted to " +
					city + ", " + state + " instead of " + app.getCityAppName();
			final CustomAlertDialog dialog = new CustomAlertDialog(this, title, message, "Continue", "Change Location");

			View.OnClickListener listener = new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int id = v.getId();
					switch(id){
					case R.id.alertConfirm:
						postRequest();
						dialog.dismiss();
						break;
					case R.id.alertCancel:
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
		else{
			postRequest();
		}

	}

	private void postRequest(){
		if(selectedRequestType.getHasCustomField()){
			RequestDraftVO requestDraftVO = populateDraft();
			Bundle b = new Bundle();
			b.putSerializable("requestData", requestDraftVO);
			b.putInt("requestDraft", requestDraft);
			b.putSerializable("customFieldMap", idCustomFieldVO.get(selectedRequestType.getId()));

			Intent intent = new Intent(NewRequestActivity.this, CustomFieldsActivity.class);
			intent.putExtras(b);
			startActivityForResult(intent, ACTIVITY_CUSTOM_FIELDS);
		}
		else{
			HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("request_submit"), Utils.isNetworkAvailable(NewRequestActivity.this)){
				final CustomProgressDialog dialog = new CustomProgressDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.submitRequest));
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
					if(result.length()>0){
						try {
							String resultTitle;
							String resultMessage;
							JSONParser parser = new JSONParser(result);
							resultTitle = parser.getStatus().getType();
							if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
								if(requestDraft>-1){
									ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
									requestDraftVOs.remove(requestDraft);
									app.setUserDrafts(requestDraftVOs);
								}
								CustomProgressDialog dialog = new CustomProgressDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.loadingRequest));
								dialog.show();
								Bundle b = new Bundle();
								b.putInt("request_id", parser.getResponse().getInt("request_id"));

								Intent intent = new Intent(NewRequestActivity.this, RequestDetailsActivity.class);
								intent.putExtras(b);
								startActivityForResult(intent, 0);
								finish();
							}
							else{
								resultMessage = parser.getStatus().getMessage();
								if(resultMessage.startsWith("You must be logged in")){
									final CustomAlertDialog alert = new CustomAlertDialog(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.login), String.format(NewRequestActivity.this.getString(R.string.loginRequired), city), NewRequestActivity.this.getString(R.string.login), NewRequestActivity.this.getString(R.string.cancel));

									View.OnClickListener listener = new View.OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											int id = v.getId();
											switch(id){
											case R.id.alertConfirm:
												Intent intent = new Intent(NewRequestActivity.this, CreateAccountActivity.class);

												startActivity(intent);
												saveAsDraft();
												alert.dismiss();
												break;
											case R.id.alertCancel:
												alert.dismiss();
												break;
											default:
												break;
											}
										}
									};
									alert.setListener(listener);
									alert.show();
								}
								else{
									final CustomAlertDialog alert = new CustomAlertDialog(NewRequestActivity.this, resultTitle, resultMessage, NewRequestActivity.this.getString(R.string.OkButton), null);
									View.OnClickListener listener = new View.OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											int id = v.getId();
											switch(id){
											case R.id.alertConfirm:
												alert.dismiss();
												break;
											default:
												break;
											}
										}
									};
									alert.setListener(listener);
									alert.show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(NewRequestActivity.this, NewRequestActivity.this.getString(R.string.unsuccessful),Toast.LENGTH_SHORT).show();
						}
						finally{
							if(dialog != null && dialog.isShowing()){
								dialog.dismiss();
							}
						}
					}else {
						Utils.noConnection(NewRequestActivity.this);
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				}
			};
			//sent with every request
			clientPost.addParameter("zipcode", zipcode);
			clientPost.addParameter("address", address);
			clientPost.addParameter("space_id", String.valueOf(spaceId));
			clientPost.addParameter("request_type_id", String.valueOf(selectedRequestType.getId()));
			clientPost.addParameter("is_private", (isPrivate) ? "1" : "0");
			clientPost.addParameter("title", title);
			if(description!=null) clientPost.addParameter("description", description);
			clientPost.addParameter("used_location", "1");
			//switched because of columns in db
			clientPost.addParameter("lat", String.valueOf(longitude));
			clientPost.addParameter("lon", String.valueOf(latitude));
			//data for geolocation debugging
			if(currLocation!=null){
				clientPost.addParameter("original_lat", String.valueOf(currLocation.getLatitude()));
				clientPost.addParameter("original_lon", String.valueOf(currLocation.getLongitude()));
				clientPost.addParameter("original_accuracy", String.valueOf(currLocation.getAccuracy()));
			}
			//for images
			if(hasImage){
				clientPost.addParameter("has_image", "1");
				clientPost.addParameter("image", pathToImage);
			}
			else if(hasVideo){
				clientPost.addParameter("has_video", "1");
				clientPost.addParameter("video", pathToImage);
			}
			else if(hasAudio){
				clientPost.addParameter("has_audio", "1");
				clientPost.addParameter("audio", pathToImage);
			}
			if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientPost.addParameter("api_key", app.getUserApiKey());
			clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());

			clientPost.execute(new Void[0]);
		}
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
	public void onBackPressed(){
		back(null);
	}
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
		FlurryAgent.onStartSession(NewRequestActivity.this, "2C3QVVZMX8Q5M6KF3458");
		//System.gc();
		super.onStart();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}

	@Override
	protected void onResume() {
		doStartListenLocation();
		//System.gc();
		super.onResume();
	}
	@Override
	protected void onStop() {
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		
		FlurryAgent.onEndSession(NewRequestActivity.this);
		//System.gc();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		//System.gc();
		stopService(new Intent(this, LocationListenerAgent.class));
		super.onDestroy();
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("requestDataOnPause", populateDraft());

	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		populateRequestFromDraft((RequestDraftVO)savedInstanceState.getSerializable("requestDataOnPause"));
	}


}

