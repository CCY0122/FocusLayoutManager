package ccy.focuslayoutmanagerproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by ccy(17022) on 2019/5/23 上午10:47
 */
public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.detail_act);

        int res = getIntent().getIntExtra("resId",0);
        if(res != 0){
            Glide.with(this).load(res).into((ImageView) findViewById(R.id.img));
        }
    }
}
