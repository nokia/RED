package org.robotframework.ide.eclipse.main.plugin.propertytester;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.viewers.ViewerCell;
import org.robotframework.ide.eclipse.main.plugin.propertytester.TableCellPropertyTester.E4TableCellPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DIPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;

public class TableCellPropertyTester extends DIPropertyTester<E4TableCellPropertyTester> {

    public TableCellPropertyTester() {
        super(E4TableCellPropertyTester.class);
    }

    public static class E4TableCellPropertyTester {

        @PropertyTest
        public Boolean testFocusedCellProperties(@Optional final FocusedViewerAccessor viewerAccessor,
                @Named(DIPropertyTester.PROPERTY) final String propertyName,
                @Named(DIPropertyTester.EXPECTED_VALUE) final Boolean expected) {

            if (viewerAccessor == null) {
                return false;
            }

            if ("thereIsAFocusedCell".equals(propertyName)) {
                return viewerAccessor.getFocusedCell() != null == expected.booleanValue();
            }
            return true;
        }

        @PropertyTest
        public Boolean testFocusedCellProperties(final FocusedViewerAccessor viewerAccessor,
                @Named(DIPropertyTester.PROPERTY) final String propertyName,
                @Named(DIPropertyTester.EXPECTED_VALUE) final Integer expected) {

            if ("focusedCellHasIndex".equals(propertyName)) {
                final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
                return focusedCell != null && focusedCell.getColumnIndex() == expected.intValue();
            }
            return true;
        }
    }
}
