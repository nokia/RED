package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshCaseCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;

public class CasesEditorFormFragment extends CodeEditorFormFragment {

    @Override
    protected ITreeContentProvider createContentProvider() {
        return new CasesContentProvider();
    }

    @Override
    protected String getViewerMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.cases.contextMenu";
    }

    @Override
    protected String getHeaderMenuId() {
        return "org.robotframework.ide.eclipse.editor.page.cases.header.contextMenu";
    }

    @Override
    protected boolean sectionIsDefined() {
        return fileModel.findSection(RobotCasesSection.class).isPresent();
    }

    @Override
    protected RobotSuiteFileSection getSection() {
        return (RobotSuiteFileSection) fileModel.findSection(RobotCasesSection.class).orNull();
    }

    @Override
    protected NewElementsCreator provideNewElementsCreator() {
        return new NewElementsCreator() {

            @Override
            public RobotElement createNew(final Object parent) {
                if (parent instanceof RobotCasesSection) {
                    final RobotCasesSection section = (RobotCasesSection) parent;
                    commandsStack.execute(new CreateFreshCaseCommand(section, true));
                    return section.getChildren().get(section.getChildren().size() - 1);
                } else if (parent instanceof RobotCase) {
                    final RobotCase testCase = (RobotCase) parent;
                    commandsStack.execute(new CreateFreshKeywordCallCommand(testCase, true));
                    return testCase.getChildren().get(testCase.getChildren().size() - 1);
                }
                return null;
            }
        };
    }

    @Override
    protected int calculateLongestArgumentsList() {
        final RobotSuiteFileSection section = getSection();
        int max = 5;
        if (section != null) {
            final List<RobotElement> children = section.getChildren();
            for (final RobotElement testCase : children) {
                for (final RobotElement nestedElement : testCase.getChildren()) {
                    final RobotKeywordCall call = (RobotKeywordCall) nestedElement;
                    max = Math.max(max, call.getArguments().size());
                }
            }
        }
        return max;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotKeywordsSection.SECTION_NAME) final String filter) {
        // nothing to do yet
    }

    @Inject
    @Optional
    private void whenCaseIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_STRUCTURAL_ALL) final RobotCase testCase) {
        if (testCase.getSuiteFile() == fileModel) {
            viewer.refresh(testCase);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenCaseDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_CASE_DETAIL_CHANGE_ALL) final RobotCase testCase) {
        if (testCase.getSuiteFile() == fileModel) {
            viewer.update(testCase, null);
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenKeywordCallDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotKeywordCall keywordCall) {
        if (keywordCall.getParent() instanceof RobotCase && keywordCall.getSuiteFile() == fileModel) {
            viewer.update(keywordCall, null);
            setDirty();
        }
    }
}
