/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class RedThemeTest {

    @Test
    public void allRedThemeColorsAreDefinedInPreferenceStylesCss() throws Exception {
        final List<String> colorIds = extractRedThemeColorIds();
        final String styles = readPreferenceStylesFile("resources/css/dark/preferencestyle.css");

        assertThat(colorIds).hasSize(19);
        for (final String colorId : colorIds) {
            assertThat(styles).containsPattern("\'" + colorId + "=\\d+,\\d+,\\d+\'");
        }
    }

    private List<String> extractRedThemeColorIds() throws IllegalAccessException {
        final List<Field> redColorNameFields = Arrays.stream(RedTheme.class.getDeclaredFields())
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .filter(f -> f.getType().isAssignableFrom(String.class))
                .filter(f -> f.getName().endsWith("_COLOR"))
                .collect(Collectors.toList());
        final List<String> colorIds = new ArrayList<String>();
        for (final Field field : redColorNameFields) {
            field.setAccessible(true);
            final String id = (String) field.get(new RedTheme());
            if (id.startsWith("org.robotframework.red.")) {
                colorIds.add(id);
            }
        }
        return colorIds;
    }

    private String readPreferenceStylesFile(final String styleFilePath) throws Exception {
        final IPath path = new Path("/plugin").append(RedPlugin.PLUGIN_ID).append(styleFilePath);
        final URL url = new URI("platform", null, path.toString(), null).toURL();
        try (InputStream in = url.openStream(); InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

}
