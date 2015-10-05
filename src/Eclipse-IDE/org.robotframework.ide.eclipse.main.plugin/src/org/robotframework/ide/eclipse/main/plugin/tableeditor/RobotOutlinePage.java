/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.NavigatorLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

class RobotOutlinePage extends ContentOutlinePage {

    private final RobotFormEditor editor;

    private final RobotSuiteFile suiteModel;

    private ISelectionChangedListener selectionListener;

    public RobotOutlinePage(final RobotFormEditor editor, final RobotSuiteFile suiteModel) {
        this.editor = editor;
        this.suiteModel = suiteModel;
    }

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);

        getTreeViewer().setContentProvider(new RobotOutlineContentProvider());
        ViewerColumnsFactory.newColumn("")
            .withWidth(400)
            .labelsProvidedBy(new NavigatorLabelProvider())
            .createFor(getTreeViewer());

        getTreeViewer().setInput(new Object[] { suiteModel });
        getTreeViewer().expandToLevel(3);

        selectionListener = createSelectionListener();
        getTreeViewer().addSelectionChangedListener(selectionListener);
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final Optional<RobotFileInternalElement> element = Selections.getOptionalFirstElement(
                        (IStructuredSelection) event.getSelection(), RobotFileInternalElement.class);
                if (!element.isPresent()) {
                    return;
                }
                final RobotFileInternalElement robotElement = element.get();
                if (editor.getActiveEditor() instanceof SuiteSourceEditor) {
                    final ISelectionProvider selectionProvider = editor.getActiveEditor()
                            .getSite()
                            .getSelectionProvider();
                    final Position position = robotElement.getDefinitionPosition();
                    final int offset = position.getOffset();
                    final int length = position.getLength();
                    selectionProvider.setSelection(new TextSelection(offset, length));
                } else {
                    robotElement.getOpenRobotEditorStrategy(getSite().getPage()).run();
                }
            }
        };
    }

    @Override
    public void dispose() {
        getTreeViewer().removeSelectionChangedListener(selectionListener);
        super.dispose();
    }
}
