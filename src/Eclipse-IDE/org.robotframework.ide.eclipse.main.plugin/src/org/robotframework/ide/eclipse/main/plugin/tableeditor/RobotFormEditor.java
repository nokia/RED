/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.mapping.QuickTokenListenerBaseTwoModelReferencesLinker;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsPreferencesPage;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.TasksEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPart;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.ErrorDialogWithLinkToPreferences;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class RobotFormEditor extends FormEditor {

    private static final String EDITOR_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.context";

    public static final String ID = "org.robotframework.ide.tableditor";

    private static IPartListener robotFormEditorPartListener;

    private final List<IEditorPart> parts = new ArrayList<>();

    private RedClipboard clipboard;

    private RobotSuiteFile suiteModel;

    private SuiteFileMarkersListener validationListener;

    public RedClipboard getClipboard() {
        return clipboard;
    }

    public RobotSuiteFile provideSuiteModel() {
        if (suiteModel == null) {
            suiteModel = createSuiteFile(getEditorInput());
        }
        return suiteModel;
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            super.init(site, input);

            clipboard = new RedClipboard(site.getShell().getDisplay());
            validationListener = new SuiteFileMarkersListener();

            prepareEclipseContext();

            initRobotFormEditorPartListener(site.getPage());

        } catch (final IllegalRobotEditorInputException e) {
            throw new PartInitException("Unable to open editor", e);
        }
    }

    private void prepareEclipseContext() {
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext eclipseContext = parentContext.getActiveLeaf();
        eclipseContext.set(RobotEditorSources.SUITE_FILE_MODEL, new ContextFunction() {

            @Override
            public Object compute(final IEclipseContext context, final String contextKey) {
                return provideSuiteModel();
            }
        });
        eclipseContext.set(RedClipboard.class, clipboard);
        eclipseContext.set(SuiteFileMarkersContainer.class, validationListener);
        ContextInjectionFactory.inject(this, eclipseContext);
        ContextInjectionFactory.inject(validationListener, eclipseContext);
    }

    private void initRobotFormEditorPartListener(final IWorkbenchPage page) {
        if (robotFormEditorPartListener == null) {
            robotFormEditorPartListener = new RobotFormEditorPartListener();
            page.addPartListener(robotFormEditorPartListener);
        }
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final String parentPrefix = RedPlugin.getDefault().getPreferences().isParentDirectoryNameInTabEnabled()
                    ? ((FileEditorInput) input).getFile().getParent().getName() + "/"
                    : "";
            setPartName(parentPrefix + input.getName());
        } else {
            final IStorage storage = input.getAdapter(IStorage.class);
            if (storage != null) {
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");
            } else {
                throw new IllegalRobotEditorInputException(
                        "Unable to open editor: unrecognized input of class: " + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    @Override
    protected void addPages() {
        try {
            prepareCommandsContext();

            if (suiteModel.isSuiteFile()) {
                addEditorPart(new CasesEditorPart(), "Test Cases");
            } else if (suiteModel.isRpaSuiteFile()) {
                addEditorPart(new TasksEditorPart(), "Tasks");
            }
            addEditorPart(new KeywordsEditorPart(), "Keywords");
            addEditorPart(new SettingsEditorPart(), "Settings");
            addEditorPart(new VariablesEditorPart(), "Variables");
            addEditorPart(new SuiteSourceEditor(), "Source", ImagesManager.getImage(RedImages.getSourceImage()));

            activateProperPage();
            initializeMarkersSupportForTables();
        } catch (final Exception e) {
            throw new RobotEditorOpeningException("Unable to initialize Suite editor", e);
        }
    }

    private void initializeMarkersSupportForTables() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(validationListener, IResourceChangeEvent.POST_CHANGE);
        validationListener.init();
    }

    private void activateProperPage() {
        final String pageToActivate = RobotFormEditorActivePageSaver.getLastActivePageId(getEditorInput());
        if (pageToActivate == null) {
            final ElementOpenMode openMode = RedPlugin.getDefault().getPreferences().getElementOpenMode();
            if (openMode == ElementOpenMode.OPEN_IN_SOURCE) {
                setActivePart("");
            } else {
                activateFirstPage();
            }
        } else {
            setActivePart(pageToActivate);
        }
    }

    private void setActivePart(final String pageIdToActivate) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart instanceof ISectionEditorPart
                    && ((ISectionEditorPart) editorPart).getId().equals(pageIdToActivate)) {
                setActivePage(i);
                return;
            }
        }
        setActivePage(getPageCount() - 1);
    }

    private void addEditorPart(final IEditorPart editorPart, final String partName) throws PartInitException {
        addEditorPart(editorPart, partName, editorPart.getTitleImage());
    }

    private void addEditorPart(final IEditorPart editorPart, final String partName, final Image image)
            throws PartInitException {
        parts.add(editorPart);
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext eclipseContext = parentContext.getActiveLeaf();
        ContextInjectionFactory.inject(editorPart, eclipseContext);

        final int newEditorPart = addPage(editorPart, getEditorInput());
        setPageImage(newEditorPart, image);
        setPageText(newEditorPart, partName);
    }

    private void prepareCommandsContext() {
        final IContextService commandsContext = getSite().getService(IContextService.class);
        commandsContext.activateContext(EDITOR_CONTEXT_ID);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        final IDocument document = getSourceEditor().getDocument();

        if (!(getActiveEditor() instanceof SuiteSourceEditor)) {
            updateSourceIfNeeded(() -> document);
        }

        final String contentTypeId = ASuiteFileDescriber.getContentType(suiteModel.getFile().getName(), document.get());
        if (!isContentTypeMismatched(contentTypeId)) {
            saveDirtyEditors(monitor);
        } else {
            if (shouldMismatchedContentBeSavedPriorToClosing(contentTypeId)) {
                saveDirtyEditors(monitor);
                reopenEditor();
            } else {
                monitor.setCanceled(true);
            }
        }
    }

    private void saveDirtyEditors(final IProgressMonitor monitor) {
        for (final IEditorPart dirtyEditor : getDirtyEditors()) {
            dirtyEditor.doSave(monitor);
        }
        updateActivePage();
    }

    private boolean isContentTypeMismatched(final String contentTypeId) {
        return suiteModel.isSuiteFile() && !ASuiteFileDescriber.isSuiteFile(contentTypeId)
                || suiteModel.isRpaSuiteFile() && !ASuiteFileDescriber.isRpaSuiteFile(contentTypeId)
                || suiteModel.isResourceFile() && !"resource".equals(suiteModel.getFileExtension())
                        && !ASuiteFileDescriber.isResourceFile(contentTypeId);
    }

    private boolean shouldMismatchedContentBeSavedPriorToClosing(final String contentTypeId) {

        final String title = "File content mismatch";
        final String description = "The file " + suiteModel.getFile().getName() + " is a %s file but after "
                + "changes there is %s section defined. From now on this file will be recognized as a %s file."
                + "\n\nClick OK to save and reopen editor or cancel saving";

        if (suiteModel.isSuiteFile() && ASuiteFileDescriber.isResourceFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "tests suite", "no Test Cases nor Tasks", "resource"));

        } else if (suiteModel.isSuiteFile() && ASuiteFileDescriber.isRpaSuiteFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "tests suite", "a Tasks", "tasks suite"));

        } else if (suiteModel.isResourceFile() && !"resource".equals(suiteModel.getFileExtension())
                && ASuiteFileDescriber.isSuiteFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "resource", "a Test Cases", "tests suite"));

        } else if (suiteModel.isResourceFile() && !"resource".equals(suiteModel.getFileExtension())
                && ASuiteFileDescriber.isRpaSuiteFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "resource", "a Tasks", "tasks suite"));

        } else if (suiteModel.isRpaSuiteFile() && ASuiteFileDescriber.isSuiteFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "tasks suite", "a Test Cases", "tests suite"));

        } else if (suiteModel.isRpaSuiteFile() && ASuiteFileDescriber.isResourceFile(contentTypeId)) {
            return MessageDialog.openConfirm(getSite().getShell(), title,
                    String.format(description, "tasks suite", "no Test Cases nor Tasks", "resource"));
        }
        return false;
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    private void reopenEditor() {
        close(false);
        final IWorkbenchPartSite site = getSite();
        final IWorkbenchPage page = site.getPage();
        site.getShell().getDisplay().asyncExec(() -> tryToOpen(suiteModel.getFile(), page));
    }

    public static void tryToOpen(final IFile file, final IWorkbenchPage page) {
        final IEditorRegistry editorRegistry = page.getWorkbenchWindow().getWorkbench().getEditorRegistry();
        final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
        try {
            page.openEditor(new FileEditorInput(file), desc.getId());
        } catch (final PartInitException e) {
            throw new RobotEditorOpeningException("Unable to open editor for file: " + file.getName(), e);
        }
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
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext context = parentContext.getActiveLeaf();
        ContextInjectionFactory.uninject(this, context);
        ContextInjectionFactory.uninject(validationListener, context);
        for (final IEditorPart part : parts) {
            ContextInjectionFactory.uninject(part, context);
        }

        super.dispose();

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(validationListener);

        clipboard.dispose();
        suiteModel.dispose();

        final IEventBroker eventBroker = getSite().getService(IEventBroker.class);
        eventBroker.post(RobotModelEvents.SUITE_MODEL_DISPOSED, RobotElementChange.createChangedElement(suiteModel));
        RobotArtifactsValidator.revalidate(suiteModel);
    }

    public SelectionLayerAccessor getSelectionLayerAccessor() {
        final IEditorPart activeEditor = getActiveEditor();
        if (activeEditor instanceof ISectionEditorPart) {
            return ((ISectionEditorPart) activeEditor).getSelectionLayerAccessor();
        }
        return null;
    }

    public java.util.Optional<TreeLayerAccessor> getTreeLayerAccessor() {
        final IEditorPart activeEditor = getActiveEditor();
        if (activeEditor instanceof ISectionEditorPart) {
            return ((ISectionEditorPart) activeEditor).getTreeLayerAccessor();
        }
        return java.util.Optional.empty();
    }

    private RobotSuiteFile createSuiteFile(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                    .createSuiteFile(((FileEditorInput) input).getFile());
            checkRuntimeEnvironment(suiteFile);
            return suiteFile;
        } else {
            final IStorage storage = input.getAdapter(IStorage.class);
            try (InputStream stream = storage.getContents()) {
                final String content = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
                return new RobotSuiteStreamFile(storage.getName(), content, storage.isReadOnly());
            } catch (final CoreException | IOException e) {
                throw new IllegalRobotEditorInputException("Unable to provide model for given input", e);
            }
        }
    }

    public SuiteSourceEditor getSourceEditor() {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart instanceof SuiteSourceEditor) {
                return (SuiteSourceEditor) editorPart;
            }
        }
        return null;
    }

    @Override
    protected void pageChange(final int newPageIndex) {
        if (newPageIndex != getCurrentPage() && getActiveEditor() instanceof ISectionEditorPart) {
            ((ISectionEditorPart) getActiveEditor()).aboutToChangeToOtherPage();
        }

        super.pageChange(newPageIndex);

        updateActivePage();
        final IEditorPart activeEditor = getActiveEditor();
        final String activePageId = activeEditor instanceof ISectionEditorPart
                ? ((ISectionEditorPart) activeEditor).getId() : "";
        RobotFormEditorActivePageSaver.saveActivePageId(getEditorInput(), activePageId);
    }

    private void updateActivePage() {
        final SuiteSourceEditor sourceEditor = getSourceEditor();

        if (getActiveEditor() instanceof ISectionEditorPart) {

            final ISectionEditorPart page = (ISectionEditorPart) getActiveEditor();
            page.updateOnActivation();

            if (isDirty()) {
                // there are some locking threads involved which results in blocking
                // main thread for hundreds of milliseconds thus giving stops when switching
                // from source part to some section editor part
                SwtThread.asyncExec(() -> sourceEditor.disableReconcilation());
            } else {
                sourceEditor.disableReconcilation();
            }

        } else if (getActiveEditor() instanceof SuiteSourceEditor) {
            sourceEditor.enableReconcilation();

            updateSourceIfNeeded(sourceEditor::getDocument);
        }
    }

    private void updateSourceIfNeeded(final Supplier<IDocument> documentSupplier) {
        if (!getDirtyEditors().isEmpty()) {
            waitForPendingEditorJobs();
            updateSourceFromModel(documentSupplier.get());
        }
    }

    private void waitForPendingEditorJobs() {
        // jobs are sending model modification events, so it has to be done before dumping model to
        // source
        for (final IEditorPart part : parts) {
            if (part instanceof ISectionEditorPart) {
                ((ISectionEditorPart) part).waitForPendingJobs();
            }
        }
    }

    private void updateSourceFromModel(final IDocument document) {
        final String separatorFromPreference = RedPlugin.getDefault()
                .getPreferences()
                .getSeparatorToUse(suiteModel.isTsvFile());
        final DumpContext ctx = new DumpContext(separatorFromPreference, true);
        final RobotFileOutput fileOutput = suiteModel.getLinkedElement().getParent();
        final DumpedResult dumpResult = new RobotFileDumper().dump(ctx, fileOutput);
        new QuickTokenListenerBaseTwoModelReferencesLinker().update(fileOutput, dumpResult);

        document.set(dumpResult.newContent());
    }

    public SuiteSourceEditor activateSourcePage() {
        if (getActiveEditor() instanceof SuiteSourceEditor) {
            return (SuiteSourceEditor) getActiveEditor();
        }
        final SuiteSourceEditor editor = getSourceEditor();
        setActiveEditor(editor);
        return editor;
    }

    public void activateFirstPage() {
        setActivePage(0);
    }

    public ISectionEditorPart activatePage(final RobotSuiteFileSection section) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof ISectionEditorPart && ((ISectionEditorPart) part).isPartFor(section)) {
                setActivePage(i);
                return (ISectionEditorPart) part;
            }
        }
        return null;
    }

    public <T> T getPage(final Class<T> pageClass) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part.getClass().equals(pageClass)) {
                return pageClass.cast(part);
            }
        }
        return null;
    }

    private void checkRuntimeEnvironment(final RobotSuiteFile suiteFile) {
        if (suiteFile != null) {
            final RobotProject robotProject = suiteFile.getRobotProject();
            if (robotProject != null) {
                final IRuntimeEnvironment env = robotProject.getRuntimeEnvironment();
                if (!env.isValidPythonInstallation() || !env.hasRobotInstalled()) {
                    final Shell shell = getSite().getShell();
                    if (shell != null && shell.isVisible()) {
                        final String dialogMessage = "Project '" + robotProject.getName()
                                + "' uses invalid Python environment"
                                + (env.hasRobotInstalled() ? "." : " (missing Robot Framework).")
                                + " Check Python/Robot Framework installation and set it in Preferences.";
                        new ErrorDialogWithLinkToPreferences(shell, "Runtime Environment Error", dialogMessage,
                                InstalledRobotsPreferencesPage.ID, "Installed Robot Frameworks").open();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            return new RobotOutlinePage(this, suiteModel);
        } else if (adapter == IToggleBreakpointsTarget.class) {
            return new ToggleBreakpointTarget();
        }
        return super.getAdapter(adapter);
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

        private static final long serialVersionUID = 1L;

        public IllegalRobotEditorInputException(final String message) {
            super(message);
        }

        public IllegalRobotEditorInputException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static class RobotEditorOpeningException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RobotEditorOpeningException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
