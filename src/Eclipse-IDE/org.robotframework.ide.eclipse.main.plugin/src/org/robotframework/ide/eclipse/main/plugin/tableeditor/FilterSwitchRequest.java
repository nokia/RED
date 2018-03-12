/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

public class FilterSwitchRequest {

    private final String id;
    private final String newFilter;

    public FilterSwitchRequest(final String id, final String newFilter) {
        this.id = id;
        this.newFilter = newFilter;
    }

    public String getId() {
        return id;
    }

    public String getNewFilter() {
        return newFilter;
    }
}