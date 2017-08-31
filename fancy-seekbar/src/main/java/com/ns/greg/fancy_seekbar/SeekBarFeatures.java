package com.ns.greg.fancy_seekbar;

import android.support.annotation.ColorInt;

/**
 * @author Gregory
 * @since 2017/8/31
 */

public interface SeekBarFeatures {

  void setMinValue(int value);

  void setMaxValue(int value);

  void setBarColors(@ColorInt int[] colors);
}
