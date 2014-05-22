package bundle.android.views.layouts;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.wassabi.psmobile.R;


public class BalloonLayout extends LinearLayout {
    public BalloonLayout(Context context) {
        super(context);
    }
    public BalloonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    //first angle of arrow, height of arrow, width of arrow
    private final double theta = 135.0;
    private final int tipHeight = 16;
    private final int tipWidth = 32;
    private final int color = R.color.REQUEST_LISTMAP;

    private Canvas canvas;
    private Paint panelPaint;
    @Override
    protected void dispatchDraw(Canvas c) {
        this.canvas = c;
        panelPaint  = new Paint();
        panelPaint.setColor(color);
        setContentFrame();
        drawTip();


        super.dispatchDraw(canvas);
    }

    private void setContentFrame(){
        RectF rect = new RectF();
        rect.set(0,0, getMeasuredWidth(), getMeasuredHeight()-tipHeight);

        canvas.drawRoundRect(rect, 0, 0, panelPaint);
    }
    private void  drawTip(){
        Path tip = new Path();
        int w1 = (int)(tipHeight/Math.sin((180-theta)));
        int w2 = tipWidth - w1;
        //tip.moveTo(0,0);
        //tip.lineTo(getMeasuredWidth(), 0);
        //tip.lineTo(getMeasuredWidth(), getMeasuredHeight());
        tip.moveTo(getMeasuredWidth()/2 + w2, getMeasuredHeight()-tipHeight);
        tip.lineTo(getMeasuredWidth()/2, getMeasuredHeight());
        tip.lineTo(getMeasuredWidth()/2 - w1, getMeasuredHeight()-tipHeight);
        //tip.lineTo(0, getMeasuredHeight());
        //tip.lineTo(0,0);

        canvas.drawPath(tip, panelPaint);
    }
}
