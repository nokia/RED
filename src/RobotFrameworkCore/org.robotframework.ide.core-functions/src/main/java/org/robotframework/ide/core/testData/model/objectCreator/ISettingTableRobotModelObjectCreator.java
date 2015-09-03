package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.model.table.setting.ForceTags;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.model.table.setting.SuiteSetup;
import org.robotframework.ide.core.testData.model.table.setting.SuiteTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestSetup;
import org.robotframework.ide.core.testData.model.table.setting.TestTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestTemplate;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.model.table.setting.UnknownSetting;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface ISettingTableRobotModelObjectCreator {

    LibraryImport createLibraryImport(final RobotToken declaration);


    LibraryAlias createLibraryAlias(final RobotToken declaration);


    VariablesImport createVariablesImport(final RobotToken declaration);


    ResourceImport createResourceImport(final RobotToken declaration);


    SuiteDocumentation createSuiteDocumentation(final RobotToken declaration);


    Metadata createMetadata(final RobotToken declaration);


    SuiteSetup createSuiteSetup(final RobotToken declaration);


    SuiteTeardown createSuiteTeardown(final RobotToken declaration);


    ForceTags createForceTags(final RobotToken declaration);


    DefaultTags createDefaultTags(final RobotToken declaration);


    TestSetup createTestSetup(final RobotToken declaration);


    TestTeardown createTestTeardown(final RobotToken declaration);


    TestTemplate createTestTemplate(final RobotToken declaration);


    TestTimeout createTestTimeout(final RobotToken declaration);


    UnknownSetting createUnknownSetting(final RobotToken declaration);
}
