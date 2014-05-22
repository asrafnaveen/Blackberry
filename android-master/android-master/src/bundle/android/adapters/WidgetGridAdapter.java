package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.PublicStuffApplication;
import bundle.android.model.vo.WidgetVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;

public class WidgetGridAdapter extends BaseAdapter

{

    private final List<WidgetVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final PublicStuffApplication app;
    private final int page;
    private final Typeface face;

    public WidgetGridAdapter(Context context, List<WidgetVO> listItems, LayoutInflater inflater, int page) {
        this.context = context;
        this.listItems = listItems;
        this.inflater = inflater;
        this.page = page;
        this.app = (PublicStuffApplication)context.getApplicationContext();
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
        View newView = inflater.inflate(R.layout.widget, null);
        WidgetVO widgetVO = listItems.get(position);
        ImageView imageView = (ImageView)newView.findViewById(R.id.widgetImage);
        TextView title = (TextView)newView.findViewById(R.id.widgetText);
        Drawable icon = (widgetVO.getImage()!=0)?context.getResources().getDrawable(widgetVO.getImage()):context.getResources().getDrawable(R.drawable.widget_forum);
        if(app.getCityBaseColor()!=null) Utils.addColorFilter(icon, app.getCityBaseColor());
        else Utils.addColorFilter(icon, "#232323");
        imageView.setImageDrawable(icon);
        title.setText(widgetVO.getTitle());
        //Utils.useLatoInView(context, newView);
        title.setTypeface(face);
        
        return newView;
    }
}