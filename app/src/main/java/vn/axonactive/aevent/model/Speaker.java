package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Speaker implements Parcelable {

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("description")
    private String description;


    @SerializedName("id")
    private int id;

    @SerializedName("major")
    private String major;

    @SerializedName("name")
    private String name;


    @SerializedName("linkedIn")
    private String linkedIn;

    @SerializedName("gender")
    private int gender;


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    protected Speaker(Parcel in) {
        avatar = in.readString();
        description = in.readString();
        id = in.readInt();
        major = in.readString();
        name = in.readString();
        linkedIn = in.readString();
        gender = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(description);
        dest.writeInt(id);
        dest.writeString(major);
        dest.writeString(name);
        dest.writeString(linkedIn);
        dest.writeInt(gender);
    }

    @SuppressWarnings("unused")
    public static final Creator<Speaker> CREATOR = new Creator<Speaker>() {
        @Override
        public Speaker createFromParcel(Parcel in) {
            return new Speaker(in);
        }

        @Override
        public Speaker[] newArray(int size) {
            return new Speaker[size];
        }
    };
}
