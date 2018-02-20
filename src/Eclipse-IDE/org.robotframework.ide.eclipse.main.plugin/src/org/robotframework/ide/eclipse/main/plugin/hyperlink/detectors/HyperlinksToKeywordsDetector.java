/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.CompoundHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordInLibrarySourceHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.RedHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.UserKeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

abstract class HyperlinksToKeywordsDetector {

    private final RobotModel model;

    public HyperlinksToKeywordsDetector(final RobotModel model) {
        this.model = model;
    }

    protected final List<IHyperlink> detectHyperlinks(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String keywordName) {

        final List<IHyperlink> hyperlinks = new ArrayList<>();

        final AccessibleKeywordsEntities context = createEntities(suiteFile);
        final ListMultimap<String, KeywordEntity> keywordProposal = context.findPossibleKeywords(keywordName, false);
        final Optional<String> nameToUse = GherkinStyleSupport.firstNameTransformationResult(keywordName,
                gherkinNameVariant -> context.isKeywordAccessible(keywordProposal, gherkinNameVariant)
                        ? Optional.of(gherkinNameVariant)
                        : Optional.empty());

        if (!nameToUse.isPresent()) {
            return hyperlinks;
        }
        final String name = nameToUse.get().isEmpty() ? keywordName : nameToUse.get().toString();

        final int lengthOfRemovedPrefix = keywordName.length() - name.length();
        final IRegion adjustedFromRegion = new Region(fromRegion.getOffset() + lengthOfRemovedPrefix,
                fromRegion.getLength() - lengthOfRemovedPrefix);

        final ListMultimap<KeywordScope, KeywordEntity> keywords = context.getPossibleKeywords(keywordProposal, name);

        final List<IHyperlink> definitionHyperlinks = new ArrayList<>();
        final List<IHyperlink> documentationHyperlinks = new ArrayList<>();

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            for (final KeywordEntity keyword : keywords.get(scope)) {
                final KeywordHyperlinkEntity keywordEntity = (KeywordHyperlinkEntity) keyword;
                switch (scope) {
                    case LOCAL:
                        String lbl = "[" + keywordEntity.getNameFromDefinition() + ", line: "
                                + keywordEntity.destinationPosition.getLine() + "] ["
                                + keywordEntity.exposingResource.getFile().getFullPath() + "]";

                        definitionHyperlinks.add(createLocalKeywordHyperlink(keywordEntity, adjustedFromRegion, lbl));
                        documentationHyperlinks.add(new UserKeywordDocumentationHyperlink(adjustedFromRegion,
                                keywordEntity.exposingResource, keywordEntity.userKeyword, lbl));
                        break;
                    case RESOURCE:
                        lbl = "[" + keywordEntity.getNameFromDefinition() + ", line: "
                                + keywordEntity.destinationPosition.getLine() + "] ["
                                + keywordEntity.exposingResource.getFile().getFullPath() + "]";

                        definitionHyperlinks
                                .add(createResourceKeywordHyperlink(keywordEntity, adjustedFromRegion, lbl));
                        documentationHyperlinks.add(new UserKeywordDocumentationHyperlink(adjustedFromRegion,
                                keywordEntity.exposingResource, keywordEntity.userKeyword, lbl));
                        break;
                    default:
                        definitionHyperlinks.add(new KeywordInLibrarySourceHyperlink(adjustedFromRegion,
                                suiteFile.getFile().getProject(), keywordEntity.libSpec, keywordEntity.kwSpec));
                        documentationHyperlinks.add(new KeywordDocumentationHyperlink(adjustedFromRegion,
                                suiteFile.getFile().getProject(), keywordEntity.libSpec, keywordEntity.kwSpec));
                        break;
                }
            }
        }

