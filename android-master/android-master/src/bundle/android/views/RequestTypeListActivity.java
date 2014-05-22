package bundle.android.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.RequestTypeListAdapter;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.vo.CategoryTypeVO;
import bundle.android.model.vo.RequestTypeVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class RequestTypeListActivity extends PsActivity{
	private PublicStuffApplication app;
	private ListView listViewList;
	private Bundle b;
	private ArrayList<RequestTypeVO> requestTypes;
	private ViewFlipper viewFlip;
	private ArrayList<CategoryTypeVO> categoryTypes;
	private HashMap<String, ArrayList<RequestTypeVO>> categoryMap;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (PublicStuffApplication)getApplicationContext();
		DataStore appStore = new DataStore(app);
		setContentView(R.layout.listview);
		b = getIntent().getExtras();
		requestTypes = b.getParcelableArrayList("requestTypes");
		
		if(b.containsKey("categoryTypes"))
			categoryTypes = b.getParcelableArrayList("categoryTypes");
		if(b.containsKey("categoryMap"))
			categoryMap = (HashMap<String, ArrayList<RequestTypeVO>>) b.getSerializable("categoryMap");
			
		//Utils.useLatoInView(this, this.findViewById(android.R.id.content));
		initElements();

		FlurryAgent.logEvent("Request Type List");
		appStore.saveToPrefs("currentEvent", "Request Type List");

		populateRequestTypesList();
	}

	private void initElements(){
		RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
		headerLayoutView.setBackgroundColor(app.getNavColor());

		viewFlip = (ViewFlipper)this.findViewById(R.id.view_flipper);
		
		ImageView clearField = (ImageView)findViewById(R.id.clearField);
		clearField.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
		
		ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
		if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
			AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
			imageLoader.execute(new Void[0]);
		}
		else{
			cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
		}
		
		Typeface face = Typeface.createFromAsset(RequestTypeListActivity.this.getAssets(), "lato_regular.ttf");
		TextView listViewTitle = (TextView) this.findViewById(R.id.listViewTitle);
		listViewTitle.setTypeface(face);
		listViewTitle.setText(RequestTypeListActivity.this.getString(R.string.requestTypeSelect));
		
		listViewList = (ListView)this.findViewById(R.id.listViewList);
		listViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//if category
				/*if(requestTypes.get(position).)
				
				//make new listview
				
				*/
				if(requestTypes.get(position).getIsCategory())
				{
					//construct new list
					//get category arraylist from categoryMap by category_id, hmm
					
					viewFlip.addView(populateCategoryList(categoryMap.get(String.valueOf(requestTypes.get(position).getId())), requestTypes.get(position).getId()));
					viewFlip.setInAnimation(RequestTypeListActivity.this, R.anim.in_from_right);
					viewFlip.setOutAnimation(RequestTypeListActivity.this, R.anim.out_to_left);
					viewFlip.showNext();
				}
				else
				{
					//if not category        
					b.putInt("request_type_position", position);
					Intent intent = new Intent();
					intent.putExtras(b);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}

	private void populateRequestTypesList(){
		LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RequestTypeListAdapter listAdapter = new RequestTypeListAdapter(this, requestTypes, mLInflater);
		listViewList.setAdapter(listAdapter);
		listViewList.invalidateViews();
		((BaseAdapter)((ListView)findViewById(R.id.listViewList)).getAdapter()).notifyDataSetChanged();
	}
	
	private View populateCategoryList(ArrayList<RequestTypeVO> nestedRequestTypes, int id){
		LayoutInflater mLInflater = (LayoutInflater) RequestTypeListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//find categories that should be nested amongst this one
		for(int i = 0; i < categoryTypes.size(); i++)
		{
			if(categoryTypes.get(i).getParent().contentEquals(String.valueOf(id)))
			{
				//add category name to list
				RequestTypeVO categoryRequestType = new RequestTypeVO(categoryTypes.get(i).getId(), categoryTypes.get(i).getName(), false);
				categoryRequestType.setDescription(categoryTypes.get(i).getDescription());
				categoryRequestType.setIsCategory(true);
				if(nestedRequestTypes==null)
				{
					nestedRequestTypes = new ArrayList<RequestTypeVO>();
					nestedRequestTypes.add(categoryRequestType);
				}
				else if(!nestedRequestTypes.contains(categoryRequestType))
						nestedRequestTypes.add(categoryRequestType);
			}
		}
		
		final ArrayList<RequestTypeVO> currentList = nestedRequestTypes;
		if(currentList!=null)
		{
			RequestTypeListAdapter listAdapter = new RequestTypeListAdapter(RequestTypeListActivity.this, currentList, mLInflater);
			ListView mylistViewList = new ListView(RequestTypeListActivity.this);
			mylistViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					//if category
					/*if(requestTypes.get(position).)

				//make new listview

					 */
					if(currentList.get(position).getIsCategory())
					{
						//construct new list
						//get category arraylist from categoryMap by category_id, hmm
						viewFlip.addView(populateCategoryList(categoryMap.get(String.valueOf(currentList.get(position).getId())), currentList.get(position).getId()));
						viewFlip.setInAnimation(RequestTypeListActivity.this, R.anim.in_from_right);
						viewFlip.setOutAnimation(RequestTypeListActivity.this, R.anim.out_to_left);
						viewFlip.showNext();
					}
					else
					{
						//if category
						b.putString("category_id", currentList.get(position).getCategoryId());
						b.putInt("request_type_position", position);
						b.putSerializable("nestedRequest", currentList);
						Intent intent = new Intent();
						intent.putExtras(b);
						setResult(RESULT_OK, intent);
						finish();
					}
				}
			});

			Collections.sort(currentList, new Comparator<RequestTypeVO>() {
				public int compare(RequestTypeVO result1, RequestTypeVO result2) {
					return result1.getName().compareTo(result2.getName());
				}
			});

			//categoryMap.put(String.valueOf(id), currentList);
			
			mylistViewList.setAdapter(listAdapter);
			listAdapter.notifyDataSetChanged();

			return mylistViewList;
		}
		else
			return new View(RequestTypeListActivity.this);

	}
	
	public void back(View v){
		if (viewFlip.getDisplayedChild()==0) 
			finish();
		else{
			int i = viewFlip.getDisplayedChild();
			viewFlip.setInAnimation(this, R.anim.in_from_left);
			viewFlip.setOutAnimation(this, R.anim.out_to_right);
			viewFlip.removeViewAt(i);
		}
			
	}
	
	public void onBackPressed() {
		//super.onBackPressed();
		if (viewFlip.getDisplayedChild()==0) 
			finish();
		else{
			int i = viewFlip.getDisplayedChild();
			viewFlip.setInAnimation(this, R.anim.in_from_right);
			viewFlip.setOutAnimation(this, R.anim.out_to_left);
			viewFlip.removeViewAt(i);
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
