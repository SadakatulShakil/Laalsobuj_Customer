package com.futureskyltd.app.external;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.futureskyltd.app.fantacy.R;

public class LinkEllipseTextView extends AppCompatTextView {
	private static String TAG = "LinkEllipseTextView";
	private String moreStr;
	
	private boolean mIsLinkable = true;
	private boolean mIsEndEllipsable = true;
	
	// Control infinite loop of onDraw().
	private boolean mIsRemake = true;
	private boolean mIsFinishEllipsed = false;
	private boolean mIsSplited = false;
	
	private int mMaxLines = 5;
	private String mText;
	private SpannableString mLinkableText;  
  private Pattern mHyperLinksPattern = Pattern.compile("(@[a-zA-Z0-9_-]+)");
  Pattern hashTagsPattern = Pattern.compile("(#[a-zA-Z0-9_-]+)");
	//	  compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
  private ArrayList<Hyperlink> mListOfLinks;  			
	
  private TextLinkClickListener mListener; 
  private TextMoreClickListener m_mListener;
  
  private Context mCfx;
  
	/**
   * Interface definition for a callback to be invoked when the link
   * item is clicked.
   */  
  public interface TextLinkClickListener {
  	public void onTextLinkClick(View textView, String clickedString);
  }
  
	/**
   * Interface definition for a callback to be invoked when the 'more'
   * item is clicked.
   */    
  public interface TextMoreClickListener {
  	public void onTextMoreClick(View textView, String clickedString);
  }  
  
  /**
   * Class constructor taking only a context. Use this constructor to create
   * {@link LinkEllipseTextView} objects from your own code.
   *
   * @param context
   */  
	public LinkEllipseTextView(Context context) {
		super(context);
		mCfx = context;
		// TODO Auto-generated constructor stub
		init(context);
	}

  /**
   * Class constructor taking a context and an attribute set. This constructor
   * is used by the layout engine to construct a {@link LinkEllipseTextView} from a set of
   * XML attributes.
   *
   * @param context
   * @param attrs   An attribute set which can contain attributes from
   *                {@link } as well as attributes inherited
   *                from {@link View}.
   */	
	public LinkEllipseTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mCfx = context;
    TypedArray a = context.getTheme().obtainStyledAttributes(
        attrs, R.styleable.LinkEllipseTextView,
        0, 0
    );
    
    try {
      // Retrieve the values from the TypedArray and store into
      // fields of this class.
      //
      // The R.styleable.LinkEllipseTextView_* constants represent the index for
      // each custom attribute in the R.styleable.LinkEllipseTextView array.
      mIsLinkable =a.getBoolean(R.styleable.LinkEllipseTextView_isLinkable, true);
      mIsEndEllipsable =a.getBoolean(R.styleable.LinkEllipseTextView_isEndEllipsable, true);
      mMaxLines = a.getInt(R.styleable.LinkEllipseTextView_maxLines, 100);
      mText = a.getString(R.styleable.LinkEllipseTextView_text);
    } finally {
      // release the TypedArray so that it can be reused.
      a.recycle();
    }
		
