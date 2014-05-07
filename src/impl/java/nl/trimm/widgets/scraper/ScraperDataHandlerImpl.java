package nl.trimm.widgets.scraper;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@PluginImplementation
public class ScraperDataHandlerImpl implements ScraperDataHandler {
	
	private final String[] ALLOWED_BINARIES = new String[]{
		"js", "css", "jpg", "png", "gif", "less", "sass", 
		"svg", "pdf", "woff", "ico", "jpeg"
	};
	
	@InjectPlugin
	private MongoDatabase mongo;

	@Override
	public String getName() {
		return "Scraper";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return ScraperConfig.class;
	}

	@Override
	public DataHandler<ScraperConfig> createDataHandler(DataHandlerFactoryConfig options) {
		final ScraperConfig config = (ScraperConfig) options;
		return new DataHandler<ScraperConfig>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Map<String,Object> result = new HashMap<String,Object>();
				String pmPath = config.getPath(req);
				String path = req.getPath().substring(pmPath.length());
				try {
					if(endsWithAny(path, ALLOWED_BINARIES)) {
						// TODO Check if cached
						URL url = new URL(config.getSource() + path);
					    req.getResponse().sendBinary(sun.misc.IOUtils.readFully(url.openStream(), -1, true));
					} else {
						// TODO Check if cached
						Document doc = Jsoup.connect(config.getSource() + path).get();
						Elements query = doc.select(config.getQuery());
						result.put("scraped", query.outerHtml().replace(config.getSource(), pmPath));
					}
				} catch (IOException e) {
					result.put("scraped", "<h1>500 - Error</h1><pre>The returned error: <br />" + e.getMessage() + "</pre>");
					req.getResponse().setStatusCode(500);
				}
				return result;
			}

			@Override
			public ScraperConfig getOptions() {
				return config;
			}
		};
	}
	
	private static boolean endsWithAny(final String string, final String... array) {
		for(String item : array) {
			if(string.endsWith(item)) {
				return true;
			}
		}
		return false;
	}

}
