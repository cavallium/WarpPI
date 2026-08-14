package it.cavallium.warppi.math.functions;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class Comparison extends FunctionOperator {
	public enum Operator {
		LESS_THAN('<'), LESS_OR_EQUAL('≤'), GREATER_THAN('>'), GREATER_OR_EQUAL('≥');

		private final char symbol;

		Operator(final char symbol) {
			this.symbol = symbol;
		}

		public char symbol() {
			return symbol;
		}
	}

	private final Operator operator;

	public Comparison(final MathContext context, final Operator operator,
			final Function left, final Function right) {
		super(context, left, right);
		this.operator = Objects.requireNonNull(operator, "operator");
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public Comparison clone() {
		return new Comparison(mathContext, operator,
				parameter1 == null ? null : parameter1.clone(),
				parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Comparison clone(final MathContext context) {
		return new Comparison(context, operator,
				parameter1 == null ? null : parameter1.clone(context),
				parameter2 == null ? null : parameter2.clone(context));
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof Comparison comparison && operator == comparison.operator
				&& Objects.equals(parameter1, comparison.parameter1)
				&& Objects.equals(parameter2, comparison.parameter2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(operator, parameter1, parameter2);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = parameter1.toBlock(context);
		result.add(new BlockChar(operator.symbol));
		result.addAll(parameter2.toBlock(context));
		return result;
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor,
			final Argument argument) {
		return visitor.visit(this, argument);
	}
}
