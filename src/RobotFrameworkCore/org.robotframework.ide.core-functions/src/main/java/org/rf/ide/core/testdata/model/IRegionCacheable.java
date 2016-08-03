/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

/**
 * @author wypych
 */
public interface IRegionCacheable<T> {

    public FileRegion getRegion();

    public T getCached();
}
