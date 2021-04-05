package com.futureskyltd.app.external;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

/** Customizing AutoCompleteTextView to return Country Name   
 *  corresponding to the selected item
 */
public class CustomAutoCompleteTextView extends AppCompatAutoCompleteTextView {

	private Context context;
	private AttributeSet attrs;
	private int defStyle;

	/** Returns the country name corresponding to the selected item */
	@SuppressWarnings("unchecked")
	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		/** Each item in the autocompetetextview suggestion list is a hashmap object */
		HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
		return hm.get("txt");
	}

	public CustomAutoCompleteTextView(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.attrs = attrs;
		init();
	}

	public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
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
}
