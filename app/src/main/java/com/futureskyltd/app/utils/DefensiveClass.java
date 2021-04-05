package com.futureskyltd.app.utils;

import org.json.JSONObject;

public class DefensiveClass {

	public static String optString(JSONObject json, String key) {
		if (json.isNull(key))
			return "";
		else
			return json.optString(key, "").trim();
	}
	
	public static String optInt(JSONObject json, String key) {
		if (json.isNull(key))
			return "0";
		else
			return json.optString(key, "0");
	}
	
	public static String optCurrency(JSONObject json, String key) {
		if (json.isNull(key))
			return "$";
		else
			return json.optString(key, "$");
	}
	
	
}
