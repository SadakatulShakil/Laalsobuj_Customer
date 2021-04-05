package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.external.LinkEllipseTextView;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hitasoft on 25/5/17.
 */

public class AllComments extends AppCompatActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = AllComments.class.getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, writeComment, itemName;
    String feedId = "", itemId = "", itemTitle = "", image = "", from = "";
    RecyclerView commentList;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    CommentsViewAdapter commentsViewAdapter;
    ArrayList<HashMap<String, String>> commentsAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_main_layout);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        writeComment = (TextView) findViewById(R.id.writeComment);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        commentList = (RecyclerView) findViewById(R.id.commentList);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);

        title.setText(getString(R.string.all_comments));
        backBtn.setImageResource(R.drawable.close);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        writeComment.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        writeComment.setOnClickListener(this);

        from = (String) getIntent().getExtras().get("from");
        if (from != null && from.equals("feeds")) {
            feedId = (String) getIntent().getExtras().get("feedId");
        } else {
            itemId = (String) getIntent().getExtras().get("itemId");
        }
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");

        itemName.setText(itemTitle);
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        getComments(0);

        LinearLayoutManager commentsManager = new LinearLayoutManager(AllComments.this, LinearLayoutManager.VERTICAL, false);
        commentList.setLayoutManager(commentsManager);

        DividerItemDecoration commentDivider = new DividerItemDecoration(AllComments.this, commentsManager.getOrientation());
        commentDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));

        commentList.addItemDecoration(commentDivider);
        commentsViewAdapter = new CommentsViewAdapter(AllComments.this, commentsAry);
        commentList.setAdapter(commentsViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(commentsManager) {
            @Override
            public void onLoadMore(int current_page) {
                getComments(current_page * Constants.OVERALL_LIMIT);
            }
        };
        commentList.addOnScrollListener(mScrollListener);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getComments(final int offset) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerID = preferences.getString("customer_id", null);

        final String CallingAPI;
        if (from != null && from.equals("feeds"))
            CallingAPI = Constants.API_GET_FEED_COMMENTS;
        else
            CallingAPI = Constants.API_GET_COMMENTS;


        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getCommentsRes=" + res);
                    if (commentsAry.size() >= Constants.OVERALL_LIMIT && commentsAry.get(commentsAry.size() - 1) == null) {
                        commentsAry.remove(commentsAry.size() - 1);
                        commentsViewAdapter.notifyItemRemoved(commentsAry.size());
                    }
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray comments = json.getJSONArray(Constants.TAG_RESULT);
                        for (int p = 0; p < comments.length(); p++) {
                            JSONObject pobj = comments.getJSONObject(p);

                            HashMap<String, String> pmap = new HashMap<>();

                            String comment_id = DefensiveClass.optString(pobj, Constants.TAG_COMMENT_ID);
                            String comment = DefensiveClass.optString(pobj, Constants.TAG_COMMENT);
                            String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                            String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                            String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                            String full_name = DefensiveClass.optString(pobj, Constants.TAG_FULL_NAME);

                            pmap.put(Constants.TAG_FULL_NAME, full_name);
                            pmap.put(Constants.TAG_COMMENT_ID, comment_id);
                            pmap.put(Constants.TAG_COMMENT, comment);
                            pmap.put(Constants.TAG_USER_ID, user_id);
                            pmap.put(Constants.TAG_USER_IMAGE, user_image);
                            pmap.put(Constants.TAG_USER_NAME, user_name);

                            commentsAry.add(pmap);
                        }

                        if (mScrollListener != null) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(AllComments.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(AllComments.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }

                    progressLay.setVisibility(View.GONE);
                    if (commentsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_message);
                        nullText.setText(getString(R.string.no_comments));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    commentsViewAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    setErrorLayout();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    setErrorLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                    setErrorLayout();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getCommentsError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (commentsAry.size() >= Constants.OVERALL_LIMIT) {
                            commentsAry.add(null);
                            commentsViewAdapter.notifyItemInserted(commentsAry.size() - 1);
                        } else if (commentsAry.size() == 0) {
                            progressLay.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Map<String, String> map = new HashMap<String, String>();
                if (from != null && from.equals("feeds"))
                    map.put("feed_id", feedId);
                else
                    map.put("item_id", itemId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getCommentsParams=" + map);
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
        if (commentsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    public class CommentsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
                        if (from.equals("itemDetail")) {
                            Intent i = new Intent(AllComments.this, WriteComment.class);
                            i.putExtra("from", "allComment");
                            i.putExtra("to", "edit");
                            i.putExtra("commentId", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT_ID));
                            i.putExtra("comment", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT));
                            i.putExtra("itemId", itemId);
                            i.putExtra("image", image);
                            i.putExtra("itemTitle", itemTitle);
                            startActivityForResult(i, 3);
                        } else {
                            Intent i = new Intent(AllComments.this, WriteComment.class);
                            i.putExtra("from", "feedallComment");
                            i.putExtra("to", "edit");
                            i.putExtra("commentId", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT_ID));
                            i.putExtra("comment", Items.get(getAdapterPosition()).get(Constants.TAG_COMMENT));
                            i.putExtra("itemId", feedId);
                            i.putExtra("image", image);
                            i.putExtra("itemTitle", itemTitle);
                            startActivityForResult(i, 3);
                        }
                        break;
                    case R.id.delete:
                        deleteDialog(getAdapterPosition());
                        break;
                    case R.id.userImage:
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        startActivity(p);
                        break;
                }
            }
        }

        public CommentsViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

                viewHolder.userName.setText(tempMap.get(Constants.TAG_FULL_NAME));
                viewHolder.comment.setText(FantacyApplication.stripHtml(tempMap.get(Constants.TAG_COMMENT)).trim());
                String image = tempMap.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }

                if (tempMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                    viewHolder.edit.setVisibility(View.VISIBLE);
                    viewHolder.delete.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.edit.setVisibility(View.GONE);
                    viewHolder.delete.setVisibility(View.GONE);
                }

                MovementMethod m = viewHolder.comment.getMovementMethod();
                if ((m == null) || !(m instanceof LinkMovementMethod)) {
                    if (viewHolder.comment.getLinksClickable()) {
                        viewHolder.comment.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }

                viewHolder.comment.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {

                    @Override
                    public void onTextLinkClick(View textView, String clickedString) {
                        if (clickedString.contains("@")) {
                            Intent p = new Intent(context, Profile.class);
                            p.putExtra("userId", "");
                            p.putExtra("userName", clickedString.replace("@", ""));
                            startActivity(p);
                        }

                        if (clickedString.contains("#")) {
                            Intent i = new Intent(context, HashTag.class);
                            i.putExtra("key", clickedString.replace("#", ""));
                            startActivity(i);
                        }
                    }
                });
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

    public void deleteDialog(final int position) {
        final Dialog dialog = new Dialog(AllComments.this);
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
                deleteComment(position);
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

    private void deleteComment(final int position) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final ProgressDialog dialog = new ProgressDialog(AllComments.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final String CallingAPI;
        if (from.equals("feeds"))
            CallingAPI = Constants.API_DELETE_POST_COMMENT;
        else
            CallingAPI = Constants.API_DELETE_COMMENT;
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.i(TAG, "deleteCommentURL= " + CallingAPI);
                Log.v(TAG, "deleteCommentRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(AllComments.this, getString(R.string.comment_deleted_successfully), Toast.LENGTH_SHORT);
                        commentsAry.remove(position);
                        commentsViewAdapter.notifyItemRemoved(position);
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
                map.put("comment_id", commentsAry.get(position).get(Constants.TAG_COMMENT_ID));
                if (from.equals("feeds"))
                    map.put("feed_id", feedId);
                else
                    map.put("item_id", itemId);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 3) {
            nullLay.setVisibility(View.GONE);
            mScrollListener.resetpagecount();
            commentsAry.clear();
            commentsViewAdapter.notifyDataSetChanged();
            getComments(0);
        }
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
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
        if (from.equals("feeds")) {
            FeedsFragment.refreshFeeds = true;
        } else {
            Intent i = new Intent(AllComments.this, DetailActivity.class);
            setResult(RESULT_OK, i);
        }
    }

    @Override
    public void onClick(View v) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        switch (v.getId()) {
            case R.id.backBtn:
                if (from.equals("feeds")) {
                    FeedsFragment.refreshFeeds = true;
                } else {
                    Intent i = new Intent(AllComments.this, DetailActivity.class);
                    setResult(RESULT_OK, i);
                }
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.writeComment:
                if (accesstoken != null) {
                    Intent i = new Intent(AllComments.this, WriteComment.class);
                    if (from != null && from.equals("feeds")) {
                        i.putExtra("itemId", feedId);
                        i.putExtra("from", "feedallComment");
                    } else {
                        i.putExtra("from", "allComment");
                        i.putExtra("itemId", itemId);
                    }
                    i.putExtra("to", "add");
                    i.putExtra("image", image);
                    i.putExtra("itemTitle", itemTitle);
                    startActivityForResult(i, 3);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
        }
    }
}
