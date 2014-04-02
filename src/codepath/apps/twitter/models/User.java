package codepath.apps.twitter.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * User - model for twitter user
 */
@Table(name = "Users")
public class User extends Model implements Serializable {
	@Column(name = "name")
	private String name;
	@Column(name = "screenName")
	private String screenName;
	@Column(name = "userId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long userId;
	@Column(name = "profileImageUrl")
	private String profileImageUrl;
	@Column(name = "numTweets")
	public int numTweets;

	public User() {
		super();
	}

	public long getUserId() {
		return userId;
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
			u.userId = json.getLong("id");
			u.profileImageUrl = json.getString("profile_image_url");
			u.numTweets = json.getInt("statuses_count");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return u;
	}

	/********* activeandroid queries **********/

	public static User byUserId(long userId) {
		try {
			return new Select().from(User.class).where("userId = ?", userId).executeSingle();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
