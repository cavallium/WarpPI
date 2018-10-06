package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the arccosine of another pattern.
 */
public class ArcCosinePattern extends VisitorPattern {
    private final Pattern argument;

    public ArcCosinePattern(final Pattern argument) {
        this.argument = argument;
    }

    @Override
    public Optional<Map<String, Function>> visit(final ArcCosine arcCosine) {
        return argument.match(arcCosine.getParameter());
    }

    @Override
    public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
        return new ArcCosine(
                mathContext,
                argument.replace(mathContext, subFunctions)
        );
    }
}
