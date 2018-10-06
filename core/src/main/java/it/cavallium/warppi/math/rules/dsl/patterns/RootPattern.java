package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Root;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates a root of degree and radicand patterns.
 */
public class RootPattern extends VisitorPattern {
    private final Pattern degree;
    private final Pattern radicand;

    public RootPattern(final Pattern degree, final Pattern radicand) {
        this.degree = degree;
        this.radicand = radicand;
    }

    @Override
    public Optional<Map<String, Function>> visit(final Root root) {
        return PatternUtils.matchFunctionOperatorParameters(root, degree, radicand);
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new Root(
                mathContext,
                degree.replace(mathContext, subFunctions),
                radicand.replace(mathContext, subFunctions)
        );
    }
}
