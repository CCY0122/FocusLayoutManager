package ccy.focuslayoutmanagerproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ccy.focuslayoutmanager.FocusLayoutManager;

public class MainActivity extends AppCompatActivity {

    public static Toast mToast;
    View emptyView;
    TextView tvFocusedPos;
    CheckBox cbAutoSelect;
    CheckBox cbInfinite;
    RecyclerView recyclerView;
    FocusLayoutManager focusLayoutManager;
    Adapter adapter;

    int colors[] = {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffff00, 0xff00ffff, 0xffff00ff,
            0xffd0d0d0, 0xff000000, 0xffe04900, 0xff900909};
    int horRes[] = {R.drawable.h5, R.drawable.h6, R.drawable.h7, R.drawable.h1, R.drawable.h2,
            R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6, R.drawable.h7,
            R.drawable.h5, R.drawable.h6, R.drawable.h7, R.drawable.h1, R.drawable.h2,
            R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6, R.drawable.h7};
    int verRes[] = {R.drawable.v5, R.drawable.v6, R.drawable.v7, R.drawable.v1, R.drawable.v2,
            R.drawable.v3, R.drawable.v4, R.drawable.v5, R.drawable.v6, R.drawable.v7};

    List<Bean> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv);
        emptyView = findViewById(R.id.empty);
        tvFocusedPos = findViewById(R.id.tv_focus_pos);
        cbAutoSelect = findViewById(R.id.auto_select_cb);
        cbInfinite = findViewById(R.id.infinite_cb);

        focusLayoutManager =
                new FocusLayoutManager.Builder()
                        .layerPadding(dp2px(this, 14))
                        .normalViewGap(dp2px(this, 14))
                        .focusOrientation(FocusLayoutManager.FOCUS_LEFT)
                        .isAutoSelect(true)
                        .maxLayerCount(3)
                        .setOnFocusChangeListener(new FocusLayoutManager.OnFocusChangeListener() {
                            @Override
                            public void onFocusChanged(int focusdPosition, int lastFocusdPosition) {
                                tvFocusedPos.setText("[" + focusdPosition + "],[" + lastFocusdPosition + "]");
                                if (focusdPosition == datas.size() - 1 &&
                                        (focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_LEFT)) {
                                    emptyView.setVisibility(View.VISIBLE);
                                } else {
                                    emptyView.setVisibility(View.GONE);
                                }
                            }
                        })
                        .build();

