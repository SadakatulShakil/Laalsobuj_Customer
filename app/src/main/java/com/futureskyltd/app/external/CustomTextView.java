package com.futureskyltd.app.external;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by hitasoft on 30/6/15.
 */


public class CustomTextView extends AppCompatTextView {

    public CustomTextView(Context context) {
        super(context);
    }
    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

}
