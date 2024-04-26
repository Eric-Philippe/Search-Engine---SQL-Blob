package apple.util.decoder;

/**
 * Converter Class directly linked to the Decoder
 * 
 * @author Eric PHILIPPE
 *
 */
public class Converter {
	/**
	 * Convert Binary to int
	 * 
	 * @param tabByte  - raw bytes
	 * @param longueur - length
	 * @return the bynary converte in int
	 */
	public static int binaryToInt(byte[] tabByte, int longueur) {
		int result = 0;
		int val;

		if (longueur == 0)
			return result;

		for (int i = 0; i < longueur; i++) {
			val = tabByte[i] & 0xFF;
			result = (result << 8) | val;
		}
		return result;
	}

	/**
	 * Convert the byte to String
	 * 
	 * @param chaine - Raw Bytes
	 * @param longueur - length
	 * @return - String
	 */
	public static String byteToString(byte[] chaine, int longueur) {
		char[] chars = new char[longueur];
		for (int i = 0; i < longueur; i++) {
			chars[i] = (char) (chaine[i] & 0xFF);
		}
		return new String(chars);
	}
	
	public static String binaryToString(String binaryTaux) {
		int taux = Integer.parseInt(binaryTaux);
		StringBuilder resultat = new StringBuilder("TAUX");
		int bitMask = 128;
		
		for (int i = 0; i < 8; i++) {
			if ((taux & bitMask) != 0) {
				if (resultat.length() == 5) {
					resultat.append(i);
				} else {
					resultat.append('+').append(i);
				}
			}
			
			bitMask >>= 1;
		}
		
		return resultat.toString();
	}

}
