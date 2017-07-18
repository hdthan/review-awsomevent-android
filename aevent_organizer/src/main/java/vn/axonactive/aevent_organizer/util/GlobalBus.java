package vn.axonactive.aevent_organizer.util;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by ltphuc on 3/13/2017.
 */

public class GlobalBus {

    private static Bus sBus;

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new Bus(ThreadEnforcer.MAIN);
        }
        return sBus;
    }

}
