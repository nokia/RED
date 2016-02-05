/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * @author Michal Anglart
 *
 */
public class ImportLibraryFixer extends RedSuiteMarkerResolution {

    public static Collection<IMarkerResolution> createFixers(final IFile file, final String keywordName) {
        final RobotProject project = RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());

        final Set<String> libs = newLinkedHashSet();
        new KeywordDefinitionLocator(file, new RobotModel()).locateKeywordDefinitionInLibraries(project,
                createKeywordsDetector(keywordName, libs));

        return newArrayList(Iterables.transform(libs, new Function<String, IMarkerResolution>() {
            @Override
            public IMarkerResolution apply(final String libName) {
                return new ImportLibraryFixer(libName);
            }
        }));
    }

    private static KeywordDetector createKeywordsDetector(final String keywordName, final Set<String> libs) {
        return new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final RobotSuiteFile exposingFile) {
                if (QualifiedKeywordName.fromOccurrence(keywordName).matchesIgnoringCase(
                        QualifiedKeywordName.create(QualifiedKeywordName.unifyDefinition(kwSpec.getName()),
                                libSpec.getName()))) {
                    libs.add(libSpec.getName());
                }
                return ContinueDecision.CONTINUE;
            }


            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file,
                    final RobotKeywordDefinition keyword) {
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private final String libName;

    private ImportLibraryFixer(final String libName) {
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
                return Optional
                        .<ICompletionProposal> of(new CompletionProposal(lineToInsert, offset, 0, lineToInsert.length(),
                                ImagesManager.getImage(RedImages.getBookImage()), getLabel(), null, null));
            } catch (final BadLocationException e) {
                return Optional.absent();
            }

        } else {
            final String toInsert = "*** Settings ***" + lineToInsert + lineDelimiter + lineDelimiter;
            return Optional.<ICompletionProposal> of(new CompletionProposal(toInsert, 0, 0, toInsert.length(),
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
