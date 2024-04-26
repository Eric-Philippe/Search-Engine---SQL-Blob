package apple.util.rechercherExpression.sqlQueryConstructor;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import apple.util.dummyClasses.AppleMotRecherche;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;

/**
 * SQL Query Tools for the Recherche Expression.
 * 
 * <p>
 * Provided commons SQL Query tools in order to avoid repetions around all the
 * Query Builder
 * 
 * @author Eric PHILIPPE
 */
public class SqlQueryUtils {
	final public static String APPLE_REGLE_TABLE = "APPLE_REGLE";
	final public static String PEAR_REGLE_TABLE = "PEAR_REGLE";

	/**
	 * Return the SELECT line
	 * 
	 * @param tableName - The name of the table we're searching in
	 * @param texteCode - If we search in textecode or not 1 or 0
	 * @return a StringBuilder with the header
	 */
	public static StringBuilder buildSqlHeader(String tableName, int texteCode) {
		StringBuilder header = new StringBuilder(
				"SELECT REGLEID FROM " + tableName + " WHERE regleid IS NOT NULL AND PARAMREG IS NULL ");
		if (texteCode == -1)
			return header;
		return header.append(" AND TEXTECODE = '" + texteCode + "' ");
	}

	/**
	 * Build all the conditions thanks to the Contexte Recherche
	 * 
	 * @param searchBuilt - Contexte Recherche
	 * @return a String builder with all the AND
	 */
	public static StringBuilder sqlContextParamater(ContexteRecherche searchBuilt, ClientProjet client) {
		StringBuilder contextQuery = new StringBuilder();

		String produit = searchBuilt.getProduit();

		if (produit != null) {
			contextQuery.append("AND produitid = '").append(produit).append("' ");
		}

		if (client == null || client.getClientId() == null) {
			contextQuery.append("AND" + IN_CLAUSE_MODVERPROJ(searchBuilt));
		} else {
			contextQuery.append(" AND clientid = '" + client.getClientId() + "' AND projetid = '" + client.getProjetId() + "'");
		}

		return contextQuery;
	}

	/**
	 * Build all the conditions thanks to the Contexte Recherche
	 * 
	 * @param searchBuilt - Contexte Recherche
	 * @return a String builder with all the AND
	 */
	public static StringBuilder sqlContextParamater(ContexteRecherche searchBuilt) {
		return sqlContextParamater(searchBuilt, null);
	}

	/**
	 * Alias for DBMS_LOB.INSTR(TEXTE, UTL_RAW.CAST_TO_RAW(""), 1, 1) > 0
	 * <p>
	 * If more than one element, we use the alias with a OR
	 * 
	 * @param args - Elements we're searching in
	 * @return a StringBuilder
	 */
	public static StringBuilder DBMS_INSTR(String... args) {
		if (args.length > 1)
			return DBMS_INSTR_MULTIPLE(args);
		else
			return new StringBuilder(
					" AND DBMS_LOB.INSTR(TEXTE, UTL_RAW.CAST_TO_RAW('" + quoteSqlReady(args[0]) + "'), 1, 1) > 0 ");
	}

	/**
	 * Alias for (DBMS_LOB.INSTR(TEXTE, UTL_RAW.CAST_TO_RAW(""), 1, 1) > 0 OR ...)
	 * 
	 * @param args - Elements we're searching in
	 * @return a StringBuilder
	 */
	private static StringBuilder DBMS_INSTR_MULTIPLE(String... args) {
		StringBuilder dbmsMultiple = new StringBuilder(
				" AND (DBMS_LOB.INSTR(TEXTE, UTL_RAW.CAST_TO_RAW('" + quoteSqlReady(args[0]) + "'), 1, 1) > 0");
		for (int i = 1; i < args.length; i++) {
			dbmsMultiple.append(
					" OR DBMS_LOB.INSTR(TEXTE, UTL_RAW.CAST_TO_RAW('" + quoteSqlReady(args[i]) + "'), 1, 1) > 0");
		}

		dbmsMultiple.append(") ");

		return dbmsMultiple;
	}

	public static StringBuilder IN_OR(String[] elements, String colomnName) {
		if (elements.length == 0) return new StringBuilder("1 = 1");
		int batchSize = 1000; // Taille maximale d'un IN

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < elements.length; i++) {
			if (i % batchSize == 0) { // Si on atteint la limite du IN, on commence une nouvelle clause
				if (i > 0) {
					sb.setLength(sb.length() - 2); // Supprime le dernier ", " ajouté
					sb.append(") OR " + colomnName + " IN (");
				} else {
					sb.append(colomnName + " IN (");
				}
			}
			sb.append("'").append(elements[i]).append("', "); // Ajoute l'élément courant entre quotes
		}

		if (sb.length() > 2)
			sb.setLength(sb.length() - 2); // Supprime le dernier ", " ajouté
		sb.append(")"); // Ferme la dernière clause

