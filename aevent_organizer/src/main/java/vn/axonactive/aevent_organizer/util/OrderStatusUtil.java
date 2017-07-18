package vn.axonactive.aevent_organizer.util;

import java.util.Date;

import vn.axonactive.aevent_organizer.model.OrderStatus;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class OrderStatusUtil {

    public static OrderStatus convertPeriodTimeToStatus(Date startTime, Date endTime) {

        Date now = new Date();

        if (startTime.getTime() < now.getTime() && endTime.getTime() < now.getTime()) {
            return OrderStatus.COMPLETED;
        }

        if (startTime.getTime() < now.getTime() && endTime.getTime() > now.getTime()) {
            return OrderStatus.ACTIVE;
        }

        return OrderStatus.INACTIVE;

    }

}
