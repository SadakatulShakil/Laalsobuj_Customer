package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.futureskyltd.app.external.ObjectSerializer;
import com.futureskyltd.app.helper.NetworkReceiver;

import java.util.ArrayList;

import static android.Manifest.permission.CAMERA;


/**
 * Created by hitasoft on 15/5/17.
 */

public class RecentSearch extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    Toolbar toolbar;
    SharedPreferences pref;
    SharedPreferences.Editor edit;
    EditText searchView;
    ListView listView;
    ImageView backBtn, barcode, cancelBtn;
    HistoryAdapter historyAdapter;
    ArrayList<String> searchAry = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_search);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchView = (EditText) findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.listView);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        barcode = (ImageView) findViewById(R.id.barcode);
        cancelBtn = (ImageView) findViewById(R.id.cancelBtn);

        setSupportActionBar(toolbar);

        pref = getApplicationContext().getSharedPreferences("SearchHistory", MODE_PRIVATE);
        edit = pref.edit();

        historyAdapter = new HistoryAdapter(RecentSearch.this, searchAry);
        listView.setAdapter(historyAdapter);

        getHistory();

        backBtn.setOnClickListener(this);
        barcode.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i("TAG", "Enter pressed");
                    if (searchView.getText().toString().trim().length() == 0) {
                        FantacyApplication.showToast(RecentSearch.this, getString(R.string.please_enter_product_name), Toast.LENGTH_SHORT);
                    } else {
                        try {
                            String query = searchView.getText().toString();

                            if (query.trim().length() != 0 && !queryContains(query)) {
                                Log.v("Added", "Query Added");
                                if (searchAry.size() > 0) {
                                    searchAry.remove(searchAry.indexOf("clear-" + getString(R.string.clear_hitsory)));
                                }
                                searchAry.add(0, "name-" + query);
                                searchAry.add("clear-" + getString(R.string.clear_hitsory));
                                historyAdapter.notifyDataSetChanged();
                            }

                            Intent i = new Intent(RecentSearch.this, CategoryListings.class);
                            i.putExtra("from", "search");
                            i.putExtra("searchKey", query);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);

                            storeHistory();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0) {
                    cancelBtn.setVisibility(View.VISIBLE);
                    barcode.setVisibility(View.INVISIBLE);
                } else {
                    cancelBtn.setVisibility(View.INVISIBLE);
                    barcode.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private boolean queryContains(String value) {
        boolean flag = false;
        for (int i = 0; i < searchAry.size(); i++) {
            String split[] = searchAry.get(i).split("-");
            String type = split[0];
            String query = split[1];
            if (type.equals("name")) {
                if (value.equalsIgnoreCase(query)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public void storeHistory() {
        try {
            ArrayList<String> tempAry = new ArrayList<String>();
            tempAry.addAll(searchAry);
            if (tempAry.size() > 0) {
                tempAry.remove(tempAry.indexOf("clear-" + getString(R.string.clear_hitsory)));
            }
            edit.clear();
            edit.putString("history", ObjectSerializer.serialize(tempAry));
            edit.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHistory() {
        try {
            ArrayList<String> tempAry = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("history", ObjectSerializer.serialize(new ArrayList<String>())));
            searchAry.addAll(tempAry);
            if (searchAry.size() > 0) {
                searchAry.add("clear-" + getString(R.string.clear_hitsory));
            }
            historyAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HistoryAdapter extends BaseAdapter {
        ArrayList<String> data;
        Context mContext;
        ViewHolder holder = null;

        public HistoryAdapter(Context context, ArrayList<String> list) {
            data = list;
            mContext = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            TextView name;
            ImageView image;
            RelativeLayout layout;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.recent_search_item, null);//layout
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                String split[] = data.get(position).split("-");
                final String type = split[0];
                final String query = split[1];
                holder.name.setText(query);
                if (type.equals("name")) {
                    holder.image.setImageResource(R.drawable.stopwatch);
                } else if (type.equals("barcode")) {
                    holder.image.setImageResource(R.drawable.barcode);
                } else if (type.equals("clear")) {
                    holder.image.setImageResource(R.drawable.cancel);
                }

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.equals("clear")) {
                            searchAry.clear();
                            edit.clear();
                            edit.commit();
                            historyAdapter.notifyDataSetChanged();
                        } else {
                            Intent i = new Intent(RecentSearch.this, CategoryListings.class);
                            if (type.equals("name")) {
                                i.putExtra("from", "search");
                            } else if (type.equals("barcode")) {
                                i.putExtra("from", "barcode");
                            }
                            i.putExtra("searchKey", query);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
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
                    Intent b = new Intent(this, BarcodeScanner.class);
                    startActivity(b);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_camera_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                /*If soft keyboard is open then close it*/
                FantacyApplication.hideSoftKeyboard(RecentSearch.this, backBtn);
                finish();
                break;
            case R.id.barcode:
                if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 100);
                } else {
                    Intent b = new Intent(this, BarcodeScanner.class);
                    startActivity(b);
                }
                break;
            case R.id.cancelBtn:
                searchView.setText("");
                cancelBtn.setVisibility(View.INVISIBLE);
                barcode.setVisibility(View.VISIBLE);
                break;
        }
    }
}
