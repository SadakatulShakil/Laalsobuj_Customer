package com.futureskyltd.app.fantacy;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.ImagePicker;
import com.futureskyltd.app.helper.ImageCompression;
import com.futureskyltd.app.helper.ImageStorage;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 19/7/17.
 */

public class OrderDetail extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    public static ProgressDialog pd;
    public static boolean imageUploading = false;
    ImageView backBtn;
    Dialog dialog;
    TextView title, orderId, orderOn, deliverOn, deliveryType, addressText, address, paymentMode, itemPrice, shippingPrice,
            taxPrice, grandTotal, orderstatus, trackingDetails, discountprice, claimOrder, dialogtitle, yes, no;
    String pdfName, orderID, uploadItemID, firstItemId, firstItemTitle;
    LinearLayout discountpricelay, statusLayout;
    RecyclerView recyclerView;
    RelativeLayout progressLay, trackingLay;
    RecyclerViewAdapter recyclerViewAdapter;
    LinearLayoutManager linearLayoutManager;
    HashMap<String, String> dataMap = new HashMap<>();
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<>();
    NestedScrollView scrollView;
    String orderStatus = "";
    RelativeLayout paymentModelayout;
    private boolean isDisputeEnabled;
    Context context = null;

    /*Assign the current context for printer manager. Then only it will work*/
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Constants.isNotification){
            Intent i = new Intent(this,FragmentMainActivity.class);
            startActivity(i);
        }
        Constants.isNotification=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.isNotification =false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        orderId = (TextView) findViewById(R.id.orderId);
        orderOn = (TextView) findViewById(R.id.orderOn);
        deliverOn = (TextView) findViewById(R.id.deliverOn);
        deliveryType = (TextView) findViewById(R.id.deliveryType);
        addressText = (TextView) findViewById(R.id.addressText);
        address = (TextView) findViewById(R.id.address);
        paymentMode = (TextView) findViewById(R.id.paymentMode);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        shippingPrice = (TextView) findViewById(R.id.shippingPrice);
        taxPrice = (TextView) findViewById(R.id.taxPrice);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        orderstatus = (TextView) findViewById(R.id.order_status);
        discountprice = (TextView) findViewById(R.id.discountprice);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        discountpricelay = (LinearLayout) findViewById(R.id.discountpricelay);
        trackingLay = (RelativeLayout) findViewById(R.id.trackingLay);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        trackingDetails = findViewById(R.id.trackingDetails);
        claimOrder = findViewById(R.id.claimOrder);
        statusLayout = findViewById(R.id.statusLayout);
        paymentModelayout = findViewById(R.id.paymentModelayout);
        title.setText(getString(R.string.order_details));
        dialog = new Dialog(OrderDetail.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        dialogtitle = (TextView) dialog.findViewById(R.id.title);
        yes = (TextView) dialog.findViewById(R.id.yes);
        no = (TextView) dialog.findViewById(R.id.no);


        yes.setText(getString(R.string.yes));
        no.setText(getString(R.string.no));

        no.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        orderID = (String) getIntent().getExtras().get("orderId");
        backBtn.setOnClickListener(this);
        orderstatus.setOnClickListener(this);
        recyclerView.setNestedScrollingEnabled(false);
        trackingDetails.setOnClickListener(this);
        claimOrder.setOnClickListener(this);
        scrollView.setVisibility(View.GONE);
        progressLay.setVisibility(View.VISIBLE);

        pd = new ProgressDialog(OrderDetail.this);
        pd.setMessage(getString(R.string.pleasewait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this,
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(this, itemsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        if (FantacyApplication.isRTL(OrderDetail.this)) {
            address.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            address.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getData() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ORDER_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.d(TAG, "getDataRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String order_id = DefensiveClass.optString(result, Constants.TAG_ORDER_ID);
                        String order_status = DefensiveClass.optString(result, Constants.TAG_STATUS);
                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String grand_total = DefensiveClass.optString(result, Constants.TAG_GRAND_TOTAL);
                        String item_total = DefensiveClass.optString(result, Constants.TAG_ITEM_TOTAL);
                        String shipping_price;
                        orderStatus = order_status;
                        if (Objects.equals(DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE), ""))
                            shipping_price = getString(R.string.free_text);
                        else
                            shipping_price = DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE);
                        String tax = DefensiveClass.optString(result, Constants.TAG_TAX);
                        String coupon_discount = DefensiveClass.optString(result, Constants.TAG_COUPON_DISCOUNT);
                        String credit_used = DefensiveClass.optString(result, Constants.TAG_CREDIT_USED);
                        String gift_amount = DefensiveClass.optString(result, Constants.TAG_GIFT_AMOUNT);
                        String sale_date = DefensiveClass.optString(result, Constants.TAG_SALE_DATE);
                        String payment_mode = DefensiveClass.optString(result, Constants.TAG_PAYMENT_MODE);
                        String delivery_type = DefensiveClass.optString(result, Constants.TAG_DELIVERY_TYPE);
                        String store_id = DefensiveClass.optString(result, Constants.TAG_STORE_ID);
                        String store_name = DefensiveClass.optString(result, Constants.TAG_STORE_NAME);
                        String store_image = DefensiveClass.optString(result, Constants.TAG_STORE_IMAGE);
                        String store_address = DefensiveClass.optString(result, Constants.TAG_STORE_ADDRESS);
                        String barcode = DefensiveClass.optString(result, Constants.TAG_BARCODE);
                        String barcode_img = DefensiveClass.optString(result, Constants.TAG_BARCODE_IMG);
                        String review_id = DefensiveClass.optString(result, Constants.TAG_REVIEW_ID);
                        String rating = DefensiveClass.optString(result, Constants.TAG_RATING);
                        String review_title = DefensiveClass.optString(result, Constants.TAG_REVIEW_TITLE);
                        String review_des = DefensiveClass.optString(result, Constants.TAG_REVIEW_DES);
                        String dispute_created = DefensiveClass.optString(result, Constants.TAG_DISPUTE_CREATED);
                        String dispute_id = DefensiveClass.optString(result, Constants.TAG_DISPUTE_ID);
                        String dispute_status = DefensiveClass.optString(result, Constants.TAG_DISPUTE_STATUS);

                        JSONObject expected_delivery = result.getJSONObject(Constants.TAG_EXPECTED_DELIVERY);
                        String from = DefensiveClass.optString(expected_delivery, Constants.TAG_FROM);
                        String to = DefensiveClass.optString(expected_delivery, Constants.TAG_TO);

                        JSONObject shipping = result.getJSONObject(Constants.TAG_SHIPPING);
                        String shippingid = DefensiveClass.optInt(shipping, Constants.TAG_SHIPPING_ID);
                        String nickname = DefensiveClass.optString(shipping, Constants.TAG_NICK_NAME);
                        String fullname = DefensiveClass.optString(shipping, Constants.TAG_FULL_NAME);
                        String country = DefensiveClass.optString(shipping, Constants.TAG_COUNTRY);
                        String state = DefensiveClass.optString(shipping, Constants.TAG_STATE);
                        String address1 = DefensiveClass.optString(shipping, Constants.TAG_ADDRESS1);
                        String address2 = DefensiveClass.optString(shipping, Constants.TAG_ADDRESS2);
                        String city = DefensiveClass.optString(shipping, Constants.TAG_CITY);
                        String zipcode = DefensiveClass.optString(shipping, Constants.TAG_ZIPCODE);
                        String phone = DefensiveClass.optString(shipping, Constants.TAG_PHONE);

                        JSONObject tracking_details = result.getJSONObject(Constants.TAG_TRACKING_DETAILS);
                        String id = DefensiveClass.optString(tracking_details, Constants.TAG_ID);
                        String shipping_date = DefensiveClass.optString(tracking_details, Constants.TAG_SHIPPING_DATE);
                        String courier_name = DefensiveClass.optString(tracking_details, Constants.TAG_COURIER_NAME);
                        String courier_service = DefensiveClass.optString(tracking_details, Constants.TAG_COURIER_SERVICE);
                        String tracking_id = DefensiveClass.optString(tracking_details, Constants.TAG_TRACKING_ID);
                        String notes = DefensiveClass.optString(tracking_details, Constants.TAG_NOTES);

                        dataMap.put(Constants.TAG_ORDER_ID, order_id);
                        dataMap.put(Constants.TAG_STATUS, order_status);
                        dataMap.put(Constants.TAG_CURRENCY, currency);
                        dataMap.put(Constants.TAG_GRAND_TOTAL, grand_total);
                        dataMap.put(Constants.TAG_ITEM_TOTAL, item_total);
                        dataMap.put(Constants.TAG_SHIPPING_PRICE, shipping_price);
                        dataMap.put(Constants.TAG_TAX, tax);
                        dataMap.put(Constants.TAG_COUPON_DISCOUNT, coupon_discount);
                        dataMap.put(Constants.TAG_CREDIT_USED, credit_used);
                        dataMap.put(Constants.TAG_GIFT_AMOUNT, gift_amount);
                        dataMap.put(Constants.TAG_SALE_DATE, sale_date);
                        dataMap.put(Constants.TAG_PAYMENT_MODE, payment_mode);
                        dataMap.put(Constants.TAG_DELIVERY_TYPE, delivery_type);
                        dataMap.put(Constants.TAG_STORE_ID, store_id);
                        dataMap.put(Constants.TAG_STORE_NAME, store_name);
                        dataMap.put(Constants.TAG_STORE_IMAGE, store_image);
                        dataMap.put(Constants.TAG_STORE_ADDRESS, store_address);
                        dataMap.put(Constants.TAG_BARCODE, barcode);
                        dataMap.put(Constants.TAG_BARCODE_IMG, barcode_img);
                        dataMap.put(Constants.TAG_REVIEW_ID, review_id);
                        dataMap.put(Constants.TAG_RATING, rating);
                        dataMap.put(Constants.TAG_REVIEW_TITLE, review_title);
                        dataMap.put(Constants.TAG_REVIEW_DES, review_des);
                        dataMap.put(Constants.TAG_DISPUTE_CREATED, dispute_created);
                        dataMap.put(Constants.TAG_DISPUTE_ID, dispute_id);
                        dataMap.put(Constants.TAG_DISPUTE_STATUS, dispute_status);
                        dataMap.put(Constants.TAG_FROM, from);
                        dataMap.put(Constants.TAG_TO, to);
                        dataMap.put(Constants.TAG_SHIPPING_ID, shippingid);
                        dataMap.put(Constants.TAG_NICK_NAME, nickname);
                        dataMap.put(Constants.TAG_FULL_NAME, fullname);
                        dataMap.put(Constants.TAG_COUNTRY, country);
                        dataMap.put(Constants.TAG_STATE, state);
                        dataMap.put(Constants.TAG_ADDRESS1, address1);
                        dataMap.put(Constants.TAG_ADDRESS2, address2);
                        dataMap.put(Constants.TAG_CITY, city);
                        dataMap.put(Constants.TAG_ZIPCODE, zipcode);
                        dataMap.put(Constants.TAG_PHONE, phone);
                        dataMap.put(Constants.TAG_ID, id);
                        dataMap.put(Constants.TAG_SHIPPING_DATE, shipping_date);
                        dataMap.put(Constants.TAG_COURIER_NAME, courier_name);
                        dataMap.put(Constants.TAG_COURIER_SERVICE, courier_service);
                        dataMap.put(Constants.TAG_TRACKING_ID, tracking_id);
                        dataMap.put(Constants.TAG_NOTES, notes);
                        itemsAry.clear();
                        JSONArray items = result.getJSONArray(Constants.TAG_ITEMS);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject temp = items.getJSONObject(i);
                            firstItemId = DefensiveClass.optString(items.getJSONObject(0), (Constants.TAG_ITEM_ID));
                            firstItemTitle = DefensiveClass.optString(items.getJSONObject(0), (Constants.TAG_ITEM_NAME));
                            String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                            String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                            String item_skucode = DefensiveClass.optString(temp, Constants.TAG_ITEM_SKUCODE);
                            String quantity = DefensiveClass.optString(temp, Constants.TAG_QUANTITY);
                            String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                            String size = DefensiveClass.optString(temp, Constants.TAG_SIZE);
                            String deal_percentage = DefensiveClass.optString(temp, Constants.TAG_DEAL_PERCENTAGE);

                            HashMap<String, String> map = new HashMap<>();

                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_ITEM_IMAGE, item_image);
                            map.put(Constants.TAG_ITEM_NAME, item_name);
                            map.put(Constants.TAG_ITEM_SKUCODE, item_skucode);
                            map.put(Constants.TAG_QUANTITY, quantity);
                            map.put(Constants.TAG_PRICE, price);
                            map.put(Constants.TAG_SIZE, size);
                            map.put(Constants.TAG_DEAL_PERCENTAGE, deal_percentage);

                            itemsAry.add(map);
                        }
                    }
                    setData();
                    isDisputeEnabled = orderStatus.equals("Delivered");
                    recyclerViewAdapter.notifyDataSetChanged();

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
                Log.e(TAG, "getDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("order_id", orderID);
                Log.i(TAG, "getDataParams: " + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }


    private void setData() {
        scrollView.setVisibility(View.VISIBLE);
        progressLay.setVisibility(View.GONE);

        orderId.setText(dataMap.get(Constants.TAG_ORDER_ID));
        deliveryType.setText(dataMap.get(Constants.TAG_DELIVERY_TYPE));
        paymentMode.setText(dataMap.get(Constants.TAG_PAYMENT_MODE));
        itemPrice.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.formatPrice(dataMap.get(Constants.TAG_ITEM_TOTAL)));
        if (dataMap.get(Constants.TAG_SHIPPING_PRICE).equals(getString(R.string.free_text))) {
            shippingPrice.setText(dataMap.get(Constants.TAG_SHIPPING_PRICE));
        } else {
            shippingPrice.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.formatPrice(dataMap.get(Constants.TAG_SHIPPING_PRICE)));
        }
        if (dataMap.get(Constants.TAG_TAX).equals(""))
            taxPrice.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + "0");
        else
            taxPrice.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.formatPrice(dataMap.get(Constants.TAG_TAX)));


        grandTotal.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.formatPrice(dataMap.get(Constants.TAG_GRAND_TOTAL)));

        if (dataMap.get(Constants.TAG_DELIVERY_TYPE).equalsIgnoreCase("pickup")) {
            addressText.setText(getString(R.string.pickup_address));
            address.setText(dataMap.get(Constants.TAG_STORE_ADDRESS));
        } else {
            addressText.setText(getString(R.string.delivery_address));
            address.setText(dataMap.get(Constants.TAG_ADDRESS1) + ", " + dataMap.get(Constants.TAG_ADDRESS2) + ", \n" +
                    dataMap.get(Constants.TAG_CITY) + ", " + dataMap.get(Constants.TAG_STATE) + ", \n" + dataMap.get(Constants.TAG_COUNTRY) + ", \n\n" +
                    dataMap.get(Constants.TAG_PHONE));
        }

        if (!dataMap.get(Constants.TAG_COUPON_DISCOUNT).equals("0")) {
            discountpricelay.setVisibility(View.VISIBLE);
            discountprice.setText("-" + dataMap.get(Constants.TAG_CURRENCY) + dataMap.get(Constants.TAG_COUPON_DISCOUNT));
        } else {
            discountpricelay.setVisibility(View.GONE);
        }

        if (!dataMap.get(Constants.TAG_SALE_DATE).equals(null) && !dataMap.get(Constants.TAG_SALE_DATE).equals("")) {
            long date = Long.parseLong(dataMap.get(Constants.TAG_SALE_DATE)) * 1000;
            orderOn.setText(FantacyApplication.getDate(date));
        }

        if (!dataMap.get(Constants.TAG_TO).equals(null) && !dataMap.get(Constants.TAG_TO).equals("")) {
            long date = Long.parseLong(dataMap.get(Constants.TAG_TO)) * 1000;
            deliverOn.setText(FantacyApplication.getDate(date));
        }
        if (dataMap.get(Constants.TAG_TRACKING_ID) != null && !dataMap.get(Constants.TAG_TRACKING_ID).equals("")) {
            trackingLay.setVisibility(View.VISIBLE);
        }


        switch (dataMap.get(Constants.TAG_STATUS)) {
            case "Shipped":
                statusLayout.setVisibility(View.VISIBLE);
                orderstatus.setVisibility(View.VISIBLE);
                claimOrder.setVisibility(View.VISIBLE);
                orderstatus.setText(getString(R.string.delivered));
                break;
            case "Delivered":
            case "Paid":
                statusLayout.setVisibility(View.GONE);
                orderstatus.setVisibility(View.GONE);
                orderstatus.setText(getString(R.string.re_turn));
                break;
            case "Refunded":
                statusLayout.setVisibility(View.GONE);
                orderstatus.setVisibility(View.GONE);

                break;
            case "Processing":
            case "Pending":
                statusLayout.setVisibility(View.VISIBLE);
                claimOrder.setVisibility(View.GONE);
                orderstatus.setVisibility(View.VISIBLE);
                orderstatus.setText(getString(R.string.cancel));
                break;
            case "Returned":
            case "Cancelled":
            case "Claimed":
                statusLayout.setVisibility(View.GONE);
                break;
            default:
                orderstatus.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
                orderstatus.setText(getString(R.string.cancel));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("onActiviReview","result");
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(this);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage(timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.i("selectedImageFile", "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(OrderDetail.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        imageUploading = true;
                        new uploadImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            }
            else {
                FantacyApplication.showToast(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
            }
        }

    }

    private void createPdf() {

        String filePath = null;
        String root = Environment.getExternalStorageDirectory()
                .toString();
        File newDir = new File(root + "/Docs_"
                + getString(R.string.app_name));
        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        Random gen = new Random();
        int n = 10000;
        n = gen.nextInt(n);
        pdfName = "transaction-" + n + ".pdf";
        File file = new File(newDir, pdfName);
        filePath = file.getAbsolutePath();

        if (file.exists()) {
            file.delete();
            Log.v("file.exists", "file.exists");
        }
        try {

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            Rectangle rect = new Rectangle(30, 30, 550, 800);
            writer.setBoxSize("art", rect);

            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            addTitle(document);
            addContent(document);

            document.close();
            refreshGallery(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "filePath==" + filePath);
        File f = new File(filePath);
        Uri uri = Uri.fromFile(f);
        Log.v(TAG, "Uri==" + uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printmanager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
            String name = "CheckPrint";
            printmanager.print(name, new MyPrintDocumentAdapter(f), null);
        } else {

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_SUBJECT, "Order Detail");
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("message/*");
            startActivity(Intent.createChooser(
                    email, "Send mail..."));
        }

    }

    private void addTitle(Document document) {
        try {
            Paragraph preface = new Paragraph();
            preface.setAlignment(Element.HEADER);
            String date = "";
            if (dataMap.get(Constants.TAG_SALE_DATE) != null && !dataMap.get(Constants.TAG_SALE_DATE).equals("")) {
                long ordDate = Long.parseLong(dataMap.get(Constants.TAG_SALE_DATE)) * 1000;
                date = FantacyApplication.getDate(ordDate);
            }
            String title = "\n\n" + "Order # " + dataMap.get(Constants.TAG_BARCODE) + " on " + date;
            preface.add(new Paragraph(title, new Font(Font.FontFamily.UNDEFINED, 18,
                    Font.BOLD)));
            document.add(preface);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void addContent(Document document) {
        try {
            Paragraph header = new Paragraph();
            header.add("\n" + "Order status : " + dataMap.get(Constants.TAG_STATUS));
            header.add("\n" + "Payment method : " + dataMap.get(Constants.TAG_PAYMENT_MODE));
            header.add("\n" + "Delivery type : " + dataMap.get(Constants.TAG_DELIVERY_TYPE));
            document.add(header);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            LineSeparator lineSeparator = new LineSeparator();
            lineSeparator.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator);

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            PdfPCell cellOne = new PdfPCell(new Phrase("Store Details", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));
            PdfPCell cellTwo = new PdfPCell(new Phrase("Shipping Address", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));

            cellOne.setBorder(Rectangle.NO_BORDER);
            cellOne.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            cellTwo.setBorder(Rectangle.NO_BORDER);
            cellTwo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            table.addCell(cellOne);
            table.addCell(cellTwo);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            document.add(table);

            PdfPTable table2 = new PdfPTable(2);
            PdfPCell cOne = new PdfPCell(new Phrase("Store Name", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_FULL_NAME), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(1);

            cOne = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_STORE_NAME)));
            cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_ADDRESS1) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(2);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_ADDRESS2) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(3);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_CITY) + " - " + dataMap.get(Constants.TAG_ZIPCODE) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(4);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_STATE) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(5);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(dataMap.get(Constants.TAG_COUNTRY) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(6);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase("Phone no : " + dataMap.get(Constants.TAG_PHONE)));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table2);

            document.add(Chunk.NEWLINE);

            LineSeparator lineSeparator2 = new LineSeparator();
            lineSeparator2.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator2);

            document.add(Chunk.NEWLINE);

            PdfPTable table3 = new PdfPTable(2);
            PdfPCell c1 = new PdfPCell(new Phrase("", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell c2 = new PdfPCell(new Phrase("Item Total                " + dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_ITEM_TOTAL), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(1);

            c1 = new PdfPCell(new Phrase(""));
            if (dataMap.get(Constants.TAG_SHIPPING_PRICE).equals(getString(R.string.free_text)))
                c2 = new PdfPCell(new Phrase("Shipping price        " + dataMap.get(Constants.TAG_SHIPPING_PRICE), new Font(Font.FontFamily.UNDEFINED, 12,
                        Font.BOLD)));
            else
                c2 = new PdfPCell(new Phrase("Shipping price        " + dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_SHIPPING_PRICE), new Font(Font.FontFamily.UNDEFINED, 12,
                        Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(2);

            c1 = new PdfPCell(new Phrase(""));
            c2 = new PdfPCell(new Phrase("Tax                           " + dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_TAX), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(3);

            c1 = new PdfPCell(new Phrase(""));
            c2 = new PdfPCell(new Phrase("Total Price               " + dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_GRAND_TOTAL), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setCellEvent(new LineCell());
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setPaddingTop(FantacyApplication.dpToPx(OrderDetail.this, 5));
            c2.setPaddingBottom(FantacyApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table3);

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.orders_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            finish();
            Intent i = new Intent(this, FragmentMainActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.create_dispute) {
            if (dataMap.get(Constants.TAG_DISPUTE_CREATED).equals("yes")) {
                Intent intent = new Intent(this, DisputeChat.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("disputeId", dataMap.get(Constants.TAG_DISPUTE_ID));
                intent.putExtra("from", dataMap.get(Constants.TAG_DISPUTE_STATUS));
                startActivity(intent);
            } else {
                Intent i = new Intent(this, CreateDispute.class);
                i.putExtra("order_id", dataMap.get(Constants.TAG_ORDER_ID));
                i.putExtra("grand_total", dataMap.get(Constants.TAG_GRAND_TOTAL));
                i.putExtra("currency", dataMap.get(Constants.TAG_CURRENCY));
                i.putExtra("items", itemsAry);
                startActivity(i);
            }
        } /*else if (id == R.id.contact_seller) {
            if (GetSet.isLogged()) {
                contactSeller(dataMap);
            } else {
                Intent login = new Intent(OrderDetail.this, LoginActivity.class);
                startActivity(login);
            }
        }*/ else if (id == R.id.download_invoice) {
            createPdf();
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeStatus(final String status) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "changeStatusRes= " + res);
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(OrderDetail.this.findViewById(R.id.parentLay), error, "error");
                    } else {
                        MyOrders.refreshorder = true;
                        getData();
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
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Log.e(TAG, "changeStatusError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("order_id", orderID);
                map.put("chstatus", status);
                if (status.equals("Delivered")) {
                    final String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                    map.put("current_time", timestamp);
                }
                if (status.equals("Returned")) {
                    map.put("id", "");
                    map.put("courier_name", "");
                    map.put("courier_service", "");
                    map.put("track_id", "");
                    map.put("notes", "");
                    map.put("shipping_date", "");
                    map.put("reason", "");
                }
                Log.v(TAG, "changeStatusParams=" + map);
                return map;
            }
        };
        pd.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void uploadSelfie(final String imageurl) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UPLOAD_SELFIE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "uploadSelfieRes=" + res);
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(OrderDetail.this.findViewById(R.id.parentLay), error, "error");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FantacyApplication.showToast(OrderDetail.this, getString(R.string.selfie_upload_success), Toast.LENGTH_SHORT);
                            }
                        });
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
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Log.e(TAG, "uploadSelfieError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("image", imageurl);
                map.put("item_id", uploadItemID);
                Log.v(TAG, "uploadSelfieParams=" + map);
                return map;
            }
        };
        pd.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        if (imageUploading) {
            pd.show();
        } else {
            getData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        if (imageUploading) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermission(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.camera_permission_description, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getApplication().getPackageName()));
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            OrderDetail.this.startActivity(intent);
                        }
                    }
                }
                break;
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(OrderDetail.this, permissions, requestCode);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.backBtn:
                if(Constants.isNotification){
                    Intent i = new Intent(this,FragmentMainActivity.class);
                    startActivity(i);
                }
                Constants.isNotification=false;
                finish();
                break;
            case R.id.claimOrder:

                dialogtitle.setText(getString(R.string.really_claim_order));
                yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeStatus("Claim");
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
                break;
            case R.id.trackingDetails:
                if (dataMap.get(Constants.TAG_TRACKING_ID) != null && !dataMap.get(Constants.TAG_TRACKING_ID).equals("")) {
                    Intent s = new Intent(OrderDetail.this, TrackingDetails.class);
                    s.putExtra("from", "status");
                    s.putExtra("shipping_date", dataMap.get(Constants.TAG_SHIPPING_DATE));
                    s.putExtra("shipping_name", dataMap.get(Constants.TAG_COURIER_NAME));
                    s.putExtra("shipping_service", dataMap.get(Constants.TAG_COURIER_SERVICE));
                    s.putExtra("tracking_id", dataMap.get(Constants.TAG_TRACKING_ID));
                    s.putExtra("additional_msg", dataMap.get(Constants.TAG_NOTES));
                    startActivity(s);
                }
                break;
            case R.id.order_status:
                if (dataMap.get(Constants.TAG_STATUS).equals("Shipped")) {
                    dialogtitle.setText(getString(R.string.really_deliver_order));
                    no.setVisibility(View.VISIBLE);

                    yes.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            changeStatus("Delivered");
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

                } else if (dataMap.get(Constants.TAG_STATUS).equals("Delivered")) {
                    Intent s = new Intent(OrderDetail.this, TrackingDetails.class);
                    s.putExtra("from", "return");
                    s.putExtra("orderid", orderID);
                    s.putExtra("image", itemsAry.get(0).get(Constants.TAG_ITEM_IMAGE));
                    s.putExtra("item_name", itemsAry.get(0).get(Constants.TAG_ITEM_NAME));
                    startActivity(s);
                } else {
                    dialogtitle.setText(getString(R.string.really_cancel_order));
                    no.setVisibility(View.VISIBLE);
                    yes.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            changeStatus("Cancel");
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
                break;
        }
    }

    private void contactSeller(final HashMap<String, String> tempMap) {
        final ProgressDialog dialog = new ProgressDialog(OrderDetail.this);
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
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        String chatId = DefensiveClass.optString(result, Constants.TAG_CHAT_ID);
                        Intent i = new Intent(OrderDetail.this, SellerChat.class);
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
                        Intent i = new Intent(OrderDetail.this, ContactSeller.class);
                        i.putExtra("itemId", firstItemId);
                        i.putExtra("shopId", tempMap.get(Constants.TAG_STORE_ID));
                        i.putExtra("image", tempMap.get(Constants.TAG_IMAGE));
                        i.putExtra("itemTitle", firstItemTitle);
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
                map.put("user_id", GetSet.getUserId());
                map.put("item_id", firstItemId);
                map.put("shop_id", tempMap.get(Constants.TAG_STORE_ID));
                map.put("offset", "0");
                map.put("limit", "10");
                Log.v(TAG, "contactSellerParams=" + map);
                return map;
            }
        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.create_dispute).setVisible(isDisputeEnabled);
        return true;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_items_details, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            if (position == 0) {
                holder.storeLay.setVisibility(View.VISIBLE);
                String storeText = getString(R.string.item_from) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.textPrimary))) + "'>" + dataMap.get(Constants.TAG_STORE_NAME) + "</font>";
                holder.storeName.setText(Html.fromHtml(storeText));
            } else {
                holder.storeLay.setVisibility(View.GONE);
            }

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_NAME));
            holder.status.setText(dataMap.get(Constants.TAG_STATUS));
            holder.quantity.setText(tempMap.get(Constants.TAG_QUANTITY));
            if (tempMap.get(Constants.TAG_SIZE) == null || tempMap.get(Constants.TAG_SIZE).equals("")) {
                holder.size.setText("N/A");
            } else {
                holder.size.setText(tempMap.get(Constants.TAG_SIZE));
            }
            holder.price.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            if (tempMap.get(Constants.TAG_DEAL_PERCENTAGE).equals("0"))
                holder.discountLay.setVisibility(View.GONE);
            else
                holder.discountLay.setVisibility(View.VISIBLE);
            holder.discountPercent.setText(tempMap.get(Constants.TAG_DEAL_PERCENTAGE) + "%");

            String image = tempMap.get(Constants.TAG_ITEM_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.itemImage);
            }

            if (FantacyApplication.isRTL(context)) {
                holder.itemName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else {
                holder.itemName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Pending")) {
                holder.processingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.shippingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.deliveredDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
            } else if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Processing")) {
                holder.placedRipple.setVisibility(View.GONE);
                holder.processingRipple.setVisibility(View.VISIBLE);
                holder.processingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.deliveredDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.processingLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.processingLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
            } else if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Shipped")) {
                holder.placedRipple.setVisibility(View.GONE);
                holder.processingRipple.setVisibility(View.GONE);
                holder.shippingRipple.setVisibility(View.VISIBLE);
                holder.processingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.deliveredDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.processingLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.processingLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
            } else if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Delivered") || dataMap.get(Constants.TAG_STATUS).equals("Paid")) {
                holder.placedRipple.setVisibility(View.GONE);
                holder.processingRipple.setVisibility(View.GONE);
                holder.shippingRipple.setVisibility(View.GONE);
                holder.deliveredRipple.setVisibility(View.VISIBLE);
                holder.processingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.deliveredDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.processingLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.processingLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.shippingLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.deliveredLine1.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
                holder.deliveredLine2.setBackgroundColor(ContextCompat.getColor(OrderDetail.this, R.color.colorPrimary));
            } else {
                holder.placedRipple.setVisibility(View.GONE);
                holder.processingRipple.setVisibility(View.GONE);
                holder.shippingRipple.setVisibility(View.GONE);
                holder.deliveredRipple.setVisibility(View.GONE);
                holder.placedDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.processingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.shippingDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
                holder.deliveredDot.setColorFilter(ContextCompat.getColor(OrderDetail.this, R.color.divider));
            }

            //if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Delivered") || dataMap.get(Constants.TAG_STATUS).equals("Paid")) {
            if((dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Delivered") || dataMap.get(Constants.TAG_STATUS).equals("Paid")) && !dataMap.get(Constants.TAG_ORDER_ID).equalsIgnoreCase("") && dataMap.get(Constants.TAG_REVIEW_ID).equalsIgnoreCase("")){
                holder.reviewSeller.setVisibility(View.VISIBLE);
                holder.uploadSelfieLay.setVisibility(View.VISIBLE);
                holder.reviewSeller.setText(getResources().getString(R.string.writereviewsmall));
            } else {
                if(!dataMap.get(Constants.TAG_REVIEW_ID).equalsIgnoreCase("")) {
                    holder.reviewSeller.setVisibility(View.VISIBLE);
                    holder.reviewSeller.setText(getResources().getString(R.string.editreview));
                }
                else holder.reviewSeller.setVisibility(View.GONE);
                if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Delivered") || dataMap.get(Constants.TAG_STATUS).equals("Paid"))
                holder.uploadSelfieLay.setVisibility(View.VISIBLE);
                else holder.uploadSelfieLay.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView itemImage, placedDot, processingDot, shippingDot, deliveredDot;
            TextView itemName, storeName, reviewSeller, status, size, quantity, price, discountPercent, discountPrice;
            RelativeLayout storeLay;
            LinearLayout uploadSelfieLay, discountLay;
            AVLoadingIndicatorView placedRipple, processingRipple, shippingRipple, deliveredRipple;
            View processingLine1, processingLine2, shippingLine1, shippingLine2, deliveredLine1, deliveredLine2;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                storeName = (TextView) view.findViewById(R.id.storeName);
                reviewSeller = (TextView) view.findViewById(R.id.reviewSeller);
                status = (TextView) view.findViewById(R.id.status);
                size = (TextView) view.findViewById(R.id.size);
                quantity = (TextView) view.findViewById(R.id.quantity);
                price = (TextView) view.findViewById(R.id.price);
                discountPercent = (TextView) view.findViewById(R.id.discountPercent);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);
                storeLay = (RelativeLayout) view.findViewById(R.id.storelay);
                uploadSelfieLay = (LinearLayout) view.findViewById(R.id.uploadSelfieLay);
                discountLay = (LinearLayout) view.findViewById(R.id.discountLay);
                placedRipple = (AVLoadingIndicatorView) view.findViewById(R.id.placedRipple);
                processingRipple = (AVLoadingIndicatorView) view.findViewById(R.id.processingRipple);
                shippingRipple = (AVLoadingIndicatorView) view.findViewById(R.id.shippingRipple);
                deliveredRipple = (AVLoadingIndicatorView) view.findViewById(R.id.deliveredRipple);
                placedDot = (ImageView) view.findViewById(R.id.placedDot);
                processingDot = (ImageView) view.findViewById(R.id.processingDot);
                shippingDot = (ImageView) view.findViewById(R.id.shippingDot);
                deliveredDot = (ImageView) view.findViewById(R.id.deliveredDot);
                processingLine1 = (View) view.findViewById(R.id.processingLine1);
                processingLine2 = (View) view.findViewById(R.id.processingLine2);
                shippingLine1 = (View) view.findViewById(R.id.shippingLine1);
                shippingLine2 = (View) view.findViewById(R.id.shippingLine2);
                deliveredLine1 = (View) view.findViewById(R.id.deliveredLine1);
                deliveredLine2 = (View) view.findViewById(R.id.deliveredLine2);

                reviewSeller.setOnClickListener(this);
                uploadSelfieLay.setOnClickListener(this);
                status.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.reviewSeller:
                        Intent r = new Intent(OrderDetail.this, WriteReview.class);
                        r.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //r.putExtra("review_id", dataMap.get(Constants.TAG_REVIEW_ID));
                       // r.putExtra("review_title", dataMap.get(Constants.TAG_REVIEW_TITLE));
                        //r.putExtra("rating", dataMap.get(Constants.TAG_RATING));
                        //r.putExtra("review_des", dataMap.get(Constants.TAG_REVIEW_DES));
                        r.putExtra("item_id", firstItemId);
                        r.putExtra("order_id", dataMap.get(Constants.TAG_ORDER_ID));
                        r.putExtra("image", Items.get(0).get(Constants.TAG_ITEM_IMAGE));
                        r.putExtra("item_name", Items.get(0).get(Constants.TAG_ITEM_NAME));
                        r.putExtra("review_id", dataMap.get(Constants.TAG_REVIEW_ID));
                        r.putExtra("rating", dataMap.get(Constants.TAG_RATING));
                        r.putExtra("review_desc", dataMap.get(Constants.TAG_REVIEW_DES));
                        startActivity(r);
                        break;
                    case R.id.uploadSelfieLay:
                        uploadItemID = Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID);
                        if (ContextCompat.checkSelfPermission(OrderDetail.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(OrderDetail.this, CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(OrderDetail.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                        } else {
                            ImagePicker.pickImage(OrderDetail.this, "Select your image:");
                        }
                        break;

                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {

        private static final int MILS_IN_INCH = 1000;
        PdfDocument pdfdocument;
        Context context;
        File file;

        public MyPrintDocumentAdapter(File f) {
            file = f;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback, Bundle extras) {

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(pdfName).
                    setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

            callback.onLayoutFinished(pdi, true);
        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback) {

            InputStream input = null;
            OutputStream output = null;

            try {

                input = new FileInputStream(file);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (FileNotFoundException ee) {
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private int computePageCount(PrintAttributes printAttributes) {
            int itemsPerPage = 4; // default item count for portrait mode

            PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
            if (!pageSize.isPortrait()) {
                // Six items per page in landscape orientation
                itemsPerPage = 3;
            }

            // Determine number of print items
            int printItemCount = 5;

            return (int) Math.ceil(printItemCount / itemsPerPage);
        }
    }

    class HeaderFooterPageEvent extends PdfPageEventHelper {

        public void onStartPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String header = getString(R.string.app_name) + " | " + getString(R.string.app_name) + getString(R.string.invoice_tag_line);
            long unixTime = System.currentTimeMillis();
            String date = FantacyApplication.getDate(unixTime);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(header), rect.getLeft(), rect.getTop(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(date), rect.getRight(), rect.getTop(), 0);
        }

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String url = Constants.URL;
            String page = "Page" + document.getPageNumber() + " of " + document.getPageNumber();
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(url), rect.getLeft(), rect.getBottom(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(page), rect.getRight(), rect.getBottom(), 0);
        }

    }

    class LineCell implements PdfPCellEvent {

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position,
                               PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.setLineDash(0f, 0f);
            canvas.setColorStroke(BaseColor.LIGHT_GRAY);
            canvas.moveTo(position.getLeft(), position.getTop());
            canvas.lineTo(position.getRight(), position.getTop());
            canvas.moveTo(position.getLeft(), position.getBottom());
            canvas.lineTo(position.getRight(), position.getBottom());
            canvas.stroke();
        }
    }

    /**
     * for upload user image
     **/
    class uploadImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        String status;
        String existingFileName = "";

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.IMAGE_UPLOAD_URL;
            try {
                existingFileName = imgpath[0];
                Log.v(TAG, "existingFileName " + existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("selfie");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v("buffer", "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v("bytesRead", "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v(TAG, "json" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("result");
                    String name = DefensiveClass.optString(image, "name");
                    String viewUrl = DefensiveClass.optString(image, "image");

                    //    imagesAry.add(viewUrl);
                    try {
                        //        JSONArray jsonArray = new JSONArray(imagesAry);
                        //      imagesJson = String.valueOf(jsonArray);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    uploadSelfie(viewUrl);
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!OrderDetail.this.isFinishing()) {
                pd.show();
            }
        }

        @Override
        protected void onPostExecute(Integer unused) {
            try {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                imageUploading = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
