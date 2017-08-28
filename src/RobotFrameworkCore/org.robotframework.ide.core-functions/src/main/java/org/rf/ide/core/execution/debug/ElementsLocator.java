/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.execution.debug.contexts.TestCaseContext;

public interface ElementsLocator {

    /**
     * The paths coming to RED may be defined in different remote file system, so
     * translation to local paths is required.
     * 
     * @param remotePath
     *            Path in some file system where tests are executed
     * @param isDirectory
     *            True if given path should be treated as a directory path, false for file path
     * @return Path in local file system
     */
    URI translate(URI remotePath, boolean isDirectory);

    SuiteContext findContextForSuite(final String suiteName, final URI path, boolean isDirectory,
            final URI currentLocalSuitePath);

    TestCaseContext findContextForTestCase(String testCaseName, URI currentSuitePath, Optional<String> template);

    KeywordContext findContextForKeyword(String libOrResourceName, String keywordName, URI currentSuitePath,
            Set<URI> loadedResources);

}
