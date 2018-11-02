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
            return Optional.ofNullable((LibrarySpecification) jaxbUnmarshaller.unmarshal(file));
        } catch (final JAXBException e) {
            return Optional.empty();
        }
    }
}
