package codepath.apps.twitter.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.models.ImageButtonData;
import codepath.apps.twitter.models.Tweet;
import codepath.apps.twitter.models.User;
import com.activeandroid.ActiveAndroid;
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
	/** name of the intent bundle that contains some string to prepopulate into the text field */
	public static final String PREPOPULATED_STRING_EXTRA = "prepopulated_string_extra";

	// views
	private MenuItem miCompose; // hide before user info is retrieved because the compose activity needs it
	private PullToRefreshListView lvTweets;
	private ProgressBar pbCenter;
	private LinearLayout llCompose; // hide before user info is retrieved because the compose activity needs it

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.timeline, menu);
		miCompose = menu.findItem(R.id.miCompose);
		miCompose.setEnabled(false);
		getUserInfo();
		return true;
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

	/**
	 * On the initial tweets retrieval, try to get the tweets from db
	 * @return		true if we succeeded in getting tweets from db
	 */
	private boolean getInitialTweetsFromDb() {
		boolean success = false;
		if (tweetsList.size() == 0) {
			tweetsList.addAll(Tweet.recentItems());
			if (tweetsList.size() > 0) {
				currentNewestTweetId = tweetsList.getLast().getTweetId();
				currentOldestTweetId = tweetsList.getFirst().getTweetId();
				getNewTweets();
				success = true;
			}
		}
		return success;
	}

	/**
	 * tries to save the given tweet into the db
	 * @param tweet		tweet to save
	 */
	private void trySaveTweet(Tweet tweet) {
		// only save tweet if it's not already in the db
		if (Tweet.byTweetId(tweet.getTweetId()) == null) {
			if (!tweet.setUserUsingDb()) { // only save the user if it's not already in the db
				tweet.getUser().save();
			}
			tweet.save();
		}
	}

	/** makes request to get old tweets */
	private void getOldTweets() {
		if (getInitialTweetsFromDb()) {
			return; // no need to do anything more because we set got the tweets from the db
		} else if (!isNetworkAvailable()) {
			// don't try to make requests and let the user know that there's no internet connection
			Toast.makeText(this, "Please connect to a network.", Toast.LENGTH_LONG).show();
		} else if (isFetchingTweets) {
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
						Tweet currTweet = iter.next();
						long id = currTweet.getTweetId();
						if (id == lastOldestTweetId) {
							iter.remove(); // duplicate entry. remove. don't want to handle it further
							continue;
						}
						// try to save the current tweet into the db
						trySaveTweet(currTweet);
						if (currentOldestTweetId < 0 || id < currentOldestTweetId) {
							currentOldestTweetId = id; // otherwise set this id as the oldest
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
					failureToastHelper(getBaseContext(), "Failed to retrieve tweets: ", jsonObject);
				}
			});
		}
	}

	/** fetches newer tweets */
	private void getNewTweets() {
		// if there's no internet, don't try to get tweets
		if (!isNetworkAvailable()) {
			Toast.makeText(this, "Please connect to a network.", Toast.LENGTH_LONG).show();
			return;
		}

		// proceed when network is available...
		TwitterApp.getRestClient().getHomeTimeline(-1, currentNewestTweetId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
				// newest tweet ID should have the highest ID value
				Iterator<Tweet> iter = tweets.iterator();
				while (iter.hasNext()) {
					Tweet currTweet = iter.next();
					long id = currTweet.getTweetId();
					if (lastPostedTweetIds.indexOf(id) >= 0) {
						// because we updated the timeline with the last posted tweet without making a request,
						// we need to remove the same tweet from the newest tweets retrieved from the server so that the tweet isn't duplicated
						iter.remove();
						continue; // don't need to handle further
					}
					// try to save the current tweet into the db
					trySaveTweet(currTweet);
					if (id > currentNewestTweetId) {
						currentNewestTweetId = id;
					}
				}
				toggleCenterProgressBar(false);
				tweetsList.addAll(0, tweets);
				adapter.notifyDataSetChanged();
				lvTweets.onRefreshComplete();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				lvTweets.onRefreshComplete();
				failureToastHelper(getBaseContext(), "Failed to retrieve new tweets: ", jsonObject);
			}
		});
	}

	/** gets the user's account info */
	private void getUserInfo() {
		// if there's no internet, don't try to get user info
		if (!isNetworkAvailable()) {
			Toast.makeText(this, "Please connect to a network.", Toast.LENGTH_LONG).show();
			return;
		}

		// proceed when network is available...
		TwitterApp.getRestClient().getUserAccount(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				accountUser = User.fromJson(jsonObject);
				// enable compose elements
				llCompose.setVisibility(View.VISIBLE);
				miCompose.setEnabled(true);
				miCompose.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				failureToastHelper(getBaseContext(), "Failed to retrieve user info: ", jsonObject);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
			Tweet t = (Tweet) data.getSerializableExtra(POSTED_TWEET_EXTRA);
			trySaveTweet(t);
			tweetsList.addFirst(t);
			adapter.notifyDataSetChanged();
			lastPostedTweetIds.add(t.getTweetId());
		}
	}

	/** @return true if there is network connection */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/** Callback for when the compose button is pressed */
	public void onCompose(View v) {
		composeHelper("");
	}

	/** Callback for when the compose button is pressed from the action bar */
	public void onCompose(MenuItem mi) {
		composeHelper("");
	}

	/** helper function that opens the compose activity */
	private void composeHelper(String prepopulatedStr) {
		Intent i = new Intent(this, ComposeActivity.class);
		i.putExtra(USER_NAME_EXTRA, accountUser.getName());
		i.putExtra(USER_SCREEN_NAME_EXTRA, accountUser.getScreenName());
		i.putExtra(USER_PROFILE_IMAGE_URL_EXTRA, accountUser.getProfileImageUrl());
		i.putExtra(PREPOPULATED_STRING_EXTRA, prepopulatedStr);
		startActivityForResult(i, COMPOSE_REQUEST_CODE);
	}

	/**
	 * Helper function that shows a toast when an onFailure result comes back from a request
	 * @param context		context
	 * @param prefix		prefix of the toast text
	 * @param jsonObject	contains info about the error text
	 */
	public static void failureToastHelper(Context context, String prefix, JSONObject jsonObject) {
		try {
			JSONArray errorsArray = jsonObject.getJSONArray("errors");
			Toast.makeText(context, prefix + ((JSONObject)errorsArray.get(0)).getString("message"), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {}
	}

	/**
	 * callback when reply button is hit
	 * @param v		reply button view
	 */
	public void onReply(View v) {
		composeHelper("@"+v.getTag());
	}

	/**
	 * callback when retweet button is hit
	 * @param v		retweet button view
	 */
	public void onRetweet(final View v) {
		final ImageButtonData tag = (ImageButtonData) v.getTag();
		TwitterApp.getRestClient().retweet(tag.getTweet().getTweetId(), new JsonHttpResponseHandler() {
			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				failureToastHelper(getBaseContext(), "Failed to retweet tweet: ", jsonObject);
			}
		});
	}

	/**
	 * callback when unfavorited button is hit (means user wants to favorite)
	 * @param v		unfavorite button view
	 */
	public void onFavorite(final View v) {
		ImageButtonData tag = (ImageButtonData) v.getTag();
		TwitterApp.getRestClient().favorite(tag.getTweet().getTweetId(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				// show/hide corresponding buttons
				ImageButtonData tag = (ImageButtonData) v.getTag();
				v.setVisibility(View.INVISIBLE);
				tag.getRelatedImageButton().setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				failureToastHelper(getBaseContext(), "Failed to favorite tweet: ", jsonObject);
			}
		});
	}

	/**
	 * callback when favorited button is hit (means user wants to unfavorite)
	 * @param v		favorite button view
	 */
	public void onUnfavorite(final View v) {
		ImageButtonData tag = (ImageButtonData) v.getTag();
		TwitterApp.getRestClient().unfavorite(tag.getTweet().getTweetId(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				// show/hide corresponding buttons
				ImageButtonData tag = (ImageButtonData) v.getTag();
				v.setVisibility(View.INVISIBLE);
				tag.getRelatedImageButton().setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				failureToastHelper(getBaseContext(), "Failed to unfavorite tweet: ", jsonObject);
			}
		});
	}
}