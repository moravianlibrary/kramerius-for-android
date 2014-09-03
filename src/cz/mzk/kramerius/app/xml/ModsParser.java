package cz.mzk.kramerius.app.xml;

import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import android.util.Log;
import cz.mzk.kramerius.app.model.Metadata;

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

		node = root.selectSingleNode("//titleInfo/title");
		if (node != null) {
			metadata.setTitle(node.getText());
		} else {
		}
		node = root.selectSingleNode("//titleInfo/subTitle");
		if (node != null) {
			metadata.setSubtitle(node.getText());
		}
		node = root.selectSingleNode("//physicalDescription/note");
		if (node != null) {
			String note = node.getText();
			note = removeSpaces(note);
			metadata.setNote(note);
		}
		node = root.selectSingleNode("//originInfo/publisher");
		if (node != null) {
			metadata.setPublisher(node.getText());
		}

		node = root.selectSingleNode("//name[@type='personal']/namePart");
		if (node != null) {
			metadata.setAuthorName(node.getText());
		}
		node = root.selectSingleNode("//name[@type='personal']/namePart[@type='date']");
		if (node != null) {
			metadata.setAuthorDate(node.getText());
		}
		return metadata;
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