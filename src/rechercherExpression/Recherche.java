package apple.util.rechercherExpression;

import java.io.Serial;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import apple.util.decoder.ConnectionPool;
import apple.util.decoder.DecoderMultiple;
import apple.util.decoder.PooledCnx;
import apple.util.dummyClasses.*;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryStrict;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryUtils;

/**
 * Classe abstraite permettant l'héritage à tous types de class de recherche de
 * regles.
 * <p>
 * Cette classe donne en conséquence les outils (variables globales, méthodes)
 * nécessaires pour effectuer toutes les recherches et unifie le tout.
 *
 * @author Eric PHILIPPE
 *
 */
public abstract class Recherche implements Serializable {
	@Serial
	private static final long serialVersionUID = -3980827519840065570L;
	/** Contexte de Recherche - Kind of StateHolder */
	protected ContexteRecherche _ctx = null;
	protected ConnectionData _cnx = null;
	protected transient DbAccess _dbAccess = null;
	/** Liste des termes codés */
	protected ArrayList<AppleMotRecherche> _lstTermesCodes = null;
	protected ArrayList<AppleMotRecherche> _lstTermesCodesExtended = null;

	/** Liste des termes synonymes */
	protected List<List<String>> _lstTermesSynonymes = null;

	/** Solutions provenant du premier filtrage, entièrement codées */
	protected TreeMap<RegleResultat, byte[]> _solutionsRaw = new TreeMap<>();
	/** Solutions provenant du premier filtra entièrement décodées */
	protected TreeMap<RegleResultat, String> _solutionsDecoded = new TreeMap<>();
	/** Solutions ne nécessitant pas d'être décodée */
	protected TreeMap<RegleResultat, String> _solutionsNonAnalyzed = new TreeMap<>();
	protected HashSet<String> _solutionsParamCotis = new HashSet<>();

	/** Research Cache for AppleMot */
	protected ConcurrentHashMap<String, AppleMot> cacheAppleMot = new ConcurrentHashMap<>();
	/** Research Cache for the text en clair */
	protected ConcurrentHashMap<String, String> cacheTextEnClair = new ConcurrentHashMap<>();

	/** Alias pour le boolean de la recherche en commentaire */
	protected boolean _inComment = false;
	/** Alias pour le boolean de la recherche dans les textes non analysés */
	protected boolean _inNonAnalyzedRegles = false;
	/** Alias pour le boolean de la recherche dans les textes de param cotis */
	protected boolean _inParamCotis = false;

	/**
	 * Permet la bascule et l'unification entre la recherche dans la table APPLE_REGLE
	 * et PEAR_REGLE
	 * 
	 * @return le nom de la table active
	 */
	public abstract String getNomTableActive();

	/**
	 * Principal lanceur. Totalité des exceptions non capturées afin de laisser le
	 * scenario agir en conséquence à sa guise.
	 * 
	 * @return une instance de Solution Recherche
     */
	public abstract ResultatsRecherche start()
			throws AppleException, InterruptedException;

	/**
	 * Constructor de base d'une instance de Recherche seulement à l'aide du
	 * Scenario Connexion
	 * 
	 * @param snr - Scenario Connexion
	 * @throws AppleException - Jette le lanceur si le contexte n'a pu être construit
	 *                      depuis le Scenario Connexion
	 */
	public Recherche(ConnectionData cnx, ScenarioConnexion snr) throws AppleException {
		ContexteRecherche contextSearch = ContexteRecherche.construct(cnx, snr);
		init(cnx, contextSearch);
	}

	/**
	 * Constructeur de base d'une instance de Recherche
	 * 
	 * @param cnx - COnnection Data
	 * @param ctx - Contexte de recherche
	 * @throws AppleException 
	 */
	public Recherche(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		init(cnx, ctx);
	}

	/**
	 * Constructeur de base d'une instance de Recherche
	 * 
	 * @param snx - Connexion
	 * @param ctx - Contexte de recherche
     */
	public Recherche(ScenarioConnexion snx, ContexteRecherche ctx) throws AppleException {
		init(snx.getConnectionData(), ctx);
	}

