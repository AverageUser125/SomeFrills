package com.somefrills.features.misc.matcher;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer/Tokenizer for matcher expressions.
 * Handles tokenization with support for quoted strings, operators, and identifiers.
 * <p>
 * Token types:
 * - IDENTIFIER: alphanumeric and underscores (TYPE, NAME, AREA, etc.)
 * - STRING: quoted string values ("Private Island", "diamond_helmet")
 * - OPERATOR: =, !=
 * - LPAREN: (
 * - RPAREN: )
 * - EOF: end of input
 */
public class MatcherLexer {
    private final String input;
    private int pos = 0;
    private List<Token> tokens;
    private int tokenIndex = 0;

    public static class Token {
        public enum Type {
            IDENTIFIER,  // TYPE, NAME, AREA, AND, OR, NAKED, etc.
            STRING,      // "quoted string value"
            OPERATOR,    // =, !=
            LPAREN,      // (
            RPAREN,      // )
            EOF          // end of input
        }

        public final Type type;
        public final String value;
        public final int pos;

        public Token(Type type, String value, int pos) {
            this.type = type;
            this.value = value;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "Token(" + type + ", '" + value + "', pos=" + pos + ")";
        }
    }

    public MatcherLexer(String input) {
        this.input = input == null ? "" : input.trim();
    }

    /**
     * Tokenize the entire input and return list of tokens
     */
    public List<Token> tokenize() throws LexerException {
        tokens = new ArrayList<>();
        pos = 0;

        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) break;

            char c = input.charAt(pos);

            if (c == '(') {
                tokens.add(new Token(Token.Type.LPAREN, "(", pos));
                pos++;
            } else if (c == ')') {
                tokens.add(new Token(Token.Type.RPAREN, ")", pos));
                pos++;
            } else if (c == '"') {
                tokens.add(parseQuotedString());
            } else if (c == '!' || c == '=') {
                tokens.add(parseOperator());
            } else if (isIdentifierStart(c)) {
                tokens.add(parseIdentifier());
            } else {
                throw new LexerException("Unexpected character '" + c + "' at position " + pos);
            }
        }

        tokens.add(new Token(Token.Type.EOF, "", pos));
        return tokens;
    }

    /**
     * Parse a quoted string
     */
    private Token parseQuotedString() throws LexerException {
        int startPos = pos;
        pos++; // skip opening quote

        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '"') {
                pos++; // skip closing quote
                return new Token(Token.Type.STRING, sb.toString(), startPos);
            } else if (c == '\\' && pos + 1 < input.length()) {
                // Handle escape sequences
                pos++;
                char next = input.charAt(pos);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(next);
                }
                pos++;
            } else {
                sb.append(c);
                pos++;
            }
        }
        throw new LexerException("Unterminated string starting at position " + startPos);
    }

    /**
     * Parse an operator (= or !=)
     */
    private Token parseOperator() throws LexerException {
        int startPos = pos;
        if (input.charAt(pos) == '!') {
            pos++;
            if (pos >= input.length() || input.charAt(pos) != '=') {
                throw new LexerException("Expected '=' after '!' at position " + startPos);
            }
            pos++;
            return new Token(Token.Type.OPERATOR, "!=", startPos);
        } else if (input.charAt(pos) == '=') {
            pos++;
            return new Token(Token.Type.OPERATOR, "=", startPos);
        }
        throw new LexerException("Invalid operator at position " + startPos);
    }

    /**
     * Parse an identifier (TYPE, NAME, AREA, AND, OR, etc.)
     */
    private Token parseIdentifier() {
        int startPos = pos;
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && isIdentifierContinue(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }

        return new Token(Token.Type.IDENTIFIER, sb.toString(), startPos);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierContinue(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ':';
    }

    /**
     * Exception thrown during tokenization
     */
    public static class LexerException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        public LexerException(String message) {
            super(message);
        }
    }

    /**
     * Peek at the current token without consuming it
     */
    public Token peek() {
        if (tokens == null) {
            throw new IllegalStateException("tokenize() must be called first");
        }
        return tokenIndex < tokens.size() ? tokens.get(tokenIndex) : tokens.getLast();
    }

    /**
     * Consume and return the current token
     */
    public Token next() {
        if (tokens == null) {
            throw new IllegalStateException("tokenize() must be called first");
        }
        if (tokenIndex < tokens.size()) {
            return tokens.get(tokenIndex++);
        }
        return tokens.getLast(); // Return EOF
    }

    /**
     * Check if current token is of given type
     */
    public boolean check(Token.Type type) {
        return peek().type == type;
    }

    /**
     * Consume token if it matches the expected type
     */
    public Token consume(Token.Type type) throws LexerException {
        Token token = peek();
        if (token.type != type) {
            throw new LexerException("Expected " + type + " but got " + token.type + " at position " + token.pos);
        }
        return next();
    }

    /**
     * Check if current token is an identifier matching the given value (case-insensitive)
     */
    public boolean checkIdentifier(String value) {
        Token token = peek();
        return token.type == Token.Type.IDENTIFIER && token.value.equalsIgnoreCase(value);
    }

    /**
     * Reset token position to beginning
     */
    public void reset() {
        tokenIndex = 0;
    }

    /**
     * Get current token index
     */
    public int getTokenIndex() {
        return tokenIndex;
    }

    /**
     * Set token index
     */
    public void setTokenIndex(int index) {
        tokenIndex = index;
    }

    /**
     * Get all tokens
     */
    public List<Token> getTokens() {
        return tokens;
    }
}