        if (definitionHyperlinks.size() > 0) {
            hyperlinks.add(definitionHyperlinks.get(0));
        }
        if (documentationHyperlinks.size() > 0) {
            hyperlinks.add(documentationHyperlinks.get(0));
        }
        if (definitionHyperlinks.size() > 1) {
            hyperlinks.add(new CompoundHyperlink(name, adjustedFromRegion,
                    newArrayList(filter(definitionHyperlinks, RedHyperlink.class)), "Show All Definitions"));
        }
        if (documentationHyperlinks.size() > 1) {
            hyperlinks.add(new CompoundHyperlink(name, adjustedFromRegion,
                    newArrayList(filter(documentationHyperlinks, RedHyperlink.class)), "Show All Documentations"));
        }
        return hyperlinks;
    }

    protected abstract IHyperlink createLocalKeywordHyperlink(final KeywordHyperlinkEntity keywordEntity, IRegion from,
            final String additionalInfo);

    protected abstract IHyperlink createResourceKeywordHyperlink(final KeywordHyperlinkEntity keywordEntity,
            IRegion from, final String additionalInfo);

    private AccessibleKeywordsEntities createEntities(final RobotSuiteFile suiteFile) {
        final AccessibleKeywordsCollector collector = new HyperlinksKeywordCollector(model, suiteFile.getFile());
        return new AccessibleKeywordsEntities(suiteFile.getFile().getFullPath(), collector);
    }

    private static final class HyperlinksKeywordCollector implements AccessibleKeywordsCollector {

        private final RobotModel model;

        private final IFile file;

        public HyperlinksKeywordCollector(final RobotModel model, final IFile file) {
            this.model = model;
            this.file = file;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return collectAccessibleKeywords(file);
        }

        private Map<String, Collection<KeywordEntity>> collectAccessibleKeywords(final IFile file) {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
            new KeywordDefinitionLocator(file, model).locateKeywordDefinition(new KeywordDetector() {

                @Override
                public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                        final RobotSuiteFile exposingFile) {

                    final KeywordScope scope = libSpec.getDescriptor().getKeywordsScope();
                    for (final Optional<String> libraryAlias : libraryAliases) {
                        final KeywordHyperlinkEntity keyword = KeywordHyperlinkEntity.from(scope, libraryAlias,
                                exposingFile, libSpec, kwSpec);

                        addAccessibleKeyword(kwSpec.getName(), keyword);
                    }
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision keywordDetected(final RobotSuiteFile suiteFile,
                        final RobotKeywordDefinition kwDefinition) {

                    final KeywordScope scope = suiteFile.getFile().equals(file) ? KeywordScope.LOCAL
                            : KeywordScope.RESOURCE;

                    final DefinitionPosition position = kwDefinition.getDefinitionPosition();
                    final KeywordHyperlinkEntity keyword = KeywordHyperlinkEntity.from(scope, suiteFile, position,
                            kwDefinition);

                    addAccessibleKeyword(kwDefinition.getName(), keyword);
                    return ContinueDecision.CONTINUE;
                }

                private void addAccessibleKeyword(final String keywordName, final KeywordHyperlinkEntity keyword) {
                    final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);
                    if (!accessibleKeywords.containsKey(unifiedName)) {
                        accessibleKeywords.put(unifiedName, new LinkedHashSet<>());
                    }
                    accessibleKeywords.get(unifiedName).add(keyword);
                }
            });
            return accessibleKeywords;
        }
    }

    static class KeywordHyperlinkEntity extends KeywordEntity {

        final DefinitionPosition destinationPosition;

        final RobotSuiteFile exposingResource;

        final RobotKeywordDefinition userKeyword;

        private final LibrarySpecification libSpec;

        private final KeywordSpecification kwSpec;

        static KeywordHyperlinkEntity from(final KeywordScope scope, final Optional<String> alias,
                final RobotSuiteFile exposingResource, final LibrarySpecification libSpec,
                final KeywordSpecification kwSpec) {
            return new KeywordHyperlinkEntity(scope, libSpec.getName(), kwSpec.getName(), alias, kwSpec.isDeprecated(),
                    exposingResource, null, null, libSpec, kwSpec);
        }

        static KeywordHyperlinkEntity from(final KeywordScope scope, final RobotSuiteFile exposingResource,
                final DefinitionPosition position, final RobotKeywordDefinition userKeyword) {
            return new KeywordHyperlinkEntity(scope, Files.getNameWithoutExtension(exposingResource.getName()),
                    userKeyword.getName(), Optional.empty(), userKeyword.isDeprecated(), exposingResource, position,
                    userKeyword, null, null);
        }

        protected KeywordHyperlinkEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final Optional<String> alias, final boolean isDeprecated, final RobotSuiteFile exposingResource,
                final DefinitionPosition position, final RobotKeywordDefinition userKeyword,
                final LibrarySpecification libSpec, final KeywordSpecification kwSpec) {
            super(scope, sourceName, keywordName, alias, isDeprecated, null, exposingResource.getFile().getFullPath());
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
