package codepath.apps.twitter.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Tweet - model representing a tweet
 */
public class Tweet extends BaseModel{
	private User user;

	public User getUser() {
		return user;
	}

	public String getBody() {
		return getString("text");
	}

	public long getId() {
		return getLong("id");
	}

	public boolean isFavorited() {
		return getBoolean("favorited");
	}

	public boolean isRetweeted() {
		return getBoolean("retweeted");
	}

	public String getCreatedAt() {
		return getString("created_at");
	}

	public static Tweet fromJson(JSONObject jsonObject) {
		Tweet tweet = new Tweet();
		try {
			tweet.jsonObject = jsonObject;
			tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
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
