/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.OpenableUri;

public class RfLintRuleDocUri implements OpenableUri {

    private static final String SCHEME = "rflintrule";

    public static boolean isRuleDocUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && uri.getPath().startsWith("/") && uri.getPath().length() > 1;
    }

    public static URI createRuleDocUri(final String ruleName) throws URISyntaxException {
        return createUri(newArrayList(ruleName));
    }

    private static URI createUri(final List<String> segments) throws URISyntaxException {
        final String path = "/" + String.join("/", segments);
        return new URI(SCHEME, null, path, null, null);
    }

    private final URI uri;

    private final Consumer<String> ruleNameConsumer;

    RfLintRuleDocUri(final URI uri, final Consumer<String> ruleNameConsumer) {
        this.uri = uri;
        this.ruleNameConsumer = ruleNameConsumer;
    }

    @Override
    public void open() {
        final String[] pathSegments = uri.getPath().split("/");
        ruleNameConsumer.accept(pathSegments[1]);
    }
}