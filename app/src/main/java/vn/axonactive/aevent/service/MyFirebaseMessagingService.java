package vn.axonactive.aevent.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.MainActivity;
import vn.axonactive.aevent.model.NotificationModel;
import vn.axonactive.aevent.sqlite.NotificationDataSource;
import vn.axonactive.aevent.util.BadgeUtil;
import vn.axonactive.aevent.util.DataStorage;

/**
 * Created by ltphuc on 24/02/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static int mId = 0;

    private static int numberOfNotification = 0;

    public static void clearNumberOfNotification() {
        numberOfNotification = 0;
    }

    public static void incrementNumberOfNotification() {
        numberOfNotification++;
    }

    public static void decrementNumberOfNotification() {
        numberOfNotification--;
    }

    public static int getNumberOfNotification() {
        return numberOfNotification;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        // validate true format
        if (validateNotification(data)) {
            showNotification(data);
        }
    }

    private String timeRemaining(long time) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        long newTime = time - timeInMillis;
        if (newTime <= 0) {
            return "Topic has already started";
        }
        long timeInMinutes = newTime / 1000 / 60;
        long hours = timeInMinutes / 60;
        long days = hours / 24;

        String remainTime = "";

        if (days > 0) {
            remainTime = remainTime + days + " days ";
        }

        if ((hours % 24) > 0) {
            remainTime = remainTime + (hours % 24) + " hours ";
        }

        return days + " days " + (hours % 24) + " hours " + (timeInMinutes % 60) + " minutes remaining.";
    }

    private boolean validateNotification(Map<String, String> data) {

        if (data == null || data.size() == 0) {
            return false;
        }

        String typeBroadcast = data.get("type_broadcast");

        if (typeBroadcast == null || typeBroadcast.isEmpty()) {
            return false;
        }

        if ("1".equals(typeBroadcast)) {

            String title = data.get("title");

            if (title == null || title.isEmpty()) {
                return false;
            }

            String content = data.get("message");

            if (content == null || content.isEmpty()) {
                return false;
            }

        } else {
            String topicId = data.get("topic_id");
            if (topicId == null || !topicId.matches("\\d+")) {
                return false;
            }

            String title = data.get("topic_name");

            if (title == null || title.isEmpty()) {
                return false;
            }

            String content = data.get("location");

            if (content == null || content.isEmpty()) {
                return false;
            }

            String typeStatus = data.get("type_status");

            if (typeStatus == null || typeStatus.isEmpty()) {
                return false;
            }

        }

        return true;
    }

    private void showNotification(Map<String, String> data) {

        incrementNumberOfNotification();
        BadgeUtil.setBadge(this, getNumberOfNotification());

        String typeBroadcast = data.get("type_broadcast");
        int topicId = -1;
        int eventId = Integer.parseInt(data.get("event_id"));

        DataStorage.id = eventId;

        String title;
        String content;
        String subTitle;

        long revTime = new Date().getTime();

        if ("1".equals(typeBroadcast)) {

            title = data.get("title");
            content = data.get("message_title");

            if (content == null) {
                content = "";
            }

            subTitle = "Message: " + data.get("message");
        } else {

            topicId = Integer.parseInt(data.get("topic_id"));
            String typeStatus = data.get("type_status");
            title = data.get("topic_name");
            content = "Location: " + data.get("location");
            subTitle = "Status: ";

            if ("0".equals(typeStatus)) {
                subTitle = subTitle + "cancelled";
            } else {

                String startTime = data.get("start_time");

                long time = Long.parseLong(startTime);

                subTitle = subTitle + timeRemaining(time);

            }
        }

        NotificationDataSource dataSource = new NotificationDataSource(this);
        dataSource.open();
        NotificationModel notification = new NotificationModel(title, content, subTitle, revTime, 1, topicId, eventId);
        dataSource.addNotification(notification);
        dataSource.close();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notificationSelected", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_app_small_white)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent);

        builder.setContentTitle(title);
        builder.setContentText(content);

        if (!"".equals(subTitle)) {
            builder.setSubText(subTitle);
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mId = mId + 1;
        manager.notify(mId, builder.build());

        Intent intentBroadcast = new Intent("vn.axonactive.aevent.broadcast.notification");
        intentBroadcast.putExtra("num", numberOfNotification);
        intentBroadcast.putExtra("data", notification);
        sendBroadcast(intentBroadcast);

    }

}
