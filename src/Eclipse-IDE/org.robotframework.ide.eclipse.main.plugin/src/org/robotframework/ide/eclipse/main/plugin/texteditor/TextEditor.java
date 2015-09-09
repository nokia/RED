/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor;

import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.texteditor.AnnotationType;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.ContentAssistKeywordContext;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.DefaultContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.KeywordsContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.SettingsSectionContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TestCasesSectionContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.VariablesSectionContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.handlers.SaveAsHandler;
import org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting.RulesGenerator;
import org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting.TextEditorScanner;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.SharedTextColors;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorIndentLineAutoEditStrategy;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorOccurrenceMarksManager;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorPartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorSourceViewerConfiguration;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorStatusLineManager;
import org.robotframework.ide.eclipse.main.plugin.texteditor.utils.TextEditorTextHover;
import org.robotframework.red.graphics.ColorsManager;

/**
 * @author mmarzec
 *
 */
@SuppressWarnings("restriction")
public class TextEditor {

	@Inject
	ECommandService commandService;
	
	@Inject
	EHandlerService handlerService;
	
	@Inject
	IDirtyProviderService dirtyProviderService;
	
	private IFile editedFile;
	private IEditorInput input;
	private SourceViewer viewer;
	
	private int breakpointLine = 0; 
	
	private ResourceMarkerAnnotationModel markerAnnotationModel;
	
	private CompositeRuler compositeRuler;

    private TextEditorTextHover textHover;
    
    private WhitespaceCharacterPainter whitespacePainter;
    
    private TextEditorOccurrenceMarksManager occurrenceMarksManager;
    
    private RulesGenerator rulesGenerator;
    
    private TextEditorIndentLineAutoEditStrategy indentLineAutoEditStrategy;
    
    private TextEditorStatusLineManager statusLineManager;
    
