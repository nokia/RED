package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;

import com.google.common.base.Optional;

class GeneralSettingsModel {

    private final Map<String, RobotElement> settings = new LinkedHashMap<>(); {
        settings.put("Suite Setup", null);
        settings.put("Suite Teardown", null);
        settings.put("Test Setup", null);
        settings.put("Test Teardown", null);
        settings.put("Test Template", null);
        settings.put("Test Timeout", null);
        settings.put("Force Tags", null);
        settings.put("Default Tags", null);
    }

    public GeneralSettingsModel() {
        System.err.println(1);
    }

    private RobotSuiteSettingsSection settingsSection = null;

    private RobotSetting documentation = null;
    
    RobotSuiteSettingsSection getSection() {
        return settingsSection;
    }

    boolean areSettingsExist() {
        return settingsSection != null;
    }

    void update(final Optional<RobotElement> settingsSection) {
        this.settingsSection = null;
        this.documentation = null;
        for (final String key : settings.keySet()) {
            settings.put(key, null);
        }

        if (settingsSection.isPresent()) {
            this.settingsSection = (RobotSuiteSettingsSection) settingsSection.get();
            for (final RobotElement setting : settingsSection.get().getChildren()) {
                final String settingName = setting.getName();
                if (settings.containsKey(settingName)) {
                    settings.put(settingName, setting);
                } else if ("Documentation".equals(settingName)) {
                    documentation = (RobotSetting) setting;
                }
            }
        }
    }

    RobotSetting getDocumentationSetting() {
        return documentation;
    }

    String getDocumentation() {
        return documentation != null && !documentation.getArguments().isEmpty() ? documentation.getArguments().get(0)
                : "";
    }

    Set<Entry<String, RobotElement>> getEntries() {
        return settings.entrySet();
    }

    boolean contains(final RobotSetting setting) {
        return settings.values().contains(setting);
    }
}
