package codepath.apps.twitter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;

import java.util.ArrayList;

/**
 * TimelineActivity - home timeline screen
 */
public class TimelineActivity extends Activity {
	// views
	private ListView lvTweets;

	/** list of tweets */
	private ArrayList<Tweet> tweets = new ArrayList<Tweet>();
	/** tweets adapter */
	private TweetsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupViews();
		setupListeners();
		getMoreTweets(true);
	}

	/** setups the views */
	private void setupViews() {
		lvTweets = (ListView) findViewById(R.id.lvTweets);
		adapter = new TweetsAdapter(getBaseContext(), tweets);
		lvTweets.setAdapter(adapter);
	}

	/** setups the listeners on the views */
	private void setupListeners() {
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				getMoreTweets(false);
			}
		});
	}

	/** makes request to get more tweets */
	private void getMoreTweets(final boolean clearList) {
		TwitterApp.getRestClient().getHomeTimeline(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				if (clearList) {
					adapter.clear();
				}
				adapter.addAll(Tweet.fromJson(jsonTweets));
			}
		});
	}
}