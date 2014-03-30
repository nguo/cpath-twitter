package codepath.apps.twitter.models;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * User - model for twitter user
 */
public class User implements Serializable {
	private String name;
	private String screenName;
	private long id;
	private String profileImageUrl;
	public int numTweets;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getNumTweets() {
		return numTweets;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public String getScreenName() {
		return screenName;
	}

	public static User fromJson(JSONObject json) {
		User u = new User();
		try {
			u.name = json.getString("name");
			u.screenName = json.getString("screen_name");
			u.id = json.getLong("id");
			u.profileImageUrl = json.getString("profile_image_url");
			u.numTweets = json.getInt("statuses_count");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return u;
	}
}
