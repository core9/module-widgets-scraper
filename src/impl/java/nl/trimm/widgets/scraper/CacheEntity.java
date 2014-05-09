package nl.trimm.widgets.scraper;

import java.util.Map;

import io.core9.plugin.database.repository.AbstractCrudEntity;
import io.core9.plugin.database.repository.Collection;
import io.core9.plugin.database.repository.CrudEntity;

@Collection("core.scraper.cache")
public class CacheEntity extends AbstractCrudEntity implements CrudEntity {
	private Map<String,Object> contents;
	private String uri;
	
	public Map<String,Object> getContents() {
		return this.contents;
	}
	
	public void setContents(Map<String,Object> contents) {
		this.contents = contents;
	}
	
	public String getUri() {
		return this.uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
}
