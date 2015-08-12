package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeEditorFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;


public class CaseSettingsFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    protected RobotSuiteFile fileModel;

    @Inject
    protected RobotEditorCommandsStack commandsStack;

    @Inject
    protected RedFormToolkit toolkit;

    @Override
    public void initialize(final Composite parent) {
        createDetailsPanel(parent);
    }

    private void createDetailsPanel(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setExpanded(false);
        section.setText("Settings");
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(section);

        final Composite composite = toolkit.createComposite(section);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        section.setClient(composite);
    }

    @Override
    public void setFocus() {
        // nothing to do yet
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }

    @Inject
    @Optional
    private void whenKeywordSelectionChanged(
            @UIEventTopic(CodeEditorFormFragment.MAIN_PART_SELECTION_CHANGED_TOPIC) final IStructuredSelection selection) {
        final RobotElement selectedElement = Selections.getOptionalFirstElement(selection, RobotElement.class).orNull();

        if (selectedElement instanceof RobotCase) {
            newCaseWasSelected((RobotCase) selectedElement);
        } else if (selectedElement instanceof RobotKeywordCall) {
            final IRobotCodeHoldingElement parent = ((RobotKeywordCall) selectedElement).getParent();
            if (parent instanceof RobotCase) {
                newCaseWasSelected((RobotCase) parent);
            }
        }
    }

    private void newCaseWasSelected(final RobotCase testCase) {
        // nothing to do yet
    }
}
