package apple.util.rechercherExpression.solutionRecherche;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.util.Pair;
import apple.util.rechercherExpression.ContexteRecherche;

/**
 * Classe impl�mentant le n�cessaire afin de recherche de mani�re efficace dans
 * les textes suppos�s solutions � l'aide des REGEX.
 * 
 * <p>
 * On use ici des regex afin d'impl�menter l'utilisation des Jokers d�finis dans
 * {@link ContexteRecherche} et �galement �viter une recherche na�ve impliquant
 * l'utilisation de boucle na�ve
 * 
 * <pre>
 * for (String word : texte)
 * </pre>
 * 
 * <p>
 * Les r�sultas sont retourn�s sous une surcouche s�rialis�e de la classe
 * {@link Pair} de javafx. Voir : {@link PairSolution}
 * 
 * <p>
 * 
 * @author Eric PHILIPPE
 *
 */
public class ExpressionMatcher extends RegexBuilder {
	public static Pattern getCompiledRegex(List<List<String>> synonyms) {
		return new RegexBuilder(synonyms).build();
	}

	/**
	 * Match all the synonyms in the text
	 *
	 * @param regex     a pre-compiled regex, use {@link ExpressionMatcher.getCompiledRegex()}
	 * @param text      the text to match
	 * @param inComment if the text is in a comment
	 * @return a list of pair of start and end index of the match
	 */
	public static List<PairSolution> matchAll(Pattern regex, String text, boolean inComment) {
		List<PairSolution> matches = new ArrayList<>();
		Matcher matcher = regex.matcher(text);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String expressionTrouvee = text.substring(start, end); // "\r\r METTRE" "/*052020"
			if (expressionTrouvee.startsWith("/*")) {
				expressionTrouvee = expressionTrouvee.substring(2);
				start += 2;
			}
			if (expressionTrouvee.endsWith("*/")) {
				expressionTrouvee = expressionTrouvee.substring(0, expressionTrouvee.length() - 2);
				end -= 2;
			}
			
			if (expressionTrouvee.contains("/*") || expressionTrouvee.contains("*/")) continue;
			
			if (!inComment && isExpressionInComment(text, matcher.start(), matcher.end()))
				continue;
			
			String expressionTrimmed = expressionTrouvee.trim(); // "METTRE"
			start += expressionTrouvee.indexOf(expressionTrimmed);
			end = start + expressionTrimmed.length();
			
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				int numNewlinesBeforeMatch = text.substring(0, start).split("\n", -1).length - 1;
				start = start - numNewlinesBeforeMatch;
				end = end - numNewlinesBeforeMatch;
				
				int numNewlinesInExpression = expressionTrouvee.split("\n", -1).length - 1;
				if (numNewlinesInExpression > 2) end -= numNewlinesInExpression - 2;
			}
						
			matches.add(new PairSolution(start, end));
		}

		return matches;
	}

	/**
	 * Check if the expression is in a comment Rmq: the expression is in a comment
	 * if the expression is between a /* and a * /
	 *
	 * @param text  the text to check
	 * @param start the start of the expression
	 * @param end   the end of the expression
	 * @return true if the expression is in a comment
	 */
	private static boolean isExpressionInComment(String text, int start, int end) {
		// Construire l'expression r�guli�re pour rechercher un commentaire
		String regex = "/\\*.*?\\*/"; // correspond � un commentaire d�limit� par /* et */
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL); // DOTALL pour inclure les sauts de ligne dans le .*

		// V�rifier si l'expression est dans un commentaire
		String texteAvantExpression = text.substring(0, start);
		String texteApresExpression = text.substring(end);
		String texteAvantCommentaire = pattern.matcher(texteAvantExpression).replaceAll("");
		String texteApresCommentaire = pattern.matcher(texteApresExpression).replaceAll("");
		boolean estDansCommentaire = texteAvantCommentaire.contains("/*") && texteApresCommentaire.contains("*/");
		return estDansCommentaire;		
	}

	/**
	 * Removes all the non hyphen dash in a text
	 * 
	 * @param input - Text Input
	 * @return the text without the non hyphen dash
	 */
	public static String replaceHyphen(String texte) {
		// Cr�er une cha�ne de caract�res vide pour stocker le r�sultat
		StringBuilder resultat = new StringBuilder();

		// Parcourir chaque caract�re du texte
		for (int i = 0; i < texte.length(); i++) {
			char c = texte.charAt(i);

			// Si le caract�re est un tiret
			if (c == '-') {
				// V�rifier s'il est isol� (ne pas avoir de tiret avant ni apr�s)
				boolean estIsol� = true;
				if (i > 0 && texte.charAt(i - 1) == '-') {
					estIsol� = false;
				}
				if (i < texte.length() - 1 && texte.charAt(i + 1) == '-') {
					estIsol� = false;
				}

				// Si le tiret n'est pas isol�, le remplacer par un espace
				if (!estIsol�) {
					resultat.append(' ');
				} else {
					resultat.append('-');
				}
			} else {
				resultat.append(c);
			}
		}

		// Ajouter des espaces � la fin pour que la longueur de la cha�ne soit toujours
		// la m�me
		int longueurFinale = texte.length();
		while (resultat.length() < longueurFinale) {
			resultat.append(' ');
		}

		// Renvoyer le r�sultat sous forme de cha�ne de caract�res
		return resultat.toString();
	}

}

