package ccy.focuslayoutmanagerproject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ccy.focuslayoutmanager.FocusLayoutManager;

public class MainActivity extends AppCompatActivity {

    View emptyView;
    TextView tvFocusedPos;
    CheckBox cbAutoSelect;
    RecyclerView recyclerView;
    FocusLayoutManager focusLayoutManager;
    Adapter adapter;

    int colors[] = {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffff00, 0xff00ffff, 0xffff00ff,
            0xffd0d0d0, 0xff000000, 0xffe04900, 0xff900909};
    List<Bean> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv);
        emptyView = findViewById(R.id.empty);
        tvFocusedPos = findViewById(R.id.tv_focus_pos);
        cbAutoSelect = findViewById(R.id.auto_select_cb);

        focusLayoutManager =
                new FocusLayoutManager.Builder()
                        .layerPadding(dp2px(this, 14))
                        .normalViewGap(dp2px(this, 16))
                        .focusOrientation(FocusLayoutManager.FOCUS_LEFT)
                        .isAutoSelect(true)
                        .maxLayerCount(3)
                        .setOnFocusChangeListener(new FocusLayoutManager.OnFocusChangeListener() {
                            @Override
                            public void onFocusChanged(int focusdPosition, int lastFocusdPosition) {
                                tvFocusedPos.setText("[" + focusdPosition + "],[" + lastFocusdPosition + "]");
                                if (focusdPosition == datas.size() - 1 &&
                                        (focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_LEFT || focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_RIGHT)) {
                                    emptyView.setVisibility(View.VISIBLE);
                                } else {
                                    emptyView.setVisibility(View.GONE);
                                }
                            }
                        })
                        .build();

        datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Bean bean = new Bean();
            bean.useColor = true;
            bean.msg = "" + (i);
            bean.color = colors[i % 10];
            datas.add(bean);
        }
        adapter = new Adapter(datas);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(focusLayoutManager);

        cbAutoSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                focusLayoutManager.setAutoSelect(isChecked);
            }
        });

    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public void layerCount_btn(View view) {
        EditText et = findViewById(R.id.layerCount);
        int count = Integer.parseInt(et.getText().toString());
        if (count <= 0) {
            Toast.makeText(this, "不合法", Toast.LENGTH_SHORT).show();
            return;
        }
        focusLayoutManager.setMaxLayerCount(count);
    }


    public void changeTrasition(View view) {
        focusLayoutManager.setLayerPadding(0);
        focusLayoutManager.setMaxLayerCount(5);
        focusLayoutManager.setSimpleTrasitionListener(new FocusLayoutManager.SimpleTrasitionListener() {
            @Override
            public float getLayerViewMaxScale(int maxLayerCount) {
                return super.getLayerViewMaxScale(maxLayerCount);
            }

            @Override
            public float getLayerViewMinScale(int maxLayerCount) {
                return 1.4f;
            }

            @Override
            public float getLayerChangeRangePercent() {
                return 1f;
            }

            @Override
            public float getFocusingViewMaxScale() {
                return 0.4f;
            }

            @Override
            public float getFocusingViewMinScale() {
                return super.getFocusingViewMinScale();
            }

            @Override
            public float getFocusingViewMaxAlpha() {
                return 0.6f;
            }

            @Override
            public float getFocusingViewChangeRangePercent() {
                return 1;
            }
        });
    }

    public void normalViewGap_btn(View view) {
        EditText et = findViewById(R.id.normalViewGap);
        int count = Integer.parseInt(et.getText().toString());

        focusLayoutManager.setNormalViewGap(dp2px(this, count));
    }

    public void layerPadding_btn(View view) {
        EditText et = findViewById(R.id.layerPadding);
        int count = Integer.parseInt(et.getText().toString());

        focusLayoutManager.setLayerPadding(dp2px(this, count));
    }

    public void orientation_btn(View view) {
        RadioGroup rg = findViewById(R.id.ori_rg);
        int id = rg.getCheckedRadioButtonId();
        if (id == R.id.l) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_LEFT);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        }
        if (id == R.id.t) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_TOP);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = (int) dp2px(this, 300);
            recyclerView.setAdapter(new Adapter(datas));
        }
        if (id == R.id.r) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_RIGHT);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        }
        if (id == R.id.b) {
            focusLayoutManager.setFocusOrientation(FocusLayoutManager.FOCUS_BOTTOM);
            ViewGroup.LayoutParams p = recyclerView.getLayoutParams();
            p.width = RecyclerView.LayoutParams.MATCH_PARENT;
            p.height = (int) dp2px(this, 300);
            recyclerView.setAdapter(new Adapter(datas));
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
            if(focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_LEFT || focusLayoutManager.getFocusOrientation() == FocusLayoutManager.FOCUS_RIGHT){
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.topMargin = (int) dp2px(view.getContext(),25);
                p.bottomMargin = (int) dp2px(view.getContext(),25);
                p.leftMargin = (int) dp2px(view.getContext(),0);
                p.rightMargin = (int) dp2px(view.getContext(),0);
                p.width = (int) dp2px(view.getContext(), 100);
                p.height = (int) dp2px(view.getContext(), 150);
            }else {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.topMargin = (int) dp2px(view.getContext(),0);
                p.bottomMargin = (int) dp2px(view.getContext(),0);
                p.leftMargin = (int) dp2px(view.getContext(),25);
                p.rightMargin = (int) dp2px(view.getContext(),25);
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
            int realPosition = position;
            Bean bean = datas.get(realPosition);

            if (bean.useColor) {
                ((CardView) viewHolder.itemView).setBackgroundResource(0);
                ((CardView) viewHolder.itemView).setBackgroundColor(bean.color);
            } else {
                ((CardView) viewHolder.itemView).setBackgroundResource(bean.background);
            }
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
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        if (pos == focusLayoutManager.getFocusdPosition()) {
                            Toast.makeText(MainActivity.this, "点击了" + pos, Toast.LENGTH_SHORT).show();
                        } else {
                            if (focusLayoutManager.isAutoSelect()) {
                                focusLayoutManager.setFocusdPosition(pos, true);
                            }
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
