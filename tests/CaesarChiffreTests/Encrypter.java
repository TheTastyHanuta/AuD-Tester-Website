public class Encrypter {

	private static String encrypt(String input, int shift) {
		char[] lettersPlainText = input.toCharArray();

		for (int i = 0; i < lettersPlainText.length; i++) {
			if (('a') <= lettersPlainText[i] && lettersPlainText[i] <= ('z')) {
				lettersPlainText[i] -= shift;
			}
		}

		return new String(lettersPlainText);
	}
	
	public static void main(String[] args) {
		String str = "sehr gut! du hast den code geknackt und dir so ruhm und ehre erworben. wie du siehst, " +
				"ist dieses verschluesselungsverfahren sehr leicht zu knacken. kompliziertere verfahren " +
				"werden in anderen veranstaltungen naeher betrachtet.";

		System.out.println(encrypt(str, -2));
	}
}
