package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.model.vo.CityVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.ArrayList;


public class CitiesListViewAdapter extends BaseAdapter {

    private final ArrayList<CityVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public CitiesListViewAdapter(Context context, ArrayList<CityVO> listItems, LayoutInflater inflater) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
       View newView = inflater.inflate(R.layout.city_list_cell, null);
        CityVO cityVO = listItems.get(position);
        TextView cityName = (TextView)newView.findViewById(R.id.cityName);
        ImageView isClient = (ImageView)newView.findViewById(R.id.isClient);
        ImageView isSelected = (ImageView)newView.findViewById(R.id.isSelected);
        cityName.setText(cityVO.getCityName() +", " + cityVO.getCityStateAbbr());
        if(cityVO.getIsClient()) isClient.setVisibility(View.VISIBLE);
        else isClient.setVisibility(View.INVISIBLE);
        if(cityVO.getSelected()) isSelected.setVisibility(View.VISIBLE);
        else isSelected.setVisibility(View.INVISIBLE);
        //Utils.useLatoInView(context, newView);
        cityName.setTypeface(face);
        
        return newView;

    }

}
