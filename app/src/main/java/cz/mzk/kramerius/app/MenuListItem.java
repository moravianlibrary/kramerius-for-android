package cz.mzk.kramerius.app;

public class MenuListItem {

	private int iconResource;
	private int iconResourceSelected;
	private String title;

	public MenuListItem(String title, int iconResource, int iconResourceSelected) {
		this.iconResource = iconResource;
		this.iconResourceSelected = iconResourceSelected;
		this.title = title;
	}

	public int getIconResource() {
		return iconResource;
	}

	public int getIconResourceSelected() {
		return iconResourceSelected;
	}

	public String getTitle() {
		return title;
	}

	public void setIconResource(int iconResource) {
		this.iconResource = iconResource;
	}

	public void setIconResourceSelected(int iconResourceSelected) {
		this.iconResourceSelected = iconResourceSelected;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
