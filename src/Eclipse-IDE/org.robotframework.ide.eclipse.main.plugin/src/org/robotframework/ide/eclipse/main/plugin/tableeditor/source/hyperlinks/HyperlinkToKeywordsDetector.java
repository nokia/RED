/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport.NameOperation;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class HyperlinkToKeywordsDetector implements IHyperlinkDetector {

    private final RobotSuiteFile suiteFile;

    public HyperlinkToKeywordsDetector(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
            final boolean canShowMultipleHyperlinks) {

        try {
            final Optional<IRegion> hyperlinkRegion = DocumentUtilities.findCellRegion(textViewer.getDocument(),
                    region.getOffset());
            if (!hyperlinkRegion.isPresent()) {
                return null;
            }
            final IRegion fromRegion = hyperlinkRegion.get();
            final String keywordName = textViewer.getDocument().get(fromRegion.getOffset(), fromRegion.getLength());

            final List<IHyperlink> hyperlinks = newArrayList();

            GherkinStyleSupport.forEachPossibleGherkinName(keywordName, new NameOperation() {
                @Override
                public void perform(final String gherkinNameVariant) {
                    if (hyperlinks.isEmpty()) {
                        final IRegion fromRegionWithoutGherkin = new Region(
                                fromRegion.getOffset() + keywordName.length() - gherkinNameVariant.length(),
                                gherkinNameVariant.length());
                        new KeywordDefinitionLocator(suiteFile.getFile())
                                .locateKeywordDefinition(createDetector(textViewer, gherkinNameVariant,
                                        fromRegionWithoutGherkin, hyperlinks, canShowMultipleHyperlinks));
                    }
                }
            });

            if (!canShowMultipleHyperlinks && hyperlinks.size() > 1) {
                throw new IllegalStateException(
                        "Cannot provide more than one hyperlink, but there were " + hyperlinks.size() + " found");
            }
            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);

        } catch (final BadLocationException e) {
            return null;
        }
    }

    private KeywordDetector createDetector(final ITextViewer textViewer, final String name, final IRegion fromRegion,
            final List<IHyperlink> hyperlinks, final boolean canShowMultipleHyperlinks) {
        return new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final boolean isFromNestedLibrary) {

                final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(name);
                if (hasEqualSources(libSpec, libraryAlias, qualifiedName.getKeywordSource())
                        && EmbeddedKeywordNamesSupport.matches(QualifiedKeywordName.unifyDefinition(kwSpec.getName()),
                                qualifiedName)) {
                    hyperlinks.add(new LibrarySourceHyperlink(fromRegion, suiteFile.getFile().getProject(), libSpec));
                    if (canShowMultipleHyperlinks) {
                        hyperlinks.add(new LibraryKeywordHyperlink(fromRegion, kwSpec));
                    }
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }

            private boolean hasEqualSources(final LibrarySpecification libSpec, final String libraryAlias,
                    final String sourceName) {
                if (!sourceName.isEmpty()) {
                    return !libraryAlias.isEmpty() ? libraryAlias.equalsIgnoreCase(sourceName)
                            : libSpec.getName().equalsIgnoreCase(sourceName);
                }
                return true;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition kwDefinition) {

                final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(name);
                if (hasEqualSources(file, qualifiedName.getKeywordSource())
                        && EmbeddedKeywordNamesSupport.matches(
                                QualifiedKeywordName.unifyDefinition(kwDefinition.getName()), qualifiedName)) {

                    final KeywordSpecification kwSpec = kwDefinition.createSpecification();
                    final Position position = kwDefinition.getDefinitionPosition();
                    final IRegion destination = new Region(position.getOffset(), position.getLength());

                    final IHyperlink definitionHyperlink = file == suiteFile
                            ? new RegionsHyperlink(textViewer, fromRegion, destination)
                            : new SuiteFileHyperlink(fromRegion, file, destination);
                    hyperlinks.add(definitionHyperlink);
                    if (canShowMultipleHyperlinks) {
                        hyperlinks.add(new LibraryKeywordHyperlink(fromRegion, kwSpec));
                    }
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }

            private boolean hasEqualSources(final RobotSuiteFile file, final String sourceName) {
                if (!sourceName.isEmpty()) {
                    return file.isResourceFile()
                            ? Files.getNameWithoutExtension(file.getName()).equalsIgnoreCase(sourceName) : false;
                }
                return true;
            }
        };
    }

}
