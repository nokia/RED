package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    
    private static class A {}
    private static class B extends A {}
}
