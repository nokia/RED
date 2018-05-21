/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.IRegionCacheable;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig.PathResolvingException;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.RemoteArgumentsResolver;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class RobotSetting extends RobotKeywordCall {

    private static final long serialVersionUID = 1L;

    private SettingsGroup group;

    public RobotSetting(final RobotSettingsSection section, final AModelElement<?> linkedElement) {
        this(section, SettingsGroup.NO_GROUP, linkedElement);
    }

    public RobotSetting(final RobotSettingsSection section, final SettingsGroup group,
            final AModelElement<?> linkedElement) {
        super(section, linkedElement);
        this.group = group;
    }

    @Override
    public RobotSettingsSection getParent() {
        return (RobotSettingsSection) super.getParent();
    }

    public SettingsGroup getGroup() {
        return group;
    }

    public void setGroup(final SettingsGroup group) {
        this.group = group;
    }

    public String getNameInGroup() {
        final List<String> arguments = getArguments();
        return arguments.isEmpty() ? "" : arguments.get(0);
    }

    @Override
    public List<String> getArguments() {
        if (arguments == null) {
            arguments = getLinkedElement().getElementTokens().stream().filter(token -> {
                final List<IRobotTokenType> types = token.getTypes();
                final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                return type != RobotTokenType.START_HASH_COMMENT && type != RobotTokenType.COMMENT_CONTINUE
                        && type != RobotTokenType.SETTING_METADATA_DECLARATION
                        && type != RobotTokenType.SETTING_VARIABLES_DECLARATION
                        && type != RobotTokenType.SETTING_RESOURCE_DECLARATION
                        && type != RobotTokenType.SETTING_LIBRARY_DECLARATION
                        && type != RobotTokenType.SETTING_SUITE_SETUP_DECLARATION
                        && type != RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION
                        && type != RobotTokenType.SETTING_TEST_SETUP_DECLARATION
                        && type != RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION
                        && type != RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION
                        && type != RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION
                        && type != RobotTokenType.SETTING_FORCE_TAGS_DECLARATION
                        && type != RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION
                        && type != RobotTokenType.SETTING_LIBRARY_ALIAS
                        && type != RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE;
            }).map(RobotToken::getText).collect(toList());
        }
        return arguments;
    }

    public boolean isImportSetting() {
        return SettingsGroup.getImportsGroupsSet().contains(getGroup());
    }

    public boolean isKeywordBased() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.SUITE_TEARDOWN || modelType == ModelType.SUITE_TEST_TEARDOWN
                || modelType == ModelType.SUITE_SETUP || modelType == ModelType.SUITE_TEST_SETUP
                || modelType == ModelType.SUITE_TEST_TEMPLATE;
    }

    public boolean isDocumentation() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.SUITE_DOCUMENTATION;
    }

    public boolean isResourceImport() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.RESOURCE_IMPORT_SETTING;
    }

    public boolean isLibraryImport() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.LIBRARY_IMPORT_SETTING;
    }

    private Optional<String> extractLibraryAlias() {
        if (getLinkedElement() instanceof LibraryImport) {
            final LibraryAlias libAlias = ((LibraryImport) getLinkedElement()).getAlias();
            return libAlias.isPresent() ? Optional.of(libAlias.getLibraryAlias().getText()) : Optional.empty();
        }
        throw new IllegalArgumentException("Unable to extract library alias from non-Library setting");
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getRobotSettingImage();
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        final AModelElement<?> linkedElement = getLinkedElement();
        if (linkedElement instanceof IRegionCacheable<?>) {
            final List<FileRegion> regions = ((IRegionCacheable<?>) linkedElement).getContinuousRegions();
            // the linked element may be made from couple of elements defined in different places
            // inside the file
            for (final FileRegion region : regions) {
                if (!region.getStart().isNotSet() && region.getStart().getOffset() <= offset
                        && offset <= region.getEnd().getOffset()) {
                    return Optional.of(this);
                }
            }

        } else {
            if (!linkedElement.getBeginPosition().isNotSet() && linkedElement.getBeginPosition().getOffset() <= offset
                    && offset <= linkedElement.getEndPosition().getOffset()) {
                return Optional.of(this);
            }
        }
        return Optional.empty();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy() {
        return new PageActivatingOpeningStrategy(this);
    }

    public Optional<ImportedLibrary> getImportedLibrary() {
        final RobotProject project = getSuiteFile().getProject();
        return getImportedLibrary(Multimaps.index(project.getLibrarySpecifications(), LibrarySpecification::getName));
    }

    Optional<ImportedLibrary> getImportedLibrary(final ListMultimap<String, LibrarySpecification> indexedLibraries) {
        if (!isLibraryImport()) {
            throw new IllegalArgumentException("Cannot provide library from setting other then Library");
        }

        final List<String> args = getArguments();
        if (args.isEmpty()) {
            return Optional.empty();
        }

        final String libNameOrPath = RobotExpressions.unescapeSpaces(args.get(0));
        if (indexedLibraries.containsKey(libNameOrPath)
                && !indexedLibraries.get(libNameOrPath).get(0).getDescriptor().isStandardRemoteLibrary()) {
            // by-name import of non-remote; only remote libraries are currently used multiple
            // times
            return Optional.of(new ImportedLibrary(indexedLibraries.get(libNameOrPath).get(0), extractLibraryAlias()));

        } else if (indexedLibraries.containsKey(libNameOrPath)
                && indexedLibraries.get(libNameOrPath).get(0).getDescriptor().isStandardRemoteLibrary()) {
            // by-name import of remote library
            // empty were filtered out, so here size() > 0
            final List<RobotToken> arguments = args.subList(1, args.size())
                    .stream()
                    .map(arg -> RobotToken.create(arg))
                    .collect(Collectors.toList());
            final RemoteArgumentsResolver resolver = new RemoteArgumentsResolver(arguments);
            final Optional<String> uri = resolver.getUri();
            try {
                final RemoteLocation remoteLocation = RemoteLocation.create(uri.get());
                final String remote = RemoteArgumentsResolver
                        .stripLastSlashAndProtocolIfNecessary(remoteLocation.getUri());

                for (final LibrarySpecification spec : indexedLibraries.get(libNameOrPath)) {
                    if (remote
                            .equals(RemoteArgumentsResolver.stripLastSlashAndProtocolIfNecessary(
                                    spec.getDescriptor().getArguments().get(0)))) {
                        return Optional.of(new ImportedLibrary(spec, extractLibraryAlias()));
                    }
                }
            } catch (final Exception e) {
                // nothing to do
            }
            return Optional.empty();
        } else {
            // maybe it's a by-path import
            try {
                return findSpecForPath(libNameOrPath);
            } catch (final PathResolvingException e) {
                // ok we won't provide any spec, since we can't resolve uri
                return Optional.empty();
            }
        }
    }

    private Optional<ImportedLibrary> findSpecForPath(final String path) {
        final RobotProject project = getSuiteFile().getProject();

        final Optional<IPath> possiblePath = getPath(project, path).map(URI::getPath).map(Path::new);
        if (!possiblePath.isPresent()) {
            return Optional.empty();
        }

        return project.getLibraryEntriesStream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getKey().isReferencedLibrary())
                .filter(entry -> {
                    final IPath entryPath = new Path(entry.getKey().getFilepath());
                    final IPath libPath1 = RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(entryPath);
                    final IPath libPath2 = RedWorkspace.Paths
                            .toAbsoluteFromWorkspaceRelativeIfPossible(entryPath.addFileExtension("py"));
                    return possiblePath.get().equals(libPath1) || possiblePath.get().equals(libPath2);
                })
                .map(Entry::getValue)
                .map(spec -> new ImportedLibrary(spec, extractLibraryAlias()))
                .findFirst();
    }

    private Optional<URI> getPath(final RobotProject project, final String path) {
        final ImportPath importPath = ImportPath.from(path);
        final Optional<ResolvedImportPath> resolvedImportPath = resolvePath(project, importPath);
        if (!resolvedImportPath.isPresent()) {
            return Optional.empty();
        }

        if (importPath.isAbsolute()) {
            return Optional.of(resolvedImportPath.get().getUri());
        } else {
            final PathsProvider pathsProvider = project.createPathsProvider();
            return new ImportSearchPaths(pathsProvider).findAbsoluteUri(getSuiteFile().getFile().getLocationURI(),
                    resolvedImportPath.get());
        }
    }

    private static Optional<ResolvedImportPath> resolvePath(final RobotProject project,
            final ImportPath importPath) {
        try {
            return ResolvedImportPath.from(importPath, project.getRobotProjectHolder().getVariableMappings());
        } catch (final MalformedPathImportException e) {
            return Optional.empty();
        }
    }

    public Optional<IResource> getImportedResource() {
        if (!isResourceImport()) {
            throw new IllegalArgumentException("Cannot provide resource from setting other then Resource");
        }

        final List<String> args = getArguments();
        if (args.isEmpty()) {
            return Optional.empty();
        }

        final String path = RobotExpressions.unescapeSpaces(args.get(0));

        final RobotProject project = getSuiteFile().getProject();
        final Optional<URI> possiblePath = getPath(project, path);
        if (!possiblePath.isPresent()) {
            return Optional.empty();
        }

        final IWorkspaceRoot workspaceRoot = project.getProject().getWorkspace().getRoot();
        final RedWorkspace redWorkspace = new RedWorkspace(workspaceRoot);
        final IResource resource = redWorkspace.forUri(possiblePath.get());
        return Optional.ofNullable(resource);
    }

    public static class ImportedLibrary {

        private final LibrarySpecification spec;

        private final Optional<String> alias;

        private ImportedLibrary(final LibrarySpecification spec, final Optional<String> alias) {
            this.spec = spec;
            this.alias = alias;
        }

        public LibrarySpecification getSpecification() {
            return spec;
        }

        Optional<String> getAlias() {
            return alias;
        }
    }

    public enum SettingsGroup {
        NO_GROUP {
            @Override
            public String getName() {
                return null;
            }
        },
        METADATA {
            @Override
            public String getName() {
                return "Metadata";
            }
        },
        LIBRARIES {
            @Override
            public String getName() {
                return "Library";
            }
        },
        RESOURCES {
            @Override
            public String getName() {
                return "Resource";
            }
        },
        VARIABLES {
            @Override
            public String getName() {
                return "Variables";
            }
        };

        public static EnumSet<SettingsGroup> getImportsGroupsSet() {
            return EnumSet.of(LIBRARIES, RESOURCES, VARIABLES);
        }

        public abstract String getName();
    }
}
