import tester.annotations.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Exercises({ @Ex(exID = SignalPlotterPublicTest._0CODESTYLE, points = 2),
		@Ex(exID = SignalPlotterPublicTest._1CONSTANTS, points = 2),
		@Ex(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, points = 5),
		@Ex(exID = SignalPlotterPublicTest._3SIGMOID, points = 1),
		@Ex(exID = SignalPlotterPublicTest._4APPLYSIGMOIDTOARRAY, points = 2),
		@Ex(exID = SignalPlotterPublicTest._5PLOTSIGMOID, points = 2),
		@Ex(exID = SignalPlotterPublicTest._6PLOTECG, points = 4),
		@Ex(exID = SignalPlotterPublicTest._7COMPUTEHEARTRATE, points = 2) })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignalPlotterPublicTest {
	public static final String _0CODESTYLE = "0. Coderichtlinien";
	public static final String _1CONSTANTS = "1. Konstanten";
	public static final String _2CREATESAMPLINGPOINTS = "2. createSamplingPoints";
	public static final String _3SIGMOID = "3. sigmoid";
	public static final String _4APPLYSIGMOIDTOARRAY = "4. applySigmoidToArray";
	public static final String _5PLOTSIGMOID = "5. plotSigmoid";
	public static final String _6PLOTECG = "6. plotEcg";
	public static final String _7COMPUTEHEARTRATE = "7. computeHeartrate";

//	@Rule
//	public final PointsLogger pl = new PointsLogger();
//
//	@ClassRule
//	public final static PointsSummary ps = new PointsSummary();

	static SignalPlotter sp = new SignalPlotter();

	@Test(timeout = 500)
	@Points(exID = _0CODESTYLE, bonus = 1e-8, malus = 1, comment = "Noch nicht korrigiert")
	public void pubTest__checkCodeStyle() {
	}
	
	@Test(timeout = 500)
	@Points(exID = _1CONSTANTS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkConstantsExist() {
		try {
			// werden nur per Reflection getestet
//			assertNotNull("FIRST_LIMIT missing.", SignalPlotter.SignalPlotter.FIRST_LIMIT);
//			assertNotNull("SECOND_LIMIT missing.", SignalPlotter.SignalPlotter.SECOND_LIMIT);
//			assertNotNull("NUMBER_OF_POINTS missing.", SignalPlotter.SignalPlotter.NUMBER_OF_POINTS);
//			assertNotNull("SAMPLING_RATE missing.", SignalPlotter.SignalPlotter.SAMPLING_RATE);
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkCreateSamplingPointsExists() {
		try {
			SignalPlotter.createSamplingPoints(0, 1, 10);
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._3SIGMOID, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkSigmoidExists() {
		try {
			SignalPlotter.sigmoid(5);
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._4APPLYSIGMOIDTOARRAY, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkApplySigmoidToArrayExists() {
		try {
			SignalPlotter.applySigmoidToArray(new double[] { 0.0, 0.1, 0.2 });
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 1000)
	@Points(exID = SignalPlotterPublicTest._5PLOTSIGMOID, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkPlotSigmoidExists() {
		try {
			SignalPlotter.plotSigmoid();
		} catch (Exception e) {
			
		}
	}

	@Test(timeout = 1000)
	@Points(exID = SignalPlotterPublicTest._6PLOTECG, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkPlotEcgExists() {
		try {
			SignalPlotter.plotEcg();
		} catch(Exception e) {
			
		}
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._7COMPUTEHEARTRATE, malus = 1000, bonus = 0.000001, comment = "Existiert?")
	public void pubTest__checkComputeHeartrateExists() {
		try {
			SignalPlotter.computeHeartRate(new double[] { 0.0, 0.1, 0.2 });
		} catch (Exception e) {
			// we only care about the existence of computeHeartRate here
		}
	}
}
