package org.robotframework.ide.eclipse.main.plugin.tableeditor;

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
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteStreamFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPage;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPage;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPage;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

public class RobotFormEditor extends FormEditor {

    private static final String EDITOR_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.context";

    public static final String ID = "org.robotframework.ide.tableditor";

    private RobotSuiteFile suiteModel;

    private boolean isEditable;

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            super.init(site, input);

            prepareEclipseContext();
        } catch (final IllegalRobotEditorInputException e) {
            throw new PartInitException("Unable to open editor", e);
        }
    }

    private void prepareEclipseContext() {
        final IEclipseContext eclipseContext = ((IEclipseContext) getSite().getService(IEclipseContext.class))
                .getActiveLeaf();
        eclipseContext.set(RobotEditorSources.SUITE_FILE_MODEL, new ContextFunction() {
            @Override
            public Object compute(final IEclipseContext context, final String contextKey) {
                return provideSuiteModel();
            }
        });
        ContextInjectionFactory.inject(this, eclipseContext);
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
            prepareCommandsContext();

            addPage(new CasesEditorPage(this));
            addPage(new SettingsEditorPage(this));
            addPage(new VariablesEditorPage(this));

            final int sourceEditorIndex = addPage(new TextEditorWrapper(), getEditorInput());
            setPageText(sourceEditorIndex, "Source");
            setActivePage(sourceEditorIndex);
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    private void prepareCommandsContext() {
        final IContextService commandsContext = (IContextService) getSite().getService(IContextService.class);
        commandsContext.activateContext(EDITOR_CONTEXT_ID);
    }

    private void addPage(final FormPage page) throws PartInitException {
        final int index = addPage(page, getEditorInput());
        setPageImage(index, page.getTitleImage());
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        commitPages(true);
        try {
            suiteModel = provideSuiteModel();
            suiteModel.commitChanges(monitor);
            suiteModel = null;
            firePropertyChange(PROP_DIRTY);
        } catch (final CoreException e) {
            monitor.setCanceled(true);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false; // FIXME : getActiveEditor() != null && getActiveEditor().isSaveAsAllowed();
    }

    @Override
    public void doSaveAs() {
        // FIXME : implement
    }

    @Override
    public void dispose() {
        super.dispose();

        final IEclipseContext context = ((IEclipseContext) getSite().getService(IEclipseContext.class)).getActiveLeaf();
        ContextInjectionFactory.uninject(this, context);
    }

    @Inject
    @Optional
    private void closeEditorWhenResourceBecomesNotAvailable(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
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
            suiteModel = RobotFramework.getModelManager().createSuiteFile(
                    ((FileEditorInput) getEditorInput()).getFile());
        } else {
            final IStorage storage = (IStorage) getEditorInput().getAdapter(IStorage.class);
            try {
                suiteModel = new RobotSuiteStreamFile(storage.getName(), storage.getContents(), storage.isReadOnly());
            } catch (final CoreException e) {
                throw new RuntimeException("Unable to provide model for given input", e);
            }
        }
        return suiteModel;
    }

    @Override
    protected void pageChange(final int newPageIndex) {
        super.pageChange(newPageIndex);
        updateActivePage();
    }

    private void updateActivePage() {
        if (getActiveEditor() instanceof SectionEditorPage) {
            final SectionEditorPage page = (SectionEditorPage) getActiveEditor();
            page.updateMessages();
            page.updateToolbars();
        }
    }

    public void activateSourcePage() {
        if (getActiveEditor() instanceof TextEditorWrapper) {
            return;
        }

        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editor = getEditor(i);
            if (editor instanceof TextEditorWrapper) {
                setActiveEditor(editor);
                break;
            }
        }
    }

    public IEditorPart activatePage(final RobotSuiteFileSection section) {
        int index = -1;

        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof SectionEditorPage && ((SectionEditorPage) part).isPartFor(section)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            setActivePage(index);
            return (IEditorPart) pages.get(index);
        }
        return null;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            return new RobotOutlinePage(suiteModel);
        }
        return super.getAdapter(adapter);
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (suiteModel == file) {
            updateActivePage();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (suiteModel == file) {
            updateActivePage();
        }
    }

    private static class IllegalRobotEditorInputException extends RuntimeException {

        public IllegalRobotEditorInputException(final String message) {
            super(message);
        }
    }
}
