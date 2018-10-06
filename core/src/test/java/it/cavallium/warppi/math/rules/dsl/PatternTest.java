package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.dsl.patterns.NumberPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SumPattern;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class PatternTest {
    @Test
    public void subFunctionPattern() {
        final Pattern pattern = new SubFunctionPattern("x");

        final Function func = new Sum(
                null,
                new Number(null, 1),
                new Number(null, 2)
        );

        final Optional<Map<String, Function>> subFunctions = pattern.match(func);
        assertTrue(subFunctions.isPresent());

        assertEquals(func, pattern.replace(subFunctions.get()));
    }

    @Test
    public void sumPattern() {
        final Pattern pattern = new SumPattern(
                new SubFunctionPattern("x"),
                new SubFunctionPattern("y")
        );

        final Function shouldNotMatch = new Subtraction(
                null,
                new Number(null, 1),
                new Number(null, 2)
        );
        assertFalse(pattern.match(shouldNotMatch).isPresent());

        final Function shouldMatch = new Sum(
                null,
                new Number(null, 1),
                new Number(null, 2)
        );
        final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
        assertTrue(subFunctions.isPresent());
        assertEquals(shouldMatch, pattern.replace(subFunctions.get()));
    }

    @Test
    public void repeatedSubFunction() {
        final Pattern pattern = new SumPattern(
                new SubFunctionPattern("x"),
                new SubFunctionPattern("x")
        );

        final Function shouldMatch = new Sum(
                null,
                new Number(null, 1),
                new Number(null, 1)
        );
        final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
        assertTrue(subFunctions.isPresent());
        assertEquals(shouldMatch, pattern.replace(subFunctions.get()));

        final Function shouldNotMatch = new Sum(
                null,
                new Number(null, 1),
                new Number(null, 2)
        );
        assertFalse(pattern.match(shouldNotMatch).isPresent());
    }

    @Test
    public void numberPattern() {
        final Pattern pattern = new NumberPattern(BigDecimal.valueOf(Math.PI));

        final Function shouldNotMatch = new Number(null, 2);
        assertFalse(pattern.match(shouldNotMatch).isPresent());

        final Function shouldMatch = new Number(null, Math.PI);
        final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
        assertTrue(subFunctions.isPresent());
        assertEquals(shouldMatch, pattern.replace(subFunctions.get()));
    }
}