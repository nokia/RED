/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver.PathResolvingException;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

public class RobotSuiteFile implements RobotFileInternalElement {

    private final RobotElement parent;

    private final IFile file;

    private String contentTypeId;

    private RobotFileOutput fileOutput;

    private List<RobotSuiteFileSection> sections = null;

    public RobotSuiteFile(final RobotElement parent, final IFile file) {
        this.parent = parent;
        this.file = file;
    }

    public RobotSuiteFileSection createRobotSection(final String name) {
        final RobotSuiteFileSection section;
        if (name.equals(RobotVariablesSection.SECTION_NAME)) {
            getLinkedElement().includeVariableTableSection();
            section = new RobotVariablesSection(this);
            section.link(getLinkedElement().getVariableTable());
        } else if (name.equals(RobotSettingsSection.SECTION_NAME)) {
            getLinkedElement().includeSettingTableSection();
            section = new RobotSettingsSection(this);
            section.link(getLinkedElement().getSettingTable());
        } else if (name.equals(RobotCasesSection.SECTION_NAME)) {
            getLinkedElement().includeTestCaseTableSection();
            section = new RobotCasesSection(this);
            section.link(getLinkedElement().getTestCaseTable());
        } else if (name.equals(RobotKeywordsSection.SECTION_NAME)) {
            getLinkedElement().includeKeywordTableSection();
            section = new RobotKeywordsSection(this);
            section.link(getLinkedElement().getKeywordTable());
        } else {
            throw new IllegalStateException("Unrecognized section '" + name + "' cannot be created");
        }

        if (getSections().contains(section)) {
            return sections.get(sections.indexOf(section));
        } else {
            sections.add(section);
            return section;
        }
    }

    public List<RobotSuiteFileSection> getSections() {
        if (file.getLocation() != null) {
            return getSections(createFileParsingStrategy());
        } else {
            return getSections(createReparsingStrategy(""));
        }
    }

    private ParsingStrategy createFileParsingStrategy() {
        return new ParsingStrategy() {

            @Override
            public RobotFileOutput parse() {
                if (getProject().getProject().exists()) {
                    final List<RobotFileOutput> outputs = getProject().getRobotParser()
                            .parse(file.getLocation().toFile());
                    return outputs.isEmpty() ? null : outputs.get(0);
                } else {
                    // this can happen e.g. when renaming project
                    return null;
                }
            }
        };
    }

    public synchronized List<RobotSuiteFileSection> getSections(final ParsingStrategy parsingStrategy) {
        if (sections == null) {
            fileOutput = parseModel(parsingStrategy);
            if (fileOutput != null) {
                link(fileOutput.getFileModel());
            }
        }
        return sections == null ? new ArrayList<RobotSuiteFileSection>() : sections;
    }

    public void parse() {
        getSections(); // this will parse the file if needed
    }

    private void link(final RobotFile model) {
        sections = Collections.synchronizedList(new ArrayList<RobotSuiteFileSection>());
        if (model.getKeywordTable().isPresent()) {
            final RobotKeywordsSection section = new RobotKeywordsSection(this);
            section.link(model.getKeywordTable());
            sections.add(section);
        }
        if (model.getTestCaseTable().isPresent()) {
            final RobotCasesSection section = new RobotCasesSection(this);
            section.link(model.getTestCaseTable());
            sections.add(section);
        }
        if (model.getSettingTable().isPresent()) {
            final RobotSettingsSection section = new RobotSettingsSection(this);
            section.link(model.getSettingTable());
            sections.add(section);
        }
        if (model.getVariableTable().isPresent()) {
            final RobotVariablesSection section = new RobotVariablesSection(this);
            section.link(model.getVariableTable());
            sections.add(section);
        }
    }

    protected RobotFileOutput parseModel(final ParsingStrategy parsingStrategy) {
        return parsingStrategy.parse();
    }

    public void dispose() {
        contentTypeId = null;
        sections = null;
        fileOutput = null;
    }

    public void reparseEverything(final String newContent) {
        contentTypeId = null;
        sections = null;
        fileOutput = null;

        getSections(createReparsingStrategy(newContent));
    }

