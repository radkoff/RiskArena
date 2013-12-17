package riskarena.riskbots.evaluation;

import java.text.DecimalFormat;

public class Utilities {
	public static String dec(double d) {
		return (new DecimalFormat("#.###").format(d)).toString();
	}
}
