package nl.trimm.widgets.scraper;

import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerGlobal;

import java.util.List;

public class ScraperConfig extends DataHandlerDefaultConfig implements DataHandlerFactoryConfig {

	private String source;
	private DataHandlerGlobal<String>  path;
	private List<QueryItem> queryItems;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public DataHandlerGlobal<String>  getPath() {
		return path;
	}

	public void setPath(DataHandlerGlobal<String>  path) {
		this.path = path;
	}

	public String getPath(Request req) {
		if(path.isGlobal()) {
			return req.getContext(this.getComponentName() + ".path", path.getValue());
		}
		return path.getValue();
	}

	public List<QueryItem> getQueryItems() {
		return this.queryItems;
	}

	public void setQueryItems(List<QueryItem> items) {
		this.queryItems = items;
	}
}
