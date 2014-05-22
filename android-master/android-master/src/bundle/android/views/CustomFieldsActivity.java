package bundle.android.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import bundle.android.PublicStuffApplication;
import bundle.android.adapters.CustomFieldListAdapter;
import bundle.android.adapters.CustomOptionsAdapter;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.model.vo.CustomFieldArrayVO;
import bundle.android.model.vo.CustomFieldOptionsVO;
import bundle.android.model.vo.CustomFieldVO;
import bundle.android.model.vo.RequestDraftVO;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class CustomFieldsActivity extends PsActivity {
    private PublicStuffApplication app;
    //private ListView customFieldsList;
    private ArrayList<CustomFieldVO> customFields= new ArrayList<CustomFieldVO>();
    private RequestDraftVO requestData;
    private int requestDraft = -1;
    private ArrayList<CustomFieldVO> passedCustomField;
    private final HashMap<String, String> custValues = new HashMap<String, String>();
    
    private CustomOptionsAdapter multiSelectAdapter;
	Spinner singleSelectSpinner;
	EditText customFieldText;
	CustomFieldVO customFieldVO;
	HashMap<Integer, TextView> customFieldMap = new HashMap<Integer, TextView>();
	HashMap<Integer, CustomFieldVO> multiFieldMap = new HashMap<Integer, CustomFieldVO>();
	
	Context context;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_fields_scrollview);
        app = (PublicStuffApplication)getApplicationContext();
        context = this;
        Bundle b = getIntent().getExtras();
        requestData = (RequestDraftVO) b.getSerializable("requestData");
        requestDraft = b.getInt("requestDraft");
        
        //customFields = (ArrayList<CustomFieldVO>) b.getSerializable("customFieldMap");
        
        initElements();
        //populateCustomFieldsList();
        
        FlurryAgent.logEvent("Custom Field View");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Custom Field View");
        
		
		
        getCustomFields(requestData.getRequestType().getId());
        
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
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

        //customFieldsList =(ListView)this.findViewById(R.id.customFieldsList);
        //customFieldsList.setFocusable(false);
        TextView customFieldsDescription = (TextView) this.findViewById(R.id.customFieldsDescription);
        customFieldsDescription.setText(String.format(CustomFieldsActivity.this.getString(R.string.requestAddl), requestData.getCity(), requestData.getState(), requestData.getRequestType().getName()));
        //customFieldsList.setClickable(false);
    }

    private void getCustomFields(int id){
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("customfields_list"), Utils.isNetworkAvailable(CustomFieldsActivity.this)){
            final CustomProgressDialog dialog = new CustomProgressDialog(CustomFieldsActivity.this, CustomFieldsActivity.this.getString(R.string.getcustomFields));
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
                            JSONArray customFieldsArray = parser.getResponse().getJSONArray("custom_fields");
                            populateCustomFields(customFieldsArray);
                            //populateCustomFieldsList();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally{
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                }else {
                    Utils.noConnection(CustomFieldsActivity.this);
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                }
            }
        };
        clientGet.addParameter("request_type_id", String.valueOf(id));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        clientGet.execute("");
    }

    private void populateCustomFields(JSONArray customFieldsJSON) throws JSONException{
         for(int i = 0; i<customFieldsJSON.length(); i++){
             JSONObject customField = customFieldsJSON.getJSONObject(i).getJSONObject("custom_field");
             
             ArrayList<CustomFieldOptionsVO> mOptionsArray = new ArrayList<CustomFieldOptionsVO>();
             
             //create JSONArray for options into ArrayList<CustomOptions> here;
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
             
             customFieldVO = new CustomFieldVO(customField.getInt("id"),
                                                                customField.getString("name"),
                                                                customField.getString("type"),
                                                                (customField.getInt("required")==1),
                                                                (customField.getInt("is_public")==1),
                                                                mOptionsArray);
             
             if(customField.has("description") && customField.get("description")!=null) 
            	 customFieldVO.setDescription(customField.getString("description"));
             
             if(requestData.getCustomFields()!=null){
                 ArrayList<CustomFieldVO> tempFields =  requestData.getCustomFields().getCustFields();
                 for(CustomFieldVO tempField : tempFields){
                     if(tempField.getId() == customFieldVO.getId() && tempField.getValue()!=null){
                         customFieldVO.setValue(tempField.getValue());
                     }
                 }
             }
             
             //maybe create views here too
             LinearLayout innerList = (LinearLayout)findViewById(R.id.innerList);
             
             final LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             View newView = mLInflater.inflate(R.layout.custom_field_cell, null);
             
             
     		TextView customFieldTitle = (TextView)newView.findViewById(R.id.customFieldTitle);
     		TextView customFieldIsRequired = (TextView)newView.findViewById(R.id.customFieldIsRequired);
     		customFieldText = (EditText)newView.findViewById(R.id.customFieldText);
     		final CheckBox customFieldCheckbox = (CheckBox)newView.findViewById(R.id.customFieldCheckbox);
     		TextView customIsPublic = (TextView)newView.findViewById(R.id.customIsPublic);
     		TextView customFieldDescription = (TextView)newView.findViewById(R.id.customFieldDescription);


     		customFieldTitle.setText(customFieldVO.getName());

     		if(customFieldVO.getRequired()) customFieldIsRequired.setVisibility(View.VISIBLE);
     		else customFieldIsRequired.setVisibility(View.GONE);

     		if(customFieldVO.getType().equals("checkbox")){
     			if(customFieldVO.getValue()!=null){
     				customFieldCheckbox.setChecked((customFieldVO.getValue().equals("1")));
     			}
     			else{
     				customFieldVO.setValue("0");
     			}
     			customFieldCheckbox.setVisibility(View.VISIBLE);
     			customFieldCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
     			{
     				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
     				{
     					customFieldVO.setValue((isChecked)?"1":"0");
     				}
     			});
     			customFieldText.setVisibility(View.GONE);
     		}//end checkbox
     		else if(customFieldVO.getType().equals("multiselect")){			
     			customFieldCheckbox.setVisibility(View.GONE);
     			
     			//populate new arraylist and then display in popup dialog with its own listview 
     			//(how to get values back??? record within object, or pass bundle back in setResult with intent)... 
     			
     			final ArrayList<CustomFieldOptionsVO> customFieldOptions = customFieldVO.getOptions();
     			/*(if(customFieldOptions.length()!=0) //if it gets here then multiselect is the type name, needs to all go in a listview or spinner
     			{
     				for(int j = 0; j < customFieldOptions.length(); j++)
     				{
     					JSONObject currentOption;
     					try {
     						currentOption = customFieldOptions.getJSONObject(j).getJSONObject("option");
     						currentOption.getString("id");
     						currentOption.getString("name");
     						currentOption.getString("description"); //hmm this can be null
     						
     					} catch (JSONException e) {
     						// TODO Auto-generated catch block
     						e.printStackTrace();
     					}
     				}//end for
     			}*///end if
     			multiFieldMap.put(i, customFieldVO);
     			
     			newView.findViewById(R.id.custom_field_root).setOnClickListener(new OnClickListener(){

     				@Override
     				public void onClick(View v) {
     					// TODO Auto-generated method stub
     					Dialog multiView = new Dialog(context);
     					
     					ListView mListView = new ListView(context);
     					multiView.requestWindowFeature(Window.FEATURE_NO_TITLE);
     					multiView.setContentView(mListView);
     					
     					mListView.setCacheColorHint(Color.parseColor("#00000000"));//.setBackgroundColor(Color.parseColor("#00000000"));
     					
     					//ArrayAdapter<JSONArray> multiSelectAdapter = new ArrayAdapter<JSONArray>(context, , customfields);
     					
     					multiSelectAdapter = new CustomOptionsAdapter(context, customFieldOptions, mLInflater);
     					mListView.setAdapter(multiSelectAdapter);
     					multiView.setCanceledOnTouchOutside(true);
     					
     					multiView.setOnCancelListener(new OnCancelListener(){

     						@Override
     						public void onCancel(DialogInterface dialog) {
     							// TODO Auto-generated method stub
     							
     							boolean breakpoint;
     							breakpoint = true;
     							//customFieldOptions.
     						}
     						
     					});
     					
     					multiView.setOnDismissListener(new OnDismissListener(){

     						@Override
     						public void onDismiss(DialogInterface dialog) {
     							// TODO Auto-generated method stub
     							boolean breakpoint;
     							 //need to see if I can retrieve modified array
     							
     							/*
     							 * for loop
     							 * 
     							 */
     							for(int i = 0; i < multiSelectAdapter.getCount(); i++)
     							{
     								CustomFieldOptionsVO mCustomFieldOptions = (CustomFieldOptionsVO)multiSelectAdapter.getItem(i);
     								if(mCustomFieldOptions.isChecked())
     								{
     									mCustomFieldOptions.getName(); //save name somewhere
     									
     									if(!customFields.get(i).getOptions().isEmpty() && i<customFields.get(i).getOptions().size()){
     										//need to revisit what this does. it removes an element of an array and puts a new version of the element at the same index
     										customFields.get(i).getOptions().remove(i);
     										customFields.get(i).getOptions().add(i, mCustomFieldOptions);
     									}
     									else
     									{
     										customFields.get(i).getOptions().add(mCustomFieldOptions);
     									}
     								}
     							}
     							breakpoint = true;
     						}
     						
     					});
     					
     					multiView.show();
     				}
     				
     			});

     			customFieldText.setVisibility(View.GONE);
     		}//end multiselect
     		else if(customFieldVO.getType().equals("singleselect"))
     		{
     			customFieldCheckbox.setVisibility(View.GONE);
     			customFieldText.setVisibility(View.GONE);

     			final ArrayList<CustomFieldOptionsVO> customFieldOptions = customFieldVO.getOptions();
     			ArrayList<String> items = new ArrayList<String>();
     			for(int iterator = 0; iterator < customFieldOptions.size(); iterator++)
     			{
     				items.add(customFieldOptions.get(iterator).getName()+":\n"+customFieldOptions.get(iterator).getDescription());
     			}
     			
     			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_singleselect, items); 
     			/*new ArrayAdapter<String>(context,
     			        android.R.layout.simple_spinner_dropdown_item, items);*/
     					/*ArrayAdapter..createFromResource(getApplicationContext(), , R.layout.custom_singleselect);*/
     	        adapter.setDropDownViewResource(R.layout.custom_singleselect);
     			
     			singleSelectSpinner = (Spinner)newView.findViewById(R.id.singleselectspinner);
     			singleSelectSpinner.setAdapter(adapter);
     			singleSelectSpinner.setVisibility(View.VISIBLE);
     			singleSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

     				@Override
     				public void onItemSelected(AdapterView<?> arg0, View arg1,
     						int arg2, long arg3) {
     					// TODO Auto-generated method stub
     					
     					//save stuff here
     					for(int i = 0; i < customFieldOptions.size(); i++)
     					{
     						
     						if(customFieldOptions.get(i).isChecked()==true && i!=singleSelectSpinner.getSelectedItemPosition())
     							customFieldOptions.get(i).setChecked(false);
     						
     					}
     					
     					CustomFieldOptionsVO singleOptionSet = customFieldOptions.get(singleSelectSpinner.getSelectedItemPosition());
     					singleOptionSet.setChecked(true);
     					customFieldOptions.remove(singleSelectSpinner.getSelectedItemPosition());
     					customFieldOptions.add(singleSelectSpinner.getSelectedItemPosition(), singleOptionSet);
     					
     				}

     				@Override
     				public void onNothingSelected(AdapterView<?> arg0) {
     					// TODO Auto-generated method stub
     					
     				}
     				
     			});
     			
     		}//end singleselect
     		else{
     			customFieldCheckbox.setVisibility(View.GONE);
     			customFieldText.setVisibility(View.VISIBLE);
     			if(customFieldVO.getValue()!=null){
     				customFieldText.setText(customFieldVO.getValue());
     			}
     			
     			customFieldMap.put(i, customFieldText);

     			customFieldText.addTextChangedListener(new TextWatcher() {
     				@Override
     				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

     				@Override
     				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

     				@Override
     				public void afterTextChanged(Editable editable) {
     					customFieldVO.setValue(customFieldText.getText().toString());
     					
     					//this.customFields.add(customFieldVO);
     					
     					//customFields..add(customFieldVO);
     					//need to know this one's position
     				}
     			});
     			
     			//add to hashmap with index and customfieldvo
     		}

     		if(customFieldVO.getPublic())customIsPublic.setVisibility(View.VISIBLE);
     		else customIsPublic.setVisibility(View.GONE);

     		if(customFieldVO.getDescription()!=null){
     			customFieldDescription.setVisibility(View.VISIBLE);
     			customFieldDescription.setText(customFieldVO.getDescription());
     		}
     		else{
     			customFieldDescription.setVisibility(View.GONE);
     			customFieldDescription.setText("");
     		}
     		//Utils.useLatoInView(context, newView);
     		if(customFieldTitle.getText().toString().contentEquals("Disclaimer"))
     		{
     			customFieldText.setVisibility(View.GONE);
     		}/*else
     			customFieldText.setVisibility(View.VISIBLE);*/

     		
             
             innerList.addView(newView);
             
             this.customFields.add(customFieldVO);
         }//end for loop
    }
