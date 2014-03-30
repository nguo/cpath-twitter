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
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONObject;

/**
 * ComposeActivity - the compose tweet screen
 */
public class ComposeActivity extends Activity {
	// views
	MenuItem miTweet;
	ImageView ivUserProfile;
	TextView tvUserRealName;
	TextView tvUserScreenName;
	EditText etBody;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		// define views
		ivUserProfile = (ImageView) findViewById(R.id.ivUserProfile);
		tvUserRealName = (TextView) findViewById(R.id.tvUserRealName);
		tvUserScreenName = (TextView) findViewById(R.id.tvUserScreenName);
		etBody = (EditText) findViewById(R.id.etBody);
		// set values
		Intent i = getIntent();
		ImageLoader.getInstance().displayImage(i.getStringExtra(TimelineActivity.USER_PROFILE_IMAGE_URL_EXTRA), ivUserProfile);
		tvUserRealName.setText(i.getStringExtra(TimelineActivity.USER_NAME_EXTRA));
		tvUserScreenName.setText("@"+i.getStringExtra(TimelineActivity.USER_SCREEN_NAME_EXTRA));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.compose, menu);
		miTweet = menu.findItem(R.id.miTweet);
		return true;
	}

	/** callback for when the tweet menu button is clicked */
	public void onTweet(MenuItem mi) {
		miTweet.setEnabled(false);
		TwitterApp.getRestClient().postTweet(etBody.getText().toString(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonPostedTweet) {
				Toast.makeText(getBaseContext(), "Tweeted", Toast.LENGTH_SHORT).show();
				Intent i = new Intent();
				i.putExtra(TimelineActivity.POSTED_TWEET_EXTRA, Tweet.fromJson(jsonPostedTweet));
				setResult(RESULT_OK, i);
				finish();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Toast.makeText(getBaseContext(), "Failed to post tweet ", Toast.LENGTH_LONG).show();
				miTweet.setEnabled(true);
				Log.d("networking", "Failed to post tweet.... " + jsonObject.toString());
			}
		});
	}
}