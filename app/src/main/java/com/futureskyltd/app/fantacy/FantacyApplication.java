package com.futureskyltd.app.fantacy;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.futureskyltd.app.external.FontsOverride;
import com.futureskyltd.app.external.RateThisApp;
import com.futureskyltd.app.helper.LocaleManager;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.google.android.material.snackbar.Snackbar;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by hitasoft on 11/5/17.
 */

@ReportsCrashes(mailTo = "crashlog@hitasoft.com")
public class FantacyApplication extends Application {

    private static FantacyApplication mInstance;
    public static final String TAG = FantacyApplication.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static Snackbar snackbar = null;
    private static Toast toast = null;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static DecimalFormat decimal = new DecimalFormat(".##");
    static String[] languages;
    static String[] langCode;
    private Locale locale;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/
        mInstance = this;
        pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        editor = pref.edit();
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setEmail(pref.getString("email", null));
            GetSet.setFullName(pref.getString("fullName", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
        }
        FontsOverride.setDefaultFont(this, "MONOSPACE", "font_regular.ttf");

        // Monitor launch times and interval from installation
        RateThisApp.Config configure = new RateThisApp.Config(3, 10);
        configure.setUrl("https://play.google.com/store/apps/details?id=" + getPackageName());
        RateThisApp.init(configure);
        RateThisApp.onStart(this, "app");
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleManager.setLocale(context));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public static synchronized FantacyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(view, context.getString(R.string.network_failure), Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
        }

        if (isConnected) {
            if (snackbar.isShown()) {
                snackbar.dismiss();
                snackbar = null;
            }
        } else {
            if (!snackbar.isShown()) {
                snackbar.show();
            }
        }
    }

    /**
     * function for avoiding emoji typing in keyboard
     **/
    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };

    public static void showToast(final Context context, String text, int duration) {

        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast = Toast.makeText(context, text, duration);
            toast.show();
        } else toast.cancel();
    }

    public static boolean isRTL(Context context) {
        if (context != null && context.getResources().getConfiguration().locale.toString().equals("ar")) {
            return true;
        } else {
            return false;
        }
    }

    public static String loadJSONFromAsset(Context context, String name) {
        String json = null;
        try {

            InputStream is = context.getResources().getAssets().open(name);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * To convert timestamp to Date
     **/
    public static String getDate(long timeStamp) {

        try {
            SharedPreferences lanPref = FantacyApplication.getInstance().getApplicationContext().getSharedPreferences("LangPref", MODE_PRIVATE);
            String languageCode = lanPref.getString(Constants.TAG_CODE, Constants.LANGUAGE_CODE);
            Locale loc = new Locale(languageCode);
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy", loc);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    /**
     * for closing the keyboard while touch outside
     **/
    public static void setupUI(Context context, View view) {
        final Activity act = (Activity) context;
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(act, v);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(act, innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity context, View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException npe) {
        } catch (Exception e) {
        }
    }

    public static void showSoftKeyboard(Activity context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (NullPointerException npe) {
        } catch (Exception e) {
        }
    }

    public static String formatPrice(String grand_total) {
        DecimalFormat precision = new DecimalFormat("#.##");
        return precision.format(Double.parseDouble(grand_total));
    }

    public static class avoidMultiSpace implements TextWatcher {

        private EditText view;

        avoidMultiSpace(EditText view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String result = s.toString().replaceAll("  ", " ");
            if (!s.toString().equals(result)) {
                view.setText(result);
                view.setSelection(result.length());
                // alert the user
            }
        }
    }

    // Showing network status in Snackbar
    public static void defaultSnack(View view, String message, String type) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            if (type.equals("alert"))
                textView.setTextColor(Color.WHITE);
            if (type.equals("error"))
                textView.setTextColor(Color.RED);
        }
        if (!snackbar.isShown()) {
            snackbar.show();
            snackbar = null;
        } else {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    public static void defaultDialog(final Activity context, String message) {
        Display display;
        display = context.getWindowManager().getDefaultDisplay();
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);

        title.setText(message);
        yes.setText(R.string.ok);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public static String stripHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return "" + Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return "" + Html.fromHtml(html);
        }
    }

    public static ArrayList<HashMap<String, String>> getSize(String json) {
        ArrayList<HashMap<String, String>> sizelist = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray size = new JSONArray(json);
            for (int x = 0; x < size.length(); x++) {
                JSONObject pobj = size.getJSONObject(x);
                HashMap<String, String> tmpMap = new HashMap<String, String>();

                String qty = DefensiveClass.optString(pobj, Constants.TAG_QTY);
                if (!qty.equals("0")) {
                    String name = DefensiveClass.optString(pobj, Constants.TAG_NAME);
                    String price = DefensiveClass.optString(pobj, Constants.TAG_PRICE);

                    tmpMap.put(Constants.TAG_NAME, name);
                    tmpMap.put(Constants.TAG_QTY, qty);
                    tmpMap.put(Constants.TAG_PRICE, price);

                    sizelist.add(tmpMap);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sizelist;
    }

    public static String getPrice(HashMap<String, String> map) {
        final ArrayList<HashMap<String, String>> sizeTemp = new ArrayList<HashMap<String, String>>();
        if (!map.get(Constants.TAG_SIZE).equals("")) {
            sizeTemp.addAll(FantacyApplication.getSize(map.get(Constants.TAG_SIZE)));
        }
        String price = "";
        if (sizeTemp.size() == 0) {
            price = map.get(Constants.TAG_PRICE);
        } else {
            int costFlag = 0;
            for (int i = 0; i < sizeTemp.size(); i++) {
                if (!sizeTemp.get(i).get(Constants.TAG_QTY).equals("0")) {
                    price = sizeTemp.get(i).get(Constants.TAG_PRICE);
                    costFlag = 1;
                    break;
                }

            }
            if (costFlag == 0) {
                price = map.get(Constants.TAG_PRICE);
            }
        }
        return price;
    }

    public static boolean isDealEnabled(String validTill) {

           if (!validTill.equals("")) {
               long timeStamp = Long.parseLong(validTill);

               Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(timeStamp * 1000);
               cal.set(Calendar.HOUR_OF_DAY, 23);
               cal.set(Calendar.MINUTE, 59);
               cal.set(Calendar.SECOND, 59);

               String calDate = android.text.format.DateFormat.format("dd-MM-yyyy", cal).toString();
               String currentDate = android.text.format.DateFormat.format("dd-MM-yyyy", (System.currentTimeMillis())).toString();
               if (currentDate.equals(calDate)) {
                   return true;
               }
           }else return true;


        return false;
    }

    public static String extractYoutubeId(String url) {
        String id = "";
        try {
            String query = new URL(url).getQuery();
            String[] param = query.split("&");
            for (String row : param) {
                String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return id;
    }
}
