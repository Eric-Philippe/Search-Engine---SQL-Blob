package apple.util.rechercherExpression.sqlQueryConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import apple.core.ConnectionData;
import apple.core.DbAccessException;
import apple.core.Logger;
import apple.util.AppleMotRecherche;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleStrict;
import apple.util.rechercherExpression.pear.RechercherExpressionPearStrict;

/**
 * Main class containing all the SQL Request Builder for the
 * {@link RechercherExpressionAppleStrict} and the
 * {@link RechercherExpressionPearStrict}
 * 
 * @author Eric PHILIPPE
 *
 */
public class SqlQueryStrict {
	public static void getAllActualClients(ConnectionData cnx, ContexteRecherche ctx, List<AppleMotRecherche> expressions, List<AppleMotRecherche> expressionsEtendues, List<List<String>> lstSynonymes, String tableTitle) {
		StringBuilder req = new StringBuilder("SELECT clientid, projetid FROM " + tableTitle + " WHERE regleid IS NOT NULL AND paramreg IS NULL AND (clientid, projetid) IN (");
		String clientsIdInClause = ctx.getClientsInput().stream()
					.map(clientProjet -> "('" + clientProjet.getClientId() + "', '" + clientProjet.getProjetId() + "')")
					.collect(Collectors.joining(", "));
		req.append(clientsIdInClause + ") ");
		req.append("AND (");
		req.append("((1 = 1) " + getSqlClauseTermsReqCodedExp(expressions, expressionsEtendues) + ") OR");
		req.append("((1 = 1) " + getSqlClauseTermInString(lstSynonymes) + ")");
		req.append(")");
		req.append("GROUP BY clientid, projetid");
		
		boolean dbAccess = false;
		try {
			dbAccess = cnx.openDbAccess();
			ResultSet result = cnx.getDbAccess().executeSelect(req.toString());
			
			String clientId, projetId;
			while (result.next()) {
				clientId = result.getString(1);
				projetId = result.getString(2);
				
				ctx.addClientToClientsSolution(clientId, projetId);
			}			
		} catch (DbAccessException | SQLException e) {
			Logger.error(e);
		} finally {
			if (dbAccess) cnx.closeDbAccess();
		}
	}
	/**
	 * Build the SQL Query in order to find all the regles from the coded text
	 * containing the given expression. This version allows the input to contain
	 * Joker in wich case we search for the word without joker, and with.
	 * 
	 * @param ctx         - Contexte Recherche
	 * @param expressions - Coded Expression
	 * @param tableTitle  - Active SQL Table
	 * @return the SQL Query from the select to the end
	 */
	public static StringBuilder getSqlQueryReglesAnalyseesFromCodedExp(ContexteRecherche ctx,
			List<AppleMotRecherche> expressions, List<AppleMotRecherche> expressionsEtendues, String tableTitle, ClientProjet client) {
		StringBuilder sqlRequest = SqlQueryUtils.buildSqlHeader(tableTitle, 1);

		sqlRequest.append(SqlQueryUtils.sqlContextParamater(ctx, client));

		sqlRequest.append(getSqlClauseTermsReqCodedExp(expressions, expressionsEtendues));

		return sqlRequest;
	}
	
	public static StringBuilder getSqlClauseTermsReqCodedExp(List<AppleMotRecherche> expressions, List<AppleMotRecherche> expressionsEtendues) {
		StringBuilder clause = new StringBuilder();
		String K = "K";

		for (AppleMotRecherche mot : expressions) {
			String word = mot.getMotCode();
			if (ContexteRecherche.isWordOnlyJokers(word)) continue;
			String[] args;
			if (SqlQueryUtils.wordHasJoker(word)) {
				List<String> expressionsEtendusEnClair = new ArrayList<>();
				for (int i = 0; i < expressionsEtendues.size(); i++) {
					expressionsEtendusEnClair.add(expressionsEtendues.get(i).getMotCode());
				}
				args = expressionsEtendusEnClair.toArray(new String[0]);
			} else {
				if (mot.isConstante())
					args = new String[] { word, K + word };
				else
					args = new String[] { word };
			}

			clause.append(SqlQueryUtils.DBMS_INSTR(args));
		}
		
		return clause;
	}
	
	/**
	 * Alias for APPLE
	 * 
	 * @see SqlQueryJoker#getSqlQueryReglesAnalyseesFromCodedExp(ContexteRecherche,
	 *      List, String, boolean)
	 */
	public static StringBuilder getSqlQueryReglesAnalyseesFromCodedExp(ContexteRecherche ctx,
			List<AppleMotRecherche> expressions, List<AppleMotRecherche> expressionsEtendues, String tableTitle) {
		return getSqlQueryReglesAnalyseesFromCodedExp(ctx, expressions, expressionsEtendues, tableTitle, null);
	}
	
