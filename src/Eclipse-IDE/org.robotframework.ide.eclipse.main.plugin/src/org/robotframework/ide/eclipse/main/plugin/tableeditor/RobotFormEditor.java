package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPage;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPage;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.FileSectionsParser;

public class RobotFormEditor extends FormEditor {

    public static final String ID = "org.robotframework.ide.tableditor";

    private RobotSuiteFile suiteModel;

    private RobotEditorCommandsStack commandsStack;

    private boolean isEditable;

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            final IEclipseContext context = (IEclipseContext) site.getService(IEclipseContext.class);
            commandsStack = new RobotEditorCommandsStack(context);
            context.set(RobotEditorCommandsStack.class, commandsStack);
            context.set("suiteFileModel", new ContextFunction() {
                @Override
                public Object compute(final IEclipseContext context, final String contextKey) {
                    return provideSuiteModel();
                }
            });
            ContextInjectionFactory.inject(this, context);

            super.init(site, input);
        } catch (final IllegalRobotEditorInputException e) {
            throw new PartInitException("Unable to open editor", e);
        }
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            isEditable = !((FileEditorInput) input).getFile().isReadOnly();
            setPartName(input.getName());
        } else {
            final IStorage storage = (IStorage) input.getAdapter(IStorage.class);
            if (storage != null) {
                isEditable = !storage.isReadOnly();
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");
            } else {
                throw new IllegalRobotEditorInputException("Unable to open editor: unrecognized input of class: "
                        + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    protected void addPages() {
        try {
            addEditCasesPage();
            addVariablesPage();
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    private void addEditCasesPage() throws PartInitException {
        final FormPage main = new CasesEditorPage(this);
        final int index = addPage(main);
        setPageImage(index, RobotImages.getRobotImage().createImage());
    }

    private void addVariablesPage() throws PartInitException {
        final FormPage main = new VariablesEditorPage(this);
        final int index = addPage(main);
        setPageImage(index, RobotImages.getRobotVariableImage().createImage());
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        commitPages(true);
        for (final IEditorPart dirtyEditorPart : getDirtyEditors()) {
            dirtyEditorPart.doSave(monitor);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return getActiveEditor() != null && getActiveEditor().isSaveAsAllowed();
    }

    @Override
    public void doSaveAs() {
        commitPages(true);
        for (final IEditorPart dirtyEditorPart : getDirtyEditors()) {
            dirtyEditorPart.doSaveAs();
        }
    }

    private List<IEditorPart> getDirtyEditors() {
        final List<IEditorPart> list = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editor = getEditor(i);
            if (editor.isDirty()) {
                list.add(editor);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void dispose() {
        super.dispose();

        commandsStack.clear();
        final IEclipseContext context = (IEclipseContext) getSite().getService(IEclipseContext.class);
        ContextInjectionFactory.uninject(this, context);
    }

    @Inject
    @Optional
    private void closeEditorWhenResourceBecomesNotAvailable(
            @UIEventTopic(RobotModelEvents.ROBOT_MODEL) final RobotElementChange change) {
        if (change.getKind() == Kind.REMOVED && getEditorInput() instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) getEditorInput()).getFile();
            final RobotElement element = change.getElement();

            if (element instanceof RobotProject && ((RobotProject) element).getProject().equals(file.getProject())) {
                close(true);
            } else if (element instanceof RobotFolder
                    && ((RobotFolder) element).getFolder().getLocation().isPrefixOf(file.getLocation())) {
                close(true);
            } else if (element instanceof RobotSuiteFile && ((RobotSuiteFile) element).getFile().equals(file)) {
                close(true);
            }
        }
    }

    public RobotSuiteFile provideSuiteModel() {
        if (suiteModel != null) {
            return suiteModel;
        }
        if (getEditorInput() instanceof FileEditorInput) {
            return RobotFramework.getModelManager().createSuiteFile(((FileEditorInput) getEditorInput()).getFile());
        } else {
            final IStorage storage = (IStorage) getEditorInput().getAdapter(IStorage.class);
            try {
                final FileSectionsParser parser = new FileSectionsParser(storage.getContents(), storage.isReadOnly());
                return parser.parseRobotSuiteFile();
            } catch (final IOException | CoreException e) {
                throw new RuntimeException("Unable to provide model for given input", e);
            }
        }
    }

    public IEditorPart activatePage(final RobotSuiteFileSection section) {
        int index = -1;

        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof RobotSectionPart && ((RobotSectionPart) part).isPartFor(section)) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            setActivePage(index);
            return (IEditorPart) pages.get(index);
        }
        return null;
    }

    private static class IllegalRobotEditorInputException extends RuntimeException {

        public IllegalRobotEditorInputException(final String message) {
            super(message);
        }
    }
}
