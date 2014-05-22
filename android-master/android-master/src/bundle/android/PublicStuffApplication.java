package bundle.android;

import android.app.Application;
import android.graphics.Color;
import android.os.StrictMode;
import bundle.android.model.vo.*;
import bundle.android.utils.DataStore;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpPostSender;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportsCrashes(formKey = "")//(formKey = "dE9RT2dWeE5hMWRlRXFqQzRfV3pxdlE6MQ")

public class PublicStuffApplication extends Application {
    private DataStore store;
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        store = new DataStore(this);
        
        
        Map<ReportField, String> mapping = new HashMap<ReportField, String>();
        
        //mapping.put(ReportField., "Timestamp"); //do date_created on server database
        mapping.put(ReportField.REPORT_ID, "report_id");
        mapping.put(ReportField.APP_VERSION_CODE, "app_version_code");
        mapping.put(ReportField.APP_VERSION_NAME, "app_version_name");
        mapping.put(ReportField.PACKAGE_NAME, "package_name");
        mapping.put(ReportField.FILE_PATH, "file_path");
        mapping.put(ReportField.PHONE_MODEL, "phone_model");
        mapping.put(ReportField.BRAND, "brand");
        mapping.put(ReportField.PRODUCT, "product");
        mapping.put(ReportField.ANDROID_VERSION, "android_version");
        mapping.put(ReportField.BUILD, "build");
        mapping.put(ReportField.TOTAL_MEM_SIZE, "total_mem_size");
        mapping.put(ReportField.AVAILABLE_MEM_SIZE, "available_mem_size");
        mapping.put(ReportField.CUSTOM_DATA, "custom_data");
        mapping.put(ReportField.IS_SILENT, "is_silent");
        mapping.put(ReportField.STACK_TRACE, "stack_trace");
        mapping.put(ReportField.INITIAL_CONFIGURATION, "initial_configuration");
        mapping.put(ReportField.CRASH_CONFIGURATION, "crash_configuration");
        mapping.put(ReportField.DISPLAY, "display");
        mapping.put(ReportField.USER_COMMENT, "user_comment");
        mapping.put(ReportField.USER_EMAIL, "user_email");
        mapping.put(ReportField.USER_APP_START_DATE, "user_app_start_date");
        mapping.put(ReportField.USER_CRASH_DATE, "user_crash_date");
        mapping.put(ReportField.DUMPSYS_MEMINFO, "dumpsys_meminfo");
        mapping.put(ReportField.DROPBOX, "dropbox");
        mapping.put(ReportField.LOGCAT, "logcat");
        mapping.put(ReportField.EVENTSLOG, "eventslog");
        mapping.put(ReportField.RADIOLOG, "radiolog");
        mapping.put(ReportField.DEVICE_ID, "device_id");
        mapping.put(ReportField.INSTALLATION_ID, "installation_id");
        mapping.put(ReportField.DEVICE_FEATURES, "device_features");
        mapping.put(ReportField.ENVIRONMENT, "environment");
        mapping.put(ReportField.SHARED_PREFERENCES, "shared_preferences");
        mapping.put(ReportField.SETTINGS_SYSTEM, "settings_system");
        mapping.put(ReportField.SETTINGS_SECURE, "settings_secure");
        
        HttpPostSender pSender = new HttpPostSender(PublicStuff.BASE_URL + "mobile_crash", mapping);
        ACRA.getErrorReporter().setReportSender(pSender);
       
