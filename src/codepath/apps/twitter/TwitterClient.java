package codepath.apps.twitter;

import android.content.Context;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
    public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
    public static final String REST_CONSUMER_KEY = "q2x2t16tmKKN8z1OIJ8Xrw";       // Change this
    public static final String REST_CONSUMER_SECRET = "b3hETRsaEQehawwqIvNRmyjOafxUDY9ub88Z3cplbw"; // Change this
    public static final String REST_CALLBACK_URL = "oauth://twitterapp"; // Change this (here and in manifest)
    
    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

	/**
	 * Gets the tweets from the home timeline for the user
	 * @param maxId		max_id param indicating the ID of the oldest tweet we have right now
	 * @param sinceId	since_id param indicating the ID of the newest tweet we have right now
	 * @param handler	the response handler
	 */
	public void getHomeTimeline(long maxId, long sinceId, AsyncHttpResponseHandler handler) {
		RequestParams params = new RequestParams();
		params.put("count", "50"); // grab 50 tweets at a time
		if (maxId >= 0) {
			params.put("max_id", Long.toString(maxId));
		}
		if (sinceId >= 0) {
			params.put("since_id", Long.toString(sinceId));
		}
		String url = getApiUrl("statuses/home_timeline.json");
		client.get(url, params, handler);
	}
}