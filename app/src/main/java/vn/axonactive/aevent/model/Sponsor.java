package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Sponsor implements Parcelable {
    @SerializedName("id")
    private int id;

    @SerializedName("image")
    private String image;

    @SerializedName("location")
    private String location;

    @SerializedName("sponsorName")
    private String sponsorName;

    @SerializedName("type")
    private int type;

    @SerializedName("website")
    private String website;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    protected Sponsor(Parcel in) {
        id = in.readInt();
        image = in.readString();
        location = in.readString();
        sponsorName = in.readString();
        type = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(location);
        dest.writeString(sponsorName);
        dest.writeInt(type);
    }

    @SuppressWarnings("unused")
    public static final Creator<Sponsor> CREATOR = new Creator<Sponsor>() {
        @Override
        public Sponsor createFromParcel(Parcel in) {
            return new Sponsor(in);
        }

        @Override
        public Sponsor[] newArray(int size) {
            return new Sponsor[size];
        }
    };
}
