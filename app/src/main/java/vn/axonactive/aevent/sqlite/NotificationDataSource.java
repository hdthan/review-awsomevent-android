package vn.axonactive.aevent.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.axonactive.aevent.model.NotificationModel;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class NotificationDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns = {
            NotificationEntry.COLUMN_ID,
            NotificationEntry.COLUMN_TITLE,
            NotificationEntry.COLUMN_REV_TIME,
            NotificationEntry.COLUMN_CONTENT,
            NotificationEntry.COLUMN_SUB_TITLE,
            NotificationEntry.COLUMN_UNREAD,
            NotificationEntry.COLUMN_TOPIC_ID,
            NotificationEntry.COLUMN_EVENT_ID
    };

    public NotificationDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public NotificationModel addNotification(NotificationModel notification) {

        if (getNumberOfRow() >= NotificationEntry.MAX_ROW) {
            int id = getMinId();
            deleteNotification(id);
        }

        ContentValues values = new ContentValues();

        values.put(NotificationEntry.COLUMN_TITLE, notification.getTitle());
        values.put(NotificationEntry.COLUMN_UNREAD, 1);
        values.put(NotificationEntry.COLUMN_CONTENT, notification.getContent());
        values.put(NotificationEntry.COLUMN_SUB_TITLE, notification.getSubTitle());
        values.put(NotificationEntry.COLUMN_REV_TIME, notification.getRevTime() + "");
        values.put(NotificationEntry.COLUMN_TOPIC_ID, notification.getTopicId() + "");
        values.put(NotificationEntry.COLUMN_EVENT_ID, notification.getEventId() + "");

        long insertId = database.insert(NotificationEntry.TABLE_NOTIFICATIONS, null,
                values);

        Cursor cursor = database.query(NotificationEntry.TABLE_NOTIFICATIONS,
                allColumns, NotificationEntry.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        NotificationModel newNotification = cursorToNotification(cursor);
        cursor.close();

        return newNotification;

    }

    public void clearNotification() {
        database.delete(NotificationEntry.TABLE_NOTIFICATIONS, NotificationEntry.COLUMN_ID
                + " > " + 0, null);
    }

    private int getMinId() {

        int id = 1;

        String sqlCommand = String.format("select min(%s) from %s", NotificationEntry.COLUMN_ID, NotificationEntry.TABLE_NOTIFICATIONS);

        Cursor c = database.rawQuery(sqlCommand, null);

        if (c.moveToFirst()) {
            id = c.getInt(0);
        }

        c.close();

        return id;
    }

    private int getNumberOfRow() {

        return (int) DatabaseUtils.queryNumEntries(database, NotificationEntry.TABLE_NOTIFICATIONS);
    }

    public void updateNotification(long id, int unread) {

        ContentValues values = new ContentValues();

        values.put(NotificationEntry.COLUMN_UNREAD, unread);

        database.update(NotificationEntry.TABLE_NOTIFICATIONS, values, NotificationEntry.COLUMN_ID + " = " + id, null);
    }

    public void deleteNotification(long id) {
        database.delete(NotificationEntry.TABLE_NOTIFICATIONS, NotificationEntry.COLUMN_ID
                + " = " + id, null);
    }

    public List<NotificationModel> getAllNotifications() {
        List<NotificationModel> notifications = new ArrayList<>();

        Cursor cursor = database.query(NotificationEntry.TABLE_NOTIFICATIONS,
                allColumns, null, null, null, null, NotificationEntry.COLUMN_REV_TIME + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            notifications.add(cursorToNotification(cursor));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return notifications;
    }

    private NotificationModel cursorToNotification(Cursor cursor) {

        NotificationModel notification = new NotificationModel();

        notification.setId(cursor.getLong(0));
        notification.setTitle(cursor.getString(1));

        long revTime = Long.parseLong(cursor.getString(2));

        notification.setRevTime(revTime);
        notification.setContent(cursor.getString(3));
        notification.setSubTitle(cursor.getString(4));
        notification.setUnread(cursor.getInt(5));
        notification.setTopicId(cursor.getInt(6));
        notification.setEventId(cursor.getInt(7));

        return notification;
    }

    public interface OnReceivedDataListener {
        void onReceived(NotificationModel notification);
    }

}
