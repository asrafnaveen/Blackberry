package bundle.android.views;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.CommentsListViewAdapter;
import bundle.android.adapters.ImageListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.model.vo.CommentVO;
import bundle.android.model.vo.ImageDialogVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.addthis.core.AddThis;
import com.addthis.core.Config;
import com.addthis.error.ATDatabaseException;
import com.addthis.error.ATSharerException;
import com.flurry.android.FlurryAgent;
//import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
//import com.google.android.gms.maps.LocationSource;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.CameraPosition;
import com.wassabi.psmobile.R;

public class RequestDetailsActivity extends android.support.v4.app.FragmentActivity implements 
LocationListener  {
	private PublicStuffApplication app;
	//top tab
	private Button requestInfo;
	private Button requestComments;
	private View infoHighlight;
	private View commentHighlight;
	View layout;
	//request details
	private ScrollView detailsView;
	private LinearLayout detailsLayout;
	private LinearLayout detailsLinView;
	//Map
	//private static GoogleMap mMap;
	private TextView spaceText;
	//title
	private TextView requestTitle;
	private ImageView myRequestDogEar;
	//followers and votes
	private Button followRequest;
	private Button supportRequest;
	private ImageButton flagRequest;
	//privacy
	private LinearLayout requestIsPrivate;
	//status
	private TextView requestStatus;
	private ImageView requestStatusImage;
	//description
	private TextView requestDescriptionLabel;
	private TextView requestDescription;
	private ImageView requestImage;
	//details
	private TextView requestAddress;
	private TextView requestId;
	TextView requestPriority;
	private TextView requestType;
	
	Button closeRequest;

	private TextView requestSubmittedBy;
	private LinearLayout submittedLayout;
	private TextView requestSubmittedOn;
	private TextView requestSubmittedOnLabel;

	//comments
	private RelativeLayout commentView;
	private ListView commentListView;
	private ImageButton commentImage;
	private EditText commentText;
	private Button postComment;
	private CommentsListViewAdapter adapter;
	private ArrayList<CommentVO> commentsList = new ArrayList<CommentVO>();

	//data
	private int request_id;
	private String request_url;
	private String request_title;
	private boolean commentHasImage;
	private String pathToCommentImage;
	private static final int ACTIVITY_SELECT_PHOTO = 0;
	private static final int ACTIVITY_TAKE_PHOTO = 1;
	private static final String TAG = "RequestDetailsActivity";
	private boolean isFollowing = false;
	private boolean isRequest = false;
	private boolean isPrivate = false;
	private boolean isSupporting = false;
	private boolean isFlagged = false;
	private Bundle b;
	private boolean updatedRequest = false;
	private RelativeLayout mapLayout;
	
	private double latitude;
	private double longitude;

	private JSONObject requestData;
	
	String reason = "";
	String reasonAppend = "";
	String publicPrivate = "";
	ArrayList<String> items; 
	EditText elaboration;
	CheckBox publicPrivateCheckbox;
	Dialog reasoningDialog;
	
	ImageButton makePrivateRequest;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//set content view and fonts
		setContentView(R.layout.requestdetails);
		app = (PublicStuffApplication)getApplicationContext();
		Utils.useLatoInView(this, this.findViewById(android.R.id.content));
		
		//Map
	//	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
	//			.findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			// First incarnation of this activity.
			//mapFragment.setRetainInstance(true);

			//get request_id
			b = getIntent().getExtras();
			request_id = b.getInt("request_id");
			getRequestData(request_id);
		} else {
			// Reincarnated activity. The obtained map is the same map instance in the previous
			// activity life cycle. There is no need to reinitialize it.
			//mMap = mapFragment.getMap();
		}
		setUpMap();
		
		initElements();
		
		FlurryAgent.logEvent("Request Details View");
		DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Request Details View");

		initAddThisConfig();
	}

	private void initElements(){
		RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
		headerLayoutView.setBackgroundColor(app.getNavColor());
		mapLayout = (RelativeLayout)this.findViewById(R.id.mapLayout);
		ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
		if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
			AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
			imageLoader.execute(new Void[0]);
		}
		else{
			cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
		}

		closeRequest = (Button)this.findViewById(R.id.closeRequest);
		makePrivateRequest = (ImageButton)this.findViewById(R.id.requestPrivacy);
		
		//top tab
		requestInfo = (Button)this.findViewById(R.id.requestInfo);
		requestComments = (Button)this.findViewById(R.id.requestComments);
		infoHighlight = this.findViewById(R.id.infoHighlight);
		commentHighlight = this.findViewById(R.id.commentHighlight);

		//request details
		detailsLayout = (LinearLayout)this.findViewById(R.id.detailsLayout);
		detailsView = (ScrollView)this.findViewById(R.id.detailsView);
		detailsLinView = (LinearLayout)this.findViewById(R.id.detailsLinView);
		spaceText =(TextView)this.findViewById(R.id.spaceText);
		//title
		requestTitle = (TextView)this.findViewById(R.id.requestTitle);
		
		myRequestDogEar = (ImageView)this.findViewById(R.id.myRequestDogEar);
		//followers and votes
		followRequest = (Button)this.findViewById(R.id.followRequest);
		supportRequest = (Button)this.findViewById(R.id.support);
		flagRequest = (ImageButton)this.findViewById(R.id.flag);
		//privacy
		requestIsPrivate = (LinearLayout)this.findViewById(R.id.requestIsPrivate);
		//status
		requestStatus= (TextView)this.findViewById(R.id.requestStatus);
		requestStatusImage =(ImageView)this.findViewById(R.id.requestStatusImage);
		//description
		requestDescriptionLabel = (TextView)this.findViewById(R.id.requestDescriptionLabel);
		requestDescription = (TextView)this.findViewById(R.id.requestDescription);
		requestImage = (ImageView)this.findViewById(R.id.requestImage);
		//details
		requestAddress = (TextView)this.findViewById(R.id.requestAddress);
		requestPriority = (TextView)this.findViewById(R.id.requestPriority);
		requestId = (TextView)this.findViewById(R.id.requestID);
		requestType  = (TextView)this.findViewById(R.id.requestType);

		requestSubmittedBy =(TextView)this.findViewById(R.id.requestSubmittedBy);
		requestSubmittedOn =(TextView)this.findViewById(R.id.requestSubmittedOn);
		submittedLayout = (LinearLayout)this.findViewById(R.id.submittedLayout);
		requestSubmittedOnLabel = (TextView)this.findViewById(R.id.requestSubmittedOnLabel);

		//comments
		commentView = (RelativeLayout)this.findViewById(R.id.commentView);
		commentListView = (ListView)this.findViewById(R.id.comment_listview);

		commentImage = (ImageButton)this.findViewById(R.id.commentImage);
		postComment = (Button)this.findViewById(R.id.postComment);
		commentText = (EditText)this.findViewById(R.id.commentText);
		if(app.getUserApiKey()==null || app.getUserApiKey().equals("")){
			commentText.setText(RequestDetailsActivity.this.getString(R.string.loginComment));
			commentText.setKeyListener(null);
			commentImage.setEnabled(false);
			postComment.setEnabled(false);
		}


	}
	private void initAddThisConfig(){
		Config.configObject().setShouldUseTwitterOAuth(true);
		Config.configObject().setTwitterConsumerKey("SnKco6Y2Anh0qDxT86QAlw");
		Config.configObject().setTwitterConsumerSecret("mAgSk7ttsUtM9X1Sb1Z2RSTOh2zA1xTz4hZT540UA");
		Config.configObject().setTwitterCallbackUrl("http://account.publicstuff.com/twitter/oauthcallback");
		Config.configObject().setFacebookAppId("121852844605678");
		Config.configObject().setAddThisAppId("4fdf90d04ae1d263");
		Config.configObject().setAddThisPubId("ra-4f9ebf0013a82384");
	}
	private void getRequestData(int id){
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("request_view"), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.requestDetails));
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
				if(result.length()>0)  {
					try {
						JSONParser parser = new JSONParser(result);
						if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
							JSONObject request = parser.getResponse();
							requestData = request;
							populateRequestData(request);
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
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
					Utils.noConnection(RequestDetailsActivity.this);
				}
			}
		};
		clientGet.addParameter("request_id", String.valueOf(id));
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		
		if(app.getUserApiKey() != null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.execute("");
	}
	private void populateRequestData(JSONObject request){

		try {
			if(request.has("display_foreign_id") && request.getString("display_foreign_id").equals("true")) {
				requestId.setText((request.get("foreign_id")!=null && !request.getString("foreign_id").equals("null"))?request.getString("foreign_id"):RequestDetailsActivity.this.getString(R.string.pending));
			}
			else{
				requestId.setText(String.valueOf(request_id));
			}
			
			requestType.setText(request.getString("request_type_name"));
			if(request.has("user") && request.getString("user")!=null && !request.getString("user").equals("")) requestSubmittedBy.setText(request.getString("user"));
			else requestSubmittedBy.setText(RequestDetailsActivity.this.getString(R.string.anonymous));
			parseStatus(request.getString("request_status"));
			requestSubmittedOn.setText(Utils.parseDate(request.getLong("date_created"), RequestDetailsActivity.this));
			followRequest.setText(request.getInt("count_followers") + RequestDetailsActivity.this.getString(R.string.followers));
			requestComments.setText(RequestDetailsActivity.this.getString(R.string.comments) +" ("+ request.getInt("count_comments")+ ")");
			supportRequest.setText(request.getString("count_supporters"));
			requestTitle.setText(request.getString("title"));
			if(request.getString("description").equals("") && request.getString("image_thumbnail").equals("")){
				requestDescriptionLabel.setVisibility(View.GONE);
				requestDescription.setVisibility(View.GONE);
				requestImage.setVisibility(View.GONE);
			}
			else{
				if(request.getString("description").equals("")) requestDescription.setVisibility(View.GONE);
				else requestDescription.setText(request.getString("description"));
				if("".equals(request.getString("image_thumbnail"))) requestImage.setVisibility(View.GONE);
				else {
					new DownloadImageTask().execute(request.getString("image_thumbnail"));
				}
			}
			if(request.getBoolean("is_visible")){
				requestIsPrivate.setVisibility(View.GONE);
			}
			else{
				isPrivate = true;
				requestIsPrivate.setVisibility(View.VISIBLE);
			}
			
			if(request.has("allow_citizen_closure"))
			{
				if(request.getBoolean("is_user_request") && request.getBoolean("allow_citizen_closure")==true && !request.getString("request_status").contentEquals("completed"))
				{
					closeRequest.setVisibility(View.VISIBLE);
				}
				else
					closeRequest.setVisibility(View.INVISIBLE);
			}
			
			/*if(request.getBoolean("is_user_request"))
			{
				closeRequest.setVisibility(View.VISIBLE);
			}*/
			
			if(request.getBoolean("is_user_request") && request.getBoolean("is_visible")) //if is user's request and is it a public request
			{
				makePrivateRequest.setVisibility(View.VISIBLE);
			}

			requestAddress.setText(request.getString("address") + ", " + request.getString("location") +", " + request.getString("zipcode"));
			if(!request.getString("priority").contentEquals("null"))
				requestPriority.setText(request.getString("priority"));
			spaceText.setText(request.getString("location"));
			request_url = request.getString("request_url");
			request_title = request.getString("url_title");

			if(!request.has("is_user_request") || !request.getBoolean("is_user_request")) {
				myRequestDogEar.setVisibility(View.INVISIBLE);
			}
			else{
				isRequest = true;
				myRequestDogEar.setVisibility(View.VISIBLE);
			}
			if(request.has("is_user_following") && request.getBoolean("is_user_following")) {
				isFollowing = true;
				followRequest.setPressed(true);
			}
			if(request.has("is_user_supporting") && request.getBoolean("is_user_supporting")) {
				isSupporting = true;
				supportRequest.setPressed(true);
			}
			if(request.has("is_user_flagged") && request.getBoolean("is_user_flagged")) {
				isFlagged = true;
				flagRequest.setPressed(true);
			}

			moveMap(request.getDouble("lat"), request.getDouble("lon"));
			if(request.has("custom_fields")){
				JSONArray customFields = request.getJSONArray("custom_fields");
				populateCustomFields(customFields);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void populateCustomFields(JSONArray customFields) throws JSONException{

		for(int i= 0; i<customFields.length(); i++){
			detailsLinView.removeView(detailsLinView.findViewWithTag(i));
			JSONObject customField = customFields.getJSONObject(i).getJSONObject("custom_field");
			LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = mLInflater.inflate(R.layout.custom_field_value, null);
			layout.setTag(i);
			TextView label = (TextView)layout.findViewById(R.id.customFieldLabel);
			label.setText(customField.getString("name"));
			TextView value = (TextView)layout.findViewById(R.id.customFieldValue);
			if(customField.has("type") && customField.getString("type").equals("checkbox")){
				value.setText((customField.getInt("value")==1)?RequestDetailsActivity.this.getString(R.string.yes):RequestDetailsActivity.this.getString(R.string.no));
			}
			else value.setText(customField.getString("value"));
			Utils.useLatoInView(this, layout);
			detailsLinView.addView(layout);
		}
	}
	
	private void parseStatus(String status){
		String statusText;
		int background;
		if(status.equals("submitted")){
			statusText = RequestDetailsActivity.this.getString(R.string.statusSubmitted);
			background = R.drawable.ic_status_large_1;
		}
		else if (status.equals("received")){
			statusText = RequestDetailsActivity.this.getString(R.string.statusAcknowledged);
			background = R.drawable.ic_status_large_2;
		}
		else if(status.equals("in progress")){
			statusText = RequestDetailsActivity.this.getString(R.string.statusInProgress);
			background = R.drawable.ic_status_large_3;
		}
		else if(status.equals("completed")){
			statusText = RequestDetailsActivity.this.getString(R.string.statusCompleted);
			background = R.drawable.ic_status_large_4;
		}
		else{
			statusText = RequestDetailsActivity.this.getString(R.string.statusOther);
			background = R.drawable.ic_status_large_5;
		}
		requestStatus.setText(statusText);
		requestStatusImage.setImageResource(background);
	}

	private void moveMap(final double lat, final double lon){/*
		latitude = lat;
		longitude = lon;
		mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange_shadow)));
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), mMap.getMaxZoomLevel()-4));
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);
		GeoPoint point = new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
		MapController mapController = mapView.getController();

		mapController.animateTo(point);
		mapController.setZoom(17);
		List<Overlay>  overlays = mapView.getOverlays();
		PSItemizedOverlay itemizedOverlay = new PSItemizedOverlay(this.getResources().getDrawable(R.drawable.ic_map_pin_orange));
		OverlayItem overlayitem = new OverlayItem(point, "", "");
		itemizedOverlay.addOverlay(overlayitem);
		overlays.add(itemizedOverlay);

		mMap.setOnMapClickListener(new OnMapClickListener(){

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(RequestDetailsActivity.this, RequestDetailsMapActivity.class);
				Bundle b = new Bundle();
				b.putDouble("latitude", lat);
				b.putDouble("longitude", lon);
				intent.putExtras(b);
				startActivity(intent);
			}
			
			
		});

	*/}
	
	private void getCommentData(int id){
		commentsList.clear();
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("comments_list"), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.gettingComments));
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
							JSONArray comments = parser.getResponse().getJSONArray("comments");
							populateCommentData(comments);
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
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
					Utils.noConnection(RequestDetailsActivity.this);
				}
			}
		};
		clientGet.addParameter("request_id", String.valueOf(id));
		if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		clientGet.execute("");
	}
	private void populateCommentData(JSONArray comments){

		/**Code here to loop through comments json object and build commentsVO objects**/
		for(int i=0; i<comments.length(); i++){
			try {
				JSONObject comment = comments.getJSONObject(i).getJSONObject("comment");
				CommentVO commentVO = new CommentVO(comment.getString("image_thumbnail"),
						((comment.getString("username").equals(""))?"Anonymous":comment.getString("username")),
						Utils.parseDate(comment.getLong("create_date"), RequestDetailsActivity.this),
						comment.getString("comment_text"));
				commentsList.add(commentVO);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		populateCommentList();
	}
	private void populateCommentList(){
		requestComments.setText(RequestDetailsActivity.this.getString(R.string.comments)+"("+commentsList.size()+")");
		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		adapter = new CommentsListViewAdapter(RequestDetailsActivity.this, commentsList, mLInflater, (ListView)findViewById(R.id.comment_listview));
		commentListView.setAdapter(adapter);
		commentListView.invalidateViews();
		((BaseAdapter)((ListView)findViewById(R.id.comment_listview)).getAdapter()).notifyDataSetChanged();
	}
	/**
	 * Click on follow button, follow/unfollow accordingly
	 * @param v
	 */
	public void followRequest(View v){
		postUserInteraction("request_follow");
	}
	/**
	 * Click on support button
	 * @param v
	 */
	public void supportRequest(View v){
		postUserInteraction("request_support");
	}
	/**
	 * Click on flag button
	 * @param v
	 */
	public void flagRequest(View v){
		postUserInteraction("request_flag");
	}

	private void postUserInteraction(String method){
		final String interaction = method.replace("request_", "");
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper(method), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.loadingDialog));
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
							updatedRequest = true;
							getRequestData(request_id);
						}
						else{
							if(parser.getStatus().getMessage().equals("Api Key not provided")){

								final CustomAlertDialog alert = new CustomAlertDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.login), "Please login to "+ interaction+ " a request", RequestDetailsActivity.this.getString(R.string.login), RequestDetailsActivity.this.getString(R.string.cancel));

								View.OnClickListener listener = new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										int id = v.getId();
										switch(id){
										case R.id.alertConfirm:
											Intent intent = new Intent(RequestDetailsActivity.this, CreateAccountActivity.class);
											startActivity(intent);
											alert.dismiss();
											finish();
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
								final CustomAlertDialog alert = new CustomAlertDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.ErrorPastTense), parser.getStatus().getMessage(), RequestDetailsActivity.this.getString(R.string.OkButton), null);

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
					}
					finally{
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				} else {
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
					Utils.noConnection(RequestDetailsActivity.this);
				}
			}
		};
		clientGet.addParameter("request_id", String.valueOf(request_id));
		if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		clientGet.execute("");
	}
	/**
	 * Back
	 * @param v
	 */
	public void back(View v){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putBoolean("updatedRequest", updatedRequest) ;
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
	/**
	 * Social share
	 * @param v
	 */
	public void shareRequest(View v){
		ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
		ImageDialogVO facebook = new ImageDialogVO(R.drawable.ic_light_facebook, RequestDetailsActivity.this.getString(R.string.socialFacebook), null, false);
		listToReturn.add(facebook);
		ImageDialogVO twitter = new ImageDialogVO(R.drawable.ic_light_twitter, RequestDetailsActivity.this.getString(R.string.socialTwitter), null, false);
		listToReturn.add(twitter);
		ImageDialogVO email = new ImageDialogVO(R.drawable.holo_light5_content_email, RequestDetailsActivity.this.getString(R.string.socialEmail), null, false);
		listToReturn.add(email);
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(RequestDetailsActivity.this.getString(R.string.shareRequest));
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
					try {
						dialog.dismiss();
						Config.configObject().setShouldUseFacebookConnect(true);
						Config.configObject().setFacebookAppId("285028994932615");
						AddThis.shareItem(RequestDetailsActivity.this, "facebook", request_url, request_title,
								"");
					} catch (ATDatabaseException e) {
						e.printStackTrace();
					} catch (ATSharerException e) {
						e.printStackTrace();
					}

					break;
				case 1:
					try {
						dialog.dismiss();
						AddThis.shareItem(RequestDetailsActivity.this, "twitter", request_url, request_title,
								"");
					} catch (ATDatabaseException e) {
						e.printStackTrace();
					} catch (ATSharerException e) {
						e.printStackTrace();
					}
					break;
				case 2:
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_SUBJECT, request_title);
					i.putExtra(Intent.EXTRA_TEXT, request_url);
					try {
						startActivity(Intent.createChooser(i, ""));
					} catch (android.content.ActivityNotFoundException ex) {
					}
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
	}
	/**
	 * see details
	 * @param v
	 */
	public void showInfo(View v){
		detailsView.setVisibility(View.VISIBLE);
		infoHighlight.setVisibility(View.VISIBLE);
		commentView.setVisibility(View.GONE);
		commentHighlight.setVisibility(View.INVISIBLE);
		mapLayout.setVisibility(View.VISIBLE);
		if(isFollowing) followRequest.setPressed(true);
		if(isSupporting) supportRequest.setPressed(true);
		if(isFlagged) flagRequest.setPressed(true);
	}
	
	public void makePrivateTask(){
		
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("request_private"), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.requestDetails));
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
				if(result.length()>0)  {
					try {
						JSONParser parser = new JSONParser(result);
						String resultTitle = parser.getStatus().getType();
						if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
							JSONObject request = parser.getResponse();
							//requestData = request;
							//populateRequestData(request);
							
							makePrivateRequest.setVisibility(View.INVISIBLE); //request_private
							requestIsPrivate.setVisibility(View.VISIBLE);
							
							//should probably show error responses
							
						}
						else{
                            String resultMessage = parser.getStatus().getMessage();

                            final CustomAlertDialog alert = new CustomAlertDialog(RequestDetailsActivity.this, resultTitle, resultMessage, RequestDetailsActivity.this.getString(R.string.OkButton), null);

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
					} catch (JSONException e) {
						e.printStackTrace();
					}
					finally{
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				} else {
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
					Utils.noConnection(RequestDetailsActivity.this);
				}
			}
		};
		clientGet.addParameter("request_id", String.valueOf(request_id));
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		
		if(app.getUserApiKey() != null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.execute("");
		
	}
	
	//will only be seen under certain circumstances to begin with
	public void makePrivate(View v){
		
		AlertDialog.Builder makePrivate = new AlertDialog.Builder(this);
		
		makePrivate.setTitle(getResources().getString(R.string.makePrivate))
		.setMessage(getResources().getString(R.string.makePrivateMessage))
		.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				makePrivateTask();
				
				//
			}

		})
		.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}

		})
		.create()
		.show();

	}
	
	/**
	 * see comments
	 * @param v
	 */
	public void showComments(View v){
		getCommentData(request_id);
		mapLayout.setVisibility(View.GONE);
		detailsView.setVisibility(View.GONE);
		infoHighlight.setVisibility(View.INVISIBLE);
		commentView.setVisibility(View.VISIBLE);
		commentHighlight.setVisibility(View.VISIBLE);
	}
	/**
	 * add image  to comment
	 * @param v
	 */
	public void commentImage(View v){
		ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
		ImageDialogVO takeImage = new ImageDialogVO(R.drawable.holo_light10_device_access_camera, RequestDetailsActivity.this.getString(R.string.takePhoto), null, false);
		listToReturn.add(takeImage);
		ImageDialogVO selectImage = new ImageDialogVO(R.drawable.holo_light5_content_picture, RequestDetailsActivity.this.getString(R.string.selectPhoto), null, false);
		listToReturn.add(selectImage);
		if(commentHasImage){
			ImageDialogVO deleteImage = new ImageDialogVO(R.drawable.holo_light5_content_discard, RequestDetailsActivity.this.getString(R.string.deletePhoto), null, false);
			listToReturn.add(deleteImage);
		}

		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vl = mLInflater.inflate(R.layout.item_list, null, false);
		Utils.useLatoInView(this, vl);
		dialog.setContentView(vl);
		TextView title = (TextView)dialog.findViewById(R.id.listTitle);
		title.setText(RequestDetailsActivity.this.getString(R.string.addImage));
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
					PackageManager pm = RequestDetailsActivity.this.getPackageManager();

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

						Toast toast = Toast.makeText(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.noCamera), duration);
						toast.show();

						toast.setGravity(Gravity.BOTTOM, 0, 0);
					}

				case 1:
					startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), ACTIVITY_SELECT_PHOTO);
					dialog.dismiss();
					break;
				case 2:
					pathToCommentImage = null;
					commentHasImage = false;
					commentImage.setImageResource(R.drawable.holo_light5_content_new_picture);
					commentImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					dialog.dismiss();
					break;
				default:
					dialog.dismiss();
					break;
				}
			}
		});
	}
	
	public void closeRequestTask(String publicPrivateString, String reasonConcatenated){
		HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("request_close"), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
			final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.requestDetails));
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
				if(result.length()>0)  {
					try {
						JSONParser parser = new JSONParser(result);
						String resultTitle = parser.getStatus().getType();
						if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
							JSONObject request = parser.getResponse();
							//requestData = request;
							//populateRequestData(request);
							
							requestStatus.setText(RequestDetailsActivity.this.getString(R.string.statusCompleted));
							requestStatusImage.setImageResource(R.drawable.ic_status_large_4);
							
							//update UI, hide close request button
							closeRequest.setVisibility(View.INVISIBLE);
							
						}
						else{
                            String resultMessage = parser.getStatus().getMessage();

                            final CustomAlertDialog alert = new CustomAlertDialog(RequestDetailsActivity.this, resultTitle, resultMessage, RequestDetailsActivity.this.getString(R.string.OkButton), null);

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
					} catch (JSONException e) {
						e.printStackTrace();
					}
					finally{
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
					}
				} else {
					if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
					Utils.noConnection(RequestDetailsActivity.this);
				}
			}
		};
		clientGet.addParameter("request_id", String.valueOf(request_id));
		clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
		clientGet.addParameter("is_checked", publicPrivateString);
		clientGet.addParameter("reason", reasonConcatenated);
		
		//need user id
		
		if(app.getUserApiKey() != null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
		clientGet.execute("");
	}
	
	/**
	 * close request
	 * @param v
	 */
	public void closeRequest(View v){
		
		//show dialog with options
		/*
		 *  "The request has been resolved"
			"There was an error in the original request"
			"The request is no longer applicable"
		 */
		//single select spinner
		items = new ArrayList<String>();
		//items.add
		items.add(getResources().getString(R.string.requestResolved));
		items.add(getResources().getString(R.string.requestError));
		items.add(getResources().getString(R.string.requestNA));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(RequestDetailsActivity.this, R.layout.custom_singleselect, items); 
	    
		adapter.setDropDownViewResource(R.layout.custom_singleselect);
		
		reasoningDialog = new Dialog(this);
		reasoningDialog.setContentView(R.layout.closerequestdialog);
		reasoningDialog.setCanceledOnTouchOutside(true);
		
		Spinner singleSelectSpinner = (Spinner)reasoningDialog.findViewById(R.id.reasonChoice);
		
		singleSelectSpinner.setAdapter(adapter);
		singleSelectSpinner.setVisibility(View.VISIBLE);
		singleSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
				//save stuff here
				reason = "";
				reason = items.get(arg2);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		reasoningDialog.show();
		
		//spinner will always have the first item selected?
		elaboration = (EditText)reasoningDialog.findViewById(R.id.elaborate);

		//append reason + getText
		
		publicPrivateCheckbox = (CheckBox)reasoningDialog.findViewById(R.id.publicCheckbox);
		//checkbox
		
		Button submissionButton = (Button)reasoningDialog.findViewById(R.id.submitButton);
		submissionButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				reasonAppend = elaboration.getText().toString();
				if(reason.contentEquals(""))
					reason = items.get(0);
				
				reasoningDialog.dismiss();
				//submit stuff, view asynctask
				closeRequestTask(String.valueOf(publicPrivateCheckbox.isChecked()), reason +". "+ reasonAppend);
				
			}
			
		});
	}
	
	//mark private
	
	/**
	 * submit comment
	 * @param v
	 */
	public void postComment(View v){
		String alertTitle = RequestDetailsActivity.this.getString(R.string.errorRequest);
		String alertMessage = "";
		if(commentText.getText().toString().equals("")) alertMessage += RequestDetailsActivity.this.getString(R.string.plsEnterComment);
		if(!alertMessage.equals("")){

			final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, RequestDetailsActivity.this.getString(R.string.OkButton), null);

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
		else{
			HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("comment_submit"), Utils.isNetworkAvailable(RequestDetailsActivity.this)){
				final CustomProgressDialog dialog = new CustomProgressDialog(RequestDetailsActivity.this, RequestDetailsActivity.this.getString(R.string.submitComment));
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
								updatedRequest = true;
								pathToCommentImage = null;
								commentHasImage = false;
								commentImage.setImageResource(R.drawable.holo_light5_content_new_picture);
								commentImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
								commentText.setText("");
								getCommentData(request_id);
							}
							else{
								resultMessage = parser.getStatus().getMessage();

								final CustomAlertDialog alert = new CustomAlertDialog(RequestDetailsActivity.this, resultTitle, resultMessage, RequestDetailsActivity.this.getString(R.string.OkButton), null);

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
						} catch (JSONException e) {
							e.printStackTrace();
						}
						finally{
							if(dialog != null && dialog.isShowing()){
								dialog.dismiss();
							}
						}
					} else {
						if(dialog != null && dialog.isShowing()){
							dialog.dismiss();
						}
						Utils.noConnection(RequestDetailsActivity.this);
					}
				}
			};
			//sent with every request
			clientPost.addParameter("comment", commentText.getText().toString());
			clientPost.addParameter("request_id", String.valueOf(request_id));
			//for images
			if(commentHasImage){
				clientPost.addParameter("has_image", "1");
				clientPost.addParameter("image", pathToCommentImage);
			}
			if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientPost.addParameter("api_key", app.getUserApiKey());
			clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
			clientPost.execute(new Void[0]);
		}
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
		protected Bitmap doInBackground(String... urls) {
			return loadImageFromNetwork(urls[0]);
		}

		protected void onPostExecute(Bitmap result) {
			requestImage.setImageBitmap(result);
		}
	}
	private Bitmap loadImageFromNetwork(String u) {
		Bitmap bmp = null;
		try{
			URL url;
			url = new URL(u);
			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

		}
		catch (IOException e){
			e.printStackTrace();
		}
		return bmp;
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

				pathToCommentImage = filePath;

				File selFile=new File(pathToCommentImage);

				Bitmap thumbnailBmp = Utils.decodeFile(selFile);
				commentImage.setImageBitmap(thumbnailBmp);
				commentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				commentHasImage = true;

			}else{
				Log.e(TAG, "data == null");
			}
			break;
		case ACTIVITY_TAKE_PHOTO:
			try{
				//System.gc();
				pathToCommentImage=Utils.getTempFile(getApplicationContext()).getAbsolutePath();
				Log.e(TAG, "filePath = " + pathToCommentImage);
				File takenFile=Utils.getTempFile(getApplicationContext());
				Bitmap thumbnailBmp = Utils.decodeFile(takenFile);
				commentImage.setImageBitmap(thumbnailBmp);
				commentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				commentHasImage = true;
				//System.gc();

			}
			catch(Exception exp){
				Log.e("TakePhoto", exp.getMessage());
			}
			break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
		if(isFollowing) followRequest.setPressed(true);
		if(isSupporting) supportRequest.setPressed(true);
		if(isFlagged) flagRequest.setPressed(true);
	}
	@Override
	protected void onStop() {
		FlurryAgent.endTimedEvent(this.getSharedPreferences("publicStuffPrefs", MODE_PRIVATE).getString("currentEvent", ""));
		FlurryAgent.onEndSession(RequestDetailsActivity.this);
		//System.gc();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		//System.gc();
		super.onDestroy();
	}
	@Override
	protected  void onRestart(){
		//System.gc();
		super.onRestart();
	}
	@Override
	protected  void onStart(){
		FlurryAgent.onStartSession(RequestDetailsActivity.this, "2C3QVVZMX8Q5M6KF3458");
		//System.gc();
		super.onStart();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		if(requestData!=null){
			savedInstanceState.putString("requestData", requestData.toString());
			savedInstanceState.putSerializable("commentList", commentsList);
			savedInstanceState.putBoolean("detailsVisible", (detailsView.getVisibility() == View.VISIBLE));
			savedInstanceState.putBoolean("commentsVisible", (commentView.getVisibility() == View.VISIBLE));
			savedInstanceState.putString("commentText", commentText.getText().toString());
			savedInstanceState.putString("pathToCommentImage", pathToCommentImage);
			savedInstanceState.putInt("requestId", request_id);
			savedInstanceState.putBoolean("updatedRequest", updatedRequest);
		}

	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		if(savedInstanceState.get("requestData")!=null){
			try{
				requestData =   new JSONObject(savedInstanceState.getString("requestData"));
				populateRequestData(requestData);
			}
			catch(JSONException e){}
			commentsList = (ArrayList<CommentVO>)savedInstanceState.getSerializable("commentList");
			populateCommentList();
			if(savedInstanceState.getBoolean("detailsVisible")){
				detailsView.setVisibility(View.VISIBLE);
				infoHighlight.setVisibility(View.VISIBLE);
				commentView.setVisibility(View.GONE);
				commentHighlight.setVisibility(View.INVISIBLE);
				if(isFollowing) followRequest.setPressed(true);
				if(isSupporting) supportRequest.setPressed(true);
				if(isFlagged) flagRequest.setPressed(true);
			}
			if(savedInstanceState.getBoolean("commentsVisible")){
				detailsView.setVisibility(View.GONE);
				infoHighlight.setVisibility(View.INVISIBLE);
				commentView.setVisibility(View.VISIBLE);
				commentHighlight.setVisibility(View.VISIBLE);
			}
			if(savedInstanceState.getString("commentText")!=null){
				commentText.setText(savedInstanceState.getString("commentText"));
			}
			if(savedInstanceState.getString("pathToCommentImage")!=null){
				pathToCommentImage =  savedInstanceState.getString("pathToCommentImage");
				File selFile=new File(pathToCommentImage);
				Bitmap thumbnailBmp = Utils.decodeFile(selFile);
				commentImage.setImageBitmap(thumbnailBmp);
				commentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
				commentHasImage = true;
			}
			request_id = savedInstanceState.getInt("requestId");
			updatedRequest = savedInstanceState.getBoolean("updatedRequest");
		}
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

	
	
	private void setUpMap() {
		/*

		Location userLocation = new Location("passedLocation");
		userLocation.setLatitude(latitude);
		userLocation.setLongitude(longitude);
		userLocation.setAccuracy(100);
		mListener.onLocationChanged(userLocation);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.getUiSettings().setZoomGesturesEnabled(false);
		mMap.getUiSettings().setTiltGesturesEnabled(false);
		mMap.getUiSettings().setScrollGesturesEnabled(false);
		mMap.getUiSettings().setCompassEnabled(false);
		mMap.getUiSettings().setRotateGesturesEnabled(false);
		//mMap.getUiSettings().setAllGesturesEnabled(false);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setLocationSource(this);
	*/}
	
	
}

