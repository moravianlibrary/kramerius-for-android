package cz.mzk.kramerius.app.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import android.util.Log;
import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Location;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.metadata.Part;
import cz.mzk.kramerius.app.metadata.Publisher;
import cz.mzk.kramerius.app.metadata.TitleInfo;

public class ModsParser {

	private static final String TAG = ModsParser.class.getName();

	public static Metadata getMetadata(String url) {

		Document document = getDocument(url);
		if (document == null) {
			return null;
		}
		Element root = document.getRootElement();
		removeNamespaces(root);
		Metadata metadata = new Metadata();

		Node node = root.selectSingleNode("//identifier[@type='issn']");
		if (node != null) {
			metadata.setIssn(node.getText());
		}

		node = root.selectSingleNode("//identifier[@type='isbn']");
		if (node != null) {
			metadata.setIsbn(node.getText());
		}

		List<Node> nodes = root.selectNodes("//note");
		if (nodes != null) {
			for (Node n : nodes) {
				String note = n.getText();
				note = removeSpaces(note);
				metadata.addNote(note);
			}
		}
		nodes = root.selectNodes("//physicalDescription/note");
		if (nodes != null) {
			for (Node n : nodes) {
				String note = n.getText();
				note = removeSpaces(note);
				metadata.addPhysicalDescriptionNote(note);
			}
		}
		fillTitleInfo(root, metadata);
		fillAuthors(root, metadata);
		fillPublishers(root, metadata);
		fillPart(root, metadata);
		fillLocation(root, metadata);

		return metadata;
	}

	private static void fillTitleInfo(Element element, Metadata metadata) {
		TitleInfo titleInfo = new TitleInfo();
		Node node = null;
		node = element.selectSingleNode("//titleInfo/title");
		if (node != null) {
			titleInfo.setTitle(node.getText());
		}
		node = element.selectSingleNode("//titleInfo/subTitle");
		if (node != null) {
			titleInfo.setSubtitle(node.getText());
		}
		node = element.selectSingleNode("//titleInfo/partName");
		if (node != null) {
			titleInfo.setPartName(node.getText());
		}
		node = element.selectSingleNode("//titleInfo/partNumber");
		if (node != null) {
			titleInfo.setPartNumber(node.getText());
		}
		metadata.setTitleInfo(titleInfo);
	}
	

	private static void fillPart(Element element, Metadata metadata) {
		Part part = new Part();
		Node node = null;
		node = element.selectSingleNode("//part/detail[@type='volume']/number");
		if (node != null) {
			part.setVolumeNumber(node.getText());
		}
		node = element.selectSingleNode("//part/detail[@type='part']/number");
		if (node != null) {
			part.setPartNumber(node.getText());
		}
		node = element.selectSingleNode("//part/detail[@type='issue']/number");
		if (node != null) {
			part.setIssueNumber(node.getText());
		}
		node = element.selectSingleNode("//part/detail[@type='pageNumber']/number");
		if (node != null) {
			part.setPageNumber(node.getText());
		}
		node = element.selectSingleNode("//part/detail[@type='pageIndex']/number");
		if (node != null) {
			part.setPageIndex(node.getText());
		}

		node = element.selectSingleNode("//part/date");
		if (node != null) {
			part.setDate(node.getText());
		}
		node = element.selectSingleNode("//part/text");
		if (node != null) {
			part.setText(node.getText());
		}
		if(!part.isEmpty()) {
			metadata.setPart(part);
		}
	}

	private static void fillAuthors(Element element, Metadata metadata) {
		List<Node> nodes = element.selectNodes("//name[@type='personal']");
		if (nodes != null) {
			for (Node n : nodes) {
				Author author = new Author();
				Node node = null;
				node = n.selectSingleNode("namePart");
				if (node != null) {
					author.setName(node.getText());
				}
				node = n.selectSingleNode("namePart[@type='date']");
				if (node != null) {
					author.setDate(node.getText());
				}
				if (!author.isEmpty()) {
					metadata.addAuthor(author);
				}
			}
		}
	}

	private static void fillLocation(Element element, Metadata metadata) {

		Location location = new Location();
		Node node = element.selectSingleNode("//location/physicalLocation");
		if (node != null) {
			location.setPhysicalLocation(node.getText());
		}

		List<Node> nodes = element.selectNodes("//location/shelfLocator");
		if (nodes != null) {
			for (Node n : nodes) {
				location.addShelfLocatons(n.getText());
				if (!location.isEmpty()) {
					metadata.setLocation(location);
				}
			}
		}
	}

	private static void fillPublishers(Element element, Metadata metadata) {
		List<Node> nodes = element.selectNodes("//originInfo");
		if (nodes != null) {
			for (Node n : nodes) {
				Publisher publisher = new Publisher();
				Node node = null;
				node = n.selectSingleNode("publisher");
				if (node != null) {
					publisher.setName(node.getText());
				}
				node = n.selectSingleNode("dateIssued");
				if (node != null) {
					publisher.setDate(node.getText());
				}
				node = n.selectSingleNode("place/placeTerm[@type='text']");
				if (node != null) {
					publisher.setPlace(node.getText());
				}
				if (!publisher.isEmpty()) {
					metadata.addPublisher(publisher);
				}
			}
		}
	}

	private static String removeSpaces(String s) {
		if (s.contains("  ")) {
			return removeSpaces(s.replace("  ", " "));
		} else {
			return s;
		}

	}

	public static void removeNamespaces(Element elem) {
		elem.setQName(QName.get(elem.getName(), Namespace.NO_NAMESPACE, elem.getQualifiedName()));
		Node n = null;
		for (int i = 0; i < elem.content().size(); i++) {
			n = (Node) elem.content().get(i);
			if (n.getNodeType() == Node.ATTRIBUTE_NODE)
				((Attribute) n).setNamespace(Namespace.NO_NAMESPACE);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				removeNamespaces((Element) n);
		}
	}

	private static Document getDocument(String urlString) {
		Document document = null;
		try {
			URL url = new URL(urlString);
			SAXReader reader = new SAXReader();
			reader.setValidation(false);
			reader.setEncoding("UTF-8");
			reader.setStripWhitespaceText(true);
			document = reader.read(url);
			// Namespace ns = new Namespace("mods",
			// "http://www.loc.gov/mods/v3");
			// document.add(ns);
		} catch (MalformedURLException ex) {
			Log.e(TAG, "mods: MalformedURLException: " + ex.getMessage());
		} catch (DocumentException ex) {
			Log.e(TAG, "mods: DocumentException: " + ex.getMessage());
		}
		return document;
	}

}