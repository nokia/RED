package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

public class SettingsSectionSorter extends ViewerSorter {

    public SettingsSectionSorter() {
        // nothing to do
    }

    public SettingsSectionSorter(final Collator collator) {
        super(collator);
    }

    @Override
    public int category(final Object element) {
        if (element instanceof ArtificialGroupingRobotElement) {
            final ArtificialGroupingRobotElement el = (ArtificialGroupingRobotElement) element;
            final SettingsGroup group = el.getGroup();
            return SettingsGroup.getImportsGroupsSet().contains(group) ? 2 : 1;
        }
        return 0;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        return category(e1) - category(e2);
    }
}
