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
import java.util.Optional;

import org.rf.ide.core.RedURI;
import org.rf.ide.core.testdata.model.RobotExpressions;

public final class ResolvedImportPath {

    public static Optional<ResolvedImportPath> from(final ImportPath importPath) throws URISyntaxException {
        return from(importPath, Collections.emptyMap());
    }

    public static Optional<ResolvedImportPath> from(final ImportPath importPath, final Map<String, String> parameters)
            throws URISyntaxException {
        final String path = importPath.getPath();

        if (RobotExpressions.isParameterized(path)) {
            final String resolvedPath = RobotExpressions.resolve(parameters, path);
            if (RobotExpressions.isParameterized(resolvedPath)) {
                return Optional.empty();
            } else {
                return Optional.of(create(resolvedPath));
            }
        } else {
            return Optional.of(create(path));
        }
    }

    private static ResolvedImportPath create(final String path) throws URISyntaxException {
        return new ResolvedImportPath(RedURI.fromString(path));
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
}
