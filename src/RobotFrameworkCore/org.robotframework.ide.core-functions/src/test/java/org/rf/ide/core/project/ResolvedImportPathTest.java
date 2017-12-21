/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.rf.ide.core.executor.RedSystemProperties;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;

import com.google.common.collect.ImmutableMap;

public class ResolvedImportPathTest {

    @Test(expected = MalformedPathImportException.class)
    public void exceptionIsThrown_whenGivenPathIsInvalidUri() {
        createResolved(":path/somewhere", Collections.emptyMap());
    }

    @Test
    public void noResolvedPathIsProvided_whenSomeParametersAreNotPossibleToBeResolved() {
        final Map<String, String> parameters = ImmutableMap.of("${var1}", "val1", "${var2}", "val2");

        assertThat(createResolved("${var}/path/somewhere", parameters)).is(absent());
        assertThat(createResolved("${var}/${var1}/path/somewhere", parameters)).is(absent());
        assertThat(createResolved("${var}/${var2}/path/somewhere", parameters)).is(absent());
    }

    @Test
    public void resolvedPathIsProvided_whenAllParametersAreResolvable() throws URISyntaxException {
        final Map<String, String> parameters = ImmutableMap.of("${var1}", "val1", "${var2}", "val2");

        assertThat(createResolved("val/path/somewhere", parameters))
                .has(elementContained(new ResolvedImportPath(new URI("val/path/somewhere"))));
        assertThat(createResolved("${var1}/path/somewhere", parameters))
                .has(elementContained(new ResolvedImportPath(new URI("val1/path/somewhere"))));
        assertThat(createResolved("${var2}/path/somewhere", parameters))
                .has(elementContained(new ResolvedImportPath(new URI("val2/path/somewhere"))));
        assertThat(createResolved("${var1}/${var2}/path/somewhere", parameters))
                .has(elementContained(new ResolvedImportPath(new URI("val1/val2/path/somewhere"))));
    }

    @Test
    public void testPathsResolution_whenResolvedPathIsRelative_inWindows() {
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final Map<String, String> parameters = Collections.emptyMap();
        final ResolvedImportPath resolvedPath = createResolved("relative/path", parameters).get();
        final URI uri = resolvedPath.resolveInRespectTo(new File("c:/some/location").toURI());

        assertThat(uri).isEqualTo(new File("c:/some/relative/path/").toURI());
    }

    @Test
    public void testPathsResolution_whenResolvedPathIsRelative_inUnix() {
        assumeFalse(RedSystemProperties.isWindowsPlatform());

        final Map<String, String> parameters = Collections.emptyMap();
        final ResolvedImportPath resolvedPath = createResolved("relative/path", parameters).get();
        final URI uri = resolvedPath.resolveInRespectTo(new File("/some/location").toURI());

        assertThat(uri).isEqualTo(new File("/some/relative/path/").toURI());
    }

    private static Optional<ResolvedImportPath> createResolved(final String path,
            final Map<String, String> parameters) {
        return ResolvedImportPath.from(ImportPath.from(path), parameters);
    }

    private static <T> Condition<Optional<? extends T>> absent() {
        return new Condition<Optional<? extends T>>() {

            @Override
            public boolean matches(final Optional<? extends T> optional) {
                return !optional.isPresent();
            }
        };
    }

    private static <T> Condition<Optional<? extends T>> elementContained(final T element) {
        return new Condition<Optional<? extends T>>() {

            @Override
            public boolean matches(final Optional<? extends T> optional) {
                return optional.isPresent() && optional.get().equals(element);
            }
        };
    }
}
