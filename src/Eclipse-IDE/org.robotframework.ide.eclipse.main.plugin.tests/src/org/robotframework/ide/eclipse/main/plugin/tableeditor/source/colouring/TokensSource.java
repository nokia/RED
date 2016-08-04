package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Function;

public class TokensSource {

    static List<RobotToken> createTokens() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Documentation]  abc  def  # comment")
                .appendLine("  [Tags]  t1  t2  # comment")
                .appendLine("  [Setup]  tc_setting_call  a1  a2  # comment")
                .appendLine("  [Teardown]  tc_setting_call  a1  a2  #comment")
                .appendLine("  [Timeout]  10  a  b  c  # comment")
                .appendLine("  [UnkownTcSetting]  a  b  c  # comment")
                .appendLine("  [Template]  tc_setting_call  b  c  # comment")
                .appendLine("  call  arg  ${x}  # comment   comment")
                .appendLine("case 2")
                .appendLine("  call  arg  ${x}  # comment   comment")
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
                .appendLine("  ${x}=  call  arg  ${x}  # comment   comment")
                .appendLine("  ${x}  call  arg  ${x}  # comment   comment")
                .appendLine("  ${x}  ${y}=  call  arg  ${x}  # comment   comment")
                .appendLine("  ${x}  ${y}  call  arg  ${x}  # comment   comment")
                .appendLine("  :FOR  ${i}  IN RANGE  10")
                .appendLine("  \\  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${x}=  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${x}  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${x}  ${y}=  call  arg  ${x}  # comment   comment")
                .appendLine("  \\  ${x}  ${y}  call  arg  ${x}  # comment   comment")
                .appendLine("userkw 2")
                .appendLine("  [arguments]  ${b}  # comment")
                .appendLine("  call  arg  ${x}  # comment   comment")
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
                .appendLine("Resource  abc  def  # comment")
                .appendLine("Variables  abc  def  # comment")
                .appendLine("Metadata  abc  def  # comment")
                .appendLine("Force Tags  abc  def  # comment")
                .appendLine("Default Tags  abc  def  # comment")
                .appendLine("Suite Setup  general_setting_call  def  # comment")
                .appendLine("Suite Teardown  general_setting_call  def  # comment")
                .appendLine("Test Setup  general_setting_call  def  # comment")
                .appendLine("Test Teardown  general_setting_call  def  # comment")
                .appendLine("Test Template  general_setting_call  def  # comment")
                .appendLine("Test Timeout  abc  def  # comment")
                .appendLine("UnkownSetting  abc  def  # comment")
                .build();
        final List<RobotLine> lines = model.getLinkedElement().getFileContent();
        final List<RobotToken> tokens = new ArrayList<>();
        for (final RobotLine line : lines) {
            tokens.addAll(newArrayList(filter(line.getLineElements(), RobotToken.class)));
        }
        return tokens;
    }

    static List<RobotLine> lines(final RobotLine... lines) {
        return newArrayList(lines);
    }

    static RobotLine line(final int no, final LineElement... elements) {
        final RobotLine line = new RobotLine(no, null);
        for (final LineElement element : elements) {
            line.addLineElement(element);
        }
        return line;
    }

    static Function<IRobotLineElement, String> toTokenContents() {
        return new Function<IRobotLineElement, String>() {

            @Override
            public String apply(final IRobotLineElement element) {
                return element.getText();
            }
        };
    }

    static class LineElement implements IRobotLineElement {

        private final FilePosition filePostion;

        private final String text;

        public LineElement(final int line, final int column, final int offset, final String content) {
            this.filePostion = new FilePosition(line, column, offset);
            this.text = content;
        }

        @Override
        public int getLineNumber() {
            return filePostion.getLine();
        }

        @Override
        public int getStartColumn() {
            return filePostion.getColumn();
        }

        @Override
        public int getEndColumn() {
            return filePostion.getColumn() + text.length();
        }

        @Override
        public int getStartOffset() {
            return filePostion.getOffset();
        }

        @Override
        public FilePosition getFilePosition() {
            return filePostion;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getRaw() {
            return getText();
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
    }
}
