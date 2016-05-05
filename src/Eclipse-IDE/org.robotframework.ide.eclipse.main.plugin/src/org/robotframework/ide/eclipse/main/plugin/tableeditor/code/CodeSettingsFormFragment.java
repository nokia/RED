/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesFilter;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

public abstract class CodeSettingsFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    protected RobotSuiteFile fileModel;

    @Inject
    protected RobotEditorCommandsStack commandsStack;

    @Inject
    protected RedFormToolkit toolkit;

    private StyledText documentation;

    private Job documentionChangeJob;

    private RowExposingTableViewer viewer;

    protected HeaderFilterMatchesCollection matches;

    private final String expandableSectionName;

    private final String description;

    private Section expandableSection;

    public CodeSettingsFormFragment(final String expandableSectionName, final String description) {
        this.expandableSectionName = expandableSectionName;
        this.description = description;
    }

    @Override
    public void initialize(final Composite parent) {
        final Composite detailsPanel = createDetailsPanel(parent);
        createViewer(detailsPanel);
        createColumns();

        setInput(com.google.common.base.Optional.<RobotCodeHoldingElement> absent());
    }

    private Composite createDetailsPanel(final Composite parent) {
        expandableSection = toolkit.createSection(parent,
                Section.DESCRIPTION | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        expandableSection.setExpanded(false);
        expandableSection.setText(String.format(expandableSectionName, ""));
        expandableSection.setDescription(description);
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(expandableSection);

        final Composite composite = toolkit.createComposite(expandableSection);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        expandableSection.setClient(composite);

        return composite;
    }

    private void createViewer(final Composite parent) {
        createDocumentationControl(parent);

        viewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(createContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
    }

    protected abstract IContentProvider createContentProvider();

    private void createDocumentationControl(final Composite panel) {
        documentation = new StyledText(panel, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        documentation.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(final PaintEvent e) {
                final StyledText text = (StyledText) e.widget;
                if (text.getText().isEmpty() && !text.isFocusControl()) {
                    final Color current = e.gc.getForeground();
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
                    e.gc.drawString("Documentation", 0, 0);
                    e.gc.setForeground(current);
                }
            }
        });
        documentation.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                documentation.redraw();
            }
            @Override
            public void focusLost(final FocusEvent e) {
                documentation.redraw();
            }
        });
        toolkit.adapt(documentation, true, false);
        toolkit.paintBordersFor(documentation);
        if (fileModel.isEditable()) {
            documentation.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    final RobotCodeHoldingElement codeElement = getCurrentCodeElement();
                    if (documentation.getText()
                            .equals(getDocumentation(com.google.common.base.Optional.fromNullable(codeElement)))) {
                        return;
                    }
                    dirtyProviderService.setDirtyState(true);

                    if (documentionChangeJob != null && documentionChangeJob.getState() == Job.SLEEPING) {
                        documentionChangeJob.cancel();
                    }
                    documentionChangeJob = createDocumentationChangeJob(codeElement, documentation.getText());
                    documentionChangeJob.schedule(300);
                }
            });
        }

        GridDataFactory.fillDefaults()
                .grab(true, true)
                .minSize(SWT.DEFAULT, 60)
                .hint(SWT.DEFAULT, 60)
                .applyTo(documentation);
    }

    private Job createDocumentationChangeJob(final RobotCodeHoldingElement codeElement, final String docu) {
        return new Job("changing documentation") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                if (codeElement == null) {
                    return Status.OK_STATUS;
                }

                final String newDocumentation = docu.replaceAll("\t", " ").replaceAll("  +", " ");
                final RobotDefinitionSetting docSetting = getDocumentationSetting(
                        com.google.common.base.Optional.fromNullable(codeElement));

                if (docSetting == null && !newDocumentation.isEmpty()) {
                    commandsStack.execute(createCommandForDocumentationCreation(codeElement, newDocumentation));
                } else if (docSetting != null && newDocumentation.isEmpty()) {
                    commandsStack.execute(new DeleteKeywordCallCommand(newArrayList(docSetting)));
                } else if (docSetting != null) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(docSetting, 0, newDocumentation));
                }
                return Status.OK_STATUS;
            }
        };
    }

    protected abstract EditorCommand createCommandForDocumentationCreation(
            final RobotCodeHoldingElement codeElement, final String newDocumentation);

    private void createColumns() {
        final Supplier<HeaderFilterMatchesCollection> matcherProvider = getMatchesProvider();
        createNameColumn(matcherProvider);

        for (int i = 0; i < calculateLongestArgumentsLength(); i++) {
            createArgumentColumn(i, matcherProvider);
        }
        createCommentColumn(matcherProvider);
    }

    private Supplier<HeaderFilterMatchesCollection> getMatchesProvider() {
        return new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
    }

    private int calculateLongestArgumentsLength() {
        return RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
    }

    private void createNameColumn(final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("Setting")
                .withMinWidth(50)
                .withWidth(150)
                .labelsProvidedBy(new CodeSettingsNamesLabelProvider(matcherProvider, prepareTooltips()))
                .createFor(viewer);
    }

    protected abstract Map<String, String> prepareTooltips();

    private void createArgumentColumn(final int index, final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("")
                .withWidth(120)
                .labelsProvidedBy(new CodeSettingsArgumentsLabelProvider(matcherProvider, index))
                .createFor(viewer);
    }

    private void createCommentColumn(final Supplier<HeaderFilterMatchesCollection> matcherProvider) {
        ViewerColumnsFactory.newColumn("Comment")
                .withMinWidth(50).withWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new CodeSettingsCommentsLabelProvider(matcherProvider))
                .createFor(viewer);
    }

    protected final void setInput(
            final com.google.common.base.Optional<? extends RobotCodeHoldingElement> codeElement) {
        documentation.setEditable(fileModel.isEditable() && codeElement.isPresent());
        viewer.getTable().setEnabled(fileModel.isEditable() && codeElement.isPresent());

        viewer.setInput(codeElement);
        documentation.setText(getDocumentation(codeElement));
        documentation.setSelection(documentation.getText().length());

        viewer.getTable().getParent().layout();
    }

    protected RobotCodeHoldingElement getCurrentCodeElement() {
        return (RobotCodeHoldingElement) ((com.google.common.base.Optional<?>) viewer.getInput()).orNull();
    }

    protected abstract RobotDefinitionSetting getDocumentationSetting(
            final com.google.common.base.Optional<? extends RobotCodeHoldingElement> codeElement);

    private String getDocumentation(
            final com.google.common.base.Optional<? extends RobotCodeHoldingElement> codeElement) {
        if (!codeElement.isPresent()) {
            return "";
        } else {
            final RobotDefinitionSetting docSetting = getDocumentationSetting(codeElement);
            return docSetting != null && !docSetting.getArguments().isEmpty() ? docSetting.getArguments().get(0) : "";
        }
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Persist
    public void onSave() {
        if (documentionChangeJob != null) {
            try {
                documentionChangeJob.join();
            } catch (final InterruptedException e) {
                RedPlugin.logError("Documentation change job was interrupted", e);
            }
        }
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final SettingsMatchesCollection settingsMatches = new SettingsMatchesCollection();
        final List<RobotElement> settings = getElementsForMatchesCollection();

        settingsMatches.collect(settings, filter);
        return settingsMatches;
    }

    protected abstract List<RobotElement> getElementsForMatchesCollection();

    protected final void handleFilteringRequest(final HeaderFilterMatchesCollection matches) {
        this.matches = matches;

        try {
            viewer.getTable().setRedraw(false);
            if (matches == null) {
                clearDocumentationMatches();
                viewer.setFilters(new ViewerFilter[0]);
            } else {
                setDocumentationMatches(matches);
                viewer.setFilters(new ViewerFilter[] { new SettingsMatchesFilter(matches) });
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    private void setDocumentationMatches(final HeaderFilterMatchesCollection settingsMatches) {
        clearDocumentationMatches();

        final Collection<Range<Integer>> ranges = settingsMatches.getRanges(documentation.getText());
        for (final Range<Integer> range : ranges) {
            final Color bg = ColorsManager.getColor(255, 255, 175);
            documentation.setStyleRange(
                    new StyleRange(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint(), null, bg));
        }
    }

    private void clearDocumentationMatches() {
        documentation.setStyleRange(null);
    }

    protected final void selectionInMainViewerHasChanged(final IStructuredSelection selection) {
        final Object selectedElement = Selections.getOptionalFirstElement(selection, Object.class).orNull();

        RobotCodeHoldingElement elementToShow = null;
        if (selectedElement instanceof RobotCodeHoldingElement) {
            elementToShow = (RobotCodeHoldingElement) selectedElement;
        } else if (selectedElement instanceof RobotKeywordCall) {
            final IRobotCodeHoldingElement parent = ((RobotKeywordCall) selectedElement).getParent();
            if (parent instanceof RobotCodeHoldingElement) {
                elementToShow = (RobotCodeHoldingElement) parent;
            }
        } else if (selectedElement instanceof ElementAddingToken) {
            final Object parent = ((ElementAddingToken) selectedElement).getParent();
            if (parent instanceof RobotCodeHoldingElement) {
                elementToShow = (RobotCodeHoldingElement) parent;
            }
        } else {
            setInput(com.google.common.base.Optional.<RobotCodeHoldingElement> absent());
        }

        final RobotCodeHoldingElement currentElement = getCurrentCodeElement();
        if (elementToShow != currentElement) {
            setInput(com.google.common.base.Optional.fromNullable(elementToShow));
            final String name = elementToShow == null ? "" : "'" + elementToShow.getName() + "'";
            expandableSection.setText(String.format(expandableSectionName, name));
        }
    }
}
