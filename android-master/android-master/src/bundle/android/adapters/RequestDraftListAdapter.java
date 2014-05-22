package bundle.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.model.vo.RequestDraftVO;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.io.File;
import java.util.List;


public class RequestDraftListAdapter extends BaseAdapter {

    private final List<RequestDraftVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final Typeface face;

    public RequestDraftListAdapter(Context context, List<RequestDraftVO> listItems, LayoutInflater inflater) {
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
        RequestDraftVO requestDraftVO = listItems.get(position);
        View newView = inflater.inflate(R.layout.draft_cell, null);
        ImageView requestImage = (ImageView)newView.findViewById(R.id.requestImage);
        if(requestDraftVO.getPathToImage()!=null){
            String pathToImage =  requestDraftVO.getPathToImage();
            File selFile=new File(pathToImage);
            Bitmap thumbnailBmp = Utils.decodeFile(selFile);
            requestImage.setImageBitmap(thumbnailBmp);
            requestImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else requestImage.setVisibility(View.GONE);
        TextView requestTitle = (TextView)newView.findViewById(R.id.requestTitle);
        TextView requestAddress = (TextView)newView.findViewById(R.id.requestAddress);
        TextView requestDescription = (TextView)newView.findViewById(R.id.requestDescription);
        if (requestDraftVO.getTitle()!=null) requestTitle.setText(requestDraftVO.getTitle());
        if (requestDraftVO.getAddress()!=null){
            requestAddress.setText(requestDraftVO.getAddress() +", " +requestDraftVO.getCity() + ", " +requestDraftVO.getState() + " " +requestDraftVO.getZipcode());
        }
        else requestAddress.setVisibility(View.GONE);
        if(requestDraftVO.getDescription()!=null) requestDescription.setText(requestDraftVO.getDescription());
        else requestDescription.setVisibility(View.GONE);
        //Utils.useLatoInView(context, newView);
        
        requestTitle.setTypeface(face);
        requestAddress.setTypeface(face);
        requestDescription.setTypeface(face);
        
        return newView;
    }
}