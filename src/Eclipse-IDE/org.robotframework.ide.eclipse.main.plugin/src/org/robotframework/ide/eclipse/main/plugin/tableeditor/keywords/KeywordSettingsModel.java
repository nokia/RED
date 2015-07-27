package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

class KeywordSettingsModel {

    private final Map<String, RobotDefinitionSetting> settings = new LinkedHashMap<>();

    private final RobotDefinitionSetting documentation;

    public KeywordSettingsModel(final RobotKeywordDefinition def) {
        documentation = def == null ? null : def.getDocumentationSetting();
        settings.put(RobotKeywordDefinition.TIMEOUT, def == null ? null : def.getTimeoutSetting());
        settings.put(RobotKeywordDefinition.TEARDOWN, def == null ? null : def.getTeardownSetting());
        settings.put(RobotKeywordDefinition.RETURN, def == null ? null : def.getReturnValueSetting());
    }

    public Collection<Entry<String, RobotDefinitionSetting>> getEntries() {
        return settings.entrySet();
    }

    public String getDocumentation() {
        return documentation == null ? "" : documentation.getArguments().get(0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == KeywordSettingsModel.class) {
            final KeywordSettingsModel that = (KeywordSettingsModel) obj;
            return documentation == that.documentation && settings.equals(that.settings);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentation, settings);
    }
}
