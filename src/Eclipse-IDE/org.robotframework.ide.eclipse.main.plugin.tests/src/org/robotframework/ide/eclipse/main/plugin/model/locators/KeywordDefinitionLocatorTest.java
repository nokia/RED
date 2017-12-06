/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createRefLib;
import static org.robotframework.ide.eclipse.main.plugin.project.library.Libraries.createStdLib;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class KeywordDefinitionLocatorTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordDefinitionLocatorTest.class);

    private RobotModel robotModel;

    @Before
    public void beforeTest() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();
    }

    @After
    public void afterTest() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void libraryKeywordsFromDefinitionAreDetected() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinitionInLibraries(robotProject, nonAccessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - StdLib", "kw2 - StdLib", "kw3 - RefLib", "kw4 - RefLib");
    }

    @Test
    public void libraryKeywordsFromDefinitionAreDetected_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinitionInLibraries(robotProject, limitedNonAccessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - StdLib", "kw2 - StdLib");
    }

    @Test
    public void localKeywordsAreDetected() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot", "*** Keywords ***", "localKw1", "localKw2");

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(localDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("localKw1 - suite.robot", "localKw2 - suite.robot");
    }

    @Test
    public void localKeywordsAreDetected_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot", "*** Keywords ***", "localKw1", "localKw2");

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(limitedLocalDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("localKw1 - suite.robot");
    }

    @Test
    public void localKeywordsAreDetected_whenKeywordISDefinedInImportedResources() throws Exception {
        projectProvider.createFile("res1.robot", "*** Keywords ***", "res1Kw1", "res1Kw2", "*** Settings ***",
                "Resource  res2.robot");
        projectProvider.createFile("res2.robot", "*** Keywords ***", "res2Kw1", "res2Kw2");
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource  res1.robot");

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(localDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("res1Kw1 - res1.robot", "res1Kw2 - res1.robot", "res2Kw1 - res2.robot",
                "res2Kw2 - res2.robot");
    }

    @Test
    public void libraryKeywordsAreDetected() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Library  StdLib",
                "Library  RefLib");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(accessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - StdLib", "kw2 - StdLib", "kw3 - RefLib", "kw4 - RefLib");
    }

    @Test
    public void libraryKeywordsAreDetected_whenLibraryIsAccessibleWithoutImport() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("BuiltIn", "Log", "Log Many"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw1", "kw2"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(accessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("Log - BuiltIn", "Log Many - BuiltIn");
    }

    @Test
    public void libraryKeywordsAreDetected_whenLibraryIsImportedInImportedResources() throws Exception {
        projectProvider.createFile("res1.robot", "*** Settings ***", "Library  StdLib", "Resource  res2.robot");
        projectProvider.createFile("res2.robot", "*** Settings ***", "Library  RefLib");
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource  res1.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(accessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - StdLib", "kw2 - StdLib", "kw3 - RefLib", "kw4 - RefLib");
    }

    @Test
    public void libraryKeywordsAreDetected_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot", "*** Settings ***", "Library  StdLib",
                "Library  RefLib");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(limitedAccessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - StdLib", "kw2 - StdLib");
    }

    @Test
    public void libraryKeywordsAreNotDetected_whenLibraryIsNotAccessible() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(createStdLib("StdLib", "kw1", "kw2"));
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw3", "kw4"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file);
        locator.locateKeywordDefinition(nonAccessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).isEmpty();
    }

    @Test
    public void libraryKeywordsAreDetected_whenLibraryIsNotAccessibleButImportIsEnabled() throws Exception {
        final IFile file = projectProvider.createFile("suite.robot");

        final RobotProject robotProject = robotModel.createRobotProject(file.getProject());
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(createRefLib("RefLib", "kw1", "kw2"));

        final Set<String> visitedLibs = new HashSet<>();
        final KeywordDefinitionLocator locator = new KeywordDefinitionLocator(file, robotModel, true);
        locator.locateKeywordDefinition(nonAccessibleLibraryDetector(visitedLibs));
        assertThat(visitedLibs).containsOnly("kw1 - RefLib", "kw2 - RefLib");
    }

    private KeywordDetector nonAccessibleLibraryDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                visitedLibs.add(kwSpec.getName() + " - " + libSpec.getName());
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private KeywordDetector limitedNonAccessibleLibraryDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                visitedLibs.add(kwSpec.getName() + " - " + libSpec.getName());
                return visitedLibs.size() < 2 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private KeywordDetector accessibleLibraryDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                    final RobotSuiteFile exposingFile) {
                visitedLibs.add(kwSpec.getName() + " - " + libSpec.getName());
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private KeywordDetector limitedAccessibleLibraryDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                    final RobotSuiteFile exposingFile) {
                visitedLibs.add(kwSpec.getName() + " - " + libSpec.getName());
                return visitedLibs.size() < 2 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private KeywordDetector localDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                visitedLibs.add(keyword.getName() + " - " + file.getName());
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private KeywordDetector limitedLocalDetector(final Set<String> visitedLibs) {
        return new TestKeywordDetector() {

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                visitedLibs.add(keyword.getName() + " - " + file.getName());
                return ContinueDecision.STOP;
            }
        };
    }

    private static class TestKeywordDetector implements KeywordDetector {

        @Override
        public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
            return ContinueDecision.CONTINUE;
        }

        @Override
        public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAlias,
                final RobotSuiteFile exposingFile) {
            return ContinueDecision.CONTINUE;
        }

        @Override
        public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
            return ContinueDecision.CONTINUE;
        }
    }
}
