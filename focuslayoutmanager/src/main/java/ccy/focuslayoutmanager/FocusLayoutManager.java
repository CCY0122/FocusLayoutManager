package ccy.focuslayoutmanager;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by ccy(17022) on 2019/5/18 下午5:07
 * todo 拒绝遍历getItemCount
 */
public class FocusLayoutManager extends RecyclerView.LayoutManager {
    public static final String TAG = "FocusLayoutManager";
    /**
     * 堆叠方向在左
     */
    public static final int FOCUS_LEFT = 1;
    /**
     * 堆叠方向在右
     */
    public static final int FOCUS_RIGHT = 2;
    /**
     * 堆叠方向在上
     */
    public static final int FOCUS_TOP = 3;
    /**
     * 堆叠方向在下
     */
    public static final int FOCUS_BOTTOM = 4;

    /**
     * 最大可堆叠层级
     */
    int maxLayerCount;
    /**
     * 滚动方向，水平/垂直
     */
    @RecyclerView.Orientation
    int orientation;
    /**
     * 堆叠的方向。
     * 滚动方向为水平时，传{@link #FOCUS_LEFT}或{@link #FOCUS_RIGHT}；
     * 滚动方向为垂直时，传{@link #FOCUS_TOP}或{@link #FOCUS_BOTTOM}。
     */
    @FocusOrientation
    private int focusOrientation;
    /**
     * 堆叠view之间的偏移量
     */
    private float layerPadding;
    /**
     * 普通view之间的margin
     */
    private float normalViewGap;
    /**
     * 变换监听接口。
     */
    private TrasitionListener trasitionListener;
    /**
     * 水平方向累计偏移量
     */
    private int mHorizontalOffset;
    /**
     * 垂直方向累计偏移量
     */
    private int mVerticalOffset;
    /**
     * 屏幕可见的第一个View的Position
     */
    private int mFirstVisiPos;
    /**
     * 屏幕可见的最后一个View的Position
     */
    private int mLastVisiPos;
    /**
     * 一次完整的聚焦滑动所需要移动的距离。
     */
    float onceCompleteScrollLength = -1;


