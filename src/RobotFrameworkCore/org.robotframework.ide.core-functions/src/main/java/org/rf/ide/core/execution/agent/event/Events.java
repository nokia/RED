/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.RedURI;

class Events {

    static URI toFileUri(final String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            final String escaped = RedURI.URI_SPECIAL_CHARS_ESCAPER.escape(source);
            return new URI("file://" + (escaped.startsWith("/") ? "" : "/") + escaped.replaceAll("\\\\", "/"));
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    static List<String> ensureListOfStrings(final List<?> list) {
        return list.stream().map(String.class::cast).collect(toList());
    }

    static List<Map<String, Object>> ensureListOfOrderedMapOfStringsToObjects(final List<?> list) {
        return list.stream().map(Map.class::cast).map(Events::ensureOrderedMapOfStringsToObjects).collect(toList());
    }

    static Map<String, Object> ensureOrderedMapOfStringsToObjects(final Map<?, ?> map) {
        final Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put((String) key, value));
        return result;
    }

    static Map<String, String> ensureOrderedMapOfStringsToStrings(final Map<?, ?> map) {
        final Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put((String) key, (String) value));
        return result;
    }
}
