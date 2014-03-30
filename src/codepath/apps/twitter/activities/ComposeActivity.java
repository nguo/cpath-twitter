package codepath.apps.twitter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONObject;

/**
 * ComposeActivity - the compose tweet screen
 */
public class ComposeActivity extends Activity {
	// views
	ImageView ivUserProfile;
	TextView tvUserRealName;
	TextView tvUserScreenName;
	EditText etBody;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		ivUserProfile = (ImageView) findViewById(R.id.ivUserProfile);
		tvUserRealName = (TextView) findViewById(R.id.tvUserRealName);
		tvUserScreenName = (TextView) findViewById(R.id.tvUserScreenName);
		etBody = (EditText) findViewById(R.id.etBody);
		Intent i = getIntent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.compose, menu);
		return true;
	}

	/** callback for when the tweet menu button is clicked */
	public void onTweet(MenuItem mi) {
		TwitterApp.getRestClient().postTweet(etBody.getText().toString(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonPostedTweet) {
				Intent i = new Intent();
				i.putExtra(TimelineActivity.POSTED_TWEET_EXTRA, jsonPostedTweet.toString());
				setResult(RESULT_OK, i);
				finish();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Log.d("networking", "Failed to post .... " + jsonObject.toString());
			}
		});
	}
}