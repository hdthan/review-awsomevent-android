package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Category implements Parcelable {
    @SerializedName("description")
    String description;

    @SerializedName("id")
    int id;

    @SerializedName("typeName")
    String typeName;

    public Category(String description, int id, String typeName) {
        this.description = description;
        this.id = id;
        this.typeName = typeName;
    }

    protected Category(Parcel in) {
        description = in.readString();
        id = in.readInt();
        typeName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeInt(id);
        dest.writeString(typeName);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SuppressWarnings("unused")
    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
