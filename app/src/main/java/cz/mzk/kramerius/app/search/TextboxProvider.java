package cz.mzk.kramerius.app.search;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Martin Řehánek on 2.12.15.
 */
public class TextboxProvider implements Parcelable {

    private final Map<Integer, Set<TextBox>> mTextboxMap = new HashMap<>();

    public TextboxProvider() {
    }


    public int size() {
        return mTextboxMap.size();
    }

    public void setTextBoxes(int pagePosition, Set<TextBox> textBoxes) {
        mTextboxMap.put(pagePosition, textBoxes);
    }

    public Set<TextBox> getTextBoxes(int pagePosition) {
        return mTextboxMap.get(pagePosition);
    }

    public void clear() {
        mTextboxMap.clear();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTextboxMap.size());//size
        for (Integer key : mTextboxMap.keySet()) {
            dest.writeInt(key);
            List<TextBox> textBoxes = new ArrayList<>();
            textBoxes.addAll(mTextboxMap.get(key));
            dest.writeList(textBoxes);
        }
    }


    protected TextboxProvider(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Integer key = in.readInt();
            List<TextBox> textBoxesList = in.readArrayList(TextBox.class.getClassLoader());
            Set<TextBox> textBoxesSet = new HashSet<>(textBoxesList.size());
            textBoxesSet.addAll(textBoxesList);
            mTextboxMap.put(key, textBoxesSet);
        }
    }

    public static final Parcelable.Creator<TextboxProvider> CREATOR = new Parcelable.Creator<TextboxProvider>() {
        public TextboxProvider createFromParcel(Parcel source) {
            return new TextboxProvider(source);
        }

        public TextboxProvider[] newArray(int size) {
            return new TextboxProvider[size];
        }
    };

    public String toTextboxCountString() {
        StringBuilder builder = new StringBuilder();
        List<Integer> keys = new ArrayList<>(mTextboxMap.keySet().size());
        keys.addAll(mTextboxMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            if (i == 0) {
                builder.append("[");
            }
            int key = keys.get(i);
            builder.append(String.format("p %d: %d tb", key, mTextboxMap.get(key).size()));
            if (i == keys.size() - 1) {
                builder.append("]");
            } else {
                builder.append(";");
            }
        }
        return builder.toString();
    }
}