    @IntDef({FOCUS_LEFT, FOCUS_RIGHT, FOCUS_TOP, FOCUS_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusOrientation {
    }


    public FocusLayoutManager() {
        this(new Builder());
    }

    private FocusLayoutManager(Builder builder) {
        this.maxLayerCount = builder.maxLayerCount;
        this.orientation = builder.orientation;
        this.focusOrientation = builder.focusOrientation;
        this.layerPadding = builder.layerPadding;
        this.trasitionListener = builder.trasitionListener;
        this.normalViewGap = builder.normalViewGap;
    }


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        //重置
        mHorizontalOffset = 0;
        mVerticalOffset = 0;
        mFirstVisiPos = 0;
        mLastVisiPos = getItemCount() - 1; //暂时置为最大值
        onceCompleteScrollLength = -1;

        //分离全部已有的view，放入临时缓存
        detachAndScrapAttachedViews(recycler);

        fill(recycler, state, 0);
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == RecyclerView.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == RecyclerView.VERTICAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        //手指从右向左滑动，dx > 0; 手指从左向右滑动，dx < 0;

        //位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }

        mHorizontalOffset += dx;//累加实际滑动距离


        dx = fill(recycler, state, dx);

        return dx;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    /**
     * @param recycler
     * @param state
     * @param delta
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int delta) {
        //todo 方向
        int resultDelta = fillHorizontal(recycler, state, delta);
        ensureRecycleChildren(recycler);

//        log("count= [" + getChildCount() + "]" + ",[recycler.getScrapList().size():" + recycler
// .getScrapList().size());
        return resultDelta;
    }


    /**
     * @param recycler
     * @param state
     * @param dx       偏移量。手指从右向左滑动，dx > 0; 手指从左向右滑动，dx < 0;
     */
    private int fillHorizontal(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {

        //----------------1、边界检测-----------------
        if (dx < 0) {
            //已达左边界
            if (mHorizontalOffset < 0) {
                mHorizontalOffset = dx = 0;
            }
        }

        if (dx > 0) {
            //滑动到只剩堆叠view，没有普通view了，说明已经到达右边界了
            if (mLastVisiPos - mFirstVisiPos <= maxLayerCount - 1) {
                //因为scrollHorizontallyBy里加了一次dx，现在减回去
                mHorizontalOffset -= dx;
                dx = 0;
            }
        }

        //分离全部的view，放入临时缓存
//        log("分离前,child count = " + getChildCount() + ";scrap count = " + recycler.getScrapList().size());
        detachAndScrapAttachedViews(recycler);
//        log("分离后,child count = " + getChildCount() + ";scrap count = " + recycler.getScrapList().size());

        //----------------2、初始化布局数据-----------------

        float startX = getPaddingLeft();
        if (onceCompleteScrollLength == -1) {
            View temp = recycler.getViewForPosition(0);
            measureChildWithMargins(temp, 0, 0);
            onceCompleteScrollLength = getDecoratedMeasurementHorizontal(temp) + normalViewGap;
        }
        //当前"一次完整的聚焦滑动"所在的进度百分比.百分比增加方向为向着堆叠移动的方向（即如果为FOCUS_LEFT，从右向左移动fraction将从0%到100%）
        float fraction =
                (Math.abs(mHorizontalOffset) % onceCompleteScrollLength) / (onceCompleteScrollLength * 1.0f);

        //堆叠区域view偏移量。在一次完整的聚焦滑动期间，其总偏移量只有一个layerPadding的距离
        float layerViewOffset = layerPadding * fraction;
        //普通区域view偏移量。在一次完整的聚焦滑动期间，其总位移量是一个onceCompleteScrollLength
        float normalViewOffset = onceCompleteScrollLength * fraction;
        boolean isLayerViewOffsetSetted = false;
        boolean isNormalViewOffsetSetted = false;

        //修正第一个可见的view：mFirstVisiPos。已经滑动了多少个完整的onceCompleteScrollLength就代表滑动了多少个item
        mFirstVisiPos = Math.abs(mHorizontalOffset) / (int) onceCompleteScrollLength; //向下取整
        //临时将mLastVisiPos设置到getItemCount() - 1，放心，下面遍历时会判断view是否已溢出屏幕，并及时结束布局
        mLastVisiPos = getItemCount() - 1;


        //----------------3、开始布局-----------------

        for (int i = mFirstVisiPos; i <= mLastVisiPos; i++) {
            //属于堆叠区域
            if (i - mFirstVisiPos < maxLayerCount) {
                //当前层级，0表示底层，maxLayerCount-1表示顶层
                View item = recycler.getViewForPosition(i);
                addView(item);
                measureChildWithMargins(item, 0, 0);

                startX += layerPadding;
                if (!isLayerViewOffsetSetted) {
                    startX -= layerViewOffset;
                    isLayerViewOffsetSetted = true;
                }

                int l, t, r, b;
                l = (int) startX;
                t = getPaddingTop();
                r = (int) (startX + getDecoratedMeasurementHorizontal(item));
                b = getPaddingTop() + getDecoratedMeasurementVertical(item);
                layoutDecoratedWithMargins(item, l, t, r, b);

                if (trasitionListener != null) {
                    trasitionListener.handleLayerView(this, item, i - mFirstVisiPos,
                            maxLayerCount, i
                            , fraction, dx);
                }


            } else {//属于普通区域

                //若已经超出屏幕了，则修正mLastVisiPos，结束布局
                View item = recycler.getViewForPosition(i);
                measureChildWithMargins(item, 0, 0);
                startX += onceCompleteScrollLength;
                if (!isNormalViewOffsetSetted) {
                    startX += layerViewOffset;
                    startX -= normalViewOffset;
                    isNormalViewOffsetSetted = true;
                }

                int l, t, r, b;
                l = (int) startX;
                t = getPaddingTop();
                r = (int) (startX + getDecoratedMeasurementHorizontal(item));
                b = getPaddingTop() + getDecoratedMeasurementVertical(item);
                addView(item);
                layoutDecoratedWithMargins(item, l, t, r, b);
                if (trasitionListener != null) {
                    if (i - mFirstVisiPos == maxLayerCount) {
                        trasitionListener.handleFocusingView(this, item, i, fraction, dx);
                    } else {
                        trasitionListener.handleNormalView(this, item, i, fraction, dx);
                    }
                }

                //判断下一个view的布局位置是不是已经超出屏幕了，若超出，跳出遍历
                if (startX + onceCompleteScrollLength > getWidth() - getPaddingRight()) {
                    mLastVisiPos = i;
                    break;
                }
            }
        }

        return dx;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    /**
     * 回收需回收的Item。
     */
    private void ensureRecycleChildren(RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeAndRecycleView(holder.itemView, recycler);
        }
    }

    /**
     * 获取某个childView在水平方向所占的空间，将margin考虑进去
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间,将margin考虑进去
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    public int getMaxLayerCount() {
        return maxLayerCount;
    }

    public void setMaxLayerCount(int maxLayerCount) {
        this.maxLayerCount = maxLayerCount;
        //todo requestLayout会重走onLayoutChildren吗?
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        //todo
    }

    public int getFocusOrientation() {
        return focusOrientation;
    }

    public void setFocusOrientation(int focusOrientation) {
        this.focusOrientation = focusOrientation;
        //todo
    }

    public float getLayerPadding() {
        return layerPadding;
    }

    public void setLayerPadding(float layerPadding) {
        this.layerPadding = layerPadding;
        //todo
    }

    public float getNormalViewGap() {
        return normalViewGap;
    }

    public void setNormalViewGap(float normalViewGap) {
        this.normalViewGap = normalViewGap;
        //todo
    }

    public TrasitionListener getTrasitionListener() {
        return trasitionListener;
    }

    public void setTrasitionListener(TrasitionListener trasitionListener) {
        this.trasitionListener = trasitionListener;
        //todo
    }


    public static class Builder {


        int maxLayerCount;
        @RecyclerView.Orientation
        int orientation;
        @FocusOrientation
        private int focusOrientation;
        private float layerPadding;
        private float normalViewGap;
        private TrasitionListener trasitionListener;


        public Builder() {
            maxLayerCount = 3;
            orientation = RecyclerView.HORIZONTAL;
            focusOrientation = FOCUS_LEFT;
            layerPadding = 22;
            normalViewGap = 32;
            trasitionListener = new TrasitionListenerConvert(new SimpleTrasitionListener() {
            });
        }

        /**
         * 最大可堆叠层级
         */
        public Builder maxLayerCount(int maxLayerCount) {
            if (maxLayerCount <= 0) {
                throw new RuntimeException("maxLayerCount不能小于0");
            }
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
            if (layerPadding < 0) {
                layerPadding = 0;
            }
            this.layerPadding = layerPadding;
            return this;
        }

        /**
         * 普通view之间的margin
         */
        public Builder normalViewGap(float normalViewGap) {
            if (normalViewGap < 0) {
                normalViewGap = 0;
            }
            this.normalViewGap = normalViewGap;
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
         * @param focusLayoutManager
         * @param view               view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param viewLayer          当前层级，0表示底层，maxLayerCount-1表示顶层
         * @param maxLayerCount      最大层级
         * @param position           item所在的position
         * @param fraction           "一次完整的聚焦滑动"所在的进度百分比.百分比增加方向为向着堆叠移动的方向（即如果为FOCUS_LEFT
         *                           ，从右向左移动fraction将从0%到100%）
         * @param offset             当次滑动偏移量
         */
        void handleLayerView(FocusLayoutManager focusLayoutManager, View view, int viewLayer,
                             int maxLayerCount, int position, float fraction, float offset);

        /**
         * 处理正聚焦的那个View（即正处在从普通位置滚向聚焦位置时的那个view,即堆叠顶层view）
         *
         * @param focusLayoutManager
         * @param view               view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param position           item所在的position
         * @param fraction           "一次完整的聚焦滑动"所在的进度百分比.百分比增加方向为向着堆叠移动的方向（即如果为FOCUS_LEFT
         *                           ，从右向左移动fraction将从0%到100%）
         * @param offset             当次滑动偏移量
         */
        void handleFocusingView(FocusLayoutManager focusLayoutManager, View view, int position,
                                float fraction, float offset);

        /**
         * 处理不在堆叠里的普通view（正在聚焦的那个view除外）
         *
         * @param focusLayoutManager
         * @param view               view对象。请仅在方法体范围内对view做操作，不要外部强引用它，view是要被回收复用的
         * @param position           item所在的position
         * @param fraction           "一次完整的聚焦滑动"所在的进度百分比.百分比增加方向为向着堆叠移动的方向（即如果为FOCUS_LEFT
         *                           ，从右向左移动fraction将从0%到100%）
         * @param offset             当次滑动偏移量
         */
        void handleNormalView(FocusLayoutManager focusLayoutManager, View view, int position,
                              float fraction, float offset);

    }

    /**
     * 简化版  滚动过程中view的变换监听接口。
     */
    public static abstract class SimpleTrasitionListener {

        /**
         * 返回堆叠view最大透明度
         *
         * @param maxLayerCount 最大层级
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getLayerViewMaxAlpha(int maxLayerCount) {
            return getFocusingViewMaxAlpha();
        }

        /**
         * 返回堆叠view最小透明度
         *
         * @param maxLayerCount 最大层级
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getLayerViewMinAlpha(int maxLayerCount) {
            return 0;
        }


        /**
         * 返回堆叠view最大缩放比例
         *
         * @param maxLayerCount 最大层级
         * @return
         */
        public float getLayerViewMaxScale(int maxLayerCount) {
            return getFocusingViewMaxScale();
        }

        /**
         * 返回堆叠view最小缩放比例
         *
         * @param maxLayerCount 最大层级
         * @return
         */
        public float getLayerViewMinScale(int maxLayerCount) {
            return 0.8f;
        }


        /**
         * 返回一个百分比值，相对于"一次完整的聚焦滑动"期间，在该百分比值内view就完成缩放、透明度的渐变变化。
         * 例：若返回值为1，说明在"一次完整的聚焦滑动"期间view将线性均匀完成缩放、透明度变化；
         * 例：若返回值为0.5，说明在"一次完整的聚焦滑动"的一半路程内（具体从什么时候开始变不固定），view将完成的缩放、透明度变化
         *
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getLayerChangeRangePercent() {
            return 0.3f;
        }

        /**
         * 返回聚焦view的最大透明度
         *
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getFocusingViewMaxAlpha() {
            return 1f;
        }

        /**
         * 返回聚焦view的最小透明度
         *
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getFocusingViewMinAlpha() {
            return getNormalViewAlpha();
        }

        /**
         * 返回聚焦view的最大缩放比例
         *
         * @return
         */
        public float getFocusingViewMaxScale() {
            return 1.2f;
        }

        /**
         * 返回聚焦view的最小缩放比例
         *
         * @return
         */
        public float getFocusingViewMinScale() {
            return getNormalViewScale();
        }

        /**
         * 返回值意义参考{@link #getLayerChangeRangePercent()}
         *
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getFocusingViewChangeRangePercent() {
            return 0.5f;
        }

        /**
         * 返回普通view的透明度
         *
         * @return
         */
        @FloatRange(from = 0.0f, to = 1.0f)
        public float getNormalViewAlpha() {
            return 1.0f;
        }

        /**
         * 返回普通view的缩放比例
         *
         * @return
         */
        public float getNormalViewScale() {
            return 1.0f;
        }

    }

    /**
     * 将SimpleTrasitionListener转换成实际的TrasitionListener的转换器
     */
    public static class TrasitionListenerConvert implements TrasitionListener {
        SimpleTrasitionListener stl;

        public TrasitionListenerConvert(SimpleTrasitionListener simpleTrasitionListener) {
            stl = simpleTrasitionListener;
        }

        @Override
        public void handleLayerView(FocusLayoutManager focusLayoutManager, View view,
                                    int viewLayer, int maxLayerCount, int position,
                                    float fraction, float offset) {
            /**
             * 期望效果：一旦向着堆叠方向移动，就开始变化，一旦远离堆叠方向移动，就开始恢复。
             * 举例：假设当前堆叠方向为FOCUS_LEFT、getLayerChangeRangePercent = 0.2。
             * 当前正向着堆叠方向移动，即手指从右往左滑，从0%到20%，view均匀完成变化，然后继续滑动到70%后，
             * 此时突然右改变方向向右滑，那么在70%~50%期间，view均匀完成恢复。
             */

            float realFraction;
            if (fraction <= stl.getLayerChangeRangePercent()) {
                realFraction = fraction / stl.getLayerChangeRangePercent();
            } else {
                realFraction = 1.0f;
            }

            float minScale = stl.getLayerViewMinScale(maxLayerCount);
            float maxScale = stl.getLayerViewMaxScale(maxLayerCount);
            float scaleDelta = maxScale - minScale; //总缩放差
            float currentLayerMaxScale =
                    minScale + scaleDelta * (viewLayer + 1) / (maxLayerCount * 1.0f);
            float currentLayerMinScale = minScale + scaleDelta * viewLayer / (maxLayerCount * 1.0f);
            float realScale =
                    currentLayerMaxScale - (currentLayerMaxScale - currentLayerMinScale) * realFraction;

            float minAlpha = stl.getLayerViewMinAlpha(maxLayerCount);
            float maxAlpha = stl.getLayerViewMaxAlpha(maxLayerCount);
            float alphaDelta = maxAlpha - minAlpha; //总透明度差
            float currentLayerMaxAlpha =
                    minAlpha + alphaDelta * (viewLayer + 1) / (maxLayerCount * 1.0f);
            float currentLayerMinAlpha = minAlpha + alphaDelta * viewLayer / (maxLayerCount * 1.0f);
            float realAlpha =
                    currentLayerMaxAlpha - (currentLayerMaxAlpha - currentLayerMinAlpha) * realFraction;

            log("layer =" + viewLayer + ";alpha = " + realAlpha + ";fraction = " + fraction);
            switch (focusLayoutManager.getFocusOrientation()) {
                case FOCUS_LEFT:
                case FOCUS_RIGHT:
                    view.setScaleX(realScale);
                    view.setScaleY(realScale);
                    view.setAlpha(realAlpha);
                    break;
                case FOCUS_TOP:
                case FOCUS_BOTTOM:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void handleFocusingView(FocusLayoutManager focusLayoutManager, View view,
                                       int position, float fraction, float offset) {
            /**
             * 期望效果：从0%开始到{@link SimpleTrasitionListener#getFocusingViewChangeRangePercent()} 期间
             * view完成变化，之后一直保持不变
             */
            float realFraction;
            if (fraction <= stl.getFocusingViewChangeRangePercent()) {
                realFraction = fraction / stl.getFocusingViewChangeRangePercent();
            } else {
                realFraction = 1.0f;
            }

            float realScale =
                    stl.getFocusingViewMinScale() + (stl.getFocusingViewMaxScale() - stl.getFocusingViewMinScale()) * realFraction;
            float realAlpha =
                    stl.getFocusingViewMinAlpha() + (stl.getFocusingViewMaxAlpha() - stl.getFocusingViewMinAlpha()) * realFraction;

            switch (focusLayoutManager.getFocusOrientation()) {
                case FOCUS_LEFT:
                case FOCUS_RIGHT:
                    view.setScaleX(realScale);
                    view.setScaleY(realScale);
                    view.setAlpha(realAlpha);
                    break;
                case FOCUS_TOP:
                case FOCUS_BOTTOM:

                    break;
                default:
                    break;
            }

        }

        @Override
        public void handleNormalView(FocusLayoutManager focusLayoutManager, View view,
                                     int position, float fraction, float offset) {
            /**
             * 期望效果：直接完成变换
             */

            view.setScaleX(stl.getNormalViewScale());
            view.setScaleY(stl.getNormalViewScale());
            view.setAlpha(stl.getNormalViewAlpha());
        }
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    public static void log(String msg) {
        log(TAG, msg);
    }

    public static void log(String tag, String msg) {
        boolean isDebug = true;
        if (isDebug) {
            Log.d(tag, msg);
        }
    }


}
