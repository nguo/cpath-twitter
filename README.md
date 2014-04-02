cpath-twitter
=============

TheCodePath: Twitter App

Sample Images:

![Alt text](assets/01login.png "Login Screen")

![Alt text](assets/02timeline.png "Home Timeline")

![Alt text](assets/03compose.png "Composing Tweet")

Completed Stories:

* All the required stories.
  * User can sign in using OAuth login flow
  * User can view last 25 tweets from their home timeline 
  * User can load more tweets once they reach the bottom of the list using "infinite scroll" pagination
  * User can compose a new tweet
* The following optional stories:
  * Links in tweets are clickable and viewable
  * User can see a counter with total number of characters left for tweet
  * User can refresh tweets timeline by pulling down to refresh
  * User can open the twitter app offline and see last loaded tweets -- even if the user isn't offline, the first tweets that the user will see are from the local db. The app only makes requests when the user wants to see older/newer tweets (or, on first install)
  * Improve the user interface and theme the app to feel twitter branded -- changed some colors, add the reply and favorite icon buttons on each tweet in the timeline. I couldn't get retweet to work. Kept getting an error saying, "not authorized to use this endpoint"
