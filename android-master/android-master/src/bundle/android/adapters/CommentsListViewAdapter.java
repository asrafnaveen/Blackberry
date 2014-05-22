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
import bundle.android.model.vo.CommentVO;
import bundle.android.utils.CommentViewCache;
import bundle.android.utils.Utils;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.wassabi.psmobile.R;

import java.util.List;

public class CommentsListViewAdapter extends BaseAdapter {

    private final List<CommentVO> listItems;
    private final Context context;
    private final LayoutInflater inflater;
    private final HttpImageLoader imageLoader;
    private final ListView listView;
    private final Typeface face;
    

    public CommentsListViewAdapter(Context context, List<CommentVO> listItems, LayoutInflater inflater, ListView listView) {
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
        CommentViewCache viewCache;
       if(newView == null){
           newView = inflater.inflate(R.layout.comment, null);
           viewCache = new CommentViewCache(newView);
           newView.setTag(viewCache);
       }
        else{
           viewCache = (CommentViewCache) newView.getTag();
       }
        CommentVO commentVO = listItems.get(position);
        String imageUrl = commentVO.getImage();
        final ImageView imageView = viewCache.getImageView();
        imageView.setTag(commentVO.getImage());
        if(imageUrl!=null && !imageUrl.equals("")){
            /*Drawable cachedImage = imageLoader.loadDrawable(imageUrl, Utils.isNetworkAvailable(context), new HttpImageLoader.ImageCallback(){
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
        TextView commentUser = viewCache.getUsernameView();
        TextView commentDate = viewCache.getDateView();
        TextView commentText = viewCache.getTextView();
        commentUser.setText(commentVO.getUser());
        commentText.setText(commentVO.getText());
        commentDate.setText(commentVO.getDate());
        
        commentUser.setTypeface(face);
        commentText.setTypeface(face);
        commentDate.setTypeface(face);
        //Utils.useLatoInView(context, newView);
        //System.gc();
        return newView;
    }


}