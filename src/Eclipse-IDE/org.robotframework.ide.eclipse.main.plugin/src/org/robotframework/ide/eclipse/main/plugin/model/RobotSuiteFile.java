/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class RobotSuiteFile implements RobotFileInternalElement {

    private final RobotElement parent;

    private final IFile file;

    private RobotFileOutput fileOutput;
    
    private List<RobotSuiteFileSection> sections = null;

    public RobotSuiteFile(final RobotElement parent, final IFile file) {
        this.parent = parent;
        this.file = file;
    }

    public RobotSuiteFileSection createRobotSection(final String name) {
        final RobotSuiteFileSection section;
        if (name.equals(RobotVariablesSection.SECTION_NAME)) {
            section = new RobotVariablesSection(this);
        } else if (name.equals(RobotSettingsSection.SECTION_NAME)) {
            section = new RobotSettingsSection(this);
        } else if (name.equals(RobotCasesSection.SECTION_NAME)) {
            section = new RobotCasesSection(this);
        } else if (name.equals(RobotKeywordsSection.SECTION_NAME)) {
            section = new RobotKeywordsSection(this);
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
        return getSections(new ParsingStrategy() {
            @Override
            public RobotFileOutput parse() {
                return getProject().getRobotParser().parse(file.getLocation().toFile()).get(0);
            }
        });
    }

    public List<RobotSuiteFileSection> getSections(final ParsingStrategy parsingStrategy) {
        if (sections == null) {
            fileOutput = parseModel(parsingStrategy);
            link(fileOutput.getFileModel());
        }
        return sections;
    }
    
    private void link(final RobotFile model) {
        sections = new ArrayList<>();
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
        sections = null;
        fileOutput = null;
    }

    public void reparseEverything(final String newContent) {
        sections = null;
        fileOutput = null;

        getSections(new ParsingStrategy() {
            @Override
            public RobotFileOutput parse() {
                return getProject().getRobotParser().parseEditorContent(newContent, file.getLocation().toFile());
            }
        });
    }

    protected void refreshOnFileChange() {
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
        return RobotSuiteFileDescriber.SUITE_FILE_CONTENT_ID.equals(getContentTypeId());
    }

    public boolean isResourceFile() {
        return RobotSuiteFileDescriber.RESOURCE_FILE_CONTENT_ID.equals(getContentTypeId());
    }

    public boolean isInitializationFile() {
        return RobotSuiteFileDescriber.INIT_FILE_CONTENT_ID.equals(getContentTypeId());
    }

    protected String getContentTypeId() {
        if (file != null) {
            try {
                return file.getContentDescription().getContentType().getId();
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
    public Position getDefinitionPosition() {
        return new Position(0);
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
//            final Position position = section.getPosition();
//            if (position.getOffset() <= offset && offset <= position.getOffset() + position.getLength()) {
                // final Optional<? extends RobotElement> candidate = section.findElement(offset);
                // if (candidate.isPresent()) {
                // return candidate;
                // }
            // }
        }
        return Optional.of(this);
    }

    public List<LibrarySpecification> getImportedLibraries() {
        final Optional<RobotSettingsSection> section = findSection(RobotSettingsSection.class);
        final List<String> toImport = newArrayList();
        if (section.isPresent()) {
            final List<RobotKeywordCall> importSettings = section.get().getImportSettings();
            for (final RobotKeywordCall element : importSettings) {
                final RobotSetting setting = (RobotSetting) element;
                if (SettingsGroup.LIBRARIES == setting.getGroup()) {
                    final String nameOrPath = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
                    if (nameOrPath != null) {
                        toImport.add(nameOrPath);
                    }
                }
            }
        }

        final List<LibrarySpecification> imported = newArrayList();
        for (final LibrarySpecification spec : getProject().getStandardLibraries()) {
            if (spec.isAccessibleWithoutImport() || toImport.contains(spec.getName())) {
                imported.add(spec);
                toImport.remove(spec.getName());
            }
        }
        for (final LibrarySpecification spec : getProject().getReferencedLibraries()) {
            if (toImport.contains(spec.getName())) {
                imported.add(spec);
                toImport.remove(spec.getName());
            }
        }
        for (final String toImportPathOrName : toImport) {
            final LibrarySpecification spec = findSpecForPath(toImportPathOrName);
            if (spec != null) {
                imported.add(spec);
            }
        }
        return imported;
    }

    private LibrarySpecification findSpecForPath(final String toImportPathOrName) {
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : getProject().getReferencedLibrariesMapping()
                .entrySet()) {
            for (final IPath path : PathsResolver.resolveToAbsolutePath(this, toImportPathOrName)) {
                if (path.equals(
                        PathsConverter.toAbsoluteFromWorkspaceRelativeIfPossible(new Path(entry.getKey().getPath())))) {
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
    
    public List<ReferencedVariableFile> getVariablesFromReferencedFiles() {
        return getProject().getVariablesFromReferencedFiles();
    }
    
    public List<ARobotInternalVariable<?>> getGlobalVariables() {
        return getProject().getRobotProjectHolder().getGlobalVariables();
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
    
    public List<String> getSectionHeaders() {
        final RobotFile file = fileOutput.getFileModel();
        final List<String> headersList = new ArrayList<>();
        headersList.add(extractHeader(file.getSettingTable().getHeaders(), "*** Settings ***"));
        headersList.add(extractHeader(file.getVariableTable().getHeaders(), "*** Variables ***"));
        headersList.add(extractHeader(file.getTestCaseTable().getHeaders(), "*** Test Cases ***"));
        headersList.add(extractHeader(file.getKeywordTable().getHeaders(), "*** Keywords ***"));
        return headersList;
    }

    private String extractHeader(final List<TableHeader<? extends ARobotSectionTable>> modelHeaders, final String defaultHeader) {
        if (!modelHeaders.isEmpty()) {
            return modelHeaders.get(0).getTableHeader().getText().toString();
        }
        return defaultHeader;
    }

    public int getLineDelimiterLength() {
        return fileOutput.getFileLineSeparator().length();
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
