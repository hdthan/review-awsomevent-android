package vn.axonactive.aevent.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "aevent.db";

    private static final int DATABASE_VERSION = 3;

    // Database creation sql statement
    private static final String CREATE_TABLE_NOTIFICATION = String.format("create table %s ( %s integer primary key autoincrement, %s text, %s text, %s text, %s text, %s integer,%s integer, %s integer )",
            NotificationEntry.TABLE_NOTIFICATIONS,
            NotificationEntry.COLUMN_ID,
            NotificationEntry.COLUMN_TITLE,
            NotificationEntry.COLUMN_REV_TIME,
            NotificationEntry.COLUMN_CONTENT,
            NotificationEntry.COLUMN_SUB_TITLE,
            NotificationEntry.COLUMN_UNREAD,
            NotificationEntry.COLUMN_TOPIC_ID,
            NotificationEntry.COLUMN_EVENT_ID);

    private static final String CREATE_TABLE_ENROLLMENT = String.format("create table %s ( %s integer primary key autoincrement, %s text, %s text, %s integer, %s text)",
            EnrollmentEntry.TABLE_ENROLLMENT,
            EnrollmentEntry.COLUMN_ID,
            EnrollmentEntry.COLUMN_TITLE,
            EnrollmentEntry.COLUMN_IMAGE_COVER,
            EnrollmentEntry.COLUMN_ENROLL_DATE,
            EnrollmentEntry.COLUMN_AUTH_CODE);


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_NOTIFICATION);
        database.execSQL(CREATE_TABLE_ENROLLMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + EnrollmentEntry.TABLE_ENROLLMENT);
        onCreate(db);
    }

}
