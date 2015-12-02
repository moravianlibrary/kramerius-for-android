package cz.mzk.kramerius.app.search;

import java.util.Set;

import cz.mzk.kramerius.app.xml.AltoParser;

/**
 * Created by Martin Řehánek on 1.12.15.
 */
public interface TextboxProvider {
    public Set<AltoParser.TextBox> getTextBoxes(int pagePosition);
}
