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
import codepath.apps.twitter.activities.TimelineActivity;
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
		TimelineActivity activity = (TimelineActivity) getContext();
		String relativeTimestamp = formatTime(tweet.getCreatedAt(), activity.getUserUtcOffsetSecs());
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


	private String formatTime(String createdAt, int userOffset) {
		Date createdDate = null;
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy"); // Sun Mar 30 04:00:36 +0000 2014
		try {
			createdDate = formatter.parse(createdAt);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		long timeDiffMs = (currentDate.getTime() - createdDate.getTime()); // in ms
		long dynamicTimeDiff = timeDiffMs;
		if (dynamicTimeDiff < 6000) {
			return "moments ago"; // less than a minute ago
		}
		dynamicTimeDiff /= 6000; // in minutes
		if (dynamicTimeDiff < 60) {
			return (int)dynamicTimeDiff + " mins ago"; // less than an hour ago
		}
		dynamicTimeDiff /= 60; // in hours
		if (dynamicTimeDiff < 24) {
			return (int)dynamicTimeDiff + " hrs ago"; // less than 1 day ago
		}
		dynamicTimeDiff /= 24; // in days
		if (dynamicTimeDiff < 6) {
			return (int)dynamicTimeDiff + " days ago"; // less than 6 days ago
		}
		dynamicTimeDiff /= 7; // in weeks
		if (dynamicTimeDiff < 5) {
			return (int)dynamicTimeDiff + " weeks ago"; // less than 5 weeks ago
		}
		SimpleDateFormat friendlyFormatter = new SimpleDateFormat("MMM dd 'yy");
		return friendlyFormatter.format(new Date(timeDiffMs));
	}
}
