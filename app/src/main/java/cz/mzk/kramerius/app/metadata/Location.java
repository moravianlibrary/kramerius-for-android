package cz.mzk.kramerius.app.metadata;

import java.util.ArrayList;
import java.util.List;

public class Location {

	private String physicalLocation;
	private List<String> shelfLocatons;

	public Location() {
		shelfLocatons = new ArrayList<String>();
	}

	public boolean isEmpty() {
		return physicalLocation == null && shelfLocatons.isEmpty();
	}

	public String getPhysicalLocation() {
		return physicalLocation;
	}

	public void setPhysicalLocation(String physicalLocation) {
		this.physicalLocation = physicalLocation;
	}

	public void addShelfLocatons(String shelfLocaton) {
		this.shelfLocatons.add(shelfLocaton);
	}

	public List<String> getShelfLocatons() {
		return shelfLocatons;
	}

}
