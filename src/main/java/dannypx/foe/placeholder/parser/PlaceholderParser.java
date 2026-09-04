package dannypx.foe.placeholder.parser;

import dannypx.foe.placeholder.lexer.PlaceholderBalanceHelper;
import dannypx.foe.placeholder.lexer.PlaceholderTokenizer;
import dannypx.foe.placeholder.parser.ast.*;
import dannypx.foe.placeholder.registry.PlaceholderRegistry;
import dannypx.foe.placeholder.registry.PlaceholderTreeNode;
import dannypx.foe.placeholder.token.PlaceholderParseException;
import dannypx.foe.placeholder.token.Token;
import dannypx.foe.placeholder.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderParser {
    private final String source;
    private final List<Token> tokens;
    private int pos = 0;

    public PlaceholderParser(String source) {
        PlaceholderBalanceHelper.checkBalanced(source);
        this.source = source;
        this.tokens = PlaceholderTokenizer.of(source).tokenize();
    }

    /// Top

    public Group parse() {
        List<Node> children = new ArrayList<>();

        while(tokens.get(pos).type() != TokenType.EOF) {
            Token token = tokens.get(pos);

            if(token.type() == TokenType.PERCENT) {
                children.add(this.parsePlaceholderTop());
            } else {
                pos++;
                children.add(new Literal(token.text()));
            }
        }
        return new Group(children);
    }

    private Node parsePlaceholderTop() {
        this.expect(TokenType.PERCENT);

        if(this.peek().type() == TokenType.PERCENT) {
            this.advance();
            return new Literal("");
        }

        Node body = this.parsePlaceholderBody();
        this.expect(TokenType.PERCENT);
        return body;
    }

    private Node parseNestedPlaceholder() {
        this.expect(TokenType.LT);
        Node body = this.parsePlaceholderBody();
        this.expect(TokenType.GT);
        return body;
    }

    /// Body

    private Node parsePlaceholderBody() {
        Token firstToken = this.expectPathSegment();
        String first = firstToken.text();
        PlaceholderTreeNode current = PlaceholderRegistry.getRoot(first);

        int errorStart = firstToken.start();
        int errorEnd = firstToken.end();
        String offendingSegment = first;
        boolean failed = current == null;

        List<String> capturedIndices = new ArrayList<>();

        while(this.peek().type() == TokenType.DOT) {
            this.advance();
            if(this.peek().type() == TokenType.LPARENTHESIS) {
                break;
            }

            Token segmentToken = this.expectPathSegment();
            String segment = segmentToken.text();
            int segmentStart = segmentToken.start();
            int segmentEnd = segmentToken.end();


            if(!failed) {
                boolean isNamedMatch = current.hasNamedChild(segment);
                boolean isNumericMatch = segmentToken.type() == TokenType.NUMBER && current.hasIndexChild();

                if(!isNamedMatch && !isNumericMatch && current.hasStringArrayWildcard()) {
                    capturedIndices.add(segment);

                    while (this.peek().type() == TokenType.DOT) {
                        this.advance();
                        if(this.peek().type() == TokenType.LPARENTHESIS) {
                            break;
                        }
                        Token wordToken = this.expectPathSegment();
                        capturedIndices.add(wordToken.text());
                        segmentEnd = wordToken.end();
                    }

                    current = current.getStringArrayChild();
                    errorEnd = segmentEnd;
                    break;
                }

                if(!isNamedMatch && !isNumericMatch && current.hasStringWildcard()) {
                    while(this.isExtendableToken(this.peek().type())) {
                        segmentEnd = this.advance().end();
                    }

                    segment = source.substring(segmentStart, segmentEnd).trim();
                }

                PlaceholderTreeNode child = current.resolveChild(segment, capturedIndices);

                if(child == null) {
                    failed = true;
                    offendingSegment = segment;
                }
                current = child;
            }
            errorEnd = segmentEnd;
        }

        String fullPath = source.substring(errorStart, errorEnd);

        if(this.peek().type() == TokenType.LPARENTHESIS) {
            this.advance();
            List<Node> args = new ArrayList<>();

            if(this.peek().type() != TokenType.RPARENTHESIS) {
                args.add(this.parseArgument());
                while(this.peek().type() == TokenType.COMMA) {
                    this.advance();
                    args.add(this.parseArgument());
                }
            }

            this.expect(TokenType.RPARENTHESIS);

            if(failed || current == null || !current.hasEval()) {
                return error(fullPath, offendingSegment, errorStart, errorEnd);
            }

            return new FunctionCall(current, args);
        }

        if(failed || current == null || !current.hasResolver()) {
            return error(fullPath, offendingSegment, errorStart, errorEnd);
        }

        return new PlaceholderReference(current, capturedIndices);
    }

    private AstError error(String fullPath, String offendingSegment, int start, int end) {
        String message = "Unresolved placeholder '" + fullPath + "' (unknown segment: '" + offendingSegment + "')";
        return new AstError(message, start, end);
    }

    private Token expectPathSegment() {
        Token token = this.peek();
        if(token.type() == TokenType.IDENTIFIER || token.type() == TokenType.NUMBER
                || token.type() == TokenType.LITERAL || token.type() == TokenType.ESCAPED_LITERAL
        ) {
            return this.advance();
        }
        throw new PlaceholderParseException(
                "Expected path segment at position " + token.start() + ", got " + token.type(), token.start()
        );
    }

    /// Arguments / Expressions

    private Node parseArgument() {
        return this.parseExpression();
    }

    private Node parseExpression() {
        Node left = this.parseOperand();
        String op = this.detectAndConsumeOperator();

        if(op != null) {
            Node right = this.parseOperand();
            return new BinaryOp(op, left, right);
        }

        return left;
    }

    private Node parseOperand() {
        TokenType type = this.peek().type();

        switch (type) {
            case MINUS -> {
                this.advance();
                Node operand = this.parseOperand();

                return new UnaryOp("-", operand);
            }
            case LT -> {
                return this.parseNestedPlaceholder();
            }
            case STRING, NUMBER, IDENTIFIER -> {
                Token token = this.advance();
                return new Literal(token.text());
            }
            default -> {
                Token unexpected = this.peek();
                throw new PlaceholderParseException(
                        "Expected an operand at position " + unexpected.start() + ", got " + type, unexpected.start()
                );
            }
        }
    }

    private String detectAndConsumeOperator() {
        TokenType type = this.peek().type();
        return switch (type) {
            case LT -> this.consumeMaybeCompound("<", "<=");
            case GT -> this.consumeMaybeCompound(">", ">=");
            case ASSIGN -> this.consumeCompoundOnly("==");
            case BANG -> this.consumeCompoundOnly("!=");
            case PLUS, MINUS, STAR, SLASH -> this.advance().text();
            default -> null;
        };
    }

    private String consumeMaybeCompound(String singleForm, String compoundForm) {
        this.advance();
        if(this.peek().type() == TokenType.ASSIGN) {
            this.advance();
            return compoundForm;
        }
        return singleForm;
    }

    private String consumeCompoundOnly(String compoundForm) {
        if(this.peekAhead(1).type() == TokenType.ASSIGN) {
            this.advance();
            this.advance();
            return compoundForm;
        }
        return null;
    }

    private boolean isExtendableToken(TokenType type) {
        return type == TokenType.WHITESPACE || type == TokenType.IDENTIFIER || type == TokenType.NUMBER
                || type == TokenType.LITERAL || type == TokenType.ESCAPED_LITERAL;
    }

    /// Helpers

    private void skipWhitespaceTokens() {
        while(tokens.get(pos).type() == TokenType.WHITESPACE && pos < tokens.size() - 1) {
            pos++;
        }
    }

    private Token peek() {
        this.skipWhitespaceTokens();
        return tokens.get(pos);
    }

    private Token peekAhead(int offset) {
        this.skipWhitespaceTokens();
        int i = pos;
        int seen = 0;
        while(seen < offset) {
            i++;
            if(i >= tokens.size() - 1) {
                return tokens.get(tokens.size() - 1);
            }
            if(tokens.get(i).type() == TokenType.WHITESPACE) {
                continue;
            }
            seen++;
        }
        return tokens.get(i);
    }

    private Token advance() {
        this.skipWhitespaceTokens();
        Token token = tokens.get(pos);
        if(pos < tokens.size() - 1) pos ++;
        return token;
    }

    private Token expect(TokenType type) {
        Token token = this.peek();
        if(token.type() != type) {
            throw new PlaceholderParseException(
                    "Expected " + type + " at position " + token.start() + ", got " + token.type() + " ('" + token.text() + "')", token.start()
            );
        }
        return this.advance();
    }
}
