/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 *
 */
class HeaderFilterSupport {

    private final Collection<HeaderFilterMatchesCollector> collectors;

    private Text filter;

    private final Form form;
    private final FormToolkit toolkit;

    private final IEventBroker broker;
    private final String topic;

    HeaderFilterSupport(final Form form, final FormToolkit toolkit, final IEventBroker broker,
            final String topic, final List<? extends HeaderFilterMatchesCollector> collectors) {
        this.form = form;
        this.toolkit = toolkit;
        this.broker = broker;
        this.topic = topic;
        this.collectors = newArrayList(collectors);
    }

    void enableFilter() {
        filter = toolkit.createText(form.getHead(), "");
        filter.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
        form.setHeadClient(filter);

        filter.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(final PaintEvent e) {
                if (filter.getText().isEmpty() && !filter.isFocusControl()) {
                    final Color current = e.gc.getForeground();
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
                    e.gc.drawText("filter elements", 3, 1);
                    e.gc.setForeground(current);
                }
            }
        });
        filter.addModifyListener(new ModifyListener() {

            private Job notifyingJob = null;

            @Override
            public void modifyText(final ModifyEvent e) {
                if (notifyingJob != null && notifyingJob.getState() == Job.SLEEPING) {
                    notifyingJob.cancel();
                }
                form.setBusy(true);
                notifyingJob = new Job("filtering section") {

                    @Override
                    protected IStatus run(final IProgressMonitor monitor) {
                        filter.getDisplay().syncExec(new Runnable() {

                            @Override
                            public void run() {
                                HeaderFilterMatchesCollection matches = null;
                                if (!filter.getText().isEmpty()) {
                                    matches = new HeaderFilterMatchesCollection();
                                    for (final HeaderFilterMatchesCollector collector : collectors) {
                                        matches.addAll(collector.collectMatches(filter.getText()));
                                    }
                                }
                                showProperTooltip(matches);
                                broker.send(topic, matches);
                            }
                        });
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                notifyingJob.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done(final IJobChangeEvent event) {
                        form.setBusy(false);
                    }
                });
                notifyingJob.schedule(350);
            }
        });
    }

    private void showProperTooltip(final HeaderFilterMatchesCollection matches) {
        final DefaultToolTip filterTip = new DefaultToolTip(filter, ToolTip.RECREATE, true);
        if (matches != null) {
            final int allMatches = matches.getNumberOfAllMatches();
            final int rowsMatching = matches.getNumberOfMatchingElement();
            final String elementForm = rowsMatching == 1 ? "element" : "elements";
            filterTip.setText("Filtering on: found " + allMatches + " match in " + rowsMatching + " " + elementForm);

            final Color filterSuccessFg = ColorsManager.getColor(0, 200, 0);
            final Color filterFailureFg = ColorsManager.getColor(255, 0, 0);
            filter.setForeground(allMatches == 0 ? filterFailureFg : filterSuccessFg);
            if (form.getMessage() == null) {
                form.setMessage("Filtering is enabled", IMessageProvider.INFORMATION);
            }
        } else {
            if (form.getMessage() != null && form.getMessage().startsWith("Filtering")) {
                form.setMessage(null);
            }
            filterTip.setText("Filtering off");
        }
        filterTip.setHideDelay(3000);
        filterTip.setImage(ImagesManager.getImage(RedImages.getFilterImage()));
        filterTip.show(new Point(0, filter.getSize().y));
    }

    void disableFilter() {
        filter.dispose();
        filter = null;
        form.setHeadClient(null);
        broker.send(topic, null);
    }

    void addFormMessageIfNeeded() {
        if (filter != null && !filter.getText().isEmpty() && form.getMessage() == null) {
            form.setMessage("Filtering is enabled", IMessageProvider.INFORMATION);
        }
    }
}
