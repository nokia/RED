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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordNameSplitter;
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
            new KeywordDefinitionLocator(suiteFile)
                    .locateKeywordDefinition(createDetector(textViewer, keywordName, fromRegion, hyperlinks));
            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);

        } catch (final BadLocationException e) {
            return null;
        }
    }

    private KeywordDetector createDetector(final ITextViewer textViewer, final String name, final IRegion fromRegion,
            final List<IHyperlink> hyperlinks) {
        return new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final boolean isFromNestedLibrary) {
                
                final KeywordNameSplitter typedKeywordNameSplitter = KeywordNameSplitter.splitKeywordName(name);
                if (kwSpec.getName().equalsIgnoreCase(typedKeywordNameSplitter.getKeywordName())
                        && hasEqualSources(libSpec, libraryAlias, typedKeywordNameSplitter.getKeywordSource())) {
                    hyperlinks.add(new LibraryKeywordHyperlink(fromRegion, kwSpec));
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file,
                    final RobotKeywordDefinition keyword) {
                
                final KeywordNameSplitter typedKeywordNameSplitter = KeywordNameSplitter.splitKeywordName(name);
                if (keyword.getName().equalsIgnoreCase(typedKeywordNameSplitter.getKeywordName())
                        && hasEqualSources(file, typedKeywordNameSplitter.getKeywordSource())) {
                    final Position position = keyword.getDefinitionPosition();
                    final IRegion destination = new Region(position.getOffset(), position.getLength());
                    if (file == suiteFile) {
                        hyperlinks.add(new RegionsHyperlink(textViewer, fromRegion, destination));
                    } else {
                        hyperlinks.add(new DifferentFileHyperlink(fromRegion, file.getFile(), destination));
                    }
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }
            
            private boolean hasEqualSources(final LibrarySpecification libSpec, final String libraryAlias,
                    final String typedKeywordSourceName) {
                if (!typedKeywordSourceName.isEmpty()) {
                    return !libraryAlias.isEmpty() ? libraryAlias.equalsIgnoreCase(typedKeywordSourceName)
                            : libSpec.getName().equalsIgnoreCase(typedKeywordSourceName);
                }
                return true;
            }
            
            private boolean hasEqualSources(final RobotSuiteFile file, final String typedKeywordSourceName) {
                if (!typedKeywordSourceName.isEmpty()) {
                    return file.isResourceFile() ? Files.getNameWithoutExtension(file.getName()).equalsIgnoreCase(
                            typedKeywordSourceName) : false;
                }
                return true;
            }

        };
    }

}
