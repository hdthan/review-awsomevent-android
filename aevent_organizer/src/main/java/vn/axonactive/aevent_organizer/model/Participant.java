package vn.axonactive.aevent_organizer.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by ltphuc on 4/3/2017.
 */

@IgnoreExtraProperties
public class Participant {

    private int check;
    private String code;
    private String email;
    private String fullName;
    private String phone;
    private long userId;

    public Participant(long userId, int check, String code, String email, String fullName, String phone) {
        this.userId = userId;
        this.check = check;
        this.code = code;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Participant() {

    }
}
