package com.somefrills.features.misc.matcher;

import com.somefrills.features.misc.matcher.MatcherLexer.LexerException;
import com.somefrills.features.misc.matcher.MatcherLexer.Token;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static com.somefrills.features.misc.matcher.MatcherLexer.Token.Type.*;
import static com.somefrills.features.misc.matcher.MatcherTypes.*;

/**
 * Parses matcher expressions into Matcher objects.
 * Supports syntax like: "TYPE=zombie AND AREA=\"Private Island\"" or "(NAME=foo OR TYPE=bar) AND HELMET=diamond_helmet"
 * <p>
 * Uses MatcherLexer for tokenization to support quoted strings and better error messages.
 * Optimization: Within AND chains, NameMatchers are prioritized first for short-circuit evaluation.
 * This prevents expensive armor stand resolution from running if the name doesn't match.
 */
public class MatcherParser {
    private final MatcherLexer lexer;
    private Token currentToken;

    public static Matcher parse(String expression) throws MatcherParseException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new MatcherParseException("Empty expression");
        }
        try {
            return new MatcherParser(expression).parseOr();
        } catch (LexerException e) {
            throw new MatcherParseException("Tokenization error: " + e.getMessage());
        }
    }

    private MatcherParser(String input) throws LexerException {
        this.lexer = new MatcherLexer(input);
        lexer.tokenize();
        this.currentToken = lexer.peek();
    }

    /**
     * Parse OR expressions (lowest precedence)
     */
    private Matcher parseOr() throws MatcherParseException {
        Matcher left = parseAnd();
        while (checkIdentifier(OR)) {
            consumeIdentifier(OR);
            Matcher right = parseAnd();
            left = new OrMatcher(left, right);
        }
        return left;
    }

    /**
     * Parse AND expressions (higher precedence than OR).
     * Collects all AND-chained matchers and reorders them by priority (highest first).
     */
    private Matcher parseAnd() throws MatcherParseException {
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(parsePrimary());

        while (checkIdentifier(AND)) {
            consumeIdentifier(AND);
            matchers.add(parsePrimary());
        }

        // Check for unexpected matcher after AND chain (missing AND/OR)
        if (peekNextIsMatcherStart()) {
            throw new MatcherParseException("Expected AND or OR before matcher at token: " + currentToken);
        }

        // Reorder by priority (highest first for short-circuit evaluation)
        matchers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        // Combine matchers left to right
        Matcher result = matchers.getFirst();
        for (int i = 1; i < matchers.size(); i++) {
            result = new AndMatcher(result, matchers.get(i));
        }
        return result;
    }

    /**
     * Check if the next token looks like the start of a matcher
     */
    private boolean peekNextIsMatcherStart() {
        Token token = currentToken;
        if (token.type == Token.Type.EOF || token.type == Token.Type.RPAREN) {
            return false;
        }

        if (token.type != Token.Type.IDENTIFIER) {
            return false;
        }

        // Check if this looks like a matcher type
        return MatcherTypes.isMatcher(token.value.toUpperCase()) || token.value.equalsIgnoreCase(NAKED);
    }

    /**
     * Parse primary expressions: parentheses or simple matchers
     */
    private Matcher parsePrimary() throws MatcherParseException {
        if (check(LPAREN)) {
            consume(LPAREN);
            Matcher result = parseOr();
            consume(RPAREN);
            return result;
        }

        return parseSimpleMatcher();
    }

    /**
     * Parse a simple matcher: TYPE=value, NAME=value, AREA=value, HELMET=value, NAKED, etc.
     * Also supports != for negation: TYPE!=value, NAME!=value, HELMET!=value
     * Values can be quoted strings or unquoted identifiers.
     */
    private Matcher parseSimpleMatcher() throws MatcherParseException {
        if (currentToken.type != IDENTIFIER) {
            throw new MatcherParseException("Expected matcher type at token: " + currentToken);
        }

        String key = currentToken.value.toUpperCase();
        consume(IDENTIFIER);

        // Handle special matchers that don't have operators (like NAKED)
        if (key.equals(NAKED)) {
            if (check(OPERATOR)) {
                throw new MatcherParseException("Matcher '" + NAKED + "' does not take any value");
            }
            return new NakedMatcher();
        }

        // Expect an operator (= or !=)
        if (!check(OPERATOR)) {
            throw new MatcherParseException("Expected '=' or '!=' after matcher type '" + key + "' at token: " + currentToken);
        }

        String operator = currentToken.value;
        consume(OPERATOR);
        boolean negated = operator.equals("!=");

        // Expect a value (either STRING or IDENTIFIER)
        String value;
        if (check(STRING)) {
            value = currentToken.value;
            consume(STRING);
        } else if (check(IDENTIFIER)) {
            value = currentToken.value;
            consume(IDENTIFIER);
        } else {
            throw new MatcherParseException("Expected value (string or identifier) after '" + key + operator + "' at token: " + currentToken);
        }

        // Create the base matcher
        Matcher baseMatcher = getBaseMatcher(key, value);

        // Wrap with NotMatcher if negated
        return negated ? new NotMatcher(baseMatcher) : baseMatcher;
    }

    private static @NonNull Matcher getBaseMatcher(String key, String value) throws MatcherParseException {
        Matcher baseMatcher;
        if (MatcherTypes.isEquipmentSlot(key)) {
            baseMatcher = new EquipmentMatcher(key, value);
        } else {
            baseMatcher = switch (key) {
                case TYPE -> new TypeMatcher(value);
                case NAME -> new NameMatcher(value);
                case AREA -> {
                    try {
                        yield new AreaMatcher(value);
                    } catch (IllegalArgumentException e) {
                        throw new MatcherParseException(e.getMessage());
                    }
                }
                default -> throw new MatcherParseException("Unknown matcher type: " + key);
            };
        }
        return baseMatcher;
    }

    /**
     * Check if current token is of given type
     */
    private boolean check(Token.Type type) {
        return currentToken.type == type;
    }

    /**
     * Check if current token is an identifier matching the given value (case-insensitive)
     */
    private boolean checkIdentifier(String value) {
        return currentToken.type == IDENTIFIER &&
                currentToken.value.equalsIgnoreCase(value);
    }

    /**
     * Consume current token and move to next
     */
    private void consume(Token.Type type) throws MatcherParseException {
        if (currentToken.type != type) {
            throw new MatcherParseException("Expected " + type + " but got " + currentToken.type + " at token: " + currentToken);
        }
        currentToken = lexer.next();
    }

    /**
     * Consume identifier matching the given value (case-insensitive)
     */
    private void consumeIdentifier(String value) throws MatcherParseException {
        if (!checkIdentifier(value)) {
            throw new MatcherParseException("Expected identifier '" + value + "' but got '" + currentToken.value + "' at token: " + currentToken);
        }
        currentToken = lexer.next();
    }

    /**
     * Exception thrown when parsing fails
     */
    public static class MatcherParseException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        public MatcherParseException(String message) {
            super(message);
        }
    }
}
