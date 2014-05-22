package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import bundle.android.model.vo.NotificationVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;

public class NotificationListAdapter extends BaseAdapter {

    private final List<NotificationVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public NotificationListAdapter(Context context, List<NotificationVO> listItems, LayoutInflater inflater) {
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
        View newView = inflater.inflate(R.layout.notification_cell, null);
        NotificationVO notificationVO = listItems.get(position);
        TextView text = (TextView)newView.findViewById(R.id.notificationText);
        TextView date = (TextView)newView.findViewById(R.id.notificationDate);
        if(notificationVO.getIsComment()) text.setText(R.string.notificationComment);
        else if(notificationVO.getIsCommentReply())text.setText(R.string.notificationReply);
        else if(notificationVO.getIsStatusUpdate())text.setText(String.format(context.getString(R.string.notificationStatus),notificationVO.getStatus()));
        date.setText(Utils.parseDate(notificationVO.getDate(), context));
        if(!notificationVO.getRead()) newView.setBackgroundColor(context.getResources().getColor(R.color.HIGHLIGHT_ORANGE));
        //Utils.useLatoInView(context, newView);
        
        text.setTypeface(face);
        date.setTypeface(face);
        
        return newView;

    }
}
