package cz.mzk.kramerius.app;

import java.util.Comparator;

import cz.mzk.kramerius.app.model.Item;

public class ItemByTitleComparator implements Comparator<Item> {

	@Override
	public int compare(Item o1, Item o2) {
		String t1 = o1.getTitle();
		String t2 = o2.getTitle();
		t1 = t1.replace("[", "");
		t1 = t1.replace("]", "");
		t2 = t2.replace("[", "");
		t2 = t2.replace("]", "");
		try {
			int n1 = Integer.parseInt(t1);
			int n2 = Integer.parseInt(t2);
			if (n1 == n2) {
				return 0;
			}
			if (n1 > n2) {
				return 1;
			}
			return -1;
		} catch (NumberFormatException ex) {
		}

		return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
	}

}