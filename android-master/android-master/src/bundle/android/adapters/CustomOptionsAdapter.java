package bundle.android.adapters;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import bundle.android.model.vo.CustomFieldOptionsVO;
import bundle.android.model.vo.CustomFieldVO;
import bundle.android.utils.DataStore;
import bundle.android.PublicStuffApplication;

import com.wassabi.psmobile.R;

public class CustomOptionsAdapter  extends BaseAdapter {

	private ArrayList<CustomFieldOptionsVO> listItems;
	private final Context context;
	private final LayoutInflater inflater;
	private final Typeface face;
	private PublicStuffApplication app;
	private DataStore store;

	public CustomOptionsAdapter(Context context, ArrayList<CustomFieldOptionsVO> listItems, LayoutInflater inflater) {
		this.context = context;
		this.listItems = listItems;
		this.inflater = inflater;
		this.face = Typeface.createFromAsset(context.getAssets(), "lato_regular.ttf");
		app = (PublicStuffApplication)context.getApplicationContext();
		store = new DataStore(app);
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public CustomFieldOptionsVO getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View newView = inflater.inflate(R.layout.custom_multiselect_cell, null);
		//LinearLayout multiViewRoot = (LinearLayout)newView.findViewById(R.id.multiviewroot);

		//there will only be one dialog open at a time, and the jsonarray will only get generated per position I think


		final CustomFieldOptionsVO currentOption;
		currentOption = listItems.get(position);
		currentOption.getId();

		//newView..addView(newTextView);
		TextView name = (TextView)newView.findViewById(R.id.customFieldTitle);
		name.setText(currentOption.getName());


		TextView description = (TextView)newView.findViewById(R.id.customDescription);
		description.setText(currentOption.getDescription());


		CheckBox selector = (CheckBox)newView.findViewById(R.id.customFieldCheckbox);

		selector.setChecked(currentOption.isChecked());

		selector.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				//write option to custom fields thing
				//app.getSharedPreferences("", mode)
				//String optionsString = store.getFromPrefs("custom_field_"+currentOption.getSuperid(), "");
				listItems.set(position, new CustomFieldOptionsVO(currentOption.getSuperid(), currentOption.getId(), currentOption.getName(), currentOption.getDescription(), isChecked));
				//optionsString = 

				//store.saveToPrefs("custom_field_"+currentOption.getSuperid(), "Option "+position+1);
			}

		});

		return newView;
	}
}