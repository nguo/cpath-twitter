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
		// set tweeter's name in the format "name @screenname" with some styling
		TextView tvUserName = (TextView) v.findViewById(R.id.tvUserName);
		String formattedName = "<b>" + u.getName() + "</b>"
				+ "<small><font color='#777777'>@" + u.getScreenName() + "</font></small?";
		tvUserName.setText(Html.fromHtml(formattedName));
		// set tweet body
		TextView tvTweetBody = (TextView) v.findViewById(R.id.tvTweetBody);
		tvTweetBody.setText(Html.fromHtml(tweet.getBody()));
		return v;
	}
}
