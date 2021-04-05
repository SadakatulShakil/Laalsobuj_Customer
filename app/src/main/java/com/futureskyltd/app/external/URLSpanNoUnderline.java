package com.futureskyltd.app.external;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by hitasoft on 2/2/17.
 */

public class URLSpanNoUnderline extends URLSpan {
    public URLSpanNoUnderline(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
