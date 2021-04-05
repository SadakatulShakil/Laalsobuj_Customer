package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.external.URLSpanNoUnderline;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 26/5/17.
 */

public class OtherAnswers extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, itemName;
    String userId = "", itemId = "", question_id = "", itemTitle = "",  image = "", shopid, selectedQuery="", buyertype = "";
    RecyclerView questionView;
    RelativeLayout progressLay, nullLay, bottomlay;
    EditText messageEdit, subject;
    ImageView nullImage;
    TextView nullText;
    View lineview;
    LinearLayout contentLay;
    Spinner querySpin;
    View subjectView;
    ProgressDialog loaderdialog;
    ArrayList<HashMap<String, String>> questionList = new ArrayList<HashMap<String, String>>();
    QuestionViewAdapter questionViewAdapter;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SharedPreferences preferences;
    private String accesstoken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_answer);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questionView = (RecyclerView)findViewById(R.id.commentList);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        title = (TextView) findViewById(R.id.title);
        itemName = (TextView) findViewById(R.id.itemName);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        bottomlay = (RelativeLayout) findViewById(R.id.bottomlay);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        contentLay = (LinearLayout) findViewById(R.id.contentLay);
        lineview =(View)findViewById(R.id.line);

        backBtn.setImageResource(R.drawable.back);

        contentLay.setBackgroundColor(getResources().getColor(R.color.white));
        itemName.setTextColor(getResources().getColor(R.color.textPrimary));
        contentLay.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        bottomlay.setVisibility(View.VISIBLE);
        lineview.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        bottomlay.setOnClickListener(this);

        userId = (String) getIntent().getExtras().get("userId");
        itemId = (String) getIntent().getExtras().get("itemId");
        question_id = (String) getIntent().getExtras().get("question_id");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");
        buyertype = (String) getIntent().getExtras().get("buyertype");

        Log.e("dataAns","-questionid "+question_id+" itemid "+itemId);

        title.setText(getResources().getString(R.string.allanswers));
