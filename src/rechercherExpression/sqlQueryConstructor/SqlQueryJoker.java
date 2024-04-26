package apple.util.rechercherExpression.sqlQueryConstructor;

import java.util.ArrayList;

import apple.util.dummyClasses.AppleMotRecherche;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleJoker;
import apple.util.rechercherExpression.pear.RechercherExpressionPearJoker;

/**
 * Main class containing all the SQL Request Builder for the
 * {@link RechercherExpressionAppleJoker} and the
 * {@link RechercherExpressionPearJoker}
 * 
 * @author Eric PHILIPPE
 *
 */
public class SqlQueryJoker {
	public static final String BC = ContexteRecherche.DEFAULT_PROJECT;
	public static final String VERSION = ContexteRecherche.DEFAULT_VERSION;
	
	/**
	 * Returns a list of all the possible word for a list of terms containing at least one joker in APPLE only
	 * @param ctx
	 * @param lstTermes
	 * @return
	 */
	public static String getAllWordsFromJokerExpressionApple(ContexteRecherche ctx, ArrayList<AppleMotRecherche> lstTermes) {
		TurboStringBuilder query = new TurboStringBuilder("WITH");
		query.append(buildTermsTable(lstTermes) + ",");
		
		query.append(buildJokerTermsTable() + ",");
		
		query.append(buildAppleZadigEltTable(ctx) + ",");
		
		query.append(buildAppleCalculEltTable(ctx) + ",");
		
		query.append(buildAppleDicoResTable(ctx));
		
		return query.toString();
	}
	
	/**
	 * Returns a list of all the possible word for a list of terms containing at least one joker in APPLE/PEAR
	 * @param ctx
	 * @param lstTermes
	 * @return
	 */
	public static String getAllWordFromJokerExpressionApplePear(ContexteRecherche ctx, ArrayList<AppleMotRecherche> lstTermes) {
		TurboStringBuilder query = new TurboStringBuilder("WITH");
		query.append(buildTermsTable(lstTermes) + ",");
		
		query.append(buildJokerTermsTable() + ",");
		
		query.append(buildAppleZadigEltTable(ctx) + ",");
		
		query.append(buildPearZadigEltTable(ctx) + ",");
		
		query.append(buildAppleCalculEltTable(ctx) + ",");
		
		query.append(buildPearCalculEltTable(ctx) + ",");
		
		query.append(buildPearDicoResTable(ctx));
		
		return query.toString();
	}
	
	private static String buildTermsTable(ArrayList<AppleMotRecherche> lstTermes) {
		TurboStringBuilder table = new TurboStringBuilder("MOTS AS (");

		return table.toString();
	}
	
	private static String buildJokerTermsTable() {
		TurboStringBuilder table = new TurboStringBuilder("MOTS_JOKERS AS (");
		
		return table.toString();
	}
	
	private static String buildAppleZadigEltTable(ContexteRecherche ctx) {
		String mainProject = ctx.getSearchInPear() ? BC : ctx.getProject();
		String mainVersion = ctx.getSearchInPear() ? VERSION : ctx.getMainVersion();
		String mainProjectNat = ctx.getMainModel().equals(ctx.getModeleNat()) ? mainProject : BC;
		
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}
	
	private static String buildPearZadigEltTable(ContexteRecherche ctx) {
		String mainProject = ctx.getSearchInPear() ? BC : ctx.getProject();
		
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}

	private static String buildAppleCalculEltTable(ContexteRecherche ctx) {
		String mainProject = ctx.getSearchInPear() ? BC : ctx.getProject();
		String mainVersion = ctx.getSearchInPear() ? VERSION : ctx.getMainVersion();
		String mainProjectNat = ctx.getMainModel().equals(ctx.getModeleNat()) ? mainProject : BC;
		
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}
	
	private static String buildPearCalculEltTable(ContexteRecherche ctx) {
		String mainProject = ctx.getSearchInPear() ? BC : ctx.getProject();
		
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}
	
	private static String buildAppleDicoResTable(ContexteRecherche ctx) {
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}
	
	private static String buildPearDicoResTable(ContexteRecherche ctx) {
		TurboStringBuilder table = new TurboStringBuilder("");
		
		return table.toString();
	}
}
