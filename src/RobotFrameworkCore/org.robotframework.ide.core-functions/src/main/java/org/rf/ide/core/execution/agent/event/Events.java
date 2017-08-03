/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.rf.ide.core.executor.RedURI;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

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

    static List<Map<Variable, VariableTypedValue>> extractVariableScopes(final List<?> arguments) {
        final List<Map<String, Object>> vars = Events.ensureListOfOrderedMapOfStringsToObjects(arguments);

        final List<Map<Variable, VariableTypedValue>> typedVars = new ArrayList<>();
        for (final Map<String, Object> frame : vars) {
            final Map<Variable, VariableTypedValue> typedScope = new LinkedHashMap<>();
            for (final String name : frame.keySet()) {
                final List<?> typeValScope = (List<?>) frame.get(name);
                final VariableScope scope = VariableScope.fromSimpleName((String) typeValScope.get(2));

                typedScope.put(new Variable(name, scope), reconstructTypesAndValues(typeValScope));
            }
            typedVars.add(typedScope);
        }
        return typedVars;
    }

    private static VariableTypedValue reconstructTypesAndValues(final List<?> typeAndVal) {
        final String type = (String) typeAndVal.get(0);
        final Object value = typeAndVal.get(1);

        if (value instanceof List<?>) {
            final List<Object> newValue = new ArrayList<>();
            for (final Object elem : ((List<?>) value)) {
                newValue.add(reconstructTypesAndValues((List<?>) elem));
            }
            return new VariableTypedValue(type, newValue);

        } else if (value instanceof Map<?, ?>) {
            final Map<Object, Object> newValue = new LinkedHashMap<>();
            for (final Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                newValue.put(entry.getKey(), reconstructTypesAndValues((List<?>) entry.getValue()));
            }
            return new VariableTypedValue(type, newValue);
        } else {
            return new VariableTypedValue(type, value);
        }
    }

    private static List<Map<String, Object>> ensureListOfOrderedMapOfStringsToObjects(final List<?> scopes) {
        return scopes.stream().map(Map.class::cast).map(Events::ensureOrderedMapOfStringsToObjects).collect(toList());
    }

    static List<String> ensureListOfStrings(final List<?> list) {
        return list.stream().map(String.class::cast).collect(Collectors.toList());
    }

    static Map<String, Object> ensureOrderedMapOfStringsToObjects(final Map<?, ?> map) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        map.entrySet().stream().forEach(e -> result.put((String) e.getKey(), e.getValue()));
        return result;
    }

    static Map<String, String> ensureOrderedMapOfStringsToStrings(final Map<?, ?> map) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        map.entrySet().stream().forEach(e -> result.put((String) e.getKey(), (String) e.getValue()));
        return result;
    }
}
