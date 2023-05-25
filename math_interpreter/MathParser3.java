package math_interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class MathParser3 {
	private final HashMap<String, Object> variables = new HashMap<>();

	Object evaluateExpression(List<Token> tokens) {
		return evaluatePostfix(convertToPostfix(tokens));
	}

	// Shunting Yard algorithm
	private static List<Token> convertToPostfix(List<Token> infixTokens) {
		final var operatorStack = new Stack<Token>();
		final var postfix = new ArrayList<Token>();
		for (final var token : infixTokens) {
			if (token instanceof Operand)
				postfix.add(token);
			if (token instanceof final Operator o) {
				if (!operatorStack.empty()) {
					Token topOperator = operatorStack.peek();
					while (!operatorStack.empty() && topOperator != StructuralToken.LEFT_PAREN && ((Operator) topOperator).hasHigherPrecedenceThan(o)) {
						postfix.add(operatorStack.pop());
						if (!operatorStack.empty())
							topOperator = operatorStack.peek();
						else
							break;
					}
				}
				operatorStack.push(o);
			}
			if (token instanceof final StructuralToken t) {
				switch (t) {
					case LEFT_PAREN -> operatorStack.push(t);
					case RIGHT_PAREN -> {
						Token topOperator;
						while ((topOperator = operatorStack.pop()) != StructuralToken.LEFT_PAREN) {
							postfix.add(topOperator);
						}
					}
					default -> {}
				}
			}
		}

		while (!operatorStack.empty()) {
			postfix.add(operatorStack.pop());
		}

		return Collections.unmodifiableList(postfix);
	}

	private Object evaluatePostfix(List<Token> postfixTokens) {
		final var operandStack = new Stack<Token>();

		for (final var token : postfixTokens) {
			if (token instanceof Operand)
				operandStack.push(token);
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
			System.out.println(tokens);
			final var postfix = convertToPostfix(tokens);
			System.out.println(postfix);
			try {
				System.out.println(mathParser.evaluatePostfix(postfix));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print("> ");
		}
	}
}
