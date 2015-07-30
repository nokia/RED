package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ImportLibraryComposite {

    private final RobotEditorCommandsStack commandsStack;

    private final FormToolkit formToolkit;

    private final RobotSuiteFile fileModel;

    private TableViewer leftViewer;

    private TableViewer rightViewer;

    private ISelectionChangedListener leftViewerSelectionChangedListener;

    private ISelectionChangedListener rightViewerSelectionChangedListener;

    public ImportLibraryComposite(final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final FormToolkit formToolkit) {
        this.commandsStack = commandsStack;
        this.formToolkit = formToolkit;
        this.fileModel = fileModel;
    }

    public Composite createImportResourcesComposite(final Composite parent) {
        final Composite librariesComposite = formToolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults()
                .numColumns(3)
                .margins(3, 3)
                .extendedMargins(0, 0, 0, 3)
                .applyTo(librariesComposite);

        final Label titleLabel = formToolkit.createLabel(librariesComposite, "Libraries available in '"
                + fileModel.getProject().getName() + "' project");
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setForeground(formToolkit.getColors().getColor(IFormColors.TITLE));
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).hint(700, SWT.DEFAULT).applyTo(titleLabel);

        leftViewer = new TableViewer(librariesComposite);
        leftViewer.setContentProvider(new LibrariesToImportContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, true).hint(220, 250).applyTo(leftViewer.getControl());
        ViewerColumnsFactory.newColumn("")
                .shouldGrabAllTheSpaceLeft(true)
                .withWidth(200)
                .labelsProvidedBy(new LibrariesLabelProvider())
                .createFor(leftViewer);
        leftViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                final Optional<LibrarySpecification> element = Selections.getOptionalFirstElement(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                if (element.isPresent()) {
                    handleLibraryAdd((Settings) leftViewer.getInput(), newArrayList(element.get()));
                }
            }
        });

        final Button toImported = formToolkit.createButton(librariesComposite, ">>", SWT.PUSH);
        toImported.setEnabled(false);
        toImported.addSelectionListener(createToImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(toImported);

        rightViewer = new TableViewer(librariesComposite);
        rightViewer.setContentProvider(new LibrariesAlreadyImportedContentProvider());
        GridDataFactory.fillDefaults().span(1, 2).grab(true, true).hint(220, 250).applyTo(rightViewer.getControl());
        ViewerColumnsFactory.newColumn("")
                .shouldGrabAllTheSpaceLeft(true)
                .withWidth(200)
                .labelsProvidedBy(new LibrariesLabelProvider())
                .createFor(rightViewer);
        rightViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(final DoubleClickEvent event) {
                final Optional<LibrarySpecification> element = Selections.getOptionalFirstElement(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                if (element.isPresent()) {
                    handleLibraryRemove((Settings) rightViewer.getInput(), newArrayList(element.get()));
                }
            }
        });

        final Button fromImported = formToolkit.createButton(librariesComposite, "<<", SWT.PUSH);
        fromImported.setEnabled(false);
        fromImported.addSelectionListener(createFromImportedListener());
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fromImported);

        final Label separator = formToolkit.createLabel(librariesComposite, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().indent(0, 5).grab(false, false).span(3, 1).applyTo(separator);
        final Label tooltipLabel = formToolkit.createLabel(librariesComposite,
                "Choose libraries to import. Only libraries which are imported by project are available. "
                        + "Edit project properties if you need other libraries", SWT.WRAP);
        GridDataFactory.fillDefaults().span(3, 1).hint(500, SWT.DEFAULT).applyTo(tooltipLabel);

        createLeftViewerSelectionListener(toImported);
        createRightViewerSelectionListener(fromImported);

        return librariesComposite;
    }

    public ISelectionChangedListener getLeftViewerSelectionChangedListener() {
        return leftViewerSelectionChangedListener;
    }

    public ISelectionChangedListener getRightViewerSelectionChangedListener() {
        return rightViewerSelectionChangedListener;
    }

    public TableViewer getLeftViewer() {
        return leftViewer;
    }

    public TableViewer getRightViewer() {
        return rightViewer;
    }

    private SelectionListener createToImportedListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings libs = (Settings) leftViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) leftViewer.getSelection(), LibrarySpecification.class);

                handleLibraryAdd(libs, specs);
            }
        };
    }

    private SelectionListener createFromImportedListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Settings libs = (Settings) rightViewer.getInput();
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) rightViewer.getSelection(), LibrarySpecification.class);

                handleLibraryRemove(libs, specs);
            }
        };
    }

    private void handleLibraryAdd(final Settings libs, final List<LibrarySpecification> specs) {
        libs.getLibrariesToImport().removeAll(specs);
        libs.getImportedLibraries().addAll(specs);

        final Optional<RobotElement> section = fileModel.findSection(RobotSettingsSection.class);
        final RobotSettingsSection settingsSection = (RobotSettingsSection) section.get();
        for (final LibrarySpecification spec : specs) {
            final ArrayList<String> args = newArrayList(spec.getName());
            if (spec.isRemote()) {
                String host = spec.getAdditionalInformation();
                if (!host.startsWith("http://")) {
                    host = "http://" + host;
                }
                args.add(host);
            }
            commandsStack.execute(new CreateSettingKeywordCallCommand(settingsSection, "Library", args));
        }

        leftViewer.refresh();
        rightViewer.refresh();
    }

    private void handleLibraryRemove(final Settings libs, final List<LibrarySpecification> specs) {
        if (!doesNotContainAlwaysAccessible(specs)) {
            return;
        }

        libs.getImportedLibraries().removeAll(specs);
        libs.getLibrariesToImport().addAll(specs);

        final Optional<RobotElement> section = fileModel.findSection(RobotSettingsSection.class);
        final RobotSettingsSection settingsSection = (RobotSettingsSection) section.get();
        final List<RobotSetting> settingsToRemove = getSettingsToRemove(settingsSection, specs);
        commandsStack.execute(new DeleteSettingKeywordCallCommand(settingsToRemove));

        leftViewer.refresh();
        rightViewer.refresh();
    }

    private List<RobotSetting> getSettingsToRemove(final RobotSettingsSection settingsSection,
            final List<LibrarySpecification> specs) {
        final List<RobotSetting> settings = newArrayList();
        final List<String> specNames = Lists.transform(specs, new Function<LibrarySpecification, String>() {

            @Override
            public String apply(final LibrarySpecification spec) {
                return spec.getName();
            }
        });
        for (final RobotElement element : settingsSection.getImportSettings()) {
            final RobotSetting setting = (RobotSetting) element;
            final String name = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
            if (specNames.contains(name)) {
                settings.add(setting);
            }
        }
        return settings;
    }

    private boolean doesNotContainAlwaysAccessible(final List<LibrarySpecification> specs) {
        for (final LibrarySpecification spec : specs) {
            if (spec.isAccessibleWithoutImport()) {
                return false;
            }
        }
        return true;
    }

    private void createLeftViewerSelectionListener(final Button buttonToActivate) {
        leftViewerSelectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                buttonToActivate.setEnabled(!specs.isEmpty() && doesNotContainAlwaysAccessible(specs));
            }
        };
        leftViewer.addSelectionChangedListener(leftViewerSelectionChangedListener);
    }

    private void createRightViewerSelectionListener(final Button buttonToActivate) {
        rightViewerSelectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final List<LibrarySpecification> specs = Selections.getElements(
                        (IStructuredSelection) event.getSelection(), LibrarySpecification.class);
                buttonToActivate.setEnabled(!specs.isEmpty() && doesNotContainAlwaysAccessible(specs));
            }
        };
        rightViewer.addSelectionChangedListener(rightViewerSelectionChangedListener);
    }

    private static class LibrariesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

        @Override
        public Image getImage(final Object element) {
            return ImagesManager.getImage(RedImages.getBookImage());
        }

        @Override
        public String getText(final Object element) {
            return ((LibrarySpecification) element).getName();
        }

        @Override
        public StyledString getStyledText(final Object element) {
            final LibrarySpecification spec = (LibrarySpecification) element;
            final StyledString text = new StyledString(spec.getName());
            if (spec.isAccessibleWithoutImport()) {
                text.append(" ");
                text.append("always accessible", new Styler() {

                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RedTheme.getEclipseDecorationColor();
                    }
                });
            } else if (spec.isRemote()) {
                text.append(" ");
                text.append(spec.getAdditionalInformation(), new Styler() {

                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RedTheme.getEclipseDecorationColor();
                    }
                });
            }
            return text;
        }
    }
}
