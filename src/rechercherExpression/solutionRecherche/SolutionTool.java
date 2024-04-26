package apple.util.rechercherExpression.solutionRecherche;


import apple.core.ConnectionData;
import apple.util.rechercherExpression.RegleResultat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * ToolBox for the {@link SolutionsContainer} class.
 * 
 * <p>
 * Generic class to define the method that will be implemented in the specific
 * SolutionTool. {@link SolutionToolAPPLE}, {@link SolutionToolPEAR}
 * 
 * <p>
 * Unify the ID allowing to call generically the methods even if APPLE use only
 * one ID et PEAR three
 * 
 * <p>
 * 
 * @author Eric PHILIPPE
 */
public abstract class SolutionTool implements Serializable {
	private static final long serialVersionUID = 2135573714005362111L;

	public SolutionTool() {
	}

	/**
	 * Convert String[] ids to a single Key joined with '#'
	 * 
	 * @param strings ids
	 * @return id#id#..
	 */
	public static String toId(String... strings) {
		StringBuilder stb = new StringBuilder(strings[0]);
		for (int i = 1; i < strings.length; i++) {
			if (strings[i] != null)
				stb.append("#" + strings[i]);
		}

		return stb.toString();
	}

	/**
	 * Blow the key joined with # i
	 * 
	 * @param str key
	 * @return String[]
	 */
	public static String[] blowId(String str) {
		return str.split("#");
	}

	/**
	 * Utils method that returns true if a string is contained inside a String
	 * tupple
	 * 
	 * @param solutions String Tupple
	 * @param solution  String
	 * @return true if it's contained
	 */
	public static boolean isSolution(String[] solutions, String solution) {
		if (solution.contains("#")) {
			String solutionClientId = solution.split("#")[0];
			String solutionRegleId = solution.split("#")[2];

			for (String str : solutions) {
				if (str.equals(solution))
					return true;
				
				String[] currentSolSplit = str.split("#");
				if (currentSolSplit.length > 1) {
					String clientId = currentSolSplit[0];
					String regleId = currentSolSplit[2];
					
					if (clientId.equals(solutionClientId) && regleId.equals(solutionRegleId)) return true;
				}
				
			}
		} else {
			for (String str : solutions) {
				if (str.equals(solution))
					return true;
			}
		}
		
		return false;
	}

	public static boolean isSolution(HashSet<String> solutions, String solution) {
		if (solution.contains("#")) {
			String solutionClientId = solution.split("#")[0];
			String solutionRegleId = solution.split("#")[2];

			for (String str : solutions) {
				if (str.equals(solution))
					return true;
				
				String[] currentSolSplit = str.split("#");
				if (currentSolSplit.length > 1) {
					String clientId = currentSolSplit[0];
					String regleId = currentSolSplit[2];
					
					if (clientId.equals(solutionClientId) && regleId.equals(solutionRegleId)) return true;
				}
				
			}
		} else {
			for (String str : solutions) {
				if (str.equals(solution))
					return true;
			}
		}

		return false;
	}

	/**
	 * Extend the {@link ResultatsRecherche} to a {@link RubriqueResultat}
	 * growing the scale from the Regle perspective to the Rubrique one
	 * 
	 * <p>
	 * Call the {@link SqlQueryResultatsEtendus} in order to make all the hard job
	 * for us
	 * 
	 * @param solutions Container of the ResultatsRecherche in order to give every
	 *                  pieces we need (Connections, Contexte, Caches...)
	 */
	public abstract void extend(ConnectionData cnx,
								HashMap<RegleResultat, byte[]> reglesAPPLEToDecode, HashMap<RegleResultat, byte[]> reglesPEARToDecode,
								HashSet<RegleResultat> reglesParamCotisAPPLEToDecode, HashSet<RegleResultat> reglesParamCotisPEARToDecode,
								SolutionsContainer solutions);

	/**
	 * Decode all the new Regle that we need to decode
	 * 
	 * <p>
	 * Use the multi-threading from the {@link DecoderMultiple}
	 * 
	 * @param solutions Container of the ResultatsRecherche in order to give every
	 *                  pieces we need (Connections, Contexte, Caches...)
	 */
	public abstract void decode(ConnectionData cnx,
								HashMap<RegleResultat, byte[]> reglesAPPLEToDecode, HashMap<RegleResultat, byte[]> reglesPEARToDecode,
								HashSet<RegleResultat> reglesParamCotisAPPLEToDecode, HashSet<RegleResultat> reglesParamCotisPEARToDecode,
								SolutionsContainer solutions);
	
	/**
	 * Decode all the new ParamCotis Regle that we need to decode
	 * 
	 * <p>
	 * Use the multi-threading decoder from the {@link DecoderMultipleCotise}
	 * 
	 * @param solutions Container of the ResultatsRecherche in order to give every
	 *                  pieces we need (Connections, Contexte, Caches...)
	 */
//	public abstract void decodeParamCotis(ConnectionData cnx, HashSet<RegleResultat> toDecode, SolutionsContainer solutions);
}