/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ImportLibraryFixer;

class KeywordsImportsFixes {

    static Collection<IMarkerResolution> changeByImportingLibraryWithMissingKeyword(final IFile file,
            final String keywordName) {
        final Collection<String> libNames = collectNotImportedLibraryNames(file, keywordName);
        return libNames.stream().map(libName -> new ImportLibraryFixer(libName)).collect(toCollection(ArrayList::new));
    }

    private static Collection<String> collectNotImportedLibraryNames(final IFile file, final String keywordName) {
        final RobotModel model = RedPlugin.getModelManager().getModel();
        final Set<String> libs = new LinkedHashSet<>();
        new KeywordDefinitionLocator(file, model)
                .locateKeywordDefinitionInLibraries(model.createRobotProject(file.getProject()), new KeywordDetector() {

                    @Override
                    public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                            final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                        if (QualifiedKeywordName.fromOccurrence(keywordName).matchesIgnoringCase(QualifiedKeywordName
                                .create(QualifiedKeywordName.unifyDefinition(kwSpec.getName()), libSpec.getName()))) {
                            libs.add(libSpec.getName());
                        }
                        return ContinueDecision.CONTINUE;
                    }

                    @Override
                    public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                            final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAlias,
                            final RobotSuiteFile exposingFile) {
                        return ContinueDecision.CONTINUE;
                    }

                    @Override
                    public ContinueDecision keywordDetected(final RobotSuiteFile file,
                            final RobotKeywordDefinition keyword) {
                        return ContinueDecision.CONTINUE;
                    }
                });
        return libs;
    }
}
