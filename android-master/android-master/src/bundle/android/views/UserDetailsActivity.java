package bundle.android.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.UserDetailsListAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.NotificationVO;
import bundle.android.model.vo.UserDetailVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class UserDetailsActivity extends Activity {
    private PublicStuffApplication app;
    private TextView requestRank;
    private TextView commentRank;
    private ListView userDetailsList;

    private final ArrayList<UserDetailVO> userDetailsVOs = new ArrayList<UserDetailVO>();
    private ArrayList<NotificationVO> notificationVOs = new ArrayList<NotificationVO>();
    private HashMap<String, String> userSettings = new HashMap<String, String>();

    private final int NOTIFICATIONS_ACTIVITY =1;
    private final int SETTINGS_ACTIVITY = 2;
    private final int DRAFTS_ACTIVITY = 3;

    private LogOutReceiver logOutReceiver;

    public void onCreate(Bundle savedInstanceState) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_LOGOUT");
        logOutReceiver = new LogOutReceiver();
        registerReceiver(logOutReceiver, intentFilter);
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        DataStore appStore = new DataStore(app);
        
        FlurryAgent.logEvent("User Details View");
        appStore.saveToPrefs("currentEvent", "User Details View");
        
        setContentView(R.layout.userdetails);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        getUserData();

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
        requestRank = (TextView)this.findViewById(R.id.requestRank);
        commentRank = (TextView)this.findViewById(R.id.commentRank);
        userDetailsList =(ListView)this.findViewById(R.id.userDetailsList);
        userDetailsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
               openList(position);
            }
        });

       if(app.getUserNotifications().size()>0) notificationVOs =app.getUserNotifications();
    }

    private void getUserData(){
        final CustomProgressDialog dialog = new CustomProgressDialog(UserDetailsActivity.this, UserDetailsActivity.this.getString(R.string.loadingProfile));
        dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("user_view"), Utils.isNetworkAvailable(UserDetailsActivity.this)){
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
                            populateUserData(parser.getResponse());
                            populateUserDetailsList();
                            loadDraftData();
                            //loadNotificationData();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }finally{
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                } else {
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Utils.noConnection(UserDetailsActivity.this);
                }
            }
        };
       if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }

    private void populateUserData(JSONObject user) throws JSONException{
        //user settings
       if(user.has("email") && !user.getString("email").equals("null")) userSettings.put("email", user.getString("email"));
       if(user.has("firstname") && !user.getString("firstname").equals("null")) userSettings.put("firstname", user.getString("firstname"));
        if(user.has("lastname") && !user.getString("lastname").equals("null")) userSettings.put("lastname", user.getString("lastname"));
        if(user.has("home_address") && !user.getString("home_address").equals("null")) userSettings.put("home_address", user.getString("home_address"));
        if(user.has("work_address") && !user.getString("work_address").equals("null")) userSettings.put("work_address", user.getString("work_address"));
        if(user.has("phone") && !user.getString("phone").equals("null")) userSettings.put("phone", user.getString("phone"));
        if(user.has("username") && !user.getString("username").equals("null")) userSettings.put("username", user.getString("username"));
        if(user.has("updates_sent") && !user.getString("updates_sent").equals("null")) userSettings.put("updates_sent", user.getString("updates_sent"));
        else userSettings.put("updates_sent", "status");
        if(user.has("email_on_comment") && user.get("email_on_comment")!=null) userSettings.put("email_on_comment", user.getString("email_on_comment"));
        //rank
        requestRank.setText(String.format(UserDetailsActivity.this.getString(R.string.requestRank), user.get("request_rank"), app.getCityName()));
        commentRank.setText(String.format(UserDetailsActivity.this.getString(R.string.commentRank), user.get("request_rank"), app.getCityName()));
        //list
        UserDetailVO requests = new UserDetailVO(UserDetailsActivity.this.getString(R.string.filterMyRequests), R.drawable.ic_orange_new_request, user.getInt("request_count"));
        userDetailsVOs.add(requests);
        UserDetailVO following = new UserDetailVO(UserDetailsActivity.this.getString(R.string.following), R.drawable.ic_orange_following, user.getInt("follow_count"));
        userDetailsVOs.add(following);
        UserDetailVO comments = new UserDetailVO(UserDetailsActivity.this.getString(R.string.commentsPlaceholder), R.drawable.ic_orange_comment_large, user.getInt("comment_count"));
        userDetailsVOs.add(comments);
        UserDetailVO upvoted = new UserDetailVO(UserDetailsActivity.this.getString(R.string.upvoted), R.drawable.ic_orange_upvote, user.getInt("upvote_count"));
        userDetailsVOs.add(upvoted);
    }

    private void loadNotificationData(){
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("user_notifications"), Utils.isNetworkAvailable(UserDetailsActivity.this)){
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
                            populateNotificationData(parser.getResponse().getJSONArray("notifications"));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else Utils.noConnection(UserDetailsActivity.this);
            }
        };
        clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.addParameter("last_login", String.valueOf(app.getUserLastLogin()/1000));
        clientGet.execute("");
    }

    private void populateNotificationData(JSONArray notifications){
        //add to array so the latest is on top
    	notificationVOs.clear();
    	
        for(int i=notifications.length()-1; i>-1; i--){
            try {
                JSONObject notification = notifications.getJSONObject(i).getJSONObject("notification");
                NotificationVO notificationVO = new NotificationVO(notification.getInt("id"), notification.getLong("date_updated"));
                notificationVO.setIsComment((notification.get("comment_on_request").equals("1")));
                notificationVO.setIsCommentReply((notification.get("comment_reply").equals("1")));
                notificationVO.setIsStatusUpdate((notification.get("status_update").equals("1")));
                if(notification.has("status") && notification.get("status")!=null) notificationVO.setStatus(notification.getString("status"));
                notificationVOs.add(0, notificationVO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //if more than 50, get rid of them
        /*if(notificationVOs.size()>50){
            for(int i= 50; i<notificationVOs.size(); i++){
               notificationVOs.remove(i);
            }
        }*/
        //which ones aren't read yet?
        int newNotifications =0;
        for (NotificationVO not : notificationVOs) {
            if (!not.getRead()) newNotifications++;
        }
        //if there are any notifications(even unread) add to list
        if(notificationVOs.size()>0){
            UserDetailVO nots = new UserDetailVO(UserDetailsActivity.this.getString(R.string.notificationsChoice), R.drawable.ic_orange_notifications, newNotifications);
            
            if(!userDetailsVOs.contains(nots))
            	userDetailsVOs.add(nots);
            
            populateUserDetailsList();
            app.setUserNotifications(notificationVOs);
        }
    }


    private void loadDraftData(){
       if(app.getUserDrafts().size()>0){
           UserDetailVO drafts = new UserDetailVO(UserDetailsActivity.this.getString(R.string.drafts), R.drawable.ic_orange_edit, app.getUserDrafts().size());
           userDetailsVOs.add(drafts);
           populateUserDetailsList();
       }
    }

    private void populateUserDetailsList(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        UserDetailsListAdapter listAdapter = new UserDetailsListAdapter(UserDetailsActivity.this, userDetailsVOs, mLInflater);
        userDetailsList.setAdapter(listAdapter);
        //userDetailsList.invalidateViews();
        //((BaseAdapter)((ListView)findViewById(R.id.userDetailsList)).getAdapter()).notifyDataSetChanged();
    }

    private void openList(int id){
       UserDetailVO userDetailVO = userDetailsVOs.get(id);
       String name = userDetailVO.getTitle();
        Bundle  b = new Bundle();
        Intent intent = null;
        HashMap<String, String> filters = new HashMap<String, String>();
       if(name.equals(UserDetailsActivity.this.getString(R.string.filterMyRequests))){
           filters.put("userRequests", "1");
           filters.put("nearby", "0");
           b.putSerializable("filters", filters);
           b.putString("originator", "my");

           intent = new Intent(this, RequestListActivity.class);
           intent.putExtras(b);
           startActivity(intent);
       }
       else if(name.equals(UserDetailsActivity.this.getString(R.string.following))){
           filters.put("followedRequests", "1");
           filters.put("nearby", "0");
           b.putSerializable("filters", filters);
           b.putString("originator", "followed");

           intent = new Intent(this, RequestListActivity.class);
           intent.putExtras(b);
           startActivity(intent);
       }
       else if(name.equals(UserDetailsActivity.this.getString(R.string.commentsPlaceholder))){
           filters.put("commentedRequests", "1");
           filters.put("nearby", "0");
           b.putSerializable("filters", filters);
           b.putString("originator", "commented");

           intent = new Intent(this, RequestListActivity.class);
           intent.putExtras(b);
           startActivity(intent);
       }
       else if(name.equals(UserDetailsActivity.this.getString(R.string.upvoted))){
           filters.put("votedRequests", "1");
           filters.put("nearby", "0");
           b.putSerializable("filters", filters);
           b.putString("originator", "voted");

           intent = new Intent(this, RequestListActivity.class);
           intent.putExtras(b);
           startActivity(intent);
       }
       else if(name.equals("Drafts")){
           intent = new Intent(this, DraftsActivity.class);
           intent.putExtras(b);
           startActivityForResult(intent, DRAFTS_ACTIVITY);
       }
       else if(name.equals("Notifications")){
           intent = new Intent(this, NotificationsActivity.class);
           intent.putExtras(b);
           startActivityForResult(intent, NOTIFICATIONS_ACTIVITY);
       }

    }

    public void settings(View v){
        Bundle b = new Bundle();
        b.putSerializable("userSettings", userSettings);
        Intent intent = new Intent(this, UserSettingsActivity.class);
        intent.putExtras(b);
        startActivityForResult(intent, SETTINGS_ACTIVITY);
        finish();
    }

    public void back(View v){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case NOTIFICATIONS_ACTIVITY:
                int newNotifications =0;
                for (NotificationVO not : app.getUserNotifications()) {
                    if (!not.getRead()) newNotifications++;
                }

                for(int i =0; i<userDetailsVOs.size(); i++){
                    UserDetailVO ud = userDetailsVOs.get(i);
                    if(ud.getTitle().equals("Notifications")){
                        ud.setCount(newNotifications);
                    }
                    userDetailsVOs.set(i, ud);
                }
                populateUserDetailsList();
                break;
            case SETTINGS_ACTIVITY:
                if(data!=null){
                    Bundle extras = data.getExtras();
                    userSettings = (HashMap<String, String>) extras.getSerializable("userSettings");
                }
                break;
            case DRAFTS_ACTIVITY:
                for(int i =0; i<userDetailsVOs.size(); i++){
                    UserDetailVO ud = userDetailsVOs.get(i);
                    if(ud.getTitle().equals("Drafts")){
                        if(app.getUserDrafts() !=null){
                            ud.setCount(app.getUserDrafts().size());
                        }
                        else ud.setCount(0);
                    }
                    if(ud.getCount()>0) userDetailsVOs.set(i, ud);
                    else userDetailsVOs.remove(i);
                    populateUserDetailsList();
                }
                break;
            default:
                break;

        }
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
    protected void onStop() {
        if (logOutReceiver != null){
            unregisterReceiver(logOutReceiver);
            logOutReceiver = null;
        }
       //System.gc();
       //FlurryAgent.endTimedEvent("User Details View");
       //FlurryAgent.onEndSession(this);
        
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
      
      
    }

}
