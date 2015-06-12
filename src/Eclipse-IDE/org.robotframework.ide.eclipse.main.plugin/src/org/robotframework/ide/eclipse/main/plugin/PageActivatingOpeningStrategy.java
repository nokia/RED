package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RobotElement.OpenStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.base.Optional;

class PageActivatingOpeningStrategy extends OpenStrategy {

    private final RobotSuiteFileSection section;
    private final IWorkbenchPage page;
    private final IFile file;
    private final Optional<RobotElement> elementToReveal;

    PageActivatingOpeningStrategy(final IWorkbenchPage page, final IFile file,
            final RobotSuiteFileSection section, final RobotElement elementToReveal) {
        this.page = page;
        this.file = file;
        this.section = section;
        this.elementToReveal = Optional.fromNullable(elementToReveal);
    }

    PageActivatingOpeningStrategy(final IWorkbenchPage page, final IFile file,
            final RobotSuiteFileSection section) {
        this(page, file, section, null);
    }

    @Override
    public void run() {
        final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            final IEditorPart ed = page.openEditor(new FileEditorInput(file), desc.getId());
            if (ed instanceof RobotFormEditor) { // it can be ErrorEditorPart if something went wrong
                final RobotFormEditor editor = (RobotFormEditor) ed;
                final ISectionEditorPart activatedPage = editor.activatePage(section);
                activatedPage.setFocus();

                if (elementToReveal.isPresent()) {
                    activatedPage.revealElement(elementToReveal.get());
                }
            }
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to open editor for file: " + file.getName(), e);
        }
    }
}
