import tester.annotations.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SecretClass
public class SignalPlotterSecretTest {
	// ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();
	
	// ====== OUTPUT REDIRECT ======
	private static final class OutputHelper {
		private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
		private static final PrintStream originalOut = System.out;
		private static final PrintStream originalErr = System.err;
		
		public static void startRedirect() {
			// Redirect program output
			System.setOut(new PrintStream(outContent));
			System.setErr(new PrintStream(errContent));
		}
		
		public static String readProgramOutput() {
			// read complete program output and clear stream for next execution
			byte[] bytes = outContent.toByteArray();
			resetStream();
			return new String(bytes, StandardCharsets.UTF_8);
		}
		
		public static void resetStream() {
			outContent.reset();
		}
		
		public static void endRedirect() {
			// Reset output streams
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
	}
	
	// ========= TEST DATA =========
	private static final Random RND = new Random(4711_0815_666L);

	// ============ TESTS ==========
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._0CODESTYLE, bonus = 1, comment = "Noch nicht korrigiert")
	public void pubTest__checkCodeStyle() {
		fail();
	}
	
	// Part 1: Constants
	String[] constantNames = new String[] { "FIRST_LIMIT", "SECOND_LIMIT", "NUMBER_OF_POINTS", "SAMPLING_RATE" };
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._1CONSTANTS, bonus = 1, comment = "Konstanten angelegt.")
	public void A01TestConstantsExist() {
		SignalPlotter fp = new SignalPlotter();
		
		TestcaseHelper.checkAttributesExist(fp.getClass(), constantNames);
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._1CONSTANTS, bonus = 1, comment = "Konstanten haben korrekten Typ.")
	public void A02TestConstantsType() {
		SignalPlotter fp = new SignalPlotter();
		Class<?>[] types = new Class<?>[] { double.class, double.class, int.class, int.class };

		TestcaseHelper.checkAttributesTypes(fp.getClass(), constantNames, types);
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._1CONSTANTS, bonus = 1, comment = "Konstanten haben korrekte Modifier.")
	public void A03TestConstantsModifier() {
		SignalPlotter fp = new SignalPlotter();
		int[] modifiers = new int[constantNames.length];
		Arrays.fill(modifiers, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

		TestcaseHelper.checkAttributesModifiers(fp.getClass(), constantNames, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._1CONSTANTS, bonus = 1, comment = "Konstanten haben korrekte Werte.")
	public void A04TestConstantsValue() {
		SignalPlotter fp = new SignalPlotter();
		Number[] values = new Number[] { -10.0, 10.0, 1000, 250 };

		// Test whether constants have the correct values
		for (int i = 0; i < constantNames.length; i++) {
			TestcaseHelper.assertEquals(values[i], TestcaseHelper.getFieldValue(fp.getClass(), fp, constantNames[i]),
					"Value of constant " + constantNames[i] + " wrong!");
		}
	}

	// Part 2: createSamplingPoints
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, bonus = 1, comment = "Basistestfaelle")
	public void B01TestCreateSampling() throws Exception {
		
		// Check default cases
		TestcaseHelper.assertArrayEquals(
				SignalPlotterRef.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT, SignalPlotterRef.SECOND_LIMIT,
						SignalPlotterRef.NUMBER_OF_POINTS),
				SignalPlotter.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT, SignalPlotterRef.SECOND_LIMIT,
						SignalPlotterRef.NUMBER_OF_POINTS),
				"createSamplingPoints failed for case FIRST_LIMIT, SECOND_LIMIT, NUMBER_OF_POINTS!");
		// TODO: check other cases
	}
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, bonus = 1, comment = "FIRST_LIMIT = SECOND_LIMIT")
	public void B02TestCreateSamplingTrivial() throws Exception {
		for (int i = 0; i < 20; i++) {
			int limit = (int) (RND.nextDouble() * 100);
			double[] res;
				res = SignalPlotter.createSamplingPoints(limit, limit, 20);
				TestcaseHelper.assertEquals(
						1,
						res.length,
						"createSamplingPoints failed for case where limits are equal.");
		}
	}
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, bonus = 1, comment = "Rueckgabewert korrekt.")
	public void B03TestCreateSamplingArrayReturn() {
		SignalPlotter fp = new SignalPlotter();
		TestcaseHelper.checkMethod(fp.getClass(),
				"createSamplingPoints(double, double, int)",
				double[].class,
				Modifier.PUBLIC | Modifier.STATIC);
	}
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._2CREATESAMPLINGPOINTS, bonus = 2, comment = "Berechnung korrekt.")
	public void B05TestCreateSamplingCalculation() throws Exception {
		// Check marginal cases

		// firstLimit == secondLimit == 0, valid NUMBER_OF_POINTS
//		Color.TestcaseHelper.assertArrayEquals(new double[] { 0 },
//				SignalPlotter.SignalPlotter.createSamplingPoints(0, 0, SignalPlotter.SignalPlotter.NUMBER_OF_POINTS),
//				"createSamplingPoints failed for case firstLimit == secondLimit and valid NUMBER_OF_POINTS!");
//
//		// firstLimit == secondLimit == 0, NUMBER_OF_POINTS == 0
//		Color.TestcaseHelper.assertArrayEquals(new double[] { 0 }, SignalPlotter.SignalPlotter.createSamplingPoints(0, 0, 0),
//				"createSamplingPoints failed for case firstLimit == secondLimit and NUMBER_OF_POINTS == 0!");
//
//		// firstLimit == secondLimit, NUMBER_OF_POINTS == 1
//		Color.TestcaseHelper.assertArrayEquals(new double[] { 0 }, SignalPlotter.SignalPlotter.createSamplingPoints(0, 0, 1),
//				"createSamplingPoints failed for case firstLimit == secondLimit and NUMBER_OF_POINTS == 1!");

		// firstLimit == 0, secondLimit == 1, NUMBER_OF_POINTS == 0
		TestcaseHelper.assertArrayEquals(new double[] {}, SignalPlotter.createSamplingPoints(0, 1, 0),
				"createSamplingPoints failed for case firstLimit == 0, secondLimit == 1, NUMBER_OF_POINTS == 0!");

		// firstLimit == 0, secondLimit == 1, NUMBER_OF_POINTS == 1
		TestcaseHelper.assertArrayEquals(new double[] { 1 }, SignalPlotter.createSamplingPoints(0, 1, 1),
				"createSamplingPoints failed for case firstLimit == 0, secondLimit == 1, NUMBER_OF_POINTS == 1!");

		// firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == 0
		int firstLimit = (int) (RND.nextDouble() * 100);
		int secondLimit = (int) (RND.nextDouble() * 100);
		TestcaseHelper.assertArrayEquals(new double[] {},
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, 0),
				"createSamplingPoints failed for case firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == 0!");

		// firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == 1
		TestcaseHelper.assertArrayEquals(new double[] { secondLimit },
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, 1),
				"createSamplingPoints failed for case firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == 1!");
		
		firstLimit = (int) (RND.nextDouble() * 100);
		secondLimit = (int) (RND.nextDouble() * 100);
		int numberOfPoints = (int) (RND.nextDouble() * 100);

		// firstLimit == random pos., secondLimit == random pos., NUMBER_OF_POINTS ==
		// random
		TestcaseHelper.assertArrayEquals(SignalPlotterRef.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				"createSamplingPoints failed for case firstLimit == random pos., secondLimit == random pos., NUMBER_OF_POINTS == random!");

		firstLimit = (int) (RND.nextDouble() * 100 - 50);
		secondLimit = (int) (RND.nextDouble() * 100 - 50);
		// firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == random
		TestcaseHelper.assertArrayEquals(SignalPlotterRef.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				"createSamplingPoints failed for case firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == random!");

		firstLimit = (int) (RND.nextDouble() * 100 - 200);
		secondLimit = (int) (RND.nextDouble() * 100 - 200);
		// firstLimit == random neg., secondLimit == neg., NUMBER_OF_POINTS == random
		TestcaseHelper.assertArrayEquals(SignalPlotterRef.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				"createSamplingPoints failed for case firstLimit == random neg., secondLimit == random neg., NUMBER_OF_POINTS == random!");

		firstLimit = (int) (RND.nextDouble() * 100 + 200);
		secondLimit = (int) (RND.nextDouble() * 100);
		// firstLimit == random pos., secondLimit == neg. (firstLimit > secondLimit),
		// NUMBER_OF_POINTS == random
		TestcaseHelper.assertArrayEquals(SignalPlotterRef.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				SignalPlotter.createSamplingPoints(firstLimit, secondLimit, numberOfPoints),
				"createSamplingPoints failed for case firstLimit == random pos., secondLimit == neg. (firstLimit > secondLimit), NUMBER_OF_POINTS == random!");
	}

	
	// Part 3: sigmoid
	@Test(timeout = 2500)
	@Points(exID = SignalPlotterPublicTest._3SIGMOID, bonus = 1, comment = "Sigmoid korrekt berechnet")
	public void C01TestSigmoid() {
		SignalPlotter fp = new SignalPlotter();
		TestcaseHelper.checkMethod(fp.getClass(),
				"sigmoid(double)",
				double.class,
				Modifier.PUBLIC | Modifier.STATIC);
		
		// Check sigmoid for different int values
		for (int i = -50; i <= 50; i++) {
			TestcaseHelper.assertEquals(SignalPlotterRef.sigmoid(i), SignalPlotter.sigmoid(i), 10e-8,
					"sigmoid failed for value: " + i + "!");
		}

		// Check sigmoid for different double values
		for (double i = -10.0; i <= 10.0; i += 0.001) {
			TestcaseHelper.assertEquals(SignalPlotterRef.sigmoid(i), SignalPlotter.sigmoid(i), 10e-8,
					"sigmoid failed for value: " + i + "!");
		}
	}
	
	
	// Part 4: applySigmoidToArray
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._4APPLYSIGMOIDTOARRAY, bonus = 1, comment = "Rueckgabewert korrekt")
	public void D01TestApplySigmoidReturn() {
		SignalPlotter fp = new SignalPlotter();
		TestcaseHelper.checkMethod(fp.getClass(),
				"applySigmoidToArray(double[])",
				double[].class,
				Modifier.PUBLIC | Modifier.STATIC);
	}
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._4APPLYSIGMOIDTOARRAY, bonus = 1, comment = "Berechnung korrekt")
	public void D02TestApplySigmoidToArray() throws Exception {
		// Check for firstLimit == FIRST_LIMIT, secondLimit == SECOND_LIMIT,
		// numberOfPoints = NUMBER_OF_POINTS
		double[] xs = SignalPlotter.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT, SignalPlotterRef.SECOND_LIMIT,
				SignalPlotterRef.NUMBER_OF_POINTS);
		double[] xsRef = SignalPlotterRef.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT,
				SignalPlotterRef.SECOND_LIMIT, SignalPlotterRef.NUMBER_OF_POINTS);

		TestcaseHelper.assertArrayEquals(SignalPlotterRef.applySigmoidToArray(xsRef),
				SignalPlotter.applySigmoidToArray(xs), "applySigmoidToArrays failed for default case!");

		// Check for firstLimit == random, secondLimit == random, NUMBER_OF_POINTS == random

		int[] randomValues = generateRandomValues(10);

		xs = SignalPlotterRef.createSamplingPoints(randomValues[0], randomValues[1], randomValues[2]);

		TestcaseHelper.assertArrayEquals(SignalPlotterRef.applySigmoidToArray(xs),
				SignalPlotter.applySigmoidToArray(xs),
				"applySigmoidToArray failed for case firstLimit == random, secondLimit == random, numberOfPoints == random!");

		// Check if applySigmoidToArray() actually creates a new array for the
		// sigmoid values or if it modifies the original array xs
		xs = SignalPlotter.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT, SignalPlotterRef.SECOND_LIMIT,
				SignalPlotterRef.NUMBER_OF_POINTS);
		double[] xsCopy = xs.clone();
		double[] ys = SignalPlotter.applySigmoidToArray(xs);
		TestcaseHelper.assertArrayEquals(xsCopy, xs, "applySigmoidToArray has modified the passed array xs!");
		TestcaseHelper.assertNotEquals(xs, ys, "applySigmoidToArray modified and returned xs!");
	}
	

	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._5PLOTSIGMOID, bonus = 2, comment = "Sigmoid korrekt angezeigt")
	public void E00TestPlotSigmoid() {
		PlotHelper.ph.callback2D = (xs, ys) -> {
			double[] xsExp = SignalPlotterRef.createSamplingPoints(SignalPlotterRef.FIRST_LIMIT, SignalPlotterRef.SECOND_LIMIT, SignalPlotterRef.NUMBER_OF_POINTS);
			double[] ysExp = SignalPlotterRef.applySigmoidToArray(xs);
			
			TestcaseHelper.assertArrayEquals(xsExp, xs, "xs is wrong.");
			TestcaseHelper.assertArrayEquals(ysExp, ys, "ys is wrong.");
		};

		SignalPlotter.plotSigmoid();
	}
	
	@Test(timeout = 500)
	@Points(exID = SignalPlotterPublicTest._6PLOTECG, bonus = 2, comment = "ECG korrekt berechnet")
	public void F00TestPlotEcgSignal() {
		// TODO: can't really test this in detail (so either it's 4 points or no points)
		PlotHelper.ph.callbackEcg = (time, ecg, timeR, r) -> {
			double[] ecgSignal = PlotHelper_2.readEcg("ecg.txt");
			double[] ecgTime;
			if (time[time.length-1] > 8.0) {
				ecgTime = SignalPlotterRef.createSamplingPoints(0, ecgSignal.length / (double) SignalPlotterRef.SAMPLING_RATE, ecgSignal.length);
			} else {
				ecgTime = SignalPlotterRef.createSamplingPoints(0, ecgSignal.length / SignalPlotterRef.SAMPLING_RATE, ecgSignal.length);
			}

			// 0.5 Punkte auf korrektes Anlegen der Variablen
			int[] idxRPeaks = PlotHelper_2.readPeaks("rpeaks.txt");
			double[] timeRPeaks = new double[idxRPeaks.length];
			double[] rPeaks = new double[idxRPeaks.length];

			// 0.5 Punkte auf Schleife
			for (int i = 0; i < idxRPeaks.length; i++) {
				// 1 Punkt auf korrektes Berechnen der R-Peak Punkte
				timeRPeaks[i] = ecgTime[idxRPeaks[i]];
				rPeaks[i] = ecgSignal[idxRPeaks[i]];
			}
						
			TestcaseHelper.assertArrayEquals(ecgTime, time, "time is wrong.");
			TestcaseHelper.assertArrayEquals(ecgSignal, ecg, "ecg is wrong.");
		};

		SignalPlotter.plotEcg();
	}
	
	
	@Test(timeout = 1000)
	@Points(exID = SignalPlotterPublicTest._6PLOTECG, bonus = 2, comment = "Peaks korrekt berechnet")
	public void F00TestPlotEcgRPeaks() {
		PlotHelper.ph.callbackEcg = (time, ecg, timeR, r) -> {
			double[] ecgSignal = PlotHelper_2.readEcg("ecg.txt");
			double[] ecgTime;
			if (time[time.length-1] > 8.0) {
				ecgTime = SignalPlotterRef.createSamplingPoints(0, ecgSignal.length / (double) SignalPlotterRef.SAMPLING_RATE, ecgSignal.length);
			} else {
				ecgTime = SignalPlotterRef.createSamplingPoints(0, ecgSignal.length / SignalPlotterRef.SAMPLING_RATE, ecgSignal.length);				
			}
			
			// 0.5 Punkte auf korrektes Anlegen der Variablen
			int[] idxRPeaks = PlotHelper_2.readPeaks("rpeaks.txt");
			double[] timeRPeaks = new double[idxRPeaks.length];
			double[] rPeaks = new double[idxRPeaks.length];

			// 0.5 Punkte auf Schleife
			for (int i = 0; i < idxRPeaks.length; i++) {
				// 1 Punkt auf korrektes Berechnen der R-Peak Punkte
				timeRPeaks[i] = ecgTime[idxRPeaks[i]];
				rPeaks[i] = ecgSignal[idxRPeaks[i]];
			}

			TestcaseHelper.assertArrayEquals(timeRPeaks, timeR, "timeRPeaks is wrong.");
			TestcaseHelper.assertArrayEquals(rPeaks, r, "rPeaks is wrong.");
		};

		SignalPlotter.plotEcg();
	}
	
	@Test(timeout = 1000)
	@Points(exID = SignalPlotterPublicTest._7COMPUTEHEARTRATE, bonus = 2, comment = "Herzrate korrekt berechnet und ausgegeben.")
	public void G00TestComputeHeartrate() {
		// redirect stdout again and check the output somehow
		OutputHelper.startRedirect();
		
		double[] ecgSignal = PlotHelper_2.readEcg("ecg.txt");
		double[] ecgTime = SignalPlotterRef.createSamplingPoints(0, ecgSignal.length / SignalPlotterRef.SAMPLING_RATE, ecgSignal.length);

		int[] idxRPeaks = PlotHelper_2.readPeaks("rpeaks.txt");
		double[] timeRPeaks = new double[idxRPeaks.length];

		for (int i = 0; i < idxRPeaks.length; i++) {
			timeRPeaks[i] = ecgTime[idxRPeaks[i]];
		}
		
		SignalPlotter.computeHeartRate(timeRPeaks);
		String output = OutputHelper.readProgramOutput();
		OutputHelper.endRedirect();
				
		String[] lines = output.split("\n");
		double[] bpms = new double[lines.length - 1];
		for (int i = 1; i < lines.length; i++) {
			bpms[i - 1] = Double.parseDouble(lines[i].replace(",", ".").replaceAll("[^0-9\\.]", "").trim());
		}
		
		double[] bpmsExp = new double[] {72.02, 67.87, 66.77, 67.87, 69.59, 68.15, 64.93, 66.23};
		
		assertEquals("Wrong amount of bpms printed.", bpmsExp.length, bpms.length);
		
		for (int i = 0; i < bpmsExp.length; i++) {
			assertEquals("BPMs differ in line " + (i + 1) + ".", bpmsExp[i], bpms[i], 1e-8);
		}		
	}
	
	public int[] generateRandomValues(int mult) {
		int firstLimit = (int) (RND.nextDouble() * mult);
		int secondLimit = (int) (RND.nextDouble() * mult);
		if (firstLimit == secondLimit) {
			secondLimit = (int) (RND.nextDouble() * mult);
		}
		int numberOfPoints = (int) (RND.nextDouble() * mult + 1);
		return new int[] { firstLimit, secondLimit, numberOfPoints };
	}
	
	public static class SignalPlotterRef {

		public static final double FIRST_LIMIT = -10.0;
		public static final double SECOND_LIMIT = 10.0;
		public static final int NUMBER_OF_POINTS = 1000;

		public static final int SAMPLING_RATE = 250;

		public static double[] createSamplingPoints(double firstLimit, double secondLimit, int numberOfPoints) {
			if (firstLimit == secondLimit) {
				numberOfPoints = 1;
			}

			double[] samplingPoints = new double[numberOfPoints];
			if (numberOfPoints == 1) {
				samplingPoints[0] = secondLimit;
				return samplingPoints;
			}

			double stepSize = (secondLimit - firstLimit) / (numberOfPoints - 1);

			for (int i = 0; i < samplingPoints.length; i++) {
				samplingPoints[i] = firstLimit + i * stepSize;
			}

			return samplingPoints;
		}

		////////////////////////////////////////////
		///////////// PLOT 2D (SIGMOID) ////////////
		////////////////////////////////////////////

		public static double sigmoid(double x) {
			return 1 / (1 + Math.exp(-x));
		}

		public static double[] applySigmoidToArray(double[] xs) {
			double[] ys = new double[xs.length];
			for (int y = 0; y < ys.length; y++) {
				ys[y] = sigmoid(xs[y]);
			}

			return ys;
		}

		public static void plotSigmoid() {
			double[] xs = createSamplingPoints(FIRST_LIMIT, SECOND_LIMIT, NUMBER_OF_POINTS);
			double[] ys = applySigmoidToArray(xs);

			PlotHelper_2.plot2D(xs, ys);
		}

		////////////////////////////////////////////
		//////////////// PLOT ECG //////////////////
		////////////////////////////////////////////

		public static void plotEcg() {
			double[] ecgSignal = PlotHelper_2.readEcg("ecg.txt");
			double[] ecgTime = createSamplingPoints(0, ecgSignal.length / SAMPLING_RATE, ecgSignal.length);

			int[] idxRPeaks = PlotHelper_2.readPeaks("rpeaks.txt");
			double[] timeRPeaks = new double[idxRPeaks.length];
			double[] rPeaks = new double[idxRPeaks.length];

			for (int i = 0; i < idxRPeaks.length; i++) {
				timeRPeaks[i] = ecgTime[idxRPeaks[i]];
				rPeaks[i] = ecgSignal[idxRPeaks[i]];
			}

			computeHeartRate(timeRPeaks);
			PlotHelper_2.plotEcg(ecgTime, ecgSignal, timeRPeaks, rPeaks);
		}

		public static void computeHeartRate(double[] timeRPeaks) {
			System.out.println("Heart Rate: ");
			for (int i = 0; i < timeRPeaks.length - 1; i++) {
				double hr = 60 / (timeRPeaks[i + 1] - timeRPeaks[i]);
				System.out.println(String.format("%.2f", hr) + " bpm");
			}
		}
	}
}
