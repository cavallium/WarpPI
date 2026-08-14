package it.cavallium.warppi.math.rules.functions;

import java.math.BigDecimal;
import java.math.MathContext;

import org.nevec.rjm.BigDecimalMath;

import it.cavallium.warppi.math.AngleMode;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class TrigonometryRule implements Rule {
	private static final MathContext PRECISION = new MathContext(Utils.scale, Utils.scaleMode2);

	@Override public String getRuleName() { return "Trigonometry"; }
	@Override public RuleType getRuleType() { return RuleType.CALCULATION; }

	@Override
	public ObjectArrayList<Function> execute(final Function function) throws Error {
		if (!(function instanceof FunctionSingle single)
				|| !(single.getParameter() instanceof Number number)
				|| function.getMathContext().exactMode)
			return null;
		try {
			final BigDecimal value;
			if (function instanceof Sine)
				value = BigDecimalMath.sin(toRadians(number.getTerm(), function.getMathContext().angleMode));
			else if (function instanceof Cosine)
				value = BigDecimalMath.cos(toRadians(number.getTerm(), function.getMathContext().angleMode));
			else if (function instanceof Tangent)
				value = BigDecimalMath.tan(toRadians(number.getTerm(), function.getMathContext().angleMode));
			else if (function instanceof ArcSine)
				value = fromRadians(BigDecimalMath.asin(number.getTerm()), function.getMathContext().angleMode);
			else if (function instanceof ArcCosine)
				value = fromRadians(BigDecimalMath.acos(number.getTerm()), function.getMathContext().angleMode);
			else if (function instanceof ArcTangent)
				value = fromRadians(BigDecimalMath.atan(number.getTerm()), function.getMathContext().angleMode);
			else
				return null;
			return new ObjectArrayList<>(new Function[] { new Number(function.getMathContext(), value) });
		} catch (final ArithmeticException exception) {
			throw (Error) new Error(Errors.NEGATIVE_PARAMETER,
					"The trigonometric argument is outside its domain.").initCause(exception);
		}
	}

	private static BigDecimal toRadians(final BigDecimal value, final AngleMode mode) throws Error {
		return switch (mode) {
			case RAD -> value;
			case DEG -> value.multiply(BigDecimalMath.pi(PRECISION)).divide(BigDecimal.valueOf(180), PRECISION);
			case GRA -> value.multiply(BigDecimalMath.pi(PRECISION)).divide(BigDecimal.valueOf(200), PRECISION);
		};
	}

	private static BigDecimal fromRadians(final BigDecimal value, final AngleMode mode) throws Error {
		return switch (mode) {
			case RAD -> value;
			case DEG -> value.multiply(BigDecimal.valueOf(180)).divide(BigDecimalMath.pi(PRECISION), PRECISION);
			case GRA -> value.multiply(BigDecimal.valueOf(200)).divide(BigDecimalMath.pi(PRECISION), PRECISION);
		};
	}
}
