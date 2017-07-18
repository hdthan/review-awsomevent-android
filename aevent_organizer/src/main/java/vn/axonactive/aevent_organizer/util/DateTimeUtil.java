package vn.axonactive.aevent_organizer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class DateTimeUtil {

    public static String parseDateTime(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String getLengthTime(Date startTime, Date endTime) {

        long length = (endTime.getTime() - startTime.getTime()) / 1000;

        long minutes = length / 60;

        long hours = minutes / 60;

        String lengthTime = "";

        if (hours > 0) {
            lengthTime = hours + " hrs ";
        }

        minutes = minutes % 60;

        if (minutes > 0) {
            lengthTime += minutes + " mins";
        }

        return lengthTime;
    }

    public static String getLengthTime(Date startTime) {

        Date endTime = new Date();

        long length = (endTime.getTime() - startTime.getTime()) / 1000;

        long minutes = length / 60;

        long hours = minutes / 60;

        long days = hours / 24;

        if (days > 0) {
            return days + " days";
        } else if (hours > 0) {
            return hours + " hrs";
        } else if (minutes > 0) {
            return minutes + " mins";
        }
        return "now";
    }

}
