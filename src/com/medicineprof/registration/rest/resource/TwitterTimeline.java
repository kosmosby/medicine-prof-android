package com.medicineprof.registration.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.medicineprof.registration.provider.ProfileConstants;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;


/**
 * Facade representing the Twitter Timeline data
 * 
 * @author hashbrown
 * 
 */
public class TwitterTimeline implements Resource {

	public static final Uri CONTENT_URI = Uri.parse("content://" + ProfileConstants.AUTHORITY
			+ "/" + ProfileConstants.TIMELINE_TABLE_NAME);

	private JSONArray timelineData;
	private List<Tweet> tweets;

	public TwitterTimeline(JSONArray timelineData) {
		this.timelineData = timelineData;
	}

	public List<Tweet> getTweets() {
		//lazy load
		if(tweets == null){
			tweets = new ArrayList<Tweet>();
			for (int i = 0; i < timelineData.length(); i++) {
				try {
					Tweet tweet = new Tweet(timelineData.getJSONObject(i));
					tweets.add(tweet);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return tweets;
	}

}
