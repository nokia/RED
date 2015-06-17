package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.Form;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectConfigurationEditorPart.ProjectConfigurationEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;

public class RedProjectConfigurationEditorPart extends DIEditorPart<ProjectConfigurationEditor> {

    public RedProjectConfigurationEditorPart() {
        super(ProjectConfigurationEditor.class);
    }

    public static class ProjectConfigurationEditor {

        protected RedFormToolkit toolkit;

        private List<? extends ISectionFormFragment> formFragments;

        private IEclipseContext context;

        private FrameworksSectionFormFragment frameworksFragment;

        @PostConstruct
        public final void postConstruct(final Composite parent, final IEditorPart editorPart) {
            adjustParentLayout(parent);
            toolkit = createToolkit(parent);

            final IEditorSite site = editorPart.getEditorSite();
            context = ((IEclipseContext) site.getService(IEclipseContext.class)).getActiveLeaf();
            context.set(RedFormToolkit.class, toolkit);
            context.set(IDirtyProviderService.class, context.get(IDirtyProviderService.class));

            final Form form = createForm(parent, editorPart.getTitleImage());
            context.set(Form.class, form);

            formFragments = createFormFragments();
            injectToFormParts(context, formFragments);
            for (final ISectionFormFragment part : formFragments) {
                part.initialize(form.getBody());
            }
            site.setSelectionProvider(getSelectionProvider());
        }

        private void adjustParentLayout(final Composite parent) {
            final FillLayout parentLayout = (FillLayout) parent.getLayout();
            parentLayout.marginHeight = 0;
            parentLayout.marginWidth = 0;
        }

        private RedFormToolkit createToolkit(final Composite parent) {
            final RedFormToolkit toolkit = new RedFormToolkit(parent.getDisplay());
            parent.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(final DisposeEvent e) {
                    toolkit.dispose();
                }
            });
            return toolkit;
        }

        protected List<? extends ISectionFormFragment> createFormFragments() {
            frameworksFragment = new FrameworksSectionFormFragment();
            return newArrayList(frameworksFragment, new ReferencedLibrariesFormFragment());
        }

        private void injectToFormParts(final IEclipseContext context,
                final List<? extends ISectionFormFragment> sectionForms) {
            for (final ISectionFormFragment part : sectionForms) {
                ContextInjectionFactory.inject(part, context);
            }
        }

        private Form createForm(final Composite parent, final Image image) {
            final Form form = toolkit.createForm(parent);
            form.setImage(image);
            form.setText("RED Project");
            toolkit.decorateFormHeading(form);

            GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).margins(3, 3).applyTo(form.getBody());
            return form;
        }

        protected ISelectionProvider getSelectionProvider() {
            return frameworksFragment.getViewer();
        }

        @Focus
        public void setFocus() {
            if (!formFragments.isEmpty()) {
                formFragments.get(0).setFocus();
            }
        }

        @Persist
        public void onSave() {
            final IDirtyProviderService dirtyProviderService = context.getActive(IDirtyProviderService.class);
            dirtyProviderService.setDirtyState(false);

            for (final ISectionFormFragment fragment : formFragments) {
                ContextInjectionFactory.invoke(fragment, Persist.class, context, context, null);
            }
        }

        @PreDestroy
        public final void preDestroy() {
            for (final ISectionFormFragment fragment : formFragments) {
                ContextInjectionFactory.uninject(fragment, context);
            }
        }
    }
}