    protected ParsingStrategy createReparsingStrategy(final String newContent) {
        return new ParsingStrategy() {

            @Override
            public RobotFileOutput parse() {
                if (getProject().getProject().exists()) {
                    final IPath location = file.getLocation();

                    if (location == null) {
                        final File f = new File(file.getName());
                        final String content = newContent.isEmpty() ? getContent(file) : newContent;
                        return getProject().getRobotParser().parseEditorContent(content, f);
                    } else {
                        return getProject().getRobotParser().parseEditorContent(newContent, location.toFile());
                    }

                }
                // this can happen e.g. when renaming project
                return null;
            }

            private String getContent(final IFile file) {
                try (InputStream stream = file.getContents()) {
                    return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
                } catch (IOException | CoreException e) {
                    return "";
                }
            }
        };
    }

    protected void refreshOnFileChange() {
        contentTypeId = null;
        sections = null;
        fileOutput = null;
        getSections();
    }

    List<RobotElementChange> synchronizeChanges(final IResourceDelta delta) {
        if ((delta.getFlags() & IResourceDelta.MARKERS) != IResourceDelta.MARKERS) {
            refreshOnFileChange();
        }
        return new ArrayList<>();
    }

    public boolean isSuiteFile() {
        final String ctId = getContentTypeId();
        return ctId != null && ctId.startsWith(ASuiteFileDescriber.SUITE_FILE_CONTENT_ID);
    }

    public boolean isResourceFile() {
        return ASuiteFileDescriber.RESOURCE_FILE_CONTENT_ID.equals(getContentTypeId());
    }

    public boolean isInitializationFile() {
        return ASuiteFileDescriber.INIT_FILE_CONTENT_ID.equals(getContentTypeId());
    }

