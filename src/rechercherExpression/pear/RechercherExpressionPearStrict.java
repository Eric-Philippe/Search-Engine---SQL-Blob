package apple.util.rechercherExpression.pear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import apple.util.decoder.ConnectionPool;
import apple.util.decoder.PooledCnx;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;
import apple.util.rechercherExpression.ExpressionProcessor;
import apple.util.rechercherExpression.Recherche;
import apple.util.rechercherExpression.RechercheUtils;
import apple.util.rechercherExpression.RegleResultat;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleStrict;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryStrict;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryUtils;
import pear.dataaccess.PEAR_REGLE;
import pear.dataaccess.PEAR_REGLEData;

/**
 * Recherche une expression dans PEAR de manière strict
 * 
 * <p>
 * La classe implémente par nature la classe {@link Recherche}
 * 
 * <p>
 * Implémente les méthodes permettant la récupérations des règles PEAR contenant
 * l'expression travaillée dans {@link ExpressionProcessor}
 * 
 * <p>
 * On référence chaque règles solutions avec une clé qui leurs sont uniques qui
 * permettront d'être ensuite atteinte et stockée de manière uniforme. Les clés
 * PEAR sont montées de la sorte :
 * 
 * <pre>
 * CLIENT_ID#PROJET_ID#REGLE_ID <\pre>
 * 
 * <p>
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercherExpressionPearStrict extends RechercherExpressionAppleStrict {
	private static final long serialVersionUID = -4546438536356133797L;

	private static final int NUM_THREADS = Math.min( 
			Runtime.getRuntime().availableProcessors(),
			AppleProperties.getInt("rechercherExpression.NUM_THREADS", Runtime.getRuntime().availableProcessors()));

	/** Mutli-Thread-proof HashMap */
	private ConcurrentHashMap<RegleResultat, byte[]> solutionsRawBuffer = new ConcurrentHashMap<RegleResultat, byte[]>();

	/** We allow to concatenate two SolutionRecherche */
	private ResultatsRecherche pastSolutionRecherche = null;

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche) Ce constructeur prend en plus en paramètre une
	 *      SolutionRecherche (Devrait provenir d'une première recherche sous APPLE)
	 *      pour pouvoir compiler les deux par la suite.
	 */
	public RechercherExpressionPearStrict(ConnectionData cnx, ContexteRecherche ctx, ResultatsRecherche solutions,
			ConcurrentHashMap<String, AppleMot> cacheMot, ConcurrentHashMap<String, String> cacheClair)
			throws AppleException {
		super(cnx, ctx);
		pastSolutionRecherche = solutions;
		this._solutionsParamCotis = solutions.getParamCotisRawSolutions();
		this.setCacheAppletMot(cacheMot);
		this.setCacheTextEnClair(cacheClair);
	}

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche)
	 */
	public RechercherExpressionPearStrict(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		super(cnx, ctx);
	}

	/**
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion)
	 */
	public RechercherExpressionPearStrict(ConnectionData cnx, ScenarioConnexion snr) throws AppleException {
		super(cnx, snr);
	}

	@Override
	public String getNomTableActive() {
		return SqlQueryUtils.PEAR_REGLE_TABLE;
	}

	public HashSet<String> executeQueryExtended(String query) throws DbAccessException {
		int clientsSize = this._ctx.getClientsWithSolutionSize();
		if (clientsSize == 1)
			return super.executeQuery(query);

		// Récupération d'une connection dans la pool disponible
		try (PooledCnx pcnx=ConnectionPool.takeConnection()){
			return this.executeQuery(query, pcnx.getCnxData());
		} // autoclose du PooledCnx par le pattern try-resource
	}

	@Override
	public void getRegleFromSqlQuery() throws AppleException {
		SqlQueryStrict.getAllActualClients(_cnx, _ctx, this._lstTermesCodes, _lstTermesCodesExtended, _lstTermesSynonymes, getNomTableActive());
		
		if (_ctx.getClientsWithSolutionSize() == 0) return;
		
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
		

		for (ClientProjet client : this._ctx.getClientsWithSolution()) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						fillRegleFromSqlQueryForClient(client);
					} catch (AppleException e) {
						throw new RuntimeException("Le thread traitant le client : " + client
								+ " a rencontré un problème : \n" + e.getMessage());
					}
				}

			});
		}

		executorService.shutdown();
		try {
			// Attend que tous les threads aient terminé avant de renvoyer la TreeMap
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (this._ctx.getClientsWithSolutionSize() > 1)
			ConnectionPool.endConnections();

		this._solutionsRaw.putAll(this.solutionsRawBuffer);

	}

	/**
	 * Fill all the regle content with their id for a single client
	 * 
	 * @param clientId - Client Id
	 * @throws DbAccessException  Database Exception
	 * @throws APPLE_DATA_EXCEPTION Internal Error
	 */
	private void fillRegleFromSqlQueryForClient(ClientProjet client) throws DbAccessException, APPLE_DATA_EXCEPTION {
		StringBuilder query = SqlQueryStrict.getSqlQueryReglesAnalyseesFromCodedExp(_ctx, this._lstTermesCodes, this._lstTermesCodesExtended,
				this.getNomTableActive(), client);

		HashSet<String> lstRegleFromTexteAnalyse = this.executeQueryExtended(query.toString());
		HashSet<String> lstRegleFromTexteAnalyseCommentaire = null;
		HashSet<String> lstRegleFromTexteNonAnalyse = null;
		HashSet<String> lstAllRegleId = new HashSet<String>(lstRegleFromTexteAnalyse);

		if (_inComment) {
			query = SqlQueryStrict.getSqlQueryReglesAnalyseesCommentaireFromSynonymes(_ctx, _lstTermesSynonymes,
					getNomTableActive(), client);
			lstRegleFromTexteAnalyseCommentaire = this.executeQueryExtended(query.toString());
			lstAllRegleId.addAll(lstRegleFromTexteAnalyseCommentaire);
		}

		if (_inNonAnalyzedRegles) {
			query = SqlQueryStrict.getSqlQueryReglesNonAnalyseesAndCommentaireFromSynonymes(_ctx, _lstTermesSynonymes,
					getNomTableActive(), client);
			lstRegleFromTexteNonAnalyse = this.executeQueryExtended(query.toString());
			lstAllRegleId.addAll(lstRegleFromTexteNonAnalyse);
		}
		
		AppleQueryAND queryAnd = new AppleQueryAND(PEAR_REGLE.REGLEID_INLIST(new ArrayList<String>(lstAllRegleId)),
				PEAR_REGLE.PRODUITID_EQ(_ctx.getProduit()));

		List<PEAR_REGLEData> lstData=null;
		// Récupération d'une connection dans la pool disponible
		try (PooledCnx pcnx=ConnectionPool.takeConnection()){
			lstData = PEAR_REGLE.listPrjData(pcnx.getCnxData(), client.getClientId(), client.getProjetId(), queryAnd, null, "DATE_EFFET DESC");
		} // autoclose du PooledCnx par le pattern try-resource

		if (lstData == null)
			return;
		for (PEAR_REGLEData data : lstData) {
			String regleId = data.getREGLEID();
			//String regleIdWithClientId = clientId + "#" + data.getPROJETID() + "#" + regleId;
			RegleResultat regleResultat = new RegleResultat(data.getREGLEID(), _ctx.getMainModel(), _ctx.getMainVersion(), data.getPROJETID(), client.getClientId(), false);
			if (regleId.charAt(3) == 'P') {
				//this._solutionsParamCotis.add(regleIdWithClientId);
			}

			if (RechercheUtils.hashContains(lstRegleFromTexteAnalyse, regleId)) {
				this.solutionsRawBuffer.put(regleResultat, data.getTEXTE());
			}

			if (_inComment && RechercheUtils.hashContains(lstRegleFromTexteAnalyseCommentaire, regleId)) {
				this.solutionsRawBuffer.put(regleResultat, data.getTEXTE());
			}

			if (_inNonAnalyzedRegles && RechercheUtils.hashContains(lstRegleFromTexteAnalyseCommentaire, regleId)) {
				this._solutionsNonAnalyzed.put(regleResultat, SqlQueryUtils.StringFromBlob(data.getTEXTE()));
			}
		}
	}

	@Override
	public ResultatsRecherche getSolutions() throws InterruptedException, AppleException {
		ResultatsRecherche solutions = super.getSolutions();
		if (this.pastSolutionRecherche != null) {
			solutions = solutions.concat(this.pastSolutionRecherche);
		}

		return solutions;
	}

}
