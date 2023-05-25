package math_interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MathLexer2 {
	private MathLexer2() {}

	static class MathLexerException extends RuntimeException {
		MathLexerException(String message) {
			super(message);
		}
	}

	private static record EndIndexOfNumberResult(int index, boolean decimalPointFound) {}

	private static EndIndexOfNumberResult endIndexOfNumber(String s, int start) {
		final var length = s.length();
		var decimalPointFound = false;
		for (var i = (s.charAt(start) == '-') ? (start + 1) : start; i < length; ++i) {
			final var c = s.charAt(i);
			if (c == '.') {
				if (decimalPointFound) {
					throw new MathLexerException("Too many decimal points!");
				}
				decimalPointFound = true;
				continue;
			}
			if (!Character.isDigit(c)) {
				return new EndIndexOfNumberResult(i, decimalPointFound);
			}
		}
		return new EndIndexOfNumberResult(length, decimalPointFound);
	}

	private static int endIndexOfIdentifier(String s, int start) {
		final var length = s.length();
		if (Character.isDigit(s.charAt(0))) {
			throw new MathLexerException("First character is digit!");
		}
		for (var i = start; i < length; ++i) {
			final var c = s.charAt(i);
			if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
				return i;
			}
		}
		return length;
	}

	static List<Token> lex(String s) {
		Objects.requireNonNull(s);
		final var length = s.length();
		final var tokens = new ArrayList<Token>();
		for (var i = 0; i < length;) {
			final var c = s.charAt(i);

			if (Character.isWhitespace(c)) {
				// if (c == '\n') {
				// 	tokens.add(StructuralToken.NEWLINE);
				// }
				++i;
				continue;
			}

			if (Character.isDigit(c)) {
				final var result = endIndexOfNumber(s, i);
				final var num = s.substring(i, result.index());
				tokens.add(
					(result.decimalPointFound())
						? Value.of(Double.parseDouble(num))
						: Value.of(Long.parseLong(num))
				);
				i = result.index();
				continue;
			}

			firstCharacterSwitch: switch (c) {
				case '\n' -> tokens.add(StructuralToken.NEWLINE);
				case '(' -> tokens.add(StructuralToken.LEFT_PAREN);
				case ')' -> tokens.add(StructuralToken.RIGHT_PAREN);

				// boolean true
				case 't' -> {
					try {
						switch (s.substring(i + 1, i + 4)) {
							case "rue" -> {
								tokens.add(Value.TRUE);
								i += 4;
							}
							default -> {
								break firstCharacterSwitch;
							}
						}
						continue;
					} catch (StringIndexOutOfBoundsException e) {
						break firstCharacterSwitch;
					}
				}

				// boolean false
				case 'f' -> {
					try {
						switch (s.substring(i + 1, i + 5)) {
							case "alse" -> {
								tokens.add(Value.FALSE);
								i += 5;
							}
							default -> {
								break firstCharacterSwitch;
							}
						}
						continue;
					} catch (StringIndexOutOfBoundsException e) {
						break firstCharacterSwitch;
					}
				}

				// null
				case 'n' -> {
					try {
						switch (s.substring(i + 1, i + 4)) {
							case "ull" -> {
								tokens.add(Value.NULL);
								i += 4;
							}
							default -> {
								break firstCharacterSwitch;
							}
						}
						continue;
					} catch (StringIndexOutOfBoundsException e) {
						break firstCharacterSwitch;
					}
				}

				// asignment or equals
				case '=' -> {
					/* check for equals */ {
						final char nextChar;

						try {
							nextChar = s.charAt(i + 1);
						} catch (StringIndexOutOfBoundsException e) {
							throw new MathLexerException("in lexing '=': not enough tokens");
						}

						switch (nextChar) {
							case '=' -> {
								tokens.add(ComparisonOperator.EQUALS);
								i += 2;
								continue;
							}
						}
					}

					final var size = tokens.size();
					
					if (size == 1 && tokens.get(0) instanceof Identifier) {
						tokens.add(AssignmentOperator.ASSIGNMENT);
					} else if (tokens.get(size - 1) instanceof Identifier && tokens.get(size - 2) == StructuralToken.LEFT_PAREN) {
						tokens.add(AssignmentOperator.ON_THE_FLY);
					}
				}

				// plus or plus assign
				case '+' -> {
					/* check for plus assign */ {
						final char nextChar;

						try {
							nextChar = s.charAt(i + 1);
						} catch (StringIndexOutOfBoundsException e) {
							throw new MathLexerException("in lexing '+': not enough tokens");
						}

						if (nextChar == '=') {
							tokens.add(AssignmentOperator.PLUS);
							i += 2;
							continue;
						}
					}

					/* check for unary or binary plus */ {
						final Token prevToken;
	
						try {
							prevToken = tokens.get(tokens.size() - 1);
						} catch (ArrayIndexOutOfBoundsException e) {
							tokens.add(UnaryArithmeticOperator.PLUS);
							++i;
							continue;
						}

						tokens.add(switch (prevToken) {
							case Operand __ -> BinaryArithmeticOperator.PLUS;
							default -> UnaryArithmeticOperator.PLUS;
						});

						++i;
						continue;
					}
				}

				case '-' -> {
					/* check for minus assign */ {
						final char nextChar;

						try {
							nextChar = s.charAt(i + 1);
						} catch (StringIndexOutOfBoundsException e) {
							throw new MathLexerException("in lexing '-': not enough tokens");
						}
	
						if (nextChar == '=') {
							tokens.add(AssignmentOperator.MINUS);
							i += 2;
							continue;
						}
					}
	
					/* check for negation or subtraction */ {
						final Token prevToken;
	
						try {
							prevToken = tokens.get(tokens.size() - 1);
						} catch (ArrayIndexOutOfBoundsException e) {
							tokens.add(UnaryArithmeticOperator.NEGATE);
							++i;
							continue;
						}

						tokens.add(switch (prevToken) {
							case Operand __ -> BinaryArithmeticOperator.MINUS;
							case StructuralToken t -> switch (t) {
								case LEFT_PAREN -> UnaryArithmeticOperator.NEGATE;
								case RIGHT_PAREN -> BinaryArithmeticOperator.MINUS;
								default -> throw new MathLexerException("unexpected token before '-'");
							};
							default -> UnaryArithmeticOperator.NEGATE;
						});

						++i;
						continue;
					}
				}

				// times or power or times assign
				case '*' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '*': not enough tokens");
					}

					switch (nextChar) {
						case '*' -> {
							tokens.add(BinaryArithmeticOperator.POWER);
							i += 2;
						}
						case '=' -> {
							tokens.add(AssignmentOperator.TIMES);
							i += 2;
						}
						default -> {
							tokens.add(BinaryArithmeticOperator.TIMES);
							++i;
						}
					}

					continue;
				}

				// divide or divide assign
				case '/' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '/': not enough tokens");
					}

					switch (nextChar) {
						case '=' -> {
							tokens.add(AssignmentOperator.DIVIDE);
							i += 2;
						}
						default -> {
							tokens.add(BinaryArithmeticOperator.DIVIDE);
							++i;
						}
					}
				}

				// not or not equal
				case '!' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '!': not enough tokens");
					}

					switch (nextChar) {
						case '=' -> {
							tokens.add(ComparisonOperator.NOT_EQUAL);
							i += 2;
						}
						default -> {
							tokens.add(UnaryBooleanOperator.NOT);
							++i;
						}
					}
				}

				// less than (or equal)
				case '<' -> {
					final char nextChar;
					
					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '<': not enough tokens");
					}

					switch (nextChar) {
						case '=' -> {
							tokens.add(ComparisonOperator.LESS_THAN_OR_EQUAL);
							i += 2;
						}
						default -> {
							tokens.add(ComparisonOperator.LESS_THAN);
							++i;
						}
					}
				}

				// greater than (or equal)
				case '>' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '>': not enough tokens");
					}

					switch (nextChar) {
						case '=' -> {
							tokens.add(ComparisonOperator.GREATER_THAN_OR_EQUAL);
							i += 2;
						}
						default -> {
							tokens.add(ComparisonOperator.GREATER_THAN);
							++i;
						}
					}
				}

				// boolean and, bitwise and, bitwise and assign
				case '&' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '&': not enough tokens");
					}

					switch (nextChar) {
						case '&' -> {
							tokens.add(BinaryBooleanOperator.AND);
							i += 2;
						}
						case '=' -> {
							tokens.add(AssignmentOperator.BITWISE_AND);
							i += 2;
						}
						default -> {
							tokens.add(BitwiseOperator.AND);
							++i;
						}
					}

					continue;
				}

				case '|' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '|': not enough tokens");
					}

					switch (nextChar) {
						case '|' -> {
							tokens.add(BinaryBooleanOperator.OR);
							i += 2;
						}
						case '=' -> {
							tokens.add(AssignmentOperator.BITWISE_OR);
							i += 2;
						}
						default -> {
							tokens.add(BitwiseOperator.OR);
							++i;
						}
					}

					continue;
				}

				case '^' -> {
					final char nextChar;

					try {
						nextChar = s.charAt(i + 1);
					} catch (StringIndexOutOfBoundsException e) {
						throw new MathLexerException("in lexing '^': not enough tokens");
					}

					switch (nextChar) {
						case '=' -> {
							tokens.add(AssignmentOperator.BITWISE_XOR);
							i += 2;
						}
						default -> {
							tokens.add(BitwiseOperator.XOR);
							++i;
						}
					}
				}
			}

			// identifier
			if (Character.isAlphabetic(c) || c == '_') {
				final var j = endIndexOfIdentifier(s, i);
				tokens.add(Identifier.of(s.substring(i, j)));
				i = j;
				continue;
			}

			++i;
		}

		return Collections.unmodifiableList(tokens);
	}

	public static void main(String[] args) {
		final var console = System.console();
		String line;
		System.out.print("> ");
		while ((line = console.readLine()) != null) {
			System.out.println(lex(line));
			System.out.print("> ");
		}
	}
}
