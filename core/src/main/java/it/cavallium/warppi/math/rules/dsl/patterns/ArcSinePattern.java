package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the arcsine of another pattern.
 */
public class ArcSinePattern extends VisitorPattern {
    private final Pattern argument;

    public ArcSinePattern(final Pattern argument) {
        this.argument = argument;
    }

    @Override
    public Optional<Map<String, Function>> visit(final ArcSine arcSine) {
        return argument.match(arcSine.getParameter());
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new ArcSine(
                mathContext,
                argument.replace(mathContext, subFunctions)
        );
    }
}
