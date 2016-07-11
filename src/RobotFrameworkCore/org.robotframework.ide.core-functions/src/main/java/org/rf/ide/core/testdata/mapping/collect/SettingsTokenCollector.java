/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class SettingsTokenCollector implements ITableTokensCollector {

    @Override
    public List<RobotToken> collect(final RobotFileOutput outModel) {
        final List<RobotToken> tokens = new ArrayList<>(0);

        final SettingTable settingsTable = outModel.getFileModel().getSettingTable();
        if (settingsTable.isPresent()) {
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getHeaders()));

            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getDefaultTags()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getDocumentation()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getForceTags()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getImports()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getMetadatas()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getSuiteSetups()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getSuiteTeardowns()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getTestSetups()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getTestTeardowns()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getTestTemplates()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getTestTimeouts()));
            tokens.addAll(AModelElementElementsHelper.collect(settingsTable.getUnknownSettings()));
        }

        return tokens;
    }
}
