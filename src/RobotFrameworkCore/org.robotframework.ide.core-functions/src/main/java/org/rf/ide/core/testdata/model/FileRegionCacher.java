/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

/**
 * Please considerate to move this logic to global region cacher, in case of refactor API will be
 * made
 * 
 * @author wypych
 */
@Beta
public class FileRegionCacher<T> {

    private final Set<IRegionCacheable<T>> cache = new LinkedHashSet<IRegionCacheable<T>>(0);

    public void register(final IRegionCacheable<T> newCacheable) {
        unregister(newCacheable);
        cache.add(newCacheable);
    }

    public void unregister(final IRegionCacheable<T> removeCacheable) {
        cache.remove(removeCacheable);
    }

    public List<IRegionCacheable<T>> findByLineNumber(final int lineNumber) {
        final List<IRegionCacheable<T>> inPosition = new ArrayList<IRegionCacheable<T>>(0);

        if (lineNumber > FilePosition.NOT_SET) {
            for (final IRegionCacheable<T> cacheElement : cache) {
                if (isRegionContainsLine(cacheElement, lineNumber)) {
                    inPosition.add(cacheElement);
                }
            }
        }

        return inPosition;
    }

    public List<IRegionCacheable<T>> findByOffset(final int offset) {
        final List<IRegionCacheable<T>> inPosition = new ArrayList<IRegionCacheable<T>>(0);

        if (offset > FilePosition.NOT_SET) {
            for (final IRegionCacheable<T> cacheElement : cache) {
                if (isRegionContainsOffset(cacheElement, offset)) {
                    inPosition.add(cacheElement);
                }
            }
        }

        return inPosition;
    }

    @VisibleForTesting
    public Set<IRegionCacheable<T>> getUnmodificableCacheContent() {
        return Collections.unmodifiableSet(cache);
    }

    private boolean isRegionContainsLine(final IRegionCacheable<T> region, final int line) {
        for (final FileRegion continuesRegion : region.getContinuousRegions()) {
            if (continuesRegion.containsLine(line)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRegionContainsOffset(final IRegionCacheable<T> region, final int offset) {
        for (final FileRegion continuesRegion : region.getContinuousRegions()) {
            if (continuesRegion.isInside(offset)) {
                return true;
            }
        }

        return false;
    }
}
