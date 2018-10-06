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
    public Optional<Map<String, Function>> match(final Function function) {
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
            final Map<String, Function> match1,
            final Map<String, Function> match2
    ) {
        if (!checkSubFunctionEquality(match1, match2)) {
            return Optional.empty();
        }

        final Map<String, Function> merged = new HashMap<>();
        merged.putAll(match1);
        merged.putAll(match2);
        return Optional.of(merged);
    }

    private boolean checkSubFunctionEquality(final Map<String, Function> match1, final Map<String, Function> match2) {
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
    public Optional<Map<String, Function>> visit(final ArcCosine arcCosine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final ArcSine arcSine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final ArcTangent arcTangent) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Cosine cosine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Division division) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final EmptyNumber emptyNumber) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Equation equation) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final EquationsSystem equationsSystem) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final EquationsSystemPart equationsSystemPart) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Expression expression) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Joke joke) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Logarithm logarithm) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Multiplication multiplication) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Negative negative) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Number number) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Power power) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Root root) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final RootSquare rootSquare) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Sine sine) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Subtraction subtraction) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final SumSubtraction sumSubtraction) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Sum sum) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Tangent tangent) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Undefined undefined) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Function>> visit(final Variable variable) {
        return Optional.empty();
    }
}
