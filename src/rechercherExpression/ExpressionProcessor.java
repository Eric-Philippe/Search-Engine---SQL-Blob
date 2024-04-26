package apple.util.rechercherExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import apple.util.dummyClasses.ConnectionData;
import apple.util.dummyClasses.AppleException;
import apple.util.dummyClasses.AppleMotRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;

/**
 * Class permettant la gestion première de l'expression recherchée.
 * 
 * <p>
 * ExpressionProcessor contient un ensemble de méthodes statiques permettant le
 * traitement, le nettoyage et la filtration de l'entrée utilisateur en
 * éliminant les morceaux parasites, et en traduisant en interne les mots clairs
 * en mots codés
 */
public class ExpressionProcessor {
	
	static final List<String> EXCEPTIONS_MOTS_ESTHETHIQUES = new ArrayList<>();
	
	static {
		EXCEPTIONS_MOTS_ESTHETHIQUES.add("I0060"); // ENFANT
	}
	/**
	 * Method that removes all the parasite character that may exist in the input
	 * one
	 */
	public static String removeParasite(String expression) {
		String str = expression;
		String unlimitedJoker = ContexteRecherche.getStringUnlimitedJoker();
		String duoUnlimitedJoker = unlimitedJoker + unlimitedJoker;
		str = str.replaceAll(duoUnlimitedJoker, unlimitedJoker);
		while (str.contains(duoUnlimitedJoker)) {
			str = str.replaceAll(duoUnlimitedJoker, unlimitedJoker);
		}

		return str;
	}

	/**
	 * Method that filters the given expression and return a AppleMotRechecheEd one,
	 * removing the word that won't be useful for the search
	 * 
	 * @param connectionData - Connexion Data
	 * @param searchBuilt    - Contexte recherche
     */
	public static <ConnectionData> ArrayList<AppleMotRecherche> buildListTerms(ConnectionData connectionData,
																			 ContexteRecherche searchBuilt, String model, String version, boolean removeWords) throws AppleException {
		ArrayList<AppleMotRecherche> sTermes = new ArrayList<AppleMotRecherche>();
		AppleRechercheMotZadig rechercheMotZadig = null;

			AppleRechercheExpression oRechExpr = new AppleRechercheExpression(connectionData, searchBuilt.getFirstClientId(),
					model, version, searchBuilt.getProject(), searchBuilt.getRawExpressionSearched());
			// Monter le Singleton en mémoire
			rechercheMotZadig = new AppleRechercheMotZadig(connectionData, searchBuilt.getFirstClientId(), model, version,
					searchBuilt.getProject(), null, null);

			ArrayList<AppleMotRecherche> lstTermeRecherche = oRechExpr.getListTermeWithI(rechercheMotZadig);

			// Remove all the last elements that are not considered as useful for the
			// research
			Iterator<AppleMotRecherche> itTermeRecherche = lstTermeRecherche.iterator();
			while (itTermeRecherche.hasNext()) {
				AppleMotRecherche motAppleRecherche = itTermeRecherche.next();
				// Si On veut pas remove des word (true) autorisera tout
				if (!removeWords)
					sTermes.add(motAppleRecherche);

			}
			return sTermes;
		}
	
	public static ArrayList<AppleMotRecherche> buildListTerms(ConnectionData cnx, ContexteRecherche ctx, HashSet<String> terms) throws AppleException {
		ContexteRecherche myCtx = new ContexteRecherche();
		ClientProjet client = ctx.getFirstClientProjet();
		myCtx.setProject(ctx.getProject());
		if (client != null) myCtx.addClientToInputClients(client.getClientId(), client.getProjetId());
		myCtx.setExpressionSearched(String.join(" ", terms));
		
		return buildListTerms(cnx, myCtx, ctx.getMainModel(), ctx.getMainVersion(), false);
	}
	
	/**
	 * Filter the terms List
	 * 
	 * @param lstTermes terme's list
	 * @return ArrayList<AppleMotRecherche>
	 */
	public static ArrayList<AppleMotRecherche> filterListTerms(ArrayList<AppleMotRecherche> lstTermes) {
		ArrayList<AppleMotRecherche> lstFilteredTermes = new ArrayList<>();
		for (AppleMotRecherche motApple : lstTermes) {
			if (!motApple.getMotCode().startsWith("I")) {
				lstFilteredTermes.add(motApple);
			}
		}

		return lstFilteredTermes;
	}

	/**
	 * Method that builds a decoded array containing array for one word A single
	 * array is all the synonyme of a word n
	 * 
	 * @param lstTermes - Coded Term's list
	 * @return - A List for each words with their synonyme
	 */
	public static List<List<String>> buildExpressionWithSynonyme(ArrayList<AppleMotRecherche> lstTermes) {
		List<List<String>> expressionWithSynonyme = new ArrayList<List<String>>();

        for (AppleMotRecherche mot : lstTermes) {
            List<String> lstMotClairEnrichi = mot.getLstMotClairEnrichi();
            String motCode = mot.getMotCode();
            if (motCode != null && motCode.length() == 5 && motCode.startsWith("I") && !EXCEPTIONS_MOTS_ESTHETHIQUES.contains(motCode) && lstTermes.size() > 1)
                lstMotClairEnrichi.add(" ");
            expressionWithSynonyme.add(mot.getLstMotClairEnrichi());
        }
		return expressionWithSynonyme;

	}
}
