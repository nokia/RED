/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordInLibrarySourceHyperlinkTest;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class KeywordsAutoDiscovererTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordInLibrarySourceHyperlinkTest.class);

    private final RobotModel model = new RobotModel();

    private IFile library;

    private LibrarySpecification libSpec;

    private RobotProject project;

    @Before
    public void before() throws Exception {
        final ReferencedLibrary lib = new ReferencedLibrary();
        lib.setType(LibraryType.PYTHON.toString());
        lib.setName("testlib");
        lib.setPath(projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);
        projectProvider.configure(config);
        final String libFileLines[] = { "#comment", "def kw_x():", "  return 0", "def   kw_abc ():", "  return 0" };
        library = projectProvider.createFile("testlib.py", libFileLines);

        libSpec = new LibrarySpecification();
        libSpec.setName("testlib");
        libSpec.setSourceFile(library);

        project = model.createRobotProject(projectProvider.getProject());
        project.setStandardLibraries(new HashMap<String, LibrarySpecification>());
        project.setReferencedLibraries(ImmutableMap.of(lib, libSpec));
    }

    @Test
    public void testIfKeywordsAreFound() throws Exception {
        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("testlib.Kw X", library, 1, 4, 4);
        verifyKwSource("testlib.Kw Abc", library, 3, 6, 6);
    }

    private void verifyKwSource(final String qualifiedKwName, final IFile expectedFile, final int expectedLine,
            final int expectedOffset, final int expectedLength) {
        final Optional<RobotDryRunKeywordSource> kwSource = project.getKeywordSource(qualifiedKwName);
        assertThat(kwSource.isPresent()).isTrue();
        assertThat(kwSource.get().getFilePath()).isEqualTo(expectedFile.getLocation().toOSString());
        assertThat(kwSource.get().getLine()).isEqualTo(expectedLine);
        assertThat(kwSource.get().getOffset()).isEqualTo(expectedOffset);
        assertThat(kwSource.get().getLength()).isEqualTo(expectedLength);
    }

}
