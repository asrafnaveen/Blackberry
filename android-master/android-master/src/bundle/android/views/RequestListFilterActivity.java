package bundle.android.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.ImageListViewAdapter;
import bundle.android.adapters.SingleListViewAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.vo.ImageDialogVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomDateDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.*;


public class RequestListFilterActivity extends PsActivity {
    private PublicStuffApplication app;
    private HashMap<String, String> filters;
    private Bundle b;

    private TextView sortText;
    private TextView statusText;
    private TextView filterText;
    private TextView requestTypeText;
    private TextView startDateText;
    private TextView endDateText;

    private int sort;
    private List<String> sortTypes;
    private final int SORT_RECENT = 0;
    private final int SORT_POPULAR = 1;

    private int status;
    private List<String> statusTypes;
    private final int STATUS_OPEN = 0;
    private final int STATUS_CLOSED = 1;
    private final int STATUS_ALL = 2;

    private int filter;
    private List<String> filterTypes;
    private final int FILTER_NEARBY = 0;
    private final int FILTER_MY = 1;
    private final int FILTER_FOLLOWING = 2;
    private final int FILTER_COMMENT = 3;
    private final int FILTER_VOTE = 4;

    private int requestTypeId = 0;
    private final List<String> requestTypes = new ArrayList<String>();
    private final List<String> requestTypeIds = new ArrayList<String>();

    private Date startDate;
    private Date endDate;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        setContentView(R.layout.requestlistfilter);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        
        DataStore appStore = new DataStore(app);
        FlurryAgent.logEvent("Nearby Request Filter");
        appStore.saveToPrefs("currentEvent", "Nearby Request Filter");

