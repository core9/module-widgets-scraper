package nl.trimm.widgets.proxy;

import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class ProxyDataHandlerImpl implements ProxyDataHandler {

	@Override
	public String getName() {
		return "Proxy";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return ProxyConfig.class;
	}

	@Override
	public DataHandler<ProxyConfig> createDataHandler(DataHandlerFactoryConfig options) {
		final ProxyConfig config = (ProxyConfig) options;
		return new DataHandler<ProxyConfig>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Map<String, Object> result = new HashMap<String, Object>();
				try {
					switch(req.getMethod()) {
					case POST:
						result.put("data", doRequest(config, req, "POST"));
						break;
					case DELETE:
						result.put("data", doRequest(config, req, "DELETE"));
						break;
					case PUT:
						result.put("data", doRequest(config, req, "PUT"));
						break;
					default:
						result.put("data", doGet(config, req));
					}
				} catch (IOException e) {
					result.put("data", e.getMessage());
				}
				return result;
			}

			@Override
			public ProxyConfig getOptions() {
				return config;
			}
		};
	}
	
	/**
	 * Run a get request, return the result
	 * @return
	 * @throws IOException 
	 */
	private String doGet(ProxyConfig config, Request req) throws IOException {
		String path = req.getPath().replaceFirst(config.getPath(req), "");
		URL url = new URL(config.getSource(req) + path + parseParams(req));
		InputStreamReader isr = new InputStreamReader(url.openStream());
		int numCharsRead;
		char[] charArray = new char[1024];
		StringBuffer sb = new StringBuffer();
		while ((numCharsRead = isr.read(charArray)) > 0) {
			sb.append(charArray, 0, numCharsRead);
		}
		return sb.toString();
	}
	
	/**
	 * Do a request, specified by the method, return the result
	 * @param config
	 * @param req
	 * @param method
	 * @return
	 * @throws IOException
	 */
	private String doRequest(ProxyConfig config, Request req, String method) throws IOException {
		String path = req.getPath().replaceFirst(config.getPath(req), "");
		URL url = new URL(config.getSource(req) + path + parseParams(req));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod(method);
		// TODO: Remove application/json, get from Request Headers
		connection.setRequestProperty("Content-Type", "application/json"); 
		connection.setRequestProperty("charset", "utf-8");
		connection.setUseCaches (false);
		if(req.getBody() != null) {
			connection.setRequestProperty("Content-Length", "" + Integer.toString(req.getBody().getBytes().length));
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
			wr.writeBytes(req.getBody());
			wr.flush();
			wr.close();
		}
				
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String output = "";
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			output += inputLine;
		in.close();
		connection.disconnect();
		return output;
	}
	
	private String parseParams(Request req) {
		String params = "";
		for(Map.Entry<String, Object> entry : req.getParams().entrySet()) {
			params += "&" + entry.getKey() + "=" + entry.getValue();
		}
		if(!params.equals("")) {
			return "?" + params.substring(1);
		}
		return params;
	}

}
