/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport.NameTransformation;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;
import com.google.common.collect.ListMultimap;
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

            final AccessibleKeywordsEntities context = createEntities();
            final Optional<String> nameToUse = GherkinStyleSupport.firstNameTransformationResult(keywordName,
                    new NameTransformation<String>() {

                        @Override
                        public Optional<String> transform(final String gherkinNameVariant) {
                            return context.isKeywordAccessible(gherkinNameVariant) ? Optional.of(gherkinNameVariant)
                                    : Optional.<String> absent();
                        }
                    });

            if (!nameToUse.isPresent()) {
                return null;
            }
            final String name = !nameToUse.isPresent() || nameToUse.get().isEmpty() ? keywordName
                    : nameToUse.get().toString();

            final int lengthOfRemovedPrefix = keywordName.length() - name.length();
            final IRegion adjustedFromRegion = new Region(fromRegion.getOffset() + lengthOfRemovedPrefix,
                    fromRegion.getLength() - lengthOfRemovedPrefix);

            final ListMultimap<KeywordScope, KeywordEntity> keywords = context.getPossibleKeywords(name);

            final List<IHyperlink> definitionHyperlinks = new ArrayList<>();
            final List<IHyperlink> documentationHyperlinks = new ArrayList<>();

            for (final KeywordScope scope : KeywordScope.defaultOrder()) {
                for (final KeywordEntity keyword : keywords.get(scope)) {
                    final KeywordHyperlinkEntity keywordEntity = (KeywordHyperlinkEntity) keyword;
                    final DefinitionPosition keywordPosition = keywordEntity.destinationPosition;
                    switch (scope) {
                        case LOCAL:
                            String lbl = "[" + keywordEntity.getNameFromDefinition() + ", line: "
                                    + keywordPosition.getLine() + "] ["
                                    + keywordEntity.exposingResource.getFile().getFullPath() + "]";

                            definitionHyperlinks.add(new RegionsHyperlink(textViewer, keywordEntity.exposingResource,
                                    adjustedFromRegion, keywordPosition.toRegion(), lbl));
                            documentationHyperlinks.add(new UserKeywordDocumentationHyperlink(adjustedFromRegion,
                                    keywordEntity.exposingResource, keywordEntity.userKeyword, lbl));
                            break;
                        case RESOURCE:
                            lbl = "[" + keywordEntity.getNameFromDefinition() + ", line: " + keywordPosition.getLine()
                                    + "] [" + keywordEntity.exposingResource.getFile().getFullPath() + "]";

                            definitionHyperlinks.add(new SuiteFileHyperlink(adjustedFromRegion,
                                    keywordEntity.exposingResource, keywordPosition.toRegion(), lbl));
                            documentationHyperlinks.add(new UserKeywordDocumentationHyperlink(adjustedFromRegion,
                                    keywordEntity.exposingResource, keywordEntity.userKeyword, lbl));
                            break;
                        default:
                            definitionHyperlinks.add(new KeywordInLibrarySourceHyperlink(adjustedFromRegion,
                                    suiteFile.getFile().getProject(), keywordEntity.libSpec));
                            documentationHyperlinks.add(new KeywordDocumentationHyperlink(adjustedFromRegion,
                                    suiteFile.getFile().getProject(), keywordEntity.libSpec, keywordEntity.kwSpec));
                            break;
                    }
                }
            }

            if (!canShowMultipleHyperlinks) {
                return definitionHyperlinks.isEmpty() ? null : definitionHyperlinks.toArray(new IHyperlink[0]);
            }

            final List<IHyperlink> hyperlinks = newArrayList();
            if (definitionHyperlinks.size() > 0 && documentationHyperlinks.size() > 0) {
                hyperlinks.add(definitionHyperlinks.get(0));
                hyperlinks.add(documentationHyperlinks.get(0));
            }
            if (definitionHyperlinks.size() > 1 && documentationHyperlinks.size() > 0) {
                hyperlinks.add(new CompoundHyperlink(name, adjustedFromRegion,
                        newArrayList(filter(definitionHyperlinks, RedHyperlink.class)), "Show All Definitions"));
                hyperlinks.add(new CompoundHyperlink(name, adjustedFromRegion,
                        newArrayList(filter(documentationHyperlinks, RedHyperlink.class)), "Show All Documentations"));
            }
            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);

        } catch (final BadLocationException e) {
            return null;
        }
    }

    private AccessibleKeywordsEntities createEntities() {
        final AccessibleKeywordsCollector collector = new HyperlinksKeywordCollector(suiteFile.getFile());
        return new AccessibleKeywordsEntities(suiteFile.getFile().getFullPath(), collector);
    }



    private static final class HyperlinksKeywordCollector implements AccessibleKeywordsCollector {

        private final IFile file;

        public HyperlinksKeywordCollector(final IFile file) {
            this.file = file;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return collectAccessibleKeywordNames(file);
        }

        private Map<String, Collection<KeywordEntity>> collectAccessibleKeywordNames(final IFile file) {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
            new KeywordDefinitionLocator(file, RedPlugin.getModelManager().getModel())
                    .locateKeywordDefinition(new KeywordDetector() {

                        @Override
                        public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                                final KeywordSpecification kwSpec, final String libraryAlias,
                                final RobotSuiteFile exposingFile) {

                            final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                                    : KeywordScope.STD_LIBRARY;
                            final KeywordHyperlinkEntity keyword = KeywordHyperlinkEntity.from(scope, libraryAlias,
                                    exposingFile, libSpec, kwSpec);

                            addAccessibleKeyword(kwSpec.getName(), keyword);
                            return ContinueDecision.CONTINUE;
                        }

                        @Override
                        public ContinueDecision keywordDetected(final RobotSuiteFile suiteFile,
                                final RobotKeywordDefinition kwDefinition) {

                            final KeywordScope scope = suiteFile.getFile().equals(file) ? KeywordScope.LOCAL
                                    : KeywordScope.RESOURCE;

                            final DefinitionPosition position = kwDefinition.getDefinitionPosition();
                            final KeywordHyperlinkEntity keyword = KeywordHyperlinkEntity.from(scope, suiteFile,
                                    position, kwDefinition);

                            addAccessibleKeyword(kwDefinition.getName(), keyword);
                            return ContinueDecision.CONTINUE;
                        }

                        private void addAccessibleKeyword(final String keywordName,
                                final KeywordHyperlinkEntity keyword) {
                            final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);
                            if (accessibleKeywords.containsKey(unifiedName)) {
                                accessibleKeywords.get(unifiedName).add(keyword);
                            } else {
                                final LinkedHashSet<KeywordEntity> setOfKeywords = newLinkedHashSet();
                                setOfKeywords.add(keyword);
                                accessibleKeywords.put(unifiedName, setOfKeywords);
                            }
                        }
                    });
            return accessibleKeywords;
        }
    }

    private static class KeywordHyperlinkEntity extends KeywordEntity {

        private final DefinitionPosition destinationPosition;

        private final RobotSuiteFile exposingResource;

        private final RobotKeywordDefinition userKeyword;

        private final LibrarySpecification libSpec;

        private final KeywordSpecification kwSpec;

        static KeywordHyperlinkEntity from(final KeywordScope scope, final String alias,
                final RobotSuiteFile exposingResource, final LibrarySpecification libSpec,
                final KeywordSpecification kwSpec) {
            return new KeywordHyperlinkEntity(scope, libSpec.getName(), kwSpec.getName(), alias, kwSpec.isDeprecated(),
                    exposingResource, null, null, libSpec, kwSpec);
        }

        static KeywordHyperlinkEntity from(final KeywordScope scope, final RobotSuiteFile exposingResource,
                final DefinitionPosition position, final RobotKeywordDefinition userKeyword) {
            return new KeywordHyperlinkEntity(scope, Files.getNameWithoutExtension(exposingResource.getName()),
                    userKeyword.getName(), "", userKeyword.isDeprecated(), exposingResource, position,
                    userKeyword, null, null);
        }

        protected KeywordHyperlinkEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final String alias, final boolean isDeprecated, final RobotSuiteFile exposingResource,
                final DefinitionPosition position, final RobotKeywordDefinition userKeyword,
                final LibrarySpecification libSpec, final KeywordSpecification kwSpec) {
            super(scope, sourceName, keywordName, alias, isDeprecated, exposingResource.getFile().getFullPath());
            this.destinationPosition = position;
            this.exposingResource = exposingResource;
            this.userKeyword = userKeyword;
            this.libSpec = libSpec;
            this.kwSpec = kwSpec;
        }

        @Override
        public boolean isSameAs(final KeywordEntity other, final IPath useplaceFilepath) {
            return Objects.equals(destinationPosition, ((KeywordHyperlinkEntity) other).destinationPosition)
                    && super.isSameAs(other, useplaceFilepath);
        }

        @Override
        public boolean equals(final Object obj) {
            return super.equals(obj)
                    || Objects.equals(destinationPosition, ((KeywordHyperlinkEntity) obj).destinationPosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), destinationPosition);
        }

    }
}
