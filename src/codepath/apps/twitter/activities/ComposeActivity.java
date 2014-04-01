package codepath.apps.twitter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ComposeActivity - the compose tweet screen
 */
public class ComposeActivity extends Activity {
	// views
	MenuItem miTweet;
	MenuItem miCharactersLeft;
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
		etBody.setText(i.getStringExtra(TimelineActivity.PREPOPULATED_STRING_EXTRA));
		// set up listeners
		etBody.addTextChangedListener(new InputValidator());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.compose, menu);
		miTweet = menu.findItem(R.id.miTweet);
		miCharactersLeft = menu.findItem(R.id.miCharactersLeft);
		int limit = Integer.parseInt(miCharactersLeft.getTitle().toString());
		miCharactersLeft.setTitle(Integer.toString(limit - etBody.getText().length()));
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
				miTweet.setEnabled(true);
				TimelineActivity.failureToastHelper(getBaseContext(), "Failed to post tweet: ", jsonObject);
			}
		});
	}

	/** input validator class that listens for text change and makes modifications based on text change */
	private class InputValidator implements TextWatcher {
		public void afterTextChanged(Editable s) {}
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {}
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {
			// track number of characters left in the tweet on the action bar
			int limit = Integer.parseInt(miCharactersLeft.getTitle().toString());
			if (before > 0) {
				miCharactersLeft.setTitle(Integer.toString(limit+1));
			} else if (count > 0) {
				miCharactersLeft.setTitle(Integer.toString(limit-1));
			}
		}
	}
}