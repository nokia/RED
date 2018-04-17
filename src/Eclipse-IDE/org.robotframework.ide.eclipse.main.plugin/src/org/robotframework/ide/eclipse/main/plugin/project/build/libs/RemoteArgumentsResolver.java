/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.util.List;
import java.util.stream.Collectors;

import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class RemoteArgumentsResolver {

    private final RobotToken uriToken;

    private final String uri;

    private final ArgumentsDescriptor descriptor;

    public RemoteArgumentsResolver(final List<RobotToken> arguments) {
        this.uriToken = selectUriToken(arguments);
        this.uri = selectUri(uriToken, arguments);
        this.descriptor = ArgumentsDescriptor.createDescriptor("uri=" + RemoteLocation.DEFAULT_ADDRESS, "timeout=None");
    }

    public RobotToken getUriToken() {
        return uriToken;
    }

    public String getUri() {
        return uri;
    }

    public ArgumentsDescriptor getDescriptor() {
        return descriptor;
    }

    public static String getUriForSettingArgumentsList(final List<String> args) {
        final List<String> arguments = args.subList(1, args.size());
        if (arguments.size() == 0) {
            return RemoteLocation.DEFAULT_ADDRESS;
        } else if (arguments.size() <= 2 && isPositional(arguments.get(0))) {
            return addProtocolIfNecessary((arguments.get(0)));
        } else if (arguments.size() == 1) {
            if (isTimeout(arguments.get(0))) {
                return RemoteLocation.DEFAULT_ADDRESS;
            } else if (isUri(arguments.get(0))) {
                return addProtocolIfNecessary(stripArgumentPrefixIfNecessary(arguments.get(0)));
            }
        } else if (arguments.size() == 2) {
            final List<String> allUris = arguments.stream().filter(RemoteArgumentsResolver::isUri).collect(
                    Collectors.toList());
            if (allUris.size() == 1) {
                return addProtocolIfNecessary(stripArgumentPrefixIfNecessary(allUris.get(0)));
            }
        }
        return null;
    }

    private RobotToken selectUriToken(final List<RobotToken> arguments) {
        if (arguments.size() == 1 || arguments.size() == 2) {
            if (isPositional(arguments.get(0).getText())) {
                return arguments.get(0);
            } else {
                final List<RobotToken> allUris = arguments.stream().filter(uri -> isUri(uri.getText())).collect(
                        Collectors.toList());
                if (allUris.size() == 1) {
                    return allUris.get(0);
                }
            }
        }
        return null;
    }

    private String selectUri(final RobotToken uriToken, final List<RobotToken> arguments) {
        if (arguments.size() == 0) {
            return RemoteLocation.DEFAULT_ADDRESS;
        } else if (arguments.size() == 1 && isTimeout(arguments.get(0).getText())) {
            return RemoteLocation.DEFAULT_ADDRESS;
        } else if (uriToken != null) {
            return addProtocolIfNecessary(stripArgumentPrefixIfNecessary(uriToken.getText()));
        }
        return null;
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


    private static String addProtocolIfNecessary(final String argument) {
        return argument.contains("://") ? argument : "http://" + argument;
    }

    private static String stripArgumentPrefixIfNecessary(final String string) {
        return string.toLowerCase().startsWith("uri=") ? string.substring(4, string.length()) : string;
    }

    public static String stripLastSlashIfNecessary(final String string) {
        return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
    }
}
