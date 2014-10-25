package nl.trimm.widgets.scraper;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.server.request.Method;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@PluginImplementation
public class ScraperDataHandlerImpl implements ScraperDataHandler {

	@InjectPlugin
	private MongoDatabase mongo;

	private CrudRepository<CacheEntity> cache;

	@PluginLoaded
	public void onDatabaseLoaded(RepositoryFactory factory) throws NoCollectionNamePresentException {
		cache = factory.getRepository(CacheEntity.class);
	}

	private final String[] ALLOWED_BINARIES = new String[]{
		"js", "css", "jpg", "png", "gif", "less", "sass",
		"svg", "pdf", "woff", "ico", "jpeg"
	};
	private final String STATIC_COLLECTION = "static.cache";

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
				String pmPath = config.getPath(req);
				String path = req.getPath().substring(pmPath.length());
				try {
					if(endsWithAny(path, ALLOWED_BINARIES)) {
					    req.getResponse().sendBinary(retrieveBinaryFile(req.getVirtualHost(), config.getSource(), path));
					    return new HashMap<String,Object>();
					} else {
						if(req.getMethod() == Method.POST) {
							return getResult(req.getVirtualHost(), config, path, pmPath);
						} else {
							return getResult(req.getVirtualHost(), config, path, pmPath);
						}
					}
				} catch (IOException e) {
					Map<String,Object> result = new HashMap<String,Object>();
					result.put("scraped", "<h1>500 - Error</h1><pre>The returned error: <br />" + e.getMessage() + "</pre>");
					req.getResponse().setStatusCode(500);
					return result;
				}
			}

			@Override
			public ScraperConfig getOptions() {
				return config;
			}
		};
	}

	/**
	 * Retrieve the page contents
	 * @param vhost
	 * @param config
	 * @param path
	 * @param pmPath
	 * @return
	 * @throws IOException
	 */
	private Map<String,Object> getResult(VirtualHost vhost, ScraperConfig config, String path, String pmPath) throws IOException {
		String hash = getHashCode(config.getSource(), path);
		CacheEntity cached = cache.read(vhost, hash);
		if(cached == null) {
			cached = new CacheEntity();
			cached.setId(hash);
			Document doc = Jsoup.connect(config.getSource() + path).get();
			Map<String,Object> result = new HashMap<String,Object>();
			for(QueryItem item : config.getQueryItems()) {
				Elements query = doc.select(item.getCssSelector());
				result.put(item.getParam(), query.html().replace(config.getSource(), pmPath));
				result.put(item.getParam() + "outerHTML", query.outerHtml().replace(config.getSource(), pmPath));
			}
			cached.setContents(result);
			cache.create(vhost, cached);
		}
		return cached.getContents();
	}

	/**
	 * Retrieve static files
	 * @param vhost
	 * @param source
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private byte[] retrieveBinaryFile(VirtualHost vhost, String source, String path) throws IOException {
		String fileId = getHashCode(source,path);
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		InputStream stream = mongo.getStaticFile((String) vhost.getContext("database"), STATIC_COLLECTION, query);
		if(stream != null) {
			return sun.misc.IOUtils.readFully(stream, -1, true);
		} else {
			URL url = new URL(source + path);
			int separator = path.lastIndexOf('/');
			Map<String,Object> metadata = new HashMap<String,Object>();
			metadata.put("folder", "/" + path.substring(0,separator));

			Map<String,Object> file = new HashMap<String,Object>();
			file.put("filename", "/" + path);
			file.put("_id", fileId);
			file.put("metadata", metadata);
			byte[] result = sun.misc.IOUtils.readFully(url.openStream(), -1, true);
			mongo.addStaticFile((String) vhost.getContext("database"), STATIC_COLLECTION, file, new ByteArrayInputStream(result));
			return result;
		}
	}

	private String getHashCode(String... sources) {
		String result = "";
		for(String part : sources) {
			result += part.hashCode();
		}
		return result;
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
