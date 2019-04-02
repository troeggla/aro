package nl.cwi.dis.aro.views;

import android.view.View;
import android.view.MotionEvent;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.util.Log;
import android.graphics.PixelFormat;

import nl.cwi.dis.aro.R;
import nl.cwi.dis.aro.views.directionhandlers.DiagonalFourWayDirectionHandler;
import nl.cwi.dis.aro.views.directionhandlers.DirectionHandler;
import nl.cwi.dis.aro.views.directionhandlers.EightWayDirectionHandler;
import nl.cwi.dis.aro.views.directionhandlers.FourWayDirectionHandler;
import nl.cwi.dis.aro.views.directionhandlers.HorizontalDirectionHandler;
import nl.cwi.dis.aro.views.directionhandlers.VerticalDirectionHandler;

public class RockerView extends View {
    public static final String LOG_TAG = "RockerView";

    public enum CallBackMode {
        CALL_BACK_MODE_MOVE,
        CALL_BACK_MODE_STATE_CHANGE,
    }

    public enum DirectionMode {
        DIRECTION_2_HORIZONTAL,
        DIRECTION_2_VERTICAL,
        DIRECTION_4_ROTATE_0,
        DIRECTION_4_ROTATE_45,
        DIRECTION_8
    }

    public enum Direction {
        DIRECTION_LEFT,
        DIRECTION_RIGHT,
        DIRECTION_UP,
        DIRECTION_DOWN,
        DIRECTION_UP_LEFT,
        DIRECTION_UP_RIGHT,
        DIRECTION_DOWN_LEFT,
        DIRECTION_DOWN_RIGHT,
        DIRECTION_CENTER
    }

    public interface OnDirectionChangeListener {
        void onStart();
        void onDirectionChanged(Direction direction);
        void onFinish();
    }

    public interface OnAngleChangeListener {
        void onStart();
        void onAngleChanged(double angle);
        void onFinish();
    }

    public interface OnDistanceLevelListener {
        void onDistanceLevel(int level);
    }

    private static final int DEFAULT_SIZE = 600;
    private static final float DEFAULT_ROCKER_SCALE = 0.8f;

    private static final int AREA_BACKGROUND_MODE_PIC = 0;
    private static final int AREA_BACKGROUND_MODE_COLOR = 1;
    private static final int AREA_BACKGROUND_MODE_XML = 2;
    private static final int AREA_BACKGROUND_MODE_DEFAULT = 3;

    private static final int ROCKER_BACKGROUND_MODE_PIC = 4;
    private static final int ROCKER_BACKGROUND_MODE_COLOR = 5;
    private static final int ROCKER_BACKGROUND_MODE_XML = 6;
    private static final int ROCKER_BACKGROUND_MODE_DEFAULT = 7;

    private Paint mAreaBackgroundPaint;
    private Paint mRockerPaint;

    private Point mRockerPosition;
    private Point mCenterPoint;

    private int mAreaRadius;
    private float mRockerScale;

    private int mRockerRadius;

    private CallBackMode mCallBackMode = CallBackMode.CALL_BACK_MODE_MOVE;
    private OnAngleChangeListener mOnAngleChangeListener;
    private OnDirectionChangeListener mOnDirectionChangeListener;
    private OnDistanceLevelListener mOnDistanceLevelListener;

    private float lastDistance = 0;
    private float baseDistance = 0;
    private int mDistanceLevel = 5;

    private int mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT;
    private Bitmap mAreaBitmap;
    private int mAreaColor;

    private int mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
    private Bitmap mRockerBitmap;
    private int mRockerColor;

    private Rect srcRect, dstRect;
    private DirectionHandler directionHandler;

    public RockerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttribute(context, attrs);

        mAreaBackgroundPaint = new Paint();
        mAreaBackgroundPaint.setAntiAlias(true);

        mRockerPaint = new Paint();
        mRockerPaint.setAntiAlias(true);

        mCenterPoint = new Point();
        mRockerPosition = new Point();

