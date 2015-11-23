/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.mapping;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testData.model.mapping.HashCommentMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.DefaultTagsMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.DefaultTagsTagNameMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.ForceTagsMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.ForceTagsTagNameMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.MetadataKeyMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.MetadataMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.MetadataValueMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.SettingDocumentationMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.SettingDocumentationTextMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.library.LibraryAliasDeclarationMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.library.LibraryAliasMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.library.LibraryArgumentsMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.library.LibraryDeclarationMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.library.LibraryNameOrPathMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.resource.ResourceDeclarationMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.resource.ResourceImportPathMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.resource.ResourceTrashDataMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteSetupKeywordArgumentMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteSetupKeywordMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteSetupMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteTeardownKeywordArgumentMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteTeardownKeywordMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.suite.SuiteTeardownMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestSetupKeywordArgumentMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestSetupKeywordMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestSetupMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTeardownKeywordArgumentMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTeardownKeywordMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTeardownMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTemplateKeywordMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTemplateMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTemplateTrashDataMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTimeoutMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTimeoutMessageMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.test.TestTimeoutValueMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.variables.VariablesArgumentsMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.variables.VariablesDeclarationMapper;
import org.rf.ide.core.testData.model.table.setting.mapping.variables.VariablesImportPathMapper;


public class SettingsMapperProvider {

    private static final List<IParsingMapper> mappers = Arrays.asList(
            new GarbageBeforeFirstTableMapper(), new TableHeaderColumnMapper(),
            new HashCommentMapper(),

            new LibraryDeclarationMapper(), new LibraryNameOrPathMapper(),
            new LibraryArgumentsMapper(), new LibraryAliasDeclarationMapper(),
            new LibraryAliasMapper(),

            new VariablesDeclarationMapper(), new VariablesImportPathMapper(),
            new VariablesArgumentsMapper(),

            new ResourceDeclarationMapper(), new ResourceImportPathMapper(),
            new ResourceTrashDataMapper(),

            new SettingDocumentationMapper(),
            new SettingDocumentationTextMapper(), new MetadataMapper(),
            new MetadataKeyMapper(), new MetadataValueMapper(),

            new SuiteSetupMapper(), new SuiteSetupKeywordMapper(),
            new SuiteSetupKeywordArgumentMapper(), new SuiteTeardownMapper(),
            new SuiteTeardownKeywordMapper(),
            new SuiteTeardownKeywordArgumentMapper(), new ForceTagsMapper(),
            new ForceTagsTagNameMapper(), new DefaultTagsMapper(),
            new DefaultTagsTagNameMapper(),

            new TestSetupMapper(), new TestSetupKeywordMapper(),
            new TestSetupKeywordArgumentMapper(), new TestTeardownMapper(),
            new TestTeardownKeywordMapper(),
            new TestTeardownKeywordArgumentMapper(), new TestTemplateMapper(),
            new TestTemplateKeywordMapper(), new TestTemplateTrashDataMapper(),
            new TestTimeoutMapper(), new TestTimeoutValueMapper(),
            new TestTimeoutMessageMapper());


    public List<IParsingMapper> getMappers() {
        return mappers;
    }
}
