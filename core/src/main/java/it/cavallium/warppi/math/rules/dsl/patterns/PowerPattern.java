package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates a power (exponentiation) of base and exponent patterns.
 */
public class PowerPattern extends VisitorPattern {
    private final Pattern base;
    private final Pattern exponent;

    public PowerPattern(final Pattern base, final Pattern exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public Optional<Map<String, Function>> visit(final Power power) {
        return PatternUtils.matchFunctionOperatorParameters(power, base, exponent);
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new Power(
                mathContext,
                base.replace(mathContext, subFunctions),
                exponent.replace(mathContext, subFunctions)
        );
    }
}
