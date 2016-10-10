/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Function;

/**
 * @author Michal Anglart
 *
 */
class SimilaritiesAnalyst {

    private static final int DEFAULT_MAXIMUM_DISTANCE = 2;

    private final SimilarityWithLevenshteinDistance similaritiesAlgorithm = new SimilarityWithLevenshteinDistance();

    Collection<String> provideSimilarLibraries(final IFile suiteFile, final String libraryName) {
        final RobotProject robotProject = RedPlugin.getModelManager().createProject(suiteFile.getProject());
        final Collection<String> allLibs = newArrayList(
                transform(robotProject.getLibrariesSpecifications(), new Function<LibrarySpecification, String>() {

                    @Override
                    public String apply(final LibrarySpecification lib) {
                        return lib.getName();
                    }
                }));
        return similaritiesAlgorithm.onlyWordsWithinDistance(allLibs, libraryName, DEFAULT_MAXIMUM_DISTANCE);
    }

    Collection<String> provideSimilarAccessibleKeywords(final IFile suiteFile, final String keywordName) {
        final Collection<String> allNames = getAccessibleKeywords(suiteFile);
        return similaritiesAlgorithm.onlyWordsWithinDistance(allNames, keywordName, DEFAULT_MAXIMUM_DISTANCE);
    }

    private Collection<String> getAccessibleKeywords(final IFile suiteFile) {
        final List<String> names = new ArrayList<>();
        new KeywordDefinitionLocator(suiteFile).locateKeywordDefinition(new KeywordDetector() {
            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final RobotSuiteFile exposingFile) {
                names.add(kwSpec.getName());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                names.add(keyword.getName());
                return ContinueDecision.CONTINUE;
            }
        });
        return names;
    }

    public Collection<String> provideSimilarAccessibleVariables(final IFile suiteFile, final int offset,
            final String varName) {
        final Collection<String> allNames = getAccessibleVariables(suiteFile, offset);
        return similaritiesAlgorithm.onlyWordsWithinDistance(allNames, varName, DEFAULT_MAXIMUM_DISTANCE);
    }

    private Collection<String> getAccessibleVariables(final IFile suiteFile, final int offset) {
        final List<String> names = new ArrayList<>();
        new VariableDefinitionLocator(suiteFile).locateVariableDefinitionWithLocalScope(new VariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                names.add(variable.getName());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                names.add(variableName);
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variable) {
                names.add(variable.getText());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                names.add(name);
                return ContinueDecision.CONTINUE;
            }
        }, offset);
        return names;
    }
}
