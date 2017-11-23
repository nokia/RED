package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;

public class SelectionsPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final SelectionsPropertyTester tester = new SelectionsPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotStructuredSelection() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + IStructuredSelection.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(StructuredSelection.EMPTY,
                SelectionsPropertyTester.ALL_ELEMENTS_HAVE_SAME_TYPE, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(StructuredSelection.EMPTY, "unknown_property", null, true)).isFalse();
        assertThat(tester.test(StructuredSelection.EMPTY, "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testAllElementsHaveSameTypeProperty() {
        final IStructuredSelection selectionWithSameTypeElements_1 = new StructuredSelection(
                new Object[] { "abc", "def", "ghi" });
        final IStructuredSelection selectionWithSameTypeElements_2 = new StructuredSelection(
                new Object[] { new B(), new A(), new B() });
        final IStructuredSelection selectionWithDifferentTypeElements_1 = new StructuredSelection(
                new Object[] { "abc", 10, new Object() });
        final IStructuredSelection selectionWithDifferentTypeElements_2 = new StructuredSelection(
                new Object[] { new B(), new A(), new Object() });

        assertThat(allElementsHaveSameType(StructuredSelection.EMPTY, true)).isTrue();
        assertThat(allElementsHaveSameType(StructuredSelection.EMPTY, false)).isFalse();

        assertThat(allElementsHaveSameType(selectionWithSameTypeElements_1, true)).isTrue();
        assertThat(allElementsHaveSameType(selectionWithSameTypeElements_1, false)).isFalse();

        assertThat(allElementsHaveSameType(selectionWithSameTypeElements_2, true)).isTrue();
        assertThat(allElementsHaveSameType(selectionWithSameTypeElements_2, false)).isFalse();

        assertThat(allElementsHaveSameType(selectionWithDifferentTypeElements_1, true)).isFalse();
        assertThat(allElementsHaveSameType(selectionWithDifferentTypeElements_1, false)).isTrue();

        assertThat(allElementsHaveSameType(selectionWithDifferentTypeElements_2, true)).isFalse();
        assertThat(allElementsHaveSameType(selectionWithDifferentTypeElements_2, false)).isTrue();
    }

    private boolean allElementsHaveSameType(final IStructuredSelection selection, final boolean expected) {
        return tester.test(selection, SelectionsPropertyTester.ALL_ELEMENTS_HAVE_SAME_TYPE, null, expected);
    }

    @Test
    public void testSelectedActualFileProperty() {
        final RobotFileInternalElement element = mock(RobotFileInternalElement.class);
        final RobotFileInternalElement elementFromHistory = mock(RobotFileInternalElement.class);
        final RobotSuiteStreamFile historyFile = mock(RobotSuiteStreamFile.class);
        when(elementFromHistory.getSuiteFile()).thenReturn(historyFile);
        final IStructuredSelection selectionWithoutRFIE = new StructuredSelection(
                new Object[] { "sth" });
        final IStructuredSelection selectionWithRFIE = new StructuredSelection(
                new Object[] { element });
        final IStructuredSelection selectionWithHistoryRFIE = new StructuredSelection(
                new Object[] { elementFromHistory });

        assertThat(selectedActualFile(StructuredSelection.EMPTY, true)).isFalse();
        assertThat(selectedActualFile(StructuredSelection.EMPTY, false)).isTrue();

        assertThat(selectedActualFile(selectionWithoutRFIE, true)).isFalse();
        assertThat(selectedActualFile(selectionWithoutRFIE, false)).isTrue();

        assertThat(selectedActualFile(selectionWithRFIE, true)).isTrue();
        assertThat(selectedActualFile(selectionWithRFIE, false)).isFalse();

        assertThat(selectedActualFile(selectionWithHistoryRFIE, true)).isFalse();
        assertThat(selectedActualFile(selectionWithHistoryRFIE, false)).isTrue();
    }

    private boolean selectedActualFile(final IStructuredSelection selection, final boolean expected) {
        return tester.test(selection, SelectionsPropertyTester.SELECTED_ACTUAL_FILE, null, expected);
    }

    @Test
    public void testIsMetadataSelectedProperty() {
        final RobotSetting nonMetaSetting = mock(RobotSetting.class);
        final RobotSetting metadataSetting = mock(RobotSetting.class);
        when(nonMetaSetting.getGroup()).thenReturn(RobotSetting.SettingsGroup.NO_GROUP);
        when(metadataSetting.getGroup()).thenReturn(RobotSetting.SettingsGroup.METADATA);
        final IStructuredSelection selectionWithNonSetting = new StructuredSelection(
                new Object[] { new Object() });
        final IStructuredSelection selectionWithNonMetaSetting = new StructuredSelection(
                new Object[] { nonMetaSetting });
        final IStructuredSelection selectionWithMetadata = new StructuredSelection(
                new Object[] { metadataSetting });

        assertThat(isMetadataSelected(StructuredSelection.EMPTY, true)).isFalse();
        assertThat(isMetadataSelected(StructuredSelection.EMPTY, false)).isTrue();

        assertThat(isMetadataSelected(selectionWithNonSetting, true)).isFalse();
        assertThat(isMetadataSelected(selectionWithNonSetting, false)).isTrue();

        assertThat(isMetadataSelected(selectionWithNonMetaSetting, true)).isFalse();
        assertThat(isMetadataSelected(selectionWithNonMetaSetting, false)).isTrue();

        assertThat(isMetadataSelected(selectionWithMetadata, true)).isTrue();
        assertThat(isMetadataSelected(selectionWithMetadata, false)).isFalse();
    }

    private boolean isMetadataSelected(final IStructuredSelection selection, final boolean expected) {
        return tester.test(selection, SelectionsPropertyTester.METADATA_SELECTED, null, expected);
    }

    @Test
    public void testIsKeywordCallButNotDocumentationProperty() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new KeywordArguments(new RobotToken()));
        final AModelElement<?> testDocElement = new TestDocumentation(new RobotToken());
        final AModelElement<?> keywordDocElement = new KeywordDocumentation(new RobotToken());
        final RobotKeywordCall testDocumentation = new RobotKeywordCall(null, testDocElement);
        final RobotKeywordCall keywordDocumentation = new RobotKeywordCall(null, keywordDocElement);
        final IStructuredSelection selectionWithCall = new StructuredSelection(
                new Object[] { call });
        final IStructuredSelection selectionWithTestDoc = new StructuredSelection(
                new Object[] { testDocumentation });
        final IStructuredSelection selectionWithKeywordDoc = new StructuredSelection(
                new Object[] { keywordDocumentation });

        assertThat(isKeywordCallButNotDocumentation(StructuredSelection.EMPTY, true)).isFalse();
        assertThat(isKeywordCallButNotDocumentation(StructuredSelection.EMPTY, false)).isTrue();

        assertThat(isKeywordCallButNotDocumentation(selectionWithCall, true)).isFalse();
        assertThat(isKeywordCallButNotDocumentation(selectionWithCall, false)).isTrue();

        assertThat(isKeywordCallButNotDocumentation(selectionWithTestDoc, true)).isFalse();
        assertThat(isKeywordCallButNotDocumentation(selectionWithTestDoc, false)).isTrue();

        assertThat(isKeywordCallButNotDocumentation(selectionWithKeywordDoc, true)).isFalse();
        assertThat(isKeywordCallButNotDocumentation(selectionWithKeywordDoc, false)).isTrue();
    }

    private boolean isKeywordCallButNotDocumentation(final IStructuredSelection selection, final boolean expected) {
        return tester.test(selection, SelectionsPropertyTester.METADATA_SELECTED, null, expected);
    }

    private static class A {
    }

    private static class B extends A {
    }
}
