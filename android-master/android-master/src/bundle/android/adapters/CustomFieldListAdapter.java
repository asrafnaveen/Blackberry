package bundle.android.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import bundle.android.model.vo.CustomFieldOptionsVO;
import bundle.android.model.vo.CustomFieldVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CustomFieldListAdapter  extends BaseAdapter {

	private final List<CustomFieldVO> listItems;
	private final Context context;
	private final LayoutInflater inflater;
	private final Typeface face;
	private CustomOptionsAdapter multiSelectAdapter;
	Spinner singleSelectSpinner;
	EditText customFieldText;

	public CustomFieldListAdapter(Context context, List<CustomFieldVO> listItems, LayoutInflater inflater) {
		this.context = context;
		this.listItems = listItems;
		this.inflater = inflater;
		this.face = Typeface.createFromAsset(context.getAssets(), "lato_regular.ttf");
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View newView = inflater.inflate(R.layout.custom_field_cell, null);
		final CustomFieldVO customFieldVO = listItems.get(position);
		TextView customFieldTitle = (TextView)newView.findViewById(R.id.customFieldTitle);
		TextView customFieldIsRequired = (TextView)newView.findViewById(R.id.customFieldIsRequired);
		customFieldText = (EditText)newView.findViewById(R.id.customFieldText);
		final CheckBox customFieldCheckbox = (CheckBox)newView.findViewById(R.id.customFieldCheckbox);
		TextView customIsPublic = (TextView)newView.findViewById(R.id.customIsPublic);
		TextView customFieldDescription = (TextView)newView.findViewById(R.id.customFieldDescription);

		customFieldTitle.setTypeface(face);
		customFieldIsRequired.setTypeface(face);
		customIsPublic.setTypeface(face);
		customFieldDescription.setTypeface(face);

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
		}
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
					
					multiSelectAdapter = new CustomOptionsAdapter(context, customFieldOptions, inflater);
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
									listItems.get(position).getOptions().remove(i);
									listItems.get(position).getOptions().add(i, mCustomFieldOptions);
								}
							}
							breakpoint = true;
						}
						
					});
					
					multiView.show();
				}
				
			});

			customFieldText.setVisibility(View.GONE);
		}
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
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
			        android.R.layout.simple_spinner_dropdown_item, items);
			
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
					
					/*if(mCustomFieldOptions.isChecked())
					{
						mCustomFieldOptions.getName(); //save name somewhere
						listItems.get(position).getOptions().remove(i);
						listItems.get(position).getOptions().add(i, mCustomFieldOptions);*/
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
		}
		else{
			customFieldCheckbox.setVisibility(View.GONE);
			customFieldText.setVisibility(View.VISIBLE);
			if(customFieldVO.getValue()!=null){
				customFieldText.setText(customFieldVO.getValue());
			}

			customFieldText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

				@Override
				public void afterTextChanged(Editable editable) {
					customFieldVO.setValue(customFieldText.getText().toString());
				}
			});
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

		return newView;

	}
}