package codepath.apps.twitter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.models.Tweet;
import codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import eu.erikw.PullToRefreshListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * TimelineActivity - home timeline screen
 */
public class TimelineActivity extends Activity {
	/** request code for compose activity */
	public static final int COMPOSE_REQUEST_CODE = 7;
	/** name of intent bundle that contains posted tweet's contents */
	public static final String POSTED_TWEET_EXTRA = "posted_tweet";
	/** name of intent bundle that contains user's name */
	public static final String USER_NAME_EXTRA = "user_name";
	/** name of intent bundle that contains user's screenname */
	public static final String USER_SCREEN_NAME_EXTRA = "user_screen_name";
	/** name of the intent bundle that contains user's profile image url */
	public static final String USER_PROFILE_IMAGE_URL_EXTRA = "user_profile_image_url";

	// views
	private PullToRefreshListView lvTweets;
	private ProgressBar pbCenter;
	private LinearLayout llCompose;

	/** list of tweets */
	private LinkedList<Tweet> tweetsList = new LinkedList<Tweet>();
	/** tweets adapter */
	private TweetsAdapter adapter;

	/** account uer's info */
	private User accountUser;
	/** the current id of the oldest tweet we pulled (must be positive) */
	private long currentOldestTweetId = -1;
	/** the current id of the newest tweet we pulled (must be positive) */
	private long currentNewestTweetId = -1;
	/** if true, then we are still awaiting a response from the previous tweets request */
	private boolean isFetchingTweets = false;
	/** if true, then we want to request more (older) tweets */
	private boolean areOlderTweetsWanted = false;
	/** the id of the tweet that we just posted */
	private ArrayList<Long> lastPostedTweetIds = new ArrayList<Long>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupViews();
		setupListeners();
		getUserInfo();
		getOldTweets();
	}

	/** setups the views */
	private void setupViews() {
		llCompose = (LinearLayout) findViewById(R.id.llCompose);
		llCompose.setVisibility(View.INVISIBLE);
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
					long lastOldestTweetId = currentOldestTweetId;
					// oldest tweet ID should be the lowest ID value
					Iterator<Tweet> iter = tweets.iterator();
					while (iter.hasNext()) {
						long id = iter.next().getId();
						if (currentOldestTweetId < 0 || id <= currentOldestTweetId) {
							if (id == lastOldestTweetId) {
								iter.remove(); // duplicate entry. remove.
							} else {
								currentOldestTweetId = id; // otherwise set this id as the oldest
							}
						}
						// also update newest tweet ID so we can use this later when we get new tweets
						if (id > currentNewestTweetId) {
							currentNewestTweetId = id;
						}
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
					Toast.makeText(getBaseContext(), "Failed to retrieve tweets -- getting tweets too frequently.", Toast.LENGTH_LONG).show();
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
				// newest tweet ID should have the highest ID value
				Iterator<Tweet> iter = tweets.iterator();
				while (iter.hasNext()) {
					long id = iter.next().getId();
					if (lastPostedTweetIds.indexOf(id) >= 0) {
						// because we updated the timeline with the last posted tweet without making a request,
						// we need to remove the same tweet from the newest tweets retrieved from the server so that the tweet isn't duplicated
						iter.remove();
					} else if (id > currentNewestTweetId) {
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

	/** gets the user's account info */
	private void getUserInfo() {
		TwitterApp.getRestClient().getUserAccount(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				accountUser = User.fromJson(jsonObject);
				llCompose.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Toast.makeText(getBaseContext(), "Failed to retrieve user info -- trying to get the info too frequently.", Toast.LENGTH_LONG).show();
				Log.d("networking", "failed getUserInfo:: " + jsonObject.toString());
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
			Tweet t = (Tweet) data.getSerializableExtra(POSTED_TWEET_EXTRA);
			tweetsList.addFirst(t);
			adapter.notifyDataSetChanged();
			lastPostedTweetIds.add(t.getId());
		}
	}

	/** Callback for when the compose button is pressed */
	public void onCompose(View v) {
		Intent i = new Intent(this, ComposeActivity.class);
		i.putExtra(USER_NAME_EXTRA, accountUser.getName());
		i.putExtra(USER_SCREEN_NAME_EXTRA, accountUser.getScreenName());
		i.putExtra(USER_PROFILE_IMAGE_URL_EXTRA, accountUser.getProfileImageUrl());
		startActivityForResult(i, COMPOSE_REQUEST_CODE);
	}
}