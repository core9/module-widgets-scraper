package nl.trimm.widgets.scraper;

public class ReplaceItem {
	private String cssSelector;
	private String elementAttribute;
	private String regex;

	public String getCssSelector() {
		return cssSelector;
	}

	public void setCssSelector(String cssSelector) {
		this.cssSelector = cssSelector;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getElementAttribute() {
		return elementAttribute;
	}

	public void setElementAttribute(String elementAttribute) {
		this.elementAttribute = elementAttribute;
	}
}
