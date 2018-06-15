/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.presenter.DataDrivenKeywordName;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SettingTable extends ARobotSectionTable {

    private final List<AImported> imports = new ArrayList<>();

    private final List<SuiteDocumentation> documentations = new ArrayList<>();

    private final List<Metadata> metadatas = new ArrayList<>();

    private final List<SuiteSetup> suiteSetups = new ArrayList<>();

    private final List<SuiteTeardown> suiteTeardowns = new ArrayList<>();

    private final List<ForceTags> forceTags = new ArrayList<>();

    private final List<DefaultTags> defaultTags = new ArrayList<>();

    private final List<TestSetup> testSetups = new ArrayList<>();

    private final List<TestTeardown> testTeardowns = new ArrayList<>();

    private final List<TestTemplate> testTemplates = new ArrayList<>();

    private final List<TestTimeout> testTimeouts = new ArrayList<>();

    private final List<UnknownSetting> unknownSettings = new ArrayList<>();

    private final SettingTableMultipleElementsViewCreator joiner = new SettingTableMultipleElementsViewCreator();

    public SettingTable(final RobotFile parent) {
        super(parent);
    }

    protected SettingTableMultipleElementsViewCreator getSettingViewCreator() {
        return joiner;
    }

    public boolean isEmpty() {
        return imports.isEmpty() && documentations.isEmpty() && metadatas.isEmpty() && suiteSetups.isEmpty()
                && suiteTeardowns.isEmpty() && forceTags.isEmpty() && defaultTags.isEmpty() && testSetups.isEmpty()
                && testTeardowns.isEmpty() && testTemplates.isEmpty() && testTimeouts.isEmpty()
                && unknownSettings.isEmpty();
    }

    public List<AImported> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public List<VariablesImport> getVariablesImports() {
        return getImports().stream().filter(VariablesImport.class::isInstance).map(VariablesImport.class::cast).collect(
                toList());
    }

    public List<ResourceImport> getResourcesImports() {
        return getImports().stream().filter(ResourceImport.class::isInstance).map(ResourceImport.class::cast).collect(
                toList());
    }

    public List<LibraryImport> getLibraryImports() {
        return getImports().stream().filter(LibraryImport.class::isInstance).map(LibraryImport.class::cast).collect(
                toList());
    }

    public LibraryImport newLibraryImport() {
        return newLibraryImport(imports.size());
    }

    public LibraryImport newLibraryImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_LIBRARY_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final LibraryImport libImp = new LibraryImport(dec);
        addImported(libImp, index);

        return libImp;
    }

    public ResourceImport newResourceImport() {
        return newResourceImport(imports.size());
    }

    public ResourceImport newResourceImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_RESOURCE_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final ResourceImport resImp = new ResourceImport(dec);
        addImported(resImp, index);

        return resImp;
    }

    public VariablesImport newVariablesImport() {
        return newVariablesImport(imports.size());
    }

    public VariablesImport newVariablesImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_VARIABLES_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final VariablesImport varImp = new VariablesImport(dec);
        addImported(varImp, index);

        return varImp;
    }

    public void addImported(final AImported imported) {
        imported.setParent(this);
        imports.add(imported);
    }

    public void addImported(final AImported imported, final int position) {
        imported.setParent(this);
        imports.add(position, imported);
    }

    public void removeImported(final AImported imported) {
        imports.remove(imported);
    }

    public boolean moveUpImported(final AImported imported) {
        return MoveElementHelper.moveUp(imports, imported);
    }

    public boolean moveDownImported(final AImported imported) {
        return MoveElementHelper.moveDown(imports, imported);
    }

    public Optional<SuiteDocumentation> documentation() {
        return getSettingViewCreator().createViewAboutSuiteDoc(documentations);
    }

    public List<SuiteDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentations);
    }

    public SuiteDocumentation newSuiteDocumentation() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final SuiteDocumentation suiteDoc = new SuiteDocumentation(dec);
        addDocumentation(suiteDoc);

        return suiteDoc;
    }

    public void addDocumentation(final SuiteDocumentation doc) {
        doc.setParent(this);
        documentations.add(doc);
        getParent().getParent().getDocumentationCacher().register(doc);
    }

    public void removeDocumentation() {
        for (final SuiteDocumentation doc : documentations) {
            getParent().getParent().getDocumentationCacher().unregister(doc);
        }
        documentations.clear();
    }

    public List<Metadata> getMetadatas() {
        return Collections.unmodifiableList(metadatas);
    }

    public Metadata newMetadata() {
        return newMetadata(metadatas.size());
    }

    public Metadata newMetadata(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_METADATA_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final Metadata metadata = new Metadata(dec);
        addMetadata(metadata, index);

        return metadata;
    }

    public void addMetadata(final Metadata metadata) {
        metadata.setParent(this);
        metadatas.add(metadata);
    }

    public void addMetadata(final Metadata metadata, final int position) {
        metadata.setParent(this);
        metadatas.add(position, metadata);
    }

    public void removeMetadata(final Metadata metadata) {
        metadatas.remove(metadata);
    }

    public boolean moveUpMetadata(final Metadata metadata) {
        return MoveElementHelper.moveUp(metadatas, metadata);
    }

    public boolean moveDownMetadata(final Metadata metadata) {
        return MoveElementHelper.moveDown(metadatas, metadata);
    }

    public Optional<SuiteSetup> suiteSetup() {
        return getSettingViewCreator().createViewAboutSuiteSetup(suiteSetups);
    }

    public List<SuiteSetup> getSuiteSetups() {
        return Collections.unmodifiableList(suiteSetups);
    }

    public SuiteSetup newSuiteSetup() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final SuiteSetup suiteSetup = new SuiteSetup(dec);
        addSuiteSetup(suiteSetup);

        return suiteSetup;
    }

    public void addSuiteSetup(final SuiteSetup suiteSetup) {
        suiteSetup.setParent(this);
        suiteSetups.add(suiteSetup);
    }

    public void removeSuiteSetup() {
        suiteSetups.clear();
    }

    public Optional<SuiteTeardown> suiteTeardown() {
        return getSettingViewCreator().createViewAboutSuiteTeardown(suiteTeardowns);
    }

    public List<SuiteTeardown> getSuiteTeardowns() {
        return Collections.unmodifiableList(suiteTeardowns);
    }

    public SuiteTeardown newSuiteTeardown() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final SuiteTeardown suiteTeardown = new SuiteTeardown(dec);
        addSuiteTeardown(suiteTeardown);

        return suiteTeardown;
    }

    public void addSuiteTeardown(final SuiteTeardown suiteTeardown) {
        suiteTeardown.setParent(this);
        suiteTeardowns.add(suiteTeardown);
    }

    public void removeSuiteTeardown() {
        suiteTeardowns.clear();
    }

    public Optional<ForceTags> forceTags() {
        return getSettingViewCreator().createViewAboutForceTags(forceTags);
    }

    public ForceTags newForceTag() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_FORCE_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final ForceTags tags = new ForceTags(dec);
        addForceTags(tags);

        return tags;
    }

    public List<ForceTags> getForceTags() {
        return Collections.unmodifiableList(forceTags);
    }

    public void addForceTags(final ForceTags tags) {
        tags.setParent(this);
        forceTags.add(tags);
    }

    public void removeForceTags() {
        forceTags.clear();
    }

    public Optional<DefaultTags> defaultTags() {
        return getSettingViewCreator().createViewAboutDefaultTags(defaultTags);
    }

    public List<DefaultTags> getDefaultTags() {
        return Collections.unmodifiableList(defaultTags);
    }

    public DefaultTags newDefaultTag() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final DefaultTags tags = new DefaultTags(dec);
        addDefaultTags(tags);

        return tags;
    }

    public void addDefaultTags(final DefaultTags tags) {
        tags.setParent(this);
        defaultTags.add(tags);
    }

    public void removeDefaultTags() {
        defaultTags.clear();
    }

    public Optional<TestSetup> testSetup() {
        return getSettingViewCreator().createViewAboutTestSetup(testSetups);
    }

    public List<TestSetup> getTestSetups() {
        return Collections.unmodifiableList(testSetups);
    }

    public TestSetup newTestSetup() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final TestSetup testSetup = new TestSetup(dec);
        addTestSetup(testSetup);

        return testSetup;
    }

    public void addTestSetup(final TestSetup testSetup) {
        testSetup.setParent(this);
        testSetups.add(testSetup);
    }

    public void removeTestSetup() {
        testSetups.clear();
    }

    public Optional<TestTeardown> testTeardown() {
        return getSettingViewCreator().createViewAboutTestTeardown(testTeardowns);
    }

    public List<TestTeardown> getTestTeardowns() {
        return Collections.unmodifiableList(testTeardowns);
    }

    public TestTeardown newTestTeardown() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final TestTeardown testTeardown = new TestTeardown(dec);
        addTestTeardown(testTeardown);

        return testTeardown;
    }

    public void addTestTeardown(final TestTeardown testTeardown) {
        testTeardown.setParent(this);
        testTeardowns.add(testTeardown);
    }

    public void removeTestTeardown() {
        testTeardowns.clear();
    }

    public List<TestTemplate> getTestTemplates() {
        return Collections.unmodifiableList(testTemplates);
    }

    public String getRobotViewAboutTestTemplate() {
        String templateName = null;
        boolean useGenerator = true;
        final RobotVersion robotVersion = getParent().getParent().getRobotVersion();
        if (robotVersion != null && robotVersion.isNewerThan(new RobotVersion(2, 9))) {
            if (isDuplicatedTemplatesDeclaration()) {
                useGenerator = false;
            }
        }

        if (useGenerator) {
            templateName = DataDrivenKeywordName.createRepresentation(testTemplates);
        }

        return templateName;
    }

    private boolean isDuplicatedTemplatesDeclaration() {
        return testTemplates.size() != 1;
    }

    public TestTemplate newTestTemplate() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final TestTemplate testTemplate = new TestTemplate(dec);
        addTestTemplate(testTemplate);

        return testTemplate;
    }

    public void addTestTemplate(final TestTemplate testTemplate) {
        testTemplate.setParent(this);
        testTemplates.add(testTemplate);
    }

    public void removeTestTemplate() {
        testTemplates.clear();
    }

    public Optional<TestTimeout> testTimeout() {
        return getSettingViewCreator().createViewAboutTestTimeout(testTimeouts);
    }

    public List<TestTimeout> getTestTimeouts() {
        return Collections.unmodifiableList(testTimeouts);
    }

    public TestTimeout newTestTimeout() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        final TestTimeout testTimeout = new TestTimeout(dec);
        addTestTimeout(testTimeout);

        return testTimeout;
    }

    public void addTestTimeout(final TestTimeout testTimeout) {
        testTimeout.setParent(this);
        testTimeouts.add(testTimeout);
    }

    public void removeTestTimeout() {
        testTimeouts.clear();
    }

    public List<UnknownSetting> getUnknownSettings() {
        return Collections.unmodifiableList(unknownSettings);
    }

    public void addUnknownSetting(final UnknownSetting unknownSetting) {
        unknownSetting.setParent(this);
        unknownSettings.add(unknownSetting);
    }
}
