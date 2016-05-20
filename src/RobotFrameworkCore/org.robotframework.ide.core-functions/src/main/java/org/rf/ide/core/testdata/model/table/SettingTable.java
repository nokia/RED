/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.presenter.DataDrivenKeywordName;
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

import com.google.common.base.Optional;

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

    private final DataDrivenKeywordName<TestTemplate> templateKeywordGenerator = new DataDrivenKeywordName<>();

    private final SettingTableMultipleDeclarationsJoiner joiner = new SettingTableMultipleDeclarationsJoiner();

    public SettingTable(final RobotFile parent) {
        super(parent);
    }

    protected SettingTableMultipleDeclarationsJoiner getSettingConsolidator() {
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

    public LibraryImport newLibraryImport() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_LIBRARY_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        LibraryImport libImp = new LibraryImport(dec);
        addImported(libImp);

        return libImp;
    }

    public ResourceImport newResourceImport() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_RESOURCE_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        ResourceImport resImp = new ResourceImport(dec);
        addImported(resImp);

        return resImp;
    }

    public VariablesImport newVariablesImport() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_VARIABLES_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        VariablesImport varImp = new VariablesImport(dec);
        addImported(varImp);

        return varImp;
    }

    public void addImported(final AImported imported) {
        imported.setParent(this);
        imports.add(imported);
    }

    public void addImported(final AImported imported, final int position) {
        imported.setParent(this);
        imports.set(position, imported);
    }

    public void removeImported(final AImported imported) {
        imports.remove(imported);
    }

    public boolean moveUpImported(final AImported imported) {
        return getMoveHelper().moveUp(imports, imported);
    }

    public boolean moveDownImported(final AImported imported) {
        return getMoveHelper().moveDown(imports, imported);
    }

    public Optional<SuiteDocumentation> documentation() {
        return getSettingConsolidator().joinSuiteDoc(getDocumentation());
    }

    public List<SuiteDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentations);
    }

    public SuiteDocumentation newSuiteDocumentation() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        SuiteDocumentation suiteDoc = new SuiteDocumentation(dec);
        addDocumentation(suiteDoc);

        return suiteDoc;
    }

    public void addDocumentation(final SuiteDocumentation doc) {
        doc.setParent(this);
        documentations.add(doc);
    }

    public void removeDocumentation() {
        documentations.clear();
    }

    public List<Metadata> getMetadatas() {
        return Collections.unmodifiableList(metadatas);
    }

    public Metadata newMetadata() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_METADATA_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        Metadata metadata = new Metadata(dec);
        addMetadata(metadata);

        return metadata;
    }

    public void addMetadata(final Metadata metadata) {
        metadata.setParent(this);
        metadatas.add(metadata);
    }

    public void addMetadata(final Metadata metadata, final int position) {
        metadata.setParent(this);
        metadatas.set(position, metadata);
    }

    public void removeMetadata(final Metadata metadata) {
        metadatas.remove(metadata);
    }

    public boolean moveUpMetadata(final Metadata metadata) {
        return getMoveHelper().moveUp(metadatas, metadata);
    }

    public boolean moveDownMetadata(final Metadata metadata) {
        return getMoveHelper().moveDown(metadatas, metadata);
    }

    public Optional<SuiteSetup> suiteSetup() {
        return getSettingConsolidator().joinSuiteSetup(getSuiteSetups());
    }

    public List<SuiteSetup> getSuiteSetups() {
        return Collections.unmodifiableList(suiteSetups);
    }

    public SuiteSetup newSuiteSetup() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        SuiteSetup suiteSetup = new SuiteSetup(dec);
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
        return getSettingConsolidator().joinSuiteTeardown(getSuiteTeardowns());
    }

    public List<SuiteTeardown> getSuiteTeardowns() {
        return Collections.unmodifiableList(suiteTeardowns);
    }

    public SuiteTeardown newSuiteTeardown() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        SuiteTeardown suiteTeardown = new SuiteTeardown(dec);
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
        return getSettingConsolidator().joinForceTag(getForceTags());
    }

    public ForceTags newForceTag() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_FORCE_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        ForceTags tags = new ForceTags(dec);
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
        return getSettingConsolidator().joinDefaultTag(getDefaultTags());
    }

    public List<DefaultTags> getDefaultTags() {
        return Collections.unmodifiableList(defaultTags);
    }

    public DefaultTags newDefaultTag() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        DefaultTags tags = new DefaultTags(dec);
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
        return getSettingConsolidator().joinTestSetup(getTestSetups());
    }

    public List<TestSetup> getTestSetups() {
        return Collections.unmodifiableList(testSetups);
    }

    public TestSetup newTestSetup() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        TestSetup testSetup = new TestSetup(dec);
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
        return getSettingConsolidator().joinTestTeardown(getTestTeardowns());
    }

    public List<TestTeardown> getTestTeardowns() {
        return Collections.unmodifiableList(testTeardowns);
    }

    public TestTeardown newTestTeardown() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        TestTeardown testTeardown = new TestTeardown(dec);
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
        RobotVersion robotVersion = getParent().getParent().getRobotVersion();
        if (robotVersion != null && robotVersion.isNewerThan(new RobotVersion(2, 9))) {
            if (isDuplicatedTemplatesDeclaration()) {
                useGenerator = false;
            }
        }

        if (useGenerator) {
            templateName = templateKeywordGenerator.createRepresentation(testTemplates);
        }

        return templateName;
    }

    private boolean isDuplicatedTemplatesDeclaration() {
        return testTemplates.size() != 1;
    }

    public TestTemplate newTestTemplate() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        TestTemplate testTemplate = new TestTemplate(dec);
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
        return getSettingConsolidator().joinTestTimeout(getTestTimeouts());
    }

    public List<TestTimeout> getTestTimeouts() {
        return Collections.unmodifiableList(testTimeouts);
    }

    public TestTimeout newTestTimeout() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getRobotVersion()).getRepresentation());

        TestTimeout testTimeout = new TestTimeout(dec);
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
