package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 11/7/17.
 */

public class GroupGiftList extends Fragment implements View.OnClickListener {

    private static final String TAG = GroupGiftList.class.getSimpleName();
    TextView giftText, giftDes, howItWorks;
    RelativeLayout introLay;
    Display display;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> giftAry = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gift_list_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.groupGift, "GroupGift");

        introLay = (RelativeLayout) view.findViewById(R.id.introLay);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        giftText = (TextView) view.findViewById(R.id.giftText);
        giftDes = (TextView) view.findViewById(R.id.giftDes);
        howItWorks = (TextView) view.findViewById(R.id.howItWorks);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        howItWorks.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        display = getActivity().getWindowManager().getDefaultDisplay();

        giftText.setText(getString(R.string.app_name) + " " + getString(R.string.group_gift));
        giftDes.setText(getString(R.string.group_gift_des));
        //  linkTextView(getActivity(), giftDes, getString(R.string.group_gift_des), getString(R.string.learn_more));

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), giftAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getData(current_page * Constants.OVERALL_LIMIT);
                    Log.v(TAG, "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                giftAry.clear();
                getData(0);
            }
        });

        giftAry.clear();
        getData(0);
    }

    private void getData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GROUP_GIFT_LIST, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "getDataRes=" + res);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (giftAry.size() >= Constants.OVERALL_LIMIT && giftAry.get(giftAry.size() - 1) == null) {
                        giftAry.remove(giftAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(giftAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String gift_id = DefensiveClass.optString(temp, Constants.TAG_GIFT_ID);
                            String start_date = DefensiveClass.optString(temp, Constants.TAG_START_DATE);
                            String end_date = DefensiveClass.optString(temp, Constants.TAG_END_DATE);
                            String grpStatus = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String title = DefensiveClass.optString(temp, Constants.TAG_TITLE);

                            map.put(Constants.TAG_GIFT_ID, gift_id);
                            map.put(Constants.TAG_START_DATE, start_date);
                            map.put(Constants.TAG_END_DATE, end_date);
                            map.put(Constants.TAG_STATUS, grpStatus);
                            map.put(Constants.TAG_TITLE, title);

                            giftAry.add(map);
                        }
                        if (mScrollListener != null && giftAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    if (giftAry.size() == 0) {
                        /*nullImage.setImageResource(R.drawable.no_list);
                        nullText.setText(getString(R.string.no_item_list));
                        nullLay.setVisibility(View.VISIBLE);*/
                        introLay.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                        introLay.setVisibility(View.GONE);
                    }
                    progressLay.setVisibility(View.GONE);
                    recyclerViewAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    setErrorLayout();
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    setErrorLayout();
                    e.printStackTrace();
                } catch (Exception e) {
                    setErrorLayout();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorLayout();
                Log.e(TAG, "getDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (giftAry.size() >= Constants.OVERALL_LIMIT) {
                            giftAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(giftAry.size() - 1);
                        } else if (giftAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getDataParams=" + map);
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

    private void setErrorLayout() {
        if (giftAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView title, status, date;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                title = (TextView) view.findViewById(R.id.title);
                status = (TextView) view.findViewById(R.id.status);
                date = (TextView) view.findViewById(R.id.date);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), GroupGiftDetail.class);
                        i.putExtra("giftId", Items.get(getAdapterPosition()).get(Constants.TAG_GIFT_ID));
                        startActivity(i);
                        Toast.makeText(context, "Under Construction !", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_list_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

                viewHolder.title.setText(tempMap.get(Constants.TAG_TITLE));

                String status = tempMap.get(Constants.TAG_STATUS);
                /*Capitalize the First Character*/
                status = status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase();
                viewHolder.status.setText(status);
                if (status.equalsIgnoreCase("Success")) {
                    viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                } else if (status.equalsIgnoreCase("Active")) {
                    viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.green));
                } else if (status.equalsIgnoreCase("Expired") || status.equalsIgnoreCase("Cancel")) {
                    viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else {
                    viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }

                if (!tempMap.get(Constants.TAG_START_DATE).equals(null) && !tempMap.get(Constants.TAG_START_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_START_DATE)) * 1000;
                    viewHolder.date.setText(FantacyApplication.getDate(date));
                }
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }

        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

 /*   private void linkTextView(final Context context, TextView view, final String desc, final String appendTxt) {

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(desc + " ");
        int count = spanTxt.length();
        spanTxt.append(appendTxt);
        spanTxt.setSpan(new MyClickableSpan(context) {
            @Override
            public void onClick(View widget) {
                showLearnmoreDialog();
            }
        }, count, spanTxt.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }*/


    private void showHelpDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.group_gift_help_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView ok = (TextView) dialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void showLearnmoreDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.group_gift_help_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView desc = (TextView) dialog.findViewById(R.id.description);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);

        title.setText(getString(R.string.app_name) + " " + getString(R.string.group_gift));
        desc.setText(getString(R.string.group_gift_des));

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.howItWorks:
                showHelpDialog();
                break;
        }
    }
}
