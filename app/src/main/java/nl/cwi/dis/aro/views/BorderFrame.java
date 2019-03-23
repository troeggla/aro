package nl.cwi.dis.aro.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BorderFrame extends View {
    private static final int DEFAULT_SIZE = 100;
    private Paint paint;

    public BorderFrame(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.argb(0, 0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setFrameColor(int a, int r, int g, int b) {
        paint.setColor(Color.argb(a, r, g, b));
        invalidate();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = DEFAULT_SIZE;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = DEFAULT_SIZE;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        paint.setStrokeWidth((float)(height * 0.04));
        canvas.drawRect(0, 0, width, height, paint);
    }
}