		return sb;

	}

	public static StringBuilder IN_OR_LIGHT(String[] elements, String colomnName) {
		if (elements.length == 0) return new StringBuilder("(1 = 0)");
		int batchSize = 1000; // Taille maximale d'un IN

		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < elements.length; i++) {
			if (i % batchSize == 0) { // Si on atteint la limite du IN, on commence une nouvelle clause
				if (i > 0) {
					sb.setLength(sb.length() - 2); // Supprime le dernier ", " ajouté
					sb.append(") OR " + colomnName + " IN (");
				} else {
					sb.append(colomnName + " IN (");
				}
			}
			sb.append(elements[i]).append(", "); // Ajoute l'élément courant entre quotes
		}
		
		sb.setLength(sb.length() - 2); // Supprime le dernier ", " ajouté
		sb.append("))"); // Ferme la dernière clause

		return sb;
	}

	/**
	 * Converte from ResultSet to HashSet
	 * 
	 * @param result - The SQL Result from a query
	 * @return - HashSet
	 */
	public static HashSet<String> listRegleToHashSet(ResultSet result) {
		HashSet<String> tempRegleId = new HashSet<String>();
		try {
			while (result.next()) {
				tempRegleId.add(result.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tempRegleId;
	}
	
	/**
	 * Converter from an expression with human joker to sql joker
	 * 
	 * @param expression - Base expression
	 * @return - Human joker replaced with %
	 */
	public static String expressionToSqlJoker(String expression) {
		String str = expression.replace(ContexteRecherche.getStringLimitedJoker(), "_")
				.replace(ContexteRecherche.getStringUnlimitedJoker(), "%");
		
		str.replaceAll("%+", "%");
		
		return str;
	}

	/**
	 * Converter from List{String} to String[]
	 * 
	 * @param stringLst - List String
	 * @return String[]
	 */
	public static String[] StringListToStringTab(List<String> stringLst) {
		String stringTab = String.join(" ", stringLst);
		return stringTab.split(" ");
	}

	/**
	 * Converter from List{String} to String[] but authorize to add one more element
	 * from the given list
	 * 
	 * @param stringLst - List String
	 * @param extension - a word to add in the list
	 * @return String[]
	 */
	public static String[] StringListToStringTab(List<String> stringLst, String extension) {
		String stringTab = String.join(" ", stringLst);
		stringTab = stringTab + " " + extension;
		return stringTab.split(" ");
	}

	/**
	 * Converter from List{AppleMotRecherche} to String[]
	 * 
	 * @param AppleLst - List{AppleMotRecherche}
	 * @return String[]
	 */
	public static String[] AppleMotRechercheToStringTab(List<AppleMotRecherche> AppleLst) {
		List<String> stringLst = new ArrayList<String>();
		for (AppleMotRecherche word : AppleLst) {
			stringLst.add(word.getMotCode());
		}
		String stringTab = String.join(" ", stringLst);
		return stringTab.split(" ");
	}

	/**
	 * Self setter for the table name.
	 * 
	 * @param tableName
	 * @return
	 */
	public static String defineTableName(String tableName) {
		if (!Arrays.asList(APPLE_REGLE_TABLE, PEAR_REGLE_TABLE).contains(tableName))
			throw new IllegalArgumentException("Merci d'entrer un nom de table valide !");
		return tableName;
	}

	public static StringBuilder IN_CLAUSE_MODVERPROJ(ContexteRecherche ctx) {
		StringBuilder strBuilder = new StringBuilder();
		String project = ctx.getProject();
		String modelNat = ctx.getModeleNat();
		String verNat = ctx.getVersionNat();
		String model = ctx.getModele();
		String ver = ctx.getVersion();
		strBuilder.append(" (modeleid, versionid, projetid) IN (('").append(modelNat).append("', '").append(verNat)
				.append("', '").append(ContexteRecherche.DEFAULT_PROJECT).append("')");
		if (!project.equals(ContexteRecherche.DEFAULT_PROJECT) && model == null) {
			strBuilder.append(", ('").append(modelNat).append("', '").append(verNat).append("', '").append(project)
					.append("')");
		}

		if (model != null && ver != null) {
			strBuilder.append(", ('").append(model).append("', '").append(ver).append("', '").append(ContexteRecherche.DEFAULT_PROJECT).append("')");
			if (!project.equals(ContexteRecherche.DEFAULT_PROJECT)) {
				strBuilder.append(", ('").append(model).append("', '").append(ver).append("', '").append(project)
						.append("')");
			}
		}

		strBuilder.append(") ");

		return strBuilder;
	}

	/**
	 * Optimized method to convert a SQL Blob (Java byte[]) to String
	 * 
	 * @param bytes - Coded Text
	 * @return Text coded in String
	 */
	public static String StringFromBlob(byte[] bytes) {
		final int THRESHOLD = 1024 * 1024; // set the threshold to 1 MB
		String str = "";
		if (bytes.length < THRESHOLD) {
			str = new String(bytes, StandardCharsets.UTF_8); // use the String constructor for small arrays
		} else {
			CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder(); // create a CharsetDecoder
			ByteBuffer buffer = ByteBuffer.wrap(bytes); // wrap the byte[] in a ByteBuffer
			try {
				str = decoder.decode(buffer).toString();
			} catch (CharacterCodingException e) {
			}
		}

		return str;
	}

	/**
	 * If a word contains at least one joker of any type
	 * 
	 * @param str text input
	 * @return true if the input contains a joker
	 */
	public static boolean wordHasJoker(String str) {
		return str.contains(ContexteRecherche.getStringLimitedJoker())
				|| str.contains(ContexteRecherche.getStringUnlimitedJoker());
	}

	/**
	 * Title
	 * 
	 * @param expression - input
	 * 
	 * @return the input where we replaced all the joker with nothing
	 */
	public static String removeJokerFromWord(String expression) {
		return expression.replace(ContexteRecherche.getStringLimitedJoker(), "")
				.replace(ContexteRecherche.getStringUnlimitedJoker(), "");
	}

	/**
	 * SQL Safe fence for constante words containing quote
	 * 
	 * @param constante input text
	 * @return the constant with the quote at the end
	 */
	public static String doubleCheckQuoteCnst(String constante) {
		if (!constante.endsWith("'"))
			return constante + "'";
		else
			return constante;
	}

	/**
	 * SQL Safe fence for words containing quote
	 * 
	 * @return the text with the quote stringified
	 */
	public static String quoteSqlReady(String word) {
		if (word.contains("'")) {
			return word.replaceAll("'", "''");
		}
		return word;
	}
}
