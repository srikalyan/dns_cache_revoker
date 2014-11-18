package com.srisnotes.dnscache.api.impl;

import com.srisnotes.dnscache.api.DNSCacheEntry;
import com.srisnotes.dnscache.api.Revoker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a reflection based implementation of Revoker
 */
public class RevokerImpl implements Revoker {
    private static final String CACHE_NAME = "addressCache";
    private static final String CACHE_FIELD = "cache";
    private static final String EXPIRATION = "expiration";
    private static final String ADDRESS = "address";

    private static final Log logger = LogFactory.getLog(RevokerImpl.class);

    @Override
    public DNSCacheEntry getDNSCacheEntry(String hostname) {
        if (hostname == null) {
            logger.debug("hostname parameter is null so returning null!");
            return null;
        }

        try {
            final Map<String, Object> cache = getAddressCacheMap();

            for (Map.Entry<String, Object> entry : cache.entrySet()) {
                if (hostname.equals(entry.getKey())) {

                    final Object cacheEntry = entry.getValue();
                    final Class cacheEntryClass = cacheEntry.getClass();
                    final Field expirationField = cacheEntryClass.getDeclaredField(EXPIRATION);
                    expirationField.setAccessible(true);

                    final long expires = (Long) expirationField.get(cacheEntry);
                    final Field addressField = cacheEntryClass.getDeclaredField(ADDRESS);
                    addressField.setAccessible(true);
                    return new DNSCacheEntry(entry.getKey(),
                                             new Date(expires),
                                             Arrays.asList(
                                                 (InetAddress[]) addressField.get(cacheEntry)));
                }
            }
        } catch (NoSuchFieldException e) {
            logger.info("NoSuchFieldException occurred which should not have happened", e);
            logger.debug(e, e);
        } catch (IllegalAccessException e) {
            logger.info("IllegalAccessException occurred which should not have happened", e);
            logger.debug(e, e);
        }

        return null;
    }

    @Override
    public boolean revokeDNSCacheEntry(String hostname) {
        if (hostname == null) {
            logger.debug("hostname parameter is null so returning false!");
            return false;
        }

        try {
            final Map<String, Object> cache = getAddressCacheMap();
            final Iterator<Map.Entry<String, Object>> cacheIterator = cache.entrySet().iterator();
            boolean found = false;

            while (cacheIterator.hasNext()) {
                Map.Entry<String, Object> entry = cacheIterator.next();
                if (hostname.equals(entry.getKey())) {
                    logger.debug("Found the entry for the hostname so removing it");
                    cacheIterator.remove();
                    found = true;
                }
            }

            return found;
        } catch (NoSuchFieldException e) {
            logger.info("NoSuchFieldException occurred which should not have happened", e);
            logger.debug(e, e);
        } catch (IllegalAccessException e) {
            logger.info("IllegalAccessException occurred which should not have happened", e);
            logger.debug(e, e);
        }

        return false;
    }

    private Map<String, Object> getAddressCacheMap()
        throws NoSuchFieldException, IllegalAccessException {
        final Field cacheName = InetAddress.class.getDeclaredField(CACHE_NAME);
        cacheName.setAccessible(true);

        final Object addressCache = cacheName.get(null);
        final Class addressCacheClass = addressCache.getClass();
        final Field cacheField = addressCacheClass.getDeclaredField(CACHE_FIELD);
        cacheField.setAccessible(true);

        return (Map<String, Object>) cacheField.get(addressCache);
    }
}
