/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

public interface RobotSuiteEditorEvents {

    String FORM_FILTER_ENABLED = "red/suite_editor/section/filter/enablement/enabled";
    String FORM_FILTER_DISABLED = "red/suite_editor/section/filter/enablement/disabled";
    String FORM_FILTER_ENABLAMENT_CHANGED = "red/suite_editor/section/filter/enablement/*";

    String FORM_FILTER_SWITCH_REQUEST_TOPIC = "red/suite_editor/section/filter/switch/request";
    String FORM_FILTER_SWITCH_REQUEST = "red/suite_editor/section/filter/switch/request/*";

    String SECTION_FILTERING_ENABLED_TOPIC = "red/suite_editor/section/filter/enabled";
    String SECTION_FILTERING_DISABLED_TOPIC = "red/suite_editor/section/filter/disabled";

}