	/**
	 * Build the SQL Query from an full synonymes expression
	 * 
	 * @param ctx          - Contexte Recherche
	 * @param lstSynonymes - Synonyme's list
	 * @param tableTitle   - Table Title
	 * @return a ready sql query to search text from the analyzed text in comments
	 */
	public static StringBuilder getSqlQueryReglesAnalyseesCommentaireFromSynonymes(ContexteRecherche ctx,
			List<List<String>> lstSynonymes, String tableTitle, ClientProjet client) {
		StringBuilder sqlRequest = SqlQueryUtils.buildSqlHeader(tableTitle, 1);

		sqlRequest.append(SqlQueryUtils.sqlContextParamater(ctx, client));

		sqlRequest.append(getSqlClauseTermInString(lstSynonymes));

		return sqlRequest;
	}

	/**
	 * Alias for APPLE
	 * 
	 * @see SqlQueryStrict#getSqlQueryReglesAnalyseesCommentaireFromSynonyes(ContexteRecherche,
	 *      List, String, boolean)
	 */
	public static StringBuilder getSqlQueryReglesAnalyseesCommentaireFromSynonymes(ContexteRecherche ctx,
			List<List<String>> lstSynonymes, String tableTitle) {
		return getSqlQueryReglesAnalyseesCommentaireFromSynonymes(ctx, lstSynonymes, tableTitle, null);
	}

	/**
	 * Build the SQL Query from an full synonymes expression
	 * 
	 * @param ctx          - Contexte Recherche
	 * @param lstSynonymes - Synonyme's list
	 * @param tableTitle   - Table Title
	 * @return a ready sql query to search text from the non analyzed text
	 */
	public static StringBuilder getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(ContexteRecherche ctx,
			List<List<String>> lstSynonymes, String tableTitle, ClientProjet client) {
		StringBuilder sqlRequest = SqlQueryUtils.buildSqlHeader(tableTitle, 0);

		sqlRequest.append(SqlQueryUtils.sqlContextParamater(ctx, client));

		sqlRequest.append(getSqlClauseTermInString(lstSynonymes));

		return sqlRequest;
	}
	
	public static StringBuilder getSqlClauseTermInString(List<List<String>> lstSynonymes) {
		StringBuilder clause = new StringBuilder();
		
		for (List<String> synonymes : lstSynonymes) {
			String[] values = SqlQueryUtils.StringListToStringTab(synonymes);
			clause.append(SqlQueryUtils.DBMS_INSTR(values));
		}
		
		return clause;
	}

	/**
	 * Alias for APPLE
	 * 
	 * @see SqlQueryStrict#getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(ContexteRecherche,
	 *      List, String, boolean)
	 */
	public static StringBuilder getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(ContexteRecherche ctx,
			List<List<String>> lstSynonymes, String tableTitle) {
		return getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(ctx, lstSynonymes, tableTitle, null);
	}

	/**
	 * Return the query in order to get the utilisation information
	 * 
	 * @param ctx      - Contexte Recherche
	 * @param reglesId - Regle Id
	 * @param mot      - Mot we're searching in
	 * @return - query
	 */
	public static StringBuilder getSqlQueryUtilisationMotAPPLE(ContexteRecherche ctx, String[] reglesId, String mot) {
		StringBuilder sqlRequest = new StringBuilder("SELECT regleid, utilisation_mot FROM APPLE_REGLE_MOT WHERE");
		sqlRequest.append(SqlQueryUtils.IN_CLAUSE_MODVERPROJ(ctx));
		sqlRequest.append("AND (" + SqlQueryUtils.IN_OR(reglesId, "regleid") + " )");
		sqlRequest.append(" AND mot = '" + mot + "'");

		return sqlRequest;
	}
	
	public static StringBuilder getSqlQueryUtilisationMotPEAR(ContexteRecherche ctx, String[] reglesId, String mot) {
		StringBuilder sqlRequest = new StringBuilder("SELECT clientid, projetid, regleid, utilisation_mot FROM PEAR_REGLE_MOT WHERE");
		sqlRequest.append("(clientid, projetid, regleid) IN (");
		for (int i = 0; i < reglesId.length - 1; i++) {
			sqlRequest.append(reglesId[i] + ", ");
		}
		
		sqlRequest.append(reglesId[reglesId.length - 1] + ")");
		sqlRequest.append(" AND mot = '" + mot + "'");

		return sqlRequest;
	}
}