/**
 * Class allowing to build regex steps by steps (or nodes by nodes)
 * Contains a GREEDINESS constant allowing to search further in the text for the jokers,
 * but costs a lot in terms of CPU at the execution
 * 
 * @author ephilipp
 *
 */
class RegexBuilder {
	final static String UNLIMITED_WILDCARD = ContexteRecherche.getStringUnlimitedJoker();
	final static String LIMITED_WILDCARD = ContexteRecherche.getStringLimitedJoker();
	
	static final int GREEDINESS_WORDS = 35; // How far do we allow the regex to search for an unlimited joker / spaces
	static final int GREEDINESS_SPACES = 100; // How many spaces char do we allow for the regex to consider before stopping
	static final int GREEDINESS_CHAR = 81; // Currently setup with the maximum length of a single word, useless to search further it when considering a single word

	/** 
	 * @WORD_BOUNDARY Constants
	 * Possible optimisation : Remove the parenthesis if it doesn't break anything to remove a useless Capturing Group
	 * Less Capturing group the better is for readability and performance
	 */
	static final String LIGHT_SPACE = "(\\s{0," + GREEDINESS_SPACES + "})"; 
	static final String WORD_BOUNDARY = "(\\s{1," + GREEDINESS_SPACES + "})";
	static final String WORD_BOUNDARY_START = "(?:^|\\s\\(|\\s{1," + GREEDINESS_SPACES + "})";
	static final String WORD_BOUNDARY_END = "(?:$|\\)\\s|\\s{1," + GREEDINESS_SPACES + "})";
	
	/**
	 * @ISOLATED_JOKER Constants Regex interpretation
	 */
	static final String ISOLATED_UNLIMITED = "([\\p{Graph}]{1," + GREEDINESS_CHAR + "}\\s{1," + GREEDINESS_SPACES + "}){0," + GREEDINESS_WORDS + "}"; // ((Wathever characters (minimum one)) with a space)(0 to GREEDINESS VALUE times)
	static final String ISOLATED_LIMITED = "[\\p{Graph}]{1," + GREEDINESS_CHAR + "}\\s{0," + GREEDINESS_SPACES + "}"; // [\\p{Graph}]+\\s{0,100}

	/**
	 * @IN_WORD_JOKER Constants Regex interpretation
	 */
	static final String IN_WORD_UNLIMITED = "(\\\\S{0," + GREEDINESS_CHAR + "})";
	static final String IN_WORD_LIMITED = "(\\\\S)";
	
	// A list of list of word with their synonyms [["Hi", "Hey", "Hello"], ["You", "People"], ["~"], ["L^V"]]
	private List<List<String>> words;
	// The regex finally produced
	private StringBuilder regex = new StringBuilder();
	// The curent node, should always be the more "atomic" possible
	private StringBuilder node = new StringBuilder();
	// Where are we on the list of words with their synonyms
	private int steps = 0;
		
	/**
	 * Create a new instante of the RegexBuilder with it's own list of synonyms
	 * @param words - The list of synonyms, should not be empty
	 * @exception IllegalArgumentException when the list is empty
	 */
	public RegexBuilder(List<List<String>> words) {
		if (words.size() == 0) throw new IllegalArgumentException("Please give a valid words input");
		this.words = words;
		this.steps = words.size();
	}
	
	/**
	 * Iterates trough each synonyms list and build the regex for them
	 * @return the regex fully built and compiled
	 */
	public Pattern build() {
		this.regex.append(WORD_BOUNDARY_START);
		
		for (int i = 0; i < this.steps; i++) {
			this.buildNextNode(i);
			this.appendNode();
		}
		
		this.regex.append(WORD_BOUNDARY_END);
		
		return Pattern.compile(regex.toString(), Pattern.MULTILINE);
	}
	
