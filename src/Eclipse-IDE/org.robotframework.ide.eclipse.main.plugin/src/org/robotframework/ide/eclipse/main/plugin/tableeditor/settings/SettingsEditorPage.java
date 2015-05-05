package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPage;

import com.google.common.base.Optional;

public class SettingsEditorPage extends SectionEditorPage {

    private static final String ID = "org.robotframework.ide.eclipse.editor.settingsPage";
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.settings.context";

    private GeneralSettingsFormPart generalPart;
    private MetadataSettingsFormPart metadataPart;
    private ImportsSettingsFormPart importsPart;

    public SettingsEditorPage(final FormEditor editor) {
        super(editor, ID, RobotSuiteSettingsSection.SECTION_NAME);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return section instanceof RobotSuiteSettingsSection;
    }

    @Override
    protected void prepareManagedForm(final IManagedForm managedForm) {
        super.prepareManagedForm(managedForm);
        final ScrolledForm form = managedForm.getForm();

        GridDataFactory.fillDefaults().applyTo(form.getBody());
        GridLayoutFactory.fillDefaults().applyTo(form.getBody());

        final FormToolkit toolkit = managedForm.getToolkit();

        final SashForm mainSash = new SashForm(form.getBody(), SWT.SMOOTH | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainSash);
        toolkit.adapt(mainSash);
        toolkit.paintBordersFor(mainSash);

        GridLayoutFactory.fillDefaults().applyTo(toolkit.createComposite(mainSash, SWT.NONE));
        final SashForm rightPanelSash = new SashForm(mainSash, SWT.SMOOTH | SWT.VERTICAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(rightPanelSash);
        toolkit.adapt(rightPanelSash);
        toolkit.paintBordersFor(rightPanelSash);

        GridLayoutFactory.fillDefaults().applyTo(toolkit.createComposite(rightPanelSash, SWT.NONE));
        GridLayoutFactory.fillDefaults().applyTo(toolkit.createComposite(rightPanelSash, SWT.NONE));

        mainSash.setWeights(new int[] { 50, 50 });
        rightPanelSash.setWeights(new int[] { 50, 50 });
    }

    @Override
    public void revealElement(final RobotElement element) {
        final RobotSetting setting = (RobotSetting) element;
        if (setting.getGroup() == SettingsGroup.NO_GROUP) {
            generalPart.revealSetting(setting);
            metadataPart.clearSettingsSelection();
            importsPart.clearSettingsSelection();
        } else if (setting.getGroup() == SettingsGroup.METADATA) {
            generalPart.clearSettingsSelection();
            metadataPart.revealSetting(setting);
            importsPart.clearSettingsSelection();
        } else if (SettingsGroup.getImportsGroupsSet().contains(setting.getGroup())) {
            generalPart.clearSettingsSelection();
            metadataPart.clearSettingsSelection();
            importsPart.revealSetting(setting);
        }
    }

    @Override
    protected List<? extends IFormPart> createPageParts(final IEditorSite editorSite) {
        generalPart = new GeneralSettingsFormPart(editorSite);
        metadataPart = new MetadataSettingsFormPart(editorSite);
        importsPart = new ImportsSettingsFormPart(editorSite);
        return Arrays.asList(generalPart, metadataPart, importsPart);
    }

    @Override
    protected ISelectionProvider getSelectionProvider() {
        return new SettingsEditorPageSelectionProvider(generalPart.getViewer(),
                metadataPart.getViewer(), importsPart.getViewer());
    }

    @Override
    protected String getContextId() {
        return CONTEXT_ID;
    }

    @Override
    public Image getTitleImage() {
        return RobotImages.getRobotSettingImage().createImage();
    }

    @Override
    public Optional<RobotElement> provideSection(final RobotSuiteFile suite) {
        return suite.findSection(RobotSuiteSettingsSection.class);
    }

    @Override
    protected String getSectionName() {
        return RobotSuiteSettingsSection.SECTION_NAME;
    }
}
