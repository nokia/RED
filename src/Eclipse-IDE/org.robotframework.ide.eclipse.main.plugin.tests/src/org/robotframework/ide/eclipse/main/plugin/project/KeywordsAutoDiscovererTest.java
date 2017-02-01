/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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

    private RobotProject project;

    private Map<ReferencedLibrary, LibrarySpecification> libraries;

    @Before
    public void before() throws Exception {
        final RobotModel model = new RobotModel();
        project = model.createRobotProject(projectProvider.getProject());
        libraries = new HashMap<>();
    }

    @Test
    public void testDifferentKeywordsFromSameLibrary() throws Exception {
        libraries.putAll(createLibrary("TestLib",
                new String[] { "#comment", "def kw_x():", "  return 0", "def   kw_abc ():", "  pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("TestLib.Kw X", "TestLib.py", 1, 4, 4);
        verifyKwSource("TestLib.Kw Abc", "TestLib.py", 3, 6, 6);
    }

    @Test
    public void testSameKeywordsFromDifferentLibraries() throws Exception {
        libraries.putAll(createLibrary("First", new String[] { "#comment", "def kw_x():", "  pass" }));
        libraries.putAll(createLibrary("Second", new String[] { "def kw_x():", "  pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("First.Kw X", "First.py", 1, 4, 4);
        verifyKwSource("Second.Kw X", "Second.py", 0, 4, 4);
    }

    @Test
    public void testKeywordsDefinedWithDecorator() throws Exception {
        libraries.putAll(createLibrary("Decorated",
                new String[] { "import robot.api.deco", "from robot.api.deco import keyword",
                        "@robot.api.deco.keyword('Add ${x:\\d+}')", "def add(x):", "  pass", "@keyword(name='Deco')",
                        "def decorated_method():", "  pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("Decorated.Add ${x:\\d+}", "Decorated.py", 3, 4, 3);
        verifyKwSource("Decorated.Deco", "Decorated.py", 6, 4, 16);
    }

    @Test
    public void testKeywordsFromDynamicLibrary() throws Exception {
        libraries.putAll(createLibrary("DynaLib", new String[] { "class DynaLib:", "  def get_keyword_names(self):",
                "    return ['Dyna Kw', 'Other Kw']", "  def run_keyword(self, name, args):", "    pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("DynaLib.Dyna Kw", "DynaLib.py", 3, 6, 11);
        verifyKwSource("DynaLib.Other Kw", "DynaLib.py", 3, 6, 11);
    }

    @Test
    public void testKeywordsFromClassHierarchy() throws Exception {
        projectProvider.createFile("Parent.py", "class Parent:", "  def parent_kw(self, arg):", "    pass");
        libraries.putAll(createLibrary("Child", new String[] { "import Parent", "class Child(Parent.Parent):",
                "  def child_kw(self, arg):", "    pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("Child.Parent Kw", "Parent.py", 1, 6, 9);
        verifyKwSource("Child.Child Kw", "Child.py", 2, 6, 8);
    }

    @Test
    public void testKeywordsFromImportedLibraries() throws Exception {
        projectProvider.createFile("External.py", "def ex_kw(args):", "  pass");
        libraries.putAll(createLibrary("Internal",
                new String[] { "from External import ex_kw", "def int_kw(args):", "  pass" }));
        configureProject();

        new KeywordsAutoDiscoverer(project).start();

        verifyKwSource("Internal.Ex Kw", "External.py", 0, 4, 5);
        verifyKwSource("Internal.Int Kw", "Internal.py", 1, 4, 6);
    }

    private Map<ReferencedLibrary, LibrarySpecification> createLibrary(final String name, final String[] lines)
            throws IOException, CoreException {
        final ReferencedLibrary library = new ReferencedLibrary();
        library.setType(LibraryType.PYTHON.toString());
        library.setName(name);
        library.setPath(projectProvider.getProject().getName());

        final LibrarySpecification libSpec = new LibrarySpecification();
        final String filePath = library.getName() + ".py";
        final IFile libFile = projectProvider.createFile(filePath, lines);
        libSpec.setName(library.getName());
        libSpec.setSourceFile(libFile);

        return ImmutableMap.of(library, libSpec);
    }

    private void configureProject() throws IOException, CoreException {
        final RobotProjectConfig config = new RobotProjectConfig();
        for (final ReferencedLibrary referencedLibrary : libraries.keySet()) {
            config.addReferencedLibrary(referencedLibrary);
        }
        projectProvider.configure(config);
        project.setStandardLibraries(new HashMap<String, LibrarySpecification>());
        project.setReferencedLibraries(libraries);
    }

    private void verifyKwSource(final String qualifiedKwName, final String expectedFilePath, final int expectedLine,
            final int expectedOffset, final int expectedLength) {
        final Optional<RobotDryRunKeywordSource> kwSource = project.getKeywordSource(qualifiedKwName);
        assertThat(kwSource.isPresent()).isTrue();
        assertThat(kwSource.get().getFilePath())
                .isEqualTo(projectProvider.getFile(expectedFilePath).getLocation().toOSString());
        assertThat(kwSource.get().getLine()).isEqualTo(expectedLine);
        assertThat(kwSource.get().getOffset()).isEqualTo(expectedOffset);
        assertThat(kwSource.get().getLength()).isEqualTo(expectedLength);
    }

}
