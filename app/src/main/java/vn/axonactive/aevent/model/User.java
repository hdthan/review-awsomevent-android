package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by dangducnam on 1/6/17.
 */

public class User implements Parcelable {

    @SerializedName("id")
    private Long id;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("gender")
    private int gender;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("birthday")
    private Date birthday;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("rangeAge")
    private String rangeAge;

    @SerializedName("job")
    private String job;

    @SerializedName("accountType")
    private String accountType;

    @SerializedName("accountCode")
    private String accountCode;

    @SerializedName("company")
    private String company;

    public User() {

    }

    public User(String email, String fullName, String password) {
        this.email = email;
        this.fullName = fullName;
    }

    protected User(Parcel in) {
        email = in.readString();
        fullName = in.readString();
        gender = in.readInt();
        avatar = in.readString();
        phone = in.readString();
        address = in.readString();
        rangeAge = in.readString();
        job = in.readString();
        accountType = in.readString();
        accountCode = in.readString();
        company = in.readString();
    }

    public User(Long id, String fullName, String phone, String email, String rangeAge, String job) {
        this.id = id;
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.rangeAge = rangeAge;
        this.job = job;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(fullName);
        dest.writeInt(gender);
        dest.writeString(avatar);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(rangeAge);
        dest.writeString(job);
        dest.writeString(accountType);
        dest.writeString(accountCode);
        dest.writeString(company);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRangeAge() {
        return rangeAge;
    }

    public void setRangeAge(String rangeAge) {
        this.rangeAge = rangeAge;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}