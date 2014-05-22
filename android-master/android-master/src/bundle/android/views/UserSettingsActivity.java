package bundle.android.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import bundle.android.PublicStuff;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.SingleListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.model.vo.*;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.wassabi.psmobile.R;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.*;

public class UserSettingsActivity extends PsActivity {
    private PublicStuffApplication app;
    private EditText username;
    private EditText firstname;
    private EditText lastname;
    private EditText email;
    private EditText phone;
    private EditText homeAddress;
    private EditText workAddress;
    private TextView updatesSent;
    private TextView pushUpdatesSent;
    private ToggleButton emailReplies;
    private ToggleButton pushReplies;
    
    private HashMap<String, String> userSettings = new HashMap<String, String>();
    private final HashMap<String, String> updatedUserSettings = new HashMap<String, String>();
    private int updateSetting = 0;
    private List<String> updateTypes;
    private List<String> pushUpdateTypes;
    private List<String> updateKeys;
    private boolean fieldsChanged = false;
    private Bundle b;
    DataStore appStore;
    Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        context = this;
        appStore = new DataStore(app);
        setContentView(R.layout.usersettings);
        //Utils.useLatoInView(this, this.findViewById(android.R.id.content));
 
        FlurryAgent.logEvent("User Settings View");
        appStore.saveToPrefs("currentEvent", "User Settings View");
        
