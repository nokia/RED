/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.io.CharStreams;

public class RobotSuiteFile extends RobotProjectElement implements RobotFileInternalElement {

    private final IFile file;

    protected String contentTypeId;

    private RobotFileOutput fileOutput;

    private List<RobotSuiteFileSection> sections;

    public RobotSuiteFile(final RobotElement parent, final IFile file) {
        super(parent);
        this.file = file;
    }

    public RobotSuiteFileSection createRobotSection(final String name) {
        final RobotTokenType headerType = RobotTokenType.findTypeOfDeclarationForTableHeader(name);
        final RobotFile model = getLinkedElement();
        model.includeTableSection(headerType);

        final RobotSuiteFileSection section = createRobotSection(headerType, model);

        if (getSections().contains(section)) {
            return sections.get(sections.indexOf(section));
        } else {
            sections.add(section);
            return section;
        }
    }

    private RobotSuiteFileSection createRobotSection(final RobotTokenType headerType, final RobotFile model) {
        switch (headerType) {
            case VARIABLES_TABLE_HEADER:
                return new RobotVariablesSection(this, model.getVariableTable());
            case SETTINGS_TABLE_HEADER:
                return new RobotSettingsSection(this, model.getSettingTable());
            case TEST_CASES_TABLE_HEADER:
                return new RobotCasesSection(this, model.getTestCaseTable());
            case TASKS_TABLE_HEADER:
                return new RobotTasksSection(this, model.getTasksTable());
            case KEYWORDS_TABLE_HEADER:
                return new RobotKeywordsSection(this, model.getKeywordTable());
            default:
                throw new IllegalStateException("Unrecognized section header type");
        }
    }

    public List<RobotSuiteFileSection> getSections() {
        if (file.getLocation() == null) {
            return getSections(createReparsingStrategy(getContent(file)));
        } else {
            return getSections(createFileParsingStrategy());
        }
    }

