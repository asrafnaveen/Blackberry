package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;

public class SingleListViewAdapter extends BaseAdapter {

    private final List<String> strings;
    private final Context   context;
    private final LayoutInflater inflater;
    private final int selected;
    private final Typeface face;

    public SingleListViewAdapter(Context context, List<String> strings, LayoutInflater inflater, int selected) {
        this.context = context;
        this.strings = strings;
        this.inflater = inflater;
        this.selected = selected;
        this.face = Typeface.createFromAsset(context.getAssets(), "lato_regular.ttf");
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       View newView = inflater.inflate(R.layout.request_type_item, null);
        String string = strings.get(position);
        TextView tv = (TextView)newView.findViewById(R.id.request_type_item);
        tv.setText(string);
        if(position == selected){
            tv.setBackgroundColor(context.getResources().getColor(R.color.ORANGE));
        }

        //Utils.useLatoInView(context, tv);
        tv.setTypeface(face);
        
        return newView;

    }
}