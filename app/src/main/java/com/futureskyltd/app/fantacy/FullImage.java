package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.futureskyltd.app.external.TouchImageView;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 24/5/17.
 */

public class FullImage extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    String from = "";
    int position;
    ViewPager imagePager;
    ImageView cancelBtn;
    ImagePagerAdapter imagePagerAdapter;
    ArrayList<HashMap<String, String>> imagesAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image_layout);

        imagePager = (ViewPager) findViewById(R.id.imagePager);
        cancelBtn = (ImageView) findViewById(R.id.cancelBtn);

        from = getIntent().getExtras().getString("from");
        position = getIntent().getExtras().getInt("position");
        imagesAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("images");

        ViewCompat.setTransitionName(imagePager, from);

        imagePagerAdapter = new ImagePagerAdapter(FullImage.this, imagesAry);
        imagePager.setAdapter(imagePagerAdapter);
        imagePager.setCurrentItem(position);
        cancelBtn.setOnClickListener(this);


        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.v("isConnected", "isConnected=" + isConnected);
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelBtn:
                finish();
                break;
        }
    }

    class ImagePagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> images;

        public ImagePagerAdapter(Context act, ArrayList<HashMap<String, String>> temp) {
            this.images = temp;
            this.context = act;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.full_image,
                    collection, false);

            itemView.setTag("posi" + position);

            TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.imageView);
            String imageUrl = images.get(position).get(Constants.TAG_IMAGE);
            if (imageUrl != null && !imageUrl.equals("")) {
                Picasso.get().load(imageUrl).into(imageView);
            }

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            ((ViewPager) collection).removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
