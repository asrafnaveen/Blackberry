package bundle.android.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import bundle.android.views.layouts.SquareGridLayout;

import com.wassabi.psmobile.R;


public class RequestGalleryViewCache {
    private final View baseView;
    private TextView requestTitle;
    private TextView  followersText;
    private TextView commentText;
    private SquareGridLayout requestImage;
    private ImageView myRequestDogEar;
    private ImageView statusImage;
    private ImageView followingIcon;
    private ImageView commentIcon;

    public RequestGalleryViewCache(View baseView) {
        this.baseView = baseView;
    }

    public TextView getRequestTitleView() {
        if (requestTitle == null) {
            requestTitle = (TextView) baseView.findViewById(R.id.requestTitle);
        }
        return requestTitle;
    }

    public TextView getFollowersTextView() {
        if (followersText == null) {
            followersText = (TextView) baseView.findViewById(R.id.followersText);
        }
        return followersText;
    }

    public TextView getCommentTextView() {
        if (commentText == null) {
            commentText = (TextView) baseView.findViewById(R.id.commentText);
        }
        return commentText;
    }


    public SquareGridLayout getRequestImageView() {
        if (requestImage == null) {
            requestImage = (SquareGridLayout) baseView.findViewById(R.id.requestImage);
        }
        return requestImage;
    }
    public ImageView getDogEarView() {
        if (myRequestDogEar == null) {
            myRequestDogEar = (ImageView) baseView.findViewById(R.id.myRequestDogEar);
        }
        return myRequestDogEar;
    }
    public ImageView getStatusImageView() {
        if (statusImage == null) {
            statusImage = (ImageView) baseView.findViewById(R.id.statusImage);
        }
        return statusImage;
    }

    public ImageView getFollowingIconView() {
        if (followingIcon == null) {
            followingIcon = (ImageView) baseView.findViewById(R.id.followingIcon);
        }
        return followingIcon;
    }
    public ImageView getCommentIconView() {
        if (commentIcon == null) {
            commentIcon = (ImageView) baseView.findViewById(R.id.commentIcon);
        }
        return commentIcon;
    }

}

