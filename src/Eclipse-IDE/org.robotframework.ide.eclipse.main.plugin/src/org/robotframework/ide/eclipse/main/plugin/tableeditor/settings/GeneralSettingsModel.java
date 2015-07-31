package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

class GeneralSettingsModel {
    
    static List<RobotElement> findGeneralSettingsList(final RobotSettingsSection section) {
        return newArrayList(Iterables.filter(newArrayList(findGeneralSettingsMapping(section).values()),
                Predicates.notNull()));
    }

    static Map<String, RobotElement> findGeneralSettingsMapping(final RobotSettingsSection section) {
        final Map<String, RobotElement> settings = createMapping();
        if (section != null) {
            for (final RobotKeywordCall setting : section.getChildren()) {
                final String settingName = setting.getName();
                if (settings.containsKey(settingName)) {
                    settings.put(settingName, setting);
                }
            }
        }
        return settings;
    }

    private static Map<String, RobotElement> createMapping() {
        final Map<String, RobotElement> settings = new LinkedHashMap<>();
        settings.put("Suite Setup", null);
        settings.put("Suite Teardown", null);
        settings.put("Test Setup", null);
        settings.put("Test Teardown", null);
        settings.put("Test Template", null);
        settings.put("Test Timeout", null);
        settings.put("Force Tags", null);
        settings.put("Default Tags", null);
        return settings;
    }
}