        if(getIntent()!=null){
        	b = getIntent().getExtras();
        	userSettings = (HashMap<String, String>)b.getSerializable("userSettings");
        }
        initElements();
    }

    private void initElements(){
        RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
        headerLayoutView.setBackgroundColor(app.getNavColor());

        
        
        updateTypes = Arrays.asList(new String[]{UserSettingsActivity.this.getString(R.string.typeChangeStatus), /*UserSettingsActivity.this.getString(R.string.typeIsClosed),*/ UserSettingsActivity.this.getString(R.string.typeEmailMe)});
        pushUpdateTypes = Arrays.asList(new String[]{UserSettingsActivity.this.getString(R.string.typeChangeStatus), /*UserSettingsActivity.this.getString(R.string.typeIsClosed),*/ UserSettingsActivity.this.getString(R.string.typePushMe)});
        updateKeys  = Arrays.asList(new String[]{"status", /*"closed",*/ "none"});
        
        ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
        if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
            AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
            imageLoader.execute(new Void[0]);
        }
        else{
            cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
        }
        username = (EditText)this.findViewById(R.id.username);
        if(userSettings.containsKey("username"))username.setText(userSettings.get("username"));
        username.addTextChangedListener(new EditTextListener());

        firstname = (EditText)this.findViewById(R.id.firstname);
        if(userSettings.containsKey("firstname"))firstname.setText(userSettings.get("firstname"));
        firstname.addTextChangedListener(new EditTextListener());

        lastname = (EditText)this.findViewById(R.id.lastname);
        if(userSettings.containsKey("lastname"))lastname.setText(userSettings.get("lastname"));
        lastname.addTextChangedListener(new EditTextListener());

        email = (EditText)this.findViewById(R.id.email);
        if(userSettings.containsKey("email"))email.setText(userSettings.get("email"));
        email.addTextChangedListener(new EditTextListener());

        phone = (EditText)this.findViewById(R.id.phone);
        if(userSettings.containsKey("phone"))phone.setText(userSettings.get("phone"));
        phone.addTextChangedListener(new EditTextListener());

        homeAddress = (EditText)this.findViewById(R.id.homeAddress);
        if(userSettings.containsKey("home_address"))homeAddress.setText(userSettings.get("home_address"));

        workAddress = (EditText)this.findViewById(R.id.workAddress);
        if(userSettings.containsKey("work_address"))workAddress.setText(userSettings.get("work_address"));

        TextView password = (TextView) this.findViewById(R.id.password);
        String pw ="************";
        password.setText(pw);

        updatesSent = (TextView)this.findViewById(R.id.updatesSent);
        updatesSent.setText(UserSettingsActivity.this.getString(R.string.changesStatus));
        if(userSettings.containsKey("updates_sent")){
            updatesSent.setText(updateTypes.get(updateKeys.indexOf(userSettings.get("updates_sent"))));
            updateSetting =  updateKeys.indexOf(userSettings.get("updates_sent"));
        }

        emailReplies = (ToggleButton)this.findViewById(R.id.emailReplies);
        emailReplies.setChecked(appStore.getFromPrefs(userSettings.get("username")+"emailComment", false));
        if(userSettings.containsKey("email_on_comment") && userSettings.get("email_on_comment").equals("1"))
        {
        	emailReplies.setChecked(true);
        	appStore.saveToPrefs(userSettings.get("username")+"emailComment", true);
        }
        emailReplies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                fieldsChanged = true;
                if(userSettings.containsKey("username"))
                	appStore.saveToPrefs(userSettings.get("username")+"emailComment", b?true:false);
            	else
            		appStore.saveToPrefs("emailComment", b?true:false);
            }
        });
        
        pushUpdatesSent = (TextView)this.findViewById(R.id.pushUpdatesSent);
        pushUpdatesSent.setText(UserSettingsActivity.this.getString(R.string.changesStatus));
        if(!appStore.getFromPrefs(app.getUserApiKey()+"pushPref", "").contentEquals("")){ //default value is ""?
        	if(pushUpdateTypes.indexOf(appStore.getFromPrefs(app.getUserApiKey()+"pushPref", ""))>-1)
        	{
        		pushUpdatesSent.setText(appStore.getFromPrefs(app.getUserApiKey()+"pushPref", ""));
        	}
        }

        pushReplies = (ToggleButton)this.findViewById(R.id.pushReplies);
        pushReplies.setChecked(appStore.getFromPrefs(app.getUserApiKey()+"pushComment", true)); //true is a default value for sharedpreferences key, it will return whatever the real boolean value is
        pushReplies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                	appStore.saveToPrefs(app.getUserApiKey()+"pushComment", b?true:false);
            }
        });

        LinearLayout changeLocationLayout = (LinearLayout) this.findViewById(R.id.changeLocationLayout);
        if(PublicStuff.IS_CLIENT_APP) changeLocationLayout.setVisibility(View.GONE);
        else changeLocationLayout.setVisibility(View.VISIBLE);

        TextView currCity = (TextView) this.findViewById(R.id.currCity);
        currCity.setText(app.getCityAppName());

        TextView currLang = (TextView)this.findViewById(R.id.currLanguage);
        currLang.setText(Locale.getDefault().getDisplayLanguage());
    }
    public void changePassword(View v){
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }
    
    public void changeLanguage(View v){
    	//make popup dialog to select from
    	
    	
        
    	final String[] languages = {UserSettingsActivity.this.getString(R.string.arabicLanguage),
    			UserSettingsActivity.this.getString(R.string.germanLanguage),
    			UserSettingsActivity.this.getString(R.string.greekLanguage),
    			UserSettingsActivity.this.getString(R.string.englishLanguage),
    			UserSettingsActivity.this.getString(R.string.spanishLanguange),
    			UserSettingsActivity.this.getString(R.string.frenchLanguage),
    			UserSettingsActivity.this.getString(R.string.hindiLanguage),
    			UserSettingsActivity.this.getString(R.string.italianLanguage),
    			UserSettingsActivity.this.getString(R.string.japaneseLanguage),
    			UserSettingsActivity.this.getString(R.string.koreanLanguage),
    			UserSettingsActivity.this.getString(R.string.polishLanguage),
    			UserSettingsActivity.this.getString(R.string.portugueseLanguage),
    			UserSettingsActivity.this.getString(R.string.russianLanguage),
    			UserSettingsActivity.this.getString(R.string.serbianLanguage),
    			UserSettingsActivity.this.getString(R.string.turkishLanguage),
    			UserSettingsActivity.this.getString(R.string.vietnameseLanguage),
    			UserSettingsActivity.this.getString(R.string.chineseLanguage)};
    	
    	/*
    	 * AR - Arabic
    	 * DE - German
    	 * EL - Greek
    	 * EN - English
    	 * ES - Espanol
    	 * FR - French
    	 * HI - Hindi
    	 * IT - Italian
    	 * JA - Japanese
    	 * KO - Korean
    	 * PL - Polish
    	 * PT - Portuguese
    	 * RU - Russian
    	 * SR - Serbian?
    	 * TR - Turkish
    	 * VI - Vietnamese
    	 * ZH - Chinese 
    	 * 
    	 */
        		
        
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingsActivity.this);
        builder.setTitle(R.string.selectLanguage)
               .setItems(languages, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int which) {
            		   // The 'which' argument contains the index position
            		   // of the selected item
            		   Locale locale; 
            		   switch(which){
            		   case 0:
            			   locale = new Locale("ar");
            			   break;
            		   case 1:
            			   locale = new Locale("de");
            			   break;
            		   case 2:
            			   locale = new Locale("el");
            			   break;
            		   case 3:
            			   locale = new Locale("en");
            			   break;
            		   case 4:
            			   locale = new Locale("es");
            			   break;
            		   case 5:
            			   locale = new Locale("fr");
            			   break;
            		   case 6:
            			   locale = new Locale("hi");
            			   break;
            		   case 7:
            			   locale = new Locale("it");
            			   break;
            		   case 8:
            			   locale = new Locale("ja");
            			   break;
            		   case 9:
            			   locale = new Locale("ko");
            			   break;
            		   case 10:
            			   locale = new Locale("pl");
            			   break;
            		   case 11:
            			   locale = new Locale("pt");
            			   break;
            		   case 12:
            			   locale = new Locale("ru");
            			   break;
            		   case 13:
            			   locale = new Locale("sr");
            			   break;
            		   case 14:
            			   locale = new Locale("tr");
            			   break;
            		   case 15:
            			   locale = new Locale("vi");
            			   break;
            		   case 16:
            			   locale = new Locale("zh");
            			   break;
            		   /*case 0:
            			   locale = new Locale("en"); 
            			   break;
            		   case 1:
            			   locale = new Locale("fr"); 
            			   break;
            		   case 2:
            			   locale = new Locale("es");
            			   break;
            			   */
            		   default:
            			   locale = context.getResources().getConfiguration().locale;
            			   break;
            		   }   
            		   Locale[] locales = Locale.getAvailableLocales();
            		   int localesLength = locales.length;
            		   boolean localeAvailable = false;
            		   for(int i = 0; i < localesLength; i++)
            		   {
            			   if(locale.getDisplayLanguage().contentEquals(locales[i].getDisplayLanguage()))
            			   {
            				   Locale.setDefault(locale);
            				   localeAvailable = true;
            				   break;
            			   }
            		   }
            		   //if locale is in list of available locales, then set default, if not say "Device doesn't have this locale"
            		   if(localeAvailable == false)
            		   {
            			   Toast.makeText(context, UserSettingsActivity.this.getString(R.string.languageUnavailable), Toast.LENGTH_SHORT).show();
            		   }
            		   else
            		   {
            			   Configuration config = new Configuration();
                           config.locale = locale;
                           getBaseContext().getResources().updateConfiguration(config, 
                           getBaseContext().getResources().getDisplayMetrics());
                           
                           finish();
                           startActivity(getIntent());
            		   }
                       
                       
               }
        });
        builder.create();
        builder.show();
        
    }

    public void changeUpdates(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);

        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(UserSettingsActivity.this.getString(R.string.emailMeRequest));

        dialog.show();
        final SingleListViewAdapter adapter = new SingleListViewAdapter(this, updateTypes, mLInflater, updateSetting);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                updatesSent.setText(updateTypes.get(position));
                updateSetting = position;
                fieldsChanged = true;
                dialog.dismiss();
            }
        });
    }
    
    public void changePushUpdates(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);

        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(UserSettingsActivity.this.getString(R.string.pushMeWhen));

        dialog.show();
        final SingleListViewAdapter adapter = new SingleListViewAdapter(this, pushUpdateTypes, mLInflater, updateSetting);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                pushUpdatesSent.setText(pushUpdateTypes.get(position));
                appStore.saveToPrefs(app.getUserApiKey()+"pushPref", pushUpdateTypes.get(position));
                //updateSetting = position;
                //fieldsChanged = true;
                dialog.dismiss();
            }
        });
    }

    public void changeCities(View v){
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
        Intent mainActivityIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivityIntent);
        b.putSerializable("userSettings", userSettings);
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void logOut(View v){
        if(app.getUserDrafts().size()>0){
            final CustomAlertDialog dialog = new CustomAlertDialog(this, UserSettingsActivity.this.getString(R.string.logOut), UserSettingsActivity.this.getString(R.string.logOutWarning), UserSettingsActivity.this.getString(R.string.logOut), UserSettingsActivity.this.getString(R.string.cancel));

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int id = v.getId();
                    switch(id){
                        case R.id.alertConfirm:
                            logOutUser();
                            dialog.dismiss();
                            break;
                        case R.id.alertCancel:
                            dialog.dismiss();
                        default:
                            break;
                    }
                }
            };
            dialog.setListener(listener);
            dialog.show();
        }
        else logOutUser();
    }

    private void logOutUser(){
        Calendar startCal = Calendar.getInstance();
        startCal.set(2000,0, 1, 0, 0);
        app.setCityUserFollowing(false);
        app.setUserApiKey("");
        app.setUserLastLogin(startCal.getTime().getTime());

        ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
        requestDraftVOs.clear();
        app.setUserDrafts(requestDraftVOs);

        ArrayList<NotificationVO> notificationVOs = app.getUserNotifications();
        notificationVOs.clear();
        app.setUserNotifications(notificationVOs);

        ArrayList<RequestListVO> requestListVOs = app.getNearbyRequests();
        requestListVOs.clear();
        app.setNearbyRequests(requestListVOs);
        app.setMyRequests(requestListVOs);
        app.setFollowedRequests(requestListVOs);
        app.setCommentedRequests(requestListVOs);
        app.setVotedRequests(requestListVOs);

        UserVO userVO= new UserVO();
        app.setUserData(userVO);

        HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_logout"), Utils.isNetworkAvailable(UserSettingsActivity.this)){
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
        					app.setUserApiKey(parser.getResponse().getString("api_key"));
        					if(parser.getResponse().has("city_follows")){
        						JSONArray cities = parser.getResponse().getJSONArray("city_follows");
        						for(int i=0; i<cities.length(); i++){
        							int id = cities.getJSONObject(i).getInt("city_follow");
        							if(id == app.getCitySpaceId()) app.setCityUserFollowing(true);
        						}
        					}
        					Intent broadcastIntent = new Intent();
        			        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        			        sendBroadcast(broadcastIntent);
        			        Intent mainActivityIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
        			        startActivity(mainActivityIntent);
        			        finish();
        				}
        				else{
        					resultMessage = parser.getStatus().getMessage();
        					Intent broadcastIntent = new Intent();
        			        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        			        sendBroadcast(broadcastIntent);
        			        Intent mainActivityIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
        			        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        			        startActivity(mainActivityIntent);
        			        finish();

        				}//end else

        				//end try
        			} catch (JSONException e) {
        				e.printStackTrace();
        				Intent broadcastIntent = new Intent();
        		        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        		        sendBroadcast(broadcastIntent);
        		        Intent mainActivityIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
        		        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		        startActivity(mainActivityIntent);
        		        finish();
        			}
        		}//end if
        		else
        		{
        			Intent broadcastIntent = new Intent();
    		        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
    		        sendBroadcast(broadcastIntent);
    		        Intent mainActivityIntent = new Intent(UserSettingsActivity.this, MainActivity.class);
    		        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		        startActivity(mainActivityIntent);
    		        finish();
        		}
        	}//end onPostExecute();
        };

        String regId = GCMRegistrar.getRegistrationId(UserSettingsActivity.this);
    	if (!regId.equals("")){
    		 clientPost.addParameter("device_token", regId);
    		 clientPost.addParameter("push_system", "android");
    	}
        
        GCMRegistrar.unregister(UserSettingsActivity.this);
    	
        if(Build.PRODUCT != null) clientPost.addParameter("device_model", Build.PRODUCT);
        if(Build.VERSION.RELEASE !=null) clientPost.addParameter("device_os", Build.VERSION.RELEASE);
        clientPost.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
        clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientPost.execute(new Void[0]);
        
    }

    public void clickSubmit(View v){
       if(fieldsChanged){
           String alertTitle = UserSettingsActivity.this.getString(R.string.errorRequest);
           String alertMessage = "";
           if(username.getText().toString().equals("")) alertMessage += UserSettingsActivity.this.getString(R.string.usernameWarning);
           if(email.getText().toString().equals("")) alertMessage += ((alertMessage.length()>0)?"\n":"") + UserSettingsActivity.this.getString(R.string.emailWarning) ;
           if(!alertMessage.equals("")){

               final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, UserSettingsActivity.this.getString(R.string.OkButton), null);

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
               if(!username.getText().toString().equals(userSettings.get("username"))){
                   updatedUserSettings.put("username",username.getText().toString());
                   userSettings.put("username", username.getText().toString());
               }
               if(!firstname.getText().toString().equals(userSettings.get("firstname"))){
                   updatedUserSettings.put("firstname",firstname.getText().toString());
                   userSettings.put("firstname", firstname.getText().toString());
               }

               if(!lastname.getText().toString().equals(userSettings.get("lastname"))){
                   updatedUserSettings.put("lastname",lastname.getText().toString());
                   userSettings.put("lastname", lastname.getText().toString());
               }
               if(!email.getText().toString().equals(userSettings.get("email"))){
                   updatedUserSettings.put("email",email.getText().toString());
                   userSettings.put("email", email.getText().toString());
               }
               if(!phone.getText().toString().equals(userSettings.get("phone"))){
                   updatedUserSettings.put("phone",phone.getText().toString());
                   userSettings.put("phone", phone.getText().toString());
               }
               if(!phone.getText().toString().equals(userSettings.get("phone"))){
                   updatedUserSettings.put("phone",phone.getText().toString());
                   userSettings.put("phone", phone.getText().toString());
               }
               if(!homeAddress.getText().toString().equals(userSettings.get("home_address"))){
                   updatedUserSettings.put("home_address",homeAddress.getText().toString());
                   userSettings.put("home_address", homeAddress.getText().toString());
               }
               if(!workAddress.getText().toString().equals(userSettings.get("work_address"))){
                   updatedUserSettings.put("work_address",workAddress.getText().toString());
                   userSettings.put("work_address", workAddress.getText().toString());
               }
               if(!updateKeys.get(updateSetting).equals(userSettings.get("updates_sent"))){
                   updatedUserSettings.put("updates_sent", updateKeys.get(updateSetting));
                   userSettings.put("updates_sent", updateKeys.get(updateSetting));
               }
               String emailReplySetting =(emailReplies.isChecked())?"1":"0";
               if(!equals(userSettings.get("email_on_comment"))){
                   updatedUserSettings.put("email_on_comment", emailReplySetting);
                   userSettings.put("email_on_comment", emailReplySetting);
               }

               updateUser();
           }
       }
       else{
           final CustomAlertDialog alert = new CustomAlertDialog(UserSettingsActivity.this, UserSettingsActivity.this.getString(R.string.thankyou), UserSettingsActivity.this.getString(R.string.settingsSaved), UserSettingsActivity.this.getString(R.string.OkButton), null);

           View.OnClickListener listener = new View.OnClickListener()
           {
               @Override
               public void onClick(View v)
               {
                   int id = v.getId();
                   switch(id){
                       case R.id.alertConfirm:
                           b.putSerializable("userSettings", userSettings);
                           Intent intent = new Intent();
                           intent.putExtras(b);
                           setResult(RESULT_OK, intent);
                           alert.dismiss();
                           finish();
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

    private void updateUser(){
        final CustomProgressDialog dialog = new CustomProgressDialog(UserSettingsActivity.this, UserSettingsActivity.this.getString(R.string.savingSettings));
        dialog.show();
        HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_update"), Utils.isNetworkAvailable(UserSettingsActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result)
            {
                dialog.dismiss();
                if(result.length()>0){
                    try {
                        JSONParser parser = new JSONParser(result);
                        if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                            String title = UserSettingsActivity.this.getString(R.string.thankyou);
                            String message = UserSettingsActivity.this.getString(R.string.settingsSaved);
                            final CustomAlertDialog alert = new CustomAlertDialog(UserSettingsActivity.this,title, message, UserSettingsActivity.this.getString(R.string.OkButton), null);

                            View.OnClickListener listener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    int id = v.getId();
                                    switch(id){
                                        case R.id.alertConfirm:
                                            b.putSerializable("userSettings", userSettings);
                                            Intent intent = new Intent();
                                            intent.putExtras(b);
                                            setResult(RESULT_OK, intent);
                                            alert.dismiss();
                                            finish();
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
                            String title = UserSettingsActivity.this.getString(R.string.ErrorPastTense);
                            String message = parser.getStatus().getMessage();
                            final CustomAlertDialog alert = new CustomAlertDialog(UserSettingsActivity.this,title, message, UserSettingsActivity.this.getString(R.string.OkButton), null);

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
                } else Utils.noConnection(UserSettingsActivity.this);
            }
        };
        Set<Map.Entry<String, String>> set = updatedUserSettings.entrySet();
        clientPost.addParameter("api_key", app.getUserApiKey());
        for (Map.Entry<String, String> entry : set) {
            clientPost.addParameter(entry.getKey(), entry.getValue());
        }
        clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientPost.execute(new Void[0]);
    }
    public void back(View v){
        b.putSerializable("userSettings", userSettings);
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }
    private class EditTextListener implements TextWatcher {
        public void afterTextChanged(Editable s) {
           fieldsChanged = true;
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    }
    @Override
    protected void onPause() {
       //System.gc();
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
       //System.gc();
        super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
     
      
    }
    
}
