package codepath.apps.twitter.models;

import android.widget.ImageButton;

/**
 * ImageButtonData - data for the image buttons in each tweet item
 */
public class ImageButtonData {
	/** the tweet associated with this image button */
	private Tweet tweet;
	/** image button that needs to be handled if this data's image button is clicked */
	private ImageButton ibtn;

	public ImageButton getRelatedImageButton() {
		return ibtn;
	}

	public Tweet getTweet() {
		return tweet;
	}

	public ImageButtonData(ImageButton ibtn, Tweet tweet) {
		this.ibtn = ibtn;
		this.tweet = tweet;
	}
}
