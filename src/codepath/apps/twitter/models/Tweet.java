package codepath.apps.twitter.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tweet - model representing a tweet
 */
@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {
	@Column(name = "tweetId", unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
	private long tweetId;
	@Column(name = "user")
	private User user;
	@Column(name = "body")
	private String body;
	@Column(name = "createdAt")
	private String createdAt;
	@Column(name = "favorited")
	private boolean favorited;

	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}

	public Tweet(){
		super();
	}

	public String getBody() {
		return body;
	}

	public long getTweetId() {
		return tweetId;
	}

	public User getUser() {
		return user;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public boolean isFavorited() {
		return favorited;
	}

	/**
	 * Tries to set the tweet's user using a user already defind in the db
	* @return	true if successfully set the tweet's user to the db's user
	 */
	public boolean setUserUsingDb() {
		User dbUser = User.byUserId(user.getUserId());
		if (dbUser != null) {
			user = dbUser;
			return true;
		}
		return false;
	}

	public static Tweet fromJson(JSONObject jsonObject) {
		Tweet tweet = new Tweet();
		try {
			tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
			tweet.body = jsonObject.getString("text");
			tweet.tweetId = jsonObject.getLong("id");
			tweet.createdAt = jsonObject.getString("created_at");
			tweet.favorited = jsonObject.getBoolean("favorited");
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

	/********* activeandroid queries **********/

	public static Tweet byTweetId(long tweetId) {
		try {
			return new Select().from(Tweet.class).where("tweetId = ?", tweetId).executeSingle();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<Tweet> recentItems() {
		try {
			return new Select().from(Tweet.class).orderBy("tweetId DESC").execute();
		} catch (Exception e) {
			e.printStackTrace();
			return new LinkedList<Tweet>();
		}
	}
}
