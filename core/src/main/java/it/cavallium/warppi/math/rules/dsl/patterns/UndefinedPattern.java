package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Undefined;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates <code>Undefined</code>.
 */
public class UndefinedPattern extends VisitorPattern {
    @Override
    public Optional<Map<String, Function>> visit(Undefined undefined) {
        return Optional.of(new HashMap<>());
    }

    @Override
    public Function replace(MathContext mathContext, Map<String, Function> subFunctions) {
        return new Undefined(mathContext);
    }
}
