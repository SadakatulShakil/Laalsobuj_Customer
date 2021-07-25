package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

/**
 * Created by hitasoft on 16/5/17.
 */

public class CategoryFragment extends Fragment {

    private static final String TAG = CategoryFragment.class.getSimpleName();
    ExpandableListView listView;
    String from = "";
    int position;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    ArrayList<HashMap<String, String>> categoryAry = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_main_layout, container, false);

        if (getArguments() != null) {
            from = getArguments().getString("from");
            position = getArguments().getInt("position");
        }

        if (from.equals("home") || from.equals("all")) {
            ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.allCategories, "Category");
        }

        listView = (ExpandableListView) view.findViewById(R.id.listView);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);
        progressLay.setVisibility(View.VISIBLE);
        categoryAry = new ArrayList<>();
        getCategory();
    }

    private void setAdapter() {
        ViewCompat.setNestedScrollingEnabled(listView, true);

        listView.setAdapter(new ParentLevel(getActivity(), categoryAry));

        if (from.equals("home")) {
            listView.expandGroup(position);
        }

        listView.setGroupIndicator(null);
        listView.setChildIndicator(null);
        listView.setChildDivider(new ColorDrawable(getActivity().getResources().getColor(R.color.white)));
        listView.setDivider(new ColorDrawable(getActivity().getResources().getColor(R.color.divider)));
        listView.setDividerHeight(FantacyApplication.dpToPx(getActivity(), 1));

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if (categoryAry.get(groupPosition).get(Constants.TAG_SIZE).equals("0")) {
                    Log.v("Parent cat", "cat name=" + categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                    if (from.equals("filter")) {
                        CategoryListings activity = (CategoryListings) getActivity();
                        activity.from = "category";
                        activity.catId = categoryAry.get(groupPosition).get(Constants.TAG_ID);
                        activity.catName = categoryAry.get(groupPosition).get(Constants.TAG_NAME);
                        activity.catState = "parent";
                        ((CategoryListings) getActivity()).onCategorySelected();
                        getActivity().onBackPressed();
                    } else {
                        Log.e("catelist",categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                        Intent i = new Intent(getActivity(), CategoryListings.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra("from", "category");
                        i.putExtra("catId", categoryAry.get(groupPosition).get(Constants.TAG_ID));
                        i.putExtra("catName", categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                        i.putExtra("catState", "parent");
                        startActivity(i);
                    }
                }
                return false;
            }
        });
    }

    private void getCategory() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CATEGORIES, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.d(TAG, "getCategoryRes="+res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray category = json.getJSONArray(Constants.TAG_CATEGORY);
                        for (int i = 0; i < category.length(); i++) {
                            JSONObject temp = category.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);
                            String icon = DefensiveClass.optString(temp, Constants.TAG_ICON);

                            JSONArray sub_category = temp.optJSONArray(Constants.TAG_SUB_CATEGORY);

                            map.put(Constants.TAG_ID, id);
                            map.put(Constants.TAG_NAME, name);
                            map.put(Constants.TAG_ICON, icon);
                            map.put(Constants.TAG_SUB_CATEGORY, String.valueOf(sub_category));
                            map.put(Constants.TAG_SIZE, String.valueOf(sub_category.length()));

                            categoryAry.add(map);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    showErrorLayout();
                    setAdapter();
                } catch (JSONException e) {
                    showErrorLayout();
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    showErrorLayout();
                    e.printStackTrace();
                } catch (Exception e) {
                    showErrorLayout();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showErrorLayout();
                Log.e(TAG, "getCategoryError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                return map;
            }

         /*   @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }*/

        };
        FantacyApplication.getInstance().getRequestQueue().cancelAll("tag");
        FantacyApplication.getInstance().addToRequestQueue(req, "cat");
    }

    private void showErrorLayout(){
        progressLay.setVisibility(View.GONE);
        if (categoryAry.size() == 0){
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<HashMap<String, String>> getSubCategory(String from, String json) {
        ArrayList<HashMap<String, String>> subcat = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray sub = new JSONArray(json);
            for (int x = 0; x < sub.length(); x++) {
                JSONObject temp = sub.getJSONObject(x);
                HashMap<String, String> map = new HashMap<String, String>();

                String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                String name = DefensiveClass.optString(temp, Constants.TAG_NAME);

                map.put(Constants.TAG_ID, id);
                map.put(Constants.TAG_NAME, name);

                if (from.equals("subcat")) {
                    JSONArray super_category = temp.optJSONArray(Constants.TAG_SUPER_CATEGORY);

                    map.put(Constants.TAG_SUPER_CATEGORY, String.valueOf(super_category));
                    map.put(Constants.TAG_SIZE, String.valueOf(super_category.length()));
                }

                subcat.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subcat;
    }

    public class ParentLevel extends BaseExpandableListAdapter {

        private Context context;
        ArrayList<HashMap<String, String>> catAry;
        GroupViewHolder groupViewHolder;

        public ParentLevel(Context context, ArrayList<HashMap<String, String>> arrayList) {
            this.context = context;
            this.catAry = arrayList;
        }

        @Override
        public Object getChild(int arg0, int arg1) {
            return arg1;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ArrayList<HashMap<String, String>> categoryAry = getSubCategory("subcat", catAry.get(groupPosition).get(Constants.TAG_SUB_CATEGORY));
            final String categoryId = catAry.get(groupPosition).get(Constants.TAG_ID);
            SecondLevelExpandableListView secondLevelELV = new SecondLevelExpandableListView(context);
            secondLevelELV.setAdapter(new SecondLevelAdapter(context, categoryAry));
            secondLevelELV.setGroupIndicator(null);
            secondLevelELV.setChildIndicator(null);
            secondLevelELV.setChildDivider(new ColorDrawable(context.getResources().getColor(R.color.white)));
            secondLevelELV.setDivider(new ColorDrawable(context.getResources().getColor(R.color.divider)));
            secondLevelELV.setDividerHeight(FantacyApplication.dpToPx(context, 1));
            secondLevelELV.setPadding(FantacyApplication.dpToPx(context, 10), 0, FantacyApplication.dpToPx(context, 10), 0);

            secondLevelELV.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
                    if (categoryAry.get(groupPosition).get(Constants.TAG_SIZE).equals("0")) {
                        Log.v("Sub cat", "cat name=" + categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                        if (from.equals("filter")) {
                            CategoryListings activity = (CategoryListings) getActivity();
                            activity.from = "category";
                            activity.catId = categoryId;
                            activity.subcatId = categoryAry.get(groupPosition).get(Constants.TAG_ID);
                            activity.catName = categoryAry.get(groupPosition).get(Constants.TAG_NAME);
                            activity.catState = "sub";
                            ((CategoryListings) getActivity()).onCategorySelected();
                            getActivity().onBackPressed();
                        } else {
                            Log.e("categd","-"+categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                            Intent i = new Intent(getActivity(), CategoryListings.class);
                            i.putExtra("from", "category");
                            i.putExtra("catId", categoryId);
                            i.putExtra("subcatId", categoryAry.get(groupPosition).get(Constants.TAG_ID));
                            i.putExtra("catName", categoryAry.get(groupPosition).get(Constants.TAG_NAME));
                            i.putExtra("catState", "sub");
                            startActivity(i);
                        }
                    }
                    return false;
                }
            });

            secondLevelELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    ArrayList<HashMap<String, String>> supercatAry = getSubCategory("supercat", categoryAry.get(groupPosition).get(Constants.TAG_SUPER_CATEGORY));
                    String subcatId = categoryAry.get(groupPosition).get(Constants.TAG_ID);
                    Log.v("Super cat", "cat name=" + supercatAry.get(childPosition).get(Constants.TAG_NAME));
                    Log.v("Super cat", "cat id=" + supercatAry.get(childPosition).get(Constants.TAG_ID));
                    if (from.equals("filter")) {
                        CategoryListings activity = (CategoryListings) getActivity();
                        activity.from = "category";
                        activity.catId = categoryId;
                        activity.subcatId = subcatId;
                        activity.supercatId = supercatAry.get(childPosition).get(Constants.TAG_ID);
                        activity.catName = supercatAry.get(childPosition).get(Constants.TAG_NAME);
                        activity.catState = "super";
                        activity.selectedCat.add(activity.supercatId);
                        ((CategoryListings) getActivity()).onCategorySelected();
                        getActivity().onBackPressed();
                    } else {
                        Intent i = new Intent(getActivity(), CategoryListings.class);
                        i.putExtra("from", "category");
                        i.putExtra("catId", categoryId);
                        i.putExtra("subcatId", subcatId);
                        i.putExtra("supercatId", supercatAry.get(childPosition).get(Constants.TAG_ID));
                        i.putExtra("catName", supercatAry.get(childPosition).get(Constants.TAG_NAME));
                        i.putExtra("catState", "super");
                        startActivity(i);
                    }
                    return false;
                }
            });
            return secondLevelELV;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (catAry.get(groupPosition).get(Constants.TAG_SIZE).equals("0")) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public int getGroupCount() {
            return categoryAry.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        private class GroupViewHolder {
            ImageView icon;
            TextView name;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.category_first_row, parent, false);

                groupViewHolder = new GroupViewHolder();

                groupViewHolder.name = (TextView) convertView.findViewById(R.id.name);
                groupViewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            HashMap<String, String> tempMap = catAry.get(groupPosition);

            groupViewHolder.name.setText(tempMap.get(Constants.TAG_NAME));

            if (getChildrenCount(groupPosition) > 0) {
                groupViewHolder.icon.setVisibility(View.VISIBLE);
            } else {
                groupViewHolder.icon.setVisibility(View.GONE);
            }

            if (isExpanded && getChildrenCount(groupPosition) > 0) {
                groupViewHolder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                groupViewHolder.icon.setColorFilter(getResources().getColor(R.color.colorPrimary));
                groupViewHolder.icon.setRotation(180);
            } else {
                groupViewHolder.name.setTextColor(getResources().getColor(R.color.textPrimary));
                groupViewHolder.icon.setColorFilter(getResources().getColor(R.color.textPrimary));
                groupViewHolder.icon.setRotation(0);
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public class
    SecondLevelExpandableListView extends ExpandableListView {

        public SecondLevelExpandableListView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //999999 is a size in pixels. ExpandableListView requires a maximum height in order to do measurement calculations.
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(999999, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public class SecondLevelAdapter extends BaseExpandableListAdapter {

        private Context context;
        ArrayList<HashMap<String, String>> catAry;
        GroupViewHolder groupViewHolder;
        ChildViewHolder childViewHolder;

        public SecondLevelAdapter(Context context, ArrayList<HashMap<String, String>> arrayList) {
            this.context = context;
            this.catAry = arrayList;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public int getGroupCount() {
            return catAry.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        private class GroupViewHolder {
            ImageView icon;
            TextView name;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.category_second_row, parent, false);
                groupViewHolder = new GroupViewHolder();

                groupViewHolder.name = (TextView) convertView.findViewById(R.id.name);
                groupViewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            HashMap<String, String> tempMap = catAry.get(groupPosition);

            groupViewHolder.name.setText(tempMap.get(Constants.TAG_NAME));

            if (getChildrenCount(groupPosition) > 0) {
                groupViewHolder.icon.setVisibility(View.VISIBLE);
            } else {
                groupViewHolder.icon.setVisibility(View.GONE);
            }

            if (isExpanded && getChildrenCount(groupPosition) > 0) {
                groupViewHolder.icon.setImageResource(R.drawable.minus);
            } else {
                groupViewHolder.icon.setImageResource(R.drawable.plus);
            }
            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        private class ChildViewHolder {
            TextView name;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.category_third_row, parent, false);
                childViewHolder = new ChildViewHolder();

                childViewHolder.name = (TextView) convertView.findViewById(R.id.name);

                convertView.setTag(childViewHolder);
            } else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }

            ArrayList<HashMap<String, String>> supercatAry = getSubCategory("supercat", catAry.get(groupPosition).get(Constants.TAG_SUPER_CATEGORY));
            HashMap<String, String> tempMap = supercatAry.get(childPosition);

            childViewHolder.name.setText(tempMap.get(Constants.TAG_NAME));

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return Integer.parseInt(catAry.get(groupPosition).get(Constants.TAG_SIZE));
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FantacyApplication.getInstance().getRequestQueue().cancelAll("tag");
    }
}
