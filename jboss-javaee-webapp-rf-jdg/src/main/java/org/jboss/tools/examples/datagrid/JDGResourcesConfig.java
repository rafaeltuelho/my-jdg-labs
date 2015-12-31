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

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.tools.examples.model.Member;

/**
 * This is the configuration class for DG.
 */
public class JDGResourcesConfig {

	@Inject
	private Logger log;
	
	final static String JDG_REMOTE_HOST_ADDR = "127.0.0.1";
	final static int JDG_REMOTE_HOST_PORT = 11222;
	
//    @ConfigureCache("members-clustered-cache")
    @MembersClusteredCache
    @Produces
    public Configuration membersClusteredCache() {
    	log.info("\n\t ->>> Configure a 'CLUSTERED-cache' with the following settings: ");
    	
    	
        Configuration config =  new ConfigurationBuilder()
                .eviction() // To ensure that a single copy of an entry remains, either in memory or in a cache store, use passivation in conjunction with eviction.
                	.strategy(EvictionStrategy.LRU)
                	.maxEntries(10000)
//        		.expiration() // Expired entries, unlike evicted entries, are removed globally, which removes them from memory, cache stores and the cluster.
//        			.lifespan(60, TimeUnit.SECONDS)
                .clustering()
                	.cacheMode(CacheMode.REPL_SYNC)
                	.sync()
                	.stateTransfer()
                	.fetchInMemoryState(true)
                	.awaitInitialTransfer(true)
                .persistence()
                	.passivation(true) // when enabled the data is written to the disk only during eviction...
                	.addSingleFileStore()
                		.async() // enable write-behind behavior
                			.enable()
                		.shared(true)
                		.preload(true) // load the cachestore content during startup
                		.fetchPersistentState(true) 
                		.ignoreModifications(false)
                		.purgeOnStartup(false)
                		.location(System.getProperty("java.io.tmpdir"))
                	.singleton()
                		.enabled(false)
                		.pushStateWhenCoordinator(true)
                		.pushStateTimeout(20000)
                .build();
        
        log.info("\n\n\t" + config.toString() + "\n");
        
        return config;
    }    

    @Produces
    @MembersClusteredCache
    @ApplicationScoped
    private AdvancedCache<String, Member> membersClusteredCache(@MembersClusteredCache EmbeddedCacheManager cacheManager){
    	log.info("\n\t ->>> Builds the CLUSTERED members-cache's \n");
    	
    	Cache<String, Member> cache = cacheManager.getCache("members-cache");
    	cache.getAdvancedCache().addListener(new ClusteredCacheListener());
    	return cache.getAdvancedCache();
    }

    @Produces
    @MembersClusteredCache
    @ApplicationScoped
    private RemoteCache<String, Member> membersRemoteCache(@MembersRemoteCache RemoteCacheManager cacheManager){
    	log.info("\n\t ->>> Builds the CLUSTERED members-cache's \n");
    	
    	RemoteCache<String, Member> cache = cacheManager.getCache("members-remote-cache");
    	return cache;
    }
    
    @Produces
    @MembersClusteredCache
    @ApplicationScoped
    public EmbeddedCacheManager membersClusteredCacheManager(@MembersClusteredCache Configuration configuration) {
    	log.info("\n\t ->>> Builds the members-CLUSTERED-cache's DefaultCacheManager \n");
    	
        GlobalConfiguration globalConfiguration = 
        		new GlobalConfigurationBuilder()
        			.clusteredDefault()
        			.transport()
        			.defaultTransport()
        			.addProperty("configurationFile", "jgroups-udp.xml")
        			.clusterName("members-clustered-datagrid")
        			.globalJmxStatistics()
        			.allowDuplicateDomains(true)
        			.build();
        
        return new DefaultCacheManager(globalConfiguration, configuration);
    }
    
    @Produces
    @MembersRemoteCache
    @ApplicationScoped
    public RemoteCacheManager membersRemoteCacheManager() {
    	log.info("\n\t ->>> Builds the members-REMOTE-cache's DefaultCacheManager \n");
    	
    	org.infinispan.client.hotrod.configuration.Configuration remoteConf = 
    			new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
    				.addServer().host(JDG_REMOTE_HOST_ADDR).port(JDG_REMOTE_HOST_PORT).build();
    	
        return new RemoteCacheManager(remoteConf);
    }
    
    public void closeMembersClusteredCacheManager(@Disposes @MembersClusteredCache EmbeddedCacheManager cm){
    	log.info("\n\t ->>> Stops members-CLUSTERED-cache's EmbeddedCacheManager \n");
    	cm.stop();
    }

    public void closeMembersRemoteCacheManager(@Disposes @MembersRemoteCache RemoteCacheManager cm){
    	log.info("\n\t ->>> Stops members-REMOTE-cache's RemoteCacheManager \n");
    	cm.stop();
    }
      
}
