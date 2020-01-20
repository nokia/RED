/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;

public class StandardLibraryLibdocGeneratorTest {

    @Test
    public void noAdditionalPathsAreProvidedToEnvironment_evenIfThereAreSomeProvidedToGenerator() {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);

        final IFile file = mock(IFile.class);
        when(file.getLocation()).thenReturn(new Path("/some/path"));
        
        final EnvironmentSearchPaths additionalPaths = new EnvironmentSearchPaths();
        additionalPaths.addPythonPath("pp1");
        additionalPaths.addPythonPath("pp2");
        additionalPaths.addClassPath("cp1");
        additionalPaths.addClassPath("cp2");

        final StandardLibraryLibdocGenerator generator = new StandardLibraryLibdocGenerator("library",
                new ArrayList<>(), file, LibdocFormat.XML);
        generator.generateLibdoc(env, additionalPaths);

        verify(env).createLibdoc(eq("library"), eq(new File("/some/path")), eq(LibdocFormat.XML),
                argThat(hasNoPaths()));
    }

    private static EmptyPathsMatcher hasNoPaths() {
        return new EmptyPathsMatcher();
    }

    private static class EmptyPathsMatcher implements ArgumentMatcher<EnvironmentSearchPaths> {

        @Override
        public boolean matches(final EnvironmentSearchPaths paths) {
            return !paths.hasPythonPaths() && !paths.hasClassPaths();
        }
    }
}
