/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import java.io.File;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class LibrarySpecificationReader {

    public static Optional<LibrarySpecification> readSpecification(final File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return Optional.empty();
        }
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(LibrarySpecification.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final LibrarySpecification spec = (LibrarySpecification) jaxbUnmarshaller.unmarshal(file);
            if (spec.getSpecificationVersion() >= 2) {
                // if there is no attribute then the keyword is not deprecated
                spec.getKeywordsStream()
                        .filter(kwSpec -> kwSpec.getDeprecatedState() == null)
                        .forEach(kwSpec -> kwSpec.setDeprecated(false));
            }
            return Optional.ofNullable(spec);
        } catch (final JAXBException e) {
            return Optional.empty();
        }
    }
}
