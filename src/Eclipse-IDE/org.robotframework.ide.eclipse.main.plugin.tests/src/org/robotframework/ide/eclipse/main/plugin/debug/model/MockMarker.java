/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Streams.forEachPair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;


public class MockMarker implements IMarker {

    private boolean deleted;

    private final Map<String, Object> attributes = new HashMap<>();

    private final IResource resource;

    public MockMarker() {
        this(null);
    }

    public MockMarker(final IResource resource) {
        this.resource = resource;
    }

    @Override
    public <T> T getAdapter(final Class<T> adapter) {
        return null;
    }

    @Override
    public void delete() {
        this.deleted = true;
    }

    @Override
    public boolean exists() {
        return !deleted;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public IResource getResource() {
        return resource;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public boolean isSubtypeOf(final String superType) {
        return false;
    }

    @Override
    public Object getAttribute(final String attributeName) {
        return attributes.get(attributeName);
    }

    @Override
    public int getAttribute(final String attributeName, final int defaultValue) {
        return attributes.get(attributeName) instanceof Integer ? (int) attributes.get(attributeName) : defaultValue;
    }

    @Override
    public String getAttribute(final String attributeName, final String defaultValue) {
        return attributes.get(attributeName) instanceof String ? (String) attributes.get(attributeName) : defaultValue;
    }

    @Override
    public boolean getAttribute(final String attributeName, final boolean defaultValue) {
        return attributes.get(attributeName) instanceof Boolean ? (Boolean) attributes.get(attributeName)
                : defaultValue;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Object[] getAttributes(final String[] attributeNames) {
        return Stream.of(attributeNames).map(attr -> attributes.get(attr)).toArray();
    }

    @Override
    public void setAttribute(final String attributeName, final int value) {
        attributes.put(attributeName, value);
    }

    @Override
    public void setAttribute(final String attributeName, final Object value) {
        attributes.put(attributeName, value);
    }

    @Override
    public void setAttribute(final String attributeName, final boolean value) {
        attributes.put(attributeName, value);
    }

    @Override
    public void setAttributes(final String[] attributeNames, final Object[] values) {
        forEachPair(Stream.of(attributeNames), Stream.of(values), (attr, val) -> attributes.put(attr, val));
    }

    @Override
    public void setAttributes(final Map<String, ? extends Object> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }
}
