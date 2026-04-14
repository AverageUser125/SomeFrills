package com.somefrills.features.misc.matcher;

import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static com.somefrills.features.misc.matcher.MatcherTypes.*;

/**
 * Parses matcher expressions into Matcher objects.
 * Supports syntax like: "TYPE=zombie AND HELMET+minecraft:diamond_helmet" or "(NAME=foo OR TYPE=bar) AND CHEST+iron_chestplate"
 * <p>
 * Optimization: Within AND chains, NameMatchers are prioritized first for short-circuit evaluation.
 * This prevents expensive armor stand resolution from running if the name doesn't match.
 */
public class MatcherParser {
    private final String input;
    private int pos = 0;

    public static Matcher parse(String expression) throws MatcherParseException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new MatcherParseException("Empty expression");
        }
        return new MatcherParser(expression).parseOr();
    }

    private MatcherParser(String input) {
        this.input = input.trim();
    }

    /**
     * Parse OR expressions (lowest precedence)
     */
    private Matcher parseOr() throws MatcherParseException {
        Matcher left = parseAnd();
        while (peekKeyword(OR)) {
            consumeKeyword(OR);
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

        while (peekKeyword(AND)) {
            consumeKeyword(AND);
            matchers.add(parsePrimary());
        }

        // Check for unexpected matcher after AND chain (missing AND/OR)
        skipWhitespace();
        if (pos < input.length() && peekNextIsMatcherStart()) {
            throw new MatcherParseException("Expected AND or OR before matcher at position " + pos);
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
    private boolean peekNextIsMatcherStart() throws MatcherParseException {
        int savedPos = pos;
        skipWhitespace();

        if (pos >= input.length() || input.charAt(pos) == ')') {
            pos = savedPos;
            return false;
        }

        // Read potential matcher key
        int start = pos;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '=' || c == '!' || c == ')' || Character.isWhitespace(c)) {
                break;
            }
            pos++;
        }

        if (pos == start) {
            pos = savedPos;
            return false;
        }

        String key = input.substring(start, pos).toUpperCase();
        pos = savedPos;

        // Check if this looks like a matcher type
        return MatcherTypes.isMatcher(key) || key.equals(NAKED);
    }

    /**
     * Parse primary expressions: parentheses or simple matchers
     */
    private Matcher parsePrimary() throws MatcherParseException {
        skipWhitespace();

        if (pos < input.length() && input.charAt(pos) == '(') {
            pos++; // consume '('
            Matcher result = parseOr();
            skipWhitespace();
            if (pos >= input.length() || input.charAt(pos) != ')') {
                throw new MatcherParseException("Expected ')' at position " + pos);
            }
            pos++; // consume ')'
            return result;
        }

        return parseSimpleMatcher();
    }

    /**
     * Parse a simple matcher: TYPE=value, NAME=value, HELMET=value, NAKED, etc.
     * Also supports != for negation: TYPE!=value, NAME!=value, HELMET!=value
     */
    private Matcher parseSimpleMatcher() throws MatcherParseException {
        skipWhitespace();
        int start = pos;

        // Read until we hit '=', '!', ')', whitespace, or end
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '=' || c == '!' || c == ')' || Character.isWhitespace(c)) {
                break;
            }
            pos++;
        }

        if (pos == start) {
            throw new MatcherParseException("Expected matcher at position " + pos);
        }

        String key = input.substring(start, pos).toUpperCase();
        skipWhitespace();

        // Handle special matchers that don't have operators (like NAKED)
        if (key.equals(NAKED)) {
            if (pos < input.length() && (input.charAt(pos) == '=' || input.charAt(pos) == '!')) {
                throw new MatcherParseException("Matcher '" + NAKED + "' does not take any value");
            }
            return new NakedMatcher();
        }

        if (pos >= input.length()) {
            throw new MatcherParseException("Expected '=' or '!=' after key '" + key + "'");
        }

        // Check for != or =
        boolean negated = false;
        if (input.charAt(pos) == '!') {
            if (pos + 1 >= input.length() || input.charAt(pos + 1) != '=') {
                throw new MatcherParseException("Expected '!=' at position " + pos);
            }
            negated = true;
            pos += 2;
        } else if (input.charAt(pos) == '=') {
            pos++;
        } else {
            throw new MatcherParseException("Expected '=' or '!=' at position " + pos);
        }

        // Read the value (until whitespace, ')', or end)
        skipWhitespace();
        start = pos;

        // Check if value is quoted
        String value;
        if (pos < input.length() && input.charAt(pos) == '"') {
            pos++; // skip opening quote
            start = pos;
            while (pos < input.length() && input.charAt(pos) != '"') {
                pos++;
            }
            if (pos >= input.length()) {
                throw new MatcherParseException("Unterminated quoted string at position " + start);
            }
            value = input.substring(start, pos);
            pos++; // skip closing quote
        } else {
            // Unquoted value: read until whitespace, ')', or end
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == ')' || Character.isWhitespace(c)) {
                    break;
                }
                pos++;
            }
            value = input.substring(start, pos);
        }

        return getMatcher(value, key, negated);
    }

    private @NonNull Matcher getMatcher(String value, String key, boolean negated) throws MatcherParseException {
        if (value.isEmpty()) {
            throw new MatcherParseException("Expected value after '" + key + (negated ? "!=" : "=") + "'");
        }

        // Create the base matcher
        Matcher baseMatcher;
        if (MatcherTypes.isEquipmentSlot(key)) {
            baseMatcher = new EquipmentMatcher(key, value);
        } else {
            baseMatcher = switch (key) {
                case TYPE -> new TypeMatcher(value);
                case NAME -> new NameMatcher(value);
                case AREA -> new AreaMatcher(value);
                default -> throw new MatcherParseException("Unknown matcher type: " + key);
            };
        }
        return negated ? new NotMatcher(baseMatcher) : baseMatcher;
    }

    private boolean peekKeyword(String keyword) {
        int savedPos = pos;
        skipWhitespace();
        boolean result = remainingMatches(keyword);
        pos = savedPos;
        return result;
    }

    private void consumeKeyword(String keyword) throws MatcherParseException {
        skipWhitespace();
        if (!remainingMatches(keyword)) {
            throw new MatcherParseException("Expected keyword '" + keyword + "' at position " + pos);
        }
        pos += keyword.length();
    }

    private boolean remainingMatches(String keyword) {
        if (pos + keyword.length() > input.length()) {
            return false;
        }
        String remaining = input.substring(pos, pos + keyword.length());
        if (!remaining.equalsIgnoreCase(keyword)) {
            return false;
        }
        // Ensure it's a complete word (followed by whitespace, '(', or end)
        int nextPos = pos + keyword.length();
        if (nextPos < input.length()) {
            char next = input.charAt(nextPos);
            return Character.isWhitespace(next) || next == '(' || next == ')';
        }
        return true;
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
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



