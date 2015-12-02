package cz.mzk.kramerius.app.card;

import cz.mzk.kramerius.app.model.Item;


public interface OnPopupMenuSelectedListener {


    public void onPopupOpenSelected(Item item);

    public void onPopupDetailsSelected(Item item);

    public void onPopupShareSelected(Item item);
}
