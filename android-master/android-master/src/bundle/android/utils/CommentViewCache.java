package bundle.android.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.wassabi.psmobile.R;


public class CommentViewCache {

    private final View baseView;
    private TextView commentUser;
    private TextView commentDate;
    private TextView commentText;
    private ImageView imageView;

    public CommentViewCache(View baseView) {
        this.baseView = baseView;
    }

    public TextView getUsernameView() {
        if (commentUser == null) {
           commentUser = (TextView) baseView.findViewById(R.id.commentUser);
        }
        return commentUser;
    }

    public TextView getDateView() {
        if (commentDate == null) {
            commentDate = (TextView) baseView.findViewById(R.id.commentDate);
        }
        return commentDate;
    }

    public TextView getTextView() {
        if (commentText == null) {
            commentText = (TextView) baseView.findViewById(R.id.commentText);
        }
        return commentText;
    }

    public ImageView getImageView() {
        if (imageView == null) {
            imageView = (ImageView) baseView.findViewById(R.id.commentImage);
        }
        return imageView;
    }
}
