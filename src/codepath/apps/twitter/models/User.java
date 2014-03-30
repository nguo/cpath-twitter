package codepath.apps.twitter.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User - model for twitter user
 */
public class User extends BaseModel {
	public String getName() {
		return getString("name");
	}

	public long getId() {
		return getLong("id");
	}

	public String getScreenName() {
		return getString("screen_name");
	}

	public String getProfileImageUrl() {
		return getString("profile_image_url");
	}

	public String getProfileBackgroundImageUrl() {
		return getString("profile_background_image_url");
	}

	public int getNumTweets() {
		return getInt("statuses_count");
	}

	public int getFollowersCount() {
		return getInt("followers_count");
	}

	public int getUtcOffsetSecs() {
		int offset = 0;
		JSONObject timezone = getJSONObject("time_zone");
		if (timezone != null) {
			try {
				offset = timezone.getInt("utc_offset");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return offset;
	}

	public static User fromJson(JSONObject json) {
		User u = new User();
		try {
			u.jsonObject = json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return u;
	}

	public static User fromJsonString(String jsonString) {
		User u = new User();
		try {
			u.jsonObject = new JSONObject(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return u;
	}
}