        srcRect = new Rect();
        dstRect = new Rect();
    }

    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView);

        Drawable areaBackground = typedArray.getDrawable(R.styleable.RockerView_areaBackground);

        if (areaBackground != null) {
            if (areaBackground instanceof BitmapDrawable) {
                mAreaBitmap = ((BitmapDrawable) areaBackground).getBitmap();
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_PIC;
            } else if (areaBackground instanceof GradientDrawable) {
                mAreaBitmap = drawable2Bitmap(areaBackground);
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_XML;
            } else if (areaBackground instanceof ColorDrawable) {
                mAreaColor = ((ColorDrawable) areaBackground).getColor();
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_COLOR;
            } else {
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT;
            }
        } else {
            mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT;
        }

        Drawable rockerBackground = typedArray.getDrawable(R.styleable.RockerView_rockerBackground);

        if (rockerBackground != null) {
            if (rockerBackground instanceof BitmapDrawable) {
                mRockerBitmap = ((BitmapDrawable) rockerBackground).getBitmap();
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_PIC;
            } else if (rockerBackground instanceof GradientDrawable) {
                mRockerBitmap = drawable2Bitmap(rockerBackground);
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_XML;
            } else if (rockerBackground instanceof ColorDrawable) {
                mRockerColor = ((ColorDrawable) rockerBackground).getColor();
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_COLOR;
            } else {
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
            }
        } else {
            mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
        }

        mRockerScale = typedArray.getFloat(R.styleable.RockerView_rockerScale, DEFAULT_ROCKER_SCALE);
        mDistanceLevel = typedArray.getInt(R.styleable.RockerView_rockerSpeedLevel, 5);
        mCallBackMode = getCallBackMode(typedArray.getInt(R.styleable.RockerView_rockerCallBackMode, 0));

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth, measureHeight;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            measureWidth = widthSize;
        } else {
            measureWidth = DEFAULT_SIZE;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = heightSize;
        } else {
            measureHeight = DEFAULT_SIZE;
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int cx = measuredWidth / 2;
        int cy = measuredHeight / 2;

        mCenterPoint.set(cx, cy);
        mAreaRadius = (measuredWidth <= measuredHeight) ? (int) (cx / (mRockerScale + 1)) : (int) (cy / (mRockerScale + 1));
        mRockerRadius = (int) (mAreaRadius * mRockerScale);

        if (mRockerPosition.x == 0 || mRockerPosition.y == 0) {
            mRockerPosition.set(mCenterPoint.x, mCenterPoint.y);
        }

        if (mAreaBackgroundMode == AREA_BACKGROUND_MODE_PIC || mAreaBackgroundMode == AREA_BACKGROUND_MODE_XML) {
            srcRect.set(0, 0, mAreaBitmap.getWidth(), mAreaBitmap.getHeight());
            dstRect.set(mCenterPoint.x - mAreaRadius, mCenterPoint.y - mAreaRadius, mCenterPoint.x + mAreaRadius, mCenterPoint.y + mAreaRadius);

            canvas.drawBitmap(mAreaBitmap, srcRect, dstRect, mAreaBackgroundPaint);
        } else if (mAreaBackgroundMode == AREA_BACKGROUND_MODE_COLOR) {
            mAreaBackgroundPaint.setColor(mAreaColor);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mAreaRadius, mAreaBackgroundPaint);
        } else {
            mAreaBackgroundPaint.setColor(Color.GRAY);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mAreaRadius, mAreaBackgroundPaint);
        }

        if (mRockerBackgroundMode == ROCKER_BACKGROUND_MODE_PIC || mRockerBackgroundMode == ROCKER_BACKGROUND_MODE_XML) {
            srcRect.set(0, 0, mRockerBitmap.getWidth(), mRockerBitmap.getHeight());
            dstRect.set(mRockerPosition.x - mRockerRadius, mRockerPosition.y - mRockerRadius, mRockerPosition.x + mRockerRadius, mRockerPosition.y + mRockerRadius);

            canvas.drawBitmap(mRockerBitmap, srcRect, dstRect, mRockerPaint);
        } else if (mRockerBackgroundMode == ROCKER_BACKGROUND_MODE_COLOR) {
            mRockerPaint.setColor(mRockerColor);
            canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mRockerPaint);
        } else {
            mRockerPaint.setColor(Color.RED);
            canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mRockerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                callBackStart();
                performClick();
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();

                baseDistance = mAreaRadius + 2;
                Log.d(LOG_TAG,"BaseDistance: " + baseDistance);

                mRockerPosition = getRockerPositionPoint(mCenterPoint, new Point((int) moveX, (int) moveY), mAreaRadius + mRockerRadius, mRockerRadius);
                moveRocker(mRockerPosition.x, mRockerPosition.y);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                callBackFinish();

                if (mOnDirectionChangeListener != null) {
                    mOnDirectionChangeListener.onDirectionChanged(Direction.DIRECTION_CENTER);
                }

                moveRocker(mCenterPoint.x, mCenterPoint.y);
                break;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private Point getRockerPositionPoint(Point centerPoint, Point touchPoint, float regionRadius, float rockerRadius) {
        float lenX = (float) (touchPoint.x - centerPoint.x);
        float lenY = (float) (touchPoint.y - centerPoint.y);
        float lenXY = (float) Math.sqrt((double) (lenX * lenX + lenY * lenY));

        double radian = Math.acos(lenX / lenXY) * (touchPoint.y < centerPoint.y ? -1 : 1);
        double angle = radian2Angle(radian);

        if (lenXY + rockerRadius <= regionRadius) {
            callBack(angle, (int) lenXY);
            return touchPoint;
        } else {
            int showPointX = (int) (centerPoint.x + (regionRadius - rockerRadius) * Math.cos(radian));
            int showPointY = (int) (centerPoint.y + (regionRadius - rockerRadius) * Math.sin(radian));

            callBack(angle, (int) Math.sqrt((showPointX - centerPoint.x) * (showPointX - centerPoint.x) + (showPointY - centerPoint.y) * (showPointY - centerPoint.y)));
            return new Point(showPointX, showPointY);
        }
    }

    private void moveRocker(float x, float y) {
        mRockerPosition.set((int) x, (int) y);
        invalidate();
    }

    private double radian2Angle(double radian) {
        double tmp = Math.round(radian / Math.PI * 180);
        return tmp >= 0 ? tmp : 360 + tmp;
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap.Config config = (drawable.getOpacity() != PixelFormat.OPAQUE) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    private void callBackStart() {
        directionHandler.reset();

        if (mOnAngleChangeListener != null) {
            mOnAngleChangeListener.onStart();
        }

        if (mOnDirectionChangeListener != null) {
            mOnDirectionChangeListener.onStart();
        }
    }

    private void handleMoveCallback(double angle) {
        Direction direction = directionHandler.getMoveDirection(angle);
        mOnDirectionChangeListener.onDirectionChanged(direction);
    }

    private void handleStateChangeCallback(double angle) {
        Direction direction = directionHandler.getStateChangeDirection(angle);
        mOnDirectionChangeListener.onDirectionChanged(direction);
    }

    private void callBack(double angle, float distance) {
        Log.d(LOG_TAG,"Distance: " + distance);

        if (Math.abs(distance - lastDistance) >= (baseDistance / mDistanceLevel)) {
            lastDistance = distance;

            if (mOnDistanceLevelListener != null) {
                int level = (int) (distance / (baseDistance / mDistanceLevel));
                mOnDistanceLevelListener.onDistanceLevel(level);
            }
        }

        if (mOnAngleChangeListener != null) {
            mOnAngleChangeListener.onAngleChanged(angle);
        }

        if (mOnDirectionChangeListener != null) {
            if (CallBackMode.CALL_BACK_MODE_MOVE == mCallBackMode) {
                this.handleMoveCallback(angle);
            } else if (CallBackMode.CALL_BACK_MODE_STATE_CHANGE == mCallBackMode) {
                this.handleStateChangeCallback(angle);
            }
        }
    }

    private void callBackFinish() {
        directionHandler.reset();

        if (mOnAngleChangeListener != null) {
            mOnAngleChangeListener.onFinish();
        }

        if (mOnDirectionChangeListener != null) {
            mOnDirectionChangeListener.onFinish();
        }
    }

    public void setOnAngleChangeListener(OnAngleChangeListener listener) {
        mOnAngleChangeListener = listener;
    }

    public void setOnDirectionChangeListener(DirectionMode directionMode, OnDirectionChangeListener listener) {
        mOnDirectionChangeListener = listener;

        switch (directionMode) {
            case DIRECTION_2_HORIZONTAL:
                directionHandler = new HorizontalDirectionHandler();
                break;
            case DIRECTION_2_VERTICAL:
                directionHandler = new VerticalDirectionHandler();
                break;
            case DIRECTION_4_ROTATE_0:
                directionHandler = new DiagonalFourWayDirectionHandler();
                break;
            case DIRECTION_4_ROTATE_45:
                directionHandler = new FourWayDirectionHandler();
                break;
            case DIRECTION_8:
                directionHandler = new EightWayDirectionHandler();
                break;
        }
    }

    public void setOnDistanceLevelListener(OnDistanceLevelListener listener) {
        mOnDistanceLevelListener = listener;
    }

    private CallBackMode getCallBackMode(int mode) {
        switch (mode) {
            case 0:
                return CallBackMode.CALL_BACK_MODE_MOVE;
            case 1:
                return CallBackMode.CALL_BACK_MODE_STATE_CHANGE;
        }

        return mCallBackMode;
    }
}
