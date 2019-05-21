package ccy.focuslayoutmanagerproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ccy.focuslayoutmanager.FocusLayoutManager;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FocusLayoutManager focusLayoutManager;
    Adapter adapter;
    int colors[] = {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffff00, 0xff00ffff, 0xffff00ff,
            0xffd0d0d0, 0xff000000, 0xffe04900, 0xff900909};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv);
        focusLayoutManager =
                new FocusLayoutManager.Builder()
                        .layerPadding(dp2px(this, 14))
                        .normalViewGap(dp2px(this, 16))
                        .build();
        List<Adapter.Bean> datas = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Adapter.Bean bean = new Adapter.Bean();
            bean.msg = "" + (i);
            bean.color = colors[i % 10];
            datas.add(bean);
        }
        adapter = new Adapter(datas);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(focusLayoutManager);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ValueAnimator anim = ValueAnimator.ofFloat(0.0f,1.0f).setDuration(500);
//                anim.setInterpolator(new LinearInterpolator());
//                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        focusLayoutManager.fraction = animation.getAnimatedFraction();
//                        focusLayoutManager.requestLayout();
//
//                    }
//                });
//                anim.start();
//            }
//        },3000);
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<Bean> datas;
        private int index = 0;

        public Adapter(List<Bean> datas) {
            this.datas = datas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card,
                    viewGroup, false);
            view.setTag(index++);
            Log.d("ccy", "onCreateViewHolder = " + index);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            Log.d("ccy", "onBindViewHolder,index = " + (int) (viewHolder.itemView.getTag()));
            Bean bean = datas.get(position);
            viewHolder.itemView.setBackgroundColor(bean.color);
            viewHolder.tv.setText(bean.msg);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.item_tv);
            }
        }

        public static class Bean {
            int color;
            String msg;
        }
    }
}
