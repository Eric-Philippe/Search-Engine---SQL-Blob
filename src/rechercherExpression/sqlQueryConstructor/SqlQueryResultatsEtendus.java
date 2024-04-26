package apple.util.rechercherExpression.sqlQueryConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;
import apple.util.rechercherExpression.solutionRecherche.ResultatRecherche;


public class SqlQueryResultatsEtendus {	
	public static String getQueryEtenduApple(ContexteRecherche ctx, String[] reglesid) {
		return SqlQueryEtenduAPPLE.buildAppleStateInformations(ctx, reglesid).toString();
	}
	
	public static String getQueryEtenduPear(ContexteRecherche ctx, ArrayList<ResultatRecherche> reglesIdClients, ArrayList<ResultatRecherche> reglesIdModele) {
		return SqlQueryEtenduPEAR.buildPearStateInformations(ctx, reglesIdClients, reglesIdModele).toString();
	}
	
	public static String getQueryEtenduAppleCotis(ContexteRecherche ctx, String[] reglesid) {
		return SqlQueryEtenduAPPLE.buildAppleCotisInformations(ctx, reglesid).toString();
	}
	
	public static String getQueryEtenduPearCotis(ContexteRecherche ctx, String[] reglesid) {
		return SqlQueryEtenduPEAR.buildPearCotisInformations(ctx, reglesid).toString();
	}
}

class SqlQueryEtenduAPPLE {
	public static final String BC = ContexteRecherche.DEFAULT_PROJECT;
	/**
	 * Construction de la requête SQL permettant
	 * @param ctx
	 * @param reglesId
	 * @return
	 */
	public static StringBuilder buildAppleStateInformations(ContexteRecherche ctx, String[] reglesId) {
		TurboStringBuilder query = new TurboStringBuilder();

		return query.toStringBuilder();
	}
	
	public static StringBuilder buildAppleCotisInformations(ContexteRecherche ctx, String[] reglesId) {
		TurboStringBuilder query = new TurboStringBuilder();;
		
		return query.toStringBuilder();
	}

	private static String overloadModeleVersionProjet(ContexteRecherche ctx) {
		boolean hasModSeg = ctx.getModele() != null;
		String natProject = hasModSeg ? BC : ctx.getProject();

		ArrayList<String> trouples = new ArrayList<String>();
		if (hasModSeg) {
			trouples.add("('" + ctx.getModele() + "', '" + ctx.getVersion() + "', '" + ctx.getProject() + "')");
			if (!ctx.getProject().equals("BC")) {
				trouples.add("('" + ctx.getModele() + "', '" + ctx.getVersion() + "', '" + BC + "')");
			}
		}
		trouples.add("('" + ctx.getModeleNat() + "', '" + ctx.getVersionNat() + "', '" + natProject + "')");
		if (!natProject.equals("BC"))
			trouples.add("('" + ctx.getModeleNat() + "', '" + ctx.getVersionNat() + "', '" + BC + "')");
		
		String mvp = "WHEN (modeleid, versionid, projetid) IN (";
		TurboStringBuilder clause = new TurboStringBuilder();
		
		for(int i = 0; i < trouples.size() - 1; i++) {
			clause.append(mvp + trouples.get(i) + ") THEN " + Integer.toString(i + 1));
		}
		
		clause.append(" ELSE " + Integer.toString(trouples.size()));
		
		if (trouples.size() == 1) return mvp + "('" + ctx.getModeleNat() + "', '" + ctx.getVersionNat() + "', '" + natProject + "')) THEN 1 ELSE 1";

		return clause.toString();
	}

	private static String getAppleReqLibelle(ContexteRecherche ctx) {
		TurboStringBuilder clause = new TurboStringBuilder("");

			
		
		return clause.toString();
	
	}
	
	private static String getAppleReqOption(ContexteRecherche ctx, String optionName) {
		TurboStringBuilder clause = new TurboStringBuilder("");
		
		return clause.toString();
	
	}
	
	private static String getFetchFirstByOrder(ContexteRecherche ctx) {
		TurboStringBuilder clause = new TurboStringBuilder();

		ArrayList<ArrayList<String>> couples = SqlQueryEtenduTools.getModVerProjHierarchy(ctx);
		
		clause.append("ORDER BY");
		clause.append("CASE");
		
		for (int i = 0; i < couples.size(); i++) {
			List<String> couple = couples.get(i);
			clause.append("WHEN modeleid = '" + couple.get(0) + "' AND versionid = '" + couple.get(1) + "' AND projetid = '" + couple.get(2) + "' THEN " + (i + 1));
		}
		clause.append("END");
		clause.append("FETCH FIRST 1 ROW ONLY");
			
		
		return clause.toString();
	
	}
	
