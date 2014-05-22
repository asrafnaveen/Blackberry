package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.model.vo.RequestTypeVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;


public class RequestTypeListAdapter extends BaseAdapter {

    private final List<RequestTypeVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public RequestTypeListAdapter(Context context, List<RequestTypeVO> listItems, LayoutInflater inflater) {
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
        View newView = inflater.inflate(R.layout.request_type_cell, null);
        RequestTypeVO requestTypeVO = listItems.get(position);
        TextView name = (TextView)newView.findViewById(R.id.requestTypeTitle);
        TextView description = (TextView)newView.findViewById(R.id.requestTypeDescription);
        name.setText(requestTypeVO.getName());
        ImageView navi = (ImageView)newView.findViewById(R.id.navi);
        
        if(requestTypeVO.getIsCategory()){
        	navi.setVisibility(View.VISIBLE);
        }
        else
        	navi.setVisibility(View.GONE);
        
        if(requestTypeVO.getDescription()!=null){
            description.setText(requestTypeVO.getDescription());
            description.setVisibility(View.VISIBLE);
        }
        else{
            description.setText("");
            description.setVisibility(View.GONE);
        }
        newView.setSelected(requestTypeVO.getSelected());
        if(newView.isSelected()){
            newView.setBackgroundColor(context.getResources().getColor(R.color.ORANGE));
        }
        
        name.setTypeface(face);
        description.setTypeface(face);
        
        //Utils.useLatoInView(context, newView);
        return newView;

    }
}