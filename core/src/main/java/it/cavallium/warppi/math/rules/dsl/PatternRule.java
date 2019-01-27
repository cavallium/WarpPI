package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A <code>Rule</code> which uses <code>Pattern</code>s to match and replace functions.
 */
public class PatternRule implements Rule {
	private final String ruleName;
	private final RuleType ruleType;
	private final Pattern target;
	private final List<Pattern> replacements;

	public PatternRule(
			final String ruleName,
			final RuleType ruleType,
			final Pattern target,
			final List<Pattern> replacements
	) {
		this.ruleName = ruleName;
		this.ruleType = ruleType;
		this.target = target;
		this.replacements = replacements;
	}

	public PatternRule(
			final String ruleName,
			final RuleType ruleType,
			final Pattern target,
			final Pattern... replacements
	) {
		this(ruleName, ruleType, target, Arrays.asList(replacements));
	}

	@Override
	public String getRuleName() {
		return ruleName;
	}

	@Override
	public RuleType getRuleType() {
		return ruleType;
	}

	Pattern getTarget() {
		return target;
	}

	List<Pattern> getReplacements() {
		return replacements;
	}

	@Override
	public ObjectArrayList<Function> execute(final Function func) {
		return target.match(func)
				.map(subFunctions -> applyReplacements(func.getMathContext(), subFunctions))
				.orElse(null);
	}

	private ObjectArrayList<Function> applyReplacements(
			final MathContext mathContext,
			final Map<String, Function> subFunctions
	) {
		return replacements.stream()
				.map(replacement -> replacement.replace(mathContext, subFunctions))
				.collect(Collectors.toCollection(ObjectArrayList::new));
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof PatternRule)) {
			return false;
		}
		final PatternRule other = (PatternRule) o;
		return ruleName.equals(other.ruleName)
				&& ruleType == other.ruleType
				&& target.equals(other.target)
				&& replacements.equals(other.replacements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ruleName, ruleType, target, replacements);
	}
}
