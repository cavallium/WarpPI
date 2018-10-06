package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates a specific number.
 */
public class NumberPattern extends VisitorPattern {
    private final BigDecimal value;

    public NumberPattern(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Optional<Map<String, Function>> visit(Number number) {
        if (number.getTerm().compareTo(value) == 0) {
            return Optional.of(new HashMap<>());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Function replace(MathContext mathContext, Map<String, Function> subFunctions) {
        return new Number(mathContext, value);
    }
}
