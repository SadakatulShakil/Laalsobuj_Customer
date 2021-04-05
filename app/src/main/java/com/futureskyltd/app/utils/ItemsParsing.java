package com.futureskyltd.app.utils;

import android.content.Context;

import com.futureskyltd.app.helper.DatabaseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 15/5/17.
 */

public class ItemsParsing {

    Context context;

    public ItemsParsing() {

    }

    public ItemsParsing(Context context) {
        this.context = context;
    }

    public ArrayList<HashMap<String, String>> getItems(JSONArray items) {
        ArrayList<HashMap<String, String>> Items = new ArrayList<HashMap<String, String>>();
        try {
            if(items != null){
                DatabaseHandler helper = DatabaseHandler.getInstance(context);

                for (int i = 0; i < items.length(); i++){
                    JSONObject temp = items.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();

                    String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                    String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                    String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);
                    String mainprice = DefensiveClass.optString(temp, Constants.TAG_MAIN_PRICE);
                    String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                    String deal_enabled = DefensiveClass.optString(temp, Constants.TAG_DEAL_ENABLED);
                    String discount_percentage = DefensiveClass.optString(temp, Constants.TAG_DISCOUNT_PERCENTAGE);
                    String valid_till = DefensiveClass.optString(temp, Constants.TAG_VALID_TILL);
                    String quantity = DefensiveClass.optInt(temp, Constants.TAG_QUANTITY);
                    String cod = DefensiveClass.optString(temp, Constants.TAG_COD);
                    String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                    String likeCount=DefensiveClass.optString(temp,Constants.TAG_LIKE_COUNT);
                    String report = DefensiveClass.optString(temp, Constants.TAG_REPORT);
                    String reward_points = DefensiveClass.optString(temp, Constants.TAG_REWARD_POINTS);
                    String share_seller = DefensiveClass.optString(temp, Constants.TAG_SHARE_SELLER);
                    String share_user = DefensiveClass.optString(temp, Constants.TAG_SHARE_USER);
                    String approve = DefensiveClass.optString(temp, Constants.TAG_APPROVE);
                    String buy_type = DefensiveClass.optString(temp, Constants.TAG_BUY_TYPE);
                    String affiliate_link = DefensiveClass.optString(temp, Constants.TAG_AFFILIATE_LINK);
                    String shipping_time = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
                    String product_url = DefensiveClass.optString(temp, Constants.TAG_PRODUCT_URL);
                    String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE).replace("original", "thumb350");
                    String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                    String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                    String fbshare_discount = DefensiveClass.optString(temp, Constants.TAG_FBSHARE_DISCOUNT);
                    String description = DefensiveClass.optString(temp, Constants.TAG_ITEM_DESCRIPTION);

                    map.put(Constants.TAG_ID, id);
                    map.put(Constants.TAG_ITEM_TITLE, item_title);
                    map.put(Constants.TAG_ITEM_DESCRIPTION, description);
                    map.put(Constants.TAG_CURRENCY, currency);
                    map.put(Constants.TAG_MAIN_PRICE, mainprice);
                    map.put(Constants.TAG_PRICE, price);
                    map.put(Constants.TAG_DEAL_ENABLED, deal_enabled);
                    map.put(Constants.TAG_DISCOUNT_PERCENTAGE, discount_percentage);
                    map.put(Constants.TAG_VALID_TILL, valid_till);
                    map.put(Constants.TAG_QUANTITY, quantity);
                    map.put(Constants.TAG_COD, cod);
                    map.put(Constants.TAG_LIKED, liked);
                    map.put(Constants.TAG_LIKE_COUNT,likeCount);
                    map.put(Constants.TAG_REPORT, report);
                    map.put(Constants.TAG_REWARD_POINTS, reward_points);
                    map.put(Constants.TAG_SHARE_SELLER, share_seller);
                    map.put(Constants.TAG_SHARE_USER, share_user);
                    map.put(Constants.TAG_APPROVE, approve);
                    map.put(Constants.TAG_BUY_TYPE, buy_type);
                    map.put(Constants.TAG_AFFILIATE_LINK, affiliate_link);
                    map.put(Constants.TAG_SHIPPING_TIME, shipping_time);
                    map.put(Constants.TAG_PRODUCT_URL, product_url);
                    map.put(Constants.TAG_IMAGE, image);
                    map.put(Constants.TAG_HEIGHT, height);
                    map.put(Constants.TAG_WIDTH, width);
                    map.put(Constants.TAG_FBSHARE_DISCOUNT, fbshare_discount);

                    //for suggestitems
                    ArrayList<HashMap<String, String>> child_items = new ArrayList<HashMap<String, String>>();
                    if(temp.has("related_items")){
                        JSONArray childArray = temp.getJSONArray("related_items");

                        map.put(Constants.TAG_SUGGESTCHILD,childArray.toString());
                    }

                    Items.add(map);

                    helper.addItemDetails(id, liked,likeCount,report, share_user);
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return Items;
    }

    public ArrayList<HashMap<String, String>> getSuggestedItems(JSONArray items) {
        ArrayList<HashMap<String, String>> Items = new ArrayList<HashMap<String, String>>();
        try {
            if(items != null){
                DatabaseHandler helper = DatabaseHandler.getInstance(context);

                for (int i = 0; i < items.length(); i++){
                    JSONObject jsonObject = items.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<>();


                    //for suggestitems
                    ArrayList<HashMap<String, String>> child_items = new ArrayList<HashMap<String, String>>();
                    if(jsonObject.has("related_items")){
                        JSONArray childArray = jsonObject.getJSONArray("related_items");
                        JSONObject temp = childArray.getJSONObject(0);

                        if(childArray.length()>0){

                            String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                            String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                            String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);
                            String mainprice = DefensiveClass.optString(temp, Constants.TAG_MAIN_PRICE);
                            String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                            String deal_enabled = DefensiveClass.optString(temp, Constants.TAG_DEAL_ENABLED);
                            String discount_percentage = DefensiveClass.optString(temp, Constants.TAG_DISCOUNT_PERCENTAGE);
                            String valid_till = DefensiveClass.optString(temp, Constants.TAG_VALID_TILL);
                            String quantity = DefensiveClass.optInt(temp, Constants.TAG_QUANTITY);
                            String cod = DefensiveClass.optString(temp, Constants.TAG_COD);
                            String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                            String likeCount=DefensiveClass.optString(temp,Constants.TAG_LIKE_COUNT);
                            String report = DefensiveClass.optString(temp, Constants.TAG_REPORT);
                            String reward_points = DefensiveClass.optString(temp, Constants.TAG_REWARD_POINTS);
                            String share_seller = DefensiveClass.optString(temp, Constants.TAG_SHARE_SELLER);
                            String share_user = DefensiveClass.optString(temp, Constants.TAG_SHARE_USER);
                            String approve = DefensiveClass.optString(temp, Constants.TAG_APPROVE);
                            String buy_type = DefensiveClass.optString(temp, Constants.TAG_BUY_TYPE);
                            String affiliate_link = DefensiveClass.optString(temp, Constants.TAG_AFFILIATE_LINK);
                            String shipping_time = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
                            String product_url = DefensiveClass.optString(temp, Constants.TAG_PRODUCT_URL);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE).replace("original", "thumb350");
                            String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                            String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                            String fbshare_discount = DefensiveClass.optString(temp, Constants.TAG_FBSHARE_DISCOUNT);
                            String description = DefensiveClass.optString(temp, Constants.TAG_ITEM_DESCRIPTION);

                            map.put(Constants.TAG_ID, id);
                            map.put(Constants.TAG_ITEM_TITLE, item_title);
                            map.put(Constants.TAG_ITEM_DESCRIPTION, description);
                            map.put(Constants.TAG_CURRENCY, currency);
                            map.put(Constants.TAG_MAIN_PRICE, mainprice);
                            map.put(Constants.TAG_PRICE, price);
                            map.put(Constants.TAG_DEAL_ENABLED, deal_enabled);
                            map.put(Constants.TAG_DISCOUNT_PERCENTAGE, discount_percentage);
                            map.put(Constants.TAG_VALID_TILL, valid_till);
                            map.put(Constants.TAG_QUANTITY, quantity);
                            map.put(Constants.TAG_COD, cod);
                            map.put(Constants.TAG_LIKED, liked);
                            map.put(Constants.TAG_LIKE_COUNT,likeCount);
                            map.put(Constants.TAG_REPORT, report);
                            map.put(Constants.TAG_REWARD_POINTS, reward_points);
                            map.put(Constants.TAG_SHARE_SELLER, share_seller);
                            map.put(Constants.TAG_SHARE_USER, share_user);
                            map.put(Constants.TAG_APPROVE, approve);
                            map.put(Constants.TAG_BUY_TYPE, buy_type);
                            map.put(Constants.TAG_AFFILIATE_LINK, affiliate_link);
                            map.put(Constants.TAG_SHIPPING_TIME, shipping_time);
                            map.put(Constants.TAG_PRODUCT_URL, product_url);
                            map.put(Constants.TAG_IMAGE, image);
                            map.put(Constants.TAG_HEIGHT, height);
                            map.put(Constants.TAG_WIDTH, width);
                            map.put(Constants.TAG_FBSHARE_DISCOUNT, fbshare_discount);
                        }
                        childArray.remove(0);
                        map.put(Constants.TAG_SUGGESTCHILD,childArray.toString());
                    }

                    Items.add(map);

                    //helper.addItemDetails(id, liked,likeCount,report, share_user);
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return Items;
    }

    public ArrayList<HashMap<String, String>> getQuestions(JSONArray questionsArray) {
        ArrayList<HashMap<String, String>> questionList = new ArrayList<HashMap<String, String>>();
        try {

            for (int p = 0; p < questionsArray.length(); p++) {
                JSONObject pobj = questionsArray.getJSONObject(p);

                HashMap<String, String> pmap = new HashMap<>();

                String question_id = DefensiveClass.optString(pobj, Constants.TAG_ID);
                String question = DefensiveClass.optString(pobj, Constants.TAG_QUESTION);
                String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                String answer_count =DefensiveClass.optString(pobj, Constants.TAG_ANSWER_COUNT);
                String parent_id = "", answer = "";

                if(pobj.has("answer")) {
                    JSONArray answerArray = pobj.getJSONArray("answer");

                    if (answerArray.length() > 0) {
                        JSONObject ansobject = answerArray.getJSONObject(0);
                        ;
                        answer = DefensiveClass.optString(ansobject, "answer");
                        parent_id = DefensiveClass.optString(ansobject, "parent_id");
                    }

                }
                pmap.put(Constants.TAG_ID, question_id);
                pmap.put(Constants.TAG_QUESTION, question);
                pmap.put(Constants.TAG_USER_ID, user_id);
                pmap.put(Constants.TAG_USER_NAME, user_name);
                pmap.put(Constants.TAG_ANSWER, answer);
                pmap.put(Constants.TAG_ANSWER_COUNT,answer_count);
                pmap.put(Constants.TAG_PARENT_ID, parent_id);
                questionList.add(pmap);
            }

        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return questionList;
    }

    public ArrayList<HashMap<String, String>> getReviews(JSONArray reviewArray) {
        ArrayList<HashMap<String, String>> questionList = new ArrayList<HashMap<String, String>>();
        try {

            for (int p = 0; p < reviewArray.length(); p++) {
                JSONObject pobj = reviewArray.getJSONObject(p);

                HashMap<String, String> pmap = new HashMap<>();

                String review_id = DefensiveClass.optString(pobj, Constants.TAG_ID);
                String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                String review = DefensiveClass.optString(pobj, Constants.TAG_REVIEW);
                String rating = DefensiveClass.optString(pobj, Constants.TAG_RATING);

                pmap.put(Constants.TAG_ID, review_id);
                pmap.put(Constants.TAG_USER_ID, user_id);
                pmap.put(Constants.TAG_USER_NAME, user_name);
                pmap.put(Constants.TAG_USER_IMAGE, user_image);
                pmap.put(Constants.TAG_RATING, rating);
                pmap.put(Constants.TAG_REVIEW,review);
                questionList.add(pmap);
            }

        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return questionList;
    }
}
