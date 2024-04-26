package apple.util.rechercherExpression.solutionRecherche;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import apple.util.dummyClasses.AppleMot;
import apple.util.rechercherExpression.Recherche;
import apple.util.rechercherExpression.RegleResultat;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;

/**
 * Classe Resultats Recherche permettant le stockage de l'objet
 * {@link ResultatsRecherche}
 * 
 * <p>
 * Classe Solution permettant le filtrage et le stockage des solutions indexées
 * de la recherche faisant le lien entre les résultats de la recherche jusqu'à
 * la solution complète étendue
 * 
 * <p>
 * Une solution indexée est une pair d'integer représentant le début et la fin
 * de l'expression trouvée.
 * 
 * @author Eric PHILIPPE
 *
 */
public class ResultatsRecherche implements Serializable {
	private static final long serialVersionUID = -5703608299693302276L;

	/** Stockage principal */
	private List<ResultatRecherche> resultatRecherche;

	/** Caches */
	transient private ConcurrentHashMap<String, AppleMot> cacheAppleMot = new ConcurrentHashMap<String, AppleMot>();
	transient private ConcurrentHashMap<String, String> cacheTextEnClair = new ConcurrentHashMap<String, String>();
	transient private HashSet<String> _solutionRawParamCotis = new HashSet<>();

	/**
	 * Créé un nouveau stockage listé de toutes les résultats de Recherche en
	 * passant le témoin de tous les caches créé à l'étape d'avant dans la
	 * {@link Recherche}
	 * 
	 * @param decodedSolutions
	 * @param synonyms
	 * @param searchInComments
	 * @param cacheAppleMot
	 * @param cacheTextEnClair
	 * @throws InterruptedException
	 */
	public ResultatsRecherche(Map<RegleResultat, String> decodedSolutions, List<List<String>> synonyms,
			boolean searchInComments, ConcurrentHashMap<String, AppleMot> cacheAppleMot,
			ConcurrentHashMap<String, String> cacheTextEnClair, HashSet<String> solutionParamCotis) throws InterruptedException {
		this.resultatRecherche = ResultatsRecherche.rechercherExpression(decodedSolutions, synonyms, searchInComments);
		this.cacheAppleMot = cacheAppleMot;
		this.cacheTextEnClair = cacheTextEnClair;
		this._solutionRawParamCotis = solutionParamCotis;
	}

	public ResultatsRecherche(TreeMap<RegleResultat, String> decodedRaw, List<List<String>> lstTermesSynonymes, boolean inComment, ConcurrentHashMap<String, AppleMot> cacheAppleMot, ConcurrentHashMap<String, String> cacheTextEnClair, HashSet<String> solutionsParamCotis) {
	}

	/**
	 * Ajoute un résultat dans la liste
	 * 
	 * @param resultatRecherche
	 */
	public void addResultat(ResultatRecherche resultatRecherche) {
		this.resultatRecherche.add(resultatRecherche);
	}

	/**
	 * Getter de toutes les solutions
	 * 
	 * @return
	 */
	public List<ResultatRecherche> getSolutions() {
		return this.resultatRecherche;
	}
	
	public HashSet<String> getParamCotisRawSolutions() {
		return this._solutionRawParamCotis;
	}
	
