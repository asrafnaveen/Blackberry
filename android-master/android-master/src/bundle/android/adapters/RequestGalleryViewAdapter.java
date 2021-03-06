package bundle.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import bundle.android.model.tasks.HttpImageLoader;
import bundle.android.model.vo.RequestListVO;
import bundle.android.utils.RequestGalleryViewCache;
import bundle.android.utils.Utils;
import bundle.android.views.layouts.SquareGridLayout;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.wassabi.psmobile.R;

import java.util.List;


public class RequestGalleryViewAdapter extends BaseAdapter

{

	private final List<RequestListVO> listItems;
	private final Context context;
	private final LayoutInflater inflater;
	private final HttpImageLoader imageLoader;
	private final GridView gridView;
	private final Typeface face;

	public RequestGalleryViewAdapter(Context context, List< RequestListVO > listItems, LayoutInflater inflater, GridView gridView) {
		this.context = context;
		this.listItems = listItems;
		this.inflater = inflater;
		this.gridView = gridView;
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
		RequestGalleryViewCache viewCache;
		if(newView == null){
			newView = inflater.inflate(R.layout.request_gallery_cell, null);
			viewCache = new RequestGalleryViewCache(newView);
			newView.setTag(viewCache);
		}
		else{
			viewCache = (RequestGalleryViewCache) newView.getTag();
		}
		RequestListVO requestListVO = listItems.get(position);
		String imageUrl = requestListVO.getImage();
		
		final SquareGridLayout imageView = viewCache.getRequestImageView();
		imageView.setTag(requestListVO.getImage());
		imageView.setImageResource(R.drawable.loading_trans);
		if(imageUrl!=null && !imageUrl.equals("")){
			
			
			/*Drawable cachedImage = imageLoader.loadDrawable(imageUrl, Utils.isNetworkAvailable(context), new HttpImageLoader.ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, String imageUrl) {
					ImageView imageViewByTag = (ImageView) gridView.findViewWithTag(imageUrl);
					if (imageViewByTag != null) {
						imageViewByTag.setImageDrawable(imageDrawable);
					}
				}
			});

			imageView.setImageDrawable(cachedImage);*/
			
			UrlImageViewHelper.setUrlDrawable(imageView, imageUrl, R.drawable.loading_trans);

		}
		else 
			imageView.setImageResource(R.drawable.loading_opaque);

		
		TextView requestTitle = viewCache.getRequestTitleView();
		TextView  followersText = viewCache.getFollowersTextView();
		TextView commentText = viewCache.getCommentTextView();
		
		requestTitle.setTypeface(face);
        followersText.setTypeface(face);
        commentText.setTypeface(face);
		
		ImageView statusImage  = viewCache.getStatusImageView();
		ImageView myRequestDogEar = viewCache.getDogEarView();
		ImageView followingIcon = viewCache.getFollowingIconView();
		ImageView commentIcon = viewCache.getCommentIconView();
		requestTitle.setText(requestListVO.getTitle());
		followersText.setText(String.valueOf(requestListVO.getFollowersCount()));
		commentText.setText(String.valueOf(requestListVO.getCommentsCount()));
		statusImage.setImageResource(requestListVO.getStatusImage());
		if(requestListVO.getUserRequest())
			myRequestDogEar.setVisibility(android.view.View.VISIBLE);
		else 
			myRequestDogEar.setVisibility(android.view.View.GONE);
		if(requestListVO.getUserFollowing())followingIcon.setImageResource(R.drawable.ic_orange_following_small);
		else followingIcon.setImageResource(R.drawable.ic_dark_following_small);
		if(requestListVO.getUserComment())commentIcon.setImageResource(R.drawable.ic_orange_comment_small);
		else commentIcon.setImageResource(R.drawable.ic_dark_comment_small);
		//Utils.useLatoInView(context, newView);
		//System.gc();
		//imageView.onSizeChanged(imageView.getLayoutParams().width, 0, 0, 0);
		return newView;
	}
}

