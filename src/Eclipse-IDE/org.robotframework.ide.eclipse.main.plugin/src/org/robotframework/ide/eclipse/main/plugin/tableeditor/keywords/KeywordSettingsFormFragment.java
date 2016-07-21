/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshCodeHolderSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeSettingsFormFragment;

public class KeywordSettingsFormFragment extends CodeSettingsFormFragment {

    public KeywordSettingsFormFragment() {
        super("Keyword %ssettings", "Provide keyword documentation and settings");
    }

    @Override
    protected IContentProvider createContentProvider() {
        return new KeywordSettingsContentProvider();
    }

    @Override
    protected EditorCommand createCommandForDocumentationCreation(
            final RobotCodeHoldingElement codeElement, final String newDocumentation) {
        return new CreateFreshCodeHolderSettingCommand(codeElement, 0, "Documentation", newArrayList(newDocumentation));
    }

    @Override
    protected Map<String, String> prepareTooltips() {
        final Map<String, String> tooltips = new LinkedHashMap<>();
        tooltips.put(RobotKeywordDefinition.TEARDOWN,
                "The keyword %s is executed after every other keyword inside the definition");
        tooltips.put(RobotKeywordDefinition.TIMEOUT,
                "Specifies maximum time this keyword is allowed to execute before being aborted.\n"
                + "This setting overrides Test Timeout setting set on suite level\n"
                + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put(RobotKeywordDefinition.RETURN,
                "Specify the return value for this keyword. Multiple values can be used.");
        return tooltips;
    }

    @Override
    protected RobotDefinitionSetting getDocumentationSetting(
            final com.google.common.base.Optional<? extends RobotCodeHoldingElement> codeElement) {
        if (codeElement.isPresent()) {
            return ((RobotKeywordDefinition) codeElement.get()).getDocumentationSetting();
        }
        return null;
    }

    @Override
    protected List<RobotElement> getElementsForMatchesCollection() {
        final RobotKeywordDefinition keyword = (RobotKeywordDefinition) getCurrentCodeElement();
        final List<RobotElement> settings = KeywordSettingsModel.buildKeywordSettingsList(keyword);
        if (keyword != null) {
            final RobotDefinitionSetting docSetting = keyword.getDocumentationSetting();
            if (docSetting != null) {
                settings.add(docSetting);
            }
        }
        return settings;
    }

    @Inject
    @Optional
    private void whenKeywordSelectionChanged(@UIEventTopic(CodeEditorFormFragment.MAIN_PART_SELECTION_CHANGED_TOPIC
            + "/" + RobotKeywordsSection.SECTION_NAME) final IStructuredSelection selection) {
        selectionInMainViewerHasChanged(selection);
    }
}
