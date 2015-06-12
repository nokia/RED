package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorSite;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.eclipse.main.plugin.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;

public class CasesEditorFormFragment implements ISectionFormFragment {

    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedFormToolkit toolkit;

    private RowExposingTableViewer viewer;

    @Override
    public void initialize(final Composite parent) {
        final SashForm mainSash = new SashForm(parent, SWT.SMOOTH | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainSash);
        toolkit.adapt(mainSash);
        toolkit.paintBordersFor(mainSash);

        viewer = new RowExposingTableViewer(mainSash, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(new CasesContentProvider());

        ViewerColumnsFactory.newColumn("").withWidth(150).labelsProvidedBy(new CasesNameLabelProvider())
        // .editingSupportedBy(new TestCaseNameEditingSupport(viewer,
        // commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable()).createFor(viewer);

        final Composite composite = toolkit.createComposite(mainSash);
        final Color separationColor = new Color(composite.getDisplay(), 240, 240, 240);
        composite.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                separationColor.dispose();
            }
        });
        composite.setBackground(separationColor);
        GridLayoutFactory.fillDefaults().extendedMargins(10, 0, 0, 0).applyTo(composite);

        final Composite caseComposite = toolkit.createComposite(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(caseComposite);

        mainSash.setWeights(new int[] { 15, 85 });

        setInput();
    }

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> casesSection = fileModel
                .findSection(RobotCasesSection.class);
        if (casesSection.isPresent()) {
            viewer.setInput(casesSection.get());
        } else {
            viewer.setInput(null);
            viewer.refresh();
        }
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }
}
