package com.futureskyltd.app.fantacy;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futureskyltd.app.external.CirclePageIndicator;
import com.futureskyltd.app.external.CornerImageView;
import com.futureskyltd.app.external.DividerItemDecorator;
import com.futureskyltd.app.external.LinkEllipseTextView;
import com.futureskyltd.app.external.RecyclerItemClickListener;
import com.futureskyltd.app.external.URLSpanNoUnderline;
import com.futureskyltd.app.helper.DatabaseHandler;
import com.futureskyltd.app.helper.DividerItemDecoration;
import com.futureskyltd.app.helper.EqualSpacingItemDecoration;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.futureskyltd.app.utils.ItemsParsing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by hitasoft on 19/5/17.
 */

public class DetailActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    Display display;
    ImageView backBtn, cartBtn, searchBtn, cancelBtn;
    RelativeLayout toolBarLay;
    Integer localCartsum;
    int screenheight, imageHeight, selfiWidth, position, selectedSizePosition, itemWidth, lastSwipePosition;
    CollectionViewAdapter collectionViewAdapter;
    LinearLayoutManager collectionManager;
    ItemViewAdapter selfiesViewAdapter, storeViewAdapter;
    RecyclerViewAdapter similarViewAdapter;
    QuestionViewAdapter questionViewAdapter;
    ReviewAdapter reviewAdapter;
    DatabaseHandler helper;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> collections = new ArrayList<HashMap<String, String>>();
    private static CallbackManager callbackManager;
    ShareDialog shareDialog;
    Boolean fb = false;
    Toolbar toolbar;
    String from, type="";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    int likedCount;
    private String jsonMap;
    private ArrayList<String> localitemIdList = new ArrayList<>();
    Map<String, Map<String, String>> retMainMap;
    Map<String, Map<String, String>> getRetMainMap;
    Map<String, String> getPhotosMap;
    private String localCartCount ="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        mViewPager = (ViewPager) findViewById(R.id.container);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        cancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        toolBarLay = (RelativeLayout) findViewById(R.id.toolBarLay);

        display = this.getWindowManager().getDefaultDisplay();
        imageHeight = display.getWidth();
        screenheight = display.getHeight();
        itemWidth = display.getWidth() * 37 / 100;
        selfiWidth = FantacyApplication.dpToPx(DetailActivity.this, 100);
        helper = DatabaseHandler.getInstance(this);


        itemsAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("items");
        position = (int) getIntent().getExtras().get("position");
        from = (String) getIntent().getExtras().get("is");

        if(getIntent().hasExtra("type")){
            type = getIntent().getStringExtra("type");
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(DetailActivity.this, itemsAry);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(position);
        lastSwipePosition = position;

        getItemDetails(position);
        String getLocalCart = preferences.getString("localCart", null);
        if(getLocalCart != null){

            getRetMainMap = new Gson().fromJson(
                    getLocalCart, new TypeToken<HashMap<String, Map<String, String>>>() {}.getType()
            );
            //FragmentMainActivity.cartCount = String.valueOf(getRetMainMap.values().size());
            Log.d(TAG, "addLocalCartItem3: "+ getRetMainMap+"...."+FragmentMainActivity.cartCount);

            /*localItemCount();*/
        }
        //localCartCount = String.valueOf(getRetMainMap.size());
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay),DetailActivity.this);

        mViewPager.addOnPageChangeListener(pageChangeListener());
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(DetailActivity.this);

        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.v(TAG, "onSuccessPostId" + result.getPostId());

                facebookShare(mViewPager.getCurrentItem());
                Log.v(TAG, "onSuccess" + result);
            }

            @Override
            public void onCancel() {
                FantacyApplication.defaultSnack(DetailActivity.this.findViewById(R.id.parentLay), getString(R.string.item_not_shared), "alert");
                Log.v(TAG, "cancelled");
            }

            @Override
            public void onError(FacebookException e) {
                FantacyApplication.defaultSnack(DetailActivity.this.findViewById(R.id.parentLay), e.getMessage(), "error");
                Log.v(TAG, "error=" + e.getMessage());
            }
        });
    }

    private ClipboardManager myClipboard;
    private ClipData myClip;

    class MyClickableSpan extends ClickableSpan{// extend ClickableSpan

        String clicked;
        public MyClickableSpan(String string) {
            super();
            clicked = string;
        }

        public void onClick(View tv) {
       //     Toast.makeText(DetailActivity.this,clicked , Toast.LENGTH_SHORT).show();
        }

        public void updateDrawState(TextPaint ds) {// override updateDrawState
            ds.setUnderlineText(false); // set to false to remove underline
            ds.setColor(getResources().getColor(R.color.colorPrimary));
        }
    }


    private void setItemDetails(final int position, final HashMap<String, String> itemDetails) {
        try {
            Log.v(TAG, "setItemDetailsPosition=" + position);
            View view = mViewPager.findViewWithTag("posi" + position);

            RecyclerView productSelfies = (RecyclerView) view.findViewById(R.id.productSelfies);
            RecyclerView commentList = (RecyclerView) view.findViewById(R.id.commentList);
            RecyclerView storeList = (RecyclerView) view.findViewById(R.id.storeList);
            RecyclerView similarList = (RecyclerView) view.findViewById(R.id.similarList);
            RecyclerView questionView = (RecyclerView) view.findViewById(R.id.questionview);
            RecyclerView reviewview = (RecyclerView) view.findViewById(R.id.reviewview);
            TextView writereview = (TextView) view.findViewById(R.id.writereview);
            LinearLayout bottomLay = (LinearLayout) view.findViewById(R.id.bottomLay);
            RelativeLayout progressLay = (RelativeLayout) view.findViewById(R.id.progress);
            RelativeLayout noreviewLay = (RelativeLayout) view.findViewById(R.id.noreviewlay);
            RelativeLayout noquestionlay = (RelativeLayout) view.findViewById(R.id.noquestionLay);
            final RecyclerView sizeList = (RecyclerView) view.findViewById(R.id.sizeList);
            RelativeLayout shareLay = (RelativeLayout) view.findViewById(R.id.shareLay);
            final LinearLayout sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);
            final TextView description = (TextView) view.findViewById(R.id.description);
            LinearLayout selfieLay = (LinearLayout) view.findViewById(R.id.selfieLay);
            RelativeLayout noCommentsLay = (RelativeLayout) view.findViewById(R.id.noCommentsLay);
            TextView viewAllComment = (TextView) view.findViewById(R.id.viewAllComment);
            TextView postQuestion = (TextView) view.findViewById(R.id.postQuestion);
            TextView viewall = (TextView) view.findViewById(R.id.viewall);
            TextView shopName = (TextView) view.findViewById(R.id.shopName);
            ImageView shopImage = (ImageView) view.findViewById(R.id.shopImage);
            TextView rating = (TextView) view.findViewById(R.id.rating);
            TextView ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);
            TextView averagerating = (TextView) view.findViewById(R.id.averagerating);
            TextView totalrating = (TextView) view.findViewById(R.id.totalrating);
            TextView totalreview = (TextView) view.findViewById(R.id.totalreview);
            TextView viewallrating = (TextView) view.findViewById(R.id.viewallrating);
            TextView rat5count = (TextView) view.findViewById(R.id.rat5_count);
            TextView rat4count = (TextView) view.findViewById(R.id.rat4_count);
            TextView rat3count = (TextView) view.findViewById(R.id.rat3_count);
            TextView rat2count = (TextView) view.findViewById(R.id.rat2_count);
            TextView rat1count = (TextView) view.findViewById(R.id.rat1_count);
            ProgressBar rat1progress = (ProgressBar) view.findViewById(R.id.rat1progress);
            ProgressBar rat2progress = (ProgressBar) view.findViewById(R.id.rat2progress);
            ProgressBar rat3progress = (ProgressBar) view.findViewById(R.id.rat3progress);
            ProgressBar rat4progress = (ProgressBar) view.findViewById(R.id.rat4progress);
            ProgressBar rat5progress = (ProgressBar) view.findViewById(R.id.rat5progress);
            LinearLayout sellerlay = (LinearLayout) view.findViewById(R.id.sellerlay);
            LinearLayout adminlay = (LinearLayout) view.findViewById(R.id.adminlay);
            LinearLayout categorylay = (LinearLayout) view.findViewById(R.id.categorylay);
            LinearLayout offerlay = (LinearLayout) view.findViewById(R.id.offerlay);
            TextView selleroffer = (TextView) view.findViewById(R.id.selleroffer);
            TextView adminoffer = (TextView) view.findViewById(R.id.adminoffer);
            TextView categoryoffer = (TextView) view.findViewById(R.id.categoryoffer);

            LinearLayout similarLay = (LinearLayout) view.findViewById(R.id.similarLay);
            final ViewPager imagePager = (ViewPager) view.findViewById(R.id.imagePager);
            CirclePageIndicator pagerIndicator = (CirclePageIndicator) view.findViewById(R.id.pagerIndicator);
            ImageView followBtn = (ImageView) view.findViewById(R.id.followBtn);
            final TextView readMore = (TextView) view.findViewById(R.id.readMore);
            LinearLayout tabLayout = (LinearLayout) view.findViewById(R.id.tabLayout);
            ProgressBar commentProgress = (ProgressBar) view.findViewById(R.id.commentProgress);
            ProgressBar questionProgress = (ProgressBar) view.findViewById(R.id.questionProgress);
            ProgressBar reviewProgress = (ProgressBar) view.findViewById(R.id.reviewProgress);
            final TextView addCart = (TextView) view.findViewById(R.id.addCart);
            final TextView buyNow = (TextView) view.findViewById(R.id.buyNow);
            TextView askQuestion = (TextView) view.findViewById(R.id.askQuestion);
            final LinearLayout playLay = (LinearLayout) view.findViewById(R.id.playLay);
            ImageView playBtn = (ImageView) view.findViewById(R.id.playBtn);
            TextView itemPrice = (TextView) view.findViewById(R.id.itemPrice);
            TextView discountPrice = (TextView) view.findViewById(R.id.discountPrice);
            TextView discountPercent = (TextView) view.findViewById(R.id.discountPercent);

            /*String descremovespace = itemDetails.get(Constants.TAG_ITEM_DESCRIPTION).replace("\n", "").replaceAll("\\<[^>]*>","").replace("\r", "");
            String descFinal = html2text(descremovespace);
*/

            //Spannable spannedText = (Spannable) Html.fromHtml(itemDetails.get(Constants.TAG_ITEM_DESCRIPTION).trim(), null, new MyTagHandler());
            //description.setText(spannedText);
            description.setText((new HtmlSpanner()).fromHtml(itemDetails.get(Constants.TAG_ITEM_DESCRIPTION)));
            description.setMovementMethod(LinkMovementMethod.getInstance());
            stripUnderlines(description);

            myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


            if (itemDetails.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") &&
                    !itemDetails.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("0") && !itemDetails.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("") &&
                    FantacyApplication.isDealEnabled(itemDetails.get(Constants.TAG_VALID_TILL))) {
                discountPrice.setVisibility(View.VISIBLE);
                discountPercent.setVisibility(View.VISIBLE);
                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                String costValue = itemDetails.get(Constants.TAG_PRICE);
                float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                        * ((Float.parseFloat(itemDetails.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                itemPrice.setText(itemDetails.get(Constants.TAG_CURRENCY) + " " + df.format(priceValue));
                discountPrice.setText(itemDetails.get(Constants.TAG_CURRENCY) + " " + itemDetails.get(Constants.TAG_PRICE));
                discountPercent.setText(itemDetails.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
            } else {
                discountPrice.setVisibility(View.GONE);
                discountPercent.setVisibility(View.GONE);
                itemPrice.setText(itemDetails.get(Constants.TAG_CURRENCY) + " " + itemDetails.get(Constants.TAG_PRICE));
            }

            description.post(new Runnable() {
                @Override
                public void run() {
                    int lineCnt = description.getLineCount();
                    // Perform any actions you want based on the line count here.
                    if (lineCnt > 3) {
                        description.setMaxLines(3);
                        readMore.setVisibility(View.VISIBLE);
                    } else {
                        readMore.setVisibility(View.GONE);
                    }
                }
            });
            shopName.setText(itemDetails.get(Constants.TAG_SHOP_NAME));

            if (FantacyApplication.isRTL(DetailActivity.this)) {
                playLay.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.video_play_button_bg_rtl));
            } else {
                playLay.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.video_play_button_bg));
            }


            if (mViewPager.getCurrentItem() == position) {
                if (itemDetails.get(Constants.TAG_QUANTITY).equals("0") || itemDetails.get(Constants.TAG_QUANTITY).equals("")) {
                    addCart.setVisibility(View.GONE);
                    buyNow.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.red));
                    buyNow.setText(getString(R.string.sold_out));
                } else {
                    addCart.setVisibility(View.VISIBLE);
                    buyNow.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.colorPrimary));
                    buyNow.setText(getString(R.string.buy_now));
                }
            }

            String imageUrl = itemDetails.get(Constants.TAG_SHOP_IMAGE);
            if (!imageUrl.equals("")) {
                Picasso.get().load(imageUrl).into(shopImage);
            }

            String average_rating = itemDetails.get(Constants.TAG_AVERAGE_RATING);
            if (average_rating.equals("") || average_rating.equals("0")) {
                rating.setVisibility(View.GONE);
               /* ratingTxt.setVisibility(View.INVISIBLE);*/
                ratingTxt.setText("Not Ratted Yet");

            } else {
                rating.setText(average_rating);
                rating.setVisibility(View.VISIBLE);
                ratingTxt.setVisibility(View.VISIBLE);
            }

            String status = itemDetails.get(Constants.TAG_STORE_FOLLOW);
            if (status.equalsIgnoreCase("yes")) {
                followBtn.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.colorPrimary));
                followBtn.setImageResource(R.drawable.store_follow);
            } else {
                followBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.primary_color_sharp_corner));
                followBtn.setImageResource(R.drawable.store_unfollow);
            }

            if(itemDetails.get(Constants.TAG_CATEGORYOFFER).equalsIgnoreCase("") &&
            itemDetails.get(Constants.TAG_SELLEROFFER).equalsIgnoreCase("")){
                offerlay.setVisibility(View.GONE);
            }else{
                offerlay.setVisibility(View.VISIBLE);

                String sellerOfferStr = "", category_offerStr = "";
                JSONObject sellerObject =null;

                if(!itemDetails.get(Constants.TAG_SELLEROFFER).equalsIgnoreCase("")){

                     sellerObject = new JSONObject(itemDetails.get(Constants.TAG_SELLEROFFER));
                    if(sellerObject.has("couponcode")){
                        sellerlay.setVisibility(View.VISIBLE);
                        String couponcode = sellerObject.getString("couponcode");
                        String couponpercentage = sellerObject.getString("couponpercentage");
                        String validfrom = sellerObject.getString("validfrom");
                        String validto = sellerObject.getString("validto");
                        String coupon_count = sellerObject.getString("coupon_count");

                        sellerOfferStr = couponcode +" "+getResources().getString(R.string.offer1)+" "+couponpercentage+"% "+getResources().getString(R.string.offer2)
                                +" "+validfrom +getResources().getString(R.string.offer3)+" "+validto+" "+getResources().getString(R.string.offer4)
                                +" "+coupon_count;

                        if(!sellerOfferStr.equalsIgnoreCase("")) {
                            final String arlen[] = sellerOfferStr.split("\\s+");
                            int i1 = 0;
                            int i2 = arlen[0].length() - 1;
                            selleroffer.setMovementMethod(LinkMovementMethod.getInstance());
                            selleroffer.setText(sellerOfferStr, TextView.BufferType.SPANNABLE);
                            Spannable mySpannable = (Spannable) selleroffer.getText();
                            MyClickableSpan myClickableSpan = new MyClickableSpan(sellerOfferStr) {
                                @Override
                                public void onClick(View widget) {
                                    myClip = ClipData.newPlainText("text", arlen[0]);
                                    myClipboard.setPrimaryClip(myClip);

                                    Toast.makeText(getApplicationContext(), "Coupon Copied",
                                            Toast.LENGTH_SHORT).show();
                                }
                            };
                            mySpannable.setSpan(myClickableSpan, i1, i2 + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                    }else {
                        sellerlay.setVisibility(View.GONE);
                    }
                }

                JSONObject categoryObject = null;
                if(!itemDetails.get(Constants.TAG_CATEGORYOFFER).equalsIgnoreCase("")){
                     categoryObject = new JSONObject(itemDetails.get(Constants.TAG_CATEGORYOFFER));
                    if(categoryObject.has("couponcode")){
                        categorylay.setVisibility(View.VISIBLE);
                        String couponcode = categoryObject.getString("couponcode");
                        String couponpercentage = categoryObject.getString("couponpercentage");
                        String validfrom = categoryObject.getString("validfrom");
                        String validto = categoryObject.getString("validto");
                        String coupon_count = categoryObject.getString("coupon_count");

                        category_offerStr = couponcode +" "+getResources().getString(R.string.offer1)+" "+couponpercentage+"% "+getResources().getString(R.string.offer2)
                                +" "+validfrom +getResources().getString(R.string.offer3)+" "+validto+" "+getResources().getString(R.string.offer4)
                                +" "+coupon_count;

                        if(!category_offerStr.equalsIgnoreCase("")) {
                            final String arlen[] = category_offerStr.split("\\s+");
                            int i1 = 0;
                            int i2 = arlen[0].length() - 1;
                            categoryoffer.setMovementMethod(LinkMovementMethod.getInstance());
                            categoryoffer.setText(category_offerStr, TextView.BufferType.SPANNABLE);
                            Spannable mySpannable = (Spannable) categoryoffer.getText();
                            MyClickableSpan myClickableSpan = new MyClickableSpan(category_offerStr) {
                                @Override
                                public void onClick(View widget) {
                                    myClip = ClipData.newPlainText("text", arlen[0]);
                                    myClipboard.setPrimaryClip(myClip);

                                    Toast.makeText(getApplicationContext(), "Coupon Copied",
                                            Toast.LENGTH_SHORT).show();
                                }
                            };
                            mySpannable.setSpan(myClickableSpan, i1, i2 + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                    }else {
                        categorylay.setVisibility(View.GONE);
                    }


                }


                if(categoryObject!=null && sellerObject!=null) {
                    if (categoryObject.has("couponcode") || sellerObject.has("couponcode"))
                        offerlay.setVisibility(View.VISIBLE);
                    else offerlay.setVisibility(View.GONE);
                }


                if(itemDetails.get(Constants.TAG_CATEGORYOFFER).equalsIgnoreCase(""))
                    categorylay.setVisibility(View.GONE);
                if(itemDetails.get(Constants.TAG_SELLEROFFER).equalsIgnoreCase(""))
                    sellerlay.setVisibility(View.GONE);


                    adminlay.setVisibility(View.GONE);

            }
            progressLay.setVisibility(View.GONE);
            bottomLay.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            commentProgress.setVisibility(View.GONE);
            questionProgress.setVisibility(View.GONE);
            reviewProgress.setVisibility(View.GONE);

            ViewCompat.setNestedScrollingEnabled(imagePager, false);
            productSelfies.setNestedScrollingEnabled(false);
            commentList.setNestedScrollingEnabled(false);
            storeList.setNestedScrollingEnabled(false);
            similarList.setNestedScrollingEnabled(false);
            questionView.setNestedScrollingEnabled(false);
            reviewview.setNestedScrollingEnabled(false);

            if (itemDetails.get(Constants.TAG_SHARE_SELLER).equalsIgnoreCase("yes") &&
                    itemDetails.get(Constants.TAG_SHARE_USER).equalsIgnoreCase("no")) {
                shareLay.setVisibility(View.VISIBLE);
            } else {
                shareLay.setVisibility(View.GONE);
            }

            if (!itemDetails.get(Constants.TAG_PHOTOS).equals("")) {
                ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));
                ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(DetailActivity.this, photosAry);
                imagePager.setAdapter(imagePagerAdapter);
                if (photosAry.size() > 1) {
                    pagerIndicator.setVisibility(View.VISIBLE);
                    pagerIndicator.setViewPager(imagePager);
                } else {
                    pagerIndicator.setVisibility(View.GONE);
                }
            }

            if (!itemDetails.get(Constants.TAG_VIDEO_URL).equals("")) {
                playLay.setVisibility(View.VISIBLE);
            } else {
                playLay.setVisibility(View.GONE);
            }

            if (itemDetails.get(Constants.TAG_SIZE).equals("")) {
                sizeLay.setVisibility(View.GONE);
            } else {
                ArrayList<HashMap<String, String>> sizeAry = FantacyApplication.getSize(itemDetails.get(Constants.TAG_SIZE));
                if (sizeAry.size() == 0) {
                    sizeLay.setVisibility(View.GONE);
                } else {
                    sizeLay.setVisibility(View.VISIBLE);
                    sizeList.setHasFixedSize(true);
                    LinearLayoutManager sizeManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    sizeList.setLayoutManager(sizeManager);

                    DividerItemDecoration itemDivider = new DividerItemDecoration(sizeList.getContext(),
                            sizeManager.getOrientation(),true);
                    itemDivider.setDrawable(getResources().getDrawable(R.drawable.whitespace));
                    sizeList.addItemDecoration(itemDivider);
                    if (FantacyApplication.isRTL(this)) {
                        sizeList.setPadding(FantacyApplication.dpToPx(this, 15), 0, 0, 0);
                    } else {
                        sizeList.setPadding(FantacyApplication.dpToPx(this, 15), 0, FantacyApplication.dpToPx(this, 15), 0);
                    }

                    SizeViewAdapter sizeViewAdapter = new SizeViewAdapter(DetailActivity.this, sizeAry, position, itemsAry.get(mViewPager.getCurrentItem()));
                    sizeList.setAdapter(sizeViewAdapter);
                }
            }

            if (itemDetails.get(Constants.TAG_PRODUCT_SELFIES).equals("")) {
                selfieLay.setVisibility(View.GONE);
            } else {
                selfieLay.setVisibility(View.VISIBLE);
                productSelfies.setHasFixedSize(true);
                productSelfies.getLayoutParams().height = FantacyApplication.dpToPx(DetailActivity.this, 100);
                LinearLayoutManager selfiesManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
                productSelfies.setLayoutManager(selfiesManager);

                selfiesViewAdapter = new ItemViewAdapter("selfie", DetailActivity.this, getProductSelfies(itemDetails.get(Constants.TAG_PRODUCT_SELFIES)));
                productSelfies.setAdapter(selfiesViewAdapter);
            }

            if (itemDetails.get(Constants.TAG_RECENT_COMMENTS).equals("")) {
                noCommentsLay.setVisibility(View.VISIBLE);
                commentList.setVisibility(View.GONE);
                viewAllComment.setVisibility(View.GONE);
            } else {
                noCommentsLay.setVisibility(View.GONE);
                commentList.setVisibility(View.VISIBLE);
                viewAllComment.setVisibility(View.VISIBLE);

                LinearLayoutManager commentsManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.VERTICAL, false);
                commentList.setLayoutManager(commentsManager);

                DividerItemDecorator commentDivider = new DividerItemDecorator(getResources().getDrawable(R.drawable.horizontal_line));
                commentList.addItemDecoration(commentDivider);

                CommentsViewAdapter commentsViewAdapter = new CommentsViewAdapter(DetailActivity.this, getComments(itemDetails.get(Constants.TAG_RECENT_COMMENTS)));
                commentList.setAdapter(commentsViewAdapter);
            }

            if (itemDetails.get(Constants.TAG_STORE_PRODUCTS).equals("")) {
                storeList.setVisibility(View.GONE);
            } else {
                storeList.setVisibility(View.VISIBLE);
                storeList.setHasFixedSize(true);
                storeList.getLayoutParams().height = FantacyApplication.dpToPx(DetailActivity.this, 100);
                LinearLayoutManager storeManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
                storeList.setLayoutManager(storeManager);

                ItemsParsing storeItems = new ItemsParsing(this);
                JSONArray storeAry = new JSONArray(itemDetails.get(Constants.TAG_STORE_PRODUCTS));
                storeViewAdapter = new ItemViewAdapter("item", DetailActivity.this, storeItems.getItems(storeAry));
                storeList.setAdapter(storeViewAdapter);
            }

            if (itemDetails.get(Constants.TAG_SIMILAR_PRODUCTS).equals("")) {
                similarLay.setVisibility(View.GONE);
            } else {
                similarLay.setVisibility(View.VISIBLE);
                similarList.setHasFixedSize(true);
                LinearLayoutManager similarManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
                similarList.setLayoutManager(similarManager);

                DividerItemDecoration itemDivider = new DividerItemDecoration(similarList.getContext(),
                        similarManager.getOrientation(),true);
                itemDivider.setDrawable(getResources().getDrawable(R.drawable.item_divider));
                similarList.addItemDecoration(new EqualSpacingItemDecoration(15));

                ItemsParsing similarItems = new ItemsParsing(this);
                JSONArray similarAry = new JSONArray(itemDetails.get(Constants.TAG_SIMILAR_PRODUCTS));
                similarViewAdapter = new RecyclerViewAdapter(DetailActivity.this, similarItems.getItems(similarAry));
                similarList.setAdapter(similarViewAdapter);
            }

            if (itemDetails.get(Constants.TAG_RECENT_QUESTIONS).equals("")) {
                questionView.setVisibility(View.GONE);
                viewall.setVisibility(View.GONE);
                noquestionlay.setVisibility(View.VISIBLE);
            }else {
                questionView.setVisibility(View.VISIBLE);
                LinearLayoutManager commentsManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                questionView.setLayoutManager(commentsManager);

                DividerItemDecoration commentDivider = new DividerItemDecoration(this, commentsManager.getOrientation(),false);
                commentDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
                questionView.addItemDecoration(commentDivider);

                String buyertype = "";
                if(!itemDetails.get(Constants.TAG_ORDER_ID).equalsIgnoreCase(""))
                    buyertype = "buyertype";

                ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));

                ItemsParsing questionItems = new ItemsParsing(this);
                JSONArray questionArray = new JSONArray(itemDetails.get(Constants.TAG_RECENT_QUESTIONS));
                questionViewAdapter = new QuestionViewAdapter(DetailActivity.this, buyertype,questionItems.getQuestions(questionArray),itemDetails.get(Constants.TAG_ID), photosAry.size()>0?photosAry.get(0).get(Constants.TAG_IMAGE):"");
                questionView.setAdapter(questionViewAdapter);

                if(questionArray.length()==0) {
                    noquestionlay.setVisibility(View.VISIBLE);
                    viewall.setVisibility(View.GONE);
                }
                else{
                    noquestionlay.setVisibility(View.GONE);
                    viewall.setVisibility(View.VISIBLE);
                }
            }

            if (itemDetails.get(Constants.TAG_ITEMREVIEWS).equals("")) {
                viewallrating.setVisibility(View.GONE);
            }else {

                JSONObject jsonObject = new JSONObject(itemDetails.get(Constants.TAG_ITEMREVIEWS));
                if(jsonObject.has("review_count")){
                    averagerating.setText(jsonObject.getString("rating").equalsIgnoreCase("")?"0":jsonObject.getString("rating"));
                    totalrating.setText(jsonObject.getString("rating_count")+" "+getResources().getString(R.string.ratings)+" & ");
                    totalreview.setText(jsonObject.getString("review_count")+" "+getResources().getString(R.string.reviews));

                    if(!jsonObject.getString("rating_count").equalsIgnoreCase("")){
                        int total_ratcnt = Integer.parseInt(jsonObject.getString("rating_count"));
                        float rat5cnt = 0, rat4cnt =0, rat3cnt = 0, rat2cnt =0, rat1cnt =0;
                        if(!jsonObject.getString("five").equalsIgnoreCase("")){
                           rat5cnt = Float.parseFloat(jsonObject.getString("five"));
                        float five_per = (rat5cnt / total_ratcnt)*100;
                            Log.e("ratingcon","-"+five_per);
                        rat5progress.setProgress(Math.round(five_per));}else rat5progress.setProgress(0);

                        if(!jsonObject.getString("four").equalsIgnoreCase("")){
                            rat4cnt = Float.parseFloat(jsonObject.getString("four"));
                        float four_per = (rat4cnt / total_ratcnt)*100;
                        rat4progress.setProgress(Math.round(four_per));}else rat4progress.setProgress(0);

                        if(!jsonObject.getString("three").equalsIgnoreCase("")){
                            rat3cnt = Float.parseFloat(jsonObject.getString("three"));
                        float three_per = (rat3cnt / total_ratcnt)*100;
                        rat3progress.setProgress(Math.round(three_per));}else rat3progress.setProgress(0);

                        if(!jsonObject.getString("two").equalsIgnoreCase("")){
                            rat2cnt = Float.parseFloat(jsonObject.getString("two"));
                        float two_per = (rat2cnt / total_ratcnt)*100;
                        rat2progress.setProgress(Math.round(two_per));}else rat2progress.setProgress(0);

                        if(!jsonObject.getString("one").equalsIgnoreCase("")){
                            rat1cnt = Float.parseFloat(jsonObject.getString("one"));
                        float one_per = (rat1cnt / total_ratcnt)*100;
                        rat1progress.setProgress(Math.round(one_per));}else rat1progress.setProgress(0);

                    }
                    rat5count.setText(""+jsonObject.getString("five"));
                    rat4count.setText(""+jsonObject.getString("four"));
                    rat3count.setText(""+jsonObject.getString("three"));
                    rat2count.setText(""+jsonObject.getString("two"));
                    rat1count.setText(""+jsonObject.getString("one"));
                    if(jsonObject.getString("five").equalsIgnoreCase("")) rat5count.setText("0");
                    if(jsonObject.getString("four").equalsIgnoreCase("")) rat4count.setText("0");
                    if(jsonObject.getString("three").equalsIgnoreCase("")) rat3count.setText("0");
                    if(jsonObject.getString("two").equalsIgnoreCase("")) rat2count.setText("0");
                    if(jsonObject.getString("one").equalsIgnoreCase("")) rat1count.setText("0");

                }

                if(jsonObject.has(Constants.TAG_RESULT)){
                    reviewview.setVisibility(View.VISIBLE);
                    viewallrating.setVisibility(View.VISIBLE);
                    reviewview.setHasFixedSize(true);
                    LinearLayoutManager commentsManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                    reviewview.setLayoutManager(commentsManager);

                    DividerItemDecoration commentDivider = new DividerItemDecoration(this, commentsManager.getOrientation(),false);
                    commentDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
                    reviewview.addItemDecoration(commentDivider);


                    JSONArray reviewarr = jsonObject.getJSONArray(Constants.TAG_RESULT);

                    if(reviewarr.length()==0){
                        viewallrating.setVisibility(View.GONE);
                        reviewview.setVisibility(View.GONE);
                        noreviewLay.setVisibility(View.VISIBLE);
                    }else noreviewLay.setVisibility(View.GONE);

                    ItemsParsing reviewObj = new ItemsParsing(this);
                    reviewAdapter = new ReviewAdapter(DetailActivity.this, reviewObj.getReviews(reviewarr));
                    reviewview.setAdapter(reviewAdapter);
                }
            }

            viewallrating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));

                     Intent intent = new Intent(DetailActivity.this, AllReviews.class);
                    intent.putExtra("itemId",itemDetails.get(Constants.TAG_ID));
                    intent.putExtra("itemTitle",itemDetails.get(Constants.TAG_ITEM_TITLE));
                    intent.putExtra("image",photosAry.size()>0?photosAry.get(0).get(Constants.TAG_IMAGE):"");
                    intent.putExtra("data",itemDetails.get(Constants.TAG_ITEMREVIEWS));

                    startActivity(intent);

                }
            });


            writereview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);

                    if (accesstoken != null) {
                        if (!itemDetails.get(Constants.TAG_ORDER_ID).equalsIgnoreCase("") && itemDetails.get(Constants.TAG_REVIEW_ID).equalsIgnoreCase("")) {

                            ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));
                            Intent login = new Intent(getApplicationContext(), WriteReview.class);
                            login.putExtra("item_name", itemDetails.get(Constants.TAG_ITEM_TITLE));
                            login.putExtra("image", photosAry.size() > 0 ? photosAry.get(0).get(Constants.TAG_IMAGE) : "");
                            login.putExtra("order_id", itemDetails.get(Constants.TAG_ORDER_ID));
                            login.putExtra("item_id", itemDetails.get(Constants.TAG_ID));
                            startActivityForResult(login, 7);

                        } else {
                            if (!itemDetails.get(Constants.TAG_ORDER_ID).equalsIgnoreCase("") && !itemDetails.get(Constants.TAG_REVIEW_ID).equalsIgnoreCase("")) {
                                Log.e("reviewcheck", "- " + itemDetails.get(Constants.TAG_ORDER_ID).equalsIgnoreCase("") + " review id" + itemDetails.get(Constants.TAG_REVIEW_ID).equalsIgnoreCase(""));
                                Toast.makeText(getApplicationContext(), ""+getResources().getString(R.string.alreadyreview), Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(getApplicationContext(), getResources().getString(R.string.younotpurchase), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Intent login = new Intent(DetailActivity.this, SignInActivity.class);
                        startActivity(login);
                    }

                }
            });



            followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken != null) {
                        String status = itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_STORE_FOLLOW);
                        if (status.equalsIgnoreCase("yes")) {
                            followStore(false, (ImageView) v, itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_SHOP_ID));
                            itemsAry.get(mViewPager.getCurrentItem()).put(Constants.TAG_STORE_FOLLOW, "no");
                            ((ImageView) v).setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.primary_color_sharp_corner));
                            ((ImageView) v).setImageResource(R.drawable.store_unfollow);
                        } else {
                            followStore(true, (ImageView) v, itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_SHOP_ID));
                            itemsAry.get(mViewPager.getCurrentItem()).put(Constants.TAG_STORE_FOLLOW, "yes");
                            ((ImageView) v).setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.colorPrimary));
                            ((ImageView) v).setImageResource(R.drawable.store_follow);
                        }
                    } else {
                        Intent login = new Intent(DetailActivity.this, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            readMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (readMore.getText().toString().equals(getResources().getString(R.string.more_details))) {
                        readMore.setText(getResources().getString(R.string.less_details));
                        description.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        readMore.setText(getResources().getString(R.string.more_details));
                        description.setMaxLines(3);
                    }
                }
            });

            addCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    String localItemId = preferences.getString("local_cartId", null);

                    if (accesstoken!= null) {
                        View view = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                        RecyclerView sizeList = (RecyclerView) view.findViewById(R.id.sizeList);
                        LinearLayout sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);

                        if (sizeLay.getVisibility() == View.VISIBLE) {
                            SizeViewAdapter sizeViewAdapter = (SizeViewAdapter) sizeList.getAdapter();
                            HashMap<String, String> sizeMap = sizeViewAdapter.getSizeDetails(selectedSizePosition);
                            String selectedSize = sizeMap.get(Constants.TAG_NAME);
                            String selectedQty = sizeMap.get(Constants.TAG_QTY);
                            if (!selectedQty.equals("") && !selectedQty.equals("0")) {
                                addCartItem(selectedSize);
                            }
                        } else {
                            addCartItem("");
                        }
                    } else {
                        View view = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                        RecyclerView sizeList = (RecyclerView) view.findViewById(R.id.sizeList);
                        LinearLayout sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);

                        if (sizeLay.getVisibility() == View.VISIBLE) {
                            SizeViewAdapter sizeViewAdapter = (SizeViewAdapter) sizeList.getAdapter();
                            HashMap<String, String> sizeMap = sizeViewAdapter.getSizeDetails(selectedSizePosition);
                            String selectedSize = sizeMap.get(Constants.TAG_NAME);
                            String selectedQty = sizeMap.get(Constants.TAG_QTY);
                                if (!selectedQty.equals("") && !selectedQty.equals("0")) {
                                    addLocalCartItem(selectedSize);
                                }
                            } else {
                            addLocalCartItem("");
                        }
                    }
                }
            });

            buyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken!= null) {
                        if (!buyNow.getText().toString().equals(getString(R.string.sold_out))) {

                            Intent c = new Intent(DetailActivity.this, CartActivity.class);
                            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            c.putExtra("shippingId", "0");

                            View view = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                            RecyclerView sizeList = (RecyclerView) view.findViewById(R.id.sizeList);
                            LinearLayout sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);
                            if (sizeLay.getVisibility() == View.VISIBLE) {
                                SizeViewAdapter sizeViewAdapter = (SizeViewAdapter) sizeList.getAdapter();
                                HashMap<String, String> sizeMap = sizeViewAdapter.getSizeDetails(selectedSizePosition);
                                String selectedSize = sizeMap.get(Constants.TAG_NAME);
                                String selectedQty = sizeMap.get(Constants.TAG_QTY);
                                if (!selectedQty.equals("") && !selectedQty.equals("0")) {
                                //    c.putExtra("itemId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID) + "-" +
                                  ///          selectedSize + "-1");
                                    c.putExtra("itemId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                                    c.putExtra("size",selectedSize);
                                    c.putExtra("quantity","1");
                                }
                            } else {
                                //c.putExtra("itemId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID) + "--1");
                                c.putExtra("itemId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                                c.putExtra("quantity","1");
                            }
                            startActivity(c);
                        }
                    } else {
                        Intent login = new Intent(DetailActivity.this, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            askQuestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken!= null) {
                        contactSeller(itemsAry.get(mViewPager.getCurrentItem()));
                    } else {
                        Intent login = new Intent(DetailActivity.this, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            postQuestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken!= null) {
                        Intent login = new Intent(DetailActivity.this, PostQuestion.class);
                        login.putExtra("itemId",itemDetails.get(Constants.TAG_ID));
                        login.putExtra("itemTitle",itemDetails.get(Constants.TAG_ITEM_TITLE));
                        ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));
                        login.putExtra("image",photosAry.size()>0?photosAry.get(0).get(Constants.TAG_IMAGE):"");
                        login.putExtra("question","");
                        login.putExtra("question_id","");
                        startActivityForResult(login,5);
                    } else {
                        Intent login = new Intent(DetailActivity.this, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            viewall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (GetSet.isLogged()) {
                        String buyerty = "";
                    if(!itemDetails.get(Constants.TAG_ORDER_ID).equalsIgnoreCase(""))
                        buyerty = "buyertype";
                        ArrayList<HashMap<String, String>> photosAry = getPhotos(itemDetails.get(Constants.TAG_PHOTOS));
                        String itemImage = photosAry.size()>0?photosAry.get(0).get(Constants.TAG_IMAGE):"";
                        Intent login = new Intent(DetailActivity.this, AllQuestions.class);
                        login.putExtra("itemId",itemDetails.get(Constants.TAG_ID));
                        login.putExtra("itemTitle",itemDetails.get(Constants.TAG_ITEM_TITLE));
                        login.putExtra("buyertype",buyerty);
                        login.putExtra("image",itemImage);
                        startActivity(login);
  //                  }
                }
            });

            playLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View itemView = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                    TextView playText = (TextView) itemView.findViewById(R.id.playText);
                    if (playText.getVisibility() == View.VISIBLE) {
                        toolBarLay.setVisibility(View.INVISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);

                        final WebView mWebView = (WebView) itemView.findViewById(R.id.webView);
                        mWebView.onResume();
                        mWebView.setVisibility(View.VISIBLE);

                        WebChromeClient mWebChromeClient = new WebChromeClient() {
                            public void onProgressChanged(WebView view, int newProgress) {
                            }
                        };

                        int width = display.getWidth();
                        int height = imageHeight;

                        final String videoText = "<html><body style=\"margin:0 0 0 0; padding:0 0 0 0;\"><iframe class=\"youtube-player\" " + "width=\"" + "100%" + "\" height=\"" + "100%" + "\" src=\"https://www.youtube.com/embed/" + FantacyApplication.extractYoutubeId(itemsAry.get((mViewPager.getCurrentItem())).get(Constants.TAG_VIDEO_URL)) + "?autoplay=1&loop=0&autohide=2&controls=0&showinfo=0&theme=dark&modestbranding=1&fs=0&rel=0\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                        Log.v("videoText", "videoText=" + videoText);

                        mWebView.getLayoutParams().height = height;
                        mWebView.getLayoutParams().width = width;

                        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                        mWebView.setWebChromeClient(mWebChromeClient);
                        mWebView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                                Log.v(TAG, "onPageStarted=" + url);
                            }

                            public void onPageFinished(WebView view, String url) {
                                Log.v(TAG, "onPageFinished=" + url);
                                float x = view.getWidth() / 2;
                                float y = view.getHeight() / 2;
                                MotionEvent motionEventDown = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                                MotionEvent motionEventUp = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 200, MotionEvent.ACTION_UP, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                                view.dispatchTouchEvent(motionEventDown);
                                view.dispatchTouchEvent(motionEventUp);
                            }
                        });
                        mWebView.getSettings().setJavaScriptEnabled(true);
                        mWebView.getSettings().setAppCacheEnabled(true);
                        mWebView.setInitialScale(1);
                        mWebView.getSettings().setLoadWithOverviewMode(true);
                        mWebView.getSettings().setUseWideViewPort(true);
                        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

                        mWebView.loadData(videoText, "text/html", "UTF-8");
                    } else {
                        playText.setVisibility(View.VISIBLE);
                    }
                }
            });

            productSelfies.addOnItemTouchListener(recyclerItemClick(this, productSelfies));
            similarList.addOnItemTouchListener(recyclerItemClick(this, similarList));
            storeList.addOnItemTouchListener(recyclerItemClick(this, storeList));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLocalCartItem(String selectedSize) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        final ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Map<String, String> mapItem = new HashMap<String, String>();
        mapItem.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
        mapItem.put("qty", "1");
        mapItem.put("size", selectedSize);
        mapItem.put("item_title", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ITEM_TITLE));
        mapItem.put("item_price", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_PRICE));
        mapItem.put("item_main_price", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_MAIN_PRICE));
        mapItem.put("shipping_time", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_SHIPPING_TIME));
        Log.d(TAG, "addItemParams=" + mapItem);

        String getLocalCart = preferences.getString("localCart", null);
        Log.d(TAG, "addLocalCartItem1: " + getLocalCart);
        if(getLocalCart == null){
            Map<String, Map<String, String>> mainMap = new HashMap<String, Map<String, String>>();
            mainMap.put(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID),  mapItem);
            Log.d(TAG, "addCartItemParams=" + mainMap);
            jsonMap = new Gson().toJson(mainMap);

            preferences.edit().putString("localCart", jsonMap).apply();
            Toast.makeText(this, "Item Added to cart !", Toast.LENGTH_SHORT).show();
            localCartsum = mainMap.values().size();
            preferences.edit().putString("localCartSum", String.valueOf(localCartsum));

            localItemCount();

        } else {
            retMainMap = new Gson().fromJson(
                    getLocalCart, new TypeToken<HashMap<String, Map<String, String>>>() {}.getType()
            );

            localCartsum = retMainMap.values().size();
            Log.d(TAG, "addLocalCartItem2: "+ retMainMap+"...."+localCartsum);
            preferences.edit().putString("localCartSum", String.valueOf(localCartsum));


            if(!retMainMap.containsKey(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID))){
                retMainMap.put(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID),  mapItem);
                jsonMap = new Gson().toJson(retMainMap);
                preferences.edit().putString("localCart", jsonMap).apply();
                Toast.makeText(this, "Item Added to cart !", Toast.LENGTH_SHORT).show();

                localItemCount();

            }else {
                Toast.makeText(this, "Already added to cart", Toast.LENGTH_SHORT).show();

            }
        }



    }

    private void localItemCount() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        try {
            if (FragmentMainActivity.cartCount.equals("")) {
                FragmentMainActivity.cartCount = "0";
            }
            int localCount = Integer.parseInt(FragmentMainActivity.cartCount) + 1;
            FragmentMainActivity.cartCount = String.valueOf(localCount);
            FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), DetailActivity.this);
            preferences.edit().putString("localCartCount", String.valueOf(localCount));

            Log.d(TAG, "localItemCount: "+"---"+String.valueOf(localCount));

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void getItemDetails(final int pos) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEM_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getItemDetailsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject temp = json.optJSONObject(Constants.TAG_RESULT);
                        HashMap<String, String> map = new HashMap<>();
                        String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                        String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                        String item_description = DefensiveClass.optString(temp, Constants.TAG_ITEM_DESCRIPTION);
                        String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);
                        String mainprice = DefensiveClass.optString(temp, Constants.TAG_MAIN_PRICE);
                        String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                        String deal_enabled = DefensiveClass.optString(temp, Constants.TAG_DEAL_ENABLED);
                        String discount_percentage = DefensiveClass.optString(temp, Constants.TAG_DISCOUNT_PERCENTAGE);
                        String valid_till = DefensiveClass.optString(temp, Constants.TAG_VALID_TILL);
                        String quantity = DefensiveClass.optString(temp, Constants.TAG_QUANTITY);
                        String cod = DefensiveClass.optString(temp, Constants.TAG_COD);
                        String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                        String report = DefensiveClass.optString(temp, Constants.TAG_REPORT);
                        String reward_points = DefensiveClass.optString(temp, Constants.TAG_REWARD_POINTS);
                        String share_seller = DefensiveClass.optString(temp, Constants.TAG_SHARE_SELLER);
                        String share_user = DefensiveClass.optString(temp, Constants.TAG_SHARE_USER);
                        String approve = DefensiveClass.optString(temp, Constants.TAG_APPROVE);
                        String buy_type = DefensiveClass.optString(temp, Constants.TAG_BUY_TYPE);
                        String affiliate_link = DefensiveClass.optString(temp, Constants.TAG_AFFILIATE_LINK);
                        String shipping_time = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
                        String product_url = DefensiveClass.optString(temp, Constants.TAG_PRODUCT_URL);
                        String shop_id = DefensiveClass.optString(temp, Constants.TAG_SHOP_ID);
                        String shop_name = DefensiveClass.optString(temp, Constants.TAG_SHOP_NAME);
                        String shop_image = DefensiveClass.optString(temp, Constants.TAG_SHOP_IMAGE);
                        String store_follow = DefensiveClass.optString(temp, Constants.TAG_STORE_FOLLOW);
                        String average_rating = DefensiveClass.optString(temp, Constants.TAG_AVERAGE_RATING);
                        String fbshare_discount = DefensiveClass.optString(temp, Constants.TAG_FBSHARE_DISCOUNT);
                        String video_url = DefensiveClass.optString(temp, Constants.TAG_VIDEO_URL);

                        JSONObject  selleroffer = temp.has(Constants.TAG_SELLEROFFER)?temp.getJSONObject(Constants.TAG_SELLEROFFER):new JSONObject();
                        JSONObject  categoryoffer = temp.has(Constants.TAG_CATEGORYOFFER)?temp.getJSONObject(Constants.TAG_CATEGORYOFFER):new JSONObject();


                        String order_id = DefensiveClass.optString(temp, Constants.TAG_ORDER_ID);
                        String review_id = DefensiveClass.optString(temp, Constants.TAG_REVIEW_ID);

                        JSONArray size = temp.optJSONArray(Constants.TAG_SIZE);
                        if (size == null) {
                            map.put(Constants.TAG_SIZE, "");
                        } else if (size.length() == 0) {
                            map.put(Constants.TAG_SIZE, "");
                        } else {
                            map.put(Constants.TAG_SIZE, size.toString());
                        }

                        JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                        if (photos == null) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() == 0) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else {
                            map.put(Constants.TAG_PHOTOS, photos.toString());
                        }

                        JSONArray selfies = temp.optJSONArray(Constants.TAG_PRODUCT_SELFIES);
                        if (selfies == null) {
                            map.put(Constants.TAG_PRODUCT_SELFIES, "");
                        } else if (selfies.length() == 0) {
                            map.put(Constants.TAG_PRODUCT_SELFIES, "");
                        } else {
                            map.put(Constants.TAG_PRODUCT_SELFIES, selfies.toString());
                        }

                        JSONArray comments = temp.optJSONArray(Constants.TAG_RECENT_COMMENTS);
                        if (comments == null) {
                            map.put(Constants.TAG_RECENT_COMMENTS, "");
                        } else if (comments.length() == 0) {
                            map.put(Constants.TAG_RECENT_COMMENTS, "");
                        } else {
                            map.put(Constants.TAG_RECENT_COMMENTS, comments.toString());
                        }

                        JSONArray questions = temp.optJSONArray(Constants.TAG_RECENT_QUESTIONS);
                        if (questions == null) {
                            map.put(Constants.TAG_RECENT_QUESTIONS, "");
                        } else if (questions.length() == 0) {
                            map.put(Constants.TAG_RECENT_QUESTIONS, "");
                        } else {
                            map.put(Constants.TAG_RECENT_QUESTIONS, questions.toString());
                        }

                        JSONArray storeProducts = temp.optJSONArray(Constants.TAG_STORE_PRODUCTS);
                        if (storeProducts == null) {
                            map.put(Constants.TAG_STORE_PRODUCTS, "");
                        } else if (storeProducts.length() == 0) {
                            map.put(Constants.TAG_STORE_PRODUCTS, "");
                        } else {
                            map.put(Constants.TAG_STORE_PRODUCTS, storeProducts.toString());
                        }

                        JSONArray similarProducts = temp.optJSONArray(Constants.TAG_SIMILAR_PRODUCTS);
                        if (similarProducts == null) {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, "");
                        } else if (similarProducts.length() == 0) {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, "");
                        } else {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, similarProducts.toString());
                        }

                        JSONObject itemreviews = temp.getJSONObject(Constants.TAG_ITEMREVIEWS);
                        if (itemreviews == null) {
                            map.put(Constants.TAG_ITEMREVIEWS, "");
                        } else if (similarProducts.length() == 0) {
                            map.put(Constants.TAG_ITEMREVIEWS, "");
                        } else {
                            map.put(Constants.TAG_ITEMREVIEWS, itemreviews.toString());
                        }


                        map.put(Constants.TAG_ID, id);
                        map.put(Constants.TAG_ITEM_TITLE, item_title);
                        map.put(Constants.TAG_ITEM_DESCRIPTION, item_description);
                        map.put(Constants.TAG_CURRENCY, currency);
                        map.put(Constants.TAG_MAIN_PRICE, mainprice);
                        map.put(Constants.TAG_PRICE, price);
                        map.put(Constants.TAG_DEAL_ENABLED, deal_enabled);
                        map.put(Constants.TAG_DISCOUNT_PERCENTAGE, discount_percentage);
                        map.put(Constants.TAG_VALID_TILL, valid_till);
                        map.put(Constants.TAG_QUANTITY, quantity);
                        map.put(Constants.TAG_COD, cod);
                        map.put(Constants.TAG_LIKED, liked);
                        map.put(Constants.TAG_REPORT, report);
                        map.put(Constants.TAG_REWARD_POINTS, reward_points);
                        map.put(Constants.TAG_SHARE_SELLER, share_seller);
                        map.put(Constants.TAG_SHARE_USER, share_user);
                        map.put(Constants.TAG_APPROVE, approve);
                        map.put(Constants.TAG_BUY_TYPE, buy_type);
                        map.put(Constants.TAG_AFFILIATE_LINK, affiliate_link);
                        map.put(Constants.TAG_SHIPPING_TIME, shipping_time);
                        map.put(Constants.TAG_PRODUCT_URL, product_url);
                        map.put(Constants.TAG_SHOP_ID, shop_id);
                        map.put(Constants.TAG_SHOP_NAME, shop_name);
                        map.put(Constants.TAG_SHOP_IMAGE, shop_image);
                        map.put(Constants.TAG_STORE_FOLLOW, store_follow);
                        map.put(Constants.TAG_AVERAGE_RATING, average_rating);
                        map.put(Constants.TAG_FBSHARE_DISCOUNT, fbshare_discount);
                        map.put(Constants.TAG_VIDEO_URL, video_url);

                        map.put(Constants.TAG_ORDER_ID, order_id);
                        map.put(Constants.TAG_REVIEW_ID, review_id);
                        map.put(Constants.TAG_SELLEROFFER, selleroffer.toString());
                       // map.put(Constants.TAG_ADMINOFFER, adminoffer);
                        map.put(Constants.TAG_CATEGORYOFFER, categoryoffer.toString());

                        itemsAry.set(pos, map);

                        Log.d(TAG, "onResponseDetail: " +itemsAry);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        setItemDetails(pos, map);
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*Intent i = new Intent(DetailActivity.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(DetailActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getItemDetailsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", customerId);
                }
                map.put("item_id", itemsAry.get(pos).get(Constants.TAG_ID));
                map.put("get_type", type);
                Log.v(TAG, "getItemDetailsParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req, "pos" + pos);
    }

    private ArrayList<HashMap<String, String>> getPhotos(String json) {
        ArrayList<HashMap<String, String>> photoAry = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray photos = new JSONArray(json);
            for (int p = 0; p < photos.length(); p++) {
                JSONObject pobj = photos.getJSONObject(p);

                HashMap<String, String> pmap = new HashMap<>();

                String image350 = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_350);
                String height = DefensiveClass.optString(pobj, Constants.TAG_HEIGHT);
                String width = DefensiveClass.optString(pobj, Constants.TAG_WIDTH);
                String imageOrg = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_ORIGINAL);

                pmap.put(Constants.TAG_IMAGE_350, image350);
                pmap.put(Constants.TAG_IMAGE, imageOrg);
                pmap.put(Constants.TAG_HEIGHT, height);
                pmap.put(Constants.TAG_WIDTH, width);
                pmap.put("type", "image");

                photoAry.add(pmap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoAry;
    }

    private ArrayList<HashMap<String, String>> getComments(String json) {
        ArrayList<HashMap<String, String>> commentsAry = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray comments = new JSONArray(json);
            for (int p = 0; p < comments.length(); p++) {
                JSONObject pobj = comments.getJSONObject(p);

                HashMap<String, String> pmap = new HashMap<>();

                String comment_id = DefensiveClass.optString(pobj, Constants.TAG_COMMENT_ID);
                String comment = DefensiveClass.optString(pobj, Constants.TAG_COMMENT);
                String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                String full_name = DefensiveClass.optString(pobj, Constants.TAG_FULL_NAME);

                pmap.put(Constants.TAG_COMMENT_ID, comment_id);
                pmap.put(Constants.TAG_COMMENT, comment);
                pmap.put(Constants.TAG_USER_ID, user_id);
                pmap.put(Constants.TAG_USER_IMAGE, user_image);
                pmap.put(Constants.TAG_USER_NAME, user_name);
                pmap.put(Constants.TAG_FULL_NAME, full_name);

                commentsAry.add(pmap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentsAry;
    }

    private ArrayList<HashMap<String, String>> getProductSelfies(String json) {
        ArrayList<HashMap<String, String>> photoAry = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray photos = new JSONArray(json);
            for (int p = 0; p < photos.length(); p++) {
                JSONObject pobj = photos.getJSONObject(p);

                HashMap<String, String> pmap = new HashMap<>();

                String image350 = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_350);
                String imageOrg = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_ORG);
                String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);

                pmap.put(Constants.TAG_IMAGE_350, image350);
                pmap.put(Constants.TAG_IMAGE_ORG, imageOrg);
                pmap.put(Constants.TAG_USER_ID, user_id);
                pmap.put(Constants.TAG_USER_NAME, user_name);
                pmap.put(Constants.TAG_USER_IMAGE, user_image);

                photoAry.add(pmap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoAry;
    }

    public void deleteDialog(final String commentId) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);

        title.setText(getString(R.string.deleteCommentMsg));
        yes.setText(getString(R.string.yes));
        no.setText(getString(R.string.no));

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteComment(commentId);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void deleteComment(final String commentId) {
        final ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELETE_COMMENT, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.v(TAG, "deleteCommentRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.comment_deleted_successfully), Toast.LENGTH_SHORT);
                        int position = mViewPager.getCurrentItem();
                        View view = mViewPager.findViewWithTag("posi" + position);
                        ProgressBar commentProgress = (ProgressBar) view.findViewById(R.id.commentProgress);
                        RelativeLayout noCommentsLay = (RelativeLayout) view.findViewById(R.id.noCommentsLay);
                        RecyclerView commentList = (RecyclerView) view.findViewById(R.id.commentList);
                        commentProgress.setVisibility(View.VISIBLE);
                        noCommentsLay.setVisibility(View.GONE);
                        commentList.setVisibility(View.GONE);
                        getItemDetails(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "deleteCommentError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("comment_id", commentId);
                map.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                Log.v(TAG, "deleteCommentParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void addCollection() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.add_to_collection);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 65 / 100);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.4f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);
        dialog.setCancelable(true);
        final EditText createname = (EditText) dialog.findViewById(R.id.editText);
        TextView create = (TextView) dialog.findViewById(R.id.create);
        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        RecyclerView collectionList = (RecyclerView) dialog.findViewById(R.id.collectionList);

        collectionManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        collectionList.setLayoutManager(collectionManager);

        getCollectionList();

        collectionViewAdapter = new CollectionViewAdapter(this, collections);
        collectionList.setAdapter(collectionViewAdapter);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createname.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(DetailActivity.this, getString(R.string.please_enter_collection_name), Toast.LENGTH_SHORT);
                } else {
                    createCollection(createname.getText().toString());
                    createname.setText("");
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void getCollectionList() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_COLLECTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getCollectionListRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        collections.clear();
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<>();

                            String collection_id = DefensiveClass.optString(temp, Constants.TAG_COLLECTION_ID);
                            String collection_name = DefensiveClass.optString(temp, Constants.TAG_COLLECTION_NAME);
                            String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
                            String checked = DefensiveClass.optString(temp, Constants.TAG_CHECKED);

                            map.put(Constants.TAG_COLLECTION_ID, collection_id);
                            map.put(Constants.TAG_COLLECTION_NAME, collection_name);
                            map.put(Constants.TAG_TYPE, type);
                            map.put(Constants.TAG_CHECKED, checked);

                            collections.add(map);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(DetailActivity.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(DetailActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    collectionViewAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getCollectionListError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                Log.v(TAG, "getCollectionListParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void createCollection(final String name) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_COLLECTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        getCollectionList();
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.collection_success), Toast.LENGTH_SHORT);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "createCollectionError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                map.put("collection_name", name);
                Log.i(TAG, "createCollectionParams: " + map);
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void updateCollectionItems(final int pos, final String checked) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UPDATE_COLLECTIONS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (collections.get(pos).get(Constants.TAG_CHECKED).equals("0")) {
                            collections.get(pos).put(Constants.TAG_CHECKED, "1");
                        } else {
                            collections.get(pos).put(Constants.TAG_CHECKED, "0");
                        }
                        collectionViewAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "updateCollectionItemsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                map.put("collection_id", collections.get(pos).get(Constants.TAG_COLLECTION_ID));
                map.put("checked", checked);
                Log.v(TAG, "updateCollectionItemsParams=" + map);
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }


        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void showProductSelfie(HashMap<String, String> data) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.product_selfie_view);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 80 / 100);
        dialog.setCancelable(true);

        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        ImageView userImage = (ImageView) dialog.findViewById(R.id.userImage);
        TextView userName = (TextView) dialog.findViewById(R.id.userName);

        userName.setText(data.get(Constants.TAG_USER_NAME));

        String imageUrl = data.get(Constants.TAG_IMAGE_ORG);
        if (!imageUrl.equals("")) {
            Picasso.get().load(imageUrl).into(image);
        }

        String userImg = data.get(Constants.TAG_USER_IMAGE);
        if (!userImg.equals("")) {
            Picasso.get().load(userImg).into(userImage);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void followStore(final Boolean follow, final ImageView followBtn, final String storeId) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_STORE;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_STORE;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "followStoreRes: " + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            followBtn.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.primary_color_sharp_corner));
                            followBtn.setImageResource(R.drawable.store_unfollow);
                        } else {
                            followBtn.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.colorPrimary));
                            followBtn.setImageResource(R.drawable.store_follow);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "followStoreError: " + error.getMessage());
                if (follow) {
                    followBtn.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.primary_color_sharp_corner));
                    followBtn.setImageResource(R.drawable.store_unfollow);
                } else {
                    followBtn.setBackgroundColor(ContextCompat.getColor(DetailActivity.this, R.color.colorPrimary));
                    followBtn.setImageResource(R.drawable.store_follow);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("store_id", storeId);
                Log.v(TAG, "followStoreParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void contactSeller(final HashMap<String, String> tempMap) {

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_VIEW_SELLER_MESSAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.v(TAG, "contactSellerRes=" + res);
                //https://prodev.hitasoft.in/fantacy5.0/fantacy/
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        String chatId = DefensiveClass.optString(result, Constants.TAG_CHAT_ID);
                        Intent i = new Intent(DetailActivity.this, SellerChat.class);
                        i.putExtra("from", "detail");
                        i.putExtra("chatid", chatId);
                        startActivity(i);
                    } else {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        ArrayList<String> subAry = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);
                            String subject = DefensiveClass.optString(temp, Constants.TAG_SUBJECT);

                            subAry.add(subject);
                        }
                        Intent i = new Intent(DetailActivity.this, ContactSeller.class);
                        i.putExtra("itemId", tempMap.get(Constants.TAG_ID));
                        i.putExtra("shopId", tempMap.get(Constants.TAG_SHOP_ID));
                        i.putExtra("image", tempMap.get(Constants.TAG_IMAGE));
                        i.putExtra("itemTitle", tempMap.get(Constants.TAG_ITEM_TITLE));
                        i.putExtra("data", subAry);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "contactSellerError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", tempMap.get(Constants.TAG_ID));
                map.put("shop_id", tempMap.get(Constants.TAG_SHOP_ID));
                map.put("offset", "0");
                map.put("limit", "10");
                Log.v(TAG, "contactSellerParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void addCartItem(final String size) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_TO_CART, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "addCartItemRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(DetailActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT);
                        try {
                            if (FragmentMainActivity.cartCount.equals("")) {
                                FragmentMainActivity.cartCount = "0";
                            }
                            int count = Integer.parseInt(FragmentMainActivity.cartCount) + 1;
                            FragmentMainActivity.cartCount = String.valueOf(count);
                            FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), DetailActivity.this);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else if (DefensiveClass.optString(json, Constants.TAG_MESSAGE).trim().equals(" Item already in cart".trim()))
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.item_already_in_cart), Toast.LENGTH_SHORT);
                    else
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "addCartItemError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                map.put("qty", "1");
                map.put("size", size);
                Log.v(TAG, "addCartItemParams=" + map);
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private RecyclerItemClickListener recyclerItemClick(final Context context, final RecyclerView recyclerView) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                switch (recyclerView.getId()) {
                    case R.id.similarList:
                        RecyclerViewAdapter similarViewAdapter = (RecyclerViewAdapter) recyclerView.getAdapter();
                        Intent i = new Intent(context, DetailActivity.class);
                        i.putExtra("items", similarViewAdapter.getItems());
                        i.putExtra("position", position);
                        startActivity(i);
                        break;
                    case R.id.storeList:
                        ItemViewAdapter storeViewAdapter = (ItemViewAdapter) recyclerView.getAdapter();
                        Intent j = new Intent(context, DetailActivity.class);
                        j.putExtra("items", storeViewAdapter.getItems());
                        j.putExtra("position", position);
                        startActivity(j);
                        break;
                    case R.id.productSelfies:
                        ItemViewAdapter selfiesViewAdapter = (ItemViewAdapter) recyclerView.getAdapter();
                        showProductSelfie(selfiesViewAdapter.getItems().get(position));
                        break;
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });

        return recyclerItemClickListener;
    }

    private void reportItem(final int position, final String check) {

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        String CallingAPI;
        if (check.equals("yes")) // Report Api
            CallingAPI = Constants.API_REPORT_ITEM;
        else  // undoreport Api
            CallingAPI = Constants.API_UNDO_REPORT;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "reportItemRes" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(DetailActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT);
                    } else {
                        FantacyApplication.showToast(DetailActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT);
                        if (check.equals("yes"))
                            itemsAry.get(position).put(Constants.TAG_REPORT, "no");
                        else
                            itemsAry.get(position).put(Constants.TAG_REPORT, "yes");
                        helper.updateItemDetails(itemsAry.get(position).get(Constants.TAG_ID), Constants.TAG_REPORT, itemsAry.get(position).get(Constants.TAG_REPORT));
                        mSectionsPagerAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (check.equals("yes"))
                    itemsAry.get(position).put(Constants.TAG_REPORT, "no");
                else
                    itemsAry.get(position).put(Constants.TAG_REPORT, "yes");
                helper.updateItemDetails(itemsAry.get(position).get(Constants.TAG_ID), Constants.TAG_REPORT, itemsAry.get(position).get(Constants.TAG_REPORT));
                Log.e(TAG, "reportItemError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemsAry.get(position).get(Constants.TAG_ID));
                Log.i(TAG, "reportItemParams: " + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public void setupFacebookShareIntent(String image) {
    Log.e("workingface","-"+itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_PRODUCT_URL));

        final ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_PRODUCT_URL)))
                .setShareHashtag(new ShareHashtag.Builder()
                        .setHashtag("#" + getString(R.string.app_name))
                        .build())
                .build();
        if (shareDialog.canShow(linkContent)) {
            boolean hasPackage = isPackageExisted(DetailActivity.this, "com.facebook.katana");
            if (hasPackage) {
                fb = true;
                shareDialog.show(linkContent, ShareDialog.Mode.NATIVE);
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana")));
                } catch (Exception e) {
                    FantacyApplication.showToast(getApplicationContext(), "Unable to Connect Try Again...", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
            }
        }
    }

    private void facebookShare(final int position) {

        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FACEBOOK_SHARE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "facebookShareRes: " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        String promocode = DefensiveClass.optString(json, Constants.TAG_PROMO_CODE);
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        HashMap<String, String> itemmap = itemsAry.get(position);
                        itemmap.put(Constants.TAG_SHARE_USER, "yes");
                        itemsAry.remove(position);
                        itemsAry.add(position, itemmap);
                        setItemDetails(position, itemmap);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                        fbCouponDialog(promocode, message);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "facebookShareError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("item_id", itemsAry.get(position).get(Constants.TAG_ID));
                Log.i(TAG, "facebookShareParams: " + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void fbCouponDialog(final String promocode, final String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coupon_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 40 / 100);
        dialog.setCancelable(true);

        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        TextView textView = (TextView) dialog.findViewById(R.id.text);
        TextView title = (TextView) dialog.findViewById(R.id.title);

        final TextView code = (TextView) dialog.findViewById(R.id.code);
        code.setText(promocode);
        textView.setText(message);
        title.setVisibility(View.VISIBLE);

        if (FantacyApplication.isRTL(DetailActivity.this)) {
            textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) DetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(R.string.app_name + "referral code", (code.getText().toString()));
                clipboard.setPrimaryClip(clip);
                FantacyApplication.showToast(DetailActivity.this, getString(R.string.promocodecopied), Toast.LENGTH_SHORT);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener() {
        ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e("positionDetails"," - "+position);
                if (toolBarLay.getVisibility() == View.INVISIBLE) {
                    toolBarLay.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.GONE);
                    View itemView = mViewPager.findViewWithTag("posi" + lastSwipePosition);
                    WebView mWebView = (WebView) itemView.findViewById(R.id.webView);
                    TextView playText = (TextView) itemView.findViewById(R.id.playText);
                    mWebView.loadUrl("");
                    mWebView.stopLoading();
                    mWebView.setVisibility(View.GONE);
                    playText.setVisibility(View.GONE);
                }
                lastSwipePosition = position;

                getItemDetails(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        return changeListener;
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s.toString().trim());
    }

    public static boolean isPackageExisted(Context c, String targetPackage) {

        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            Intent g = new Intent(Intent.ACTION_SEND);
            g.setType("text/plain");
            g.putExtra(Intent.EXTRA_TEXT, itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_PRODUCT_URL));
            startActivity(Intent.createChooser(g, "Share"));
            return true;
        } else if (id == R.id.report) {
            if (GetSet.isLogged()) {
                final Dialog dialog = new Dialog(DetailActivity.this);
                Display display = getWindowManager().getDefaultDisplay();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.default_popup);
                dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);

                if (helper.getItemDetails(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID), Constants.TAG_REPORT).equalsIgnoreCase("yes")) {
                    title.setText(getString(R.string.really_undo_report));
                } else {
                    title.setText(getString(R.string.really_report));
                }
                yes.setText(getString(R.string.yes));
                no.setText(getString(R.string.no));

                no.setVisibility(View.VISIBLE);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (helper.getItemDetails(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID), Constants.TAG_REPORT).equalsIgnoreCase("yes")) {
                            reportItem(mViewPager.getCurrentItem(), "no");
                            helper.updateItemDetails(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID), Constants.TAG_REPORT, "no");
                        } else {
                            reportItem(mViewPager.getCurrentItem(), "yes");
                            helper.updateItemDetails(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID), Constants.TAG_REPORT, "yes");
                        }
                        dialog.dismiss();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                if (!dialog.isShowing()) {
                    dialog.show();
                }
            } else {
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (helper.getItemDetails(itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID), Constants.TAG_REPORT).equalsIgnoreCase("yes")) {
            menu.findItem(R.id.report).setTitle(getString(R.string.undo_report));
        } else {
            menu.findItem(R.id.report).setTitle(getString(R.string.report_inappropriate));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (fb) {
            fb = false;
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == RESULT_OK && requestCode == 3) {
            int position = mViewPager.getCurrentItem();
            View view = mViewPager.findViewWithTag("posi" + position);
            ProgressBar commentProgress = (ProgressBar) view.findViewById(R.id.commentProgress);
            RelativeLayout noCommentsLay = (RelativeLayout) view.findViewById(R.id.noCommentsLay);
            RecyclerView commentList = (RecyclerView) view.findViewById(R.id.commentList);
            commentProgress.setVisibility(View.VISIBLE);
            noCommentsLay.setVisibility(View.GONE);
            commentList.setVisibility(View.GONE);
            getItemDetails(position);
        }else if (resultCode == RESULT_OK && requestCode == 5) {
            int position = mViewPager.getCurrentItem();
            View view = mViewPager.findViewWithTag("posi" + position);
            ProgressBar questionProgress = (ProgressBar) view.findViewById(R.id.questionProgress);
            //RelativeLayout noCommentsLay = (RelativeLayout) view.findViewById(R.id.noCommentsLay);
            RecyclerView questionview = (RecyclerView) view.findViewById(R.id.questionview);
            questionProgress.setVisibility(View.VISIBLE);
            //noCommentsLay.setVisibility(View.GONE);
            questionview.setVisibility(View.GONE);
            getItemDetails(position);
        }else if (resultCode == RESULT_OK && requestCode == 7) {
            int position = mViewPager.getCurrentItem();
            View view = mViewPager.findViewWithTag("posi" + position);
            ProgressBar reviewProgress = (ProgressBar) view.findViewById(R.id.reviewProgress);
            //RelativeLayout noCommentsLay = (RelativeLayout) view.findViewById(R.id.noCommentsLay);
            RecyclerView reviewview = (RecyclerView) view.findViewById(R.id.reviewview);
            reviewProgress.setVisibility(View.VISIBLE);
            //noCommentsLay.setVisibility(View.GONE);
            reviewview.setVisibility(View.GONE);
            getItemDetails(position);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), DetailActivity.this);
        if (NetworkReceiver.isConnected()) {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        } else {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        View itemView = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
        if (itemView != null) {
            WebView mWebView = (WebView) itemView.findViewById(R.id.webView);
            if (mWebView.getVisibility() == View.VISIBLE) {
                float x = mWebView.getWidth() / 2;
                float y = mWebView.getHeight() / 2;
                MotionEvent motionEventDown = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                MotionEvent motionEventUp = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 200, MotionEvent.ACTION_UP, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                mWebView.dispatchTouchEvent(motionEventDown);
                mWebView.dispatchTouchEvent(motionEventUp);
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                if (from != null && from.equals("deeplink")) {
                    Intent sucess = new Intent(this, FragmentMainActivity.class);
                    this.startActivity(sucess);
                }
                finish();

                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String accesstoken = preferences.getString("TOKEN", null);
                if (accesstoken != null) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    /*Intent login = new Intent(this, LoginActivity.class);
                    startActivity(login);*/
                    String getLocalCart = preferences.getString("localCart", null);
                    Intent c = new Intent(this, LocalCartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    c.putExtra("localCartMap", getLocalCart);
                    startActivity(c);
                }
                break;
            case R.id.cancelBtn:
                View itemView = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                WebView mWebView = (WebView) itemView.findViewById(R.id.webView);
                TextView playText = (TextView) itemView.findViewById(R.id.playText);
                float x = mWebView.getWidth() / 2;
                float y = mWebView.getHeight() / 2;
                MotionEvent motionEventDown = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                MotionEvent motionEventUp = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 200, MotionEvent.ACTION_UP, x, y, 0.5f, 0.5f, 0, 0.5f, 0.5f, InputDevice.SOURCE_TOUCHSCREEN, 0);
                mWebView.dispatchTouchEvent(motionEventDown);
                mWebView.dispatchTouchEvent(motionEventUp);
                mWebView.onPause();
                mWebView.loadUrl("");
                mWebView.stopLoading();
                mWebView.clearCache(true);
                mWebView.clearHistory();
                mWebView.setVisibility(View.GONE);
                playText.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                toolBarLay.setVisibility(View.VISIBLE);
                break;
        }
    }

    class SectionsPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> items;

        public SectionsPagerAdapter(Context act, ArrayList<HashMap<String, String>> temp) {
            this.items = temp;
            this.context = act;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.detail_product_layout,
                    collection, false);

            itemView.setTag("posi" + position);
            Log.v(TAG, "instantiateItemPosition=" + position);

            TextView shareit = (TextView) itemView.findViewById(R.id.shareit);
            TextView facebookshare = (TextView) itemView.findViewById(R.id.facebookshare);
            TextView itemName = (TextView) itemView.findViewById(R.id.itemName);
            TextView itemPrice = (TextView) itemView.findViewById(R.id.itemPrice);
            TextView discountPrice = (TextView) itemView.findViewById(R.id.discountPrice);
            TextView discountPercent = (TextView) itemView.findViewById(R.id.discountPercent);
            ViewPager imagePager = (ViewPager) itemView.findViewById(R.id.imagePager);
            TextView viewAllComment = (TextView) itemView.findViewById(R.id.viewAllComment);
            TextView writeComment = (TextView) itemView.findViewById(R.id.writeComment);

            final CheckBox likedBtn = (CheckBox) itemView.findViewById(R.id.likedBtn);
            final TextView likeCount = (TextView) itemView.findViewById(R.id.productLikeCount);
            LinearLayout addCollectionLay = (LinearLayout) itemView.findViewById(R.id.addCollectionLay);
            LinearLayout groupGiftLay = (LinearLayout) itemView.findViewById(R.id.groupGiftLay);
            ImageView shopImage = (ImageView) itemView.findViewById(R.id.shopImage);
            TextView codText = (TextView) itemView.findViewById(R.id.codText);
            RelativeLayout progressLay = (RelativeLayout) itemView.findViewById(R.id.progress);
            TextView shopName = (TextView) itemView.findViewById(R.id.shopName);
            ImageView codImage = (ImageView) itemView.findViewById(R.id.codImage);
            TextView shipping = (TextView) itemView.findViewById(R.id.shipping);
            TextView availableQuantity = (TextView) itemView.findViewById(R.id.availableQuantity);

            final HashMap<String, String> tempMap = items.get(position);

            progressLay.setVisibility(View.VISIBLE);
            ViewCompat.setNestedScrollingEnabled(imagePager, false);

            itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));

            facebookshare.setText(getString(R.string.share_facebook) + " " + tempMap.get(Constants.TAG_FBSHARE_DISCOUNT) + "% " + getString(R.string.share_discount));
            String shippingTime = "";
            switch (tempMap.get(Constants.TAG_SHIPPING_TIME)) {
                case "One business day":
                    shippingTime = getString(R.string.one_business_day);
                    break;
                case "Two business days":
                    shippingTime = getString(R.string.two_business_days);
                    break;
                case "Three business days":
                    shippingTime = getString(R.string.three_business_days);
                    break;
                case "Four business days":
                    shippingTime = getString(R.string.four_business_days);
                    break;
                case "One-Two weeks":
                    shippingTime = getString(R.string.one_two_week_business);
                    break;
                case "Two-Three weeks":
                    shippingTime = getString(R.string.two_three_week_business);
                    break;
                case "Three-Four weeks":
                    shippingTime = getString(R.string.three_four_week_business);
                    break;
                case "Four-Six weeks":
                    shippingTime = getString(R.string.four_six_week_business);
                    break;
                case "Six-Eight weeks":
                    shippingTime = getString(R.string.six_eight_week_business);
                    break;
                default:
                    shippingTime = tempMap.get(Constants.TAG_SHIPPING_TIME);
                    break;
            }
            shipping.setText(shippingTime);

            if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") &&
                    !tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("0") && !tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("") &&
                    FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                discountPrice.setVisibility(View.VISIBLE);
                discountPercent.setVisibility(View.VISIBLE);
                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                String costValue = tempMap.get(Constants.TAG_PRICE);
                float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                        * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + df.format(priceValue));
                discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                discountPercent.setText(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
            } else {
                discountPrice.setVisibility(View.GONE);
                discountPercent.setVisibility(View.GONE);
                itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            }
            likedCount = getLikedCountFromLocal(tempMap);
            likedBtn.setChecked(helper.getItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKED).equalsIgnoreCase(Constants.TAG_YES));

            setLikeCount(String.valueOf(likedCount), likeCount);
            if (tempMap.get(Constants.TAG_COD).equalsIgnoreCase("yes")) {
                codText.setVisibility(View.VISIBLE);
                codImage.setVisibility(View.VISIBLE);
                codText.setText(getString(R.string.cod_available));
                codText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                codImage.setImageResource(R.drawable.tick_color);
                codImage.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                codText.setVisibility(View.GONE);
                codImage.setVisibility(View.GONE);
            }

            try {
                int qty = Integer.parseInt(tempMap.get(Constants.TAG_QUANTITY));
                if (qty > 0 && qty < 10) {
                    availableQuantity.setVisibility(View.VISIBLE);
                    availableQuantity.setText(getString(R.string.only_qty_available, qty));
                } else {
                    availableQuantity.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                availableQuantity.setVisibility(View.GONE);
            }

            ArrayList<HashMap<String, String>> photoAry = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> photoMap = new HashMap<String, String>();
            photoMap.put(Constants.TAG_IMAGE, tempMap.get(Constants.TAG_IMAGE));
            photoMap.put(Constants.TAG_HEIGHT, tempMap.get(Constants.TAG_HEIGHT));
            photoMap.put(Constants.TAG_WIDTH, tempMap.get(Constants.TAG_WIDTH));
            photoMap.put("type", "image");
            photoAry.add(photoMap);

            imagePager.getLayoutParams().height = imageHeight;

            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(DetailActivity.this, photoAry);
            imagePager.setAdapter(imagePagerAdapter);

            //getItemDetails(position);

            shareit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);

                    if (accesstoken != null) {
                        ArrayList<HashMap<String, String>> photosAry = getPhotos(tempMap.get(Constants.TAG_PHOTOS));
                        String image = photosAry.size()>0?photosAry.get(0).get(Constants.TAG_IMAGE):"";
                        setupFacebookShareIntent(image);
                    } else {
                        Intent login = new Intent(context, LoginActivity.class);
                        startActivity(login);
                    }
                }
            });

            viewAllComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, AllComments.class);
                    i.putExtra("from", "itemDetail");
                    i.putExtra("itemId", tempMap.get(Constants.TAG_ID));
                    i.putExtra("image", tempMap.get(Constants.TAG_IMAGE));
                    i.putExtra("itemTitle", tempMap.get(Constants.TAG_ITEM_TITLE));
                    startActivityForResult(i, 3);
                }
            });

            writeComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);

                    if (accesstoken != null) {
                        Intent i = new Intent(DetailActivity.this, WriteComment.class);
                        i.putExtra("from", "itemDetail");
                        i.putExtra("to", "add");
                        i.putExtra("itemId", tempMap.get(Constants.TAG_ID));
                        i.putExtra("image", tempMap.get(Constants.TAG_IMAGE));
                        i.putExtra("itemTitle", tempMap.get(Constants.TAG_ITEM_TITLE));
                        startActivityForResult(i, 3);
                    } else {
                        Intent login = new Intent(context, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            groupGiftLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);

                    if (accesstoken != null) {
                        View view = mViewPager.findViewWithTag("posi" + mViewPager.getCurrentItem());
                        TextView buyNow = (TextView) view.findViewById(R.id.buyNow);
                        RecyclerView sizeList = (RecyclerView) view.findViewById(R.id.sizeList);
                        LinearLayout sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);

                        if (!buyNow.getText().toString().equals(getString(R.string.sold_out))) {
                            Intent i = new Intent(DetailActivity.this, CreateGroupGift.class);
                            i.putExtra("itemId", tempMap.get(Constants.TAG_ID));
                            i.putExtra("image", tempMap.get(Constants.TAG_IMAGE));
                            i.putExtra("itemTitle", tempMap.get(Constants.TAG_ITEM_TITLE));

                            if (sizeLay.getVisibility() == View.VISIBLE) {
                                SizeViewAdapter sizeViewAdapter = (SizeViewAdapter) sizeList.getAdapter();
                                HashMap<String, String> sizeMap = sizeViewAdapter.getSizeDetails(selectedSizePosition);
                                String selectedSize = sizeMap.get(Constants.TAG_NAME);
                                i.putExtra("size", selectedSize);
                            } else
                                i.putExtra("size", "");
                            startActivity(i);
                        } else {
                            FantacyApplication.showToast(DetailActivity.this, getString(R.string.product_soldout), Toast.LENGTH_SHORT);

                        }
                    } else {
                        Intent login = new Intent(context, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            likedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken != null) {
                        String status = likedBtn.isChecked() ? Constants.TAG_YES : Constants.TAG_NO;
                        likedCount = getLikedCountFromLocal(itemsAry.get(mViewPager.getCurrentItem()));
                        itemsAry.get(mViewPager.getCurrentItem()).put(Constants.TAG_LIKED, status);
                        if (likedBtn.isChecked()) {
                            likedCount = likedCount + 1;
                        } else {
                            likedCount = likedCount - 1;
                        }
                        likeItem(mViewPager.getCurrentItem(), status);
                        setLikeCount(String.valueOf(likedCount), likeCount);
                        Log.d(TAG, "likeClick:  " +likedCount);
                        helper.updateItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKED, status);
                        helper.updateItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT, String.valueOf(likedCount));
                    } else {
                        likedBtn.setChecked(false);
                        Intent login = new Intent(context, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            shopImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, StoreProfile.class);
                    i.putExtra("storeId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_SHOP_ID));
                    startActivity(i);
                }
            });
            shopName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, StoreProfile.class);
                    i.putExtra("storeId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_SHOP_ID));
                    startActivity(i);
                }
            });

            addCollectionLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                    String accesstoken = preferences.getString("TOKEN", null);
                    if (accesstoken != null) {
                        addCollection();
                    } else {
                        Intent login = new Intent(context, SignInActivity.class);
                        startActivity(login);
                    }
                }
            });

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        private int getLikedCountFromLocal(HashMap<String, String> tempMap) {
            if (!helper.getItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT).equals(""))
                likedCount = Integer.parseInt(helper.getItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT));
            else
                likedCount = 0;
            return likedCount;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            ((ViewPager) collection).removeView((ViewGroup) view);
            Log.e("positionDetails"," - deleted Pos "+position);
            FantacyApplication.getInstance().getRequestQueue().cancelAll("pos" + position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        private void likeItem(final int position, final String check) {
            SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEMLIKE, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        JSONObject json = new JSONObject(res);
                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            Log.i(TAG, "likeItemRes: " + res);
                        } else {
                            resetLikeCount(check);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    resetLikeCount(check);
                    Log.e(TAG, "likeItemError: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", customerId);
                    map.put("item_id", itemsAry.get(position).get(Constants.TAG_ID));
                    Log.i(TAG, "likeItemParams: " + map);
                    return map;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + accesstoken);
                    Log.d(TAG, "getHeaders: " + accesstoken);
                    return headers;
                }

            };
            FantacyApplication.getInstance().addToRequestQueue(req);
        }

    }

    public void setLikeCount(String likedCount, TextView likeCount) {
        if (likedCount.equals("1"))
            likeCount.setText(likedCount + " " + getString(R.string.like));
        else
            likeCount.setText(likedCount + " " + getString(R.string.likes));
    }

    public void resetLikeCount(String check) {
        View itemView = mViewPager.findViewWithTag("posi" + position);
        final CheckBox likedBtn = (CheckBox) itemView.findViewById(R.id.likedBtn);
        final TextView likeCount = (TextView) itemView.findViewById(R.id.productLikeCount);

        if (check.equals("yes")) {
            itemsAry.get(position).put(Constants.TAG_LIKED, Constants.TAG_NO);
            likedBtn.setChecked(false);
            likedCount = likedCount - 1;
        } else {
            itemsAry.get(position).put(Constants.TAG_LIKED, "yes");
            likedBtn.setChecked(true);
            likedCount = likedCount + 1;
        }
        helper.updateItemDetails(itemsAry.get(position).get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT, String.valueOf(likedCount));
        helper.updateItemDetails(itemsAry.get(position).get(Constants.TAG_ID), Constants.TAG_LIKED, itemsAry.get(position).get(Constants.TAG_LIKED));
        setLikeCount("" + likedCount, likeCount);
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    class ImagePagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> imageAry;

        public ImagePagerAdapter(Context act, ArrayList<HashMap<String, String>> newary) {
            this.imageAry = newary;
            this.context = act;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return imageAry.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            final View itemView = inflater.inflate(R.layout.product_detail_image, collection, false);

            itemView.setTag("pos" + position);

            final ImageView image = (ImageView) itemView.findViewById(R.id.image);

            final HashMap<String, String> tempMap = imageAry.get(position);
            String imageUrl = tempMap.get(Constants.TAG_IMAGE);
            if (imageUrl != null && !imageUrl.equals("")) {
                Picasso.get().load(imageUrl).into(image);
            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DetailActivity.this, FullImage.class);
                    intent.putExtra("from", "detail");
                    intent.putExtra("position", position);
                    intent.putExtra("images", imageAry);
                    Pair<View, String> bodyPair = Pair.create(view, "detail");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(DetailActivity.this, bodyPair);
                    ActivityCompat.startActivity(DetailActivity.this, intent, options.toBundle());
                }
            });

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    public class SizeViewAdapter extends RecyclerView.Adapter<SizeViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> sizes;
        Context context;
        int pos;
        HashMap<String, String> itemMap;

        public SizeViewAdapter(Context context, ArrayList<HashMap<String, String>> sizes, int pos, HashMap<String, String> itemDetails) {
            this.sizes = sizes;
            this.context = context;
            this.pos = pos;
            this.itemMap = itemDetails;
        }

        public HashMap<String, String> getSizeDetails(int position) {
            return sizes.get(position);
        }

        @Override
        public SizeViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.size_list_items, parent, false);

            return new SizeViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SizeViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = sizes.get(position);

            holder.size.setText(tempMap.get(Constants.TAG_NAME));
            if (selectedSizePosition == position) {
                holder.size.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                holder.sizeLay.setBackground(ContextCompat.getDrawable(context, R.drawable.primary_color_sharp_corner));
                View view = mViewPager.findViewWithTag("posi" + pos);
                TextView itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                TextView addCart = (TextView) view.findViewById(R.id.addCart);
                TextView buyNow = (TextView) view.findViewById(R.id.buyNow);
                TextView discountPrice = (TextView) view.findViewById(R.id.discountPrice);
                TextView discountPercent = (TextView) view.findViewById(R.id.discountPercent);
                TextView availableQuantity = (TextView) view.findViewById(R.id.availableQuantity);

                if (itemMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") &&
                        !itemMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("0") && !itemMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("")) {
                    discountPrice.setVisibility(View.VISIBLE);
                    discountPercent.setVisibility(View.VISIBLE);
                    discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    String costValue = tempMap.get(Constants.TAG_PRICE);
                    float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                            * ((Float.parseFloat(itemMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                    itemPrice.setText(itemMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                    discountPrice.setText(itemMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                    discountPercent.setText(itemMap.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
                } else {
                    discountPrice.setVisibility(View.GONE);
                    discountPercent.setVisibility(View.GONE);

                    itemPrice.setText(itemMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                }

                if (tempMap.get(Constants.TAG_QTY).equals("0") || tempMap.get(Constants.TAG_QTY).equals("")) {
                    addCart.setVisibility(View.GONE);
                    buyNow.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    buyNow.setText(getString(R.string.sold_out));
                    availableQuantity.setVisibility(View.GONE);
                } else {
                    availableQuantity.setVisibility(View.VISIBLE);
                    int qty = Integer.parseInt(tempMap.get(Constants.TAG_QTY));
                    availableQuantity.setText(getString(R.string.only_qty_available, qty));
                    addCart.setVisibility(View.VISIBLE);
                    buyNow.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    buyNow.setText(getString(R.string.buy_now));
                }
            } else {
                holder.sizeLay.setBackground(ContextCompat.getDrawable(context, R.drawable.divider_text_sharp_corner));
                holder.size.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
            }
        }

        @Override
        public int getItemCount() {
            return sizes.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView size;
            LinearLayout sizeLay;

            public MyViewHolder(View view) {
                super(view);

                size = (TextView) view.findViewById(R.id.size);
                sizeLay = (LinearLayout) view.findViewById(R.id.sizeLay);

                sizeLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sizeLay:
                        selectedSizePosition = getAdapterPosition();
                        notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public class ItemViewAdapter extends RecyclerView.Adapter<ItemViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String from = "";

        public ItemViewAdapter(String from, Context context, ArrayList<HashMap<String, String>> Items) {
            this.from = from;
            this.Items = Items;
            this.context = context;
        }

        public ArrayList<HashMap<String, String>> getItems() {
            return Items;
        }

        @Override
        public ItemViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.collections_item_image, parent, false);

            return new ItemViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ItemViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.storeImage.getLayoutParams().width = selfiWidth;
            holder.storeImage.getLayoutParams().height = selfiWidth;

            String image = "";
            if (from.equals("item")) {
                image = tempMap.get(Constants.TAG_IMAGE);
            } else {
                image = tempMap.get(Constants.TAG_IMAGE_ORG);
            }
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.storeImage);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            CornerImageView storeImage;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);
                storeImage = (CornerImageView) view.findViewById(R.id.image);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
            }
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        public ArrayList<HashMap<String, String>> getItems() {
            return Items;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_items, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
            holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));

            holder.mainLay.getLayoutParams().width = itemWidth;
            holder.itemImage.getLayoutParams().height = itemWidth;
            holder.itemImage.getLayoutParams().width = itemWidth;

            String image = tempMap.get(Constants.TAG_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.itemImage);
            }

            if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                holder.discountPrice.setVisibility(View.VISIBLE);
                String costValue = tempMap.get(Constants.TAG_PRICE);
                float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                        * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                holder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            } else {
                holder.discountPrice.setVisibility(View.GONE);
                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView itemName, itemPrice, discountPrice;
            ImageView itemImage;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

    public class CommentsViewAdapter extends RecyclerView.Adapter<CommentsViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public CommentsViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public CommentsViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comments_list_item, parent, false);

            return new CommentsViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CommentsViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.userName.setText(tempMap.get(Constants.TAG_FULL_NAME));
            holder.comment.setText(FantacyApplication.stripHtml(tempMap.get(Constants.TAG_COMMENT)).trim());
            String image = tempMap.get(Constants.TAG_USER_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.userImage);
            }

            if (tempMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                holder.edit.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.edit.setVisibility(View.GONE);
                holder.delete.setVisibility(View.GONE);
            }

            MovementMethod m = holder.comment.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (holder.comment.getLinksClickable()) {
                    holder.comment.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            holder.comment.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {

                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                        getProfile(clickedString);
                    }

                    if (clickedString.contains("#")) {
                        Intent i = new Intent(context, HashTag.class);
                        i.putExtra("key", clickedString.replace("#", ""));
                        startActivity(i);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage;
            TextView userName, edit, delete;
            LinkEllipseTextView comment;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                userName = (TextView) view.findViewById(R.id.userName);
                comment = (LinkEllipseTextView) view.findViewById(R.id.comment);
                edit = (TextView) view.findViewById(R.id.edit);
                delete = (TextView) view.findViewById(R.id.delete);

                edit.setOnClickListener(this);
                delete.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.edit:
                        Intent i = new Intent(DetailActivity.this, WriteComment.class);
                        i.putExtra("from", "itemDetail");
                        i.putExtra("to", "edit");
                        i.putExtra("commentId", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT_ID));
                        i.putExtra("comment", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT));
                        i.putExtra("itemId", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ID));
                        i.putExtra("image", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_IMAGE));
                        i.putExtra("itemTitle", itemsAry.get(mViewPager.getCurrentItem()).get(Constants.TAG_ITEM_TITLE));
                        startActivityForResult(i, 3);
                        break;
                    case R.id.delete:
                        deleteDialog(Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT_ID));
                        break;
                    case R.id.userImage:
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        startActivity(p);
                        break;
                }
            }
        }
    }

    public class CollectionViewAdapter extends RecyclerView.Adapter<CollectionViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public CollectionViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public CollectionViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.collection_add_list_item, parent, false);
            return new CollectionViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CollectionViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.name.setText(tempMap.get(Constants.TAG_COLLECTION_NAME));
            if (tempMap.get(Constants.TAG_CHECKED).equals("0")) {
                holder.checkBox.setChecked(false);
            } else {
                holder.checkBox.setChecked(true);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            CheckBox checkBox;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                mainLay.setOnClickListener(this);
                checkBox.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        if (Items.get(getAdapterPosition()).get(Constants.TAG_CHECKED).equals("0")) {
                            updateCollectionItems(getAdapterPosition(), "1");
                            Items.get(getAdapterPosition()).put(Constants.TAG_CHECKED, "1");
                        } else {
                            updateCollectionItems(getAdapterPosition(), "0");
                            Items.get(getAdapterPosition()).put(Constants.TAG_CHECKED, "0");
                        }
                        notifyDataSetChanged();
                        break;
                    case R.id.checkBox:
                        if (Items.get(getAdapterPosition()).get(Constants.TAG_CHECKED).equals("0")) {
                            updateCollectionItems(getAdapterPosition(), "1");
                            Items.get(getAdapterPosition()).put(Constants.TAG_CHECKED, "1");
                        } else {
                            updateCollectionItems(getAdapterPosition(), "0");
                            Items.get(getAdapterPosition()).put(Constants.TAG_CHECKED, "0");
                        }
                        break;
                }
            }
        }
    }


    public class QuestionViewAdapter extends RecyclerView.Adapter<QuestionViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String itemId, itemImage, buyertype;

        public QuestionViewAdapter(Context context, String buyertype, ArrayList<HashMap<String, String>> Items, String itemId, String itemImage) {
            this.Items = Items;
            this.context = context;
            this.itemId = itemId;
            this.itemImage = itemImage;
            this.buyertype = buyertype;
        }

        @Override
        public QuestionViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.question_item, parent, false);
            return new QuestionViewAdapter.MyViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(final QuestionViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> map = Items.get(position);

            String qstr = map.get(Constants.TAG_QUESTION);
            String astr = map.get(Constants.TAG_ANSWER);
            Log.e("qstring","-orignail "+qstr+" ans" +astr);
//            String qstrremovespace = qstr.replace("\n", "");
//            String qstrFinal = html2text(qstrremovespace);
//
//            String ansremovespace = qstr.replace("\n", "").replaceAll("\\<[^>]*>","").replace("\r", "");
//            String ansFinal = html2text(ansremovespace);


            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.question.setText("Q: "+Html.fromHtml(qstr.trim(),Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.question.setText("Q: "+Html.fromHtml(qstr.trim()));
            }*/
            holder.question.setText("Q: "+(new HtmlSpanner()).fromHtml(qstr));
            holder.question.setMovementMethod(LinkMovementMethod.getInstance());
            stripUnderlines(holder.question);


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                holder.answer.setText("A: "+Html.fromHtml(astr,Html.FROM_HTML_MODE_LEGACY));
//            } else {
//                holder.answer.setText("A: "+Html.fromHtml(astr));
//            }
            holder.answer.setText("A: "+(new HtmlSpanner()).fromHtml(astr));
            holder.answer.setMovementMethod(LinkMovementMethod.getInstance());
            stripUnderlines(holder.answer);



            if(map.get(Constants.TAG_ANSWER).equalsIgnoreCase("")) {
                holder.answer.setVisibility(View.GONE);
            }
            else {
                holder.answer.setVisibility(View.VISIBLE);
            }

            //if(map.get(Constants.TAG_USER_ID).equalsIgnoreCase(GetSet.getUserId())){
                holder.writeanswer.setVisibility(View.GONE);
            //}else holder.writeanswer.setVisibility(View.VISIBLE);

            if(!map.get(Constants.TAG_ANSWER_COUNT).equalsIgnoreCase("")){
                int count = Integer.parseInt(map.get(Constants.TAG_ANSWER_COUNT));
                if(count>1)
                    holder.readother.setVisibility(View.VISIBLE);
                else holder.readother.setVisibility(View.GONE);
            }else holder.readother.setVisibility(View.GONE);


        }


        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView writeanswer, question, answer, readother;
            LinearLayout parentLay;

            public MyViewHolder(View view) {
                super(view);

                writeanswer = (TextView) view.findViewById(R.id.writeanswer);
                answer = (TextView) view.findViewById(R.id.answer);
                question = (TextView) view.findViewById(R.id.question);
                readother = (TextView) view.findViewById(R.id.readother);
                parentLay = (LinearLayout) view.findViewById(R.id.parentlay);
                writeanswer.setOnClickListener(this);
                parentLay.setOnClickListener(this);
                question.setOnClickListener(this);
                answer.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.writeanswer:
                        open(itemId, Items.get(getAdapterPosition()).get(Constants.TAG_QUESTION),""+Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                        break;
                    case R.id.parentlay:
                        openAnswers(buyertype,Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID), itemId, Items.get(getAdapterPosition()).get(Constants.TAG_ID), itemImage, Items.get(getAdapterPosition()).get(Constants.TAG_QUESTION));
                        break;
                    case R.id.question:
                        openAnswers(buyertype,Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID), itemId, Items.get(getAdapterPosition()).get(Constants.TAG_ID), itemImage, Items.get(getAdapterPosition()).get(Constants.TAG_QUESTION));
                        break;
                    case R.id.answer:
                        openAnswers(buyertype,Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID), itemId, Items.get(getAdapterPosition()).get(Constants.TAG_ID), itemImage, Items.get(getAdapterPosition()).get(Constants.TAG_QUESTION));
                        break;

                    }

            }
        }
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }


    public void openAnswers(String buyertype,String userid,String itemId, String questionId, String itemImage, String QuestionStr) {
    Intent intent = new Intent(DetailActivity.this,OtherAnswers.class);
        intent.putExtra("userId",userid);
    intent.putExtra("itemId",itemId);
        intent.putExtra("question_id",questionId);
    intent.putExtra("itemTitle",QuestionStr);
        intent.putExtra("image",itemImage);
        intent.putExtra("buyertype",buyertype);
    startActivity(intent);
    }

    Dialog dialog;

    public void open(final String ItemId, final String quesStr, final String quesiontId) {
        // custom dialog
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.answer_dialog);

        TextView question = (TextView)dialog.findViewById(R.id.question);
        final EditText answerEdit = (EditText)dialog.findViewById(R.id.answeredit);
        TextView oktxt = (TextView)dialog.findViewById(R.id.ok);
        TextView canceltxt = (TextView)dialog.findViewById(R.id.cancel);

        question.setText(quesStr);

        oktxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                writeAnswer(ItemId, quesiontId, answerEdit.getText().toString());

            }
        });

        canceltxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String itemId, itemImage;

        public ReviewAdapter(Context context, ArrayList<HashMap<String, String>> items) {
            this.Items = items;
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final HashMap<String, String> map = Items.get(position);

            holder.user_name.setText(""+map.get(Constants.TAG_USER_NAME));

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.reviewtxt.setText(""+Html.fromHtml(map.get(Constants.TAG_REVIEW),Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.reviewtxt.setText(""+Html.fromHtml(map.get(Constants.TAG_REVIEW)));
            }
*/
            holder.reviewtxt.setText((new HtmlSpanner()).fromHtml(map.get(Constants.TAG_REVIEW)));
            holder.reviewtxt.setMovementMethod(LinkMovementMethod.getInstance());
            stripUnderlines(holder.reviewtxt);

            if(map.get(Constants.TAG_REVIEW).equalsIgnoreCase(""))
                holder.reviewtxt.setVisibility(View.GONE);
            else holder.reviewtxt.setVisibility(View.VISIBLE);

            holder.ratingBar.setRating(Float.parseFloat(map.get(Constants.TAG_RATING)));
            LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable().getCurrent();
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.divider), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_ATOP);

            String imageUrl = map.get(Constants.TAG_USER_IMAGE);
            if (!imageUrl.equals("")) {
                Picasso.get().load(imageUrl).into(holder.image);
            }

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent p = new Intent(context, Profile.class);
                    p.putExtra("userId", map.get(Constants.TAG_USER_ID));
                    startActivity(p);
                }
            });

        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView user_name, reviewtxt;
            RatingBar ratingBar;
            RelativeLayout parentLay;
            ImageView image;

            public MyViewHolder(View view) {
                super(view);

                user_name = (TextView) view.findViewById(R.id.user_name);
                reviewtxt = (TextView) view.findViewById(R.id.reviewtxt);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                image = (ImageView) view.findViewById(R.id.image);
                parentLay = (RelativeLayout) view.findViewById(R.id.parentlay);
                //parentLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.writeanswer:

                        break;
                    case R.id.parentlay:
                        break;

                }

            }
        }
    }


    private void writeAnswer(final String itemId, final String parentId, final String content) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADDPRODUCTFAQ, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG ,"addproductfaq="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        finish();
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_SHORT);
                    } else {
                        FantacyApplication.showToast(DetailActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "sendChatError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemId);
                map.put("content", content);
                map.put("type", "answer");
                map.put("parent_id", parentId);
                Log.v(TAG, "sendChatParams="+map);
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void getProfile(final String clickedString) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "getProfileRes=" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Intent p = new Intent(getApplicationContext(), Profile.class);
                        p.putExtra("userId", "");
                        p.putExtra(Constants.TAG_USERNAME_URL, clickedString.replace("@", ""));
                        startActivity(p);
                    } else if (status.equalsIgnoreCase("false")) {
                        invalidUsrDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getProfileError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_USERNAME_URL, clickedString.replace("@", ""));
                if (GetSet.isLogged()) {
                    map.put("logged_user_id", customerId);
                }
                Log.v(TAG, "getProfileParams=" + map);
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    /*
     * dialog for invalid user
     * */
    public void invalidUsrDialog() {
        final Dialog dialog = new Dialog(DetailActivity.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);
        no.setVisibility(View.GONE);
        title.setText(getString(R.string.user_not_found));
        yes.setText(getString(R.string.ok));
        //no.setText(getString(R.string.no));
        //no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

}
