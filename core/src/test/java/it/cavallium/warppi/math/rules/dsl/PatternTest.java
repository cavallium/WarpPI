package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;
import org.junit.Test;

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
}