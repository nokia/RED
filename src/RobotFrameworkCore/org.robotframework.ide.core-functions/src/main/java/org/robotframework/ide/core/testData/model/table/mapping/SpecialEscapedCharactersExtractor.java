package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;


public class SpecialEscapedCharactersExtractor {

    private final static List<NamedSpecial> SPECIALS = NamedSpecial.expected();

    private final static Pattern PATTERN = Pattern.compile(build(SPECIALS));


    @VisibleForTesting
    public List<Special> extract(final StringBuilder text) {
        List<Special> extracted = new LinkedList<>();

        int currentPos = 0;
        Matcher matcher = PATTERN.matcher(text.toString());
        while(matcher.find()) {
            int start_t = matcher.start();
            int end_t = matcher.end();
            if (start_t - currentPos > 0) {
                extracted.add(new Special(NamedSpecial.UNKNOWN_TEXT, text
                        .substring(currentPos, start_t)));
            }

            extracted.add(new Special(getType(SPECIALS, matcher), text
                    .substring(start_t, end_t)));
            currentPos = end_t;
        }

        int length = text.length();
        if (currentPos < length) {
            extracted.add(new Special(NamedSpecial.UNKNOWN_TEXT, text
                    .substring(currentPos, length)));
        }

        return extracted;
    }


    @VisibleForTesting
    protected NamedSpecial getType(final List<NamedSpecial> specials,
            final Matcher matcher) {

        int typeIndex = -1;
        for (int i = 1; i < matcher.groupCount(); i++) {
            if (matcher.group(i) != null) {
                typeIndex = i;
                break;
            }
        }

        NamedSpecial type;
        if (typeIndex > -1) {
            type = specials.get(typeIndex - 1);
        } else {
            type = NamedSpecial.UNKNOWN_TEXT;
        }

        return type;
    }


    @VisibleForTesting
    protected static String build(List<NamedSpecial> specials) {
        StringBuilder pattern = new StringBuilder();
        int size = specials.size();
        for (int i = 0; i < size; i++) {
            pattern.append("([\\\\]")
                    .append(specials.get(i).getPatternSuffix()).append(")");
            if (i < size - 1) {
                pattern.append('|');
            }
        }

        return pattern.toString();
    }

    public static class Special {

        private final NamedSpecial type;
        private final String text;


        public Special(final NamedSpecial type, final String text) {
            this.type = type;
            this.text = text;
        }


        public NamedSpecial getType() {
            return type;
        }


        public String getText() {
            return text;
        }
    }

    public enum NamedSpecial {
        UNKNOWN_TEXT("", ""),
        /**
         * \|
         */
        ESCAPED_PIPE("[|]", "|"),
        /**
         * key\=value
         */
        ESCAPED_EQUALS("[=]", "="),
        /**
         * \#
         */
        ESCAPED_HASH("[#]", "#"),
        /**
         * \%{env_var}
         */
        ESCAPED_PROCENT("[%]", "%"),
        /**
         * \&{dictionary_var}
         */
        ESCAPED_AMPERSAND("[&]", "&"),
        /**
         * \@{list_var}
         */
        ESCAPED_AT("[@]", "@"),
        /**
         * \${scalar_var}
         */
        ESCAPED_DOLAR("[$]", "$"),
        /**
         * \ ddddd
         */
        ESCAPED_WHITESPACE("[\\s+]", " "),
        /**
         * \\
         */
        ESCAPED_BACKSLASH("{2}", "\\");

        private final String patternSuffix;
        private final String normalized;


        private NamedSpecial(final String patternSuffix, final String normalized) {
            this.patternSuffix = patternSuffix;
            this.normalized = normalized;
        }


        public String getPatternSuffix() {
            return patternSuffix;
        }


        public String getNormalized() {
            return normalized;
        }


        public static List<NamedSpecial> expected() {
            List<NamedSpecial> spec = new LinkedList<>();
            for (NamedSpecial ns : NamedSpecial.values()) {
                if (ns != NamedSpecial.UNKNOWN_TEXT) {
                    spec.add(ns);
                }
            }

            return spec;
        }
    }
}
