package apple.util.rechercherExpression;

import java.util.HashSet;
import java.util.List;

/**
 * Common tools for all the research
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercheUtils {
	/**
	 * Converter from List[AppleMotRecherche] to String
	 * 
	 * @param listTermes - Coded termes
	 * @return - The String version of the list
	 */
	public static <AppleMotRecherche> String listAppleMotRechercheToString(List<AppleMotRecherche> listTermes) {
		StringBuilder expression = new StringBuilder(listTermes.getFirst().getMotCode());
		for (int i = 1; i < listTermes.size(); i++) {
			expression.append(" " + listTermes.get(i).getMotCode());
		}
		return expression.toString();
	}

	/**
	 * Returns true if the given set contains the given text, taking in charge the
	 * null case of the HashSet
	 * 
	 * @param set  - Set we're working with
	 * @param text - Text to search
	 * @return - boolean if the hashSet contains the text
	 */
	public static boolean hashContains(HashSet<String> set, String text) {
		if (set == null)
			return false;
		if (!set.contains(text))
			return false;
		return true;
	}

}
