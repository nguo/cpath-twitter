package codepath.apps.twitter.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.models.Tweet;
import codepath.apps.twitter.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TweetsAdapter - adapter for tweet items
 */
public class TweetsAdapter extends ArrayAdapter<Tweet> {
	/** constructor */
	public TweetsAdapter(Context context, List<Tweet> tweets) {
		super(context, 0, tweets);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			v = inflater.inflate(R.layout.tweet_item, null);
		}
		Tweet tweet = getItem(position);
		User u = tweet.getUser();
		// load tweeter's profile image
		ImageView ivProfile = (ImageView) v.findViewById(R.id.ivProfile);
		ImageLoader.getInstance().displayImage(tweet.getUser().getProfileImageUrl(), ivProfile);
		// set tweet time
		String relativeTimestamp = getFormattedTime(tweet.getCreatedAt());
		// set tweeter's name in the format "name @screenname" with some styling
		TextView tvUserName = (TextView) v.findViewById(R.id.tvUserName);
		String formattedName = "<b>" + u.getName() + "</b>"
				+ "<small><font color='#777777'>@" + u.getScreenName() + "</font></small>"
				+ "<b>" + relativeTimestamp + "</b>";
		tvUserName.setText(Html.fromHtml(formattedName));
		// set tweet body
		TextView tvTweetBody = (TextView) v.findViewById(R.id.tvTweetBody);
		tvTweetBody.setText(Html.fromHtml(tweet.getBody()));
		return v;
	}

	/**
	 * Gets the formatted time (relative time) since the creation of this tweet until now
	 * @param createdAt		formatted absolute date of when the tweet was created
	 * @return the formatted time text for how long ago this tweet was tweeted (eg. "1 day ago")
	 */
	private String getFormattedTime(String createdAt) {
		Date createdDate = null;
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy"); // eg. Sun Mar 30 04:00:36 +0000 2014
		try {
			createdDate = formatter.parse(createdAt);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		if (createdDate == null) {
			return ""; // for some reason we couldn't properly parse the created date
		}
		long timeDiffMs = (currentDate.getTime() - createdDate.getTime()); // in ms
		long dynamicTimeDiff = timeDiffMs;
		if (dynamicTimeDiff < 6000) {
			return "moments ago"; // less than a minute ago
		}
		dynamicTimeDiff /= 6000; // in minutes
		if (dynamicTimeDiff < 60) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "min"); // less than an hour ago
		}
		dynamicTimeDiff /= 60; // in hours
		if (dynamicTimeDiff < 24) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "hr"); // less than 1 day ago
		}
		dynamicTimeDiff /= 24; // in days
		if (dynamicTimeDiff < 6) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "day"); // less than 6 days ago
		}
		dynamicTimeDiff /= 7; // in weeks
		if (dynamicTimeDiff < 5) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "week"); // less than 5 weeks ago
		}
		// if more than 5 weeks ago, then just show the simplified date for the tweet (eg. Mar 01 '13)
		SimpleDateFormat friendlyFormatter = new SimpleDateFormat("MMM dd 'yy");
		return friendlyFormatter.format(new Date(timeDiffMs));
	}

	/**
	 * Returns a friendly, correctly-pluralized time text (eg. 1 min ago, 2 hrs ago)
	 * @param qty		number of units
	 * @param unit		string to pluralize
	 * @return
	 */
	private String getFriendlyTimeTextHelper(long qty, String unit) {
		int intQty = (int)qty;
		if (intQty >= 2) { // because we will cast this value to int, we're going to
			unit += "s";
		}
		return intQty + " " + unit + " ago";
	}
}
