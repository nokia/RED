/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;

class AbsoluteUriFinder {

    Optional<URI> find(final PathsProvider pathsProvider, final Map<String, String> variableMappings,
            final File importingFile, final String dependencyImportPath) {

        if (!importingFile.exists()) {
            throw new IllegalStateException("Current file should exist");
        }
        final ImportPath importPath = ImportPath.from(dependencyImportPath);
        final Optional<ResolvedImportPath> resolvedImportPath = ResolvedImportPath.from(importPath, variableMappings);
        if (!resolvedImportPath.isPresent()) {
            throw new IllegalStateException("Unable to resolve parameterized import path");
        }
        return new ImportSearchPaths(pathsProvider).findAbsoluteUri(importingFile.toURI(), resolvedImportPath.get());
    }
}
