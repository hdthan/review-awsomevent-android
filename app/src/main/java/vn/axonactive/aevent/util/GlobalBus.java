package vn.axonactive.aevent.util;

import com.squareup.otto.Bus;

/**
 * Created by ltphuc on 3/13/2017.
 */

public class GlobalBus {

    private static Bus sBus;

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }

}
