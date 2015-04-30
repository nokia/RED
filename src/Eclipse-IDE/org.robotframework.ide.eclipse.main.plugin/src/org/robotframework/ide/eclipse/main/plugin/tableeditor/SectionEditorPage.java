package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.CommandContributionItem;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSectionCommand;

public abstract class SectionEditorPage extends FormPage {

    private RobotEditorCommandsStack commandsStack;
    
    private List<? extends IFormPart> pageParts;

    private IHyperlinkListener createSectionLinkListener;

    private final List<CommandContributionItem> updateNeededContributions = new ArrayList<>();

    public SectionEditorPage(final FormEditor editor, final String pageId, final String name) {
        super(editor, pageId, name);
    }

    @Override
    public boolean isEditor() {
        return true;
    }

    public abstract boolean isPartFor(RobotSuiteFileSection section);

    public abstract void revealElement(final RobotElement element);

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);
        prepareManagedForm(managedForm);

        pageParts = createPageParts(getEditorSite());
        prepareEclipseContext(pageParts);

        for (final IFormPart part : pageParts) {
            managedForm.addPart(part);
        }
        getSite().setSelectionProvider(getSelectionProvider());

        prepareCommandsContext();

        createToolbarActions();
        addFormMessages();
    }

    protected void prepareManagedForm(final IManagedForm managedForm) {
        final ScrolledForm form = managedForm.getForm();
        form.setImage(getTitleImage());
        form.setText(getPartName());
        managedForm.getToolkit().decorateFormHeading(form.getForm());

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(form.getBody());
        GridDataFactory.fillDefaults().applyTo(form.getBody());
    }

    protected abstract List<? extends IFormPart> createPageParts(IEditorSite iEditorSite);

    private void prepareEclipseContext(final List<? extends IFormPart> parts) {
        commandsStack = new RobotEditorCommandsStack();
        final IEclipseContext context = ((IEclipseContext) getSite().getService(IEclipseContext.class)).getActiveLeaf();
        context.set(RobotEditorCommandsStack.class, commandsStack);

        for (final IFormPart part : parts) {
            ContextInjectionFactory.inject(part, context);
        }
    }

    protected abstract ISelectionProvider getSelectionProvider();

    private void prepareCommandsContext() {
        final IContextService service = (IContextService) getSite().getService(IContextService.class);
        service.activateContext(getContextId());
    }

    protected abstract String getContextId();

    protected abstract String getSectionName();

    protected void createToolbarActions() {
        final ToolBarManager toolBarManager = (ToolBarManager) getManagedForm().getForm().getToolBarManager();

        final CommandContributionItem deleteSection = ToolbarContributions.createDeleteSectionContributionItem(
                getSite(), getSectionName());
        updateNeededContributions.add(deleteSection);
        toolBarManager.add(deleteSection);
        toolBarManager.update(true);
    }

    private void addFormMessages() {
        final RobotSuiteFile fileModel = ((RobotFormEditor) getEditor()).provideSuiteModel();
        final com.google.common.base.Optional<RobotElement> variablesSection = provideSection(fileModel);
        final Form form = getManagedForm().getForm().getForm();
        if (!variablesSection.isPresent() && fileModel.isEditable()) {
            createSectionLinkListener = createHyperlinkListener(fileModel);
            form.addMessageHyperlinkListener(createSectionLinkListener);
            form.setMessage("Section is not yet defined, do you want to create it?", IMessageProvider.ERROR);
        } else {
            form.removeMessageHyperlinkListener(createSectionLinkListener);
            if (((RobotSuiteFileSection) variablesSection.get()).isReadOnly()) {
                form.setMessage("Section is read-only!", IMessageProvider.WARNING);
            } else {
                form.setMessage(null, 0);
            }
        }
    }

    public abstract com.google.common.base.Optional<RobotElement> provideSection(final RobotSuiteFile suite);

    private HyperlinkAdapter createHyperlinkListener(final RobotSuiteFile suite) {
        return new HyperlinkAdapter() {
            @Override
            public void linkEntered(final HyperlinkEvent e) {
                ((Hyperlink) e.getSource()).setToolTipText("Click to create section");
            }

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                commandsStack.execute(new CreateSectionCommand(suite, getSectionName()));
            }
        };
    }

    @Override
    public void dispose() {
        super.dispose();

        updateNeededContributions.clear();
        if (commandsStack != null) {
            commandsStack.clear();
        }
        for (final IFormPart part : pageParts) {
            ContextInjectionFactory.uninject(part,
                ((IEclipseContext) getSite().getService(IEclipseContext.class)).getActiveLeaf());
        }
    }

    void updateMessages() {
        addFormMessages();
    }

    void updateToolbars() {
        for (final CommandContributionItem item : updateNeededContributions) {
            item.update();
        }
        getManagedForm().getForm().getToolBarManager().update(true);
    }
}
