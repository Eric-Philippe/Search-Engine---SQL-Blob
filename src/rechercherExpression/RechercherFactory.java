package apple.util.rechercherExpression;

import java.util.concurrent.ConcurrentHashMap;

import apple.util.dummyClasses.ConnectionData;
import apple.util.dummyClasses.AppleException;
import apple.util.dummyClasses.ScenarioConnexion;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleJoker;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleStrict;
import apple.util.rechercherExpression.pear.RechercherExpressionPearJoker;
import apple.util.rechercherExpression.pear.RechercherExpressionPearStrict;
import apple.util.rechercherExpression.solutionRecherche.ResultatsRecherche;

/**
 * Factory class du module de Recherche.
 * 
 * <pre>
 * SolutionRecherche solutions = RechercherFactory.search(ScenarioConnexion, ContexteRecherche);
 * </pre>
 * <p>
 * 
 * <pre>
 * Recherche maRecherche = RechercherFactory.getRechercheInstance(Enum(PEAR | APPLE), ScenarioCOnnexion,
 * 		ContexteRecherche);
 * </pre>
 * <p>
 * 
 * @author Eric PHILIPPE
 *
 */
public class RechercherFactory {
	/**
	 * Méthode SERVER ORIENTEE unifiée de recherche se basant sur le
	 * ContexteRecherche pour diriger cette dernière
	 * 
	 * @param ctx - ContexteRecherche
	 * @return Solutions trouvée pour la recherche
	 * @throws InterruptedException - Thread cassé
	 * @throws Exception
	 */
	public static <ConnectionData> ResultatsRecherche search(ConnectionData cnx, ContexteRecherche ctx) throws InterruptedException, Exception, AppleException {
		boolean ppc = ctx.getSearchInPear();
		boolean apple = ctx.getSearchInApple();

		Recherche recherche = null;
		ResultatsRecherche solution = null;

		if (!ppc && apple) {
			recherche = getRechercheInstance(RechercheInstanceEnum.APPLE, cnx, ctx);
			solution = recherche.start();
		} else if (ppc && !apple) {
			recherche = getRechercheInstance(RechercheInstanceEnum.PEAR, cnx, ctx);
			solution = recherche.start();
		} else if (ppc && apple)
		{
			recherche = getRechercheInstance(RechercheInstanceEnum.APPLE, cnx, ctx);
			solution = recherche.start();
			 ConcurrentHashMap<String, AppleMot> cacheAppleMot = recherche.getCacheAppleMot();
			 ConcurrentHashMap<String, String> cacheTextEnClair = recherche.getCacheTextEnClair();
			recherche = getRechercheInstance(RechercheInstanceEnum.PEAR, cnx, ctx, solution, cacheAppleMot, cacheTextEnClair);
			solution = recherche.start();
		}

		return solution;

	}

	/**
	 * Méthode BATCH ORIENTEE unifiée de recherche se basant sur le
	 * ScenarioConnexion pour construire le ContexteRecherche pour diriger cette dernière
	 * 
	 * @param snr - ScenarioConnexion
	 * @param ctx - ContexteRecherche
	 * @return Solutions trouvée pour la recherche
	 * @throws InterruptedException - Thread cassé
	 */
	public static ResultatsRecherche search(ConnectionData cnx, ScenarioConnexion snr) throws AppleException, Exception {
		ContexteRecherche ctx = ContexteRecherche.construct(cnx, snr);
		return search(cnx, ctx);
	}
	
	public static ResultatsRecherche search(ScenarioConnexion snr, ContexteRecherche ctx) throws AppleException, Exception {
		return search(snr.getConnectionData(), ctx);
	}


	/**
	 * Retourne une instance de recherche à l'aide de l'enum type donné en paramètre
	 * 
	 * @param type - Type de la recherche (APPLE, PEAR)
	 * @param snr  - ScenarioConnexion
	 * @param ctx  - ContexteRecherche
	 * @return l'instance de recherche désiré
	 * @throws AppleException 
	 */
	public static Recherche getRechercheInstance(RechercheInstanceEnum type, ConnectionData cnx,
			ContexteRecherche ctx) throws AppleException {
		switch (type) {
		case APPLE:
			if (ctx.expressionHasJoker())
				return new RechercherExpressionAppleJoker(cnx, ctx);
			else
				return new RechercherExpressionAppleStrict(cnx, ctx);
		case PEAR:
			if (ctx.expressionHasJoker())
				return new RechercherExpressionPearJoker(cnx, ctx);
			else return new RechercherExpressionPearStrict(cnx, ctx);
		default:
			return null;

		}
	}

	/**
	 * Recherche factory pour une recherche n + 1 (On suppose qu'une première
	 * recherche a été faite au préalable.
	 * 
	 * @param type      - Type de la recherche (PEAR) [APPLE exclu]
	 * @param snr - ScenarioConnexion
	 * @param ctx - ContexteRecherche
	 * @param solutions - Solutions n - 1
	 * @return l'instance désirée
	 * @throws AppleException 
	 */
	public static Recherche getRechercheInstance(RechercheInstanceEnum type, ConnectionData cnx,
			ContexteRecherche ctx, ResultatsRecherche solutions, ConcurrentHashMap<String, AppleMot> cacheMot, ConcurrentHashMap<String, String> cacheClair) throws AppleException {
		switch (type) {
		case PEAR:
			if (ctx.expressionHasJoker()) return new RechercherExpressionPearJoker(cnx, ctx, solutions, cacheMot, cacheClair);
			else return new RechercherExpressionPearStrict(cnx, ctx, solutions, cacheMot, cacheClair);
		default:
			return null;
		}
	}

}

/**
 * Types de recherches disponibles
 * 
 * @author Eric PHILIPPE
 *
 */
enum RechercheInstanceEnum {
	APPLE, PEAR
}