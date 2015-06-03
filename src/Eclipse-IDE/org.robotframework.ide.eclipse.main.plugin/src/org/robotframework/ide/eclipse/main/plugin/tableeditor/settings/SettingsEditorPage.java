package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
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
        GridLayoutFactory.fillDefaults().extendedMargins(3, 3, 10, 10).applyTo(form.getBody());
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
        return new SettingsEditorPageSelectionProvider(generalPart.getViewer(), metadataPart.getViewer(),
                importsPart.getViewer());
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
