package bundle.android.views.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class SquareGridLayout extends ImageView {

    public SquareGridLayout(Context context) {
        super(context);
    }

    public SquareGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    /*@Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (getLayoutParams() != null && w != h) {
            getLayoutParams().height = w;
            setLayoutParams(getLayoutParams());
        }
    }*/
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width > (int)(1 * height + 0.5)) {
            width = (int)(1 * height + 0.5);
        } else {
            height = (int)(width / 1 + 0.5);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}
