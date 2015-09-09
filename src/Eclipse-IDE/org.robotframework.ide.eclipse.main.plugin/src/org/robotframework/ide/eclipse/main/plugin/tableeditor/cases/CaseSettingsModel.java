/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

class CaseSettingsModel {

    static List<RobotElement> buildCaseSettingsList(final RobotCase robotCase) {
        return newArrayList(
                Iterables.filter(newArrayList(buildCaseSettingsMapping(robotCase).values()), Predicates.notNull()));
    }

    static Map<String, RobotElement> buildCaseSettingsMapping(final RobotCase robotCase) {
        final Map<String, RobotElement> settings = new LinkedHashMap<>();
        settings.put(RobotCase.TAGS, robotCase == null ? null : robotCase.getTagsSetting());
        settings.put(RobotCase.SETUP, robotCase == null ? null : robotCase.getSetupSetting());
        settings.put(RobotCase.TEMPLATE, robotCase == null ? null : robotCase.getTemplateSetting());
        settings.put(RobotCase.TIMEOUT, robotCase == null ? null : robotCase.getTimeoutSetting());
        settings.put(RobotCase.TEARDOWN, robotCase == null ? null : robotCase.getTeardownSetting());
        return settings;
    }
}
