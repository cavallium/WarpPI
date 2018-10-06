package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.dsl.Pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates any function as a named sub-function.
 */
public class SubFunctionPattern implements Pattern {
    private final String name;

    public SubFunctionPattern(String name) {
        this.name = name;
    }

    @Override
    public Optional<Map<String, Function>> match(Function function) {
        HashMap<String, Function> subFunctions = new HashMap<>();
        subFunctions.put(name, function);
        return Optional.of(subFunctions);
    }

    @Override
    public Function replace(MathContext mathContext, Map<String, Function> subFunctions) {
        return subFunctions.get(name);
    }
}
