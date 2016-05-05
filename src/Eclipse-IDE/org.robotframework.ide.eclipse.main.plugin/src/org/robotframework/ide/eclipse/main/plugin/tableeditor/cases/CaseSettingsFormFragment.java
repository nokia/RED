/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshCaseSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeSettingsFormFragment;

public class CaseSettingsFormFragment extends CodeSettingsFormFragment {

    public CaseSettingsFormFragment() {
        super("Test case %ssettings", "Provide test case documentation and settings");
    }

    @Override
    protected IContentProvider createContentProvider() {
        return new CaseSettingsContentProvider();
    }

    @Override
    protected EditorCommand createCommandForDocumentationCreation(final RobotCodeHoldingElement codeElement,
            final String newDocumentation) {
        return new CreateFreshCaseSettingCommand((RobotCase) codeElement, 0, "Documentation",
                newArrayList(newDocumentation));
    }

    @Override
    protected Map<String, String> prepareTooltips() {
        final HashMap<String, String> tooltips = new HashMap<>();
        tooltips.put(RobotCase.TAGS, "These tags are set to this test case and they possibly override Default Tags");
        tooltips.put(RobotCase.SETUP, "The keyword $s is executed before other keywords inside the definition");
        tooltips.put(RobotCase.TEMPLATE, "The keyword %s is used as a template");
        tooltips.put(RobotCase.TIMEOUT,
                "Specifies maximum time this test case is allowed to execute before being aborted.\n"
                        + "This setting overrides Test Timeout setting set on suite level\n"
                        + "Numerical values are intepreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        tooltips.put(RobotCase.TEARDOWN, "The keyword %s is executed after every other keyword inside the definition");
        return tooltips;
    }

    @Override
    protected RobotDefinitionSetting getDocumentationSetting(
            final com.google.common.base.Optional<? extends RobotCodeHoldingElement> codeElement) {
        if (codeElement.isPresent()) {
            return ((RobotCase) codeElement.get()).getDocumentationSetting();
        }
        return null;

    }

    @Override
    protected List<RobotElement> getElementsForMatchesCollection() {
        final RobotCase testCase = (RobotCase) getCurrentCodeElement();
        final List<RobotElement> settings = CaseSettingsModel.buildCaseSettingsList(testCase);
        if (testCase != null) {
            final RobotDefinitionSetting docSetting = testCase.getDocumentationSetting();
            if (docSetting != null) {
                settings.add(docSetting);
            }
        }
        return settings;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC
            + "/Test_Cases") final HeaderFilterMatchesCollection matches) {
        handleFilteringRequest(matches);
    }

    @Inject
    @Optional
    private void whenKeywordSelectionChanged(@UIEventTopic(CodeEditorFormFragment.MAIN_PART_SELECTION_CHANGED_TOPIC
            + "/Test_Cases") final IStructuredSelection selection) {
        selectionInMainViewerHasChanged(selection);
    }
}