        super.onCreate();
    }
    //User Related
    private long userLastLogin;

    public void setUserApiKey(String apiKey){
        store.saveToPrefs("USER_API_KEY", apiKey);
    }
    public String getUserApiKey(){
        return store.getFromPrefs("USER_API_KEY", "");
    }
    
    public void setUserGCM(String regId){
    	store.saveToPrefs("GCM_KEY", regId);
    }
    
    public String getUserGCM(){
    	return store.getFromPrefs("GCM_KEY", "");
    }
    
    public void setUserLastLogin(long login){
        userLastLogin = login;
    }
    public long getUserLastLogin(){
        return  userLastLogin;
    }
    
    public void setUserData(UserVO userVO){
        store.saveExternalUserData(userVO);
    }
    
    public void setUserName(String username){
    	store.saveToPrefs("username", username);
    }
    
    public String getUserName()
    {
    	return store.getFromPrefs("username", "");	
    }
    
    public void setLocale(String locale){
    	store.saveToPrefs("locale", locale);
    }
    
    public String getLocale()
    {
    	return store.getFromPrefs("locale", "");
    }
    
    public UserVO getUserData(){
        return  store.getExternalUserData();
    }
    public void setUserDrafts(ArrayList<RequestDraftVO> requestDraftVOs){
        store.saveExternalRequestDraft(requestDraftVOs);
        for(RequestDraftVO draft : requestDraftVOs){
            if(draft.getCustomFields()!=null && draft.getUniqueID()!=0){
                store.saveExternalDraftCustomFields(draft.getCustomFields(), draft.getUniqueID());
            }
        }
    }
    public ArrayList<RequestDraftVO> getUserDrafts(){
        ArrayList<RequestDraftVO> draftList = store.getExternalRequestDraft();
        int i =0;
        for(RequestDraftVO draft : draftList){
            if(draft.getUniqueID()!=0){
                draft.setCustomFields(store.getExternalDraftCustomFields(draft.getUniqueID()));
                draftList.set(i, draft);
            }
            i++;
        }
        return  draftList;
    }
    public void  setUserNotifications(ArrayList<NotificationVO> notificationVOs){
        store.saveExternalNotificationData(notificationVOs);
    }
    public ArrayList<NotificationVO> getUserNotifications(){
        return  store.getExternalNotificationData();
    }
    public void setCitySpaceId (int spaceId){
        store.saveToPrefs("CITY_SPACE_ID", spaceId);
    }
    public int getCitySpaceId (){
        if(!PublicStuff.IS_CLIENT_APP){
            return store.getFromPrefs("CITY_SPACE_ID", 0);
        }
        else{
           return PublicStuff.CITY_SPACE_ID;
        }
    }
    public void setCityClientId(int clientId){
        store.saveToPrefs("CITY_CLIENT_ID", clientId);
    }
    public int getCityClientId(){
        return store.getFromPrefs("CITY_CLIENT_ID", 0);
    }
    public void setCityName(String name){
        store.saveToPrefs("CITY_NAME", name);
    }
    public String getCityName(){
        return store.getFromPrefs("CITY_NAME", "");
    }
    public void setCityBaseColor(String color){
        store.saveToPrefs("CITY_BASE_COLOR", color);
    }
    public  String getCityBaseColor(){
        return store.getFromPrefs("CITY_BASE_COLOR", "#232323");
    }
    public int getNavColor(){
       return  Color.parseColor(getCityBaseColor());
    }
    public void setCityUserFollowing(boolean following){
        store.saveToPrefs("CITY_USER_FOLLOWING", following);
    }
    public boolean getCityUserFollowing(){
        return store.getFromPrefs("CITY_USER_FOLLOWING", false);
    }
    public void setCityIcon(String icon){
        store.saveToPrefs("CITY_ICON", icon);
    }
    public String getCityIcon(){
        return store.getFromPrefs("CITY_ICON", "");
    }
    public void setCityAbout(String about){
        store.saveToPrefs("CITY_ABOUT", about);
    }
    public String getCityAbout(){
        return  store.getFromPrefs("CITY_ABOUT", "");
    }
    public void setCityBanner(String banner){
        store.saveToPrefs("CITY_BANNER", banner);
    }
    public String getCityBanner(){
        return store.getFromPrefs("CITY_BANNER", "");
    }
    public void setCityLat(double lat){
        store.saveToPrefs("CITY_LAT", (float) lat);
    }
    public double getCityLat(){
        return store.getFromPrefs("CITY_LAT", 0f);
    }
    public void setCityLon(double lon){
        store.saveToPrefs("CITY_LON", (float) lon);
    }
    public double getCityLon(){
        return store.getFromPrefs("CITY_LON", 0f);
    }
    public void setCityTagline(String tagline){
        store.saveToPrefs("CITY_TAGLINE", tagline);
    }
    public String getCityTagline(){
        return store.getFromPrefs("CITY_TAGLINE", "");
    }
    public void setCityWidgets(ArrayList<WidgetVO> widgetVOs){
        store.saveExternalWidgetList(widgetVOs);
    }
    public ArrayList<WidgetVO> getCityWidgets(){
        return store.getExternalWidgetList();
    }
    public void setCityUrl(String url){
        store.saveToPrefs("CITY_URL", url);
    }
    public String getCityUrl(){
        return store.getFromPrefs("CITY_URL", "");
    }
    public void setCitySubmittedRequests(int requests){
        store.saveToPrefs("CITY_SUBMITTED_REQUESTS", requests);
    }
    public int getCitySubmittedRequests(){
        return store.getFromPrefs("CITY_SUBMITTED_REQUESTS", 0);
    }
    public void setCityCompletedRequests(int requests){
        store.saveToPrefs("CITY_COMPLETED_REQUESTS", requests);
    }
    public int getCityCompletedRequests(){
        return store.getFromPrefs("CITY_COMPLETED_REQUESTS", 0);
    }
    public void setCityAppName(String appName){
        store.saveToPrefs("CITY_APP_NAME", appName);
    }
    public String getCityAppName(){
        return store.getFromPrefs("CITY_APP_NAME", "");
    }
    public void setNearbyRequests(ArrayList<RequestListVO> requests){
        store.saveExternalRequestList(requests, "nearby_requests");
    }
    public ArrayList<RequestListVO> getNearbyRequests(){
        return store.getExternalRequestList("nearby_requests");
    }
    public void setMyRequests(ArrayList<RequestListVO> requests){
        store.saveExternalRequestList(requests, "my_requests");
    }
    public ArrayList<RequestListVO> getMyRequests(){
        return store.getExternalRequestList("my_requests");
    }
    public void setFollowedRequests(ArrayList<RequestListVO> requests){
        store.saveExternalRequestList(requests, "followed_requests");
    }
    public ArrayList<RequestListVO> getFollowedRequests(){
        return store.getExternalRequestList("followed_requests");
    }
    public void setCommentedRequests(ArrayList<RequestListVO> requests){
        store.saveExternalRequestList(requests, "commented_requests");
    }
    public ArrayList<RequestListVO> getCommentedRequests(){
        return store.getExternalRequestList("commented_requests");
    }
    public void setVotedRequests(ArrayList<RequestListVO> requests){
        store.saveExternalRequestList(requests, "voted_requests");
    }
    public ArrayList<RequestListVO> getVotedRequests(){
        return store.getExternalRequestList("voted_requests");
    }

    public File getCurrCacheDir(){
        File cachedir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            cachedir = new File(android.os.Environment.getExternalStorageDirectory(),"PublicStuff");
        }
        else{
            cachedir = getCacheDir();
        }

        if(!cachedir.exists())
            cachedir.mkdirs();
        return  cachedir;
    }

}
