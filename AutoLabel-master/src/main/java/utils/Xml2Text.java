package utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;
import java.util.Iterator;

public class Xml2Text extends DefaultHandler {

	boolean isText = false;
	int isPicture = 0;
	int notClosedTdCount = 0;
	String textOnly = "";
	boolean needPicture = false;
	boolean isLatex = false;
	Map<String, String> latexMap = ReadJSON.latexSymbolMap();// 数学符号对照表

	public String getText() {
		if (textOnly.endsWith(" ") && textOnly.length() > 1) {
			textOnly = textOnly.substring(0, textOnly.length() - 1);
		}
		return textOnly;
	}

	public void needPicture() {
		needPicture = true;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.trim().toLowerCase().equals("td")) {
			notClosedTdCount++;
		}
		if (qName.trim().toLowerCase().equals("text")) {
			isText = true;
		}
		if (qName.trim().toLowerCase().equals("latex")) {
			isLatex = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.trim().toLowerCase().equals("td")) {
			textOnly += " ";
			notClosedTdCount--;
		}
		if (qName.trim().toLowerCase().equals("text")) {
			isText = false;
		}
		if (qName.trim().toLowerCase().equals("latex")) {
			isLatex = false;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isText) {
			textOnly += new String(ch, start, length);
			if (notClosedTdCount == 0) {
				textOnly += "";
			}
		}
		if (isLatex) {
			String newText = "";
			String text = new String(ch, start, length);
			Iterator<String> iter = latexMap.keySet().iterator();
			while(iter.hasNext()){
				String label = iter.next();
				String meaning = latexMap.get(label);
				if (text.indexOf(label) != -1) {
					newText += meaning;
				}
			}
			textOnly += newText;
		}
	}
}
