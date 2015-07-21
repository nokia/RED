package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.EditorPart;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPart;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

public class RobotFormEditor extends FormEditor {

    private static final String EDITOR_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.context";

    public static final String ID = "org.robotframework.ide.tableditor";

    private Clipboard clipboard;

    private RobotSuiteFile suiteModel;

    private boolean isEditable;

    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            super.init(site, input);

            clipboard = new Clipboard(site.getShell().getDisplay());
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
        eclipseContext.set(Clipboard.class, clipboard);
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

            addEditorPart(new CasesEditorPart(), "Test Cases");
            addEditorPart(new KeywordsEditorPart(), "Keywords");
            addEditorPart(new SettingsEditorPart(), "Settings");
            addEditorPart(new VariablesEditorPart(), "Variables");
            addEditorPart(new TextEditorWrapper(), "Source", null);

            setActivePage(getPageToActivate());
        } catch (final PartInitException e) {
            throw new RuntimeException("Unable to initialize editor", e);
        }
    }

    private int getPageToActivate() {
        final int def = 4;
        if (getEditorInput() instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) getEditorInput();
            final IFile file = fileInput.getFile();

            final String sectionName = ID + ".activePage." + file.getFullPath().toPortableString();
            final IDialogSettings dialogSettings = RobotFramework.getDefault().getDialogSettings();
            final IDialogSettings section = dialogSettings.getSection(sectionName);
            if (section == null) {
                return def;
            }
            final int activeIndex = section.getInt("activePage");
            if (activeIndex >= 0 && activeIndex <= 4) {
                return activeIndex;
            }
            return def;
        }
        return def;
    }

    private void saveActivePage(final int index) {
        if (getEditorInput() instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) getEditorInput();
            final IFile file = fileInput.getFile();
            
            final String sectionName = ID + ".activePage." + file.getFullPath().toPortableString();
            final IDialogSettings dialogSettings = RobotFramework.getDefault().getDialogSettings();
            IDialogSettings section = dialogSettings.getSection(sectionName);
            if (section == null) {
                section = dialogSettings.addNewSection(sectionName);
            }
            section.put("activePage", index);
        }
    }

    private void addEditorPart(final EditorPart editorPart, final String partName) throws PartInitException {
        addEditorPart(editorPart, partName, editorPart.getTitleImage());
    }

    private void addEditorPart(final EditorPart editorPart, final String partName, final Image image)
            throws PartInitException {
        final int newVariablesPart = addPage(editorPart, getEditorInput());
        setPageImage(newVariablesPart, image);
        setPageText(newVariablesPart, partName);
    }

    private void prepareCommandsContext() {
        final IContextService commandsContext = (IContextService) getSite().getService(IContextService.class);
        commandsContext.activateContext(EDITOR_CONTEXT_ID);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        try {
            for (final IEditorPart dirtyEditor : getDirtyEditors()) {
                dirtyEditor.doSave(monitor);
            }

            suiteModel = provideSuiteModel();
            suiteModel.commitChanges(monitor);
            suiteModel = null;

            updateActivePage();
        } catch (final CoreException e) {
            monitor.setCanceled(true);
        }
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = newArrayList();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // it is not allowed currently
    }

    @Override
    public void dispose() {
        super.dispose();

        clipboard.dispose();

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
        saveActivePage(newPageIndex);
    }

    private void updateActivePage() {
        if (getActiveEditor() instanceof ISectionEditorPart) {
            final ISectionEditorPart page = (ISectionEditorPart) getActiveEditor();
            page.updateOnActivation();
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

    public ISectionEditorPart activatePage(final RobotSuiteFileSection section) {
        int index = -1;

        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof ISectionEditorPart && ((ISectionEditorPart) part).isPartFor(section)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            setActivePage(index);
            return (ISectionEditorPart) pages.get(index);
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
