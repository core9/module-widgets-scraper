package nl.trimm.widgets.proxy;

import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerGlobalString;

public class ProxyConfig extends DataHandlerDefaultConfig implements DataHandlerFactoryConfig {
	
	private DataHandlerGlobalString path;
	private DataHandlerGlobalString source;


	public DataHandlerGlobalString getPath() {
		return path;
	}
	
	public String getPath(Request req) {
		if(path.isGlobal()) {
			return req.getContext(this.getComponentName() + ".path", path.getValue());
		}
		return path.getValue();
	}

	public void setPath(DataHandlerGlobalString path) {
		this.path = path;
	}

	public DataHandlerGlobalString getSource() {
		return source;
	}
	
	public String getSource(Request req) {
		if(source.isGlobal()) {
			return req.getContext(this.getComponentName() + ".source", source.getValue());
		}
		return source.getValue();
	}

	public void setSource(DataHandlerGlobalString source) {
		this.source = source;
	}

}
