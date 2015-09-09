/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;

class LibraryDocumentationComposite extends InputLoadingFormComposite {

    private InputLoadingFormComposite.InputJob collectingJob;
    private Label versionLabel;
    private Label scopeLabel;
    private FormText documentationText;
    private ScrolledFormText scrolledFormText;


    LibraryDocumentationComposite(final Composite parent, final LibrarySpecification specification) {
        super(parent, SWT.NONE, specification.getName() + " library documentation");
        this.collectingJob = new InputLoadingFormComposite.InputJob("Loading library documentation") {
            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return new Documentation(specification);
            }
        };
        createComposite();
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RedImages.getBookImage());

        final Composite actualComposite = getToolkit().createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(actualComposite);

        final Label version = getToolkit().createLabel(actualComposite, "Version");
        version.setFont(JFaceResources.getHeaderFont());
        version.setForeground(getToolkit().getColors().getColor(IFormColors.TITLE));
        versionLabel = getToolkit().createLabel(actualComposite, "");
        versionLabel.setFont(JFaceResources.getHeaderFont());
        versionLabel.setForeground(getToolkit().getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(versionLabel);

        final Label scope = getToolkit().createLabel(actualComposite, "Scope");
        scope.setFont(JFaceResources.getBannerFont());
        scope.setForeground(getToolkit().getColors().getColor(IFormColors.TITLE));
        scopeLabel = getToolkit().createLabel(actualComposite, "");
        scopeLabel.setFont(JFaceResources.getBannerFont());
        scopeLabel.setForeground(getToolkit().getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(scopeLabel);

        final Label separator = getToolkit().createSeparator(actualComposite, SWT.HORIZONTAL | SWT.SHADOW_OUT);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(separator);

        scrolledFormText = new ScrolledFormText(actualComposite, SWT.V_SCROLL | SWT.H_SCROLL, true);
        getToolkit().adapt(scrolledFormText);
        GridDataFactory.fillDefaults().span(2, 1).hint(400, 500).grab(true, true).applyTo(scrolledFormText);
        GridLayoutFactory.fillDefaults().applyTo(scrolledFormText);

        documentationText = scrolledFormText.getFormText();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(documentationText);
        documentationText.setWhitespaceNormalized(false);

        documentationText.setFont("monospace", JFaceResources.getTextFont());
        documentationText.setFont("monospace_inline", JFaceResources.getTextFont());
        documentationText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        documentationText.setFont("header", JFaceResources.getBannerFont());

        final HyperlinkAdapter hyperlinkListener = createHyperlinkListener();
        documentationText.addHyperlinkListener(hyperlinkListener);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                documentationText.removeHyperlinkListener(hyperlinkListener);
            }
        });
        return actualComposite;
    }

    @Override
    protected Composite getControl() {
        return (Composite) super.getControl();
    }

    private HyperlinkAdapter createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent event) {
                final Object href = event.getHref();
                if (href instanceof String) {
                    try {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                                .openURL(new URL((String) href));
                    } catch (PartInitException | MalformedURLException e) {
                        throw new IllegalStateException("Unable to open hyperlink: " + event.getLabel(), e);
                    }
                }
            }
        };
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        return collectingJob;
    }

    @Override
    protected void fillControl(final Object jobResult) {
        final Documentation doc = (Documentation) jobResult;

        versionLabel.setText(doc.version);
        scopeLabel.setText(doc.scope);
        documentationText.setText(doc.text, doc.isHtml, true);

        scrolledFormText.reflow(true);
        getControl().layout();
    }

    private class Documentation {
        private final String version;
        private final String scope;
        private final String text;
        private final boolean isHtml;

        public Documentation(final LibrarySpecification spec) {
            version = spec.getVersion().isEmpty() ? "unspecified" : spec.getVersion();
            scope = spec.getScope();
            isHtml = spec.canBeConvertedToHtml();
            if (isHtml) {
                text = "<form>" + spec.getDocumentationAsHtml() + "</form>";
            } else {
                text = spec.getDocumentation();
            }
        }
    }
}
