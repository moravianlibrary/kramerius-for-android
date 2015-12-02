package cz.mzk.kramerius.app.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.data.KrameriusContract.LanguageEntry;
import cz.mzk.kramerius.app.util.LangUtils;
import cz.mzk.kramerius.app.util.VersionUtils;

public class LanguageSearchFilter extends SearchFilter {

    private static final String LOG_TAG = LanguageSearchFilter.class.getSimpleName();

    private TextView mInput;
    private boolean mLanguageSelected;
    private boolean[] mSelectedMask;

    public LanguageSearchFilter(Context context, ViewGroup parentView, List<SearchFilter> filters, boolean removable,
                                String key, String name) {
        super(context, parentView, filters, removable, key, name);
        init(context, parentView);
    }

    private void init(Context context, ViewGroup parent) {
        mLanguageSelected = false;
        View view = LayoutInflater.from(context).inflate(R.layout.view_search_language, parent, false);
        mInput = (TextView) view.findViewById(R.id.search_filter_text);
        mInput.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                assignLanguages();
            }
        });
        mInput.setText(R.string.search_language_select_all);
        initView(view);
    }

    private void assignLanguages() {
        Cursor cursor = getContext().getContentResolver().query(LanguageEntry.CONTENT_URI,
                new String[]{LanguageEntry.COLUMN_NAME}, LanguageEntry.COLUMN_LANG + "=?",
                new String[]{LangUtils.getLanguage()}, LanguageEntry.COLUMN_NAME + " COLLATE LOCALIZED");
        if (cursor == null) {
            return;
        }
        final List<String> list = new ArrayList<String>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        String[] array = new String[list.size()];
        list.toArray(array);

        if (mSelectedMask == null) {
            mSelectedMask = new boolean[cursor.getCount()];
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.search_language_select_title)
                .setMultiChoiceItems(array, mSelectedMask, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            mSelectedMask[which] = true;
                        } else {
                            mSelectedMask[which] = false;
                        }

                    }
                }).setPositiveButton(R.string.gen_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLanguagesSelected(list);

            }
        }).setNegativeButton(R.string.gen_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).show();
    }

    private void onLanguagesSelected(List<String> languages) {
        List<Integer> selected = new ArrayList<Integer>();
        for (int i = 0; i < mSelectedMask.length; i++) {
            if (mSelectedMask[i]) {
                selected.add(i);
            }
        }
        if (selected.isEmpty()) {
            mInput.setText(R.string.search_language_select_all);
            mLanguageSelected = false;
            return;
        }
        mLanguageSelected = true;
        String text = "";
        for (int i = 0; i < selected.size(); i++) {
            String language = languages.get(selected.get(i));
            text += language;
            if (i < selected.size() - 1) {
                text += ", ";
            }
        }
        mInput.setText(text);
    }

    public String getValue() {
        if (!mLanguageSelected) {
            return "";
        }
        String value = "";
        StringTokenizer tokenizer = new StringTokenizer(mInput.getText().toString(), ", ");
        while (tokenizer.hasMoreTokens()) {
            value += "'" + tokenizer.nextToken() + "'";
            if (tokenizer.hasMoreTokens()) {
                value += ",";
            }
        }
        return value;
    }

    @Override
    public int validate() {
        return 0;
    }

    @Override
    public void addToQuery(SearchQuery query) {
        if (!mLanguageSelected) {
            return;
        }
        Cursor cursor = getContext().getContentResolver().query(LanguageEntry.CONTENT_URI,
                new String[]{LanguageEntry.COLUMN_CODE},
                LanguageEntry.COLUMN_NAME + " IN (" + getValue() + ") AND " + LanguageEntry.COLUMN_LANG + "=?",
                new String[]{LangUtils.getLanguage()}, null);
        if (cursor == null) {
            return;
        }
        List<String> languages = new ArrayList<String>();
        while (cursor.moveToNext()) {
            languages.add(cursor.getString(0));
        }
        if (VersionUtils.Debuggable()) {
            Log.d(LOG_TAG, "addToQuery - languages: " + languages.toString());
        }
        query.languages(languages);
        cursor.close();
    }

}