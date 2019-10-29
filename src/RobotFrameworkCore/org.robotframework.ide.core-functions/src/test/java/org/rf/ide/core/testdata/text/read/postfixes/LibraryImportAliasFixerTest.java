/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class LibraryImportAliasFixerTest {

    @Test
    public void aliasesAreFoundForNewerRobotVersion() throws Exception {
        final RobotFile modelFile = getModelFile("LibraryImportWithAlias.robot", new RobotVersion(3, 1));

        assertThat(modelFile.getSettingTable().isPresent()).isTrue();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<LibraryImport> imports = modelFile.getSettingTable().getLibraryImports();
        assertThat(imports).hasSize(8);

        assertImportWithoutAlias(imports.get(0));
        assertImportWithAlias(imports.get(1));
        assertImportWithAlias(imports.get(2));
        assertImportWithAlias(imports.get(3));
        assertImportWithoutAlias(imports.get(4));
        assertImportWithoutAlias(imports.get(5));
        assertImportWithAlias(imports.get(6));
        assertImportWithoutAlias(imports.get(7));
    }

    @Test
    public void aliasesAreFoundForOlderRobotVersion() throws Exception {
        final RobotFile modelFile = getModelFile("LibraryImportWithAlias.robot", new RobotVersion(3, 0));

        assertThat(modelFile.getSettingTable().isPresent()).isTrue();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getTasksTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isFalse();

        final List<LibraryImport> imports = modelFile.getSettingTable().getLibraryImports();
        assertThat(imports).hasSize(8);

        assertImportWithoutAlias(imports.get(0));
        assertImportWithAlias(imports.get(1));
        assertImportWithAlias(imports.get(2));
        assertImportWithAlias(imports.get(3));
        assertImportWithoutAlias(imports.get(4));
        assertImportWithoutAlias(imports.get(5));
        assertImportWithAlias(imports.get(6));
        assertImportWithAlias(imports.get(7));
    }

    private void assertImportWithAlias(final LibraryImport libraryImport) {
        final List<RobotToken> tokens = libraryImport.getElementTokens();
        assertThat(tokens.get(0).getTypes().contains(RobotTokenType.SETTING_LIBRARY_DECLARATION));
        assertThat(tokens.get(1).getTypes().contains(RobotTokenType.SETTING_LIBRARY_NAME));
        assertThat(tokens.subList(2, tokens.size() - 2))
                .allSatisfy(token -> assertThat(token.getTypes().contains(RobotTokenType.SETTING_LIBRARY_ARGUMENT)));
        assertThat(tokens.get(tokens.size() - 2).getTypes().contains(RobotTokenType.SETTING_LIBRARY_ALIAS));
        assertThat(tokens.get(tokens.size() - 1).getTypes().contains(RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE));
    }

    private void assertImportWithoutAlias(final LibraryImport libraryImport) {
        final List<RobotToken> tokens = libraryImport.getElementTokens();
        tokens.get(0).getTypes().contains(RobotTokenType.SETTING_LIBRARY_DECLARATION);
        tokens.get(1).getTypes().contains(RobotTokenType.SETTING_LIBRARY_NAME);
        assertThat(tokens.subList(2, tokens.size()))
                .allSatisfy(token -> assertThat(token.getTypes().contains(RobotTokenType.SETTING_LIBRARY_ARGUMENT)));
    }

    private RobotFile getModelFile(final String fileName, final RobotVersion robotVersion) throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        final String mainPath = "parser/bugs/";
        final File file = new File(RobotParser.class.getResource(mainPath + fileName).toURI());
        when(projectHolder.shouldBeParsed(file)).thenReturn(true);

        // execute
        final RobotParser parser = new RobotParser(projectHolder, robotVersion);
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        assertThat(robotFileOutput.getType()).isEqualTo(RobotFileType.RESOURCE);
        return robotFileOutput.getFileModel();
    }

}
