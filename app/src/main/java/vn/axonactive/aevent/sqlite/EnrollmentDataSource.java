package vn.axonactive.aevent.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.axonactive.aevent.model.Enrollment;
import vn.axonactive.aevent.model.Event;

/**
 * Created by ltphuc on 4/10/2017.
 */

public class EnrollmentDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns = {
            EnrollmentEntry.COLUMN_ID,
            EnrollmentEntry.COLUMN_TITLE,
            EnrollmentEntry.COLUMN_IMAGE_COVER,
            EnrollmentEntry.COLUMN_ENROLL_DATE,
            EnrollmentEntry.COLUMN_AUTH_CODE
    };

    public EnrollmentDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void clearEnrollment() {
        database.delete(EnrollmentEntry.TABLE_ENROLLMENT, EnrollmentEntry.COLUMN_ID
                + " > " + 0, null);
    }

    public List<Enrollment> getAllEnrollment() {
        List<Enrollment> enrollments = new ArrayList<>();

        Cursor cursor = database.query(EnrollmentEntry.TABLE_ENROLLMENT,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            enrollments.add(cursorToEnrollment(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return enrollments;

    }

    public void addEnrollment(List<Enrollment> enrollments) {
        clearEnrollment();
        for (Enrollment enrollment : enrollments) {
            addEnrollment(enrollment);
        }
    }

    public void addEnrollment(Enrollment enrollment) {

        Event event = enrollment.getEvent();

        ContentValues values = new ContentValues();
        values.put(EnrollmentEntry.COLUMN_TITLE, event.getTitle());
        values.put(EnrollmentEntry.COLUMN_IMAGE_COVER, event.getImageCover());
        values.put(EnrollmentEntry.COLUMN_ENROLL_DATE, enrollment.getEnrollDate().getTime());
        values.put(EnrollmentEntry.COLUMN_AUTH_CODE, enrollment.getAuthorizationCode());

        database.insert(EnrollmentEntry.TABLE_ENROLLMENT, null, values);

    }

    private Enrollment cursorToEnrollment(Cursor cursor) {

        Enrollment enrollment = new Enrollment();

        Event event = new Event();
        event.setTitle(cursor.getString(1));
        event.setImageCover(cursor.getString(2));

        enrollment.setEvent(event);

        enrollment.setEnrollDate(new Date(cursor.getLong(3)));
        enrollment.setAuthorizationCode(cursor.getString(4));

        return enrollment;
    }

}
