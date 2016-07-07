package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;

public class RedXmlValidationPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RedXmlValidationPropertyTester tester = new RedXmlValidationPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotProjectTreeElement() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + ProjectTreeElement.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(mock(ProjectTreeElement.class),
                RedXmlValidationPropertyTester.IS_EXCLUDED, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(mock(ProjectTreeElement.class), "unknown_property", null, true)).isFalse();
        assertThat(tester.test(mock(ProjectTreeElement.class), "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testIsIncludedProperty() {
        final ProjectTreeElement includedElement = new ProjectTreeElement(null, false);
        final ProjectTreeElement notIncludedElement = new ProjectTreeElement(null, true);

        assertThat(isIncluded(includedElement, true)).isTrue();
        assertThat(isIncluded(includedElement, false)).isFalse();

        assertThat(isIncluded(notIncludedElement, true)).isFalse();
        assertThat(isIncluded(notIncludedElement, false)).isTrue();
    }

    @Test
    public void testIsExcludedProperty() {
        final ProjectTreeElement excludedElement = new ProjectTreeElement(null, true);
        final ProjectTreeElement notExcludedElement = new ProjectTreeElement(null, false);

        assertThat(isExcluded(excludedElement, true)).isTrue();
        assertThat(isExcluded(excludedElement, false)).isFalse();

        assertThat(isExcluded(notExcludedElement, true)).isFalse();
        assertThat(isExcluded(notExcludedElement, false)).isTrue();
    }

    @Test
    public void testIsInternalFolderProperty() {
        final ProjectTreeElement internalFolder = mock(ProjectTreeElement.class);
        final ProjectTreeElement nonInternalFolder = mock(ProjectTreeElement.class);
        when(internalFolder.isInternalFolder()).thenReturn(true);
        when(nonInternalFolder.isInternalFolder()).thenReturn(false);

        assertThat(isInternalFolder(internalFolder, true)).isTrue();
        assertThat(isInternalFolder(internalFolder, false)).isFalse();

        assertThat(isInternalFolder(nonInternalFolder, true)).isFalse();
        assertThat(isInternalFolder(nonInternalFolder, false)).isTrue();
    }

    private boolean isIncluded(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_INCLUDED, null, expected);
    }

    private boolean isExcluded(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_EXCLUDED, null, expected);
    }

    private boolean isInternalFolder(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_INTERNAL_FOLDER, null, expected);
    }
}
