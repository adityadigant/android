package com.growthfile.growthfileNew;
import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import java.util.HashMap;

/**
 * An extension to Application class to provide tracker for analytics purposes. Having the tracker
 * instances here allows all the activities to access the same tracker instances. The trackers can
 * be initialised on startup or when they are required based on performance requirements.
 */
public class AnalyticsSampleApp extends Application {

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "178604171";

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.


    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public AnalyticsSampleApp() {
        super();
    }
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) : null;
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