//        datas = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            Bean bean = new Bean();
//            bean.useColor = true;
//            bean.msg = "" + (i);
//            bean.color = colors[i % 10];
//            datas.add(bean);
//        }
        datas = getDatas(false);
        adapter = new Adapter(datas);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(focusLayoutManager);

        cbAutoSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                focusLayoutManager.setAutoSelect(isChecked);
            }
        });
        cbInfinite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recyclerView.setAdapter(new Adapter(datas));
                if (isChecked) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            focusLayoutManager.scrollToPosition(1000);
                        }
                    });
                }
            }
        });

    }

    public List<Bean> getDatas(boolean vertical) {
        List<Bean> datas = new ArrayList<>();
        if (vertical) {
            for (int i = 0; i < verRes.length; i++) {
                Bean bean = new Bean();
                bean.useColor = false;
                bean.background = verRes[i];
                datas.add(bean);
            }
        } else {
            for (int i = 0; i < horRes.length; i++) {
                Bean bean = new Bean();
                bean.useColor = false;
                bean.background = horRes[i];
                datas.add(bean);
            }
        }

        return datas;
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public void layerCount_btn(View view) {
        EditText et = findViewById(R.id.layerCount);
        try {
            int count = Integer.parseInt(et.getText().toString());
            if (count <= 0) {
                Toast.makeText(this, "不合法", Toast.LENGTH_SHORT).show();
                return;
            }
            focusLayoutManager.setMaxLayerCount(count);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }


    public void changeTrasition(View view) {
        focusLayoutManager.setMaxLayerCount(3);
        focusLayoutManager.setNormalViewGap(dp2px(this, 4));
        focusLayoutManager.setLayerPadding(dp2px(this, 50));
        focusLayoutManager.removeTrasitionlistener(null);
        focusLayoutManager.addTrasitionListener(new FocusLayoutManager.TrasitionListener() {
            @Override
            public void handleLayerView(FocusLayoutManager focusLayoutManager, View view,
                                        int viewLayer, int maxLayerCount, int position,
                                        float fraction, float offset) {
                if (view instanceof CardView) {
                    ((CardView) view).setCardElevation(0);
                }
                float realFraction = fraction;

                float minRo = 80;
                float maxRo = 0;
                float roDelta = maxRo - minRo;
                float currentLayerMaxRo =
                        minRo + roDelta * (viewLayer + 1) / (maxLayerCount * 1.0f);
                float currentLayerMinRo =
                        minRo + roDelta * viewLayer / (maxLayerCount * 1.0f);
                float realRo =
                        currentLayerMaxRo - (currentLayerMaxRo - currentLayerMinRo) * realFraction;

                float minScale = 0.7f;
                float maxScale = 1f;
                float scaleDelta = maxScale - minScale;
                float currentLayerMaxScale =
                        minScale + scaleDelta * (viewLayer + 1) / (maxLayerCount * 1.0f);
                float currentLayerMinScale =
                        minScale + scaleDelta * viewLayer / (maxLayerCount * 1.0f);
                float realScale =
                        currentLayerMaxScale - (currentLayerMaxScale - currentLayerMinScale) * realFraction;

                float minAlpha = 0;
                float maxAlpha = 1;
                float alphaDelta = maxAlpha - minAlpha; //总透明度差
                float currentLayerMaxAlpha =
                        minAlpha + alphaDelta * (viewLayer + 1) / (maxLayerCount * 1.0f);
                float currentLayerMinAlpha =
                        minAlpha + alphaDelta * viewLayer / (maxLayerCount * 1.0f);
                float realAlpha =
                        currentLayerMaxAlpha - (currentLayerMaxAlpha - currentLayerMinAlpha) * realFraction;

                view.setScaleX(realScale);
                view.setScaleY(realScale);
                view.setRotationY(realRo);
                view.setAlpha(realAlpha);

            }

            @Override
            public void handleFocusingView(FocusLayoutManager focusLayoutManager, View view,
                                           int position, float fraction, float offset) {
                if (view instanceof CardView) {
                    ((CardView) view).setCardElevation(0);
                }
                float realFraction = fraction;

                float realScale =
                        0.85f + (1f - 0.85f) * realFraction;
                float realAlpha = 1;

                view.setScaleX(realScale);
                view.setScaleY(realScale);
                view.setAlpha(realAlpha);
                view.setRotationY(0);

            }

            @Override
            public void handleNormalView(FocusLayoutManager focusLayoutManager, View view,
                                         int position, float fraction, float offset) {
                if (view instanceof CardView) {
                    ((CardView) view).setCardElevation(0);
                }
                view.setScaleX(0.85f);
                view.setScaleY(0.85f);
                view.setAlpha(1);
                view.setRotationY(0);
            }
        });
    }

    public void normalViewGap_btn(View view) {
        EditText et = findViewById(R.id.normalViewGap);
        try {
            int count = Integer.parseInt(et.getText().toString());

            focusLayoutManager.setNormalViewGap(dp2px(this, count));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void layerPadding_btn(View view) {
        EditText et = findViewById(R.id.layerPadding);
        try {
            int count = Integer.parseInt(et.getText().toString());

            focusLayoutManager.setLayerPadding(dp2px(this, count));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void orientation_btn(View view) {
        RadioGroup rg = findViewById(R.id.ori_rg);
        int id = rg.getCheckedRadioButtonId();
        if (id == R.id.l) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_LEFT);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            recyclerView.setAdapter(new Adapter(datas = getDatas(false)));
        }
        if (id == R.id.r) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_RIGHT);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            recyclerView.setAdapter(new Adapter(datas = getDatas(false)));
        }
        if (id == R.id.t) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_TOP);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.WRAP_CONTENT;
            p.height = (int) dp2px(this, 480);
            recyclerView.setAdapter(new Adapter(datas = getDatas(true)));
        }
        if (id == R.id.b) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_BOTTOM);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.WRAP_CONTENT;
            p.height = (int) dp2px(this, 480);
            recyclerView.setAdapter(new Adapter(datas = getDatas(true)));
        }
    }


    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

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
            if (focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_LEFT || focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_RIGHT) {
                ViewGroup.MarginLayoutParams p =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.topMargin = (int) dp2px(view.getContext(), 25);
                p.bottomMargin = (int) dp2px(view.getContext(), 25);
                p.leftMargin = (int) dp2px(view.getContext(), 0);
                p.rightMargin = (int) dp2px(view.getContext(), 0);
                p.width = (int) dp2px(view.getContext(), 100);
                p.height = (int) dp2px(view.getContext(), 150);
            } else {
                ViewGroup.MarginLayoutParams p =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.topMargin = (int) dp2px(view.getContext(), 0);
                p.bottomMargin = (int) dp2px(view.getContext(), 0);
                p.leftMargin = (int) dp2px(view.getContext(), 25);
                p.rightMargin = (int) dp2px(view.getContext(), 25);
                p.width = (int) dp2px(view.getContext(), 150);
                p.height = (int) dp2px(view.getContext(), 100);
            }
            view.setTag(++index);
            Log.d("ccy", "onCreateViewHolder = " + index);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            Log.d("ccy", "onBindViewHolder,index = " + (int) (viewHolder.itemView.getTag()));
            int realPosition = cbInfinite.isChecked() ? position % datas.size() : position;

            Bean bean = datas.get(realPosition);

            if (bean.useColor) {
                ((CardView) viewHolder.itemView).setBackgroundResource(0);
                ((CardView) viewHolder.itemView).setBackgroundColor(bean.color);
            } else {
                Glide.with(viewHolder.itemView)
                        .load(bean.background)
                        .into(viewHolder.iv);
            }
            viewHolder.tv.setText(bean.msg);
        }

        @Override
        public int getItemCount() {
            return cbInfinite.isChecked() ? Integer.MAX_VALUE : datas.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv;
            ImageView iv;

            public ViewHolder(@NonNull final View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.item_tv);
                iv = itemView.findViewById(R.id.item_iv);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        if (mToast == null) {
                            mToast = Toast.makeText(MainActivity.this, "" + pos,
                                    Toast.LENGTH_SHORT);
                        }
                        mToast.setText("" + pos);
                        mToast.show();

                        if (pos == focusLayoutManager.getFocusdPosition()) {

                            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                            intent.putExtra("resId", datas.get(pos).background);
                            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation
                                    (MainActivity.this, itemView, "img").toBundle());
                        } else {
                            focusLayoutManager.setFocusdPosition(pos, true);
                        }
                    }
                });
            }
        }

    }

    public static class Bean {
        boolean useColor = true;
        int color;
        int background;
        String msg;
    }
}
