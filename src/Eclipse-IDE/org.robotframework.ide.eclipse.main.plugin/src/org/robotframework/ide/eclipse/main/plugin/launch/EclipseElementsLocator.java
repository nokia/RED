/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.execution.debug.ElementsLocator;
import org.rf.ide.core.execution.debug.contexts.ErrorMessages;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.debug.contexts.KeywordFromLibraryContext;
import org.rf.ide.core.execution.debug.contexts.KeywordOfUserContext;
import org.rf.ide.core.execution.debug.contexts.KeywordUnknownContext;
import org.rf.ide.core.execution.debug.contexts.SuiteContext;
import org.rf.ide.core.execution.debug.contexts.TestCaseContext;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotContainer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.model.locators.TestCasesDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

public class EclipseElementsLocator implements ElementsLocator {

    private final RobotModel model;
    private final RedWorkspace workspace;

    private final IProject project;

    private final LoadingCache<TypedUri, Optional<URI>> pathsTranslationsCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build(new CacheLoader<TypedUri, Optional<URI>>() {

                @Override
                public Optional<URI> load(final TypedUri remoteUri) {
                    return Optional.ofNullable(translate(remoteUri.uri, remoteUri.isDirectory));
                }
            });

    private final LoadingCache<IFile, AccessibleKeywordsEntities> entitiesCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<IFile, AccessibleKeywordsEntities>() {
                @Override
                public AccessibleKeywordsEntities load(final IFile file) {
                    return new AccessibleKeywordsEntities(file.getFullPath(),
                            new DebuggerAccessibleKeywordsCollector(model, file));
                }
            });
    private final LoadingCache<KeywordWithFile, ListMultimap<KeywordScope, KeywordEntity>> keywordsCache = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<KeywordWithFile, ListMultimap<KeywordScope, KeywordEntity>>() {

                @Override
                public ListMultimap<KeywordScope, KeywordEntity> load(final KeywordWithFile kwWithFile) {
                    final AccessibleKeywordsEntities keywordEntities = entitiesCache.getUnchecked(kwWithFile.file);
                    return keywordEntities.getPossibleKeywords(kwWithFile.keywordName, false);
                }
            });

    public EclipseElementsLocator(final IProject project) {
        this.model = RedPlugin.getModelManager().getModel();
        this.workspace = new RedWorkspace(project.getWorkspace().getRoot());
        this.project = project;
    }

    @Override
    public URI translate(final URI path, final boolean isDirectory) {
        final Optional<? extends IResource> resource = isDirectory ? workspace.containerForUri(path)
                : workspace.fileForUri(path);
        if (resource.isPresent()) {
            return resource.get().getLocationURI();
        }

        try {
            final List<URI> candidates = new ArrayList<>();

            final String fileName = new File(path).getName();
            project.accept(res -> {
                final boolean localResourceIsDir = res.getType() != IResource.FILE;
                // both remote and local uri have to be directories or files at the same time
                if (localResourceIsDir == isDirectory) {
                    final IPath location = res.getLocation();
                    if (location.segment(location.segmentCount() - 1).equalsIgnoreCase(fileName)) {
                        candidates.add(res.getLocationURI());
                    }
                }
                return true;
            });

            if (candidates.isEmpty()) {
                return null;
            } else if (candidates.size() == 1) {
                return candidates.get(0);
            } else {
                return choseCandidate(path, candidates);
            }
        } catch (final CoreException e) {
            return null;
        }
    }

    private URI choseCandidate(final URI path, final List<URI> candidates) {
        final String[] pathElements = path.getPath().split("/");

        URI currentBestCandidate = candidates.get(0);
        int currentBestScore = scoreCandidate(pathElements, candidates.get(0));

        for (int i = 1; i < candidates.size(); i++) {
            final URI candidate = candidates.get(i);
            final int score = scoreCandidate(pathElements, candidate);
            if (score > currentBestScore) {
                currentBestCandidate = candidates.get(i);
                currentBestScore = score;
            }
        }
        return currentBestCandidate;
    }

    private int scoreCandidate(final String[] originalPath, final URI candidate) {
        final String[] candidatePathElements = candidate.getPath().split("/");

        int score = 0;
        for (int i = originalPath.length - 1, j = candidatePathElements.length - 1; i >= 0 && j >= 0; i--, j--) {
            if (originalPath[i].equalsIgnoreCase(candidatePathElements[j])) {
                score++;
            } else {
                break;
            }
        }
        return score;
    }

    @Override
    public SuiteContext findContextForSuite(final String suiteName, final URI path, final boolean isDirectory,
            final URI currentLocalSuitePath) {
        if (path == null) {
            // only for top-level merged suites (when Robot runs on multiple data sources)
            return new SuiteContext(suiteName);
        }

        final URI localUri = currentLocalSuitePath == null
                ? pathsTranslationsCache.getUnchecked(new TypedUri(path, isDirectory)).orElse(null)
                : resolve(currentLocalSuitePath, path);

        if (localUri == null) {
            final String errorMsg = String.format(
                    ErrorMessages.errorOfSuiteNotFoundBecauseOfUnknownLocation(isDirectory),
                    suiteName, new File(path.getPath()).getAbsolutePath());
            return new SuiteContext(suiteName, isDirectory, errorMsg);
        }


        final Function<URI, Optional<RobotFile>> associatedModelProvider;
        final boolean isFoundInWorkspace;
        if (isDirectory) {
            associatedModelProvider = uri -> workspace.containerForUri(uri)
                    .map(container -> model.createRobotContainer(container))
                    .flatMap(RobotContainer::getInitFileModel)
                    .map(RobotSuiteFile::getLinkedElement);
            isFoundInWorkspace = workspace.hasContainerForUri(localUri);

        } else {
            associatedModelProvider = uri -> workspace.fileForUri(uri)
                    .map(file -> model.createSuiteFile(file))
                    .map(RobotSuiteFile::getLinkedElement);
            isFoundInWorkspace = workspace.hasFileForUri(localUri);

        }
        final String missingLocationError = String.format(
                ErrorMessages.errorOfSuiteNotFoundBecauseOfMissingLocation(isDirectory), suiteName,
                new File(path.getPath()).getAbsolutePath(), new File(localUri.getPath()).getAbsolutePath());

        return isFoundInWorkspace
                ? new SuiteContext(suiteName, localUri, isDirectory, associatedModelProvider)
                : new SuiteContext(suiteName, localUri, isDirectory, missingLocationError);
    }

    private URI resolve(final URI local, final URI remote) {
        final String[] splittedRemote = remote.getPath().split("/");
        final String lastSegment = splittedRemote[splittedRemote.length - 1];
        try {
            return new URI("file", null, null, -1, local.getPath() + "/" + lastSegment, null, null);
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    @Override
    public TestCaseContext findContextForTestCase(final String testCaseName, final URI currentSuitePath,
            final Optional<String> template) {
        final Optional<IFile> suiteFile = Optional.ofNullable(currentSuitePath)
                .map(uri -> (IFile) workspace.forUri(uri));

        if (!suiteFile.isPresent()) {
            final String errorMsg = String.format(ErrorMessages.testNotFound_missingSuite, testCaseName);
            return new TestCaseContext(errorMsg);
        }

        final List<RobotCase> matchingCases = new ArrayList<>();
        new TestCasesDefinitionLocator(suiteFile.get()).locateTestCaseDefinition((s, testCase) -> {
            if (testCase.getName().equalsIgnoreCase(testCaseName)) {
                matchingCases.add(testCase);
            }
            return ContinueDecision.CONTINUE;
        });

        final URI locationUri = suiteFile.get().getLocationURI();
        final List<RobotFile> models = collectSuiteAndInitFileModelsAbove(suiteFile.get());
        if (matchingCases.isEmpty()) {
            final String errorMsg = String.format(ErrorMessages.testNotFound_noMatch, testCaseName);
            final RobotSuiteFile suiteModel = model.createSuiteFile(suiteFile.get());
            final int line = findLineForCaseTableHeader(suiteModel);
            return new TestCaseContext(models, locationUri, line, errorMsg);

        } else if (matchingCases.size() == 1) {
            final RobotCase foundCase = matchingCases.get(0);
            final TestCase testCase = foundCase.getLinkedElement();

            final Optional<String> templateInUse = foundCase.getTemplateInUse();

            if (template.equals(templateInUse)) {
                return new TestCaseContext(testCase, locationUri, models, template.orElse(null));
            } else {
                final String errorMsg = String.format(ErrorMessages.testNotFound_templatesMismatch, testCaseName,
                        template.map(t -> "'" + t + "'").orElse("no"),
                        templateInUse.map(t -> "'" + t + "'").orElse("no"));
                return new TestCaseContext(testCase, locationUri, models, template.orElse(null), errorMsg);
            }

        } else {
            final RobotCase foundCase = matchingCases.get(0);
            final TestCase testCase = foundCase.getLinkedElement();

            final String errorMsg = String.format(ErrorMessages.testNotFound_tooManyMatches, testCaseName);
            return new TestCaseContext(testCase, locationUri, models, template.orElse(null), errorMsg);
        }
    }

    private int findLineForCaseTableHeader(final RobotSuiteFile suiteModel) {
        return suiteModel.findSection(RobotCasesSection.class).map(RobotCasesSection::getHeaderLine).orElse(1);
    }

    @Override
    public KeywordContext findContextForKeyword(final String libOrResourceName, final String keywordName,
            final URI currentSuitePath, final Set<URI> loadedResources) {
        if (currentSuitePath == null) {
            return new KeywordUnknownContext(String.format(ErrorMessages.keywordNotFound,
                    QualifiedKeywordName.asCall(keywordName, libOrResourceName)));
        }

        KeywordContext context = findKeywordContext(libOrResourceName, keywordName, currentSuitePath);
        if (context != null) {
            return context;
        }
        // no keyword found, but maybe it could be found in dynamically loaded resources set
        for (final URI dynResourceRemoteUri : loadedResources) {
            final URI dynamicResourceLocalUri = pathsTranslationsCache
                    .getUnchecked(new TypedUri(dynResourceRemoteUri, false))
                    .orElse(null);
            if (dynamicResourceLocalUri != null) {
                context = findKeywordContext(libOrResourceName, keywordName, dynamicResourceLocalUri);
                if (context != null) {
                    return context;
                }
            }
        }

        final String errorMsg = String.format(ErrorMessages.keywordNotFound_noMatch,
                QualifiedKeywordName.asCall(keywordName, libOrResourceName), new File(currentSuitePath));
        return new KeywordUnknownContext(errorMsg);
    }

    private KeywordContext findKeywordContext(final String libOrResourceName, final String keywordName,
            final URI currentSuitePath) {
        final IFile suiteFile = (IFile) workspace.forUri(currentSuitePath);
        final ListMultimap<KeywordScope, KeywordEntity> possibleKeywords = getPossibleKeywords(
                QualifiedKeywordName.asCall(keywordName, libOrResourceName), suiteFile);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            final Collection<KeywordEntity> matchingKeywords = possibleKeywords.get(scope);
            if (matchingKeywords.size() == 1) {
                final DebuggerKeywordEntity entity = (DebuggerKeywordEntity) matchingKeywords.iterator().next();

                if (entity.isLibrary()) {
                    return new KeywordFromLibraryContext();
                } else {
                    final UserKeyword keyword = entity.userKeyword.getLinkedElement();
                    final List<RobotFile> models = collectSuiteAndInitFileModelsAbove(suiteFile);
                    final URI locationUri = entity.userKeyword.getSuiteFile().getFile().getLocationURI();
                    return new KeywordOfUserContext(keyword, locationUri, models);
                }

            } else if (matchingKeywords.size() > 1) {
                final String errorMsg = String.format(ErrorMessages.keywordNotFound_tooManyMatches,
                        QualifiedKeywordName.asCall(keywordName, libOrResourceName));
                return new KeywordUnknownContext(errorMsg);
            }
        }
        return null;
    }

    private ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords(final String keywordName,
            final IFile suiteFile) {
        return keywordsCache.getUnchecked(new KeywordWithFile(suiteFile, keywordName));
    }

    private List<RobotFile> collectSuiteAndInitFileModelsAbove(final IFile startingSuite) {
        // collects all models of: starting suites and all the init files above it
        // this is needed for looking up Test Setup/Test Teardown settings
        final List<RobotFile> allModels = new ArrayList<>();

        RobotElement current = model.createSuiteFile(startingSuite);
        while (!(current instanceof RobotModel)) {
            final RobotSuiteFile fileModel = current instanceof RobotSuiteFile
                    ? (RobotSuiteFile) current
                    : ((RobotContainer) current).getInitFileModel().orElse(null);

            if (fileModel != null) {
                allModels.add(fileModel.getLinkedElement());
            }
            current = current.getParent();
        }
        return allModels;
    }

    private static class DebuggerAccessibleKeywordsCollector implements AccessibleKeywordsCollector {

        private final RobotModel model;

        private final IFile file;

        public DebuggerAccessibleKeywordsCollector(final RobotModel model, final IFile file) {
            this.model = model;
            this.file = file;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
            new KeywordDefinitionLocator(file, model).locateKeywordDefinition(new KeywordDetector() {

                @Override
                public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final Set<String> libraryAliases,
                        final RobotSuiteFile exposingFile) {

                    final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                            : KeywordScope.STD_LIBRARY;
                    for (final String libraryAlias : libraryAliases) {
                        addAccessibleKeyword(kwSpec.getName(),
                                DebuggerKeywordEntity.from(scope, libraryAlias, exposingFile, libSpec, kwSpec));
                    }
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision keywordDetected(final RobotSuiteFile suiteFile,
                        final RobotKeywordDefinition kwDefinition) {

                    final KeywordScope scope = suiteFile.getFile().equals(file) ? KeywordScope.LOCAL
                            : KeywordScope.RESOURCE;

                    addAccessibleKeyword(kwDefinition.getName(), DebuggerKeywordEntity.from(scope, suiteFile, kwDefinition));
                    return ContinueDecision.CONTINUE;
                }

                private void addAccessibleKeyword(final String keywordName, final DebuggerKeywordEntity keyword) {
                    final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);

                    if (!accessibleKeywords.containsKey(unifiedName)) {
                        accessibleKeywords.put(unifiedName, newLinkedHashSet());
                    }
                    accessibleKeywords.get(unifiedName).add(keyword);
                }
            });
            return accessibleKeywords;
        }

    }

    private static class DebuggerKeywordEntity extends KeywordEntity {

        private final RobotKeywordDefinition userKeyword;

        static DebuggerKeywordEntity from(final KeywordScope scope, final String alias,
                final RobotSuiteFile exposingResource, final LibrarySpecification libSpec,
                final KeywordSpecification kwSpec) {
            return new DebuggerKeywordEntity(scope, libSpec.getName(), kwSpec.getName(), alias, kwSpec.isDeprecated(),
                    exposingResource.getFile().getFullPath(), null);
        }

        public boolean isLibrary() {
            return userKeyword == null;
        }

        static DebuggerKeywordEntity from(final KeywordScope scope, final RobotSuiteFile exposingResource,
                final RobotKeywordDefinition userKeyword) {
            return new DebuggerKeywordEntity(scope, Files.getNameWithoutExtension(exposingResource.getName()),
                    userKeyword.getName(), "", userKeyword.isDeprecated(), exposingResource.getFile().getFullPath(),
                    userKeyword);
        }

        protected DebuggerKeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final String alias, final boolean isDeprecated, final IPath exposingFilePath,
                final RobotKeywordDefinition userKeyword) {
            super(scope, sourceName, keywordName, alias, isDeprecated, null, exposingFilePath);
            this.userKeyword = userKeyword;
        }
    }

    private static final class TypedUri {

        private final boolean isDirectory;

        private final URI uri;

        public TypedUri(final URI uri, final boolean isDirectory) {
            this.uri = uri;
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == TypedUri.class) {
                final TypedUri that = (TypedUri) obj;
                return this.isDirectory == that.isDirectory && Objects.equals(this.uri, that.uri);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, isDirectory);
        }
    }

    private static final class KeywordWithFile {

        private final IFile file;

        private final String keywordName;

        public KeywordWithFile(final IFile file, final String keywordName) {
            this.file = file;
            this.keywordName = keywordName;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == KeywordWithFile.class) {
                final KeywordWithFile that = (KeywordWithFile) obj;
                return Objects.equals(this.keywordName, that.keywordName) && Objects.equals(this.file, that.file);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, keywordName);
        }
    }
}