	@PostConstruct
	public void postConstruct(final Composite parent, final IEditorInput input, final IEditorPart editorPart) {
	    this.input = input;

		final FillLayout layout = new FillLayout();
		layout.marginHeight = 1;
        parent.setLayout(layout);
        
        final ParameterizedCommand saveAsCommand = this.createSaveAsCommand();
        
        FileEditorInput fileEditorInput = null;
        if(input instanceof FileEditorInput) {
        	fileEditorInput = (FileEditorInput) input;
        	editedFile = fileEditorInput.getFile();
        } else if (input instanceof FileRevisionEditorInput) {
        	final FileRevisionEditorInput historyEditor = (FileRevisionEditorInput) input;
        	
        	final IWorkspace workspace= ResourcesPlugin.getWorkspace(); 
        	try {
        		editedFile = workspace.getRoot().getFile(historyEditor.getStorage().getFullPath());
			} catch (final CoreException e1) {
				e1.printStackTrace();
				return;
			}
        } else {
            // FIXME : there are other inputs when editor is opened via history
            // for example!
            return;
        }
        
        final String text = this.extractTextFromFile();
        
        //this.deleteMarkersFromFile();
		//this.createMarkers();
        
		final AnnotationType errorAnnotationType = new AnnotationType("org.eclipse.ui.workbench.texteditor.error", null);
		final AnnotationType breakpointAnnotationType = new AnnotationType("org.eclipse.debug.core.breakpoint", null);
		final AnnotationType occurrencesMarkAnnotationType = new AnnotationType("org.robotframework.ide.texteditor.occurrencesMark", null);
		
		final DefaultMarkerAnnotationAccess markerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		markerAnnotationModel = new ResourceMarkerAnnotationModel(editedFile);
		
		compositeRuler = new CompositeRuler(1);
		compositeRuler.setModel(markerAnnotationModel);
		
		final OverviewRuler overviewRuler = new OverviewRuler(markerAnnotationAccess, 15, new SharedTextColors());
		overviewRuler.addAnnotationType(errorAnnotationType.getType());
		overviewRuler.addHeaderAnnotationType(errorAnnotationType.getType());
		overviewRuler.addAnnotationType(occurrencesMarkAnnotationType.getType());
        overviewRuler.setModel(markerAnnotationModel);
		
		//TODO move SourceViewer initialization to other class
		viewer = new SourceViewer(parent, compositeRuler, overviewRuler, true, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setEditable(true);
		viewer.getTextWidget().setFont(JFaceResources.getTextFont());
		
		final AnnotationRulerColumn annotationColumn = new AnnotationRulerColumn(15,markerAnnotationAccess);
		annotationColumn.addAnnotationType(errorAnnotationType.getType());
		annotationColumn.addAnnotationType(breakpointAnnotationType.getType());
		annotationColumn.addAnnotationType(occurrencesMarkAnnotationType.getType());
		
		final LineNumberRulerColumn lineNumberColumn = new LineNumberRulerColumn();
        lineNumberColumn.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        lineNumberColumn.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		lineNumberColumn.setFont(JFaceResources.getTextFont());
		
		viewer.addVerticalRulerColumn(annotationColumn);
		viewer.addVerticalRulerColumn(lineNumberColumn);
		
		final SourceViewerDecorationSupport decorationSupport = new SourceViewerDecorationSupport(viewer, overviewRuler, markerAnnotationAccess, new SharedTextColors());
		final IPreferenceStore editorsPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.editors");
		final AnnotationPreference occurrencesMarkAnnotationPreference = this.createOccurrencesMarkAnnotationPreference(occurrencesMarkAnnotationType);
		decorationSupport.setAnnotationPreference(occurrencesMarkAnnotationPreference);
		final AnnotationPreference errorAnnotationPreference = this.createErrorAnnotationPreference(errorAnnotationType);
		decorationSupport.setAnnotationPreference(errorAnnotationPreference);
		decorationSupport.install(editorsPreferenceStore);
		
		final IDocument document = new Document();
		document.set( text );
		viewer.setDocument( document, markerAnnotationModel );
		
		final Menu contextMenu = this.createContextMenu(lineNumberColumn, saveAsCommand);

		final TextViewerUndoManager undoManager = new TextViewerUndoManager(5);
		viewer.setUndoManager(undoManager);
		undoManager.connect(viewer);
		
		final RobotSuiteFile suiteFile = RedPlugin.getModelManager().createSuiteFile(editedFile);
        final Map<String, ContentAssistKeywordContext> keywordMap = provideContentAssistantKeywordsMapping(suiteFile);
        
        textHover = new TextEditorTextHover(keywordMap);
        indentLineAutoEditStrategy = new TextEditorIndentLineAutoEditStrategy();
        final TextEditorSourceViewerConfiguration svc = new TextEditorSourceViewerConfiguration(textHover, indentLineAutoEditStrategy);
        viewer.configure(svc);
        
        createPartitioner(document);
        
		final ContentAssistant contentAssistant = this.createContentAssistant(keywordMap);
		contentAssistant.install(viewer);
		
		final List<String> keywordList = new ArrayList<String>(keywordMap.keySet());
		final PresentationReconciler reconciler = this.createPresentationReconciler(keywordList);
		reconciler.install(viewer);
		
		occurrenceMarksManager = new TextEditorOccurrenceMarksManager(viewer, editedFile);
		
		activateFindReplaceAction(parent, editorPart);
		
		//TODO Add Listener classes
		viewer.getTextWidget().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				dirtyProviderService.setDirtyState(true);
				occurrenceMarksManager.removeOldOccurrenceMarks();
			}
		});
		
		viewer.getTextWidget().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent e) {
			    if (e.keyCode == SWT.SHIFT) {
                    indentLineAutoEditStrategy.setShiftPresssed(false);
                }
			}
			@Override
			public void keyPressed(final KeyEvent e) {
			    if (e.keyCode == SWT.SHIFT) {
			        indentLineAutoEditStrategy.setShiftPresssed(true);
			    }
			    
				if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'z')) {
					if(undoManager.undoable()) {
						undoManager.undo();
						dirtyProviderService.setDirtyState(true);
					}
				}
				if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'y')) {
					if(undoManager.redoable()) {
						undoManager.redo();
						dirtyProviderService.setDirtyState(true);
					}
				}
				if ((e.stateMask == SWT.CTRL) && (e.keyCode == 's') && editorPart.isDirty()) {
					save(null);
				}
				if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.SPACE)) {
                    if (viewer.getTextWidget().getLineCount() == viewer.getTextWidget().getLineAtOffset(
                            ((TextSelection) viewer.getSelection()).getOffset())+1) {
                        viewer.getTextWidget().append("\n");
                    }
				    contentAssistant.showPossibleCompletions();
				}
				if((e.stateMask & SWT.CTRL) != 0 && (e.stateMask & SWT.SHIFT) != 0 && e.keyCode == 'v') {
					contentAssistant.showContextInformation();
				}
			}
		});
		
        viewer.getTextWidget().addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(final MouseEvent e) {
                if (e.button != 3 && e.count == 1) {
                    final Point point = viewer.getTextWidget().getSelection();
                    occurrenceMarksManager.showOccurrenceMarks(point.x);
                }
            }

            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button == 3) {
                    contextMenu.setVisible(true);
                }
            }

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
            }
        });
		
        compositeRuler.getControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                try {
                    final int line = computeBreakpointLineNumber(e.y);
                    
                    if (line > viewer.getTextWidget().getLineCount()) {
                        return;
                    }
                    
                    final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
                    
                    for (final IBreakpoint point : breakpointManager.getBreakpoints()) {
                        if (point.getMarker().getResource().equals(editedFile)
                                && point.getMarker().getAttribute(IMarker.LINE_NUMBER, -1) == line) {
                            breakpointManager.removeBreakpoint(point, true);
                            return;
                        }
                    }
                    breakpointManager.addBreakpoint(new RobotLineBreakpoint(editedFile, line));
                } catch (final CoreException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        statusLineManager = new TextEditorStatusLineManager(editorPart, viewer.getDocument());
        viewer.getTextWidget().addCaretListener(new CaretListener() {

            @Override
            public void caretMoved(CaretEvent event) {
                statusLineManager.updatePosition(event.caretOffset);
            }
        });
	}

    private Map<String, ContentAssistKeywordContext> provideContentAssistantKeywordsMapping(
            final RobotSuiteFile suiteFile) {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteFile);
        final List<RedKeywordProposal> keywordProposals = proposals.getKeywordProposals(sortedByNames());
        
        final Map<String, ContentAssistKeywordContext> mapping = new LinkedHashMap<>();
        for (final RedKeywordProposal proposal : keywordProposals) {
            mapping.put(proposal.getLabel(), new ContentAssistKeywordContext(proposal));
        }
        return mapping;
    }
	
	@Persist
	public void save(final IProgressMonitor monitor) {
		
		dirtyProviderService.setDirtyState(false);
		
        try (InputStream is = new ByteArrayInputStream(viewer.getDocument().get().getBytes())) {
            editedFile.setContents(is, false, true, monitor);
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        }
		
        updateMarkersPositions();
	}
	
	@Focus
	public void onFocus() {
        viewer.getTextWidget().setFocus();
        statusLineManager.updatePosition(viewer.getTextWidget().getCaretOffset());
	}
	
	@SuppressWarnings("unchecked")
    @Inject
    @Optional
    private void highlightLineEvent(@UIEventTopic("TextEditor/HighlightLine") final org.osgi.service.event.Event event,
            @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {
	    if(((String) event.getProperty("file")).equals(editedFile.getName())) {
            if (editor != null) {
                editor.activateSourcePage();
            }
            textHover.setDebugVariables((Map<String, Object>) event.getProperty("vars"));
	        final int line = Integer.parseInt((String) event.getProperty("line"));
	        if(line > 0) {
                viewer.getTextWidget().setLineBackground(breakpointLine, 1, null);
                viewer.getTextWidget().setLineBackground(line - 1, 1, ColorsManager.getColor(198, 219, 174));
                showHighlightedLine(line);
                compositeRuler.immediateUpdate();
                breakpointLine = line - 1;
	        }
	    }
    }
	
	private void showHighlightedLine(final int lineNumber) {
        final int visibleLinesNumber = viewer.getTextWidget().getClientArea().height
                / viewer.getTextWidget().getLineHeight();
        if (visibleLinesNumber < 2) {
            viewer.getTextWidget().setTopIndex(lineNumber - 1);
            return;
        }
        final int linesToCenter = visibleLinesNumber / 2;
        final int topIndexPosiiton = lineNumber - linesToCenter;
        if (topIndexPosiiton >= 0) {
            viewer.getTextWidget().setTopIndex(topIndexPosiiton);
        } else {
            viewer.getTextWidget().setTopIndex(lineNumber - 1);
        }
	}

    @Inject
    @Optional
    private void clearHighlightedLineEvent(@UIEventTopic("TextEditor/ClearHighlightedLine") final String file,
            @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {
        if ("".equals(file) || editedFile.getName().equals(file)) {
            textHover.setDebugVariables(null);
            
            editor.activateSourcePage();
            viewer.getTextWidget().setLineBackground(breakpointLine, 1, null);
	        breakpointLine = 0;
	    }
    }
    
    @Inject
    @Optional
    private void showTestCaseEvent(@UIEventTopic("TextEditor/ShowTestCase") final org.osgi.service.event.Event event) {
        if (editedFile.getName().equals(event.getProperty("fileName"))) {
            try {
                final IRegion resultRegion = new FindReplaceDocumentAdapter(viewer.getDocument()).find(0,
                        "^" + event.getProperty("testCaseName") + "\\b", true, true, false, true);
                if (resultRegion != null) {
                    final int topIndexPosition = viewer.getDocument().getLineOfOffset(resultRegion.getOffset());
                    viewer.getTextWidget().setTopIndex(topIndexPosition);
                    viewer.getTextWidget().setSelection(resultRegion.getOffset(),
                            resultRegion.getOffset() + resultRegion.getLength());
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
	
	private ParameterizedCommand createSaveAsCommand() {
		ParameterizedCommand cmd = null;
		final Command saveAsCommand = commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE_AS);
		if (saveAsCommand.isDefined()) {
			handlerService.activateHandler(IWorkbenchCommandConstants.FILE_SAVE_AS,new SaveAsHandler());
			cmd = commandService.createCommand(IWorkbenchCommandConstants.FILE_SAVE_AS, null);
			handlerService.canExecute(cmd);
		}
		return cmd;
	}
	
    private void activateFindReplaceAction(final Composite parent, final IEditorPart editorPart) {

        final FindReplaceAction findAction = new FindReplaceAction(
                ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedTextEditorMessages"), null,
                parent.getShell(), viewer.getFindReplaceTarget());
        final IHandlerService hs = (IHandlerService) editorPart.getSite().getService(IHandlerService.class);
        final IHandler findReplaceHandler = new AbstractHandler() {

            @Override
            public Object execute(final ExecutionEvent event) throws ExecutionException {
                if (viewer != null && viewer.getDocument() != null) {
                    findAction.run();
                }
                return null;
            }
        };
        hs.activateHandler(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, findReplaceHandler);
    }
	
	private String extractTextFromFile() {
		Scanner scanner = null;
        final StringBuilder text = new StringBuilder("");
        try (InputStream is = editedFile.getContents()) {
            scanner = new Scanner(is).useDelimiter("\\n");
            while (scanner.hasNext()) {
                text.append(scanner.next() + "\n");
            }
        } catch (CoreException | IOException e) {
            throw new TextEditorInitializationException("Unable to get file content", e);
        }
        if (scanner != null) {
            scanner.close();
        }
		return text.toString();
	}
	
	private void deleteMarkersFromFile() {
		try {
			final IMarker[] markers = editedFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
			for (int i = 0; i < markers.length; i++) {
				markers[i].delete();
			}
		} catch (final CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void updateMarkersPositions() {
        try {
            if (markerAnnotationModel != null) {
                markerAnnotationModel.updateMarkers(viewer.getDocument());
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
	
	private AnnotationPreference createErrorAnnotationPreference(final AnnotationType type) {
		final AnnotationPreference annotationPreference = new AnnotationPreference();
		annotationPreference.setAnnotationType(type.getType());
		annotationPreference.setTextStylePreferenceKey("errorTextStyle");
		annotationPreference.setTextStyleValue("UNDERLINE");
		annotationPreference.setOverviewRulerPreferenceKey("errorIndicationInOverviewRuler");
		annotationPreference.setOverviewRulerPreferenceValue(true);
		annotationPreference.setTextPreferenceKey("errorIndication");
		annotationPreference.setTextPreferenceValue(true);
		annotationPreference.setColorPreferenceKey("errorIndicationColor");
		annotationPreference.setColorPreferenceValue(new RGB(255, 0, 0));
		annotationPreference.setPresentationLayer(6);
		return annotationPreference;
	}
	
	private AnnotationPreference createOccurrencesMarkAnnotationPreference(final AnnotationType type) {
        final AnnotationPreference annotationPreference = new AnnotationPreference();
        annotationPreference.setAnnotationType(type.getType());
        annotationPreference.setOverviewRulerPreferenceKey("org.robotframework.ide.texteditor.occurrencesMark.overview");
        annotationPreference.setOverviewRulerPreferenceValue(true);
        annotationPreference.setTextPreferenceKey("org.robotframework.ide.texteditor.occurrencesMark.text");
        annotationPreference.setTextPreferenceValue(true);
        annotationPreference.setColorPreferenceKey("org.robotframework.ide.texteditor.occurrencesMark.color");
        annotationPreference.setColorPreferenceValue(new RGB(212, 212, 212));
        annotationPreference.setHighlightPreferenceKey("org.robotframework.ide.texteditor.occurrencesMark.highlight");
        annotationPreference.setHighlightPreferenceValue(true);
        annotationPreference.setVerticalRulerPreferenceKey("org.robotframework.ide.texteditor.occurrencesMark.ruler");
        annotationPreference.setVerticalRulerPreferenceValue(false);
        annotationPreference.setPresentationLayer(6);
        annotationPreference.setContributesToHeader(false);
        return annotationPreference;
    }
	
	private Menu createContextMenu(final LineNumberRulerColumn lineNumberColumn, final ParameterizedCommand saveAsCommand) {
		final Menu menu = new Menu(viewer.getControl());
		
		final MenuItem cutItem = new MenuItem(menu, SWT.PUSH);
		cutItem.setText("Cut\tCtrl+X");
		cutItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (viewer.getTextWidget().getSelectionCount() > 0) {
                    viewer.getTextWidget().cut();
                }
            }
        });
        final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText("Copy\tCtrl+C");
        copyItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (viewer.getTextWidget().getSelectionCount() > 0) {
                    viewer.getTextWidget().copy();
                }
            }
        });
        final MenuItem pasteItem = new MenuItem(menu, SWT.PUSH);
        pasteItem.setText("Paste\tCtrl+V");
        pasteItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewer.getTextWidget().paste();
            }
        });
        new MenuItem(menu, SWT.SEPARATOR);
		final MenuItem saveAsItem = new MenuItem(menu, SWT.PUSH);
		saveAsItem.setText("Save as...");
		saveAsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				handlerService.executeHandler(saveAsCommand); 
			}
		});
		
		final MenuItem breakpointItem = new MenuItem(menu, SWT.PUSH);
		breakpointItem.setText("Add breakpoint");
		breakpointItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                try {
                    final int line = viewer.getTextWidget().getLineAtOffset(viewer.getTextWidget().getSelection().x);
                    //TODO: check in model if the line can have a breakpoint 
                    final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
                    boolean hasBreakpoint = false;
                    for (final IBreakpoint point : breakpointManager.getBreakpoints()) {
                        if (point.getMarker().getResource().equals(editedFile)
                                && point.getMarker().getAttribute(IMarker.LINE_NUMBER, -1) == (line + 1)) {
                            hasBreakpoint = true;
                            break;
                        }
                    }

                    if (!hasBreakpoint) {
                        breakpointManager.addBreakpoint(new RobotLineBreakpoint(
                                (IResource) input.getAdapter(IResource.class), line + 1));
                    }
                } catch (final CoreException e1) {
                    e1.printStackTrace();
                }
            }
        });
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		final MenuItem toggleLineNumberColumnItem = new MenuItem(menu, SWT.CHECK);
		toggleLineNumberColumnItem.setSelection(true);
		toggleLineNumberColumnItem.setText("Show line numbers");
        toggleLineNumberColumnItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if(toggleLineNumberColumnItem.getSelection()) {
					viewer.addVerticalRulerColumn(lineNumberColumn);
				} else {
					viewer.removeVerticalRulerColumn(lineNumberColumn);
				}
			}
		});
        final MenuItem showWhitespacesItem = new MenuItem(menu, SWT.CHECK);
        showWhitespacesItem.setSelection(false);
        showWhitespacesItem.setText("Show whitespace characters");
        showWhitespacesItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (showWhitespacesItem.getSelection()) {
                    if (whitespacePainter == null) {
                        whitespacePainter = new WhitespaceCharacterPainter(viewer);
                        viewer.addPainter(whitespacePainter);
                    }
                } else {
                    viewer.removePainter(whitespacePainter);
                    whitespacePainter = null;
                }
            }
        });
        final MenuItem enableBlockSelectionItem = new MenuItem(menu, SWT.CHECK);
        enableBlockSelectionItem.setSelection(false);
        enableBlockSelectionItem.setText("Enable block selection");
        enableBlockSelectionItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (enableBlockSelectionItem.getSelection()) {
                    viewer.getTextWidget().setBlockSelection(true);
                } else {
                    viewer.getTextWidget().setBlockSelection(false);
                }
            }
        });
		return menu;
	}
	
	private ContentAssistant createContentAssistant(final Map<String, ContentAssistKeywordContext> keywordMap) {
		final ContentAssistant contentAssistant = new ContentAssistant();
		contentAssistant.enableColoredLabels(true);
		contentAssistant.enableAutoInsert(true);
		contentAssistant.enablePrefixCompletion(true);
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setEmptyMessage("No proposals");
		contentAssistant.setShowEmptyList(true);
		contentAssistant.setContentAssistProcessor(new TestCasesSectionContentAssistProcessor(keywordMap), TextEditorPartitionScanner.TEST_CASES_SECTION);
		contentAssistant.setContentAssistProcessor(new KeywordsContentAssistProcessor(keywordMap), TextEditorPartitionScanner.KEYWORDS_SECTION);
		contentAssistant.setContentAssistProcessor(new SettingsSectionContentAssistProcessor(), TextEditorPartitionScanner.SETTINGS_SECTION);
		contentAssistant.setContentAssistProcessor(new VariablesSectionContentAssistProcessor(), TextEditorPartitionScanner.VARIABLES_SECTION);
		contentAssistant.setContentAssistProcessor(new DefaultContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_BELOW);
		contentAssistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
            @Override
            protected IInformationControl doCreateInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });
		return contentAssistant;
	}
	
	private void createPartitioner(final IDocument document) {
        IDocumentPartitioner partitioner = new FastPartitioner(new TextEditorPartitionScanner(), new String[] {
                TextEditorPartitionScanner.TEST_CASES_SECTION, TextEditorPartitionScanner.KEYWORDS_SECTION,
                TextEditorPartitionScanner.SETTINGS_SECTION, TextEditorPartitionScanner.VARIABLES_SECTION });
        partitioner.connect(document);
	    document.setDocumentPartitioner(partitioner);
	}
	
    private PresentationReconciler createPresentationReconciler(final List<String> keywordList) {
        final PresentationReconciler reconciler = new PresentationReconciler();
        rulesGenerator = new RulesGenerator(viewer.getTextWidget().getDisplay(), keywordList);
        setupPresentationReconciler(reconciler, TextEditorPartitionScanner.TEST_CASES_SECTION, new TextEditorScanner(
                rulesGenerator.getTestCasesSectionRules()));
        setupPresentationReconciler(reconciler, TextEditorPartitionScanner.KEYWORDS_SECTION, new TextEditorScanner(
                rulesGenerator.getKeywordsSectionRules()));
        setupPresentationReconciler(reconciler, TextEditorPartitionScanner.SETTINGS_SECTION, new TextEditorScanner(
                rulesGenerator.getSettingsSectionRules()));
        setupPresentationReconciler(reconciler, TextEditorPartitionScanner.VARIABLES_SECTION, new TextEditorScanner(
                rulesGenerator.getVariablesSectionRules()));
        setupPresentationReconciler(reconciler, IDocument.DEFAULT_CONTENT_TYPE,
                new TextEditorScanner(rulesGenerator.getDefaultRules()));
        return reconciler;
    }
	
    private void setupPresentationReconciler(final PresentationReconciler reconciler, final String contentType,
            final RuleBasedScanner scanner) {
        final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
        reconciler.setDamager(dr, contentType);
        reconciler.setRepairer(dr, contentType);
    }
	
	private int computeBreakpointLineNumber(final int eventY) {
        final int lineHeight = viewer.getTextWidget().getLineHeight();
        int line = (int) Math.round((eventY / (double) lineHeight)) + viewer.getTopIndex();

        final int lineTopPixel = viewer.getTextWidget().getLinePixel(line);
        final int lineBottomPixel = lineTopPixel - lineHeight;
        if (eventY < lineBottomPixel) {
            line--;
        } else if ((eventY - lineBottomPixel) > lineHeight) {
            line++;
        }
        return line;
    }

    @PreDestroy
    public void preDestroy() {
        if (rulesGenerator != null) {
            rulesGenerator.disposeResources();
        }
        
        occurrenceMarksManager.removeOldOccurrenceMarks();
    }

    public class TextEditorInitializationException extends RuntimeException {

        public TextEditorInitializationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
