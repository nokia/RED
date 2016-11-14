/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.rf.ide.core.testdata.model.RobotExpressions;

import com.google.common.base.Optional;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public final class ResolvedImportPath {

    public static final Escaper URI_SPECIAL_CHARS_ESCAPER = Escapers.builder()
            .addEscape(' ', "%20")
            .addEscape('!', "%21")
            .addEscape('"', "%22")
            .addEscape('#', "%23")
            .addEscape('$', "%24")
            .addEscape('%', "%25")
            .addEscape('&', "%26")
            .addEscape('(', "%28")
            .addEscape(')', "%29")
            .addEscape(';', "%3b")
            .addEscape('@', "%40")
            .addEscape('^', "%5e")
            .build();

    public static Optional<ResolvedImportPath> from(final ImportPath importPath) {
        return from(importPath, Collections.<String, String> emptyMap());
    }

    public static Optional<ResolvedImportPath> from(final ImportPath importPath, final Map<String, String> parameters)
            throws MalformedPathImportException {
        try {
            final String path = importPath.getPath();

            if (RobotExpressions.isParameterized(path)) {
                final String resolvedPath = RobotExpressions.resolve(parameters, path);
                if (RobotExpressions.isParameterized(resolvedPath)) {
                    return Optional.<ResolvedImportPath> absent();
                } else {
                    return Optional.of(create(importPath, resolvedPath));
                }
            } else {
                return Optional.of(create(importPath, path));
            }

        } catch (final URISyntaxException e) {
            throw new MalformedPathImportException(e);
        }
    }

    private static ResolvedImportPath create(final ImportPath importPath, final String path) throws URISyntaxException {
        final String escapedPath = URI_SPECIAL_CHARS_ESCAPER.escape(path);
        final String escapedPathWithScheme = importPath.isAbsolute() ? "file:/" + escapedPath : escapedPath;
        return new ResolvedImportPath(new URI(escapedPathWithScheme.replaceAll("\\\\", "/")));
    }

    private final URI uri;

    public ResolvedImportPath(final URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public URI resolveInRespectTo(final URI locationUri) {
        return locationUri.resolve(uri);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ResolvedImportPath.class) {
            final ResolvedImportPath that = (ResolvedImportPath) obj;
            return this.uri.equals(that.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    public static class MalformedPathImportException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MalformedPathImportException(final Throwable t) {
            super(t);
        }
    }
}
