package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSectionPart;

public class CasesEditorPage extends FormPage implements RobotSectionPart {

    private static final String ID = "org.robotframework.ide.eclipse.editor.mainPage";
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.cases.context";
    private static final String TITLE = "Edit Cases";

    public CasesEditorPage(final FormEditor editor) {
        super(editor, ID, TITLE);
    }

    @Override
    public boolean isEditor() {
        return true;
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);

        getSite().setSelectionProvider(null);

        final IContextService service = (IContextService) getSite().getService(IContextService.class);
        service.activateContext(CONTEXT_ID);
    }

    @Override
    public Image getTitleImage() {
        return RobotImages.getRobotImage().createImage();
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return false;
    }

}