    protected String getContentTypeId() {
        if (contentTypeId != null) {
            return contentTypeId;
        }
        if (file != null) {
            try {
                final IContentDescription contentDescription = file.getContentDescription();
                if (contentDescription != null) {
                    final IContentType contentType = contentDescription.getContentType();
                    if (contentType != null) {
                        contentTypeId = contentType.getId();
                        return contentTypeId;
                    }
                }
            } catch (final CoreException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFile other = (RobotSuiteFile) obj;
            return Objects.equals(file, other.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getRobotImage();
    }

    @Override
    public Position getPosition() {
        return new Position(0);
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(0, 0, 0);
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    public IFile getFile() {
        return file;
    }

    public String getFileExtension() {
        return file.getFileExtension();
    }

    public RobotFile getLinkedElement() {
        return fileOutput == null ? null : fileOutput.getFileModel();
    }

    @Override
    public List<RobotSuiteFileSection> getChildren() {
        return sections == null ? Lists.<RobotSuiteFileSection> newArrayList() : sections;
    }

    public boolean isEditable() {
        return !file.isReadOnly();
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return this;
    }

    public RobotProject getProject() {
        return RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
    }

    @SuppressWarnings("unchecked")
    public <T extends RobotElement> Optional<T> findSection(final Class<T> sectionClass) {
        for (final RobotElement elem : getSections()) {
            if (sectionClass.isInstance(elem)) {
                return (Optional<T>) Optional.of(elem);
            }
        }
        return Optional.absent();
    }

    /**
     * Gets model element for given offset in file
     * 
     * @param offset
     * @return
     */
    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        for (final RobotSuiteFileSection section : getChildren()) {
            final Optional<? extends RobotElement> candidate = section.findElement(offset);
            if (candidate.isPresent()) {
                return candidate;
            }
            // final Position position = section.getPosition();
            // if (position.getOffset() <= offset && offset <= position.getOffset() +
            // position.getLength()) {
            // final Optional<? extends RobotElement> candidate = section.findElement(offset);
            // if (candidate.isPresent()) {
            // return candidate;
            // }
            // }
        }
        return Optional.of(this);
    }

    public Map<LibrarySpecification, String> getImportedLibraries() {
        final Optional<RobotSettingsSection> section = findSection(RobotSettingsSection.class);
        final Map<String, String> toImport = newHashMap();
        if (section.isPresent()) {
            final List<RobotKeywordCall> importSettings = section.get().getImportSettings();
            for (final RobotKeywordCall element : importSettings) {
                final RobotSetting setting = (RobotSetting) element;
                if (SettingsGroup.LIBRARIES == setting.getGroup()) {
                    final String nameOrPath = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
                    if (nameOrPath != null) {
                        final String alias = extractLibraryAlias(setting);
                        toImport.put(nameOrPath, alias);
                    }
                }
            }
        }

        final Map<LibrarySpecification, String> imported = newHashMap();
        for (final LibrarySpecification spec : getProject().getLibrariesSpecifications()) {
            if (toImport.containsKey(spec.getName())) {
                imported.put(spec, toImport.get(spec.getName()));
                toImport.remove(spec.getName());
            } else if (spec.isAccessibleWithoutImport()) {
                imported.put(spec, "");
            }
        }
        for (final String toImportPathOrName : toImport.keySet()) {
            try {
                final LibrarySpecification spec = findSpecForPath(toImportPathOrName);
                if (spec != null) {
                    imported.put(spec, toImport.get(toImportPathOrName));
                }
            } catch (final PathResolvingException e) {
                // ok we won't provide any spec, since we can't resolve uri
            }
        }
        return imported;
    }

    private String extractLibraryAlias(final RobotSetting setting) {
        final LibraryAlias libAlias = ((LibraryImport) setting.getLinkedElement()).getAlias();
        return libAlias.isPresent() ? libAlias.getLibraryAlias().getText() : "";
    }

    private LibrarySpecification findSpecForPath(final String toImportPathOrName) {
        List<IPath> possiblePathsToLib = null;
        try {
            possiblePathsToLib = PathsResolver.resolveToAbsolutePossiblePaths(this, toImportPathOrName);
        } catch (final PathResolvingException e) {
            possiblePathsToLib = PathsResolver.resolveToAbsolutePossiblePaths(this,
                    PathsResolver.resolveParametrizedPath(getProject(), toImportPathOrName).toPortableString());
        }
        if (possiblePathsToLib == null || possiblePathsToLib.isEmpty()) {
            return null;
        }
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : getProject().getReferencedLibraries()
                .entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            final IPath entryPath = entry.getKey().getFilepath();
            final IPath libPath1 = PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(entryPath);
            final IPath libPath2 = PathsConverter
                    .toAbsoluteFromWorkspaceRelativeIfPossible(entryPath.addFileExtension("py"));
            for (final IPath candidate : possiblePathsToLib) {
                if (candidate.equals(libPath1) || candidate.equals(libPath2)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public List<RobotKeywordDefinition> getUserDefinedKeywords() {
        final Optional<RobotKeywordsSection> optionalKeywords = findSection(RobotKeywordsSection.class);
        if (optionalKeywords.isPresent()) {
            return optionalKeywords.get().getUserDefinedKeywords();
        }
        return newArrayList();
    }

    public List<IPath> getResourcesPaths() {
        final Optional<RobotSettingsSection> optionalSettings = findSection(RobotSettingsSection.class);
        if (optionalSettings.isPresent()) {
            return optionalSettings.get().getResourcesPaths();
        }
        return newArrayList();
    }

    public List<IPath> getVariablesPaths() {
        final Optional<RobotSettingsSection> optionalSettings = findSection(RobotSettingsSection.class);
        if (optionalSettings.isPresent()) {
            return optionalSettings.get().getVariablesPaths();
        }
        return newArrayList();
    }

    public List<VariablesFileImportReference> getVariablesFromLocalReferencedFiles() {
        return fileOutput != null ? fileOutput.getVariablesImportReferences()
                : new ArrayList<VariablesFileImportReference>();
    }

    public List<ImportedVariablesFile> getImportedVariables() {
        final Optional<RobotSettingsSection> section = findSection(RobotSettingsSection.class);
        final List<ImportedVariablesFile> alreadyImported = newArrayList();
        if (section.isPresent()) {
            for (final RobotElement element : section.get().getVariablesSettings()) {
                final RobotSetting setting = (RobotSetting) element;
                alreadyImported.add(new ImportedVariablesFile(setting.getArguments()));
            }
            return alreadyImported;
        }
        return newArrayList();
    }

    public static class ImportedVariablesFile {

        private List<String> args;

        public ImportedVariablesFile(final List<String> args) {
            this.args = args;
        }

        public List<String> getArgs() {
            return args;
        }

        public void setArgs(final List<String> args) {
            this.args = args;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == ImportedVariablesFile.class) {
                final ImportedVariablesFile other = (ImportedVariablesFile) obj;
                return Objects.equals(args, other.args);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return args.hashCode();
        }
    }

    interface ParsingStrategy {

        RobotFileOutput parse();
    }
}
