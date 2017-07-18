package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ltphuc on 4/7/2017.
 */

public class EventExpand implements Parcelable {

    @SerializedName("event")
    private Event event;

    @SerializedName("type")
    private int type;

    public EventExpand(Event event, int type) {
        this.event = event;
        this.type = type;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    protected EventExpand(Parcel in) {
        event = in.readParcelable(Event.class.getClassLoader());
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(event, flags);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EventExpand> CREATOR = new Creator<EventExpand>() {
        @Override
        public EventExpand createFromParcel(Parcel in) {
            return new EventExpand(in);
        }

        @Override
        public EventExpand[] newArray(int size) {
            return new EventExpand[size];
        }
    };
}
