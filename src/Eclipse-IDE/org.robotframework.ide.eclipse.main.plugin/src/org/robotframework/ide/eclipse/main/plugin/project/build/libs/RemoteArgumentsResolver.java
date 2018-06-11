/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class RemoteArgumentsResolver {

    private final Optional<RobotToken> uriToken;

    private final Optional<String> uri;

    private final Optional<RobotToken> timeoutToken;

    private final Optional<String> timeout;

    private final ArgumentsDescriptor descriptor;

    public RemoteArgumentsResolver(final List<RobotToken> arguments) {
        this.uriToken = selectUriToken(arguments);
        this.uri = selectUri(uriToken, arguments);
        this.timeoutToken = selectTimeoutToken(arguments);
        this.timeout = selectTimeout(timeoutToken, arguments);
        this.descriptor = ArgumentsDescriptor.createDescriptor("uri=" + RemoteLocation.DEFAULT_ADDRESS, "timeout=None");
    }

    public Optional<RobotToken> getUriToken() {
        return uriToken;
    }

    public Optional<String> getUri() {
        return uri;
    }

    public Optional<RobotToken> getTimeoutToken() {
        return timeoutToken;
    }

    public Optional<String> getTimeout() {
        return timeout;
    }

    public ArgumentsDescriptor getDescriptor() {
        return descriptor;
    }

    private Optional<RobotToken> selectUriToken(final List<RobotToken> arguments) {
        if (arguments.size() == 1 || arguments.size() == 2) {
            if (isPositional(arguments.get(0).getText())) {
                return Optional.of(arguments.get(0));
            } else {
                final List<RobotToken> allUris = arguments.stream().filter(uri -> isUri(uri.getText())).collect(
                        Collectors.toList());
                if (allUris.size() == 1) {
                    return Optional.of(allUris.get(0));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> selectUri(final Optional<RobotToken> uriToken, final List<RobotToken> arguments) {
        if (arguments.isEmpty()) {
            return Optional.of(RemoteLocation.DEFAULT_ADDRESS);
        } else if (arguments.size() == 1 && isTimeout(arguments.get(0).getText())) {
            return Optional.of(RemoteLocation.DEFAULT_ADDRESS);
        } else if (uriToken.isPresent()) {
            return Optional.of(addProtocolIfNecessary(stripUriArgumentPrefixIfNecessary(uriToken.get().getText())));
        }
        return Optional.empty();
    }

    private Optional<RobotToken> selectTimeoutToken(final List<RobotToken> arguments) {
        if (arguments.size() == 1) {
            if (isTimeout(arguments.get(0).getText())) {
                return Optional.of(arguments.get(0));
            }
        }
        if (arguments.size() == 2) {
            if (isPositional(arguments.get(1).getText())) {
                return Optional.of(arguments.get(1));
            } else {
                final List<RobotToken> allTimeouts = arguments.stream().filter(timeout -> isTimeout(timeout.getText())).collect(
                        Collectors.toList());
                if (allTimeouts.size() == 1) {
                    return Optional.of(allTimeouts.get(0));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> selectTimeout(final Optional<RobotToken> timeoutToken, final List<RobotToken> arguments) {
        if (timeoutToken.isPresent()) {
            return Optional.of(stripTimeoutArgumentPrefixIfNecessary(timeoutToken.get().getText()));
        }
        return Optional.empty();
    }

    private static boolean isPositional(final String argument) {
        return !isUri(argument) && !isTimeout(argument);
    }

    private static boolean isUri(final String argument) {
        return argument.toLowerCase().startsWith("uri=");
    }

    private static boolean isTimeout(final String argument) {
        return argument.toLowerCase().startsWith("timeout=");
    }

    private static String stripUriArgumentPrefixIfNecessary(final String string) {
        return string.toLowerCase().startsWith("uri=") ? string.substring(4, string.length()) : string;
    }

    private static String stripTimeoutArgumentPrefixIfNecessary(final String string) {
        return string.toLowerCase().startsWith("timeout=") ? string.substring(8, string.length()) : string;
    }

    public static String addProtocolIfNecessary(final String argument) {
        return argument.contains("://") ? argument : "http://" + argument;
    }

    public static String stripLastSlashAndProtocolIfNecessary(final String string) {
        return stripLastSlashIfNecessary(stripProtocolIfNecessary(string));
    }

    public static String stripLastSlashIfNecessary(final String string) {
        return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
    }

    private static String stripProtocolIfNecessary(final String string) {
        if (string.toLowerCase().startsWith("https://")) {
            return string.substring(8, string.length());
        } else if (string.toLowerCase().startsWith("http://")) {
            return string.substring(7, string.length());
        }
        return string;
    }
}