    init(context);
	}
	
  /**
   * Initialize the control. This code is in a separate method so that it can be
   * called from both constructors.
   */	
	private void init(Context context) {
		mCfx = context;		
		moreStr ="More Text";// mCfx.getString(R.string.more);
		// Cancel ellipsis in super. 
		super.setEllipsize(null);
	}

  /**
   * Returns true if the text should be clickable links.
   *
   * @return True if the text should be clickable links, false otherwise.
   */	
	public boolean getIsLinkable() {
		return mIsLinkable;
	}
	
  /**
   * Controls whether the text is clickable links or not. Setting this property to
   * false makes default state of the text.
   *
   * @param isLinkable true if the text should be clickable, false otherwise
   */	
	public void setIsLinkable(boolean isLinkable) {
		mIsLinkable = isLinkable;		
		mIsRemake = true;
		invalidate();
	}

  /**
   * Returns true if the text should be end ellipsis.
   *
   * @return True if the text should be end ellipsis, false otherwise.
   */		
	public boolean getIsEndEllipsable() {
		return mIsEndEllipsable;
	}
	
  /**
   * Controls whether the text is end ellipsis or not. Setting this property to
   * false makes default state of the text.
   *
   * @param isEndEllipsable true if the text should be end ellipsis, false otherwise
   */		
	public void setIsEndEllipsable(boolean isEndEllipsable) {
		mIsEndEllipsable = isEndEllipsable;
  	mIsFinishEllipsed = false;		
  	mIsRemake = true;  	
  	invalidate();		
	}
	
  /**
   * Returns the max lines reserved for text.
   *
   * @return The max lines reserved for text.
   */	
  public int getMaxLines() {
    return mMaxLines;
  }	
	
  /**
   * Set the max lines of the text.
   *
   * @param maxLines the max lines of the text.
   */  
  public void setMaxLines(int maxLines) {
  	mMaxLines = maxLines;  	
  	if(mIsEndEllipsable) {
  		mIsFinishEllipsed = false;
  		mIsRemake = true;
  		invalidate();
  	}
  	else {
  		super.setMaxLines(maxLines);  		
  	}
  }
  
  /**
   * Returns the text presented in textview.
   *
   * @return the text presented in textview.
   */	  
  public String getText() {
  	return mText;
  }
  
  /**
   * Set the text to present, in textview.
   *
   * @param text the text to present, in textview.
   */    
  public void setText(String text) {
  	mText = text;
  	
  	super.setText(text);
		mIsFinishEllipsed = false;
		mIsRemake = true;
		invalidate();
  }
  	
  
	public TextLinkClickListener getOnTextLinkClickListener() {
		return mListener;
	}
	
  /**
   * Register a callback to be invoked when the currently link item is clicked.
   *
   * @param listener Can be null.
   *                 The current link item is clicked listener to attach to this view.
   */	
	public void setOnTextLinkClickListener(TextLinkClickListener listener) {
	   mListener = listener;
	}

	public TextMoreClickListener getOnTextMoreClickListener() {
		return m_mListener;
	}

  /**
   * Register a callback to be invoked when the currently 'more' item is clicked.
   *
   * @param listener Can be null.
   *                 The current 'more' item is clicked listener to attach to this view.
   */		
	public void setOnTextMoreClickListener(TextMoreClickListener listener) {
	   m_mListener = listener;
	}	
	
	@Override
	public void setEllipsize(TruncateAt where) {
		super.setEllipsize(null);
	}
	
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);      
    
    if(mIsRemake) {
    	remakeText();
    }    	    
  }
  
  private void remakeText() {
  	
  	if(mIsEndEllipsable) {
  		// Split the text if the text line is larger than the maxLines and add end ellipsis with 'more'.
  		int lineCount = getLineCount();
  		if(lineCount > getMaxLines()) {
  	  	if(!mIsFinishEllipsed) {
  	  		splitText();
  	  		mIsSplited = true;
  	  	}
  		}
  	}
  	else {
  		// Back to default state.
  		super.setText(mText);
  	}
  	
  	String text = super.getText().toString();  	
  	if(text.length() != 0) {
  		if(mIsLinkable) {
  			// Make the text to be link clikable text, if mIsLinkable is true.  			
  			gatherLinksForText(text);  			  		
  		}  	
  		else if(mIsEndEllipsable) {
  			// The text always has end ellipsis with 'more' which should be clickable, if mIsEndEllipsable is true.
  			if(mIsSplited) {
  				mLinkableText = new SpannableString(text);  			
  				InternalMoreSpan span = new InternalMoreSpan(moreStr);  	
  				mLinkableText.setSpan(span, mLinkableText.length()-moreStr.length(), mLinkableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  			}
  		}
  		else {
  			mLinkableText = new SpannableString(text);
  		}
			super.setText(mLinkableText);  		
  	}	  	
  	
  	
  	mIsFinishEllipsed = true;
  	mIsRemake = false;
  	mIsSplited = false;
  }
  
  private void splitText() {  	
  	String text = super.getText().toString();  	
  	Layout layout = getLayout();
  	int maxLines = getMaxLines();
  	
  	String splitedText = text.substring(0, layout.getLineEnd(maxLines - 1)).trim();
  	splitedText = splitedText + " ..." + moreStr;
  	
  	mIsFinishEllipsed = true;  	
  	super.setText(splitedText);
  }
  
  //
  // Reference site : http://www.javacodegeeks.com/2012/09/android-custom-hyperlinked-textview.html
  //
  private void gatherLinksForText(String text) {
     mLinkableText = new SpannableString(text);
     //
     // gatherLinks basically collects the Links depending upon the Pattern that we supply
     // and add the links to the ArrayList of the links
     //
   
     if(mListOfLinks == null) {
    	 this.mListOfLinks = new ArrayList<Hyperlink>();    	 
     }
     else {
    	 this.mListOfLinks.clear();
     }
     
     gatherLinks(mListOfLinks, mLinkableText, mHyperLinksPattern);
     gatherLinks(mListOfLinks, mLinkableText, hashTagsPattern);

     for(int i = 0; i< mListOfLinks.size(); i++) {
         Hyperlink linkSpec = mListOfLinks.get(i);
         
         // this process here makes the clickable links from the text
          
         mLinkableText.setSpan(linkSpec.span, linkSpec.start, linkSpec.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     }

     if(mIsSplited) {
    	 InternalMoreSpan span = new InternalMoreSpan(moreStr);  	
    	 mLinkableText.setSpan(span, mLinkableText.length()-moreStr.length(), mLinkableText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     }
  }    
  
  private final void gatherLinks(ArrayList<Hyperlink> links, Spannable s, Pattern pattern) {
  	// Matcher matching the pattern
  	Matcher m = pattern.matcher(s);
  	
  	while (m.find()) {
  		int start = m.start();
  		int end = m.end();

  		// Hyperlink is basically used like a structure for storing the information about
  		// where the link was found.

  		Hyperlink spec = new Hyperlink();

  		spec.textSpan = s.subSequence(start, end);
  		spec.span = new InternalURLSpan(spec.textSpan.toString());
  		spec.start = start;
  		spec.end = end;

  		links.add(spec);
  	}  
  }  
	
  /**
   * Internal child class that find link in the text and store information.
   */  
  private class Hyperlink {
  	CharSequence textSpan;
    InternalURLSpan span;
    int start;
    int end;
  }

  /**
   * Internal child class that make span clickable.
   */    
  private class InternalURLSpan extends ClickableSpan {
  	private String clickedSpan;

  	public InternalURLSpan (String clickedString) {
  		clickedSpan = clickedString;
  	}
  	@Override
  	public void updateDrawState(TextPaint tp) {
  		super.updateDrawState(tp);
  		tp.setUnderlineText(false);
  	}

  	@Override
  	public void onClick(View textView) {
  		if(mListener != null) {
  			mListener.onTextLinkClick(textView, clickedSpan);
  		}
  	}  
  }  
    
  /**
   * Internal child class that make span clickable.
   */      
  private class InternalMoreSpan extends ClickableSpan {
  	private String clickedSpan;
  	
  	public InternalMoreSpan(String clickedString) {
  		clickedSpan = clickedString;
  	}
  	  	
  	@Override
  	public void updateDrawState(TextPaint tp) {
  		// Remove underline and red color, because 'more' is not link.
  		super.updateDrawState(tp);
  		tp.setUnderlineText(false);
  		tp.setColor(0xfff2b9b6);
  		tp.setTextSize(22);
		tp.setTypeface(FontCache.get("FuturaStd-Light.ttf", mCfx));
  	}
  	
  	@Override
  	public void onClick(View textView) {  		
  		if(m_mListener != null) {
  			m_mListener.onTextMoreClick(textView, clickedSpan);
  		}
  	}    	
  }  
}
