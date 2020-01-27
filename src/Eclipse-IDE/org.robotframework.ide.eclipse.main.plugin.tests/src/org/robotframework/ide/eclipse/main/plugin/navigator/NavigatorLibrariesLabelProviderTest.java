/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotProjectDependencies.ErroneousLibrarySpecification;

/**
 * @author lwlodarc
 */
public class NavigatorLibrariesLabelProviderTest {

    private final NavigatorLibrariesLabelProvider labelProvider = new NavigatorLibrariesLabelProvider();

    @Test
    public void imageForRobotProjectDependencies_isNotNull() {
        final RobotProjectDependencies dependencies = new RobotProjectDependencies(null);
        final Image img = labelProvider.getImage(dependencies);

        assertThat(img).isNotNull();
    }

    @Test
    public void imageForLibrarySpecification_isNotNull() {
        final LibrarySpecification specification = new LibrarySpecification();
        final Image img = labelProvider.getImage(specification);

        assertThat(img).isNotNull();
    }

    @Test
    public void textForRobotProjectDependencies_isCorrect() {
        final RobotProjectDependencies dependencies = new RobotProjectDependencies(null);
        final String txt = labelProvider.getText(dependencies);

        assertThat(txt).isEqualTo("Robot Standard libraries");
    }

    @Test
    public void textForLibrarySpecification_isCorrect() {
        final LibrarySpecification specification = new LibrarySpecification();
        final String expected = "foo";
        specification.setName(expected);
        final String txt = labelProvider.getText(specification);

        assertThat(txt).isEqualTo(expected);
    }

    @Test
    public void styledTextForRobotProjectDependencies_isCorrect() {
        final RobotProjectDependencies dependencies = new RobotProjectDependencies(null);
        final StyledString txt = labelProvider.getStyledText(dependencies);

        assertThat(txt.getString()).isEqualTo("Robot Standard libraries");
    }

    @Test
    public void styledTextForLibrarySpecification_isCorrect() {
        final LibrarySpecification specification = new LibrarySpecification();
        final String name = "foo";
        specification.setName(name);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary(name);
        specification.setDescriptor(descriptor);

        final StyledString txt = labelProvider.getStyledText(specification);

        assertThat(txt.getString()).isEqualTo(name + " (0)");
    }

    @Test
    public void styledTextForModifiedLibrarySpecification_isCorrect() {
        final LibrarySpecification specification = new LibrarySpecification();
        final String name = "foo";
        specification.setName(name);
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary(name);
        specification.setDescriptor(descriptor);
        specification.setIsModified(true);

        final StyledString txt = labelProvider.getStyledText(specification);

        assertThat(txt.getString()).isEqualTo("*" + name + " (0)");
    }

    @Test
    public void styledTextForLibrarySpecificationWithPathAndArguments_isCorrect() {
        final LibrarySpecification specification = new LibrarySpecification();
        final String name = "foo";
        final String path = "path/to/lib";
        specification.setName(name);
        final List<String> args = new ArrayList<>();
        args.add("arg1");
        args.add("arg2");
        args.add("arg3");
        final LibraryDescriptor descriptor = new LibraryDescriptor(name, LibraryType.PYTHON, path, args);
        specification.setDescriptor(descriptor);

        final StyledString txt = labelProvider.getStyledText(specification);

        assertThat(txt.getString()).isEqualTo(name + " [arg1, arg2, arg3] " + path + " (0)");
    }

    @Test
    public void styledTextForErroneousLibrarySpecification_isCorrect() {
        final String name = "foo";
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary(name);
        final ErroneousLibrarySpecification specification = new ErroneousLibrarySpecification(descriptor);

        final StyledString txt = labelProvider.getStyledText(specification);

        assertThat(txt.getString()).isEqualTo(name + " (non-accessible)");
    }

}
