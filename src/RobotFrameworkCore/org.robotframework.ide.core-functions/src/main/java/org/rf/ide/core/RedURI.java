/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * @author Michal Anglart
 *
 */
public class RedURI {

    public static final Escaper URI_SPECIAL_CHARS_ESCAPER = Escapers.builder()
            .addEscape(' ', "%20")
            .addEscape('!', "%21")
            .addEscape('"', "%22")
            .addEscape('#', "%23")
            .addEscape('$', "%24")
            .addEscape('%', "%25")
            .addEscape('&', "%26")
            .addEscape('(', "%28")
            .addEscape(')', "%29")
            .addEscape(';', "%3b")
            .addEscape('@', "%40")
            .addEscape('^', "%5e")
            .build();

    public static String reverseUriSpecialCharsEscapes(final String uriWithEscapedChars) {
        return uriWithEscapedChars.replaceAll("%20", " ")
                .replaceAll("%21", "!")
                .replaceAll("%22", "\"")
                .replaceAll("%23", "#")
                .replaceAll("%24", "\\$")
                .replaceAll("%25", "%")
                .replaceAll("%26", "&")
                .replaceAll("%28", "\\(")
                .replaceAll("%29", "\\)")
                .replaceAll("%3b", ";")
                .replaceAll("%40", "@")
                .replaceAll("%5e", "\\^");
    }

    public static URI fromString(final String path) throws URISyntaxException {
        final String escapedPath = URI_SPECIAL_CHARS_ESCAPER.escape(path);
        final String sep = RedSystemProperties.isWindowsPlatform() ? "/" : "//";
        final String escapedPathWithScheme = new File(path).isAbsolute() ? "file:" + sep + escapedPath : escapedPath;
        final String normalizedPath = RedSystemProperties.isWindowsPlatform() ? 
                escapedPathWithScheme.replaceAll("\\\\", "/") :
                escapedPathWithScheme.replaceAll("\\\\", "%5c");
        return new URI(normalizedPath);
    }
    
}
