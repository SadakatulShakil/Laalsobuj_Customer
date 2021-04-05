package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
 * Created by hitasoft on 18/7/17.
 */

public class Help extends Fragment {

    private static final String TAG = Help.class.getSimpleName();
    String email = "", address = "", phone = "";
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<String> topicsAry = new ArrayList<>();
    ArrayList<HashMap<String, String>> helpAry = new ArrayList<HashMap<String, String>>();
    RelativeLayout progress, nullLay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.help, "Help");

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progress = (RelativeLayout) view.findViewById(R.id.progress);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        helpAry.clear();
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), helpAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_PAGE_NAME, getString(R.string.contact_us));
        helpAry.add(0, map);

        progress.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        getHelpData();
    }

    private void getHelpData() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HELP, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "getHelpDataRes=" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        email = DefensiveClass.optString(json, Constants.TAG_EMAIL);
                        address = DefensiveClass.optString(json, Constants.TAG_ADDRESS);
                        phone = DefensiveClass.optString(json, Constants.TAG_PHONE);

                        JSONArray topics = json.getJSONArray(Constants.TAG_CONTACT_TOPICS);
                        for (int i = 0; i < topics.length(); i++) {
                            topicsAry.add(topics.getString(i));
                        }

                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            String page_name = DefensiveClass.optString(temp, Constants.TAG_PAGE_NAME);
                            String main = DefensiveClass.optString(temp, Constants.TAG_MAIN_CONTENT);
                            String sub = DefensiveClass.optString(temp, Constants.TAG_SUB_CONTENT);

                            map.put(Constants.TAG_PAGE_NAME, page_name);
                            map.put(Constants.TAG_MAIN_CONTENT, main);
                            map.put(Constants.TAG_SUB_CONTENT, sub);

                            helpAry.add(map);
                        }
                        recyclerViewAdapter.notifyDataSetChanged();
                        progress.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        nullLay.setVisibility(View.GONE);
                    } else if (status.equalsIgnoreCase("error")) {
                       /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                        i.putExtra("from", "maintenance");
                        startActivity(i);*/
                    }
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
                Log.e(TAG, "getHelpDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            View bgView;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                bgView = (View) view.findViewById(R.id.bgView);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        if (Items.get(getAdapterPosition()).get(Constants.TAG_PAGE_NAME).equals(getString(R.string.contact_us))) {
                            Intent i = new Intent(getActivity(), ContactUs.class);
                            i.putExtra("topicsAry", topicsAry);
                            i.putExtra("email", email);
                            i.putExtra("address", address);
                            i.putExtra("phone", phone);
                            startActivity(i);
                        } else {
                            HelpContent.mainContent = Items.get(getAdapterPosition()).get(Constants.TAG_MAIN_CONTENT);
                            HelpContent.subContent = Items.get(getAdapterPosition()).get(Constants.TAG_SUB_CONTENT);
                            Intent i = new Intent(getActivity(), HelpContent.class);
                            i.putExtra("pageName", Items.get(getAdapterPosition()).get(Constants.TAG_PAGE_NAME));
                            startActivity(i);
                        }
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.help_list_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.name.setText(tempMap.get(Constants.TAG_PAGE_NAME));
            if (tempMap.get(Constants.TAG_PAGE_NAME).equalsIgnoreCase("About")) {
                holder.bgView.setVisibility(View.VISIBLE);
            } else {
                holder.bgView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    private void setErrorLayout() {
        progress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
    }
}