        b = getIntent().getExtras();
        filters = (HashMap<String, String>) b.getSerializable("filters");
        getRequestTypes(app.getCityClientId());
    }

    private void initElements(){
        RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
        headerLayoutView.setBackgroundColor(app.getNavColor());

        sortTypes = Arrays.asList(new String[]{RequestListFilterActivity.this.getString(R.string.sortRecent), RequestListFilterActivity.this.getString(R.string.sortPopular)});
        statusTypes = Arrays.asList(new String[]{RequestListFilterActivity.this.getString(R.string.statusOpen), RequestListFilterActivity.this.getString(R.string.statusClosed), RequestListFilterActivity.this.getString(R.string.statusAll)});
        filterTypes = Arrays.asList(new String[]{RequestListFilterActivity.this.getString(R.string.filterNearby), RequestListFilterActivity.this.getString(R.string.filterMyRequests), RequestListFilterActivity.this.getString(R.string.filterFollowedRequests), RequestListFilterActivity.this.getString(R.string.filterCommentedRequests), RequestListFilterActivity.this.getString(R.string.filterVotedRequests)});
        
        ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
        if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
            AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
            imageLoader.execute(new Void[0]);
        }
        else{
            cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
        }
        sortText = (TextView)this.findViewById(R.id.sortText);
        statusText = (TextView)this.findViewById(R.id.statusText);
        filterText = (TextView)this.findViewById(R.id.filterText);
        requestTypeText = (TextView)this.findViewById(R.id.requestTypeText);
        startDateText = (TextView)this.findViewById(R.id.startDateText);
        endDateText = (TextView)this.findViewById(R.id.endDateText);
    }

    private void populateFilterList(HashMap<String, String> filters){
        if(filters.containsKey("requestTypeId")){
            int position =  requestTypeIds.indexOf(filters.get("requestTypeId"));
            requestTypeId =  Integer.parseInt(filters.get("requestTypeId"));
            requestTypeText.setText(requestTypes.get(position));
        }
        if(filters.containsKey("sortBy") && filters.get("sortBy").equals("popular")){
            sortText.setText(RequestListFilterActivity.this.getString(R.string.sortPopular));
            sort = SORT_POPULAR;
        }
        else{
            sortText.setText("Recent");
            sort = SORT_RECENT;
        }
        if(filters.containsKey("status")){
             if(filters.get("status").equals("closed")){
                 statusText.setText(RequestListFilterActivity.this.getString(R.string.statusClosed));
                 status = STATUS_CLOSED;
             }
            else if(filters.get("status").equals("all")){
                statusText.setText(RequestListFilterActivity.this.getString(R.string.statusAll));
                status = STATUS_ALL;
            }
            else{
                statusText.setText(RequestListFilterActivity.this.getString(R.string.statusOpen));
                status = STATUS_OPEN;
            }
        }
        else{
            statusText.setText(RequestListFilterActivity.this.getString(R.string.statusOpen));
            status = STATUS_OPEN;
        }
        if(filters.containsKey("afterTimestamp")){
            startDate =  new Date(Long.parseLong(filters.get("afterTimestamp"))*1000);
            startDateText.setText(DateFormat.getDateInstance().format(startDate));
        }
        else{
            Calendar beginCal = Calendar.getInstance();
            beginCal.set(2000, 0, 1, 0, 0, 0);
            startDate =  beginCal.getTime();
            startDateText.setText(DateFormat.getDateInstance().format(startDate));
        }
        if(filters.containsKey("beforeTimestamp")){
            endDate =  new Date(Long.parseLong(filters.get("beforeTimestamp"))*1000);
            endDateText.setText(DateFormat.getDateInstance().format(endDate));
        }
        else{
            endDate =  new Date();
            endDateText.setText(DateFormat.getDateInstance().format(endDate));
        }

        if(filters.containsKey("userRequests")){
            filterText.setText(RequestListFilterActivity.this.getString(R.string.filterMyRequests));
            filter = FILTER_MY;
        }else if(filters.containsKey("followedRequests")){
            filterText.setText(RequestListFilterActivity.this.getString(R.string.filterFollowedRequests));
            filter = FILTER_FOLLOWING;
        }else if(filters.containsKey("commentedRequests")){
            filterText.setText(RequestListFilterActivity.this.getString(R.string.filterCommentedRequests));
            filter = FILTER_COMMENT;
        } else if(filters.containsKey("votedRequests")){
            filterText.setText(RequestListFilterActivity.this.getString(R.string.filterVotedRequests));
            filter = FILTER_VOTE;
        } else{
            filterText.setText(RequestListFilterActivity.this.getString(R.string.nearby));
            filter = FILTER_NEARBY;
        }
    }
    private void getRequestTypes(int id){
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("requesttypes_list"), Utils.isNetworkAvailable(RequestListFilterActivity.this)){
            final CustomProgressDialog dialog = new CustomProgressDialog(RequestListFilterActivity.this, RequestListFilterActivity.this.getString(R.string.requestTypes));
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
                            requestTypeIds.clear();
                            JSONArray requestTypesArray = parser.getResponse().getJSONArray("request_types");
                            requestTypes.add("All Request Types");
                            requestTypeIds.add("0");
                            for(int i=0; i<requestTypesArray.length(); i++){
                                JSONObject request_type= requestTypesArray.getJSONObject(i).getJSONObject("request_type");
                                requestTypes.add(request_type.getString("name"));
                                requestTypeIds.add(request_type.getString("id"));

                            }
                            populateFilterList(filters);
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
                    Utils.noConnection(RequestListFilterActivity.this);
                }

            }
        };
        clientGet.addParameter("client_id", String.valueOf(id));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");

    }

    /**
     * Back button
     * @param v
     */
    public void back(View v){
        finish();
    }

    /**
     * Send data back to request list controller
     * @param v
     */
    public void clickSubmit(View v){
        if(startDate == null){
            Calendar beginCal = Calendar.getInstance();
            beginCal.set(2000, 0, 1, 0, 0, 0);
            startDate =  beginCal.getTime();
        }
        if(endDate ==null){
            endDate = new Date();
        }
        if(startDate.after(endDate)){
            final CustomAlertDialog dialog = new CustomAlertDialog(this, RequestListFilterActivity.this.getString(R.string.ErrorPastTense), RequestListFilterActivity.this.getString(R.string.endDateError), RequestListFilterActivity.this.getString(R.string.OkButton), null);

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
            Intent intent = new Intent();
            b.putSerializable("filters", filters);
            intent.putExtras(b);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    /**
     * Show sort dialog
     * @param v
     */
    public void sort(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);

        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(RequestListFilterActivity.this.getString(R.string.sortBy));

        dialog.show();
        final SingleListViewAdapter adapter = new SingleListViewAdapter(this, sortTypes, mLInflater, sort);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                sortText.setText(sortTypes.get(position));
                sort = position;
                switch(sort){
                    case SORT_POPULAR:
                        filters.put("sortBy","popular");
                        break;
                    case SORT_RECENT:
                        filters.put("sortBy","recent");
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }
    /**
     * Show status dialog
     * @param v
     */
    public void status(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);

        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(RequestListFilterActivity.this.getString(R.string.status));

        dialog.show();
        final SingleListViewAdapter adapter = new SingleListViewAdapter(this, statusTypes, mLInflater, status);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                statusText.setText(statusTypes.get(position));
                status = position;
                switch(status){
                    case STATUS_OPEN:
                        filters.put("status","open");
                        break;
                    case STATUS_CLOSED:
                        filters.put("status","closed");
                        break;
                    case STATUS_ALL:
                        filters.put("status","all");
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }
    /**
     * Show filter dialog
     * @param v
     */
    public void filter(View v){
        ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
        ImageDialogVO nearby = new ImageDialogVO(R.drawable.ic_map_pin_orange, filterTypes.get(0), null, (filter==FILTER_NEARBY));
        listToReturn.add(nearby);
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")){
            ImageDialogVO my = new ImageDialogVO(R.drawable.ic_orange_new_request, filterTypes.get(1), null, (filter==FILTER_MY));
            listToReturn.add(my);
            ImageDialogVO following = new ImageDialogVO(R.drawable.ic_orange_following, filterTypes.get(2), null, (filter==FILTER_FOLLOWING));
            listToReturn.add(following);
            ImageDialogVO comment = new ImageDialogVO(R.drawable.ic_orange_comment_large, filterTypes.get(3), null, (filter==FILTER_COMMENT));
            listToReturn.add(comment);
            ImageDialogVO voted = new ImageDialogVO(R.drawable.ic_orange_upvote, filterTypes.get(4), null, (filter==FILTER_VOTE));
            listToReturn.add(voted);
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);

        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(RequestListFilterActivity.this.getString(R.string.filterBy));

        dialog.show();

        final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToReturn, mLInflater);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                filterText.setText(filterTypes.get(position));
                filter = position;
                if(filters.containsKey("nearby"))filters.remove("nearby");
                if(filters.containsKey("userRequests"))filters.remove("userRequests");
                if(filters.containsKey("followedRequests"))filters.remove("followedRequests");
                if(filters.containsKey("commentedRequests"))filters.remove("commentedRequests");
                if(filters.containsKey("votedRequests"))filters.remove("votedRequests");
                switch(filter){
                    case FILTER_NEARBY:
                        filters.put("nearby", "1");
                        break;
                    case FILTER_MY:
                        filters.put("userRequests", "1");
                        filters.put("nearby", "0");
                        break;
                    case FILTER_FOLLOWING:
                        filters.put("followedRequests", "1");
                        filters.put("nearby", "0");
                        break;
                    case FILTER_COMMENT:
                        filters.put("commentedRequests", "1");
                        filters.put("nearby", "0");
                        break;
                    case FILTER_VOTE:
                        filters.put("votedRequests", "1");
                        filters.put("nearby", "0");
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }
    /**
     * Show requestType dialog
     * @param v
     */
    public void requestType(View v){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);
        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);

        dialog.show();
        int selected = requestTypeIds.indexOf(String.valueOf(requestTypeId));
        final SingleListViewAdapter adapter = new SingleListViewAdapter(this, requestTypes, mLInflater, selected);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                requestTypeText.setText(requestTypes.get(position));
                requestTypeId = Integer.parseInt(requestTypeIds.get(position));
                if(requestTypeId!=0){
                    filters.put("requestTypeId", String.valueOf(requestTypeId));
                }
                else if(filters.containsKey("requestTypeId")){
                    filters.remove("requestTypeId");
                }
                dialog.dismiss();
            }
        });
    }
    /**
     * Show startDate dialog
     * @param v
     */
    public void startDate(View v){
        final CustomDateDialog dialog = new CustomDateDialog(this, RequestListFilterActivity.this.getString(R.string.startDateSelect), startDate, RequestListFilterActivity.this.getString(R.string.OkButton), null);

        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int id = v.getId();
                switch(id){
                    case R.id.alertConfirm:
                        DatePicker dp = (DatePicker)dialog.findViewById(R.id.datePicker);
                        Calendar beginCal = Calendar.getInstance();
                        beginCal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 0, 0, 0);
                        startDate =  beginCal.getTime();
                        startDateText.setText(DateFormat.getDateInstance().format(startDate));
                        filters.put("afterTimestamp", String.valueOf((int)(startDate.getTime()/1000)));
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        };
        dialog.setListener(listener);
        dialog.show();
    }
    /**
     * Show endDate dialog
     * @param v
     */
    public void endDate(View v){
        final CustomDateDialog dialog = new CustomDateDialog(this, RequestListFilterActivity.this.getString(R.string.endDateSelect), endDate, RequestListFilterActivity.this.getString(R.string.OkButton), null);

        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int id = v.getId();
                switch(id){
                    case R.id.alertConfirm:
                        DatePicker dp = (DatePicker)dialog.findViewById(R.id.datePicker);
                        Calendar endCal = Calendar.getInstance();
                        endCal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        endDate =  endCal.getTime();
                        endDateText.setText(DateFormat.getDateInstance().format(endDate));
                        filters.put("beforeTimestamp", String.valueOf((int)(endDate.getTime()/1000)));
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        };
        dialog.setListener(listener);
        dialog.show();
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