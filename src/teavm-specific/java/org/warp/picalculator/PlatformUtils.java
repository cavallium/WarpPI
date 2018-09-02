package org.warp.picalculator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.warp.picalculator.gui.expression.blocks.Block;
import org.warp.picalculator.math.rules.RulesManager;

public final class PlatformUtils {
	public static final boolean isJavascript = true;
	public static String osName = "JavaScript";
	
	public static void setThreadName(Thread t, String string) {
	}

	public static void setDaemon(Thread kt) {
	}

	public static void setDaemon(Thread kt, boolean val) {
	}

	public static void throwNewExceptionInInitializerError(String string) {
		throw new NullPointerException(string);
	}

	public static String[] stacktraceToString(Error e) {
		return e.getMessage().toUpperCase().replace("\r", "").split("\n");
	}

	public static void loadPlatformRules() {
		RulesManager.addRule(new rules.functions.DivisionRule());
		RulesManager.addRule(new rules.functions.EmptyNumberRule());
		RulesManager.addRule(new rules.functions.ExpressionRule());
		RulesManager.addRule(new rules.functions.JokeRule());
		RulesManager.addRule(new rules.functions.MultiplicationRule());
		RulesManager.addRule(new rules.functions.NegativeRule());
		RulesManager.addRule(new rules.functions.NumberRule());
		RulesManager.addRule(new rules.functions.PowerRule());
		RulesManager.addRule(new rules.functions.RootRule());
		RulesManager.addRule(new rules.functions.SubtractionRule());
		RulesManager.addRule(new rules.functions.SumRule());
		RulesManager.addRule(new rules.functions.SumSubtractionRule());
		RulesManager.addRule(new rules.functions.VariableRule());
		RulesManager.addRule(new rules.ExpandRule1());
		RulesManager.addRule(new rules.ExpandRule2());
		RulesManager.addRule(new rules.ExpandRule5());
		RulesManager.addRule(new rules.ExponentRule1());
		RulesManager.addRule(new rules.ExponentRule2());
		RulesManager.addRule(new rules.ExponentRule3());
		RulesManager.addRule(new rules.ExponentRule4());
		RulesManager.addRule(new rules.ExponentRule8());
		RulesManager.addRule(new rules.ExponentRule9());
		RulesManager.addRule(new rules.ExponentRule15());
		RulesManager.addRule(new rules.ExponentRule16());
		RulesManager.addRule(new rules.ExponentRule17());
		RulesManager.addRule(new rules.FractionsRule1());
		RulesManager.addRule(new rules.FractionsRule2());
		RulesManager.addRule(new rules.FractionsRule3());
		RulesManager.addRule(new rules.FractionsRule4());
		RulesManager.addRule(new rules.FractionsRule5());
		RulesManager.addRule(new rules.FractionsRule6());
		RulesManager.addRule(new rules.FractionsRule7());
		RulesManager.addRule(new rules.FractionsRule8());
		RulesManager.addRule(new rules.FractionsRule9());
		RulesManager.addRule(new rules.FractionsRule10());
		RulesManager.addRule(new rules.FractionsRule11());
		RulesManager.addRule(new rules.FractionsRule12());
		RulesManager.addRule(new rules.FractionsRule14());
		RulesManager.addRule(new rules.NumberRule1());
		RulesManager.addRule(new rules.NumberRule2());
		RulesManager.addRule(new rules.NumberRule3());
		RulesManager.addRule(new rules.NumberRule4());
		RulesManager.addRule(new rules.NumberRule5());
		RulesManager.addRule(new rules.NumberRule7());
		RulesManager.addRule(new rules.UndefinedRule1());
		RulesManager.addRule(new rules.UndefinedRule2());
		RulesManager.addRule(new rules.VariableRule1());
		RulesManager.addRule(new rules.VariableRule2());
		RulesManager.addRule(new rules.VariableRule3());
	}

	public static void gc() {
	}

	private static boolean shift, alpha;
	
	public static void shiftChanged(boolean shift) {
		PlatformUtils.shift = shift;
		HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha": ""));
	}

	public static void alphaChanged(boolean alpha) {
		PlatformUtils.alpha = alpha;
		HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha": ""));
	}
	
}