    private String getContent(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
        } catch (IOException | CoreException e) {
            return "";
        }
    }

    public synchronized List<RobotSuiteFileSection> getSections(final ParsingStrategy parsingStrategy) {
        if (sections == null) {
            link(parsingStrategy.parse());
        }
        return sections == null ? new ArrayList<>() : sections;
    }

    public void parse() {
        getSections(); // this will parse the file if needed
    }

    public void link(final RobotFileOutput fileOutput) {
        this.fileOutput = fileOutput;
        if (this.fileOutput != null) {
            link(fileOutput.getFileModel());
        }
    }

    private synchronized void link(final RobotFile model) {
        sections = Collections.synchronizedList(new ArrayList<>());
        if (model.getKeywordTable().isPresent()) {
            final RobotKeywordsSection section = new RobotKeywordsSection(this, model.getKeywordTable());
            section.link();
            sections.add(section);
        }
        if (model.getTestCaseTable().isPresent()) {
            final RobotCasesSection section = new RobotCasesSection(this, model.getTestCaseTable());
            section.link();
            sections.add(section);
        }
        if (model.getTasksTable().isPresent()) {
            final RobotTasksSection section = new RobotTasksSection(this, model.getTasksTable());
            section.link();
            sections.add(section);
        }
        if (model.getSettingTable().isPresent()) {
            final RobotSettingsSection section = new RobotSettingsSection(this, model.getSettingTable());
            section.link();
            sections.add(section);
        }
        if (model.getVariableTable().isPresent()) {
            final RobotVariablesSection section = new RobotVariablesSection(this, model.getVariableTable());
            section.link();
            sections.add(section);
        }
        Collections.sort(sections,
                (section1, section2) -> Integer.compare(section1.getHeaderLine(), section2.getHeaderLine()));
    }

    public synchronized void dispose() {
        if (fileOutput != null) {
            // this is required because we want to reparse file output when user did some
            // changes to the model, but those changes were discarded (editor wasn't saved)
            fileOutput.setLastModificationEpochTime(System.currentTimeMillis());
        }
        contentTypeId = null;
        sections = null;
        fileOutput = null;
    }

    public synchronized void reparseEverything(final String newContent) {
        contentTypeId = null;
        sections = null;
        fileOutput = null;

        getSections(createReparsingStrategy(newContent));
    }

    protected synchronized void refreshOnFileChange() {
        contentTypeId = null;
        sections = null;
        fileOutput = null;
        getSections();
    }

    List<RobotElementChange> synchronizeChanges(final IResourceDelta delta) {
        if (delta.getFlags() != IResourceDelta.MARKERS) {
            refreshOnFileChange();
        }
        return new ArrayList<>();
    }

    public boolean isFromLocalStorage() {
        return false;
    }

    public boolean isEditable() {
        return !file.isReadOnly();
    }

    public boolean isTsvFile() {
        return "tsv".equalsIgnoreCase(getFileExtension());
    }

    public boolean isSuiteFile() {
        return ASuiteFileDescriber.isSuiteFile(getContentTypeId());
    }

    public boolean isRpaSuiteFile() {
        return ASuiteFileDescriber.isRpaSuiteFile(getContentTypeId());
    }

    public boolean isResourceFile() {
        return ASuiteFileDescriber.isResourceFile(getContentTypeId());
    }

    public boolean isInitializationFile() {
        return ASuiteFileDescriber.isInitializationFile(getContentTypeId());
    }

    protected String getContentTypeId() {
        if (contentTypeId != null) {
            return contentTypeId;
        }
        try {
            if (!file.isSynchronized(IResource.DEPTH_ONE)) {
                file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
            }
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
    public OpenStrategy getOpenRobotEditorStrategy() {
        return new OpenStrategy();
    }

    public IFile getFile() {
        return file;
    }

    public String getFileExtension() {
        return file.getFileExtension();
    }

    public IPath getFullPath() {
        return file.getFullPath();
    }

    @Override
    public RobotFile getLinkedElement() {
        return fileOutput == null ? null : fileOutput.getFileModel();
    }

    @Override
    public List<RobotSuiteFileSection> getChildren() {
        return sections == null ? new ArrayList<>() : sections;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return this;
    }

    public RobotParser createRobotParser() {
        final RobotProject robotProject = getRobotProject();
        return new RobotParser(robotProject.getRobotProjectHolder(), robotProject.getRobotParserComplianceVersion());
    }

    public File getRobotParserFile() {
        final IPath location = file.getLocation();
        return location == null ? new File(file.getName()) : location.toFile();
    }

    protected ParsingStrategy createReparsingStrategy(final String fileContent) {
        return () -> {
            if (getRobotProject().getProject().exists()) {
                final RobotParser parser = createRobotParser();
                return parser.parseEditorContent(fileContent, getRobotParserFile());
            }
            // this can happen e.g. when renaming project
            return null;
        };
    }

    private ParsingStrategy createFileParsingStrategy() {
        return () -> {
            if (getRobotProject().getProject().exists()) {
                final RobotParser parser = createRobotParser();
                final List<RobotFileOutput> outputs = parser.parse(getRobotParserFile());
                return outputs.isEmpty() ? null : outputs.get(0);
            }
            // this can happen e.g. when renaming project
            return null;
        };
    }

    public IRuntimeEnvironment getRuntimeEnvironment() {
        return getRobotProject().getRuntimeEnvironment();
    }

    public <T extends RobotElement> Optional<T> findSection(final Class<T> sectionClass) {
        for (final RobotElement elem : getSections()) {
            if (sectionClass.isInstance(elem)) {
                return Optional.of(sectionClass.cast(elem));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets model element for given offset in file
     *
     * @param offset
     * @return
     */
    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        for (final RobotSuiteFileSection section : getSections()) {
            final Optional<? extends RobotElement> candidate = section.findElement(offset);
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        return Optional.of(this);
    }

    public Set<LibrarySpecification> getNotImportedLibraries() {
        final Set<LibrarySpecification> allLibraries = new HashSet<>(getRobotProject().getLibrarySpecifications());
        allLibraries.removeAll(getImportedLibraries().keySet());
        return allLibraries;
    }

    public Multimap<LibrarySpecification, Optional<String>> getImportedLibraries() {
        final Collection<LibrarySpecification> specifications = getRobotProject().getLibrarySpecifications();
        final ImmutableListMultimap<String, LibrarySpecification> specs = Multimaps.index(specifications,
                LibrarySpecification::getName);

        final List<RobotSetting> libraryImports = findSection(RobotSettingsSection.class).map(Stream::of)
                .orElseGet(Stream::empty)
                .map(RobotSettingsSection::getLibrariesSettings)
                .flatMap(Collection::stream)
                .collect(toList());

        final SetMultimap<LibrarySpecification, Optional<String>> importedLibs = LinkedHashMultimap.create();
        for (final RobotSetting setting : libraryImports) {
            setting.getImportedLibrary(specs)
                    .ifPresent(importedLib -> importedLibs.put(importedLib.getSpecification(), importedLib.getAlias()));
        }

        // some libs are accessible always even when not imported explicitely
        specifications.stream().filter(LibrarySpecification::isAccessibleWithoutImport).forEach(
                spec -> {
                    if (!importedLibs.containsKey(spec)) {
                        importedLibs.put(spec, Optional.empty());
                    }
                });
        return importedLibs;
    }

    public List<IResource> getImportedResources() {
        return findSection(RobotSettingsSection.class).map(Stream::of)
                .orElseGet(Stream::empty)
                .map(RobotSettingsSection::getResourcesSettings)
                .flatMap(Collection::stream)
                .map(RobotSetting::getImportedResource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public List<RobotKeywordDefinition> getUserDefinedKeywords() {
        return findSection(RobotKeywordsSection.class).map(RobotKeywordsSection::getUserDefinedKeywords)
                .orElseGet(ArrayList::new);
    }

    public List<RobotCase> getTestCases() {
        return findSection(RobotCasesSection.class).map(RobotCasesSection::getTestCases)
                .orElseGet(ArrayList::new);
    }

    public List<RobotTask> getTasks() {
        return findSection(RobotTasksSection.class).map(RobotTasksSection::getTasks)
                .orElseGet(ArrayList::new);
    }

    public List<String> getResourcesPaths() {
        return findSection(RobotSettingsSection.class).map(RobotSettingsSection::getResourcesPaths)
                .orElseGet(ArrayList::new);
    }

    public List<String> getVariablesPaths() {
        return findSection(RobotSettingsSection.class).map(RobotSettingsSection::getVariablesPaths)
                .orElseGet(ArrayList::new);
    }

    public List<VariablesFileImportReference> getVariablesFromLocalReferencedFiles() {
        if (fileOutput == null) {
            return new ArrayList<>();
        }
        final RobotProject robotProject = getRobotProject();
        return fileOutput.getVariablesImportReferences(robotProject.getRobotProjectHolder(),
                new RobotProjectPathsProvider(getRobotProject()).createPathsProvider());
    }

    public String getDocumentation() {
        return findSection(RobotSettingsSection.class)
                .flatMap(section -> section.getSetting(ModelType.SUITE_DOCUMENTATION))
                .map(RobotKeywordCall::getLinkedElement)
                .map(SuiteDocumentation.class::cast)
                .map(DocumentationServiceHandler::toShowConsolidated)
                .orElse("<not documented>");
    }

    public Documentation createDocumentation() {
        // TODO : provide format depending on source
        final Set<String> keywords = getSuiteFile().getUserDefinedKeywords()
                .stream()
                .map(RobotKeywordDefinition::getName)
                .collect(toSet());
        return new Documentation(DocFormat.ROBOT, getDocumentation(), keywords);
    }

    public static IFile createRobotInitializationFile(final IFolder folder, final String extension)
            throws CoreException {
        final IFile initFile = folder.getFile(RobotFile.INIT_NAME + "." + extension);
        initFile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
        return initFile;
    }

    @FunctionalInterface
    public interface ParsingStrategy {

        RobotFileOutput parse();
    }
}
