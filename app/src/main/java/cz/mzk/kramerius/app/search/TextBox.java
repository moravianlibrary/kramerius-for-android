package cz.mzk.kramerius.app.search;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Martin Řehánek on 2.12.15.
 */
public class TextBox implements Parcelable {
    private final String mNormalizedString;
    private final Rect mRectangle;

    public TextBox(String normalizedString, Rect rectangle) {
        mNormalizedString = normalizedString;
        mRectangle = rectangle;
    }

    public String getNormalizedString() {
        return mNormalizedString;
    }

    public Rect getRectangle() {
        return mRectangle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mNormalizedString);
        dest.writeParcelable(this.mRectangle, 0);
    }

    protected TextBox(Parcel in) {
        this.mNormalizedString = in.readString();
        this.mRectangle = in.readParcelable(Rect.class.getClassLoader());
    }

    public static final Parcelable.Creator<TextBox> CREATOR = new Parcelable.Creator<TextBox>() {
        public TextBox createFromParcel(Parcel source) {
            return new TextBox(source);
        }

        public TextBox[] newArray(int size) {
            return new TextBox[size];
        }
    };
}
