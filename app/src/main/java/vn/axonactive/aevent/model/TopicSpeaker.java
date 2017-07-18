package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/10/17.
 */

public class TopicSpeaker implements Parcelable {

    @SerializedName("id")
    int id;

    @SerializedName("speaker")
    Speaker speaker;

    public TopicSpeaker(int id, Speaker speaker) {
        this.id = id;
        this.speaker = speaker;
    }

    protected TopicSpeaker(Parcel in) {
        id = in.readInt();
        speaker = (Speaker) in.readValue(Speaker.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeValue(speaker);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    @SuppressWarnings("unused")
    public static final Creator<TopicSpeaker> CREATOR = new Creator<TopicSpeaker>() {
        @Override
        public TopicSpeaker createFromParcel(Parcel in) {
            return new TopicSpeaker(in);
        }

        @Override
        public TopicSpeaker[] newArray(int size) {
            return new TopicSpeaker[size];
        }
    };
}
