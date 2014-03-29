package codepath.apps.twitter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;

import java.util.ArrayList;

/**
 * TimelineActivity - home timeline screen
 */
public class TimelineActivity extends Activity {
	private ListView lvTweets;
	private TweetsAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupViews();

		TwitterApp.getRestClient().getHomeTimeline(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
				adapter = new TweetsAdapter(getBaseContext(), tweets);
				lvTweets.setAdapter(adapter);
			}
		});
	}

	/** setups the views */
	private void setupViews() {
		lvTweets = (ListView) findViewById(R.id.lvTweets);
	}
}