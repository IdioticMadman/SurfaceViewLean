package robert.com.surfaceviewlean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.socks.library.KLog;

import static android.graphics.BitmapFactory.decodeResource;

/**
 * @author: robert
 * @date: 2017-10-21
 * @time: 11:57
 * @说明:
 */
public class LuckyPanView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "LuckyPanView";
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /* 用于绘制的线程*/
    private Thread mThread;
    /* 是否绘制*/
    private boolean isRunning;
    /* 转盘文字 */
    private String[] mStrings = new String[]{"单反相机", "IPad", "恭喜发财",
            "IPhone", "服装一套", "恭喜发财"};
    /* 转盘图标 */
    private int[] mImages = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.f040,
            R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
    /* 转盘背景颜色 */
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};
    /* 图片对应的Bitmap对象 */
    private Bitmap[] mImageBmp;
    /* 背景bitmap */
    private Bitmap mBgBmp = decodeResource(getResources(), R.drawable.bg2);
    /* 绘制文字大小 */
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
    /* 条目个数 */
    private int mItemSize = 6;
    /* 盘块中心 */
    private int mCenter;
    /* 整个盘块的范围 */
    private RectF mRectF = new RectF();
    /* 盘块的半径*/
    private int mRadius;
    /* 盘块画笔 */
    private Paint mArcPaint;
    /* 文本的画笔 */
    private Paint mTextPaint;
    /* 转盘内边距,取转盘最小值,因为圆的 */
    private int mPadding;
    /* 键盘速度 */
    private double mSpeed = 0;
    /* 转盘起始角度 */
    private volatile double mStartAngle;
    /* 判断是否点击了停止按钮 */
    private boolean isShouldEnd;

    public LuckyPanView(Context context) {
        this(context, null);
    }

    public LuckyPanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        //设置焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //屏幕常亮
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int radius = Math.min(getMeasuredHeight(), getMeasuredWidth());
        mPadding = getPaddingLeft();
        //直径
        mRadius = radius - mPadding * 2;
        //中心点
        mCenter = radius / 2;
        setMeasuredDimension(radius, radius);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //初始化转盘画笔
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        mArcPaint = paint;
        //初始化文字画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xffffffff);
        paint.setTextSize(mTextSize);
        mTextPaint = paint;
        //转盘矩阵范围
        mRectF = new RectF(mPadding, mPadding, mRadius + mPadding, mRadius + mPadding);
        //初始化图片
        mImageBmp = new Bitmap[mItemSize];
        for (int i = 0; i < mImages.length; i++) {
            mImageBmp[i] = BitmapFactory.decodeResource(getResources(), mImages[i]);
        }

        isRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
    }

    @Override
    public void run() {
        //不断进行绘制
        while (isRunning) {//维持50ms绘制一次
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50) {
                SystemClock.sleep(50 - (end - start));
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //画背景
                drawBg();
                //绘制盘块
                float tempAngle = (float) mStartAngle;
                float sweepAngle = 360 / mItemSize;
                for (int i = 0; i < mItemSize; i++) {
                    mArcPaint.setColor(mColors[i]);
                    //绘制盘块
                    mCanvas.drawArc(mRectF, tempAngle, sweepAngle, true, mArcPaint);
                    //绘制文本
                    drawText(tempAngle, sweepAngle, mStrings[i]);
                    //绘制图标
                    drawIcon(tempAngle, sweepAngle, mImageBmp[i]);
                    tempAngle = tempAngle + sweepAngle;
                }
                if (isShouldEnd) {
                    KLog.e(TAG, "draw: " + mStartAngle);
                    mSpeed--;
                }
                if (mSpeed < 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
                mStartAngle += mSpeed;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {//释放canvas
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    public void luckyStart(int index) {
        float avAgngle = 360 / mItemSize;
        //随机的一个角度
        float randomAngle = (float) (avAgngle * Math.random());
        //总共的角度，让他转4圈加上转的角度再加上一个随机的角度
        float angle = 360 * 4 + avAgngle * index + randomAngle;
        KLog.e(TAG, "luckyStart: " + angle);
        //计算初始速度  s = 平均速度*时间   匀加速运动所以平均速度为(起始速度+结束速度)/2 时间 t = v/a 速度的变化除以加速度
        mSpeed = Math.sqrt(2 * angle) + 0.5d;
        KLog.e(TAG, "luckyStart: " + mSpeed);
        isShouldEnd = false;
    }

    public void luckyStop() {
        mStartAngle = 0;
        isShouldEnd = true;
    }

    //是否在结束中，还在转，但是已经点击过停止即isShouldEnd为TRUE
    public boolean isEnding() {
        return isShouldEnd && isTurning();
    }

    /**
     * 是否停止旋转
     *
     * @return
     */
    public boolean isTurning() {
        return mSpeed != 0;
    }

    /**
     * 画图表
     *
     * @param tempAngle  弧度开始角度
     * @param sweepAngle 弧度大小
     * @param imageBmp   icon
     */
    private void drawIcon(float tempAngle, float sweepAngle, Bitmap imageBmp) {
        int imgWidth = mRadius / 8;

        double angle = Math.toRadians(tempAngle + sweepAngle / 2);
        int x = mCenter + (int) ((mRadius / 2 / 2) * Math.cos(angle));
        int y = mCenter + (int) ((mRadius / 2 / 2) * Math.sin(angle));
        mCanvas.drawBitmap(imageBmp, null,
                new RectF(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2),
                null);
    }

    /**
     * 绘制每个盘块的文本
     *
     * @param startAngle 弧度开始角度
     * @param sweepAngle 弧度大小
     * @param string     文本
     */
    private void drawText(float startAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRectF, startAngle, sweepAngle);
        //水平和垂直 偏移量
        //利用水平偏移量让文字居中
        float textWidth = mTextPaint.measureText(string);
        //弧度的一半减去文字宽度的一半，则是文字的水平偏移量
        int hOffset = (int) (mRadius * Math.PI / mItemSize / 2 - textWidth / 2);
        //垂直偏移量设置为当前半径的六分之一
        int vOffset = mRadius / 2 / 6;
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        if (mCanvas != null) {
            mCanvas.drawColor(Color.WHITE);
            mCanvas.drawBitmap(mBgBmp, null,
                    new RectF(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2),
                    null);
        }
    }
}
