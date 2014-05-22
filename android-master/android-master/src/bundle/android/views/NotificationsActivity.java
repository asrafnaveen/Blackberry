package bundle.android.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.NotificationListAdapter;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.vo.NotificationVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;

import java.util.ArrayList;

public class NotificationsActivity extends PsActivity {
    private PublicStuffApplication app;
    private ListView listViewList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        setContentView(R.layout.listview);
        
        FlurryAgent.logEvent("Notifications View");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Notifications View");
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        populateNotificationsList();
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
        TextView listViewTitle = (TextView) this.findViewById(R.id.listViewTitle);
        listViewTitle.setText(NotificationsActivity.this.getString(R.string.myNotifications));
        listViewList = (ListView)this.findViewById(R.id.listViewList);
        listViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Bundle b = new Bundle();
                NotificationVO notificationVO = app.getUserNotifications().get(position);
                notificationVO.setRead(true);
                ArrayList<NotificationVO> notificationVOs= app.getUserNotifications();
                notificationVOs.set(position, notificationVO);
                //app.setUserNotifications(notificationVOs);
                b.putInt("request_id", notificationVO.getRequestId());
                Intent intent = new Intent(NotificationsActivity.this, RequestDetailsActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void populateNotificationsList(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        NotificationListAdapter listAdapter = new NotificationListAdapter(this, app.getUserNotifications(), mLInflater);
        listViewList.setAdapter(listAdapter);
        //listViewList.invalidateViews();
        //((BaseAdapter)((ListView)findViewById(R.id.listViewList)).getAdapter()).notifyDataSetChanged();
    }
    public void back(View v){
        finish();
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
