/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;

/**
 * @author wypych
 */
public class DocumentationCacherInsideRobotFileOutputTest {

    private static RobotFileOutput out;

    private static SettingTable settingTable;

    private static KeywordTable keywordTable;

    private static TestCaseTable testCaseTable;

    @Test
    public void test_checkForOffsetLessThanFileLength() {
        // given
        final long offset = -1;

        // when
        final Optional<IDocumentationHolder> docFound = out.findDocumentationForOffset((int) offset);

        // then
        assertThat(docFound.isPresent()).isFalse();
    }

    @Test
    public void test_checkForOffsetOutsideFileLength() {
        // given
        final long offset = out.getProcessedFile().length() + 10;

        // when
        final Optional<IDocumentationHolder> docFound = out.findDocumentationForOffset((int) offset);

        // then
        assertThat(docFound.isPresent()).isFalse();
    }

    @Test
    public void test_assertCacherContainsOnlyWhatIsExpected() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Set<IRegionCacheable<IDocumentationHolder>> cache = out.getDocumentationCacher()
                .getUnmodificableCacheContent();

        // then
        assertThat(cache).containsOnlyElementsOf(Arrays.asList(suiteDocumentation, keyKW1Documentation,
                keyKW2Documentation, testTC1Documentation, testTC2Documentation));
    }

    @Test
    public void test_givenTestCaseTable_and_positionAtMiddleOfDocumentation_shouldReturn_documentation() {
        // given
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docTC1Found = out.findDocumentationForOffset(middleOffset(testTC1Documentation));
        final Optional<IDocumentationHolder> docTC2Found = out.findDocumentationForOffset(middleOffset(testTC2Documentation));

        // then
        assertThat(docTC1Found.isPresent()).isTrue();
        assertThat(testTC1Documentation).isSameAs(docTC1Found.get());
        assertThat(docTC2Found.isPresent()).isTrue();
        assertThat(testTC2Documentation).isSameAs(docTC2Found.get());
    }

    @Test
    public void test_givenTestCaseTable_and_positionAtOneByteAfterEndOfDocumentation_shouldReturn_absent() {
        // given
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docTC1Found = out
                .findDocumentationForOffset(testTC1Documentation.getEndPosition().getOffset() + 1);
        final Optional<IDocumentationHolder> docTC2Found = out
                .findDocumentationForOffset(testTC2Documentation.getEndPosition().getOffset() + 1);

        // then
        assertThat(docTC1Found.isPresent()).isFalse();
        assertThat(docTC2Found.isPresent()).isFalse();
    }

    @Test
    public void test_givenTestCaseTable_and_positionAtEndOfDocumentation_shouldReturn_documentation() {
        // given
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docTC1Found = out
                .findDocumentationForOffset(testTC1Documentation.getEndPosition().getOffset());
        final Optional<IDocumentationHolder> docTC2Found = out
                .findDocumentationForOffset(testTC2Documentation.getEndPosition().getOffset());

        // then
        assertThat(docTC1Found.isPresent()).isTrue();
        assertThat(testTC1Documentation).isSameAs(docTC1Found.get());
        assertThat(docTC2Found.isPresent()).isTrue();
        assertThat(testTC2Documentation).isSameAs(docTC2Found.get());
    }

    @Test
    public void test_givenTestCaseTable_and_positionAtBeginOfDocumentation_shouldReturn_documentation() {
        // given
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docTC1Found = out
                .findDocumentationForOffset(testTC1Documentation.getBeginPosition().getOffset());
        final Optional<IDocumentationHolder> docTC2Found = out
                .findDocumentationForOffset(testTC2Documentation.getBeginPosition().getOffset());

        // then
        assertThat(docTC1Found.isPresent()).isTrue();
        assertThat(testTC1Documentation).isSameAs(docTC1Found.get());
        assertThat(docTC2Found.isPresent()).isTrue();
        assertThat(testTC2Documentation).isSameAs(docTC2Found.get());
    }

    @Test
    public void test_givenTestCaseTable_and_positionAtOneByteBeforeBeginOfDocumentation_shouldReturn_absent() {
        // given
        final TestDocumentation testTC1Documentation = testCaseTable.getTestCases().get(0).getDocumentation().get(0);
        final TestDocumentation testTC2Documentation = testCaseTable.getTestCases().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docTC1Found = out
                .findDocumentationForOffset(testTC1Documentation.getBeginPosition().getOffset() - 1);
        final Optional<IDocumentationHolder> docTC2Found = out
                .findDocumentationForOffset(testTC2Documentation.getBeginPosition().getOffset() - 1);

        // then
        assertThat(docTC1Found.isPresent()).isFalse();
        assertThat(docTC2Found.isPresent()).isFalse();
    }

    @Test
    public void test_givenKeywordTable_and_positionAtMiddleOfDocumentation_shouldReturn_documentation() {
        // given
        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docKW1Found = out.findDocumentationForOffset(middleOffset(keyKW1Documentation));
        final Optional<IDocumentationHolder> docKW2Found = out.findDocumentationForOffset(middleOffset(keyKW2Documentation));

        // then
        assertThat(docKW1Found.isPresent()).isTrue();
        assertThat(keyKW1Documentation).isSameAs(docKW1Found.get());
        assertThat(docKW2Found.isPresent()).isTrue();
        assertThat(keyKW2Documentation).isSameAs(docKW2Found.get());
    }

    @Test
    public void test_givenKeywordTable_and_positionAtOneByteAfterEndOfDocumentation_shouldReturn_absent() {
        // given
        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docKW1Found = out
                .findDocumentationForOffset(keyKW1Documentation.getEndPosition().getOffset() + 1);
        final Optional<IDocumentationHolder> docKW2Found = out
                .findDocumentationForOffset(keyKW2Documentation.getEndPosition().getOffset() + 1);

        // then
        assertThat(docKW1Found.isPresent()).isFalse();
        assertThat(docKW2Found.isPresent()).isFalse();
    }

    @Test
    public void test_givenKeywordTable_and_positionAtEndOfDocumentation_shouldReturn_documentation() {
        // given
        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docKW1Found = out
                .findDocumentationForOffset(keyKW1Documentation.getEndPosition().getOffset());
        final Optional<IDocumentationHolder> docKW2Found = out
                .findDocumentationForOffset(keyKW2Documentation.getEndPosition().getOffset());

        // then
        assertThat(docKW1Found.isPresent()).isTrue();
        assertThat(keyKW1Documentation).isSameAs(docKW1Found.get());
        assertThat(docKW2Found.isPresent()).isTrue();
        assertThat(keyKW2Documentation).isSameAs(docKW2Found.get());
    }

    @Test
    public void test_givenKeywordTable_and_positionAtBeginOfDocumentation_shouldReturn_documentation() {
        // given
        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docKW1Found = out
                .findDocumentationForOffset(keyKW1Documentation.getBeginPosition().getOffset());
        final Optional<IDocumentationHolder> docKW2Found = out
                .findDocumentationForOffset(keyKW2Documentation.getBeginPosition().getOffset());

        // then
        assertThat(docKW1Found.isPresent()).isTrue();
        assertThat(keyKW1Documentation).isSameAs(docKW1Found.get());
        assertThat(docKW2Found.isPresent()).isTrue();
        assertThat(keyKW2Documentation).isSameAs(docKW2Found.get());
    }

    @Test
    public void test_givenKeywordTable_and_positionAtOneByteBeforeBeginOfDocumentation_shouldReturn_absent() {
        // given
        final KeywordDocumentation keyKW1Documentation = keywordTable.getKeywords().get(0).getDocumentation().get(0);
        final KeywordDocumentation keyKW2Documentation = keywordTable.getKeywords().get(1).getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docKW1Found = out
                .findDocumentationForOffset(keyKW1Documentation.getBeginPosition().getOffset() - 1);
        final Optional<IDocumentationHolder> docKW2Found = out
                .findDocumentationForOffset(keyKW2Documentation.getBeginPosition().getOffset() - 1);

        // then
        assertThat(docKW1Found.isPresent()).isFalse();
        assertThat(docKW2Found.isPresent()).isFalse();
    }

    @Test
    public void test_givenSettingTable_and_positionAtMiddleOfDocumentation_shouldReturn_documentation() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docFound = out.findDocumentationForOffset(middleOffset(suiteDocumentation));

        // then
        assertThat(docFound.isPresent()).isTrue();
        assertThat(suiteDocumentation).isSameAs(docFound.get());
    }

    @Test
    public void test_givenSettingTable_and_positionAtOneByteAfterEndOfDocumentation_shouldReturn_absent() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docFound = out
                .findDocumentationForOffset(suiteDocumentation.getEndPosition().getOffset() + 1);

        // then
        assertThat(docFound.isPresent()).isFalse();
    }

    @Test
    public void test_givenSettingTable_and_positionAtEndOfDocumentation_shouldReturn_documentation() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docFound = out
                .findDocumentationForOffset(suiteDocumentation.getEndPosition().getOffset());

        // then
        assertThat(docFound.isPresent()).isTrue();
        assertThat(suiteDocumentation).isSameAs(docFound.get());
    }

    @Test
    public void test_givenSettingTable_and_positionAtBeginOfDocumentation_shouldReturn_documentation() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docFound = out
                .findDocumentationForOffset(suiteDocumentation.getBeginPosition().getOffset());

        // then
        assertThat(docFound.isPresent()).isTrue();
        assertThat(suiteDocumentation).isSameAs(docFound.get());
    }

    @Test
    public void test_givenSettingTable_and_positionAtOneByteBeforeBeginOfDocumentation_shouldReturn_absent() {
        // given
        final SuiteDocumentation suiteDocumentation = settingTable.getDocumentation().get(0);

        // when
        final Optional<IDocumentationHolder> docFound = out
                .findDocumentationForOffset(suiteDocumentation.getBeginPosition().getOffset() - 1);

        // then
        assertThat(docFound.isPresent()).isFalse();
    }

    @BeforeClass
    public static void setup() throws Exception {
        final RobotProjectHolder projectHolder = new RobotProjectHolder(
                new RobotRuntimeEnvironment(null, null, "3.0.0"));
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(getFile("presenter//DocPositionsFind.robot"),
                RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy()));
        out = modelFile.getParent();
        assertThat(out).isNotNull();

        settingTable = modelFile.getSettingTable();
        keywordTable = modelFile.getKeywordTable();
        testCaseTable = modelFile.getTestCaseTable();

        assertThat(settingTable.isPresent());
        assertThat(keywordTable.isPresent());
        assertThat(testCaseTable.isPresent());
    }

    private int middleOffset(final IDocumentationHolder holder) {
        final int startOffset = holder.getBeginPosition().getOffset();
        final int endOffset = holder.getContinuousRegions()
                .get(holder.getContinuousRegions().size() - 1)
                .getEnd()
                .getOffset();

        return startOffset + ((endOffset - startOffset) % 2);
    }

    private static Path getFile(final String path) throws URISyntaxException {
        final URL resource = DocumentationCacherInsideRobotFileOutputTest.class.getResource(path);
        return Paths.get(resource.toURI());
    }
}
