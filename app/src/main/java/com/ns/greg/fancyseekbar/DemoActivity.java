package com.ns.greg.fancyseekbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ns.greg.fancy_seekbar.FancySeekBar;

/**
 * @author Gregory
 * @since 2017/8/31
 */

public class DemoActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_demo);

    int[] colors = new int[] {
        getResources().getColor(R.color.color_one),
        getResources().getColor(R.color.color_two),
        getResources().getColor(R.color.color_three),
        getResources().getColor(R.color.color_four),
        getResources().getColor(R.color.color_five),
        getResources().getColor(R.color.color_six),
        getResources().getColor(R.color.color_seven),
        getResources().getColor(R.color.color_eight),
        getResources().getColor(R.color.color_nine),
        getResources().getColor(R.color.color_ten)
    };
    FancySeekBar fancySeekBar = (FancySeekBar) findViewById(R.id.seek_bar);
    fancySeekBar.setBarColors(colors);
  }

  @Override protected void onResume() {
    super.onResume();
  }
}
