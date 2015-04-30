package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPage;

public class VariablesEditorPage extends SectionEditorPage {

    public static final String ID = "org.robotframework.ide.eclipse.editor.variablesPage";
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.variables.context";

    private VariablesFormPart variablesPart;

    public VariablesEditorPage(final FormEditor editor) {
        super(editor, ID, RobotVariablesSection.SECTION_NAME);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return section instanceof RobotVariablesSection;
    }

    @Override
    protected List<? extends IFormPart> createPageParts(final IEditorSite editorSite) {
        variablesPart = new VariablesFormPart(editorSite);
        return Arrays.asList(variablesPart);
    }

    @Override
    protected ISelectionProvider getSelectionProvider() {
        return variablesPart.getViewer();
    }

    @Override
    protected String getContextId() {
        return CONTEXT_ID;
    }

    @Override
    public Image getTitleImage() {
        return RobotImages.getRobotVariableImage().createImage();
    }

    @Override
    public void revealElement(final RobotElement robotVariable) {
        variablesPart.revealVariable(robotVariable);
    }

    @Override
    public com.google.common.base.Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
        return suite.findSection(RobotVariablesSection.class);
    }

    @Override
    protected String getSectionName() {
        return RobotVariablesSection.SECTION_NAME;
    }
}
