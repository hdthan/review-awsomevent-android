package vn.axonactive.aevent.util;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.model.Event;

/**
 * Created by Dell on 2/19/2017.
 */

public class DataStorage {

    public static final String APP_PREFS = BuildConfig.APP_PREFS;
    public static final String FIRST_LAUNCH = "FIRST_LAUNCH";
    public static final String TOKEN = "TOKEN";
    public static final String FULL_NAME = "FULL_NAME";
    public static final String ACCOUNT_CODE = "ACCOUNT_CODE";
    public static final String URL_AVATAR = "URL_AVATAR";
    public static final String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public static final String EMAIL = "EMAIL";
    public static Event currentEvent = null;
    public static int id = -1;
    public static String email = "";
}
