package robert.com.surfaceviewlean;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author: robert
 * @date: 2017-10-21
 * @time: 11:57
 * @说明: SurfaceView 编写模板
 */
public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread mThread;
    /**
     * 是否绘制
     */
    private boolean isRunning;

    public SurfaceViewTemplate(Context context) {
        this(context, null);
    }

    public SurfaceViewTemplate(Context context, @Nullable AttributeSet attrs) {
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
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //drawSomeThing
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {//释放canvas
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    @Override
    public void run() {
        //不断进行绘制
        while (isRunning) {
            draw();
        }
    }
}
