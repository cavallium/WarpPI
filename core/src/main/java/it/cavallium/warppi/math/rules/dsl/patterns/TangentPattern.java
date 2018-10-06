package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the tangent of another pattern.
 */
public class TangentPattern extends VisitorPattern {
    private final Pattern argument;

    public TangentPattern(final Pattern argument) {
        this.argument = argument;
    }

    @Override
    public Optional<Map<String, Function>> visit(final Tangent tangent) {
        return argument.match(tangent.getParameter());
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new Tangent(
                mathContext,
                argument.replace(mathContext, subFunctions)
        );
    }
}