	/**
	 * Atomic build of a single list of synonym, create a new node, and append its
	 * @param synonyms
	 */
	private void buildNextNode(int step) {
		this.node = new StringBuilder("(");
		
		List<String> synonyms = this.words.get(step);
		if (!this.listHasJoker(synonyms)) this.fillNodeWithSimpleSynonym(step);
		else if (synonyms.get(0).length() == 1) this.fillNodeWithIsolatedJoker(step);
		else this.fillNodeWithInwordJoker(step);
		
		this.node.append(")");
	}
	
	/**
	 * Add the current node to the full regex and return the content
	 * Does not reset the content of it, allowing to use the node more than one time
	 * @return
	 */
	private String appendNode() {
		this.regex.append(this.node);
		return this.node.toString();
	}
	
	/**
	 * Basic scenario, we only have a list of String withoutt joker
	 * Mainly, it's a OR builder with a few instructions to manage an empty string as a possibility
	 * @param step
	 */
	private void fillNodeWithSimpleSynonym(int step) {
		List<String> synonyms = this.words.get(step);
		Collections.sort(synonyms, (s1, s2) -> s1.length() - s2.length());
		// WORD_1|WORD_2|... 
		String regexOR = synonyms.stream()
								.filter(s -> !s.isEmpty() && !s.equals(" ")) // Remove any empty word
								.map(RegexBuilder::escapeRegexCharacter) // Escape every char that could break the regex
								.collect(Collectors.joining("|")); // Convert back to Lists
		
		
		// Sous-node 1 [All the synonyms]
		boolean hasEmptyWord = this.listHasEmptyWord(synonyms);
		boolean nextIsNotEmptyWordList = this.steps - 1 != step && !this.listHasEmptyWord(this.words.get(step + 1));
		if (hasEmptyWord) node.append("(" + WORD_BOUNDARY + "(" + regexOR + ")" + (nextIsNotEmptyWordList ? WORD_BOUNDARY : LIGHT_SPACE) + ")|\\s{0,100}");
		else node.append("(" + regexOR + ")");
		
		if (this.steps - 1 != step && !this.listHasEmptyWord(this.words.get(step + 1))) {
			// Sous-node 2 [WordBoundary / Space]
			if(!hasEmptyWord) node.append(WORD_BOUNDARY);
			else node.append(LIGHT_SPACE);
		}
	}
	
	/**
	 * Scenario when the word is an ISOLATED_JOKER, we just have to replace it with the right regex
	 * @param step
	 */
	private void fillNodeWithIsolatedJoker(int step) {
		String jokerUsed = this.words.get(step).get(0);
		if (jokerUsed.equals(LIMITED_WILDCARD)) node.append(ISOLATED_LIMITED);
		else node.append(ISOLATED_UNLIMITED);
	}
	
	/**
	 * Scenario when the word contains at least one joker inside itself, 
	 * we replace the joker with the corresponding regex traduction
	 * @param step
	 */
	private void fillNodeWithInwordJoker(int step) {
		// Sous-node-1 [Word with the wildcard traduced in regex]
		//
		String wordWithJoker = this.words.get(step).get(0);
		wordWithJoker = wordWithJoker.replaceAll("\\^", IN_WORD_LIMITED).replaceAll("\\~", IN_WORD_UNLIMITED);

		// Sous-node 2 [Word boundary]
		node.append(wordWithJoker);
		
		// Sous-node 3 [SPACE]
		if (this.steps - 1 != step) node.append(LIGHT_SPACE);
		
	}
	
	/**
	 * Espace any regex character that could be traduced wrongly during the process
	 * @param str - the string to escape
	 * @return - the str escaped
	 */
	private static String escapeRegexCharacter(String str) {
		String escaped = str.replaceAll("[\\[\\]\\{\\}\\(\\)\\-\\$\\|\\*\\+\\?\\/\\.\\\\]", "\\\\$0");

		return escaped;
	}
	
	/**
	 * Return true if the given List<String> contains a joker
	 * @param words
	 * @return
	 */
	public boolean listHasJoker(List<String> words) {
		return words.stream().anyMatch(s -> this.hasJoker(s));
	}
	
	
	/**
	 * Return true if the given String contains a joker
	 * @param word
	 * @return
	 */
	public boolean hasJoker(String word) {
		return word.contains(LIMITED_WILDCARD) || word.contains(UNLIMITED_WILDCARD);
	}
	
	
	/**
	 * Return true if the given List<String> contains at least one empty element
	 * @param synonyms
	 * @return
	 */
	public boolean listHasEmptyWord(List<String> synonyms) {
		return synonyms.contains(" ");
	}
	
	
}