	public ArrayList<ResultatRecherche> getAllRegleIdsFromClients() {
		ArrayList<ResultatRecherche> solutionsString = new ArrayList<>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isFromPear()) {
				solutionsString.add(resSol);
			}
		}

		return solutionsString;
	}
	
	public ArrayList<ResultatRecherche> getAllRegleIdsFromModele() {
		ArrayList<ResultatRecherche> solutionsString = new ArrayList<>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isFromApple()) {
				solutionsString.add(resSol);
			}
		}

		return solutionsString;
	}
	
	/**
	 * Get les solutions en provenance de APPLE uniquement
	 * 
	 * @return
	 */
	public ArrayList<ResultatRecherche> getSolutionsAppleOnly() {
		ArrayList<ResultatRecherche> solutionsApple = new ArrayList<>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (!resSol.getRegleId().startsWith("Z8_"))
				solutionsApple.add(resSol);
		}

		return solutionsApple;
	}

	/**
	 * Get les solutions en provenance de PEAR uniquement
	 * 
	 * @return
	 */
	public ArrayList<ResultatRecherche> getSolutionsPearOnly() {
		ArrayList<ResultatRecherche> solutionsPear = new ArrayList<>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.getRegleId().startsWith("Z8_"))
				solutionsPear.add(resSol);
		}

		return solutionsPear;
	}

	/**
	 * Getter Unitaire d'une solution APPLE à l'aide de son indentifiant
	 * 
	 * @param regleId
	 * @return
	 */
	public ResultatRecherche getSolution(String regleId) {
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.getRegleId().equals(regleId) && resSol.getTexte() != "") {
				return resSol;
			}
		}

		return null;
	}

	/**
	 * Getter Unitaire d'une solution PEAR à l'aide de ses clés indentifiants
	 * 
	 * @param regleId
	 * @return
	 */
	public ResultatRecherche getSolution(String clientId, String projetId, String regleId) {
		if (!regleId.startsWith("Z8")) return getSolution(regleId);
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.getTexte() != null && resSol.getRegleId().equals(regleId) && resSol.getClientId() != null
					&& resSol.getClientId().equals(clientId) && resSol.getProjetId().equals(projetId)) {
				return resSol;
			}
		}

		return null;
	}

	/**
	 * Set l'utilisation auprès d'une règle APPLE
	 * 
	 * @param regleId
	 * @param utilisation
	 */
	public void addUtilisation(String regleId, String utilisation) {
		ResultatRecherche res = getSolution(regleId);
		if (res == null)
			return;

		res.setUtilisation(utilisation);
	}

	/**
	 * Set l'utilisation auprès d'une règle PEAR
	 * 
	 * @param regleId
	 * @param clientId
	 * @param projetId
	 * @param utilisation
	 */
	public void addUtilisation(String regleId, String clientId, String projetId, String utilisation) {
		ResultatRecherche res = getSolution(clientId, projetId, regleId);
		if (res == null)
			return;

		res.setUtilisation(utilisation);
	}

	/**
	 * Retourne sous un {@link String[]} l'ensemble des règles ID de APPLE
	 * 
	 * @return
	 */
	public String[] getStringTupleRegleId() {
		List<String> reglesId = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			reglesId.add(resSol.getRegleId());
		}

		return reglesId.toArray(new String[0]);
	}

	/**
	 * Retourne sous un {@link String[]} l'ensemble des règles ID de PEAR sous format
	 * CLIENT_ID#PROJET_ID#REGLE_ID
	 * 
	 * @return
	 */
	public String[] getStringTupleClientProjetRegleParamCotis(ArrayList<String> clients, String defaultProjects) {
		List<String> reglesId = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isParamCotis()) {
				if (resSol.isFromPear()) {
					reglesId.add(SolutionTool.toId(resSol.getClientId(), resSol.getProjetId(), resSol.getRegleId()));
				} else {
					for (String client: clients) {
						reglesId.add(SolutionTool.toId(client, defaultProjects, resSol.getRegleId()));
					}
				}
			}
		}
		
		if (reglesId.isEmpty()) return null;
		return reglesId.toArray(new String[0]);
	}
	
	public String[] getStringTupleRegleIdParamCotis() {
		List<String> reglesId = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isParamCotis()) {
				reglesId.add(resSol.getRegleId());
			}
		}

		return reglesId.toArray(new String[0]);
	}

	/**
	 * Retourne sous un {@link String[]} l'ensemble des règles ID de PEAR sous format
	 * CLIENT_ID#PROJET_ID#REGLE_ID
	 * 
	 * @return
	 */
	public String[] getStringTupleClientProjetRegle(List<ClientProjet> clients, String defaultProjects) {
		List<String> reglesId = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isFromPear()) {
				reglesId.add(SolutionTool.toId(resSol.getClientId(), resSol.getProjetId(), resSol.getRegleId()));
			} else {
				for (ClientProjet client: clients) {
					reglesId.add(SolutionTool.toId(client.getClientId(), defaultProjects, resSol.getRegleId()));
				}
			}
		}
		
		if (reglesId.isEmpty()) return null;
		return reglesId.toArray(new String[0]);
	}
	
	public String[] getTrouplesClientProjetRegle(List<ClientProjet> clients, String defaultProjects) {
		return getTrouplesClientProjetRegle(clients, defaultProjects, "BC");
	}

	/**
	 * Retourne sous un {@link String[]} l'ensemble des règles ID de PEAR sous format
	 * "(CLIENT_ID, PROJET_ID, REGLE_ID)"
	 * 
	 * @return
	 */
	public String[] getTrouplesClientProjetRegle(List<ClientProjet> clients, String defaultProjects, String maxProject) {
		List<String> trouples = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isFromPear()) {
				trouples.add("('" + resSol.getClientId() + "', '" + resSol.getProjetId() + "', '" + resSol.getRegleId() + "')");
				if (!resSol.getProjetId().equals(maxProject)) {
					trouples.add("('" + resSol.getClientId() + "', '" + maxProject + "', '" + resSol.getRegleId() + "')");
				}
			} else {
				for (ClientProjet client: clients) {
					trouples.add("('" + client.getClientId() + "', '" + defaultProjects + "', '" + resSol.getRegleId() + "')");
					if (!defaultProjects.equals("BC")) {
						trouples.add("('" + client + "', 'BC', '" + resSol.getRegleId() + "')");
					}
				}
			}
		}
		
		

		return trouples.toArray(new String[0]);
	}
	
	public String[] getCoupleClientRegle(List<ClientProjet> clients) {
		List<String> trouples = new ArrayList<String>();
		for (ResultatRecherche resSol : this.resultatRecherche) {
			if (resSol.isFromPear()) {
				trouples.add("('" + resSol.getClientId() + "', '" + resSol.getRegleId() + "')");
			} else {
				for (ClientProjet client: clients) {
					trouples.add("('" + client.getClientId() + "', '" + resSol.getRegleId() + "')");
				}
			}
		}
		
		

		return trouples.toArray(new String[0]);
	}

	/**
	 * Contains méthod for APPLE results
	 * 
	 * @param regleId
	 * @return
	 */
	public boolean contains(String regleId) {
		for (ResultatRecherche resultat : this.resultatRecherche) {
			if (resultat.isFromApple() && resultat.getRegleId().equals(regleId) && resultat.getTexte() != null)
				return true;
		}
		return false;
	}
	
	/**
	 * Contains méthod for PEAR results
	 * 
	 * @param regleId
	 * @return
	 */
	public boolean contains(String clientId, String projetId, String regleId) {
		for (ResultatRecherche resultat : this.resultatRecherche) {
			if (resultat.isFromPear() && resultat.getRegleId().equals(regleId) && resultat.getTexte() != null) {
				String myClientId = resultat.getClientId();
				if (myClientId == null) myClientId = "";
				String myProjetId = resultat.getProjetId();
				if (projetId == null) projetId = "";
				if (myProjetId.equals(projetId) && myClientId.equals(clientId)) return true;
			}
		}
		return false;
	}

	public ResultatsRecherche concat(ResultatsRecherche foreignResult) {
		for (ResultatRecherche resSol : foreignResult.getSolutions()) {
			this.resultatRecherche.add(resSol);
		}

		return this;
	}

	public ConcurrentHashMap<String, AppleMot> getCacheAppleMot() {
		return this.cacheAppleMot;
	}

	public ConcurrentHashMap<String, String> getCacheTextEnClair() {
		return this.cacheTextEnClair;
	}
	
	public void emptyCache() {
		this.cacheAppleMot = new ConcurrentHashMap<>();
		this.cacheTextEnClair = new ConcurrentHashMap<>();
	}

	/**
	 * Recherche les index des expressions dans tous les solutions
	 * 
	 * @param decodedSolutions
	 * @param synonyms
	 * @param searchInComments
	 * @return
	 * @throws InterruptedException
	 */
	private static List<ResultatRecherche> rechercherExpression(Map<RegleResultat, String> decodedSolutions,
			List<List<String>> synonyms, boolean searchInComments) throws InterruptedException {
		List<ResultatRecherche> researchResults = new ArrayList<>();
		List<Callable<Void>> tasks = new ArrayList<>();

		for (Map.Entry<RegleResultat, String> regleIdDecodedText : decodedSolutions.entrySet()) {
			RegleResultat regleResultat = regleIdDecodedText.getKey();
			String decodedText = regleIdDecodedText.getValue();
			
			Pattern regex = ExpressionMatcher.getCompiledRegex(synonyms);

			tasks.add(() -> {
				List<PairSolution> occurrences = ExpressionMatcher.matchAll(regex,
						ExpressionMatcher.replaceHyphen(decodedText), searchInComments);
				if (occurrences.isEmpty())
					return null;

				synchronized (researchResults) {
					researchResults.add(new ResultatRecherche(regleResultat, decodedText, occurrences));
				}
				return null;
			});
		}

		if (!tasks.isEmpty()) {
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			try {
				executor.invokeAll(tasks);
			} finally {
				executor.shutdown();
			}
		}

		return researchResults;
	}

}