	/**
	 * Main Process for the research
	 * 
	 * @param cnx - Scenario Connexion
	 * @param ctx - Contexte Recherche
	 */
	private void init(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		_ctx = ctx;
		_cnx = cnx;
		boolean dbClosed = _cnx.openDbAccess();
		_dbAccess = _cnx.getDbAccess();
		ArrayList<AppleMotRecherche> lstTermes = ExpressionProcessor.buildListTerms(_cnx, _ctx, _ctx.getMainModel(),
				_ctx.getMainVersion(), false);
		if (dbClosed) _cnx.closeDbAccess();
		_lstTermesCodes = ExpressionProcessor.filterListTerms(lstTermes);
		_lstTermesSynonymes = ExpressionProcessor.buildExpressionWithSynonyme(lstTermes);
		_inComment = _ctx._searchInComments;
		_inNonAnalyzedRegles = _ctx._searchInNonAnalyzedText;
		_inParamCotis = _ctx._searchInParamCotis;
	}

	/**
	 * Alias d'exécution d'une query SQL
	 * 
	 * @param query - Query SQL
	 * @return - Resultat de la Query
	 */
	public HashSet<String> executeQuery(String query, ConnectionData cnx)  {
		boolean dbAccess = cnx.openDbAccess();
		HashSet<String> hashResult = new HashSet<String>();
		try {
			ResultSet result = cnx.getDbAccess().executeSelect(query);

			hashResult = SqlQueryUtils.listRegleToHashSet(result);
		} catch (Exception err) {
			Logger.error(err.toString(), err);
		} finally {
			if (dbAccess) cnx.closeDbAccess();
		}

		return hashResult;
	}

	public HashMap<String, String> executeQueryHashMap(StringBuilder query) {		
		HashMap<String, String> hashResult = new HashMap<>();
		ResultSet result;
		boolean dbAccess = false;
		try {
			dbAccess = this._cnx.openDbAccess();
			result = this._cnx.getDbAccess().executeSelect(query.toString());

			while (result.next()) {
				hashResult.put(result.getString(1),result.getString(2));
			}

		} catch (SQLException e) {
			Logger.error(e.toString(), e);
		} finally {
			if (dbAccess) this._cnx.closeDbAccess();
		}

		return hashResult;
	}

	public List<String> executeQueryList(StringBuilder query) {		
		List<String> listResult = new ArrayList<>();
		ResultSet result;
		boolean dbAccess = false;
		try {
			dbAccess = this._cnx.openDbAccess();
			result = this._cnx.getDbAccess().executeSelect(query.toString());

			while (result.next()) {
				listResult.add(result.getString(1));
			}

		} catch (SQLException e) {
			Logger.error(e.toString(), e);
		} finally {
			if (dbAccess) this._cnx.closeDbAccess();
		}

		return listResult;
	}

	/**
	 * Alias d'exécution d'une query SQL
	 * 
	 * @param query - Query SQL
	 * @return - Resultat de la Query
	 */
	public HashSet<String> executeQuery(String query)  {
		return this.executeQuery(query, this._cnx);
	}

	/**
	 * Alias d'exécution d'une raw query SQL
	 * 
	 * @param query - Raw Query SQL
	 * @return - Resultat de la Query
	 */
	public HashSet<String> executeQuery(StringBuilder query)  {
		return this.executeQuery(query.toString());
	}

	/**
	 * Donne le total de solutions actuelles
	 * 
	 * @return la taille total de solutions actuelles
	 */
	public int getTotalSolutions() {
		return this._solutionsRaw.size();
	}

	/**
	 * Getter finale de l'instance de Solution de cette Recherche
	 * 
	 * @return l'instance finale des solutions de la Recherche
	 * @throws InterruptedException - Problème dans un des Threads
	 * @throws AppleException
	 */
	public ResultatsRecherche getSolutions() throws InterruptedException, AppleException {
		DecoderMultiple decoder = new DecoderMultiple(this.getCacheAppleMot(), this.getCacheTextEnClair());
		TreeMap<RegleResultat, String> decodedRaw = decoder.decodeMultiple(_solutionsRaw, _cnx, _ctx.getMainModel(),
				_ctx.getMainVersion(), _ctx.getProject(), _ctx.getProduit());

		this.setCacheAppletMot(decoder.getAppleMotCache());
		this.setCacheTextEnClair(decoder.getTexteEnClairCache());

		if (!this._solutionsNonAnalyzed.isEmpty()) {
			decodedRaw.putAll(this._solutionsNonAnalyzed);
		}

		ResultatsRecherche resultatsRecherche = new ResultatsRecherche(decodedRaw, _lstTermesSynonymes, _inComment,
				this.getCacheAppleMot(), this.getCacheTextEnClair(), _solutionsParamCotis);

		if (this.getTotalSolutions() != 0)
			setUtilisation(_ctx, resultatsRecherche, _lstTermesCodes);

		return resultatsRecherche;
	}
	

