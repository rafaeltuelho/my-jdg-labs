/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tools.examples.datagrid;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.cache.annotation.CacheRemoveAll;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import javax.inject.Named;

import org.infinispan.AdvancedCache;
import org.infinispan.context.Flag;
import org.infinispan.eviction.EvictionStrategy;
import org.jboss.tools.examples.model.Member;
import org.richfaces.cdi.push.Push;

/**
 * <p>The greeting cache manager.</p>
 * <p/>
 * <p>This manager is used to collect informations on the greeting cache and to clear it's content if needed.</p>
 *
 * @author Kevin Pollet <pollet.kevin@gmail.com> (C) 2011
 * @see CacheRemoveAll
 */
@Named
@ApplicationScoped
public class MembersCacheManager {
	
	@Inject
	private Logger log;

    @Inject
    @Push(topic = "pushCdi")
    Event<String> pushEvent;
	
    @Inject
    @MembersClusteredCache
    private AdvancedCache<String, Member> cache;

    public String getCacheName() {
        return cache.getName();
    }

    public int getNumberOfEntries() {
        return cache.size();
    }

    public int getNumberOfEntriesSkipCacheLoad() {
        return cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).size();
    }

    public EvictionStrategy getEvictionStrategy() {
        return cache.getCacheConfiguration().eviction().strategy();
    }

    public int getEvictionMaxEntries() {
        return cache.getCacheConfiguration().eviction().maxEntries();
    }

    public List<Member> getCachedValues() {
        return new ArrayList<Member>(cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD).values());
    }

    public void clearCache() {
    	cache.clear();
    }

    public void onMembersCacheChange(@Observes(notifyObserver=Reception.ALWAYS) final @Cached Member member) {
    	log.info("new Member event fired from ClusteredCacheListener!!!");
        pushEvent.fire(String.format("New member added to the cache: %s (email: %s)", member.getName(), member.getEmail()));
    }
    
}
