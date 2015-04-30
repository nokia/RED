package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPage;

import com.google.common.base.Optional;

public class CasesEditorPage extends SectionEditorPage {

    private static final String ID = "org.robotframework.ide.eclipse.editor.mainPage";
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.cases.context";

    private CasesFormPart casesPart;

    public CasesEditorPage(final FormEditor editor) {
        super(editor, ID, RobotCasesSection.SECTION_NAME);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return section instanceof RobotCasesSection;
    }

    @Override
    public void revealElement(final RobotElement element) {
        // nothing to do so far
    }

    @Override
    protected List<? extends IFormPart> createPageParts(final IEditorSite editorSite) {
        casesPart = new CasesFormPart(editorSite);
        return Arrays.asList(casesPart);
    }

    @Override
    protected ISelectionProvider getSelectionProvider() {
        return casesPart.getViewer();
    }

    @Override
    protected String getContextId() {
        return CONTEXT_ID;
    }

    @Override
    public Image getTitleImage() {
        return RobotImages.getRobotImage().createImage();
    }

    @Override
    public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
        return suite.findSection(RobotCasesSection.class);
    }

    @Override
    protected String getSectionName() {
        return RobotCasesSection.SECTION_NAME;
    }

}
