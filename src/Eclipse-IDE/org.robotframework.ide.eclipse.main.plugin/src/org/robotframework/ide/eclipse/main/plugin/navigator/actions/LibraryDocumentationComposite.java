/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;
import org.robotframework.red.swt.Listeners;

class LibraryDocumentationComposite extends InputLoadingFormComposite {

    private InputLoadingFormComposite.InputJob collectingJob;

    private Label versionLabel;
    private Label scopeLabel;
    private ScrolledFormText documentationFormText;

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

        documentationFormText = new ScrolledFormText(actualComposite, SWT.V_SCROLL | SWT.H_SCROLL, true);
        getToolkit().adapt(documentationFormText);
        GridDataFactory.fillDefaults().span(2, 1).hint(400, 500).grab(true, true).applyTo(documentationFormText);

        final FormText docFormText = documentationFormText.getFormText();
        docFormText.setWhitespaceNormalized(false);
        docFormText.setFont("monospace", JFaceResources.getTextFont());
        docFormText.setFont("monospace_inline", JFaceResources.getTextFont());
        docFormText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        docFormText.setFont("header", JFaceResources.getBannerFont());

        final IHyperlinkListener hyperlinkListener = createHyperlinkListener();
        docFormText.addHyperlinkListener(hyperlinkListener);
        addDisposeListener(e -> docFormText.removeHyperlinkListener(hyperlinkListener));
        return actualComposite;
    }

    @Override
    protected void createActions() {
        addAction(new WrapFormAction(documentationFormText));
        super.createActions();
    }

    @Override
    protected Composite getControl() {
        return (Composite) super.getControl();
    }

    static IHyperlinkListener createHyperlinkListener() {
        return Listeners.linkActivatedAdapter(event -> {
            final Object href = event.getHref();
            if (href instanceof String) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL((String) href));
                } catch (PartInitException | MalformedURLException e) {
                    throw new IllegalStateException("Unable to open hyperlink: " + event.getLabel(), e);
                }
            }
        });
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

        documentationFormText.getFormText().setText(doc.text, doc.isHtml, true);
        documentationFormText.reflow(true);
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
