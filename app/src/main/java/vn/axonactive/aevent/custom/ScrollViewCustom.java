package vn.axonactive.aevent.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import vn.axonactive.aevent.R;

/**
 * Created by Dell on 3/30/2017.
 */

public class ScrollViewCustom extends ScrollView {

    public static int WITHOUT_MAX_HEIGHT_VALUE = -1;
    public static int WITHOUT_MIN_HEIGHT_VALUE = Integer.MAX_VALUE;

    private int maxHeight = WITHOUT_MAX_HEIGHT_VALUE;

    private int minHeight = WITHOUT_MIN_HEIGHT_VALUE;

    public ScrollViewCustom(Context context) {
        super(context);
    }


    public ScrollViewCustom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollViewCustom,
                0, 0);
        try {
            setMaxHeight(a.getDimensionPixelSize(R.styleable.ScrollViewCustom_max_height, WITHOUT_MAX_HEIGHT_VALUE));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE
                    && heightSize > maxHeight) {
                heightSize = maxHeight;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
            getLayoutParams().height = heightSize;
        } catch (Exception e) {
            //error
        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

}
