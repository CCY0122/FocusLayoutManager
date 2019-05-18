package ccy.focuslayoutmanager;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ccy(17022) on 2019/5/18 下午5:07
 */
public class FocusLayoutManager extends RecyclerView.LayoutManager {
    public static final String TAG = "FocusLayoutManager";
    public static final int FOCUS_LEFT = 1;
    public static final int FOCUS_RIGHT = 2;
    public static final int FOCUS_TOP = 3;
    public static final int FOCUS_BOTTOM = 4;


    @IntDef({FOCUS_LEFT, FOCUS_RIGHT, FOCUS_TOP, FOCUS_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusOrientation {
    }


    public FocusLayoutManager() {
        this(new Builder());
    }

    private FocusLayoutManager(Builder builder) {

    }


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
    }

    @Override
    public boolean canScrollHorizontally() {
        return super.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        return super.canScrollVertically();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    public static class Builder {


        int maxLayerCount;
        @RecyclerView.Orientation
        int orientation;
        @FocusOrientation
        private int focusOrientation;
        private float layerPadding;
        private TrasitionListener trasitionListener;


        public Builder() {
            maxLayerCount = 3;
            orientation = RecyclerView.HORIZONTAL;
            focusOrientation = FOCUS_LEFT;
            layerPadding = 16;
            trasitionListener = new TrasitionListenerConvert(new SimpleTrasitionListener() {
            });
        }

        /**
         * 最大可堆叠层级
         */
        public Builder maxLayerCount(int maxLayerCount) {
            this.maxLayerCount = maxLayerCount;
            return this;
        }

        /**
         * 滚动方向，水平/垂直
         */
        public Builder orientation(@RecyclerView.Orientation int orientation) {
            this.orientation = orientation;
            return this;
        }

        /**
         * 堆叠的方向。
         * 滚动方向为水平时，传{@link #FOCUS_LEFT}或{@link #FOCUS_RIGHT}；
         * 滚动方向为垂直时，传{@link #FOCUS_TOP}或{@link #FOCUS_BOTTOM}。
         *
         * @param focusOrientation
         * @return
         */
        public Builder focusOrientation(@FocusOrientation int focusOrientation) {
            this.focusOrientation = focusOrientation;
            return this;
        }

        /**
         * 堆叠view之间的偏移量
         *
         * @param layerPadding
         * @return
         */
        public Builder layerPadding(float layerPadding) {
            this.layerPadding = layerPadding;
            return this;
        }

        /**
         * 滚动过程中view的变换监听接口。
         *
         * @param trasitionListener
         * @return
         */
        public Builder trasitionListener(TrasitionListener trasitionListener) {
            this.trasitionListener = trasitionListener;
            return this;
        }

        /**
         * 滚动过程中view的变换监听接口。
         *
         * @param simpleTrasitionListener
         * @return
         */
        public Builder simpleTrasitionListener(SimpleTrasitionListener simpleTrasitionListener) {
            if (trasitionListener != null) {
                Log.e(TAG, "警告：设置SimpleTrasitionListener发现你已经设置过了TrasitionListener");
            }
            this.trasitionListener = new TrasitionListenerConvert(simpleTrasitionListener);
            return this;
        }


        public FocusLayoutManager build() {
            return new FocusLayoutManager(this);
        }
    }


    /**
     * 滚动过程中view的变换监听接口。
     */
    public interface TrasitionListener {

        /**
         * 处理在堆叠里的view。
         *
         * @param view          view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param viewLayer     当前层级，0表示底层，maxLayerCount表示顶层
         * @param maxLayerCount 最大层级
         * @param position      item所在的position
         * @param fraction      一次完整焦点变化滚动量的百分比 todo 告知哪个方向为正
         */
        void handleLayerView(View view, int viewLayer, int maxLayerCount, int position,
                             float fraction);

        /**
         * 处理正聚焦的那个View（即正处在从普通位置滚向聚焦位置时的那个view）
         *
         * @param view     view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param position item所在的position
         * @param fraction 一次完整焦点变化滚动量的百分比 todo 告知哪个方向为正
         */
        void handleFocusingView(View view, int position, float fraction);

        /**
         * 处理不在堆叠里的普通view（正在聚焦的那个view除外）
         *
         * @param view     view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param position item所在的position
         * @param fraction 一次完整焦点变化滚动量的百分比 todo 告知哪个方向为正
         */
        void handleNormalView(View view, int position, float fraction);

    }

    /**
     * 简化版  滚动过程中view的变换监听接口。
     */
    public static abstract class SimpleTrasitionListener {

        /**
         * 返回当前层级view的透明度
         *
         * @param viewLayer     当前层级，0表示底层，maxLayerCount表示顶层
         * @param maxLayerCount 最大层级
         * @return
         */
        float getLayerAlpha(int viewLayer, int maxLayerCount) {
            float minAlpha = 0.3f;
            float maxAlpha = 1.0f;
            //透明度均分
            float alpha = minAlpha + ((maxAlpha - minAlpha) / maxLayerCount * viewLayer);
            log("viewLayer = " + viewLayer + ";max = " + maxLayerCount + ";alpha = " + alpha);
            return alpha;
        }

        /**
         * 返回当前层级view的缩放比例
         *
         * @param viewLayer
         * @param maxLayerCount
         * @return
         */
        float getLayerScale(int viewLayer, int maxLayerCount) {
            float minScale = 0.6f;
            float maxScale = 1.2f;
            //缩放比例均分
            float scale = minScale + ((maxScale - minScale) / maxLayerCount * viewLayer);
            log("viewLayer = " + viewLayer + ";max = " + maxLayerCount + ";scale = " + scale);
            return scale;
        }


        /**
         * 返回一个百分比值范围，相对于一次完整焦点变化滚动期间，在该百分比值内view就完成缩放、透明度的渐变变化。
         * 例：若返回值为1，说明在一次完整的焦点变化期间view将匀速完成缩放、透明度变化；
         * 例：若返回值为0.5，说明在焦点变化到一半时，view已经完成的缩放、透明度变化（也可能是才开始变化）
         *
         * @return
         */
        float getLayerFinishChangeFraction() {
            return 0.2f;
        }

        /**
         * 返回聚焦view的透明度
         *
         * @return
         */
        float getFocusingViewAlpha() {
            return 1;
        }

        /**
         * 返回聚焦view的缩放比例
         *
         * @return
         */
        float getFocusingViewScale() {
            return 1.2f;
        }

        /**
         * 返回值意义参考{@link #getLayerFinishChangeFraction()}
         *
         * @return
         */
        float getFocusingViewFinishChangeFraction() {
            return 0.5f;
        }

        /**
         * 返回普通view的透明度
         *
         * @return
         */
        float getNormalViewAlpha() {
            return 1;
        }

        /**
         * 返回普通view的缩放比例
         *
         * @return
         */
        float getNormalViewScale() {
            return 1;
        }

    }

    /**
     * 将SimpleTrasitionListener转换成实际的TrasitionListener
     */
    public static class TrasitionListenerConvert implements TrasitionListener {
        SimpleTrasitionListener stl;

        public TrasitionListenerConvert(SimpleTrasitionListener simpleTrasitionListener) {
            stl = simpleTrasitionListener;
        }

        @Override
        public void handleLayerView(View view, int viewLayer, int maxLayerCount, int position,
                                    float fraction) {

        }

        @Override
        public void handleFocusingView(View view, int position, float fraction) {

        }

        @Override
        public void handleNormalView(View view, int position, float fraction) {

        }
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    public static void log(String msg) {
        boolean isDebug = true;
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }


}
