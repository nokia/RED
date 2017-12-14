/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class LibraryImportCollectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryImportCollectorTest.class);

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite_without_settings.robot",
                "*** Test Cases ***");
        projectProvider.createFile("suite_without_imports.robot",
                "*** Settings ***",
                "*** Test Cases ***");
        projectProvider.createFile("suite_with_resource_without_imports.robot",
                "*** Settings ***",
                "Resource  resources/resource_without_imports.robot",
                "*** Test Cases ***");
        projectProvider.createFile("suite_with_libraries.robot",
                "*** Settings ***",
                "Library  lib1",
                "Library  a/lib2",
                "Library  b/lib3",
                "*** Test Cases ***");
        projectProvider.createFile("suite_with_resource_with_libraries.robot",
                "*** Settings ***",
                "Library  suiteLib",
                "Resource  resources/resource_with_libraries.robot",
                "*** Test Cases ***");
        projectProvider.createFile("suite_with_resources_with_cycle.robot",
                "*** Settings ***",
                "Resource  resources/resource_with_cycle_1.robot");
        projectProvider.createFile("suite_with_incorrect_resources.robot",
                "*** Settings ***",
                "Resource  not_existing.robot",
                "Resource  resources");

        projectProvider.createDir("resources");
        projectProvider.createFile("resources/resource_without_imports.robot",
                "*** Settings ***");
        projectProvider.createFile("resources/resource_with_libraries.robot",
                "*** Settings ***",
                "Library  resLib1",
                "Library  resLib2");
        projectProvider.createFile("resources/resource_with_cycle_1.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_2.robot",
                "Library  resCycle1");
        projectProvider.createFile("resources/resource_with_cycle_2.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_1.robot",
                "Resource  resource_with_cycle_3.robot",
                "Library  resCycle2");
        projectProvider.createFile("resources/resource_with_cycle_3.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_2.robot",
                "Library  resCycle3");
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void emptyResultIsReturned_whenSuiteDoesNotContainSettingsSection() {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_without_settings.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).isEmpty();
    }

    @Test
    public void emptyResultIsReturned_whenSuiteSettingsSectionDoesNotContainImports() {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_without_imports.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).isEmpty();
    }

    @Test
    public void emptyResultIsReturned_whenSuiteSettingsSectionDoesNotContainImports2() {
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_resource_without_imports.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).isEmpty();
    }

    @Test
    public void emptyResultIsReturned_whenSuiteSettingsSectionDoesNotContainImports3() {
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_incorrect_resources.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).isEmpty();
    }

    @Test
    public void libraryImportsAreReturned_whenSuiteSettingsSectionContainsImports() {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_libraries.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).has(correctPathsOrNames(ImmutableMap.of(suite, newArrayList("lib1", "a/lib2", "b/lib3"))));
    }

    @Test
    public void libraryImportsAreReturned_whenSuiteSettingsSectionContainsImports2() {
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_resource_with_libraries.robot"));

        final RobotSuiteFile res = model
                .createSuiteFile(projectProvider.getFile("resources/resource_with_libraries.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).has(correctPathsOrNames(
                ImmutableMap.of(suite, newArrayList("suiteLib"), res, newArrayList("resLib1", "resLib2"))));
    }

    @Test
    public void libraryImportsAreReturned_whenSuiteSettingsSectionContainsImports3() {
        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_resources_with_cycle.robot"));

        final RobotSuiteFile res1 = model
                .createSuiteFile(projectProvider.getFile("resources/resource_with_cycle_1.robot"));

        final RobotSuiteFile res2 = model
                .createSuiteFile(projectProvider.getFile("resources/resource_with_cycle_2.robot"));

        final RobotSuiteFile res3 = model
                .createSuiteFile(projectProvider.getFile("resources/resource_with_cycle_3.robot"));

        final Map<RobotSuiteFile, List<LibraryImport>> imports = LibraryImportCollector
                .collectLibraryImportsIncludingNestedResources(suite);

        assertThat(imports).has(correctPathsOrNames(ImmutableMap.of(res1, newArrayList("resCycle1"), res2,
                newArrayList("resCycle2"), res3, newArrayList("resCycle3"))));
    }

    private Condition<Map<RobotSuiteFile, List<LibraryImport>>> correctPathsOrNames(
            final Map<RobotSuiteFile, List<String>> expectedPathsOrNames) {
        return new Condition<Map<RobotSuiteFile, List<LibraryImport>>>() {

            @Override
            public boolean matches(final Map<RobotSuiteFile, List<LibraryImport>> imports) {
                final Map<RobotSuiteFile, List<String>> actualPathsOrNames = imports.entrySet()
                        .stream()
                        .collect(toMap(entry -> entry.getKey(),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(LibraryImport::getPathOrName)
                                        .map(RobotToken::getText)
                                        .collect(toList())));
                return expectedPathsOrNames.equals(actualPathsOrNames);
            }
        };
    }
}
