package codepath.apps.twitter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
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
	/** request code for compose activity */
	public static final int COMPOSE_REQUEST_CODE = 7;
	/** name of intent bundle that contains posted tweet's contents */
	public static final String POSTED_TWEET_EXTRA = "posted_tweet";

	// views
	private PullToRefreshListView lvTweets;
	private ProgressBar pbCenter;
	private LinearLayout llCompose;

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
		getOldTweets();
	}

	/** setups the views */
	private void setupViews() {
		llCompose = (LinearLayout) findViewById(R.id.llCompose);
		pbCenter = (ProgressBar) findViewById(R.id.pbCenter);
		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
		View footerView = getLayoutInflater().inflate(R.layout.lv_footer_item, null);
		lvTweets.addFooterView(footerView);
		adapter = new TweetsAdapter(this, tweetsList);
		lvTweets.setAdapter(adapter);
		toggleCenterProgressBar(true);
	}

	/** toggles the visibility of the listview and the progress bar */
	private void toggleCenterProgressBar(boolean showPb) {
		if (showPb) {
			lvTweets.setVisibility(View.INVISIBLE);
			pbCenter.setVisibility(View.VISIBLE);
		} else {
			pbCenter.setVisibility(View.INVISIBLE);
			lvTweets.setVisibility(View.VISIBLE);
		}
	}

	/** setups the listeners on the views */
	private void setupListeners() {
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				getOldTweets();
			}
		});
		// Set a listener to be invoked when the list should be refreshed.
		lvTweets.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				getNewTweets();
			}
		});
		lvTweets.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent me) {
				if (me.getAction() == MotionEvent.ACTION_UP) { //You might try with MotionEvent.ACTION_MOVE also, but you'll get way more calls.
					llCompose.setVisibility(View.VISIBLE);
				} else {
					llCompose.setVisibility(View.INVISIBLE);
				}
				return false;
			}
		});
	}

	/** makes request to get old tweets */
	private void getOldTweets() {
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
					long prevOldestTweetId = currentOldestTweetId;
					int posOfDuplicate = -1;
					for (int i=0; i < tweets.size(); i++) {
						long id = tweets.get(i).getId();
						if (currentOldestTweetId < 0 || id < currentOldestTweetId) {
							if (id == prevOldestTweetId) {
								posOfDuplicate = i; // duplicate entry. remove.
							} else {
								currentOldestTweetId = id; // otherwise set this id as the oldest
							}
						}
						// also update newest tweet ID so we can use this later when we get new tweets
						if (id > currentNewestTweetId) {
							currentNewestTweetId = id;
						}
					}
					if (posOfDuplicate >= 0) {
						tweets.remove(posOfDuplicate);
					}
					// show listview and refresh adapter
					toggleCenterProgressBar(false);
					adapter.addAll(tweets);
					// reset flag because we're no longer waiting for a response
					isFetchingTweets = false;
					if (areOlderTweetsWanted) {
						// if we previously wanted to get more tweets but was denied because of another request in process,
						// then we can now make the call to get more tweets
						getOldTweets();
						areOlderTweetsWanted = false;
					}
				}

				@Override
				public void onFailure(Throwable throwable, JSONObject jsonObject) {
					Toast.makeText(getBaseContext(), "Failed to get tweets -- searching for tweets too frequently.", Toast.LENGTH_LONG).show();
					Log.d("networking", "failed getMoreOldTweets:: " + jsonObject.toString());
				}
			});
		}
	}

	/** fetches newer tweets */
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
			Toast.makeText(getBaseContext(), "Posted", Toast.LENGTH_LONG).show();
		}
	}

	/** Callback for when the compose button is pressed */
	public void onCompose(View v) {
		Intent i = new Intent(this, ComposeActivity.class);
		startActivityForResult(i, COMPOSE_REQUEST_CODE);
	}
}