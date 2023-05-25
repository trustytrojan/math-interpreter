package math_interpreter;

final class LongDoubleOperators {
	private LongDoubleOperators() {}

	private static Number exception() {
		throw new ClassCastException("expected long or double");
	}

	static Number negate(Number a) {
		return switch (a) {
			case final Long aL -> -aL;
			case final Double aD -> -aD;
			default -> exception();
		};
	}

	/*
	private static void typeCheck(Number a) {
		if (!(a instanceof Long) || !(a instanceof Double)) {
			exception();
		}
	}
	*/

	static Number add(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL + bL;
		}
		return a.doubleValue() + b.doubleValue();
	}

	static Number subtract(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL - bL;
		}
		return a.doubleValue() - b.doubleValue();
	}

	static Number multiply(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL * bL;
		}
		return a.doubleValue() * b.doubleValue();
	}

	static Number divide(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL / bL;
		}
		return a.doubleValue() / b.doubleValue();
	}

	static Number power(Number a, Number b) {
		System.out.println(a);
		System.out.println(b);
		final var result = Math.pow(a.doubleValue(), b.doubleValue());
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return (long) result;
		}
		return result;
	}

	static boolean lessThan(Number a, Number b) {
		return (a instanceof Long && b instanceof Long)
			? a.longValue() < b.longValue()
			: a.doubleValue() < b.doubleValue();
	}

	static boolean lessThanOrEqual(Number a, Number b) {
		return (a instanceof Long && b instanceof Long)
			? a.longValue() <= b.longValue()
			: a.doubleValue() <= b.doubleValue();
	}

	static boolean greaterThan(Number a, Number b) {
		return (a instanceof Long && b instanceof Long)
			? a.longValue() > b.longValue()
			: a.doubleValue() > b.doubleValue();
	}

	static boolean greaterThanOrEqual(Number a, Number b) {
		return (a instanceof Long && b instanceof Long)
			? a.longValue() >= b.longValue()
			: a.doubleValue() >= b.doubleValue();
	}

	static Number and(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL & bL;
		}
		throw new ClassCastException();
	}

	static Number or(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL | bL;
		}
		throw new ClassCastException();
	}

	static Number xor(Number a, Number b) {
		if (a instanceof final Long aL && b instanceof final Long bL) {
			return aL ^ bL;
		}
		throw new ClassCastException();
	}

	static Number sin(Number x) {
		return Math.sin(x.doubleValue());
	}
}
