/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.GotoLineAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.eclipse.ui.themes.ITheme;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditorActionBarContributor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorSelectionFixer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleBreakpointHandler;
import org.robotframework.red.swt.StyledTextWrapper;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Splitter;

public class SuiteSourceEditor extends TextEditor {

    private static final String SOURCE_PART_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.sources.context";

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    private SuiteSourceEditorFoldingSupport foldingSupport;

    @Inject
    private KeywordUsagesFinder kwUsagesFinder;

    private final OnSaveSourceFormattingTrigger saveSourceFormatterTrigger = new OnSaveSourceFormattingTrigger();

    private final OnSaveLibrariesAutodiscoveryTrigger saveLibDiscoveryTrigger = new OnSaveLibrariesAutodiscoveryTrigger();

    public SourceViewer getViewer() {
        return (SourceViewer) getSourceViewer();
    }

    public KeywordUsagesFinder getKeywordUsagesFinder() {
        return kwUsagesFinder;
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SuiteSourceEditorConfiguration(this, getContentAssistActivationTrigger()));
        setDocumentProvider(new SuiteSourceDocumentProvider(() -> fileModel));
    }

    private KeySequence getContentAssistActivationTrigger() {
        final IBindingService service = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        return (KeySequence) service.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    }

    @Override
    protected void createActions() {
        super.createActions();

        final GotoLineAction gotoAction = new GotoLineAction(this);
        gotoAction.setActionDefinitionId(ITextEditorActionConstants.GOTO_LINE);
        setAction(ITextEditorActionConstants.GOTO_LINE, gotoAction);
    }

    @Override
    protected void doSetInput(final IEditorInput input) throws CoreException {
        super.doSetInput(input);
        if (input.getAdapter(IStorage.class) != null) {
            fileModel.reparseEverything(getDocument().get());
            final IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
            eventBroker.post(RobotModelEvents.REPARSING_DONE, fileModel);
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        final ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

        installProjectionAndFolding(viewer);

        if (!fileModel.isFromLocalStorage()) {
            new SuiteSourceCurrentCellHighlighter(fileModel, viewer.getDocument()).install(viewer);
            new SuiteSourceOccurrenceMarksHighlighter(fileModel, viewer.getDocument()).install(viewer);
        }
        installBreakpointTogglingOnDoubleClick();
        installStatusBarUpdater(viewer);

        setFontFromPreference(viewer);
        addFontChangeListener(viewer);

        installPreferencesListener(viewer);

        activateContext();

        getSite().getService(ICommandService.class).addExecutionListener(saveLibDiscoveryTrigger);
    }

    @Override
    protected ISourceViewer createSourceViewer(final Composite parent, final IVerticalRuler ruler, final int styles) {
        final ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
                styles);
        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);
        return viewer;
    }

    @Override
    protected void performSave(final boolean overwrite, final IProgressMonitor progressMonitor) {
        final SuiteSourceEditorSelectionFixer selectionFixer = new SuiteSourceEditorSelectionFixer(
                getSelectionProvider());
        saveSourceFormatterTrigger.formatSourceIfRequired(this::getDocument, selectionFixer, fileModel,
                progressMonitor);

        super.performSave(overwrite, progressMonitor);

        saveLibDiscoveryTrigger.startLibrariesAutoDiscoveryIfRequired(fileModel);
    }

    @Override
    public void dispose() {
        getSite().getService(ICommandService.class).removeExecutionListener(saveLibDiscoveryTrigger);

        super.dispose();
    }

    @Override
    protected boolean isEditorInputIncludedInContextMenu() {
        return false;
    }

    @Override
    protected void installTabsToSpacesConverter() {
        // we will install our own edit strategy for tabs converting
    }

    private void installProjectionAndFolding(final ProjectionViewer viewer) {
        // turn projection mode on
        new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors()).install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
        foldingSupport = new SuiteSourceEditorFoldingSupport(new StyledTextWrapper(viewer.getTextWidget()),
                viewer.getProjectionAnnotationModel());
    }

    private void installStatusBarUpdater(final ProjectionViewer viewer) {
        viewer.getTextWidget().addCaretListener(event -> {
            updateLineLocationStatusBar(event.caretOffset);
            updateLineDelimitersStatus();
        });
    }

    private void updateLineLocationStatusBar(final int caretPosition) {
        try {
            final IDocument document = getDocument();
            int lineNumber = document.getLineOfOffset(caretPosition);
            final int columnNumber = caretPosition - document.getLineOffset(lineNumber) + 1;
            lineNumber++;

            IContributionItem find = getEditorSite().getActionBars()
                .getStatusLineManager()
                .find(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
            if (find instanceof StatusLineContributionItem) {
                find.setText(lineNumber + ":" + columnNumber);
            }
        } catch (final BadLocationException e) {
            RedPlugin.logError("Unable to get position in source editor in order to update status bar", e);
        }
    }

    private void updateLineDelimitersStatus() {
        final IDocument document = getDocument();

        IContributionItem find = getEditorSite().getActionBars()
                .getStatusLineManager()
                .find(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
        if (find instanceof StatusLineContributionItem) {
            final String delimiter = DocumentUtilities.getDelimiter(document);
            find.setText("\r\n".equals(delimiter) ? "CR+LF" : "LF");
        }
    }

    private void installBreakpointTogglingOnDoubleClick() {
        getVerticalRuler().getControl().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(final MouseEvent event) {
                try {
                    final IFile file = (IFile) getEditorInput().getAdapter(IResource.class);
                    final int line = computeBreakpointLineNumber(event.y);
                    ToggleBreakpointHandler.E4ToggleBreakpointHandler.toggle(file, line);
                } catch (final CoreException e) {
                    RedPlugin.logError("Unable to toggle breakpoint", e);
                }
            }
        });
    }

    private int computeBreakpointLineNumber(final int eventY) {
        final StyledText text = getSourceViewer().getTextWidget();
        final int lineHeight = text.getLineHeight();
        final int line = (int) Math.round((eventY / (double) lineHeight)) + getSourceViewer().getTopIndex();

        final int lineBottomPixel = text.getLinePixel(line) - lineHeight;
        if (eventY < lineBottomPixel) {
            return line - 1;
        } else if ((eventY - lineBottomPixel) > lineHeight) {
            return line + 1;
        } else {
            return line;
        }
    }

    private void installPreferencesListener(final ProjectionViewer viewer) {
        final IPreferenceChangeListener preferenceListener = event -> {
            if (event == null || event.getKey() == null) {
                return;

            } else if (event.getKey().startsWith(RedPreferences.SYNTAX_COLORING)) {
                final SyntaxHighlightingCategory category = SyntaxHighlightingCategory.fromPreferenceId(event.getKey());
                final String newValue = (String) event.getNewValue();
                final ColoringPreference newPref = newValue == null ? category.getDefault()
                        : ColoringPreference.fromPreferenceString(newValue);

                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getColoringTokens().refresh(category, newPref);
                refreshViewerColouring();

            } else if (RedPreferences.TASKS_DETECTION_ENABLED.equals(event.getKey())) {
                final boolean isEnabled = Boolean.parseBoolean((String) event.getNewValue());

                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getColoringTokens().refreshTasksAttributes(isEnabled);
                refreshViewerColouring();

            } else if (RedPreferences.TASKS_TAGS.equals(event.getKey())) {
                final String newValue = (String) event.getNewValue();
                final Set<String> newTags = newValue == null || newValue.isEmpty() ? new HashSet<>()
                        : new HashSet<>(Splitter.on(';').splitToList(newValue));

                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getColoringTokens().refreshTasksAttributes(newTags);
                refreshViewerColouring();

            } else if (newHashSet(RedPreferences.FOLDABLE_CASES, RedPreferences.FOLDABLE_TASKS,
                    RedPreferences.FOLDABLE_KEYWORDS, RedPreferences.FOLDABLE_SECTIONS,
                    RedPreferences.FOLDABLE_DOCUMENTATION, RedPreferences.FOLDING_LINE_LIMIT)
                            .contains(event.getKey())) {

                foldingSupport.updateFoldingStructure(fileModel, getDocument());

            } else if (newHashSet(RedPreferences.SEPARATOR_MODE, RedPreferences.SEPARATOR_JUMP_MODE_ENABLED,
                    RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED,
                    RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED,
                    RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN).contains(event.getKey())) {

                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getEditStrategyPreferences().refresh();

            } else if (RedPreferences.ASSISTANT_AUTO_INSERT_ENABLED.equals(event.getKey())) {
                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getContentAssistant().enableAutoInsert(Boolean.parseBoolean((String) event.getNewValue()));

            } else if (RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED.equals(event.getKey())) {
                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getContentAssistant().enableAutoActivation(Boolean.parseBoolean((String) event.getNewValue()));

            } else if (RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY.equals(event.getKey())) {
                final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
                config.getContentAssistant().setAutoActivationDelay(Integer.parseInt((String) event.getNewValue()));
            }
        };
        final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID);
        preferences.addPreferenceChangeListener(preferenceListener);
        viewer.getControl().addDisposeListener(e -> preferences.removePreferenceChangeListener(preferenceListener));
    }

    void refreshViewerColouring() {
        final SuiteSourceEditorConfiguration config = (SuiteSourceEditorConfiguration) getSourceViewerConfiguration();
        final ISourceViewer viewer = getSourceViewer();
        SwtThread.asyncExec(() -> {
            if (viewer != null && viewer.getTextWidget() != null && !viewer.getTextWidget().isDisposed()) {
                try {
                    viewer.getTextWidget().setRedraw(false);

                    config.resetTokensStore();
                    viewer.invalidateTextPresentation();
                } finally {
                    viewer.getTextWidget().setRedraw(true);
                }
            }
        });
    }

    private void activateContext() {
        final IContextService service = getSite().getService(IContextService.class);
        service.activateContext(SOURCE_PART_CONTEXT_ID);
    }

    public SuiteSourceEditorConfiguration getViewerConfiguration() {
        return (SuiteSourceEditorConfiguration) super.getSourceViewerConfiguration();
    }

    public IDocument getDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    RobotSuiteFile getFileModel() {
        return fileModel;
    }

    SuiteSourceEditorFoldingSupport getFoldingSupport() {
        return foldingSupport;
    }

    /**
     * Returns line number of cursor position.
     *
     * @return Line number indexed from 1
     */
    public int getCurrentLine() {
        final StyledText text = getSourceViewer().getTextWidget();
        return text.getLineAtOffset(text.getSelection().x) + 1;
    }

    /**
     * Returns line number from ruler activity.
     *
     * @return Line number indexed from 1
     */
    public int getLineFromRulerActivity() {
        return getVerticalRuler().getLineOfLastMouseButtonActivity() + 1;
    }

    private void addFontChangeListener(final ProjectionViewer viewer) {
        final ITheme currentTheme = PlatformUI.getWorkbench()
                .getThemeManager()
                .getCurrentTheme();
        final IPropertyChangeListener propertyListener = event -> {
            if (RedTheme.RED_SOURCE_EDITOR_FONT.equals(event.getProperty())) {
                setFont(viewer, RedTheme.Fonts.getRedSourceEditorFont());
            }
        };
        currentTheme.addPropertyChangeListener(propertyListener);
        viewer.getControl().addDisposeListener(e -> currentTheme.removePropertyChangeListener(propertyListener));
    }

    private void setFontFromPreference(final ProjectionViewer viewer) {
        final Font redSourceEditorFont = RedTheme.Fonts.getRedSourceEditorFont();
        final Font defaultTextEditorFont = RedTheme.Fonts.getTextEditorFont();

        if (!redSourceEditorFont.getFontData()[0].equals(defaultTextEditorFont.getFontData()[0])) {
            setFont(viewer, redSourceEditorFont);
        }
    }

    private void setFont(final ISourceViewer sourceViewer, final Font font) {
        if (sourceViewer.getDocument() != null) {
            final ISelectionProvider provider = sourceViewer.getSelectionProvider();
            final ISelection selection = provider.getSelection();
            final int topIndex = sourceViewer.getTopIndex();

            final StyledText styledText = sourceViewer.getTextWidget();
            Control parent = styledText;
            if (sourceViewer instanceof ITextViewerExtension) {
                final ITextViewerExtension extension = (ITextViewerExtension) sourceViewer;
                parent = extension.getControl();
            }
            parent.setRedraw(false);

            styledText.setFont(font);

            if (getVerticalRuler() instanceof IVerticalRulerExtension) {
                final IVerticalRulerExtension e = (IVerticalRulerExtension) getVerticalRuler();
                e.setFont(font);
            }

            provider.setSelection(selection);
            sourceViewer.setTopIndex(topIndex);

            if (parent instanceof Composite) {
                final Composite composite = (Composite) parent;
                composite.layout(true);
            }
            parent.setRedraw(true);
        } else {
            final StyledText styledText = sourceViewer.getTextWidget();
            styledText.setFont(font);
            if (getVerticalRuler() instanceof IVerticalRulerExtension) {
                final IVerticalRulerExtension e = (IVerticalRulerExtension) getVerticalRuler();
                e.setFont(font);
            }
        }
    }

    public void enableReconcilation() {
        final IReconciler reconciler = getSourceViewerConfiguration().getReconciler(getSourceViewer());
        reconciler.install(getSourceViewer());
    }

    public void disableReconcilation() {
        final IReconciler reconciler = getSourceViewerConfiguration().getReconciler(getSourceViewer());
        reconciler.uninstall();
    }
}
