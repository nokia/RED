package org.robotframework.ide.core.testData.text;

public enum RobotTokenType {
    START_LINE("<NEW_LINE>"), ASTERISK("*"), CARRIAGE_RETURN("<CR>"), LINE_FEED(
            "<LF>"), END_OF_FILE("<EOF>"), SPACE("<SPACE>"), PIPE("|"), AT("@"), HASH(
            "#"), DOLAR_SIGN("$"), PERCENT("%"), CARET("^"), AMPERSAND("&"), OPEN_CIRCLE_BRACKET(
            "("), CLOSE_CIRCLE_BRACKET(")"), MINUS("-"), UNDERSCORE("_"), PLUS(
            "+"), EQUALS("="), OPEN_CURLY_BRACKET("{"), CLOSE_CURLY_BRACKET("}"), OPEN_SQUARE_BRACKET(
            "["), CLOSE_SQUARE_BRACKET("]"), BACKSLASH("\\"), FORWARD_SLASH("/"), COLON(
            ":"), SEMICOLON(";"), QUOTATION_MARK("\""), APOSTROPHE("'"), LESS_THAN(
            "<"), GREATER_THAN(">"), COMMA(","), DOT("."), QUESTION_MARK("?"), TAB(
            "<TAB>"), UNICODE_LETTER("<UNICODE_LETTER>"), DIGIT("[DIGIT]"), VERTICAL_TAB(
            "[U+000B<VT>]"), FORM_FEED("[U+000C<FF>]"), NEXT_LINE(
            "[U+0085<NEL>]"), LINE_SEPARATOR("[U+2028<LS>]"), PARAGRAPH_SEPARATOR(
            "[U+2029<PS>]"), UNKNOWN_CHARACTER("<UNKNOWN>");

    private String toStringRepresentation = "";


    private RobotTokenType(final String toStringRepresentation) {
        this.toStringRepresentation = toStringRepresentation;
    }


    public String getSpecialAsText() {
        return toStringRepresentation;
    }
}
