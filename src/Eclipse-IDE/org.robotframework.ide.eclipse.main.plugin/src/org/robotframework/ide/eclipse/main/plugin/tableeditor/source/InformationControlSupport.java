/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksListener;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport;

// supressing seems it semms that BrowserInformationControl will become part of the API in future
// eclipse anyway
@SuppressWarnings("restriction")
public class InformationControlSupport {

    private static boolean isBrowserBased = true;

    private final String caption;

    private IInformationControlCreator hoverControlCreator;
    private IInformationControlCreator focusedPopupControlCreator;

    public InformationControlSupport(final String caption) {
        this.caption = caption;
    }

    public boolean isBrowserBased() {
        return isBrowserBased;
    }

    public IInformationControlCreator getHoverControlCreator() {
        if (hoverControlCreator == null) {
            hoverControlCreator = new HoverControlCreator();
        }
        return hoverControlCreator;
    }

    private IInformationControlCreator getFocusedPopupControlCreator() {
        if (focusedPopupControlCreator == null) {
            focusedPopupControlCreator = new FocusedPopupControlCreator();
        }
        return focusedPopupControlCreator;
    }

    private class HoverControlCreator extends AbstractReusableInformationControlCreator {

        @Override
        protected IInformationControl doCreateInformationControl(final Shell parent) {
            isBrowserBased = BrowserInformationControl.isAvailable(parent);
            if (isBrowserBased) {
                return new HoverBrowserInformationControl(parent);
            } else {
                return new DefaultInformationControl(parent);
            }
        }
    }

    private class HoverBrowserInformationControl extends BrowserInformationControl {

        private HoverBrowserInformationControl(final Shell parent) {
            super(parent, JFaceResources.DEFAULT_FONT, caption);
        }

        @Override
        public IInformationControlCreator getInformationPresenterControlCreator() {
            return getFocusedPopupControlCreator();
        }

        @Override
        public Point computeSizeHint() {
            final Point size = super.computeSizeHint();
            size.x = Math.max(size.x, 400);
            return size;
        }
    }

    private static class FocusedPopupControlCreator extends AbstractReusableInformationControlCreator {

        @Override
        protected IInformationControl doCreateInformationControl(final Shell parent) {
            if (isBrowserBased) {
                return new FocusedPopupBrowserInformationControl(parent);
            } else {
                return new DefaultInformationControl(parent);
            }
        }
    }

    private static class FocusedPopupBrowserInformationControl extends BrowserInformationControl {

        private FocusedPopupBrowserInformationControl(final Shell parent) {
            super(parent, JFaceResources.DEFAULT_FONT, true);

            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final DocumentationsLinksSupport support = new DocumentationsLinksSupport(page, input -> {
                setInput(input.provideHtml());
            });
            addLocationListener(new DocumentationsLinksListener(support));
        }
    }
}
