package apple.util.rechercherExpression.apple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import apple.core.ConnectionData;
import apple.core.AppleException;
import apple.core.AppleQueryAND;
import apple.dataaccess.APPLE_REGLE;
import apple.dataaccess.APPLE_REGLEData;
import apple.scenario.ScenarioConnexion;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ExpressionProcessor;
import apple.util.rechercherExpression.Recherche;
import apple.util.rechercherExpression.RechercheUtils;
import apple.util.rechercherExpression.RegleResultat;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryStrict;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryUtils;

/**
 * Recherche une expression dans APPLE de manière strict
 * 
 * <p>
 * La classe implémente par nature la classe {@link Recherche}
 * 
 * <p>
 * Implémente les méthodes permettant la récupérations des règles APPLE contenant
 * l'expression travaillée dans {@link ExpressionProcessor}
 * 
 * <p>
 * On référence chaque règles solutions avec une clé qui leurs sont uniques qui
 * permettront d'être ensuite atteinte et stockée de manière uniforme. Les clés
 * APPLE sont montées de la sorte :
 * 
 * <pre>
 * REGLE_ID<\pre>
 * 
 * <p>
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercherExpressionAppleStrict extends Recherche {
	private static final long serialVersionUID = -7699493836980064374L;

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche)
	 */
	public RechercherExpressionAppleStrict(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		super(cnx, ctx);
	}

	/**
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion)
	 */
	public RechercherExpressionAppleStrict(ConnectionData cnx, ScenarioConnexion snr) throws AppleException {
		super(cnx, snr);
	}

	@Override
	public String getNomTableActive() {
		return SqlQueryUtils.APPLE_REGLE_TABLE;
	}

	@Override
	public ResultatsRecherche start() throws InterruptedException, AppleException {
		getRegleFromSqlQuery();
		return getSolutions();
	}

	/**
	 * Première étape de filtrage - On Recherche les règles, à l'aide de SQL,
	 * contenant le(s) terme(s) recherché(s)
	 * 
	 * @throws AppleException - Exception sur le PrjModUnion
	 */
	public void getRegleFromSqlQuery() throws AppleException {
		StringBuilder query = SqlQueryStrict.getSqlQueryReglesAnalyseesFromCodedExp(_ctx, _lstTermesCodes,
				_lstTermesCodesExtended, getNomTableActive());

		HashSet<String> lstRegleFromTexteAnalyse = executeQuery(query);
		HashSet<String> lstRegleFromTexteAnalyseCommentaire = null;
		HashSet<String> lstRegleFromTexteNonAnalyse = null;
		HashSet<String> lstAllRegleId = new HashSet<String>(lstRegleFromTexteAnalyse);

		if (_inComment) {
			query = SqlQueryStrict.getSqlQueryReglesAnalyseesCommentaireFromSynonymes(_ctx, _lstTermesSynonymes,
					getNomTableActive());
			lstRegleFromTexteAnalyseCommentaire = executeQuery(query);
			lstAllRegleId.addAll(lstRegleFromTexteAnalyseCommentaire);
		}

		if (_inNonAnalyzedRegles) {
			query = SqlQueryStrict.getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(_ctx, _lstTermesSynonymes,
					getNomTableActive());
			lstRegleFromTexteNonAnalyse = executeQuery(query);
			lstAllRegleId.addAll(lstRegleFromTexteNonAnalyse);
		}

		AppleQueryAND queryAnd = new AppleQueryAND(APPLE_REGLE.REGLEID_INLIST(new ArrayList<String>(lstAllRegleId)),
				APPLE_REGLE.PRODUITID_EQ(_ctx.getProduit()));

		List<APPLE_REGLEData> lstData = APPLE_REGLE.listPrjUnionModNatData(_cnx, _ctx.getMainModel(), _ctx.getMainVersion(),
				_ctx.getProject(), queryAnd, null, "DATE_EFFET DESC");
		if (lstData == null) return;
		
		for (APPLE_REGLEData data : lstData) {
			String regleId = data.getREGLEID();

			String clientId = null;
			if (this._ctx.getSearchInApple() && this._ctx.getSearchInPear()) {
				clientId = this._ctx.getFirstClientProjet().getClientId();
			}
			RegleResultat regleResultat = new RegleResultat(data.getREGLEID(), data.getMODELEID(), data.getVERSIONID(),
					data.getPROJETID(), clientId, true);

			if (RechercheUtils.hashContains(lstRegleFromTexteAnalyse, regleId)) {
				_solutionsRaw.put(regleResultat, data.getTEXTE());
			}

			if (_inComment && RechercheUtils.hashContains(lstRegleFromTexteAnalyseCommentaire, regleId)) {
				_solutionsRaw.put(regleResultat, data.getTEXTE());
			}

			if (_inNonAnalyzedRegles && RechercheUtils.hashContains(lstRegleFromTexteAnalyseCommentaire, regleId)) {
				_solutionsNonAnalyzed.put(regleResultat, SqlQueryUtils.StringFromBlob(data.getTEXTE()));
			}
		}
	}

}
