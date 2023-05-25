package math_interpreter;

import java.util.HashMap;

interface Token {}

enum StructuralToken implements Token {
	NEWLINE, LEFT_PAREN, RIGHT_PAREN, COMMA;
}

interface Operator extends Token {
	byte precedence();

	default boolean hasHigherPrecedenceThan(Operator other) {
		return precedence() > other.precedence();
	}
}

interface UnaryOperator extends Operator {
	Object evaluate(Object x);
}

enum UnaryArithmeticOperator implements UnaryOperator {
	PLUS, NEGATE;

	@Override
	public byte precedence() {
		return 6;
	}

	@Override
	public Number evaluate(Object x) {
		if (x instanceof final Number xN) {
			return switch (this) {
				case PLUS -> xN;
				case NEGATE -> LongDoubleOperators.negate(xN);
			};
		}
		throw new ClassCastException();
	}
}

enum UnaryBooleanOperator implements UnaryOperator {
	NOT;

	@Override
	public byte precedence() {
		return -4;
	}

	@Override
	public Boolean evaluate(Object x) {
		if (x instanceof final Boolean xB) {
			return !xB;
		}
		throw new ClassCastException();
	}
}

interface BinaryOperator extends Operator {
	Object evaluate(Object a, Object b);
}

enum BinaryArithmeticOperator implements BinaryOperator {
	PLUS, MINUS, TIMES, DIVIDE, POWER;

	@Override
	public byte precedence() {
		return switch (this) {
			case PLUS -> 0;
			case MINUS -> 0;
			case TIMES -> 1;
			case DIVIDE -> 2;
			case POWER -> 3;
		};
	}

	@Override
	public Number evaluate(Object a, Object b) {
		if (a instanceof final Number aN && b instanceof final Number bN) {
			return switch (this) {
				case PLUS -> LongDoubleOperators.add(aN, bN);
				case MINUS -> LongDoubleOperators.subtract(aN, bN);
				case TIMES -> LongDoubleOperators.multiply(aN, bN);
				case DIVIDE ->  LongDoubleOperators.divide(aN, bN);
				case POWER -> LongDoubleOperators.power(aN, bN);
			};
		}
		throw new ClassCastException("arguments must be Long or Double");
	}
}

enum BitwiseOperator implements BinaryOperator {
	AND, OR, XOR;

	@Override
	public byte precedence() {
		return 4;
	}

	@Override
	public Number evaluate(Object a, Object b) {
		if (a instanceof final Number aN && b instanceof final Number bN) {
			return switch (this) {
				case AND -> LongDoubleOperators.and(aN, bN);
				case OR -> LongDoubleOperators.or(aN, bN);
				case XOR -> LongDoubleOperators.xor(aN, bN);	
			};
		}
		throw new ClassCastException();
	}
}

enum ComparisonOperator implements BinaryOperator {
	EQUALS,
	NOT_EQUAL,
	LESS_THAN,
	LESS_THAN_OR_EQUAL,
	GREATER_THAN,
	GREATER_THAN_OR_EQUAL;

	@Override
	public byte precedence() {
		return -2;
	}

	@Override
	public Boolean evaluate(Object a, Object b) {
		if (a instanceof final Number aN && b instanceof final Number bN) {
			return switch (this) {
				case EQUALS -> aN.equals(bN);
				case NOT_EQUAL -> !aN.equals(bN);
				case LESS_THAN -> LongDoubleOperators.lessThan(aN, bN);
				case LESS_THAN_OR_EQUAL -> LongDoubleOperators.lessThanOrEqual(aN, bN);
				case GREATER_THAN -> LongDoubleOperators.greaterThan(aN, bN);
				case GREATER_THAN_OR_EQUAL -> LongDoubleOperators.greaterThanOrEqual(aN, bN);
			};
		}
		if (a instanceof final Boolean aB && b instanceof final Boolean bB) {
			return switch (this) {
				case EQUALS -> aB.equals(bB);
				case NOT_EQUAL -> !aB.equals(bB);
				default -> false; 
			};
		}
		return Boolean.FALSE;
	}
}

enum BinaryBooleanOperator implements BinaryOperator {
	AND, OR;

	@Override
	public byte precedence() {
		return -3;
	}

	@Override
	public Boolean evaluate(Object a, Object b) {
		if (a instanceof final Boolean aB && b instanceof final Boolean bB) {
			return switch (this) {
				case AND -> aB && bB;
				case OR -> aB || bB;
			};
		}
		throw new ClassCastException();
	}
}

enum AssignmentOperator implements Operator {
	ASSIGNMENT,
	ON_THE_FLY,
	PLUS,
	MINUS,
	TIMES,
	DIVIDE,
	POWER,
	BITWISE_AND,
	BITWISE_OR,
	BITWISE_XOR;

	@Override
	public byte precedence() {
		return switch (this) {
			case ON_THE_FLY -> 5;
			default -> -1;
		};
	}

	public Object evaluate(HashMap<String, Object> variables, String identifier, Object value) {
		if (this == ASSIGNMENT || this == ON_THE_FLY) {
			variables.put(identifier, value);
			return value;
		}

		final var oldValue = variables.get(identifier);
		final Object newValue;
		if (oldValue instanceof final Number a && value instanceof final Number b) {
			newValue = switch (this) {
				case PLUS -> LongDoubleOperators.add(a, b);
				case MINUS -> LongDoubleOperators.subtract(a, b);
				case TIMES -> LongDoubleOperators.multiply(a, b);
				case DIVIDE -> LongDoubleOperators.divide(a, b);
				case POWER -> LongDoubleOperators.power(a, b);
				case BITWISE_AND -> LongDoubleOperators.and(a, b);
				case BITWISE_OR -> LongDoubleOperators.or(a, b);
				case BITWISE_XOR -> LongDoubleOperators.xor(a, b);
				default -> throw new RuntimeException();
			};
		} else {
			throw new ClassCastException();
		}

		variables.put(identifier, newValue);
		return newValue;
	}
}

interface Operand extends Token {
	Object getValue(HashMap<String, Object> variables);
}

final class Identifier implements Operand {
	private static final HashMap<Object, Identifier> IDENTIFIER_CACHE = new HashMap<>();

	static Identifier of(String s) {
		final var cachedIdentifier = IDENTIFIER_CACHE.get(s);
		if (cachedIdentifier != null) {
			return cachedIdentifier;
		}
		return new Identifier(s);
	}

	final String identifier;

	private Identifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return identifier;
	}

	@Override
	public Object getValue(HashMap<String, Object> variables) {
		return variables.get(identifier);
	}
}

final class Value implements Operand {
	static final Value NULL = new Value(null);
	static final Value TRUE = new Value(Boolean.TRUE);
	static final Value FALSE = new Value(Boolean.FALSE);

	private static final HashMap<Object, Value> VALUE_CACHE = new HashMap<>();

	static Value of(Object o) {
		final var cachedValue = VALUE_CACHE.get(o);
		if (cachedValue != null) {
			return cachedValue;
		}
		return new Value(o);
	}

	final Object value;

	private Value(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if (value == null)
			return "null";
		return value.getClass().getSimpleName() + '(' + value + ')';
	}

	@Override
	public Object getValue(HashMap<String, Object> variables) {
		return value;
	}
}
