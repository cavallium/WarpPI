package it.cavallium.warppi.math.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.math.MathContext;

class MultiplicationTest {
	@Test
	void removesNegativeUnitFromEitherSide() {
		final MathContext context = new MathContext();
		final Number value = new Number(context, 7);
		final Number negativeUnit = new Number(context, -1);

		assertEquals(value, new Multiplication(context, negativeUnit, value).toPositive());
		assertEquals(value, new Multiplication(context, value, negativeUnit).toPositive());
	}
}
