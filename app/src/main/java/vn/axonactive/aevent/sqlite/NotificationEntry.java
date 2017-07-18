package vn.axonactive.aevent.sqlite;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class NotificationEntry {

    public static final String TABLE_NOTIFICATIONS = "notification";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_REV_TIME = "rev_time";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_SUB_TITLE = "sub_title";
    public static final String COLUMN_UNREAD = "unread";
    public static final String COLUMN_TOPIC_ID = "topic_id";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final int MAX_ROW = 30;

}
