import tester.annotations.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

@SecretClass
public class CaesarChiffreSecretTest {
    // ========== SYSTEM ===========
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//
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

    // ====== TESTS ======
    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._0CODESTYLE, bonus = 1, comment = "Noch nicht korrigiert")
    public void secTest__checkCodeStyle() {
        fail();
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._1CONSTANTS, bonus = 0.5, comment = "Konstanten korrekt angelegt.")
    public void secTest__checkConstants() {
        CaesarChiffre cc = new CaesarChiffre();

        String[] names = new String[]{"GERMAN_LANGUAGE_PATTERN", "ENCRYPTED_MESSAGE", "SEPARATOR"};
        Object[] values = new Object[]{CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN, CaesarChiffreRef.ENCRYPTED_MESSAGE,
                CaesarChiffreRef.SEPARATOR};
        Class<?>[] types = new Class<?>[]{String.class, String.class, char.class};
        int[] modifiers = new int[names.length];
        Arrays.fill(modifiers, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

        // Check if constants exist, if they have the right modifiers, the right types
        // and the correct values
        TestcaseHelper.checkAttributes(cc.getClass(), names, types, modifiers);
        for (int i = 0; i < names.length; i++) {
            TestcaseHelper.assertEquals(values[i], TestcaseHelper.getFieldValue(cc.getClass(), cc, names[i]),
                    "Value of constant " + names[i] + "wrong!");
        }
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._2GETINDEXOFMAXIMUMENTRY, bonus = 1.5, comment = "getIndexOfMaximumEntry funktioniert.")
    public void secTest__getIndexOfMaximumEntry() throws Exception {
        // ascending values
        int[] testValues = new int[]{1, 2, 3, 4, 5};
        assertEquals("getIndexOfMaximumEntry failed for ascending values!",
                CaesarChiffreRef.getIndexOfMaximumEntry(testValues), CaesarChiffre.getIndexOfMaximumEntry(testValues));

        // descending values
        testValues = new int[]{5, 4, 3, 2, 1, 0};
        assertEquals("getIndexOfMaximumEntry failed for descending values!",
                CaesarChiffreRef.getIndexOfMaximumEntry(testValues), CaesarChiffre.getIndexOfMaximumEntry(testValues));

        // test for multiple maxima (should return the 1st occurence)
        testValues = new int[]{5, 5, 1, 2, 3, 5};
        assertEquals("getIndexOfMaximumEntry failed for multiple maxima!",
                CaesarChiffreRef.getIndexOfMaximumEntry(testValues), CaesarChiffre.getIndexOfMaximumEntry(testValues));

        // Random length, only zeros
        testValues = new int[(int) (Math.random() * 1000)];
        for (int i = 0; i < testValues.length; i++) {
            testValues[i] = 0;
        }
        assertEquals("getIndexOfMaximumEntry failed for zeros!", CaesarChiffreRef.getIndexOfMaximumEntry(testValues),
                CaesarChiffre.getIndexOfMaximumEntry(testValues));

        // Random length, random positive and negative values
        testValues = new int[(int) (Math.random() * 1000)];
        for (int i = 0; i < testValues.length; i++) {
            testValues[i] = (int) (Math.random() * 100 - 50);
        }
        assertEquals("getIndexOfMaximumEntry failed for random values!",
                CaesarChiffreRef.getIndexOfMaximumEntry(testValues), CaesarChiffre.getIndexOfMaximumEntry(testValues));
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._3GETHISTOGRAM, bonus = 0.5, comment = "Histogram hat richtige Laenge")
    public void secTest__getHistogram__length() throws Exception {
        // check whether histogram has length 128 or 256
        int[] histogram = CaesarChiffre.getHistogram("");
        assertTrue("Histogram should be 128 or 256 in length to cover ASCII characters.",
                histogram.length == 128 || histogram.length == 256);
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._3GETHISTOGRAM, bonus = 0.5, comment = "Konvertiert zu Kleinbuchstaben")
    public void secTest__getHistogram__onlyLowercase() throws Exception {
        // check whether toLowerCase was called (check output for two inputs that only
        // differ in case and compare output)
        int[] hist1 = CaesarChiffre.getHistogram(CaesarChiffreRef.ENCRYPTED_MESSAGE);
        int[] hist2 = CaesarChiffre.getHistogram(CaesarChiffreRef.ENCRYPTED_MESSAGE.toUpperCase());

        assertArrayEquals("Did you convert the input to lowercase?", hist1, hist2);

        hist1 = CaesarChiffre.getHistogram(CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN);
        hist2 = CaesarChiffre.getHistogram(CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN.toUpperCase());

        assertArrayEquals("Did you convert the input to lowercase?", hist1, hist2);
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._3GETHISTOGRAM, bonus = 0.5, comment = "Separator ignoriert")
    public void secTest__getHistogram__separator() throws Exception {
        // check whether the histogram has value 0 for spaces
        int[] hist = CaesarChiffre.getHistogram(CaesarChiffreRef.ENCRYPTED_MESSAGE);
        assertEquals("Separator included in histogram", 0, hist[' ']);
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._3GETHISTOGRAM, bonus = 1, comment = "Funktion")
    public void secTest__getHistogram__function() throws Exception {
        // check whether the histogram is correctly calculated
        assertArrayEquals("getHistogram() failed for ENCRYPTED_MESSAGE",
                CaesarChiffreRef.getHistogram(CaesarChiffreRef.ENCRYPTED_MESSAGE),
                CaesarChiffre.getHistogram(CaesarChiffreRef.ENCRYPTED_MESSAGE));

        assertArrayEquals("getHistogram() failed for GERMAN_LANGUAGE_PATTERN",
                CaesarChiffreRef.getHistogram(CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN),
                CaesarChiffre.getHistogram(CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN));

        char[] randomText = new char[(int) (Math.random() * 1000) + 1000];
        for (int i = 0; i < randomText.length; i++) {
            randomText[i] = (char) (Math.random() * 26 + 97);
        }
        assertArrayEquals("getHistogram() failed for random Text!",
                CaesarChiffreRef.getHistogram(new String(randomText)),
                CaesarChiffre.getHistogram(new String(randomText)));
    }

    private static void helper__getSignificantLetter(String text) throws Exception {
        int[] histogram = CaesarChiffreRef.getHistogram(text);

        char significantLetterExp = (char) CaesarChiffreRef.getIndexOfMaximumEntry(histogram);
        int quantityExp = histogram[significantLetterExp];
        int quotaExp1 = (int) (((double) quantityExp) / text.length() * 100.0);
        int quotaExp2 = (int) (((double) quantityExp) / text.replace(" ", "").length() * 100.0);

        // redirect stdout again and check the output somehow
        OutputHelper.startRedirect();

        char significantLetter = CaesarChiffre.getSignificantLetter(text);

        String output = OutputHelper.readProgramOutput();
        OutputHelper.endRedirect();

        String lastLine = output.split("\n")[1];
        lastLine = lastLine.replaceAll("[^\\d(]", "");
        int quantity = Integer.parseInt(lastLine.split("\\(")[0]);
        int quota = Integer.parseInt(lastLine.split("\\(")[1]);

        assertEquals("Wrong significant letter", significantLetterExp, significantLetter);
        assertEquals("Wrong quantity", quantityExp, quantity);
        if (quota != quotaExp1 && quota != quotaExp2) {
            assertEquals("Wrong quota", quotaExp1, quota);
        }
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._4GETSIGNIFICANTLETTER, bonus = 2, comment = "Funktion")
    public void secTest__getSignificantLetter() throws Exception {
        helper__getSignificantLetter(CaesarChiffreRef.ENCRYPTED_MESSAGE);
        helper__getSignificantLetter(CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN);

        char[] randomText = new char[(int) (Math.random() * 1000) + 1000];
        for (int i = 0; i < randomText.length; i++) {
            randomText[i] = (char) (Math.random() * 26 + 97);
        }
        helper__getSignificantLetter(new String(randomText));
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._5GETSHIFT, bonus = 1, comment = "Gegebener Text")
    public void pubTest__checkGetShift() throws Exception {
        assertEquals("getShift failed for default values!",
                Math.abs(CaesarChiffreRef.getShift(CaesarChiffreRef.ENCRYPTED_MESSAGE, CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)),
                Math.abs(CaesarChiffre.getShift(CaesarChiffreRef.ENCRYPTED_MESSAGE, CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)));

    }

    // TODO: PRO SEMESTER NEUE TEXTE ZUM TESTEN GENERIEREN!!!!!
    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._5GETSHIFT, bonus = 0.5, comment = "Anderer Text")
    public void pubTest__checkGetShift_other() throws Exception {
        assertEquals("getShift failed for other values!", Math.abs(CaesarChiffreRef.getShift(
                        "96CK=:496? 8=F64<HF?D49! 5F 92DE 56? 4@56 86<?24<E F?5 5:C 6:?6? 5C:?< 36: 56C >65:K:?E649?:<6C76:6C :> K:C<6= G6C5:6?E. H6?? 5F 5@CE 86?2FD@ D49?6== F?5 56C 6CDE6 3:DE[ 56C 52D 4@56H@CE A@=J>@CA9:6 2? 56C 32C ?6??E, 36<@>>DE 5F 6:?6? 5C:?< 2F7D 92FD. G:6= DA2DD!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)),
                Math.abs(CaesarChiffre.getShift(
                        "96CK=:496? 8=F64<HF?D49! 5F 92DE 56? 4@56 86<?24<E F?5 5:C 6:?6? 5C:?< 36: 56C >65:K:?E649?:<6C76:6C :> K:C<6= G6C5:6?E. H6?? 5F 5@CE 86?2FD@ D49?6== F?5 56C 6CDE6 3:DE[ 56C 52D 4@56H@CE A@=J>@CA9:6 2? 56C 32C ?6??E, 36<@>>DE 5F 6:?6? 5C:?< 2F7D 92FD. G:6= DA2DD!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)));

        assertEquals("getShift failed for other values!", Math.abs(CaesarChiffreRef.getShift(
                        "ifs{mjdifo hmvfdlxvotdi! ev ibtu efo dpef hflobdlu voe ejs fjofo esjol cfj efs nfej{joufdiojlfsgfjfs jn {jslfm wfsejfou. xfoo ev epsu hfobvtp tdiofmm voe efs fstuf cjtu, efs ebt dpefxpsu qpmznpsqijf bo efs cbs ofoou, cflpnntu ev fjofo esjol bvgt ibvt. wjfm tqbtt!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)),
                Math.abs(CaesarChiffre.getShift(
                        "ifs{mjdifo hmvfdlxvotdi! ev ibtu efo dpef hflobdlu voe ejs fjofo esjol cfj efs nfej{joufdiojlfsgfjfs jn {jslfm wfsejfou. xfoo ev epsu hfobvtp tdiofmm voe efs fstuf cjtu, efs ebt dpefxpsu qpmznpsqijf bo efs cbs ofoou, cflpnntu ev fjofo esjol bvgt ibvt. wjfm tqbtt!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN)));
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._6DECODE, bonus = 1, comment = "Gegebener Text")
    public void pubTest__checkDecode1() throws Exception {
        assertEquals("decode failed for default values!",
                CaesarChiffreRef.decode(CaesarChiffreRef.ENCRYPTED_MESSAGE, CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN),
                CaesarChiffre.decode(CaesarChiffreRef.ENCRYPTED_MESSAGE, CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN));
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._6DECODE, bonus = 1, comment = "Anderer Text 1")
    public void pubTest__checkDecode2() throws Exception {
        assertEquals("decode failed for other values!", CaesarChiffreRef.decode(
                        "96CK=:496? 8=F64<HF?D49! 5F 92DE 56? 4@56 86<?24<E F?5 5:C 6:?6? 5C:?< 36: 56C >65:K:?E649?:<6C76:6C :> K:C<6= G6C5:6?E. H6?? 5F 5@CE 86?2FD@ D49?6== F?5 56C 6CDE6 3:DE[ 56C 52D 4@56H@CE A@=J>@CA9:6 2? 56C 32C ?6??E, 36<@>>DE 5F 6:?6? 5C:?< 2F7D 92FD. G:6= DA2DD!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN),
                CaesarChiffre.decode(
                        "96CK=:496? 8=F64<HF?D49! 5F 92DE 56? 4@56 86<?24<E F?5 5:C 6:?6? 5C:?< 36: 56C >65:K:?E649?:<6C76:6C :> K:C<6= G6C5:6?E. H6?? 5F 5@CE 86?2FD@ D49?6== F?5 56C 6CDE6 3:DE[ 56C 52D 4@56H@CE A@=J>@CA9:6 2? 56C 32C ?6??E, 36<@>>DE 5F 6:?6? 5C:?< 2F7D 92FD. G:6= DA2DD!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN));
    }

    @Test(timeout = 500)
    @Points(exID = CaesarChiffrePublicTest._6DECODE, bonus = 1, comment = "Anderer Text 2")
    public void pubTest__checkDecode3() throws Exception {
        assertEquals("decode failed for other values!", CaesarChiffreRef.decode(
                        "ifs{mjdifo hmvfdlxvotdi! ev ibtu efo dpef hflobdlu voe ejs fjofo esjol cfj efs nfej{joufdiojlfsgfjfs jn {jslfm wfsejfou. xfoo ev epsu hfobvtp tdiofmm voe efs fstuf cjtu, efs ebt dpefxpsu qpmznpsqijf bo efs cbs ofoou, cflpnntu ev fjofo esjol bvgt ibvt. wjfm tqbtt!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN),
                CaesarChiffre.decode(
                        "ifs{mjdifo hmvfdlxvotdi! ev ibtu efo dpef hflobdlu voe ejs fjofo esjol cfj efs nfej{joufdiojlfsgfjfs jn {jslfm wfsejfou. xfoo ev epsu hfobvtp tdiofmm voe efs fstuf cjtu, efs ebt dpefxpsu qpmznpsqijf bo efs cbs ofoou, cflpnntu ev fjofo esjol bvgt ibvt. wjfm tqbtt!",
                        CaesarChiffreRef.GERMAN_LANGUAGE_PATTERN));
    }

    public static class CaesarChiffreRef {

        private static final String GERMAN_LANGUAGE_PATTERN = "Werden zwei Glasstaebe mit einem "
                + "Wolltuch gerieben, dann kann man feststellen, dass sich die beiden "
                + "Staebe gegenseitig abstossen. Wird das gleiche Experiment mit zwei "
                + "Kunststoffstaeben wiederholt, dann bleibt das Ergebnis gleich, auch "
                + "diese beiden Staebe stossen sich gegenseitig ab. Im Gegensatz dazu ziehen "
                + "sich ein Glas und ein Kunststoffstab gegenseitig an. Diese mit den Gesetzen "
                + "der Mechanik nicht zu erklaerende Erscheinung fuehrt man auf Ladungen zurueck. "
                + "Da sowohl Anziehung als auch Abstossung auftritt, muessen zwei verschiedene "
                + "Arten von Ladungen existieren. Man unterscheidet daher positive und negative Ladungen.";

        public static final String ENCRYPTED_MESSAGE = "ugjt iwv! fw jcuv fgp eqfg igmpcemv wpf fkt uq twjo wpf gjtg gtyqtdgp. pqtocngtygkug mcpp ocp jkgt inwgjygkp qfgt cgjpnkejgu igykppgp. fkgu hcgnnv kp fkgugo ugoguvgt ngkfgt cwu... uqtt{";


        private static final char SEPARATOR = ' ';

        public static int getIndexOfMaximumEntry(int[] values) {
            int maxIndex = 0;

            for (int i = 0; i < values.length; i++) {
                if (values[i] > values[maxIndex]) {
                    maxIndex = i;
                }
            }
            return maxIndex;
        }

        public static int[] getHistogram(String text) {
            int[] histogram = new int[128];

            text = text.toLowerCase();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == SEPARATOR) {
                    continue;
                }
                histogram[c]++;
            }
            return histogram;
        }

        public static char getSignificantLetter(String text) {
            int[] histogram = getHistogram(text);

            char significantLetter = (char) getIndexOfMaximumEntry(histogram);
            int quantity = histogram[significantLetter];
            int quota = (int) (((double) quantity) / text.length() * 100.0);

            System.out.println("Most significant letter: " + significantLetter);
            System.out.println("Quantity: " + quantity + " times (" + quota + " % of whole text)");

            return significantLetter;
        }

        public static int getShift(String encryptedText, String languagePattern) {
            char sigOfChiffre = getSignificantLetter(encryptedText);
            char sigOfPattern = getSignificantLetter(languagePattern);

            int shift = sigOfPattern - sigOfChiffre;

            System.out.println("Most significant letter in the pattern text: " + sigOfPattern);
            System.out.println("Most significant letter in the encrypted text: " + sigOfChiffre);
            System.out.println("Resulting shift: " + shift);

            return shift;
        }

        public static String decode(String encryptedText, String languagePattern) {
            int shift = getShift(encryptedText, languagePattern);

            char[] lettersEncryptedText = encryptedText.toCharArray();

            for (int i = 0; i < lettersEncryptedText.length; i++) {
                if (('a' - shift) <= lettersEncryptedText[i] && lettersEncryptedText[i] <= ('z' - shift)) {
                    lettersEncryptedText[i] += shift;
                }
            }

            return new String(lettersEncryptedText);
        }
    }
}
