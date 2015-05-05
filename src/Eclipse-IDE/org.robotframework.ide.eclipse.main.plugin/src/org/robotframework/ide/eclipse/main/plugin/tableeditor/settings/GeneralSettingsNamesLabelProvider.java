package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

class GeneralSettingsNamesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private final Map<String, String> tooltips = new LinkedHashMap<>(); {
        tooltips.put("Suite Setup",
                "The keyword %s is executed before executing any of the test cases or lower level suites");
        tooltips.put("Suite Teardown",
                "The keyword %s is executed after all test cases and lower level suites have been executed");
        tooltips.put("Test Setup",
                "The keyword %s is executed before every test cases in this suite unless test cases override it");
        tooltips.put("Test Teardown",
                "The keyword %s is executed after every test cases in this suite unless test cases override it");
        tooltips.put("Test Template", "The keyword %s is used as default template keyword in this suite");
        tooltips.put("Test Timeout",
                "Specifies default timeout for each test case in this suite, which can be overridden by test case settings.\n"
                        + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put("Force Tags", "Sets tags to all test cases in this suite. Inherited tags are not shown here.");
        tooltips.put("Default Tags", "Sets tags to all tests cases in this suite, unless test case specifies own tags");
    }

    @Override
    public Color getBackground(final Object element) {
        final Entry<String, RobotElement> entry = getEntry(element);

        if (entry.getValue() == null) {
            return new Color(Display.getDefault(), 250, 250, 250);
        } else {
            return null;
        }
    }

    @Override
    public StyledString getStyledText(final Object element) {
        final RobotSetting setting = getSetting(element);
        if (setting == null) {
            return new StyledString(getSettingName(element), Stylers.withForeground(200, 200, 200));
        } else {
            return new StyledString(setting.getName());
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        final Entry<String, RobotElement> entry = getEntry(element);
        final RobotSetting setting = getSetting(element);
        final String keyword = setting == null ? "given in first argument" : getKeyword(setting);
        
        return String.format(tooltips.get(entry.getKey()), keyword);
    }

    private String getKeyword(final RobotSetting element) {
        final List<String> arguments = element.getArguments();
        return arguments.isEmpty() ? "<empty>" : "'" + arguments.get(0) + "'";
    }

    @Override
    public Image getToolTipImage(final Object object) {
        return RobotImages.getTooltipImage().createImage();
    }

    @SuppressWarnings("unchecked")
    private Entry<String, RobotElement> getEntry(final Object element) {
        return (Entry<String, RobotElement>) element;
    }

    private String getSettingName(final Object element) {
        return (String) ((Entry<?, ?>) element).getKey();
    }

    private RobotSetting getSetting(final Object element) {
        return (RobotSetting) ((Entry<?, ?>) element).getValue();
    }
}
