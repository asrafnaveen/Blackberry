package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.model.vo.UserDetailVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;


public class UserDetailsListAdapter extends BaseAdapter {

    private final List<UserDetailVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public UserDetailsListAdapter(Context context, List<UserDetailVO> listItems, LayoutInflater inflater) {
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
        View newView = inflater.inflate(R.layout.user_details_cell, null);
        UserDetailVO userDetailVO = listItems.get(position);
        ImageView imageView = (ImageView)newView.findViewById(R.id.userListIcon);
        TextView title = (TextView)newView.findViewById(R.id.userListTitle);
        TextView count = (TextView)newView.findViewById(R.id.userListCount);
        imageView.setImageResource(userDetailVO.getImage());
        title.setText(userDetailVO.getTitle());
        count.setText(String.valueOf(userDetailVO.getCount()));
        
        title.setTypeface(face);
        count.setTypeface(face);
        //Utils.useLatoInView(context, newView);
        return newView;

    }
}