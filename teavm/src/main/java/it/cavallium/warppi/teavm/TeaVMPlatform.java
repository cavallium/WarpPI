package it.cavallium.warppi.teavm;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.html.HtmlEngine;
import it.cavallium.warppi.math.rules.RulesManager;
import it.cavallium.warppi.util.Error;

public class TeaVMPlatform implements Platform {

	private final TeaVMConsoleUtils cu;
	private final TeaVMGpio gi;
	private final TeaVMStorageUtils su;
	private final String on;
	private final Map<String, GraphicEngine> el;
	private final TeaVMPngUtils pu;
	private final TeaVMSettings settings;

	public TeaVMPlatform() {
		cu = new TeaVMConsoleUtils();
		gi = new TeaVMGpio();
		su = new TeaVMStorageUtils();
		pu = new TeaVMPngUtils();
		on = "JavaScript";
		el = new HashMap<>();
		el.put("HTML5 engine", new HtmlEngine());
		settings = new TeaVMSettings();
	}
	
	@Override
	public ConsoleUtils getConsoleUtils() {
		return cu;
	}

	@Override
	public Gpio getGpio() {
		return gi;
	}

	@Override
	public StorageUtils getStorageUtils() {
		return su;
	}

	@Override
	public PngUtils getPngUtils() {
		return pu;
	}

	@Override
	public TeaVMSettings getSettings() {
		return settings;
	}

	@Override
	public void setThreadName(Thread t, String name) {
	}

	@Override
	public void setThreadDaemon(Thread t) {
	}

	@Override
	public void setThreadDaemon(Thread t, boolean value) {
	}

	@Override
	public void exit(int value) {
		System.err.println("====================PROGRAM END====================");
	}

	@Override
	public void gc() {
		
	}

	@Override
	public boolean isJavascript() {
		return true;
	}

	@Override
	public String getOsName() {
		return on;
	}
	
	private boolean shift, alpha;
	
	@Override
	public void alphaChanged(boolean alpha) {
		this.alpha = alpha;
		HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha": ""));
	}

	@Override
	public void shiftChanged(boolean shift) {
		this.shift = shift;
		HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha": ""));
	}

	@Override
	public Semaphore newSemaphore() {
		return new TeaVMSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(int i) {
		return new TeaVMSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(URL[] urls) {
		return new TeaVMURLClassLoader(urls);
	}

	@Override
	public Map<String, GraphicEngine> getEnginesList() {
		return el;
	}

	@Override
	public GraphicEngine getEngine(String string) throws NullPointerException {
		return el.get(string);
	}

	@Override
	public void throwNewExceptionInInitializerError(String text) {
		throw new NullPointerException();
	}

	@Override
	public String[] stacktraceToString(Error e) {
		return e.getMessage().toUpperCase().replace("\r", "").split("\n");
	}

	@Override
	public void loadPlatformRules() {
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

	@Override
	public void zip(String targetPath, String destinationFilePath, String password) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public void unzip(String targetZipFilePath, String destinationFolderPath, String password) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public boolean compile(String[] command, PrintWriter printWriter, PrintWriter errors) {
		throw new java.lang.UnsupportedOperationException("Not implemented.");
	}

	@Override
	public boolean isRunningOnRaspberry() {
		return false;
	}

}
