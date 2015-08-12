package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

class KeywordSettingsModel {

    static List<RobotElement> findKeywordSettingsList(final RobotKeywordDefinition keywordDefinition) {
        return newArrayList(Iterables.filter(newArrayList(findKeywordSettingsMapping(keywordDefinition).values()),
                Predicates.notNull()));
    }

    static Map<String, RobotElement> findKeywordSettingsMapping(final RobotKeywordDefinition def) {
        final Map<String, RobotElement> settings = new LinkedHashMap<>();
        settings.put(RobotKeywordDefinition.TIMEOUT, def == null ? null : def.getTimeoutSetting());
        settings.put(RobotKeywordDefinition.TEARDOWN, def == null ? null : def.getTeardownSetting());
        settings.put(RobotKeywordDefinition.RETURN, def == null ? null : def.getReturnValueSetting());
        return settings;
    }
}
