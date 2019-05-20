/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.PreferenceUpdater;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class LibrariesWatchHandlerTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static final String PYTHON_LIBRARY_NAME = "libTest";

    private static final String PYTHON_LIBRARY_FILE_NAME = "libTest.py";

    private static final String PYTHON_MODULE_LIBRARY_NAME = "moduleLib";

    private static final String PYTHON_MODULE_LIBRARY_INIT_FILE_NAME = "__init__.py";

    private static final String PYTHON_MODULE_LIBRARY_FILE_NAME = "moduleClass.py";

    private static final String JAVA_LIBRARY_NAME = "jlibTest";

    private static final String JAVA_LIBRARY_FILE_NAME = "jlibTest.java";

    private static File pythonLibraryFile = null;

    private static File pythonModuleLibraryFolder = null;

    private static File pythonModuleLibraryInitFile = null;

    private static File pythonModuleLibraryFile = null;

    private static File javaLibraryFile = null;

    @BeforeClass
    public static void setUp() throws IOException {
        pythonLibraryFile = testFolder.newFile(PYTHON_LIBRARY_FILE_NAME);
        pythonModuleLibraryFolder = testFolder.newFolder(PYTHON_MODULE_LIBRARY_NAME);
        pythonModuleLibraryInitFile = testFolder
                .newFile(PYTHON_MODULE_LIBRARY_NAME + File.separator + PYTHON_MODULE_LIBRARY_INIT_FILE_NAME);
        pythonModuleLibraryFile = testFolder
                .newFile(PYTHON_MODULE_LIBRARY_NAME + File.separator + PYTHON_MODULE_LIBRARY_FILE_NAME);
        testFolder.newFile(PYTHON_MODULE_LIBRARY_NAME + File.separator + "someFileInModule.txt");
        javaLibraryFile = testFolder.newFile(JAVA_LIBRARY_FILE_NAME);
    }

    @Test
    public void testRegisterPythonLibrary_whenLibraryIsSimpleFile() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_LIBRARY_NAME,
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);

        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        assertThat(librariesWatchHandler.getRegisteredPaths()).hasSize(1)
                .containsEntry(PYTHON_LIBRARY_FILE_NAME, pythonLibraryFile.getParentFile().getPath());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).hasSize(1)
                .containsEntry(referencedLibrary, new Path(pythonLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec)).hasSize(1)
                .containsOnly(pythonLibraryFile.getName());
    }

    @Test
    public void testRegisterPythonLibrary_whenMultipleClassesFromLibraryAreUsed() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME,
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass1",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        final ReferencedLibrary referencedLibrary3 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass2",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec3 = createNewLibSpec(referencedLibrary3);

        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);
        librariesWatchHandler.registerLibrary(referencedLibrary3, libSpec3);

        assertThat(librariesWatchHandler.getRegisteredPaths()).hasSize(1)
                .containsEntry(PYTHON_LIBRARY_FILE_NAME, pythonLibraryFile.getParentFile().getPath());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries().get(referencedLibrary1))
                .isEqualTo(new Path(pythonLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries().get(referencedLibrary2))
                .isEqualTo(new Path(pythonLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries().get(referencedLibrary3))
                .isEqualTo(new Path(pythonLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec1)).hasSize(1)
                .containsOnly(pythonLibraryFile.getName());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec2)).hasSize(1)
                .containsOnly(pythonLibraryFile.getName());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec3)).hasSize(1)
                .containsOnly(pythonLibraryFile.getName());
    }

    @Test
    public void testRegisterPythonLibrary_whenLibraryIsModule() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryInitFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);

        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        assertThat(librariesWatchHandler.getRegisteredPaths()).hasSize(2)
                .containsEntry(PYTHON_MODULE_LIBRARY_INIT_FILE_NAME, pythonModuleLibraryFolder.getPath())
                .containsEntry(PYTHON_MODULE_LIBRARY_FILE_NAME, pythonModuleLibraryFolder.getPath());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).hasSize(1)
                .containsEntry(referencedLibrary, new Path(pythonModuleLibraryInitFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec)).hasSize(2)
                .containsOnly(pythonModuleLibraryFile.getName(), pythonModuleLibraryInitFile.getName());
    }

    @Test
    public void testRegisterJavaLibrary() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(JAVA_LIBRARY_NAME,
                javaLibraryFile.getPath(), LibraryType.JAVA);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);

        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        assertThat(librariesWatchHandler.getRegisteredPaths()).hasSize(1)
                .containsEntry(JAVA_LIBRARY_FILE_NAME, javaLibraryFile.getParentFile().getPath());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).hasSize(1)
                .containsEntry(referencedLibrary, new Path(javaLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec)).hasSize(1)
                .containsOnly(javaLibraryFile.getName());
    }

    @Test
    public void testRegisterVirtualLibrary() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary("virtualLibTest", "",
                LibraryType.VIRTUAL);

        librariesWatchHandler.registerLibrary(referencedLibrary, new LibrarySpecification());

        assertThat(librariesWatchHandler.getRegisteredPaths()).isEmpty();
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).isEmpty();
        assertThat(librariesWatchHandler.getLibrarySpecifications().asMap()).isEmpty();
    }

    @Test
    public void testRegisterNonExistenceLibrary() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary("libTest",
                pythonModuleLibraryFolder.getPath() + "/libTest.py", LibraryType.PYTHON);

        librariesWatchHandler.registerLibrary(referencedLibrary, new LibrarySpecification());

        assertThat(librariesWatchHandler.getRegisteredPaths()).isEmpty();
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).isEmpty();
        assertThat(librariesWatchHandler.getLibrarySpecifications().asMap()).isEmpty();
    }

    @Test
    public void testRegisterPythonLibrary_whenTheSameLibSpecWithDifferentKeywordsIsRegistered() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_LIBRARY_NAME,
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);

        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        referencedLibrary = createNewReferencedLibrary(PYTHON_LIBRARY_NAME, pythonLibraryFile.getPath(),
                LibraryType.PYTHON);
        libSpec = createNewLibSpec(referencedLibrary);
        final KeywordSpecification kwSpec = createNewKeywordSpec("someNewKeyword", newArrayList("arg"));
        libSpec.setKeywords(newArrayList(kwSpec));

        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        assertThat(librariesWatchHandler.getRegisteredPaths()).hasSize(1)
                .containsEntry(PYTHON_LIBRARY_FILE_NAME, pythonLibraryFile.getParentFile().getPath());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).hasSize(1)
                .containsEntry(referencedLibrary, new Path(pythonLibraryFile.getPath()).toPortableString());
        assertThat(librariesWatchHandler.getLibrarySpecifications().size()).isEqualTo(1);
        assertThat(librariesWatchHandler.getLibrarySpecifications().get(libSpec)).hasSize(1)
                .containsOnly(pythonLibraryFile.getName());
        assertThat(librariesWatchHandler.getLibrarySpecifications().keySet().iterator().next().getKeywords().get(0))
                .isEqualTo(kwSpec);
    }

    @Test
    public void testUnregisterLibraries() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass1",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass2",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);

        librariesWatchHandler.unregisterLibraries(newArrayList(referencedLibrary1, referencedLibrary2));

        assertThat(librariesWatchHandler.getUnregisteredFiles()).hasSize(2)
                .containsOnly(pythonLibraryFile.getName(), pythonLibraryFile.getName());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).isEmpty();
        assertThat(librariesWatchHandler.getLibrarySpecifications().asMap()).isEmpty();
    }

    @Test
    public void testUnregisterModuleLibrary() {
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryInitFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);
        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        librariesWatchHandler.unregisterLibraries(newArrayList(referencedLibrary));

        assertThat(librariesWatchHandler.getUnregisteredFiles()).hasSize(2)
                .containsOnly(pythonModuleLibraryFile.getName(), pythonModuleLibraryInitFile.getName());
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).isEmpty();
        assertThat(librariesWatchHandler.getLibrarySpecifications().asMap()).isEmpty();
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsEnabled() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, true);

        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass1",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass2",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);
        librariesWatchHandler.setRebuildTasksQueueSizeBeforeBuilderInvoke(1);

        librariesWatchHandler.handleModifyEvent(PYTHON_LIBRARY_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().size()).isEqualTo(2);
        assertThat(librariesWatchHandler.getSpecificationsToRebuild().get(project)).containsOnly(libSpec1, libSpec2);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSize()).isEqualTo(0);
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsEnabledAndLibraryIsModule() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, true);

        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryInitFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);
        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);
        librariesWatchHandler.setRebuildTasksQueueSizeBeforeBuilderInvoke(1);

        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_INIT_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().size()).isEqualTo(1);
        assertThat(librariesWatchHandler.getSpecificationsToRebuild().get(project)).containsOnly(libSpec);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSizeAfterEachBuilderInvoke()).containsExactly(1);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSize()).isEqualTo(0);

        librariesWatchHandler.getSpecificationsToRebuild().clear();

        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().size()).isEqualTo(1);
        assertThat(librariesWatchHandler.getSpecificationsToRebuild().get(project)).containsOnly(libSpec);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSizeAfterEachBuilderInvoke()).containsExactly(1, 1);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSize()).isEqualTo(0);
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsEnabledAndDuplicatedTasksAreRemoved() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, true);

        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME,
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryFolder.getPath() + File.separator + PYTHON_MODULE_LIBRARY_INIT_FILE_NAME,
                LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);
        librariesWatchHandler.setRebuildTasksQueueSizeBeforeBuilderInvoke(8);

        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_FILE_NAME);
        for (int i = 1; i <= 7; i++) {
            librariesWatchHandler.handleModifyEvent(PYTHON_LIBRARY_FILE_NAME);
        }
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().size()).isEqualTo(2);
        assertThat(librariesWatchHandler.getSpecificationsToRebuild().get(project)).containsOnly(libSpec1, libSpec2);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSizeAfterEachBuilderInvoke()).containsExactly(8, 1);
        assertThat(librariesWatchHandler.getRebuildTasksQueueSize()).isEqualTo(0);
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsDisabled() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, false);

        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass1",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass2",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary1), libSpec1);
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary2), libSpec2);
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project, refLibs);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);

        librariesWatchHandler.handleModifyEvent(PYTHON_LIBRARY_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().asMap()).isEmpty();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec1)).isTrue();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec2)).isTrue();
        assertThat(libSpec1.isModified()).isTrue();
        assertThat(libSpec2.isModified()).isTrue();
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsDisabledAndDuplicatedEventsAreHandled() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, false);

        final ReferencedLibrary referencedLibrary1 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass1",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec1 = createNewLibSpec(referencedLibrary1);
        final ReferencedLibrary referencedLibrary2 = createNewReferencedLibrary(PYTHON_LIBRARY_NAME + ".PythonClass2",
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec2 = createNewLibSpec(referencedLibrary2);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary1), libSpec1);
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary2), libSpec2);
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project, refLibs);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        librariesWatchHandler.registerLibrary(referencedLibrary1, libSpec1);
        librariesWatchHandler.registerLibrary(referencedLibrary2, libSpec2);

        for (int i = 1; i <= 5; i++) {
            librariesWatchHandler.handleModifyEvent(PYTHON_LIBRARY_FILE_NAME);
        }
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().asMap()).isEmpty();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec1)).isTrue();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec2)).isTrue();
        assertThat(libSpec1.isModified()).isTrue();
        assertThat(libSpec2.isModified()).isTrue();
    }

    @Test
    public void testHandleModifyEvent_whenAutoReloadIsDisabledAndLibraryIsModule() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, false);

        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary), libSpec);
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project, refLibs);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_INIT_FILE_NAME);
        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().asMap()).isEmpty();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec)).isTrue();
        assertThat(libSpec.isModified()).isTrue();
    }

    @Test
    public void testRemoveDirtySpecs() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, false);

        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_MODULE_LIBRARY_NAME,
                pythonModuleLibraryInitFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(LibraryDescriptor.ofReferencedLibrary(referencedLibrary), libSpec);
        final IProject project = createNewProjectMock(true);
        final RobotProject robotProject = createNewRobotProject(project, refLibs);
        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        librariesWatchHandler.handleModifyEvent(PYTHON_MODULE_LIBRARY_INIT_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().asMap()).isEmpty();
        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec)).isTrue();
        assertThat(libSpec.isModified()).isTrue();

        librariesWatchHandler.removeDirtySpecs(refLibs.values());

        assertThat(librariesWatchHandler.isLibSpecDirty(libSpec)).isFalse();
    }

    @Test
    public void testHandleModifyEvent_whenProjectNotExists() {
        preferenceUpdater.setValue(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, true);

        final IProject project = createNewProjectMock(false);
        final RobotProject robotProject = createNewRobotProject(project);

        final DummyLibrariesWatchHandler librariesWatchHandler = new DummyLibrariesWatchHandler(robotProject);
        final ReferencedLibrary referencedLibrary = createNewReferencedLibrary(PYTHON_LIBRARY_NAME,
                pythonLibraryFile.getPath(), LibraryType.PYTHON);
        final LibrarySpecification libSpec = createNewLibSpec(referencedLibrary);
        librariesWatchHandler.registerLibrary(referencedLibrary, libSpec);

        librariesWatchHandler.handleModifyEvent(PYTHON_LIBRARY_FILE_NAME);
        librariesWatchHandler.execAllAwaitingMessages();

        assertThat(librariesWatchHandler.getSpecificationsToRebuild().asMap()).isEmpty();
        assertThat(librariesWatchHandler.getLibrarySpecifications().asMap()).isEmpty();
        assertThat(librariesWatchHandler.getRegisteredRefLibraries()).isEmpty();
    }

    private IProject createNewProjectMock(final boolean projectExists) {
        final IProject project = mock(IProject.class);
        when(project.exists()).thenReturn(projectExists);
        return project;
    }

    private RobotProject createNewRobotProject(final IProject project) {
        return createNewRobotProject(project, new HashMap<>());
    }

    private RobotProject createNewRobotProject(final IProject project,
            final Map<LibraryDescriptor, LibrarySpecification> referencedLibraries) {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(referencedLibraries);
        return robotProject;
    }

    private ReferencedLibrary createNewReferencedLibrary(final String name, final String path, final LibraryType type) {
        return ReferencedLibrary.create(type, name, path);
    }

    private LibrarySpecification createNewLibSpec(final ReferencedLibrary referencedLibrary) {
        final LibrarySpecification libSpec = LibrarySpecification.create(referencedLibrary.getName());
        final KeywordSpecification keywordSpec = createNewKeywordSpec("keyword1", newArrayList("arg1"));
        libSpec.setKeywords(newArrayList(keywordSpec));
        return libSpec;
    }

    private KeywordSpecification createNewKeywordSpec(final String name, final List<String> args) {
        final KeywordSpecification keywordSpec = KeywordSpecification.create(name);
        keywordSpec.setArguments(args);
        return keywordSpec;
    }

    static class DummyLibrariesWatchHandler extends LibrariesWatchHandler {

        private final Map<String, String> registeredPaths = new HashMap<>();

        private final List<String> unregisteredFiles = newArrayList();

        private final Multimap<IProject, LibrarySpecification> specificationsToRebuild = LinkedHashMultimap.create();

        private final List<Integer> rebuildTasksQueueSizeAfterEachBuilderInvoke = newArrayList();

        private int rebuildTasksQueueSizeBeforeBuilderInvoke = 0;

        public DummyLibrariesWatchHandler(final RobotProject robotProject) {
            super(robotProject);
        }

        @Override
        public void registerPath(final java.nio.file.Path dir, final String fileName) {
            registeredPaths.put(fileName, dir.toString());
        }

        @Override
        public void unregisterFile(final String fileName) {
            unregisteredFiles.add(fileName);
        }

        @Override
        protected void invokeLibrariesBuilder(final IProgressMonitor monitor,
                final Multimap<IProject, LibrarySpecification> groupedSpecifications) {
            int sleepCounter = 0;
            while (sleepCounter < 20 && getRebuildTasksQueueSize() < rebuildTasksQueueSizeBeforeBuilderInvoke
                    && specificationsToRebuild.isEmpty()) {
                sleep(100);
                sleepCounter++;
            }
            specificationsToRebuild.putAll(groupedSpecifications);
            rebuildTasksQueueSizeAfterEachBuilderInvoke.add(getRebuildTasksQueueSize());
        }

        public Map<String, String> getRegisteredPaths() {
            return registeredPaths;
        }

        public List<String> getUnregisteredFiles() {
            return unregisteredFiles;
        }

        public Multimap<IProject, LibrarySpecification> getSpecificationsToRebuild() {
            return specificationsToRebuild;
        }

        public void execAllAwaitingMessages() {
            while (Display.getDefault().readAndDispatch()) {
                // handle all events coming to UI
            }
        }

        private void sleep(final long millis) {
            try {
                Thread.sleep(millis);
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Shouldn't be interrupted!", e);
            }
        }

        public void setRebuildTasksQueueSizeBeforeBuilderInvoke(final int rebuildTasksQueueSizeBeforeBuilderInvoke) {
            this.rebuildTasksQueueSizeBeforeBuilderInvoke = rebuildTasksQueueSizeBeforeBuilderInvoke;
        }

        public List<Integer> getRebuildTasksQueueSizeAfterEachBuilderInvoke() {
            return rebuildTasksQueueSizeAfterEachBuilderInvoke;
        }

    }
}
