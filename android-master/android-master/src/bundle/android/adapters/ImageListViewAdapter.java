package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.model.vo.ImageDialogVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.List;

import com.wassabi.psmobile.R.*;

public class ImageListViewAdapter extends BaseAdapter {

    private final List<ImageDialogVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public ImageListViewAdapter(Context context, List<ImageDialogVO> listItems, LayoutInflater inflater) {
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
        View newView = inflater.inflate(layout.list_item_image, null);
        ImageDialogVO imageDialogVO = listItems.get(position);
        ImageView imageView = (ImageView)newView.findViewById(id.listItemImageView);
        TextView title = (TextView)newView.findViewById(id.listItemTitle);
        TextView subtitle = (TextView)newView.findViewById(id.listItemSubtitle);
        imageView.setImageResource(imageDialogVO.getImage());
        title.setText(imageDialogVO.getTitle());
        if(imageDialogVO.getSubTitle()!=null){
            subtitle.setText(imageDialogVO.getSubTitle());
        }
        else subtitle.setVisibility(View.GONE);
        newView.setSelected(imageDialogVO.getSelected());
        if(newView.isSelected()){
            newView.setBackgroundColor(context.getResources().getColor(R.color.ORANGE));
        }
        
        title.setTypeface(face);
        subtitle.setTypeface(face);
        //Utils.useLatoInView(context, newView);
        return newView;

    }
}
