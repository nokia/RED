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
import java.util.Map.Entry;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class GeneralSettingsModel {

    private static final String DEFAULT_TAGS = "Default Tags";
    private static final String FORCE_TAGS = "Force Tags";
    private static final String TEST_TIMEOUT = "Test Timeout";
    private static final String TEST_TEMPLATE = "Test Template";
    private static final String TEST_TEARDOWN = "Test Teardown";
    private static final String TEST_SETUP = "Test Setup";
    private static final String SUITE_TEARDOWN = "Suite Teardown";
    private static final String SUITE_SETUP = "Suite Setup";
    
    public static List<RobotElement> findGeneralSettingsList(final RobotSettingsSection section) {
        if (section == null) {
            return newArrayList();
        }
        return newArrayList(
                Iterables.filter(newArrayList(fillSettingsMapping(section).values()),
                Predicates.notNull()));
    }

    public static Map<String, RobotElement> fillSettingsMapping(final RobotSettingsSection section) {
        final Map<String, RobotElement> initialMapping = AccessibleSettings.forFile(section.getSuiteFile())
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

    static boolean isKeywordBased(final Entry<String, RobotElement> entry) {
        return newArrayList(SUITE_SETUP, SUITE_TEARDOWN, TEST_SETUP, TEST_TEARDOWN).contains(entry.getKey());
    }

    static boolean isTemplate(final Entry<String, RobotElement> entry) {
        return TEST_TEMPLATE.equals(entry.getKey());
    }

    static enum AccessibleSettings {
        OF_INIT_FILE {
            @Override
            Map<String, RobotElement> createInitialMapping() {
                final Map<String, RobotElement> settings = new LinkedHashMap<>();
                settings.put(SUITE_SETUP, null);
                settings.put(SUITE_TEARDOWN, null);
                settings.put(TEST_SETUP, null);
                settings.put(TEST_TEARDOWN, null);
                // there are no templates in __init__ files
                settings.put(TEST_TIMEOUT, null);
                settings.put(FORCE_TAGS, null);
                // there are no default tags in __init__ files
                return settings;
            }
        },
        OF_SUITE_FILE {
            @Override
            Map<String, RobotElement> createInitialMapping() {
                final Map<String, RobotElement> settings = new LinkedHashMap<>();
                settings.put(SUITE_SETUP, null);
                settings.put(SUITE_TEARDOWN, null);
                settings.put(TEST_SETUP, null);
                settings.put(TEST_TEARDOWN, null);
                settings.put(TEST_TEMPLATE, null);
                settings.put(TEST_TIMEOUT, null);
                settings.put(FORCE_TAGS, null);
                settings.put(DEFAULT_TAGS, null);
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

        static AccessibleSettings forFile(final RobotSuiteFile suiteModel) {
            if (suiteModel.isInitializationFile()) {
                return OF_INIT_FILE;
            } else if (suiteModel.isSuiteFile()) {
                return OF_SUITE_FILE;
            } else {
                return OF_RESOURCE_FILE;
            }
        }
    }
}
