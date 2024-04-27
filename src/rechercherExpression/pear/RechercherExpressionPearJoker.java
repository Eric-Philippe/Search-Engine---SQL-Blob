package apple.util.rechercherExpression.pear;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import apple.core.ConnectionData;
import apple.core.AppleException;
import apple.scenario.ScenarioConnexion;
import apple.util.AppleMot;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ExpressionProcessor;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryJoker;

/**
 * Recherche une expression dans PEAR de manière strict
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercherExpressionPearJoker extends RechercherExpressionPearStrict {
	private static final long serialVersionUID = 6781344581871535589L;

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche) Ce constructeur prend en plus en paramètre une
	 *      SolutionRecherche (Devrait provenir d'une première recherche sous APPLE)
	 *      pour pouvoir compiler les deux par la suite.
	 */
	public RechercherExpressionPearJoker(ConnectionData cnx, ContexteRecherche ctx, ResultatsRecherche solutions,
			ConcurrentHashMap<String, AppleMot> cacheMot, ConcurrentHashMap<String, String> cacheClair) throws AppleException {
		super(cnx, ctx, solutions, cacheMot, cacheClair);
	}

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche)
	 */
	public RechercherExpressionPearJoker(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		super(cnx, ctx);
	}

	/**
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion)
	 */
	public RechercherExpressionPearJoker(ConnectionData cnx, ScenarioConnexion snr) throws AppleException {
		super(cnx, snr);
	}
	
	@Override
	public ResultatsRecherche start() throws AppleException, InterruptedException {
		getRegleFromSqlQueryJoker();
		return getSolutions();
	}
	
	public void getRegleFromSqlQueryJoker() throws AppleException  {
		String query = SqlQueryJoker.getAllWordFromJokerExpressionApplePear(_ctx, _lstTermesCodes);
		HashSet<String> words = this.executeQuery(query);
		_lstTermesCodesExtended = ExpressionProcessor.buildListTerms(_cnx, _ctx, words);
		
		this.getRegleFromSqlQuery();
	}
}
