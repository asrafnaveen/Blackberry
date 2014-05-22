package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import bundle.android.model.tasks.HttpImageLoader;
import bundle.android.model.vo.RequestListVO;
import bundle.android.utils.RequestListViewCache;
import bundle.android.utils.Utils;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.wassabi.psmobile.R;

import java.util.List;


public class RequestListViewAdapter extends BaseAdapter {

    private final List<RequestListVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final HttpImageLoader imageLoader;
    private final ListView listView;
    private final Typeface face;

    public RequestListViewAdapter(Context context, List<RequestListVO> listItems, LayoutInflater inflater, ListView listView) {
        this.context = context;
        this.listItems = listItems;
        this.inflater = inflater;
        this.listView = listView;
        this.imageLoader = new HttpImageLoader();
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
        View newView = convertView;
        RequestListViewCache viewCache;
        if(newView == null){
            newView = inflater.inflate(R.layout.request_list_cell, null);
            viewCache = new RequestListViewCache(newView);
            newView.setTag(viewCache);
        }
        else{
            viewCache = (RequestListViewCache) newView.getTag();
        }
        RequestListVO requestListVO = listItems.get(position);
        String imageUrl = requestListVO.getImage();
        final ImageView imageView = viewCache.getRequestImageView();
        imageView.setTag(requestListVO.getImage());
        if(imageUrl!=null && !imageUrl.equals("")){
            /*Drawable cachedImage = imageLoader.loadDrawable(imageUrl, Utils.isNetworkAvailable(context), new HttpImageLoader.ImageCallback() {
                public void imageLoaded(Drawable imageDrawable, String imageUrl) {
                    ImageView imageViewByTag = (ImageView) listView.findViewWithTag(imageUrl);
                    if (imageViewByTag != null) {
                        imageViewByTag.setImageDrawable(imageDrawable);
                    }
                }
            });

            imageView.setImageDrawable(cachedImage);*/
        	
        	UrlImageViewHelper.setUrlDrawable(imageView, imageUrl, R.drawable.loading_trans);

        }
        else imageView.setImageDrawable(null);
        TextView requestTitle = viewCache.getRequestTitleView();
        TextView requestAddress = viewCache.getRequestAddressView();
        TextView  followersText = viewCache.getFollowersTextView();
        TextView commentText = viewCache.getCommentTextView();
        TextView  distanceText = viewCache.getDistanceTextView();
        
        //init text change
        requestTitle.setTypeface(face);
        requestAddress.setTypeface(face);
        followersText.setTypeface(face);
        commentText.setTypeface(face);
        distanceText.setTypeface(face);
        
        
        ImageView statusImage  = viewCache.getStatusImageView();
        ImageView myRequestDogEar = viewCache.getDogEarView();
        ImageView followingIcon = viewCache.getFollowingIconView();
        ImageView commentIcon = viewCache.getCommentIconView();
        requestTitle.setText(requestListVO.getTitle());
        requestAddress.setText(requestListVO.getAddress());
        followersText.setText(String.valueOf(requestListVO.getFollowersCount()));
        distanceText.setText(requestListVO.getDistance());
        commentText.setText(String.valueOf(requestListVO.getCommentsCount()));
        statusImage.setImageResource(requestListVO.getStatusImage());
        if(requestListVO.getUserRequest())myRequestDogEar.setVisibility(View.VISIBLE);
        else myRequestDogEar.setVisibility(View.GONE);
        if(requestListVO.getUserFollowing())followingIcon.setImageResource(R.drawable.ic_orange_following_small);
        else followingIcon.setImageResource(R.drawable.ic_light_following_small);
        if(requestListVO.getUserComment())commentIcon.setImageResource(R.drawable.ic_orange_comment_small);
        else commentIcon.setImageResource(R.drawable.ic_light_comment_small);
        //Utils.useLatoInView(context, newView);
        //System.gc();
        return newView;
    }


}
