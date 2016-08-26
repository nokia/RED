package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;

public class CasesTableSortingConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getColumnCount()).thenReturn(5);

        final CasesTableSortingConfiguration configuration = new CasesTableSortingConfiguration(dataProvider);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(NullComparator.class),
                eq(DisplayMode.NORMAL), eq("COLUMN_1"));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(NullComparator.class),
                eq(DisplayMode.NORMAL), eq("COLUMN_2"));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(NullComparator.class),
                eq(DisplayMode.NORMAL), eq("COLUMN_3"));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(NullComparator.class),
                eq(DisplayMode.NORMAL), eq("COLUMN_4"));
        verifyNoMoreInteractions(configRegistry);
    }
}
