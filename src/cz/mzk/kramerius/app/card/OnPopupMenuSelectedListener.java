package cz.mzk.kramerius.app.card;

import cz.mzk.kramerius.app.model.Item;


public interface OnPopupMenuSelectedListener {

	
	public void onPopupOpenSelectd(Item item);
	public void onPopupDetailsSelectd(Item item);
	public void onPopupShareSelectd(Item item);
}
