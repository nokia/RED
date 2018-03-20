/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.ImportedLibrary;


public class LibraryImportSettingInput extends InternalElementInput<RobotSetting> {

    private LibrarySpecification specification;

    public LibraryImportSettingInput(final RobotSetting libraryImportSetting) {
        super(libraryImportSetting);
    }

    @Override
    public void prepare() {
        specification = element.getImportedLibrary().map(ImportedLibrary::getSpecification).orElseThrow(
                () -> new DocumentationInputGenerationException("Library specification not found, nothing to display"));
    }

    @Override
    protected String createHeader() {
        final Optional<URI> imgUri = RedImages.getBookImageUri();
        final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                ? ArgumentsDescriptor.createDescriptor()
                : specification.getConstructor().createArgumentsDescriptor();

        return Headers.formatSimpleHeader(imgUri, specification.getName(),
                newArrayList("Version", specification.getVersion()), 
                newArrayList("Scope", specification.getScope()),
                newArrayList("Arguments", descriptor.getDescription()));
    }

    @Override
    protected Documentation createDocumentation() {
        return specification.createDocumentation();
    }
}
