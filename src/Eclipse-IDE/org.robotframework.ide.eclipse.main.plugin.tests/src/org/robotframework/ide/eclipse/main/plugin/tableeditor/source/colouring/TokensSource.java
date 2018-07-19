/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class TokensSource {

    static List<RobotToken> createTokens() {
        final List<RobotLine> lines = createTokensInLines();
        final List<RobotToken> tokens = new ArrayList<>();
        for (final RobotLine line : lines) {
            tokens.addAll(newArrayList(filter(line.getLineElements(), RobotToken.class)));
        }
        return tokens;
    }

    static List<RobotLine> createTokensInLines() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Documentation]  abc  def  # comment")
                .appendLine("  [Tags]  t1  t2  # comment")
                .appendLine("  [Setup]  tc_setting_call  a1  a2  # comment")
                .appendLine("  [Teardown]  tc_setting_call  a1  a2  #comment")
                .appendLine("  [Timeout]  10  a  b  c  # comment")
                .appendLine("  [UnkownTcSetting]  a  b  c  # comment")
                .appendLine("  [Template]  tc_setting_template  b  c  # comment")
                .appendLine("  call  arg  ${x}  # comment   comment")
                .appendLine("case 2")
                .appendLine("  call  arg  ${x}  # comment   comment")
                .appendLine("  given gherkin_call  # comment")
                .appendLine("  when then gherkin_call  # comment")
                .appendLine("  ${var_asgn}=  given gherkin_call  # comment")
                .appendLine("  call  given arg  # comment")
                .appendLine("*Keywords")
                .appendLine("userkw 1")
                .appendLine("  [arguments]  ${a}  # comment")
                .appendLine("  [documentation]  abc  def  # comment")
                .appendLine("  [tags]  t1  t2  # comment")
                .appendLine("  [teardown]  kw_setting_call  a1  a2  #comment")
                .appendLine("  [timeout]  10  a  b  c  # comment")
                .appendLine("  [unkownKwSetting]  a  b  c  # comment")
                .appendLine("  [return]  a  b  c  # comment")
                .appendLine("  call  arg  ${x}  # comment   comment")
                .appendLine("  ${var_asgn}=  call  arg  ${x}  # comment   comment")
                .appendLine("  ${var_asgn}  call  arg  ${x}  # comment   comment")
                .appendLine("  ${var_asgn_1}  ${var_asgn_2}=  call  arg  ${x}  # comment   comment")
                .appendLine("  ${var_asgn_1}  ${var_asgn_2}  call  arg  ${x}  # comment   comment")
                .appendLine("  :FOR  ${i}  IN RANGE  10")
                .appendLine("  \\  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${var_asgn}=  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${var_asgn}  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${var_asgn_1}  ${var_asgn_2}=  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${var_asgn_1}  ${var_asgn_2}  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  given gherkin_call  # comment")
                .appendLine("  \\  when then gherkin_call  # comment")
                .appendLine("  \\  ${var_asgn}=  given gherkin_call  # comment")
                .appendLine("  \\  call  given arg  # comment")
                .appendLine("userkw 2")
                .appendLine("  [arguments]  ${b}  # comment")
                .appendLine("  call  arg  ${x}  # comment   comment")
                .appendLine("  given gherkin_call  # comment")
                .appendLine("  when then gherkin_call  # comment")
                .appendLine("  ${var_asgn}=  given gherkin_call  # comment")
                .appendLine("  call  given arg  # comment")
                .appendLine("* * * Variables * * *")
                .appendLine("${var_def_1}  1  # comment")
                .appendLine("${var_def_2}  1  2  3  # comment")
                .appendLine("@{var_def_3}  1  2  3  # comment")
                .appendLine("&{var_def_4}  k1=v1  k2=v2  # comment")
                .appendLine("{var_def_5}  1  2  3  # comment")
                .appendLine("*** unknown section ***")
                .appendLine("some stuff")
                .appendLine("*** Settings ***")
                .appendLine("Documentation  abc  def  # comment")
                .appendLine("Library  abc  def  # comment")
                .appendLine("Library  abc  WITH NAME  def  # comment")
                .appendLine("Resource  abc  def  # comment")
                .appendLine("Variables  abc  def  # comment")
                .appendLine("Metadata  abc  def  # comment")
                .appendLine("Force Tags  abc  def  # comment")
                .appendLine("Default Tags  abc  def  # comment")
                .appendLine("Suite Setup  general_setting_call  def  # comment")
                .appendLine("Suite Teardown  general_setting_call  def  # comment")
                .appendLine("Test Setup  general_setting_call  def  # comment")
                .appendLine("Test Teardown  general_setting_call  def  # comment")
                .appendLine("Test Template  general_setting_template  def  # comment")
                .appendLine("Test Timeout  abc  def  # comment")
                .appendLine("UnkownSetting  abc  def  # comment")
                .build();
        return model.getLinkedElement().getFileContent();
    }

    static List<RobotLine> lines(final RobotLine... lines) {
        return newArrayList(lines);
    }

    static RobotLine line(final int no, final LineElement... elements) {
        final RobotLine line = new RobotLine(no, null);
        final int firstOffset = elements[0].getStartOffset();
        int currentColumn = 0;
        for (final LineElement element : elements) {
            line.addLineElement(element);

            currentColumn += element.getText().length();
        }
        line.setEndOfLine(newArrayList(Constant.LF), firstOffset + currentColumn, currentColumn);
        return line;
    }

    static class LineElement implements IRobotLineElement {

        private final FilePosition filePosition;

        private final String text;

        public LineElement(final int line, final int column, final int offset, final String content) {
            this.filePosition = new FilePosition(line, column, offset);
            this.text = content;
        }

        @Override
        public int getLineNumber() {
            return filePosition.getLine();
        }

        @Override
        public int getStartColumn() {
            return filePosition.getColumn();
        }

        @Override
        public int getEndColumn() {
            return filePosition.getColumn() + text.length();
        }

        @Override
        public int getStartOffset() {
            return filePosition.getOffset();
        }

        @Override
        public FilePosition getFilePosition() {
            return filePosition;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public List<IRobotTokenType> getTypes() {
            return new ArrayList<>();
        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public VersionAvailabilityInfo getVersionInformation() {
            return VersionAvailabilityInfoBuilder.create().build();
        }

        @Override
        public LineElement copyWithoutPosition() {
            return copy(false);
        }

        @Override
        public LineElement copy() {
            return copy(true);
        }

        private LineElement copy(final boolean posInclude) {
            final FilePosition fp;
            if (posInclude) {
                fp = this.getFilePosition();
            } else {
                fp = FilePosition.createNotSet();
            }

            return new LineElement(fp.getLine(), fp.getColumn(), fp.getOffset(), this.getText());
        }
    }
}
