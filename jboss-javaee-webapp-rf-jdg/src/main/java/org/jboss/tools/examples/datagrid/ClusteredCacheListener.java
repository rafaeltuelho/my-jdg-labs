package org.jboss.tools.examples.datagrid;

import java.util.logging.Logger;

import javax.enterprise.event.Event;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.Annotateds;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStarted;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.jboss.tools.examples.model.Member;


@Listener(clustered=true)
public class ClusteredCacheListener {
	
	private Logger log;
	private Event<Member> memberEventSrc;
	
	public ClusteredCacheListener() {
		log = Logger.getLogger(ClusteredCacheListener.class.getName());
		memberEventSrc = BeanProvider.getContextualReference(Event.class, false);
	}

	@CacheEntryRemoved
	public void onCacheRemovedEntry(CacheEntryRemovedEvent<String, Member> event){
		log.info(" entry removed to the " + event.getCache().getName());
		log.info(" entry removed value: " + event.getValue().getEmail());
	}

	@CacheEntryCreated
	public void onCacheCreatedEntry(CacheEntryCreatedEvent<String, Member> event){
		log.info(" new entry added to the " + event.getCache().getName());
		log.info(" \t entry created value: " + event.getValue().getEmail());
		
		memberEventSrc.select(Member.class, new CachedQualifier()).fire(event.getValue());
	}

	@CacheStarted
    public void onCacheStarted(CacheStartedEvent event)
    {
		log.info("Cache started.  Details = " + event);
    }	
	
}