	private static String getAppleReqRubPhase(ContexteRecherche ctx) {
		TurboStringBuilder clause = new TurboStringBuilder("(");
		
		ArrayList<ArrayList<String>> couples = SqlQueryEtenduTools.getModVerProjHierarchy(ctx);
		for (int i = 0; i < couples.size(); i++) {
			couples.get(i).add("rub.rubid");
			couples.get(i).add("rub.phase");
		} 
		
		String inClauseValues = couples.stream()
				.map(innerList -> innerList.stream()
						.map(str -> !str.contains("rub.") ? "'" + str + "'" : str)
						.collect(Collectors.joining(", ", "(", ")")))
				.collect(Collectors.joining(", "));
		
		clause.append(inClauseValues);
		clause.add(")");
		
		return clause.toString();
	}
	
	private static String getAppleReqPriorityFetch(ContexteRecherche ctx) {		
		ArrayList<ArrayList<String>> couples = SqlQueryEtenduTools.getModVerProjHierarchy(ctx);
		
		TurboStringBuilder clause = new TurboStringBuilder("ORDER BY CASE");
		for (int i = 0; i < couples.size(); i++) {
			ArrayList<String> couple = couples.get(i);
			clause.append("WHEN (modeleid, versionid, projetid) IN (('" + couple.get(0) + "', '" + couple.get(1) + "', '" + couple.get(2) + "')) THEN " + (i + 1));
		}
		clause.append("END FETCH FIRST 1 ROW ONLY");
		
		return clause.toString();
	}
	}

class SqlQueryEtenduPEAR {
	public static final String BC = ContexteRecherche.DEFAULT_PROJECT;
	public static final String VERSION = ContexteRecherche.DEFAULT_VERSION;
	
	public static StringBuilder buildPearStateInformations(ContexteRecherche ctx, ArrayList<ResultatRecherche> reglesIdClients, ArrayList<ResultatRecherche> reglesIdModele) {
		TurboStringBuilder query = new TurboStringBuilder();
		
		return query.toStringBuilder();
	}

	public static StringBuilder buildPearCotisInformations(ContexteRecherche ctx, String[] trouples) {
		TurboStringBuilder query = new TurboStringBuilder();
		
		return query.toStringBuilder();
	}
}

class SqlQueryEtenduTools {
	/**
	 * Retourne un tableau des couples (Modèles, Version, Projet) trié par 
	 * ordre de priorité d'accès 
	 * 1. (ModSeg, Ver, Projet autre que BC)
	 * 2. (ModSeg, Ver, Projet BC)
	 * 3. (ModNat, Ver, Projet autre que BC) -- uniquement si aucun modèle segment n'est donné
	 * 4. (ModNat, Ver, BC)
	 * @param ctx
	 * @return
	 */
	public static ArrayList<ArrayList<String>> getModVerProjHierarchy(ContexteRecherche ctx) {
		ArrayList<ArrayList<String>> couples = new ArrayList<ArrayList<String>>();
		
		return couples;
	}
	
	public static String modeleVersionProjet(ContexteRecherche ctx) {
		boolean hasModSeg = ctx.getModele() != null;
		String natProject = hasModSeg ? "BC" : ctx.getProject();

		StringBuilder clause = new StringBuilder("(");

		// NAT
		clause.append("('" + ctx.getModeleNat() + "', '" + ctx.getVersionNat() + "' , '" + natProject + "')");
		if (!natProject.equals("BC") && !hasModSeg)
			clause.append(", ('" + ctx.getModeleNat() + "', '" + ctx.getVersionNat() + "' , 'BC')");

		// SEG
		if (hasModSeg) {
			clause.append(", ('" + ctx.getModele() + "', '" + ctx.getVersion() + "' , '" + ctx.getProject() + "')");
			if (!ctx.getProject().equals("BC"))
				clause.append(", ('" + ctx.getModele() + "', '" + ctx.getVersion() + "' , 'BC')");

		}

		clause.append(")");

		return clause.toString();
	}
}