//        String titleremovespace = itemTitle.replace("\n", "").replaceAll("\\<[^>]*>","").replace("\r", "");
//        String titleFinal = html2text(titleremovespace);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            itemName.setText(""+Html.fromHtml(itemTitle,Html.FROM_HTML_MODE_LEGACY));
        } else {
            itemName.setText(""+Html.fromHtml(itemTitle));
        }
        itemName.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(itemName);*/

        itemName.setText((new HtmlSpanner()).fromHtml(itemTitle));
        itemName.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(itemName);


        if(userId.equalsIgnoreCase(GetSet.getUserId())){
            bottomlay.setVisibility(View.GONE);
        }else {

            if (buyertype.equalsIgnoreCase("buyertype")){
                bottomlay.setVisibility(View.VISIBLE);
            }else bottomlay.setVisibility(View.GONE);

        }

        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        loaderdialog = new ProgressDialog(OtherAnswers.this);
        loaderdialog.setMessage(getString(R.string.pleasewait));
        loaderdialog.setCancelable(false);
        loaderdialog.setCanceledOnTouchOutside(false);
        loaderdialog.show();
        getQuestions(0);


        LinearLayoutManager commentsManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        questionView.setLayoutManager(commentsManager);

        DividerItemDecoration commentDivider = new DividerItemDecoration(this, commentsManager.getOrientation());
        commentDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));

        questionView.addItemDecoration(commentDivider);
        questionViewAdapter = new QuestionViewAdapter(this, questionList);
        questionView.setAdapter(questionViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(commentsManager) {
            @Override
            public void onLoadMore(int current_page) {
                getQuestions(current_page * Constants.OVERALL_LIMIT);
            }
        };
        questionView.addOnScrollListener(mScrollListener);

        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
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


    private void getQuestions(final int offset) {
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_READOTHERANSWER, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (questionList.size() >= Constants.OVERALL_LIMIT && questionList.get(questionList.size() - 1) == null) {
                        questionList.remove(questionList.size() - 1);
                        questionViewAdapter.notifyItemRemoved(questionList.size());
                    }

                    if (loaderdialog.isShowing()) {
                        loaderdialog.dismiss();
                    }
                    Log.v(TAG ,"addproductfaq="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                    if (status.equalsIgnoreCase("true")) {
                        JSONArray questionsArray = json.getJSONArray(Constants.TAG_RESULT);
                        for (int p = 0; p < questionsArray.length(); p++) {
                            JSONObject pobj = questionsArray.getJSONObject(p);

                            HashMap<String, String> pmap = new HashMap<>();

                            String question_id = DefensiveClass.optString(pobj, Constants.TAG_ID);
                            String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                            String store_id = DefensiveClass.optString(pobj, Constants.TAG_STORE_ID);
                            String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                            String answer = "";
                            answer = DefensiveClass.optString(pobj, Constants.TAG_ANSWER);

                            pmap.put(Constants.TAG_ID, question_id);
                            pmap.put(Constants.TAG_USER_ID, user_id);
                            pmap.put(Constants.TAG_USER_NAME, user_name);
                            pmap.put(Constants.TAG_STORE_ID, store_id);
                            pmap.put(Constants.TAG_ANSWER, answer);
                            questionList.add(pmap);
                        }

                        if (mScrollListener != null && questionList.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                            Log.e("sendChatParams","-loading false");
                        }
                        if(questionsArray.length()==0)
                            mScrollListener.setLoading(true);

                    } else {
                        FantacyApplication.showToast(OtherAnswers.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                    }

                    progressLay.setVisibility(View.GONE);
                    if (questionList.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_message);
                        nullText.setText(getString(R.string.no_ans));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    questionViewAdapter.notifyDataSetChanged();


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
                if (loaderdialog.isShowing()) {
                    loaderdialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (questionList.size() >= Constants.OVERALL_LIMIT) {
                            questionList.add(null);
                            questionViewAdapter.notifyItemInserted(questionList.size() - 1);
                        } else if (questionList.size() == 0) {
                            progressLay.setVisibility(View.VISIBLE);
                        }

                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });
                Map<String, String> map = new HashMap<String, String>();
                map.put("parent_id", question_id);
                map.put("offset", ""+Integer.toString(offset));
                map.put("limit", ""+Integer.toString(Constants.OVERALL_LIMIT));
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
        FantacyApplication.getInstance().addToRequestQueue(req);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.bottomlay:
                if (accesstoken != null) {
                    if(!itemTitle.equalsIgnoreCase("")) {
                       if (buyertype.equalsIgnoreCase("buyertype")){
                        Intent login = new Intent(OtherAnswers.this, PostQuestion.class);
                        login.putExtra("itemId", itemId);
                        login.putExtra("itemTitle", itemTitle);
                        login.putExtra("question", itemTitle);
                        login.putExtra("question_id", question_id);
                        login.putExtra("image", "");
                        startActivityForResult(login, 3);
                    } else{
                            Toast.makeText(getApplicationContext(),"You haven't purchased this product yet",Toast.LENGTH_SHORT).show();
                    }
                    }else{
                        Intent login = new Intent(OtherAnswers.this, PostQuestion.class);
                        login.putExtra("itemId", itemId);
                        login.putExtra("itemTitle", itemTitle);
                        login.putExtra("question", itemTitle);
                        login.putExtra("question_id", itemId);
                        login.putExtra("image", "");
                        startActivityForResult(login, 3);
                    }

                }else {
                    Intent login = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(login);
                }
                break;
        }
    }



    public class QuestionViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

            TextView  answer, readother, user_name;

            public MyViewHolder(View view) {
                super(view);
                answer = (TextView) view.findViewById(R.id.answer);
                user_name = (TextView) view.findViewById(R.id.user_name);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                }
            }

        }

        public QuestionViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.answer_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof MyViewHolder) {
                final HashMap<String, String> map = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    viewHolder.answer.setText("A: "+Html.fromHtml(map.get(Constants.TAG_ANSWER),Html.FROM_HTML_MODE_LEGACY));
                } else {
                    viewHolder.answer.setText("A: "+Html.fromHtml(map.get(Constants.TAG_ANSWER)));
                }
                viewHolder.answer.setMovementMethod(LinkMovementMethod.getInstance());
                stripUnderlines(viewHolder.answer);
*/
                viewHolder.answer.setText((new HtmlSpanner()).fromHtml(map.get(Constants.TAG_ANSWER)));
                viewHolder.answer.setMovementMethod(LinkMovementMethod.getInstance());
                stripUnderlines(viewHolder.answer);

                viewHolder.user_name.setText(map.get(Constants.TAG_USER_NAME));

                if(map.get(Constants.TAG_ANSWER).equalsIgnoreCase("")) {
                    viewHolder.answer.setVisibility(View.GONE);
                }
                else {
                    viewHolder.answer.setVisibility(View.VISIBLE);
                }

                viewHolder.user_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(map.get(Constants.TAG_STORE_ID).equalsIgnoreCase("")) {
                            Intent p = new Intent(context, Profile.class);
                            p.putExtra("userId", map.get(Constants.TAG_USER_ID));
                            startActivity(p);
                        }else{
                            Intent i = new Intent(OtherAnswers.this, StoreProfile.class);
                            i.putExtra("storeId",map.get(Constants.TAG_STORE_ID));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode);
         if (resultCode == RESULT_OK && requestCode == 3) {
             questionList.clear();
             questionViewAdapter.notifyDataSetChanged();
             getQuestions(0);
        }

    }



    Dialog dialog;

    public void open(String quesStr, final String quesiontId) {
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
                writeAnswer(itemId, quesiontId, answerEdit.getText().toString());
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


    private void writeAnswer(final String itemId, final String parentId, final String content) {
        final ProgressDialog dialog = new ProgressDialog(OtherAnswers.this);
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
                        FantacyApplication.showToast(OtherAnswers.this, getString(R.string.message_send_successfully), Toast.LENGTH_SHORT);
                    } else {
                        FantacyApplication.showToast(OtherAnswers.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                map.put("user_id", "289");
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


}
