package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.parser.features.interfaces.FeatureBasic;
import it.cavallium.warppi.util.Error;

public class FeatureNumber implements FeatureBasic {
	private String numberString;

	public FeatureNumber(final char c) {
		numberString = c + "";
	}

	public FeatureNumber(final String s) {
		numberString = s;
	}

	public FeatureNumber() {
		numberString = "";
	}

	public String getNumberString() {
		return numberString;
	}

	public void append(final char ch) {
		numberString += ch;
	}

	@Override
	public Number toFunction(final MathContext context) throws Error {
		String nmbstr = getNumberString();
		if (nmbstr.charAt(0) == '.') {
			nmbstr = '0' + nmbstr;
		} else if (nmbstr.charAt(nmbstr.length() - 1) == '.') {
			nmbstr += "0";
		} else if (nmbstr.length() == 1) {
			if (nmbstr.charAt(0) == MathematicalSymbols.MINUS) {
				nmbstr += "1";
			} else if (nmbstr.charAt(0) == MathematicalSymbols.SUBTRACTION) {
				nmbstr += "1";
			}
		}
		return new Number(context, nmbstr);
	}
}