	/**
	 * Utilisation Setter sur toutes les solutions
	 *
     */
	public void setUtilisation(ContexteRecherche ctx, ResultatsRecherche resultRech, ArrayList<AppleMotRecherche> motsCode) {
		if (motsCode.size() != 1)
			return;
		if (ctx.getSearchInApple() && !resultRech.getSolutionsAppleOnly().isEmpty())
			fillUtilisationResultatAPPLE(resultRech, ctx, motsCode.getFirst().getMotCode());
		if (ctx.getSearchInPear() && !resultRech.getSolutionsPearOnly().isEmpty())
			fillUtilisationResultatPEAR(resultRech, ctx, motsCode.getFirst().getMotCode());
	}
	
	/**
	 * Récupère l'utilisation de l'expression rercherché dans APPLE
	 * 
	 * @param resultatsRecherche
	 * @param ctx
	 * @param mot
	 */
	private static void fillUtilisationResultatAPPLE(ResultatsRecherche resultatsRecherche, ContexteRecherche ctx,
			String mot) {
		String[] reglesId = resultatsRecherche.getStringTupleRegleId();
		String query = SqlQueryStrict.getSqlQueryUtilisationMotAPPLE(ctx, reglesId, mot).toString();
		// Récupération d'une connection dans la pool disponible
		try (PooledCnx pcnx=ConnectionPool.takeConnection()){
            assert pcnx != null;
            ResultSet result = pcnx.getCnxData().getDbAccess().executeSelect(query);
			while (result.next()) {
				String regleId = result.getString(1);
				String utilisation = result.getString(2);
				resultatsRecherche.addUtilisation(regleId, utilisation);
			}
		} catch (SQLException e) {
			Logger.error(e.toString(), e);
		} // autoclose du PooledCnx par le pattern try-resource

	}

	/**
	 * Récupère l'utilisation de l'expression rercherché dans PEAR
	 * 
	 * @param resultatsRecherche
	 * @param ctx
	 * @param mot
	 */
	private static void fillUtilisationResultatPEAR(ResultatsRecherche resultatsRecherche, ContexteRecherche ctx,
			String mot) {
		String[] reglesId = resultatsRecherche.getTrouplesClientProjetRegle(ctx.getAllInterstingClientProjet(), ctx.getProject());
		String query = SqlQueryStrict.getSqlQueryUtilisationMotPEAR(ctx, reglesId, mot).toString();
		// Récupération d'une connection dans la pool disponible
		try (PooledCnx pcnx=ConnectionPool.takeConnection()){
            assert pcnx != null;
            ResultSet result = pcnx.getCnxData().getDbAccess().executeSelect(query);
			while (result.next()) {
				String clientId = result.getString(1);
				String projetId = result.getString(2);
				String regleId = result.getString(3);
				String utilisation = result.getString(4);
				resultatsRecherche.addUtilisation(regleId, clientId, projetId, utilisation);
			}
		} catch (SQLException e) {
			Logger.error(e.toString(), e);
		} // autoclose du PooledCnx par le pattern try-resource
	}


	/**
	 * Set AppleMot Cache
	 * 
	 * @param cache è Cache ConcurrentHashMap
	 */
	public void setCacheAppletMot(ConcurrentHashMap<String, AppleMot> cache) {
		cacheAppleMot = cache;
	}

	/**
	 * Set Text en Clair Cache
	 * 
	 * @param cache è Cache ConcurrentHashMap
	 */
	public void setCacheTextEnClair(ConcurrentHashMap<String, String> cache) {
		cacheTextEnClair = cache;
	}

	/**
	 * AppleMot Cache Getter
	 * 
	 * @return Cache AppleMot
	 */
	public ConcurrentHashMap<String, AppleMot> getCacheAppleMot() {
		return cacheAppleMot;
	}

	/**
	 * Text en Clair cache get
	 * 
	 * @return text en clair cache
	 */
	public ConcurrentHashMap<String, String> getCacheTextEnClair() {
		return cacheTextEnClair;
	}

}
