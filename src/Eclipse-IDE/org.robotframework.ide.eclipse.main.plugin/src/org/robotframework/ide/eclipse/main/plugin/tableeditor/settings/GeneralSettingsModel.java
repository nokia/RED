/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class GeneralSettingsModel {
    
    public static List<RobotElement> findGeneralSettingsList(final RobotSettingsSection section) {
        if (section == null) {
            return newArrayList();
        }
        return newArrayList(
                Iterables.filter(newArrayList(fillSettingsMapping(section).values()),
                Predicates.notNull()));
    }

    public static Map<String, RobotElement> fillSettingsMapping(final RobotSettingsSection section) {
        final Map<String, RobotElement> initialMapping = AccessibleSettings.forFile(section.getSuiteFile().getFile())
                .createInitialMapping();

        if (section != null) {
            for (final RobotKeywordCall setting : section.getChildren()) {
                final String settingName = setting.getName();
                if (initialMapping.containsKey(settingName)) {
                    initialMapping.put(settingName, setting);
                }
            }
        }
        return initialMapping;
    }

    static enum AccessibleSettings {
        OF_INIT_FILE {
            @Override
            Map<String, RobotElement> createInitialMapping() {
                final Map<String, RobotElement> settings = new LinkedHashMap<>();
                settings.put("Suite Setup", null);
                settings.put("Suite Teardown", null);
                settings.put("Test Setup", null);
                settings.put("Test Teardown", null);
                // there are no templates in __init__ files
                settings.put("Test Timeout", null);
                settings.put("Force Tags", null);
                // there are no default tags in __init__ files
                return settings;
            }
        },
        OF_SUITE_FILE {
            @Override
            Map<String, RobotElement> createInitialMapping() {
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
        },
        OF_RESOURCE_FILE {
            @Override
            Map<String, RobotElement> createInitialMapping() {
                return new HashMap<>();
            }
        };

        abstract Map<String, RobotElement> createInitialMapping();

        static AccessibleSettings forFile(final IFile file) {
            if (ASuiteFileDescriber.isInitializationFile(file)) {
                return OF_INIT_FILE;
            } else if (ASuiteFileDescriber.isSuiteFile(file)) {
                return OF_SUITE_FILE;
            } else {
                return OF_RESOURCE_FILE;
            }
        }
    }
}
