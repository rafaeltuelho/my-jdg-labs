package org.infinispan.quickstart.cdi;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.quickstart.cdi.config.GreetingCache;

@Startup
@Singleton
@ApplicationScoped
public class CachePreLoader {
	@Inject
	private Logger log;

	@Inject
	@GreetingCache
	private Cache<String, String> cache;
	
	@PostConstruct
	private void init(){
		log.info("\n\t Initializing Distributed Cache node\n");
		
		for (int i = 0; i < 10; i++) {
			cache.put("k" + i, java.util.UUID.randomUUID().toString());			
		}
		
		log.info("\t\t " + cache.size());
	}
	
	@PreDestroy
	private void cleanUp(){
		log.info("\n\t Destroying Distributed Cache instance node\n");
		cache.clear();
	}
}
