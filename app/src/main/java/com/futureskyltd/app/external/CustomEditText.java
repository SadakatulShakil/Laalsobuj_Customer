package com.futureskyltd.app.external;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;

/**
 * Created by hitasoft on 12/2/16.
 */
public class CustomEditText extends AppCompatEditText {

    private Context context;
    private AttributeSet attrs;
    private int defStyle;

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
        init();
    }
    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        this.setTypeface(font);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        super.setTypeface(tf);
    }
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }
    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }
}