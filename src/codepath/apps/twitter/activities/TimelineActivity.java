package codepath.apps.twitter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

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

	/** the current id of the oldest tweet we pulled (must be positive) */
	private long currentOldestTweetId = -1;
	/** if true, then we are still awaiting a response from the previous tweets request */
	private boolean isFetchingTweets = false;
	/** if true, then we want to request more (older) tweets */
	private boolean areOlderTweetsWanted = false;

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
		adapter = new TweetsAdapter(this, tweets);
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
		if (isFetchingTweets) {
			areOlderTweetsWanted = true; // can't make request because we're still waiting for previous request to come back
		} else {
			// if no pending fetches, then make the request
			isFetchingTweets = true;
			TwitterApp.getRestClient().getHomeTimeline(currentOldestTweetId, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONArray jsonTweets) {
					ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
					// oldest tweet ID should be the lowest ID value
					for (int i=0; i < tweets.size(); i++) {
						long id = tweets.get(i).getId();
						if (id < currentOldestTweetId || currentOldestTweetId < 0) {
							currentOldestTweetId = id;
						}
					}
					// refresh adapter
					if (clearList) {
						adapter.clear();
					}
					adapter.addAll(tweets);
					// reset flag because we're no longer waiting for a response
					isFetchingTweets = false;
					if (areOlderTweetsWanted) {
						// if we previously wanted to get more tweets but was denied because of another request in process,
						// then we can now make the call to get more tweets
						getMoreTweets(false);
						areOlderTweetsWanted = false;
					}
				}

				@Override
				public void onFailure(Throwable throwable, JSONObject jsonObject) {
					Log.d("networking", "failed getMoreTweets:: " + jsonObject.toString());
				}
			});
		}
	}
}