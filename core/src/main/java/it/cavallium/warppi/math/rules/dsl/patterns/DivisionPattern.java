package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates a division of two other patterns.
 */
public class DivisionPattern extends VisitorPattern {
    private final Pattern dividend;
    private final Pattern divisor;

    public DivisionPattern(final Pattern dividend, final Pattern divisor) {
        this.dividend = dividend;
        this.divisor = divisor;
    }

    @Override
    public Optional<Map<String, Function>> visit(final Division division) {
        return PatternUtils.matchFunctionOperatorParameters(division, dividend, divisor);
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new Division(
                mathContext,
                dividend.replace(mathContext, subFunctions),
                divisor.replace(mathContext, subFunctions)
        );
    }
}
