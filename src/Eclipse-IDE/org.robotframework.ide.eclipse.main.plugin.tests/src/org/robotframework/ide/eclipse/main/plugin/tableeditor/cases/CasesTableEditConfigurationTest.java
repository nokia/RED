package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.red.nattable.edit.RedTextCellEditor;

public class CasesTableEditConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final CasesTableEditConfiguration configuration = new CasesTableEditConfiguration(
                new RobotSuiteFileCreator().build());
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(RedTextCellEditor.class), eq(DisplayMode.NORMAL),
                eq(CasesElementsLabelAccumulator.VARIABLES_ASSIST_REQUIRED));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(RedTextCellEditor.class), eq(DisplayMode.NORMAL),
                eq(CasesElementsLabelAccumulator.KEYWORD_ASSIST_REQUIRED));
    }
}
