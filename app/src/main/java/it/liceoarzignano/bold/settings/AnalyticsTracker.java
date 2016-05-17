package it.liceoarzignano.bold.settings;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import it.liceoarzignano.bold.R;

public class AnalyticsTracker {

    private static AnalyticsTracker sInstance;
    private final Context mContext;

    private AnalyticsTracker(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Description:
     * Fire analytics Tracker
     *
     * @param context: used to initialize sInstance
     */
    public static synchronized void initializeTracker(Context context) {
        if (sInstance == null) {
            sInstance = new AnalyticsTracker(context);
        }
    }

    /**
     * Description:
     * Get instance for the Tracker
     *
     * @return instance
     */
    public static synchronized AnalyticsTracker getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }

        return sInstance;
    }

    /**
     * Description:
     * Initialize Tracker
     *
     * @return initialized Tracker
     */
    private static synchronized Tracker getAnalyticsTracker() {
        return AnalyticsTracker.getInstance().get();
    }

    /**
     * Description:
     * Send an event to Google Analytics
     *
     * @param action:  event action name
     * @param context: used to get Tracker instance if it's not been initialized
     */
    public static void trackEvent(String action, Context context) {
        if (sInstance == null) {
            initializeTracker(context);
        }
        Tracker tracker = getAnalyticsTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setAction(action)
                .setCategory("Bold")
                .setLabel("Bold Event")
                .build());
    }

    /**
     * Description:
     * Load Tracker preferences from the xml config file
     *
     * @return Tracker with preferences
     */
    public synchronized Tracker get() {
        return GoogleAnalytics.getInstance(mContext).newTracker(R.xml.ga_tracker);
    }

}
