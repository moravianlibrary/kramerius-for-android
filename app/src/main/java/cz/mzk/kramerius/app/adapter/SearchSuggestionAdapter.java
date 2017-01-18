package cz.mzk.kramerius.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.kramerius.app.R;

/**
 * Created by rychtar on 18.01.17.
 */

public class SearchSuggestionAdapter extends BaseAdapter {

    private ArrayList<String> data;
    private int historyCount = 0;
    private LayoutInflater inflater;
    private boolean ellipsize;

    public SearchSuggestionAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.ellipsize = true;
        data = new ArrayList<>();
    }

    public void refresh(String query, List<String> history, List<String> suggestions) {
        historyCount = history.size();
        int c = historyCount;
        data.clear();
        data.addAll(history);
        String q = query.toLowerCase();
        for(String s : suggestions) {
            if(data.contains(s)) {
                continue;
            }
            if(s.toLowerCase().startsWith(q)) {
                data.add(c, s);
            } else {
                data.add(s);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SuggestionsViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
            viewHolder = new SuggestionsViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SuggestionsViewHolder) convertView.getTag();
        }
        String currentListData = (String) getItem(position);
        viewHolder.textView.setText(currentListData);
        if (ellipsize) {
            viewHolder.textView.setSingleLine();
            viewHolder.textView.setEllipsize(TextUtils.TruncateAt.END);
        }
        if(position < historyCount) {
            viewHolder.imageView.setImageResource(R.drawable.ic_recent_grey);
        } else {
            viewHolder.imageView.setImageResource(R.drawable.ic_search_grey);
        }
        return convertView;
    }

    private class SuggestionsViewHolder {

        TextView textView;
        ImageView imageView;

        public SuggestionsViewHolder(View convertView) {
            textView = (TextView) convertView.findViewById(R.id.suggestion_text);
            imageView = (ImageView) convertView.findViewById(R.id.suggestion_icon);
        }
    }
}