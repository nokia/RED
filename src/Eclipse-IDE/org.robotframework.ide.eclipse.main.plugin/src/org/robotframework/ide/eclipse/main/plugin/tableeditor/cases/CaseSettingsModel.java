package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;

class CaseSettingsModel {

    private final Map<String, RobotElement> settings = new LinkedHashMap<>(); {
        settings.put("Setup", null);
        settings.put("Teardown", null);
        settings.put("Timeout", null);
        settings.put("Template", null);
        settings.put("Tags", null);
    }

    private final RobotSetting documentation = null;

    private RobotCase testCase = null;

    CaseSettingsModel(final RobotCase testCase) {
        this.testCase = testCase;
    }
}
