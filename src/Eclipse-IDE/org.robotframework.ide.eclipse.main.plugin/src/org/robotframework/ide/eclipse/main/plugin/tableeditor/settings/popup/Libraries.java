package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;

class Libraries {

    private final List<LibrarySpecification> toImport;
    private final List<LibrarySpecification> imported;

    static Libraries create(final RobotSuiteFile fileModel) {
        final List<String> alreadyImported = getAlreadyImportedLibraries(fileModel);

        final List<LibrarySpecification> imported = newArrayList();
        final List<LibrarySpecification> toImport = newArrayList();

        final List<LibrarySpecification> standardLibraries = fileModel.getProject().getStandardLibraries();
        for (final LibrarySpecification spec : standardLibraries) {
            if (spec.isAccessibleWithoutImport() || alreadyImported.contains(spec.getName())) {
                imported.add(spec);
            } else {
                toImport.add(spec);
            }
        }
        return new Libraries(toImport, imported);
    }

    private static List<String> getAlreadyImportedLibraries(final RobotSuiteFile fileModel) {
        final Optional<RobotElement> section = fileModel.findSection(RobotSuiteSettingsSection.class);
        final List<String> alreadyImported = newArrayList();
        if (section.isPresent()) {
            final List<RobotElement> importSettings = ((RobotSuiteSettingsSection) section.get())
                    .getImportSettings();
            for (final RobotElement element : importSettings) {
                final RobotSetting setting = (RobotSetting) element;
                if (SettingsGroup.LIBRARIES == setting.getGroup()) {
                    final String name = setting.getArguments().isEmpty() ? null : setting.getArguments().get(0);
                    if (name != null) {
                        alreadyImported.add(name);
                    }
                }
            }
        }
        return alreadyImported;
    }

    private Libraries(final List<LibrarySpecification> toImport, final List<LibrarySpecification> imported) {
        this.toImport = toImport;
        this.imported = imported;
    }

    List<LibrarySpecification> getLibrariesToImport() {
        return toImport;
    }

    List<LibrarySpecification> getImportedLibraries() {
        return imported;
    }
}