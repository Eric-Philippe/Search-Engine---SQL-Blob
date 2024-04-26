package apple.util.rechercherExpression.apple;

import java.util.HashSet;

import apple.core.ConnectionData;
import apple.core.AppleException;
import apple.scenario.ScenarioConnexion;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ExpressionProcessor;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryJoker;

/**
 * Recherche une expression dans APPLE à l'aide de joker de recherche
 * 
 * <p>
 * La Recherche d'expression avec Joker étend par nature la
 * {@link RechercherExpressionAppleStrict}
 * 
 * <p>
 * Ce classe permet d'être moins strict et moins affiné sur la qualité et le
 * type des mots recherchés en priorisant sur l'existence de solution à l'aide
 * des Jokers de recherches.
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercherExpressionAppleJoker extends RechercherExpressionAppleStrict {
	private static final long serialVersionUID = -5818579656655325970L;

	/**
	 * @throws AppleException 
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion,
	 *      ContexteRecherche)
	 */
	public RechercherExpressionAppleJoker(ConnectionData cnx, ContexteRecherche ctx) throws AppleException {
		super(cnx, ctx);
	}

	/**
	 * @see apple.util.rechercherExpression.Recherche#Recherche(ScenarioConnexion)
	 */
	public RechercherExpressionAppleJoker(ConnectionData cnx, ScenarioConnexion snr) throws AppleException {
		super(cnx, snr);
	}

	@Override
	public ResultatsRecherche start() throws AppleException, InterruptedException {
		getRegleFromSqlQueryJoker();
		return getSolutions();
	}

	public void getRegleFromSqlQueryJoker() throws AppleException  {
		String query = SqlQueryJoker.getAllWordsFromJokerExpressionApple(_ctx, _lstTermesCodes);
		HashSet<String> words = this.executeQuery(query);
		_lstTermesCodesExtended = ExpressionProcessor.buildListTerms(_cnx, _ctx, words);
		
		this.getRegleFromSqlQuery();
	}

}
