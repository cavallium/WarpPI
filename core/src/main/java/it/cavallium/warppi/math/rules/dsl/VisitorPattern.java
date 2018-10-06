package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionVisitor;
import it.cavallium.warppi.math.functions.*;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.functions.equations.EquationsSystemPart;
import it.cavallium.warppi.math.functions.trigonometry.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A <code>Pattern</code> which implements <code>match</code> as a visitor.
 */
public abstract class VisitorPattern implements Pattern, FunctionVisitor<Optional<Map<String, Function>>> {
    @Override
    public Optional<Map<String, Function>> match(Function function) {
        return function.accept(this);
    }

    /**
     * Gathers captured sub-functions from two matches, checking for equality
     * of ones with the same name.
     *
     * @param match1 Sub-functions from one match.
     * @param match2 Sub-functions from the other match.
     * @return A <code>Map</code> containing all sub-functions, or an empty
     *         <code>Optional</code> if the same name is used to refer to
     *         non-equal sub-functions in the two matches.
     */
    protected Optional<Map<String, Function>> mergeMatches(
            Map<String, Function> match1,
            Map<String, Function> match2
    ) {
        if (!checkSubFunctionEquality(match1, match2)) {
            return Optional.empty();
        }

        Map<String, Function> merged = new HashMap<>();
        merged.putAll(match1);
        merged.putAll(match2);
        return Optional.of(merged);
    }

    private boolean checkSubFunctionEquality(Map<String, Function> match1, Map<String, Function> match2) {
        for (Map.Entry<String, Function> leftSubFunction : match1.entrySet()) {
            final String key = leftSubFunction.getKey();
            if (match2.containsKey(key)
                    && !match2.get(key).equals(leftSubFunction.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Map<String, Function>> visit(ArcCosine arcCosine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(ArcSine arcSine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(ArcTangent arcTangent) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Cosine cosine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Division division) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(EmptyNumber emptyNumber) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Equation equation) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(EquationsSystem equationsSystem) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(EquationsSystemPart equationsSystemPart) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Expression expression) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Joke joke) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Logarithm logarithm) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Multiplication multiplication) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Negative negative) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Number number) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Power power) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Root root) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(RootSquare rootSquare) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Sine sine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Subtraction subtraction) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(SumSubtraction sumSubtraction) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Sum sum) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Tangent tangent) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Undefined undefined) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(Variable variable) {
        return Optional.empty();
    }
}
