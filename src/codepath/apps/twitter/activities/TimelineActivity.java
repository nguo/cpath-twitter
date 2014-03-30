package codepath.apps.twitter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import eu.erikw.PullToRefreshListView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * TimelineActivity - home timeline screen
 */
public class TimelineActivity extends Activity {
	// views
	private PullToRefreshListView lvTweets;

	/** list of tweets */
	private LinkedList<Tweet> tweetsList = new LinkedList<Tweet>();
	/** tweets adapter */
	private TweetsAdapter adapter;

	/** the current id of the oldest tweet we pulled (must be positive) */
	private long currentOldestTweetId = -1;
	private long currentNewestTweetId = -1;
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
		getOldTweets(true);
	}

	/** setups the views */
	private void setupViews() {
		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
		adapter = new TweetsAdapter(this, tweetsList);
		lvTweets.setAdapter(adapter);
	}

	/** setups the listeners on the views */
	private void setupListeners() {
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				getOldTweets(false);
			}
		});
		// Set a listener to be invoked when the list should be refreshed.
		lvTweets.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				getNewTweets();
			}
		});
	}

	/** makes request to get more tweets */
	private void getOldTweets(final boolean clearList) {
		if (isFetchingTweets) {
			areOlderTweetsWanted = true; // can't make request because we're still waiting for previous request to come back
		} else {
			// if no pending fetches, then make the request
			isFetchingTweets = true;
			TwitterApp.getRestClient().getHomeTimeline(currentOldestTweetId, -1, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONArray jsonTweets) {
					ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
					// oldest tweet ID should be the lowest ID value
					long updatedOldestTweetId = currentOldestTweetId;
					int posOfDuplicate = -1;
					for (int i=0; i < tweets.size(); i++) {
						long id = tweets.get(i).getId();
						if (updatedOldestTweetId < 0 || id < updatedOldestTweetId) {
							if (id == currentOldestTweetId) {
								// duplicate entry. remove.
								posOfDuplicate = i;
							} else {
								updatedOldestTweetId = id;
							}
						}
						if (id > currentNewestTweetId) {
							currentNewestTweetId = id;
						}
					}
					currentOldestTweetId = updatedOldestTweetId;
					if (posOfDuplicate >= 0) {
						tweets.remove(posOfDuplicate);
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
						getOldTweets(false);
						areOlderTweetsWanted = false;
					}
				}

				@Override
				public void onFailure(Throwable throwable, JSONObject jsonObject) {
					Log.d("networking", "failed getMoreOldTweets:: " + jsonObject.toString());
				}
			});
		}
	}

	private void getNewTweets() {
		TwitterApp.getRestClient().getHomeTimeline(-1, currentNewestTweetId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
				// newest tweet ID should be the highest ID value
				for (int i = 0; i < tweets.size(); i++) {
					long id = tweets.get(i).getId();
					if (id > currentNewestTweetId) {
						currentNewestTweetId = id;
					}
				}
				tweetsList.addAll(0, tweets);
				adapter.notifyDataSetChanged();
				lvTweets.onRefreshComplete();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				lvTweets.onRefreshComplete();
				Log.d("networking", "failed getNewTweets:: " + jsonObject.toString());
			}
		});
	}
}