package bundle.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.model.vo.WidgetVO;
import bundle.android.service.LocationListenerAgent;
import bundle.android.utils.CurrentLocation;
import bundle.android.utils.DataStore;
import bundle.android.views.CityDashboardActivity;
import bundle.android.views.NewRequestActivity;
import bundle.android.views.RequestListActivity;
import bundle.android.views.WebViewActivity;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.wassabi.psmobile.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WidgetViewAdapter extends PagerAdapter {
    private final Context context;
    private final List<WidgetVO> listItems;
    private final LayoutInflater mLayoutInflater;
    private final int currentPage = this.currentPage;
    private CustomProgressDialog dialog = null;
    private final PublicStuffApplication app;
    private PullToRefreshGridView mPullRefreshGridView;

    public WidgetViewAdapter(Context c, List<WidgetVO> widgetVOs, LayoutInflater layoutInflater){
        context = c;
        listItems = widgetVOs;
        mLayoutInflater = layoutInflater;
        this.app = (PublicStuffApplication)context.getApplicationContext();

        
    }
    @Override
    public int getCount() {
        return (listItems.size()/CityDashboardActivity.ICON_CONSTANT) + ((listItems.size()%CityDashboardActivity.ICON_CONSTANT==0)?0:1);
    }


    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final int page = position;
        View layout = mLayoutInflater.inflate(R.layout.widgetsgrid, null);
    	//mPullRefreshGridView = (PullToRefreshGridView) layout.findViewById(R.id.widgetGridView);
        //GridView gv = mPullRefreshGridView.getRefreshableView();
        GridView gv = (GridView)layout.findViewById(R.id.widgetGridView);
        gv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WidgetClickListener(page, i);
            }
        });

        ArrayList<WidgetVO> wvo = new ArrayList<WidgetVO>();
       for(int i = position *CityDashboardActivity.ICON_CONSTANT; i<(position*CityDashboardActivity.ICON_CONSTANT) +CityDashboardActivity.ICON_CONSTANT; i++){
           if(i<listItems.size()){
               wvo.add(listItems.get(i));
           }
       }
        WidgetGridAdapter widgetGridAdapter = new WidgetGridAdapter(context, wvo, mLayoutInflater, position);
        gv.setAdapter(widgetGridAdapter);

        (collection).addView(layout);

        return layout;
    }


    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        (collection).removeView((View) view);
    }



    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==(object);
    }

    @Override
    public void finishUpdate(ViewGroup arg0) {}


    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {}

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(ViewGroup arg0) {}

    private void WidgetClickListener(int page, int item)
    {

            Intent intent = null;
            Bundle b = new Bundle();
            switch (item + (page *CityDashboardActivity.ICON_CONSTANT))
            {
                case 0:
                    dialog = new CustomProgressDialog(context, context.getString(R.string.findingLocation));
                    dialog.show();
                    intent = new Intent(context, NewRequestActivity.class);
                    break;
                case 1:
                    double latitude = 0;
                    double longitude = 0;
                    DataStore store = new DataStore(context);
                    Location currLocation = CurrentLocation.getLocation(LocationListenerAgent.lmgrGps);
                    Location pastLocation = store.getCurrLocation();

                    if(!CurrentLocation.isBetterLocation(currLocation, pastLocation)){
                        currLocation = pastLocation;

                    }
                    if(currLocation==null){
                        latitude = app.getCityLat();
                        longitude = app.getCityLon();
                    }
                    else{
                        latitude = currLocation.getLatitude();
                        longitude = currLocation.getLongitude();
                    }
                    dialog = new CustomProgressDialog(context, context.getString(R.string.getRequestList));
                    dialog.show();
                    HashMap<String, String> filters = new HashMap<String, String>();
                    filters.put("nearby", "1");
                    filters.put("latitude", String.valueOf(latitude));
                    filters.put("longitude", String.valueOf(longitude));
                    b.putSerializable("filters", filters);
                    b.putString("originator", "nearby");
                    intent = new Intent(context, RequestListActivity.class);
                    intent.putExtras(b);

                    break;
                default:
                    if(listItems.get(item + (page *CityDashboardActivity.ICON_CONSTANT)).getUrl()!=null)
                    b.putString("url", listItems.get(item + (page *CityDashboardActivity.ICON_CONSTANT)).getUrl());
                    b.putString("title", listItems.get(item + (page *CityDashboardActivity.ICON_CONSTANT)).getTitle());
                    intent = new Intent(context, WebViewActivity.class);
                    intent.putExtras(b);
                    break;
            }
            context.startActivity(intent);
            if(dialog!= null)dialog.cancel();
        }



}