//    private void populateCustomFieldsList(){
//       for(int i = 0; i < customFields.size(); i++)
//       {
//    	   
//    	   ArrayList<CustomFieldOptionsVO> mOptionsArray = new ArrayList<CustomFieldOptionsVO>();
//           
//           //create JSONArray for options into ArrayList<CustomOptions> here;
//           if(customField.getJSONArray("options").length()>0){
//          	 
//          	 for(int j = 0; j < customField.getJSONArray("options").length(); j++)
//          	 {
//          		 CustomFieldOptionsVO mOptions = new CustomFieldOptionsVO(customField.getInt("id"),customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getInt("id"), 
//          				 customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getString("name"),
//          				 customField.getJSONArray("options").getJSONObject(j).getJSONObject("option").getString("description"),
//          				 false);
//
//          		 mOptionsArray.add(mOptions);
//          	 }
//           }
//    	   
//    	 //maybe create views here too
//           LinearLayout innerList = (LinearLayout)findViewById(R.id.innerList);
//           
//           final LayoutInflater mLInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//           View newView = mLInflater.inflate(R.layout.custom_field_cell, null);
//           
//           
//   		TextView customFieldTitle = (TextView)newView.findViewById(R.id.customFieldTitle);
//   		TextView customFieldIsRequired = (TextView)newView.findViewById(R.id.customFieldIsRequired);
//   		customFieldText = (EditText)newView.findViewById(R.id.customFieldText);
//   		final CheckBox customFieldCheckbox = (CheckBox)newView.findViewById(R.id.customFieldCheckbox);
//   		TextView customIsPublic = (TextView)newView.findViewById(R.id.customIsPublic);
//   		TextView customFieldDescription = (TextView)newView.findViewById(R.id.customFieldDescription);
//
//
//   		customFieldTitle.setText(customFieldVO.getName());
//
//   		if(customFieldVO.getRequired()) customFieldIsRequired.setVisibility(View.VISIBLE);
//   		else customFieldIsRequired.setVisibility(View.GONE);
//
//   		if(customFieldVO.getType().equals("checkbox")){
//   			if(customFieldVO.getValue()!=null){
//   				customFieldCheckbox.setChecked((customFieldVO.getValue().equals("1")));
//   			}
//   			else{
//   				customFieldVO.setValue("0");
//   			}
//   			customFieldCheckbox.setVisibility(View.VISIBLE);
//   			customFieldCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//   			{
//   				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//   				{
//   					customFieldVO.setValue((isChecked)?"1":"0");
//   				}
//   			});
//   			customFieldText.setVisibility(View.GONE);
//   		}//end checkbox
//   		else if(customFieldVO.getType().equals("multiselect")){			
//   			customFieldCheckbox.setVisibility(View.GONE);
//   			
//   			//populate new arraylist and then display in popup dialog with its own listview 
//   			//(how to get values back??? record within object, or pass bundle back in setResult with intent)... 
//   			
//   			final ArrayList<CustomFieldOptionsVO> customFieldOptions = customFieldVO.getOptions();
//   			/*(if(customFieldOptions.length()!=0) //if it gets here then multiselect is the type name, needs to all go in a listview or spinner
//   			{
//   				for(int j = 0; j < customFieldOptions.length(); j++)
//   				{
//   					JSONObject currentOption;
//   					try {
//   						currentOption = customFieldOptions.getJSONObject(j).getJSONObject("option");
//   						currentOption.getString("id");
//   						currentOption.getString("name");
//   						currentOption.getString("description"); //hmm this can be null
//   						
//   					} catch (JSONException e) {
//   						// TODO Auto-generated catch block
//   						e.printStackTrace();
//   					}
//   				}//end for
//   			}*///end if
//   			multiFieldMap.put(i, customFieldVO);
//   			
//   			newView.findViewById(R.id.custom_field_root).setOnClickListener(new OnClickListener(){
//
//   				@Override
//   				public void onClick(View v) {
//   					// TODO Auto-generated method stub
//   					Dialog multiView = new Dialog(context);
//   					
//   					ListView mListView = new ListView(context);
//   					multiView.requestWindowFeature(Window.FEATURE_NO_TITLE);
//   					multiView.setContentView(mListView);
//   					
//   					mListView.setCacheColorHint(Color.parseColor("#00000000"));//.setBackgroundColor(Color.parseColor("#00000000"));
//   					
//   					//ArrayAdapter<JSONArray> multiSelectAdapter = new ArrayAdapter<JSONArray>(context, , customfields);
//   					
//   					multiSelectAdapter = new CustomOptionsAdapter(context, customFieldOptions, mLInflater);
//   					mListView.setAdapter(multiSelectAdapter);
//   					multiView.setCanceledOnTouchOutside(true);
//   					
//   					multiView.setOnCancelListener(new OnCancelListener(){
//
//   						@Override
//   						public void onCancel(DialogInterface dialog) {
//   							// TODO Auto-generated method stub
//   							
//   							boolean breakpoint;
//   							breakpoint = true;
//   							//customFieldOptions.
//   						}
//   						
//   					});
//   					
//   					multiView.setOnDismissListener(new OnDismissListener(){
//
//   						@Override
//   						public void onDismiss(DialogInterface dialog) {
//   							// TODO Auto-generated method stub
//   							boolean breakpoint;
//   							 //need to see if I can retrieve modified array
//   							
//   							/*
//   							 * for loop
//   							 * 
//   							 */
//   							for(int i = 0; i < multiSelectAdapter.getCount(); i++)
//   							{
//   								CustomFieldOptionsVO mCustomFieldOptions = (CustomFieldOptionsVO)multiSelectAdapter.getItem(i);
//   								if(mCustomFieldOptions.isChecked())
//   								{
//   									mCustomFieldOptions.getName(); //save name somewhere
//   									
//   									if(!customFields.get(i).getOptions().isEmpty() && i<customFields.get(i).getOptions().size()){
//   										//need to revisit what this does. it removes an element of an array and puts a new version of the element at the same index
//   										customFields.get(i).getOptions().remove(i);
//   										customFields.get(i).getOptions().add(i, mCustomFieldOptions);
//   									}
//   									else
//   									{
//   										customFields.get(i).getOptions().add(mCustomFieldOptions);
//   									}
//   								}
//   							}
//   							breakpoint = true;
//   						}
//   						
//   					});
//   					
//   					multiView.show();
//   				}
//   				
//   			});
//
//   			customFieldText.setVisibility(View.GONE);
//   		}//end multiselect
//   		else if(customFieldVO.getType().equals("singleselect"))
//   		{
//   			customFieldCheckbox.setVisibility(View.GONE);
//   			customFieldText.setVisibility(View.GONE);
//
//   			final ArrayList<CustomFieldOptionsVO> customFieldOptions = customFieldVO.getOptions();
//   			ArrayList<String> items = new ArrayList<String>();
//   			for(int iterator = 0; iterator < customFieldOptions.size(); iterator++)
//   			{
//   				items.add(customFieldOptions.get(iterator).getName()+":\n"+customFieldOptions.get(iterator).getDescription());
//   			}
//   			
//   			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.custom_singleselect, items); 
//   			/*new ArrayAdapter<String>(context,
//   			        android.R.layout.simple_spinner_dropdown_item, items);*/
//   					/*ArrayAdapter..createFromResource(getApplicationContext(), , R.layout.custom_singleselect);*/
//   	        adapter.setDropDownViewResource(R.layout.custom_singleselect);
//   			
//   			singleSelectSpinner = (Spinner)newView.findViewById(R.id.singleselectspinner);
//   			singleSelectSpinner.setAdapter(adapter);
//   			singleSelectSpinner.setVisibility(View.VISIBLE);
//   			singleSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
//
//   				@Override
//   				public void onItemSelected(AdapterView<?> arg0, View arg1,
//   						int arg2, long arg3) {
//   					// TODO Auto-generated method stub
//   					
//   					//save stuff here
//   					for(int i = 0; i < customFieldOptions.size(); i++)
//   					{
//   						
//   						if(customFieldOptions.get(i).isChecked()==true && i!=singleSelectSpinner.getSelectedItemPosition())
//   							customFieldOptions.get(i).setChecked(false);
//   						
//   					}
//   					
//   					CustomFieldOptionsVO singleOptionSet = customFieldOptions.get(singleSelectSpinner.getSelectedItemPosition());
//   					singleOptionSet.setChecked(true);
//   					customFieldOptions.remove(singleSelectSpinner.getSelectedItemPosition());
//   					customFieldOptions.add(singleSelectSpinner.getSelectedItemPosition(), singleOptionSet);
//   					
//   				}
//
//   				@Override
//   				public void onNothingSelected(AdapterView<?> arg0) {
//   					// TODO Auto-generated method stub
//   					
//   				}
//   				
//   			});
//   			
//   		}//end singleselect
//   		else{
//   			customFieldCheckbox.setVisibility(View.GONE);
//   			customFieldText.setVisibility(View.VISIBLE);
//   			if(customFieldVO.getValue()!=null){
//   				customFieldText.setText(customFieldVO.getValue());
//   			}
//   			
//   			customFieldMap.put(i, customFieldText);
//
//   			customFieldText.addTextChangedListener(new TextWatcher() {
//   				@Override
//   				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//   				@Override
//   				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//   				@Override
//   				public void afterTextChanged(Editable editable) {
//   					customFieldVO.setValue(customFieldText.getText().toString());
//   					
//   					//this.customFields.add(customFieldVO);
//   					
//   					//customFields..add(customFieldVO);
//   					//need to know this one's position
//   				}
//   			});
//   			
//   			//add to hashmap with index and customfieldvo
//   		}
//
//   		if(customFieldVO.getPublic())customIsPublic.setVisibility(View.VISIBLE);
//   		else customIsPublic.setVisibility(View.GONE);
//
//   		if(customFieldVO.getDescription()!=null){
//   			customFieldDescription.setVisibility(View.VISIBLE);
//   			customFieldDescription.setText(customFieldVO.getDescription());
//   		}
//   		else{
//   			customFieldDescription.setVisibility(View.GONE);
//   			customFieldDescription.setText("");
//   		}
//   		//Utils.useLatoInView(context, newView);
//   		if(customFieldTitle.getText().toString().contentEquals("Disclaimer"))
//   		{
//   			customFieldText.setVisibility(View.GONE);
//   		}/*else
//   			customFieldText.setVisibility(View.VISIBLE);*/
//
//   		
//           
//           innerList.addView(newView);
//           
//           customFields.remove(i);
//           customFields.add(0, customFieldVO);
//           
//    	   
//       }
//    }

    public void clickSubmit(View v){
        String alertMessage = "";
        for (int i = 0; i < this.customFields.size(); i++) {
                CustomFieldVO customFieldVO =  this.customFields.get(i);// (CustomFieldVO)customFieldsList.getAdapter().getItem(i);
                
                //options 
                
                if(customFieldVO.getType().equals("checkbox")){
                    custValues.put("custom_field_"+customFieldVO.getId(), customFieldVO.getValue());
                }
                else if(customFieldVO.getType().equals("multiselect") || customFieldVO.getType().equals("singleselect"))
                {
                	//another for loop
                	ArrayList<CustomFieldOptionsVO> options = customFieldVO.getOptions();
                	String optionsString = "";
                	ArrayList<String> optionsArray = new ArrayList<String>();
                	for(int j = 0; j < options.size(); j++)
                	{
                		if(options.get(j).isChecked())
                			optionsArray.add(options.get(j).getName());//optionsString = optionsString.concat("\""+options.get(j).getName()+"\"");
                	}
                	JSONArray newjson = new JSONArray(optionsArray);
					optionsString = newjson.toString();
                	custValues.put("custom_field_"+customFieldVO.getId(), optionsString);
                	//then do '[' and ']' at first and last character respectively;
                	
                	//CustomFieldOptionsVO customFieldOptionsVO = (CustomFieldOptionsVO)custom
                	//then turn options model into jsonstring
                	//custValues.put("custom_field_"+customFieldVO.getId(), optiosnmodel);
                }
                else{
                	
                	if(customFieldMap.containsKey(i))
                	{
                		//get textview, set value blah blah yay
                		customFieldVO.setValue(customFieldMap.get(i).getText().toString());
                	}
                	
                    if(customFieldVO.getRequired() && (customFieldVO.getValue()== null || customFieldVO.getValue().equals(""))){
                        alertMessage += (alertMessage.length()>0)?"\n":"" + String.format(CustomFieldsActivity.this.getString(R.string.requiredField), customFieldVO.getName());
                    }
                    else if (customFieldVO.getValue()!= null && !customFieldVO.getValue().equals("")){
                        custValues.put("custom_field_"+customFieldVO.getId(), customFieldVO.getValue());
                    }
                }
        } //end for
        
        
        if(!alertMessage.equals("")){

            final CustomAlertDialog dialog = new CustomAlertDialog(this, CustomFieldsActivity.this.getString(R.string.errorRequest), alertMessage, CustomFieldsActivity.this.getString(R.string.OkButton), null);

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
            HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("request_submit"), Utils.isNetworkAvailable(CustomFieldsActivity.this)){
                final CustomProgressDialog dialog = new CustomProgressDialog(CustomFieldsActivity.this, CustomFieldsActivity.this.getString(R.string.submitRequest));
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
                                CustomProgressDialog dialog = new CustomProgressDialog(CustomFieldsActivity.this, CustomFieldsActivity.this.getString(R.string.loadingRequest));
                                dialog.show();
                                Bundle b = new Bundle();
                                b.putInt("request_id", parser.getResponse().getInt("request_id"));

                                Intent intent = new Intent(CustomFieldsActivity.this, RequestDetailsActivity.class);
                                intent.putExtras(b);
                                startActivity(intent);
                                Intent data = new Intent();
                                CustomFieldsActivity.this.setResult(RESULT_OK, data);
                                finish();
                            }
                            else{
                                resultMessage = parser.getStatus().getMessage();
                                if(resultMessage.startsWith("You must be logged in")){
                                    final CustomAlertDialog alert = new CustomAlertDialog(CustomFieldsActivity.this, CustomFieldsActivity.this.getString(R.string.loginNow), String.format(CustomFieldsActivity.this.getString(R.string.submitrequestto), requestData.getCity()), CustomFieldsActivity.this.getString(R.string.loginNow), CustomFieldsActivity.this.getString(R.string.cancel));

                                    View.OnClickListener listener = new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            int id = v.getId();
                                            switch(id){
                                                case R.id.alertConfirm:
                                                    Intent intent = new Intent(CustomFieldsActivity.this, CreateAccountActivity.class);

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
                                    final CustomAlertDialog alert = new CustomAlertDialog(CustomFieldsActivity.this, resultTitle, resultMessage, CustomFieldsActivity.this.getString(R.string.OkButton), null);
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
                    }else {
                        Utils.noConnection(CustomFieldsActivity.this);
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                }
            };
            //sent with every request
            clientPost.addParameter("zipcode", requestData.getZipcode());
            clientPost.addParameter("address", requestData.getAddress());
            clientPost.addParameter("space_id", String.valueOf(requestData.getSpaceId()));
            clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
            clientPost.addParameter("request_type_id", String.valueOf(requestData.getRequestType().getId()));
            clientPost.addParameter("is_private", (requestData.getPrivate()) ? "1" : "0");
            clientPost.addParameter("title", requestData.getTitle());
            if(requestData.getDescription()!=null) clientPost.addParameter("description", requestData.getDescription());
            clientPost.addParameter("used_location", "1");
            //switched because of columns in db
            clientPost.addParameter("lat", String.valueOf(requestData.getLongitude()));
            clientPost.addParameter("lon", String.valueOf(requestData.getLatitude()));
            //for images
            if(requestData.getPathToImage()!=null){
                clientPost.addParameter("has_image", "1");
                clientPost.addParameter("image", requestData.getPathToImage());
            }
            if(app.getUserApiKey()!=null && !app.getUserApiKey().equals("")) clientPost.addParameter("api_key", app.getUserApiKey());
            Set<Map.Entry<String, String>> set = custValues.entrySet();

            for (Map.Entry<String, String> entry : set) {
                clientPost.addParameter(entry.getKey(), entry.getValue());
            }
            clientPost.execute(new Void[0]);
        }
    }

    private void saveAsDraft(){
        ArrayList<RequestDraftVO> requestDraftVOs = app.getUserDrafts();
        requestData.setCustomFields(new CustomFieldArrayVO(this.customFields));
        if(requestDraft>-1){
            requestDraftVOs.set(requestDraft, requestData);
        }
        else{
            requestDraftVOs.add(requestData);
        }
        app.setUserDrafts(requestDraftVOs);
        Intent data = new Intent();
        CustomFieldsActivity.this.setResult(RESULT_OK, data);
        finish();
    }

    public void back(View v){
        requestData.setCustomFields(new CustomFieldArrayVO(this.customFields));
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable("requestData", requestData);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
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
