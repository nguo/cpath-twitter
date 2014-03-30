package codepath.apps.twitter.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Tweet - model representing a tweet
 */
public class Tweet implements Serializable {
	private User user;
	private String body;
	private long id;
	private String createdAt;

	public String getBody() {
		return body;
	}

	public long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public static Tweet fromJson(JSONObject jsonObject) {
		Tweet tweet = new Tweet();
		try {
			tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
			tweet.body = jsonObject.getString("text");
			tweet.id = jsonObject.getLong("id");
			tweet.createdAt = jsonObject.getString("created_at");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tweet;
	}

	public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());
		for (int i=0; i<jsonArray.length(); i++) {
			JSONObject tweetJson;
			try {
				tweetJson = jsonArray.getJSONObject(i);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			Tweet tweet = Tweet.fromJson(tweetJson);
			if (tweet != null) {
				tweets.add(tweet);
			}
		}
		return tweets;
	}
}
