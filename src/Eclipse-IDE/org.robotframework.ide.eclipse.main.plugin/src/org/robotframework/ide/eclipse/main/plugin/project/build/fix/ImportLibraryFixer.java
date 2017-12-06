/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class ImportLibraryFixer extends RedSuiteMarkerResolution {

    public static Collection<IMarkerResolution> createFixers(final IFile file, final String keywordName) {
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

    private final String libName;

    public ImportLibraryFixer(final String libName) {
        this.libName = libName;
    }

    @Override
    public String getLabel() {
        return "Import '" + libName + "' library";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {

        final Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);
        final String lineDelimiter = getLineDelimiter(document);
        final String lineToInsert = lineDelimiter + "Library    " + libName;
        if (section.isPresent()) {
            final int line = section.get().getHeaderLine();
            try {
                final IRegion lineInformation = document.getLineInformation(line - 1);
                final int offset = lineInformation.getOffset() + lineInformation.getLength();
                return Optional.of(new CompletionProposal(lineToInsert, offset, 0, lineToInsert.length(),
                        ImagesManager.getImage(RedImages.getBookImage()), getLabel(), null, null));
            } catch (final BadLocationException e) {
                return Optional.empty();
            }

        } else {
            final String toInsert = "*** Settings ***" + lineToInsert + lineDelimiter + lineDelimiter;
            return Optional.of(new CompletionProposal(toInsert, 0, 0, toInsert.length(),
                    ImagesManager.getImage(RedImages.getBookImage()), getLabel(), null, null));
        }
    }

    private static String getLineDelimiter(final IDocument document) {
        try {
            final String delimiter = document.getLineDelimiter(0);
            return delimiter == null ? "\n" : delimiter;
        } catch (final BadLocationException e) {
            return "\n";
        }
    }
}
