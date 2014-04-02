package codepath.apps.twitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.models.ImageButtonData;
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
		// set tweet statuses
		setupFavButtons(tweet, v);
		ImageButton ibtnReply = (ImageButton) v.findViewById(R.id.ibtnReply);
		ibtnReply.setTag(tweet.getUser().getScreenName());
		// set tweeter's real name
		TextView tvRealName = (TextView) v.findViewById(R.id.tvRealName);
		tvRealName.setText(u.getName());
		// set tweeter's screenname
		TextView tvScreenName = (TextView) v.findViewById(R.id.tvScreenName);
		tvScreenName.setText("@" + u.getScreenName());
		// set tweet's relative timestamp
		TextView tvTimeStamp = (TextView) v.findViewById(R.id.tvTimestamp);
		tvTimeStamp.setText(getFormattedTime(tweet.getCreatedAt()));
		// set tweet body
		TextView tvTweetBody = (TextView) v.findViewById(R.id.tvTweetBody);
		setTextViewHTML(tvTweetBody, tweet.getBody());
		return v;
	}

	/** setups the favorite buttons */
	private void setupFavButtons(Tweet tweet, View v) {
		ImageButton ibtnFavorite = (ImageButton) v.findViewById(R.id.ibtnFavorite);
		ImageButton ibtnUnfavorite = (ImageButton) v.findViewById(R.id.ibtnUnfavorite);
		if (tweet.isFavorited()) {
			ibtnUnfavorite.setVisibility(View.INVISIBLE);
			ibtnFavorite.setVisibility(View.VISIBLE);
		} else {
			ibtnFavorite.setVisibility(View.INVISIBLE);
			ibtnUnfavorite.setVisibility(View.VISIBLE);
		}
		ibtnFavorite.setTag(new ImageButtonData(ibtnUnfavorite, tweet));
		ibtnUnfavorite.setTag(new ImageButtonData(ibtnFavorite, tweet));
	}

	/**
	 * Gets the formatted time (relative time) since the creation of this tweet until now
	 * @param createdAt		formatted absolute date of when the tweet was created
	 * @return the formatted time text for how long ago this tweet was tweeted (eg. "1 day ago")
	 */
	private String getFormattedTime(String createdAt) {
		Date createdDate;
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

		long dynamicTimeDiff = (currentDate.getTime() - createdDate.getTime()); // in ms
		if (dynamicTimeDiff < 60000) {
			return "just now"; // less than a minute ago
		}
		dynamicTimeDiff /= 60000; // in minutes
		if (dynamicTimeDiff < 60) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "m"); // less than an hour ago
		}
		dynamicTimeDiff /= 60; // in hours
		if (dynamicTimeDiff < 24) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "h"); // less than 1 day ago
		}
		dynamicTimeDiff /= 24; // in days
		if (dynamicTimeDiff < 7) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "d"); // less than 7 days ago
		}
		dynamicTimeDiff /= 7; // in weeks
		if (dynamicTimeDiff < 5) {
			return getFriendlyTimeTextHelper(dynamicTimeDiff, "w"); // less than 5 weeks ago
		}
		// if more than 5 weeks ago, then just show the simplified date for the tweet (eg. Mar 01 2013)
		SimpleDateFormat minimalFormatter = new SimpleDateFormat("MMM dd yyyy");
		return minimalFormatter.format(createdDate);
	}

	/**
	 * Returns a friendly time text (eg. "1m" or "2h")
	 * @param qty		number of units
	 * @param unit		string to pluralize
	 * @return
	 */
	private String getFriendlyTimeTextHelper(long qty, String unit) {
		return (int)qty + unit;
	}

	/**
	 * Setup clickable span with onClick listener for the string builder
	 * @param strBuilder	string builder to use
	 * @param span			span that contains url info
	 */
	protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
		int start = strBuilder.getSpanStart(span);
		int end = strBuilder.getSpanEnd(span);
		int flags = strBuilder.getSpanFlags(span);
		ClickableSpan clickable = new ClickableSpan() {
			public void onClick(View view) {
				// Do something with span.getURL() to handle the link click...
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(span.getURL()));
				getContext().startActivity(launchBrowser);
			}
		};
		strBuilder.setSpan(clickable, start, end, flags);
		strBuilder.removeSpan(span);
	}

	/**
	 * Makes the textview clickable with friendly html text
	 * @param tv		textview to set text for
	 * @param nonhtml	tweet body before href treatment
	 */
	protected void setTextViewHTML(TextView tv, String nonhtml) {
		// get friendly text char sequence
		CharSequence sequence = Html.fromHtml(nonhtml.replaceAll("(http(s?)://(t\\.co/(\\p{alnum}+)))", "<a href=\"$1\">$3</a>"));
		// create string builder
		SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
		URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
		for(URLSpan span : urls) {
			makeLinkClickable(strBuilder, span); // make the links clickable
		}
		// set the textview text
		tv.setText(strBuilder);
	}
}