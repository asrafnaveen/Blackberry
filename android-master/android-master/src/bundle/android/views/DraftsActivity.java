package bundle.android.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.ImageListViewAdapter;
import bundle.android.adapters.RequestDraftListAdapter;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.vo.ImageDialogVO;
import bundle.android.model.vo.RequestDraftVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;

import java.util.ArrayList;


public class DraftsActivity extends PsActivity {
    private PublicStuffApplication app;
    private ListView listViewList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (PublicStuffApplication)getApplicationContext();
        setContentView(R.layout.listview);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        initElements();
        
        FlurryAgent.logEvent("Drafts");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Drafts");
        
        populateDraftsList();
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
        listViewTitle.setText(DraftsActivity.this.getString(R.string.myDrafts));
        listViewList = (ListView)this.findViewById(R.id.listViewList);
        listViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Bundle b = new Bundle();
                b.putInt("requestDraft", position);
                Intent intent = new Intent(DraftsActivity.this, NewRequestActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }
        });
        listViewList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener
                (){
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int
                    position, long id) {
                     promptDelete(position);
                return false;
            }
        });
    }

    private void populateDraftsList(){
        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RequestDraftListAdapter listAdapter = new RequestDraftListAdapter(this, app.getUserDrafts(), mLInflater);
        listViewList.setAdapter(listAdapter);
        listViewList.invalidateViews();
        ((BaseAdapter)((ListView)findViewById(R.id.listViewList)).getAdapter()).notifyDataSetChanged();
    }

    private void promptDelete(int position){
        final int draftPosition = position;
        ArrayList<ImageDialogVO> listToReturn = new ArrayList<ImageDialogVO>();
            ImageDialogVO deleteDraft = new ImageDialogVO(R.drawable.holo_light5_content_discard, DraftsActivity.this.getString(R.string.delDraft), null, false);
            listToReturn.add(deleteDraft);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vl = mLInflater.inflate(R.layout.item_list, null, false);
        Utils.useLatoInView(this, vl);
        dialog.setContentView(vl);
        TextView title = (TextView)dialog.findViewById(R.id.listTitle);
        title.setText(DraftsActivity.this.getString(R.string.delDraft));
        dialog.show();

        final ImageListViewAdapter adapter = new ImageListViewAdapter(this, listToReturn, mLInflater);
        ListView lv = (ListView) dialog.findViewById(R.id.item_listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
            {
                switch(pos){
                    case 0:
                        ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
                        requestDraftVOs.remove(draftPosition);
                        app.setUserDrafts(requestDraftVOs);
                        populateDraftsList();
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        });
    }
    public void back(View v){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        switch(resultCode){
            case 0:
                populateDraftsList();
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
    protected void onDestroy() {
       //System.gc();
        super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
