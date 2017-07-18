package vn.axonactive.aevent_organizer.util;

/**
 * Created by ltphuc on 1/18/2017.
 */

public class Validation {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static boolean isPhoneNumber(String phone) {

        String regularExpression = "\\d{10,11}";

        return phone.matches(regularExpression);
    }

    public static boolean isEmail(String email) {
        return email.matches(EMAIL_PATTERN);
    }

    public static boolean isPassword(String password) {

        if (password.length() < 7) {
            return false;
        }
        return true;
    }

}
