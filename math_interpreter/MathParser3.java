package math_interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class MathParser3 {
	/**
	 * Finds the next non-nested {@code StructuralToken.RIGHT_PAREN} in
	 * {@code tokens} starting at {@code leftParenIndex}. Mainly used when parsing
	 * function calls.
	 * 
	 * @param tokens         A {@code List<Token>} typically provided by
	 *                       {@code JsonLexer.lex}
	 * @param leftParenIndex Start index of search, typically points to a
	 *                       {@code StructuralToken.LEFT_PAREN}
	 * @return The index of the next non-nested {@code StructuralToken.RIGHT_PAREN}
	 */
	private static int indexOfNonNestedRightParen(List<Token> tokens, int leftParenIndex) {
		final var size = tokens.size();
		var depth = 0;
		for (var i = leftParenIndex; i < size; ++i) {
			final var token = tokens.get(i);
			if (token == StructuralToken.LEFT_PAREN)
				++depth;
			if (token == StructuralToken.RIGHT_PAREN && --depth == 0)
				return i;
		}
		return size;
	}

	/**
	 * Finds the next non-nested {@code StructuralToken.COMMA} in {@code tokens}
	 * starting at {@code startIndex}. Mainly used when parsing function calls.
	 * 
	 * @param tokens     A {@code List<Token>} typically provided by
	 *                   {@code JsonLexer.lex}
	 * @param startIndex Start index of search, typically points to a token
	 *                   immediately after a {@code StructuralToken.COMMA}
	 * @return The index of the next non-nested {@code StructuralToken.COMMA}
	 */
	private static int indexOfNonNestedComma(List<Token> tokens, int startIndex) {
		final var size = tokens.size();
		var depth = 0;
		for (var i = startIndex; i < size; ++i) {
			final var token = tokens.get(i);
			if (token == StructuralToken.LEFT_PAREN)
				++depth;
			if (token == StructuralToken.RIGHT_PAREN)
				--depth;
			if (token == StructuralToken.COMMA && depth == 0)
				return i;
		}
		return size;
	}

	private static void checkSyntax(List<Token> tokens) {
		// TODO: implement syntax checker
	}

	private static void parseFunctionCalls(List<Token> tokens) {
		final var size = tokens.size();
		for (var i = 0; i < size - 2; ++i) {
			final var identifier = tokens.get(i);
			final var leftParenIndex = i + 1;
			final var leftParen = tokens.get(leftParenIndex);
			if (identifier instanceof Identifier && leftParen == StructuralToken.LEFT_PAREN) {
				final var rightParenIndex = indexOfNonNestedRightParen(tokens, leftParenIndex);
				
			}
		}
	}

	/**
	 * Converts the given list of tokens representing a mathematical expression to
	 * postfix notation, using the Shunting Yard algorithm.
	 * 
	 * @param infixTokens A {@code List<Token>} typically provided by
	 *                    {@code JsonLexer.lex}
	 * @return A new {@code List<Token>} containing the same mathematical expression
	 *         in postfix notation
	 */
	private static List<Token> convertToPostfix(List<Token> infixTokens) {
		final var operatorStack = new Stack<Token>();
		final var postfix = new ArrayList<Token>();

		for (final var token : infixTokens) {
			if (token instanceof Operand) {
				postfix.add(token);
			}

			if (token instanceof final Operator o) {
				if (!operatorStack.empty()) {
					var topOperator = operatorStack.peek();
					while (!operatorStack.empty() && topOperator != StructuralToken.LEFT_PAREN
							&& ((Operator) topOperator).hasHigherPrecedenceThan(o)) {
						postfix.add(operatorStack.pop());
						if (!operatorStack.empty())
							topOperator = operatorStack.peek();
						else
							break;
					}
				}
				operatorStack.push(o);
			}

			if (token == StructuralToken.LEFT_PAREN) {
				operatorStack.push(token);
			}

			if (token == StructuralToken.RIGHT_PAREN) {
				Token topOperator;
				while ((topOperator = operatorStack.pop()) != StructuralToken.LEFT_PAREN) {
					postfix.add(topOperator);
				}
			}
		}

		while (!operatorStack.empty()) {
			postfix.add(operatorStack.pop());
		}

		return Collections.unmodifiableList(postfix);
	}

	/**
	 * Stores variables in this {@code MathParser}'s context.
	 */
	private final HashMap<String, Object> variables = new HashMap<>();

	Object evaluateExpression(List<Token> tokens) {
		return evaluatePostfix(convertToPostfix(tokens));
	}

	private Object evaluatePostfix(List<Token> postfixTokens) {
		final var operandStack = new Stack<Token>();

		for (final var token : postfixTokens) {
			if (token instanceof Operand) {
				operandStack.push(token);
			}

			if (token instanceof final BinaryOperator binaryOperator) {
				// pop in this order because some binary operators are not commutative
				final var operand2 = ((Operand) operandStack.pop()).getValue(variables);
				final var operand1 = ((Operand) operandStack.pop()).getValue(variables);
				final var result = binaryOperator.evaluate(operand1, operand2);
				operandStack.push(Value.of(result));
			}

			if (token instanceof final UnaryOperator unaryOperator) {
				final var operand = ((Operand) operandStack.pop()).getValue(variables);
				final var result = unaryOperator.evaluate(operand);
				operandStack.push(Value.of(result));
			}

			if (token instanceof final AssignmentOperator assignmentOperator) {
				final var operand = ((Operand) operandStack.pop()).getValue(variables);
				final var identifier = ((Identifier) operandStack.pop()).identifier;
				final var result = assignmentOperator.evaluate(variables, identifier, operand);
				operandStack.push(Value.of(result));
			}
		}

		return ((Operand) operandStack.peek()).getValue(variables);
	}

	public static void main(String[] args) {
		final var mathParser = new MathParser3();
		final var console = System.console();
		String line;
		System.out.print("> ");
		while ((line = console.readLine()) != null) {
			final var tokens = MathLexer2.lex(line);
			System.out.println("Lexed tokens: " + tokens);
			final var postfix = convertToPostfix(tokens);
			System.out.println("Postfix: " + postfix);
			try {
				System.out.println(mathParser.evaluatePostfix(postfix));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print("> ");
		}
	}
}
