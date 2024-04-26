package apple.util.rechercherExpression;

import apple.util.dummyClasses.ConnectionData;
import apple.util.dummyClasses.Logger;
import apple.util.dummyClasses.ScenarioConnexion;

import java.io.Serial;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contexte de Recherche du module de Recherche
 * 
 * <p>
 * L'objet Contexte stock toutes les informations relatives à la recherche, à
 * savoir les différents filtres, et est ensuite partagé tout au long du
 * processus
 * 
 * <p>
 * L'objet ContexteRecherche offre deux orientations à savoir une Server ; tout
 * est direct et lancé de manière synchrone au processus. Si l'on veut faire
 * passer le processus en tâche asynchrone, alors on propose des méthodes pour
 * démonter et remonter l'objet à l'aide des variables de
 * {@link apple.util.dummyClasses.ScenarioConnexion}
 * 
 * <p>
 * La class contient également les éléments communs et finaux à la recherche
 * permettant le changement direct et rapide des éléments. Voir
 * {@link #LIMITED_JOKER_CHAR} {@link #UNLIMITED_JOKER_CHAR} ainsi que les
 * valeurs par défauts {@link #DEFAULT_PROJECT} {@link #DEFAULT_VERSION}
 * 
 * @author Eric PHILIPPE
 */
public class ContexteRecherche implements Serializable {
	private static final long serialVersionUID = 3412016009159098852L;
	/** Current project you're searching in */
	String _project = null;
	/** Current version you're searching in */
	String _version = null;
	/** Current modele you're searching in */
	String _modele = null;
	/** Version Nat : MANDATORY */
	String _versionNat = null;
	/** Modele Nat : MANDATORY */
	String _modeleNat = null;
	/** Z2M par défaut */
	String _produit = "Z2M";
	/** By default : Enabled */
	boolean _searchInApple = true;
	/** By default : Disabled */
	boolean _searchInPear = false;
	/** By default : Disabled */
	boolean _searchInComments = false;
	/** By default : Disabled */
	boolean _searchInNonAnalyzedText = false;
	/** By default : Disabled */
	boolean _searchInParamCotis = false;
	/** by default : Enabled */
	boolean _saveCacheInSolution = true;
	/** By default : Disabled */
	boolean _searchInEveryProjects = false;
	/** By default : Enabled */
	boolean _decodeNeighborTexts = true;
	/** Raw expression */
	String _expressionSearched = null;
	/** Raffined expression */
	String _expressionRaffinee = null;
	/** We may search inside a list of clients only */
	List<ClientProjet> _InputClients = new ArrayList<>();
	/**
	 * Only a bunch of clients from the original list might be worth working with
	 * instead of everything
	 */
	List<ClientProjet> _ClientsWithSolution = new ArrayList<>();

	/** @USEFUL_CONSTANTS */
	final public static CharSequence LIMITED_JOKER_CHAR = "^";
	final public static CharSequence UNLIMITED_JOKER_CHAR = "~";
	final public static String DEFAULT_PROJECT = "BC";
	final public static String DEFAULT_VERSION = "001";
	final public static String EMPTY_REGLE = "EMPTY";

	/**
	 * ========== @MAIN_TRIGGER ==========
	 */
	/**
	 * Check if the search context is ready to be used to trigger the search
	 * 
	 * @return if the contexte you gave is ready to be launched
	 */
	public boolean isReadyForLaunch() {
		if (_expressionSearched == null || _expressionSearched.isEmpty())
			return false;
		if (!_searchInApple && !_searchInPear)
			return false;
		if (_project == null)
			return false;
		if (_modeleNat == null || _versionNat == null)
			return false;
		if (_searchInApple && !isValidForApple())
			return false;
        return !_searchInPear || isValidForPear();
    }

	/**
	 * ========== @DECONSTRUCTOR ===========
	 */
	/**
	 * Deconstruct your search context into a scenarioConnexion It supposes that
	 * your search context is ready
	 * 
	 * @param snr - Scenario Connexion
	 */
	public void deconstruct(ScenarioConnexion snr) {
		if (!isReadyForLaunch())
			throw new IllegalArgumentException(
					"Merci de vérifier l'entiéreté de vos paramètres avant de déconstruire votre builder !");
		snr.addValue("T_EXPRESSION", _expressionSearched);
		snr.addValue("T_CLIENTS", _InputClients.toString());
		snr.addValue("T_SEARCH_IN_APPLE", _searchInApple);
		snr.addValue("T_SEARCH_IN_PEAR", _searchInPear);
		snr.addValue("T_SEARCH_IN_COMMENTS", _searchInComments);
		snr.addValue("T_SEARCH_IN_NON_ANALYSED_TEXT", _searchInNonAnalyzedText);
		snr.addValue("T_SEARCH_IN_PARAMCOTIS", _searchInParamCotis);
		snr.addValue("T_VERSION", _version);
		snr.addValue("T_MODELE", _modele);
		snr.addValue("T_VERNAT", _versionNat);
		snr.addValue("T_MODNAT", _modeleNat);
		snr.addValue("T_PRODUIT", _produit);
		snr.addValue("T_PROJET", _project);
		snr.addValue("T_SAVE_CACHE", _saveCacheInSolution);
	}

	/**
	 * ========== @CONSTRUCTOR ===========
	 */
	/**
	 * Build your search context using
	 * the scenarioConnexion Following the deconstructor, it supposed to
	 * be search ready in order to be fully built
	 */
	public static ContexteRecherche construct(ConnectionData cnx, ScenarioConnexion snr) {
		ContexteRecherche valise = new ContexteRecherche();
		valise.setExpressionSearched(snr.getValue("T_EXPRESSION"));
		valise.setSearchInApple(Boolean.parseBoolean(snr.getValue("T_SEARCH_IN_APPLE")));
		valise.setSearchInPear(Boolean.parseBoolean(snr.getValue("T_SEARCH_IN_PEAR")));
		valise.setSearchInNonAnalyzedText(false);
		valise.setSearchInComments(Boolean.parseBoolean(snr.getValue("T_SEARCH_IN_COMMENTS")));
		valise.setCacheInSolution(false);
		valise.setVersion(snr.getValue("T_VER_MODELE"));
		valise.setModele(snr.getValue("T_MODELE"));
		valise.setVersionNat(snr.getValue("T_VER_MODNAT"));
		valise.setModeleNat(snr.getValue("T_MODNAT"));
		valise.setProduit(snr.getValue("T_PRODUIT"));
		valise.setProject(DEFAULT_PROJECT);
		List<String> clientsId = Arrays.asList(snr.getValue("T_LIST_CLIENT").split(",")); // [390170,710210]
		valise.setClientPj(cnx, clientsId);
		valise.setDecodeNeighborText(false);
		
		if (!valise.isReadyForLaunch())
			throw new IllegalArgumentException("Scenario Connexion incomplet !");
		return valise;
	}

	/**
	 * ============= @SETTER ==============
	 */
	/**
	 * Setter if the next research has to search in APPLE By default Enabled
	 * 
	 * @param value - if we do search in APPLE
	 */
	public void setSearchInApple(boolean value) {
		_searchInApple = value;
	}

	/**
	 * Setter if the next research has to search in PEAR By default Disabled
	 * 
	 * @param value - if we do search in PEAR
	 */
	public void setSearchInPear(boolean value) {
		_searchInPear = value;
	}

	/**
	 * Setter if the next research has to search inside the comments By default
	 * Disabled
	 * 
	 * @param value - if we do search in non analyzed text
	 */
	public void setSearchInNonAnalyzedText(boolean value) {
		_searchInNonAnalyzedText = value;
	}

	/**
	 * Setter if the next research has to search inside the text that are not
	 * analyzed By default Disabled
	 * 
	 * @param value - if we do search in comments
	 */
	public void setSearchInComments(boolean value) {
		_searchInComments = value;
	}

	/**
	 * Setter if the next research has to search inside the ParamCotis rubrique
	 * 
	 * @param value - if we do search in paramCotis
	 */
	public void setSearchInParamCotis(boolean value) {
		_searchInParamCotis = value;
	}

	public void setSearchInEveryProject(boolean value) {
		_searchInEveryProjects = value;
	}

	/**
	 * Project's Setter
	 * 
	 * @param project - Project we want to set
	 */
	public void setProject(String project) {
		_project = project;
	}

	/**
	 * Version's Setter - Faculative
	 * 
	 * @param version - Version we want to set
	 */
	public void setVersion(String version) {
		_version = version;
	}

	/**
	 * Modele's Setter - Facultative
	 * 
	 * @param modele - Modele
	 */
	public void setModele(String modele) {
		_modele = modele;
	}

	/**
	 * Version Nat's Setter - MANDATORY
	 * 
	 * @param versionNat - Version National
	 */
	public void setVersionNat(String versionNat) {
		_versionNat = versionNat;
	}

	/**
	 * Modele Nat's Setter - MANDATORY
	 * 
	 * @param modeleNat - Model National
	 */
	public void setModeleNat(String modeleNat) {
		_modeleNat = modeleNat;
	}

	/**
	 * Produit's Setter
	 * 
	 * @param produit
	 */
	public void setProduit(String produit) {
		_produit = produit;
	}

	/**
	 * Setter in order to tell if we want to save the cache in the solution in order
	 * to use it again in a close following decoder
	 * 
	 * @param saveIt if we want to save the cache in the solution returned at the
	 *               end
	 */
	public void setCacheInSolution(boolean saveIt) {
		_saveCacheInSolution = saveIt;
	}

	/**
	 * Setter of the expression searched
	 * 
	 * @param expression
	 */
	public void setExpressionSearched(String expression) {
		_expressionSearched = expression.toUpperCase();
	}

	/**
	 * Setter of the clients we're searching in
	 * 
	 * @param clients - List of clients
	 */
	public void setInputClients(List<ClientProjet> clients) {
		if (clients == null || clients.isEmpty())
			throw new IllegalArgumentException();
		_InputClients = clients;
	}

	public void setClientsWithSolution(List<ClientProjet> clients) {
		if (clients == null || clients.isEmpty())
			throw new IllegalArgumentException();
		_ClientsWithSolution = clients;
	}

	public void addClientToClientsSolution(ClientProjet client) {
		_ClientsWithSolution.add(client);
	}

	public void addClientToClientsSolution(String clientId, String projetId) {
		_ClientsWithSolution.add(new ClientProjet(clientId, projetId));
	}

	public void addClientToInputClients(String clientId, String projetId) {
		ClientProjet client = new ClientProjet(clientId, projetId);
		_InputClients.add(client);
	}

	/**
	 * ============= @GETTER ==============
	 */
	/**
	 * Getter if the next research has to search in APPLE
	 * 
	 * @return the boolean corresponding to the search in APPLE
	 */
	public boolean getSearchInApple() {
		return _searchInApple;
	}

	/**
	 * Getter if the next research has to search in PEAR
	 * 
	 * @return the boolean corresponding to the search in PEAR
	 */
	public boolean getSearchInPear() {
		return _searchInPear;
	}

	/**
	 * Getter if the next research has to search inside the comments
	 * 
	 * @return the boolean corresponding to the search inside comments
	 */
	public boolean getSearchInComments() {
		return _searchInComments;
	}

	public boolean getSearchInEveryProject() {
		return _searchInEveryProjects;
	}

	/**
	 * Getter if the next research has to search inside the text that are not
	 * analyzed (raw)
	 * 
	 * @return the boolean corresponding to the search inside non analyzed texts
	 */
	public boolean getSearchInNonAnalyzedText() {
		return _searchInNonAnalyzedText;
	}

	/**
	 * Getter of the boolean search in ParamCotis. By default setup to Disabled.
	 * 
	 * @return
	 */
	public boolean getSearchInParamCotis() {
		return _searchInParamCotis;
	}

	/**
	 * Project's Getter - By default it's "BC"
	 * 
	 * @return
	 */
	public String getProject() {
		return _project;
	}

	/**
	 * Version's Getter. Can be null
	 * 
	 * @return the version set
	 */
	public String getVersion() {
		return _version;
	}

	/**
	 * Modele's Getter. Can be null
	 * 
	 * @return the model set
	 */
	public String getModele() {
		return _modele;
	}

	/**
	 * Version Nat's Getter
	 * 
	 * @return the version national set
	 */
	public String getVersionNat() {
		return _versionNat;
	}

	/**
	 * Modele Nat's Getter
	 * 
	 * @return the model national set
	 */
	public String getModeleNat() {
		return _modeleNat;
	}

	/**
	 * Model Getter
	 * 
	 * @return model prioritizing on the simple model
	 */
	public String getMainModel() {
		if (this._modele != null)
			return this._modele;
		return this._modeleNat;
	}

	/**
	 * Version Getter
	 * 
	 * @return model prioritizing on the simple model
	 */
	public String getMainVersion() {
		if (this._version != null)
			return this._version;
		return this._versionNat;
	}

	/**
	 * Produit's Getter
	 * 
	 * @return the produit set
	 */
	public String getProduit() {
		return _produit;
	}

	/**
	 * Boolean cache save in solution getter
	 * 
	 * @return the boolean corresponding to if we save the cache in result
	 */
	public boolean getSaveCacheInSolution() {
		return _saveCacheInSolution;
	}

	/**
	 * Getter of the raw expression searched
	 * 
	 * @return the raw expression searched
	 */
	public String getRawExpressionSearched() {
		return _expressionSearched;
	}

	/**
	 * Getter of the raffined expression searched
	 * 
	 * @return the raffined expression searched
	 */
	public String getExpressionRaffined() {
		return _expressionRaffinee;
	}
	
	public boolean getDecodeNeighborText() {
		return _decodeNeighborTexts;
	}
	
	public void setDecodeNeighborText(boolean value) {
		_decodeNeighborTexts = value;
	}

	/**
	 * Getter of the filtered clients
	 * 
	 * @return the list of all the clients ids
	 */
	public List<ClientProjet> getClientsWithSolution() {
		return _ClientsWithSolution;
	}

	public List<ClientProjet> getClientsInput() {
		return _InputClients;
	}

	/**
	 * Getter of the first client
	 * 
	 * @return the first client of the list
	 */
	public ClientProjet getFirstClientProjet() {
		if (!_ClientsWithSolution.isEmpty()) {
			return _ClientsWithSolution.getFirst();
		} else if (!_InputClients.isEmpty()) {
			return _InputClients.getFirst();
		}
		return null;
	}

	public String getFirstClientId() {
		if (!_ClientsWithSolution.isEmpty()) {
			return _ClientsWithSolution.get(0).getClientId();
		} else if (!_InputClients.isEmpty()) {
			return _InputClients.getFirst().getClientId();
		}
		return null;
	}

	public String getFirstProjetId() {
		if (!_ClientsWithSolution.isEmpty()) {
			return _ClientsWithSolution.getFirst().getProjetId();
		} else if (!_InputClients.isEmpty()) {
			return _InputClients.getFirst().getProjetId();
		}
		return null;
	}

	public String getFirstClientIdFromOutput() {
		if (!_ClientsWithSolution.isEmpty()) {
			return _ClientsWithSolution.getFirst().getClientId();
		}
		return null;
	}

	public String getFirstProjetIdFromOutput() {
		if (!_ClientsWithSolution.isEmpty()) {
			return _ClientsWithSolution.getFirst().getProjetId();
		}
		return null;
	}

	public boolean hasOnlyBc() {
		if (getClientsWithSolutionSize() == 0)
			return false;
		for (int i = 0; i < getClientsWithSolutionSize(); i++) {
			if (!_ClientsWithSolution.get(i)._projetId.equals(DEFAULT_PROJECT)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Getter of the client size
	 * 
	 * @return the client's array size
	 */
	public int getClientsWithSolutionSize() {
		return getClientsWithSolution().size();
	}

	public int getClientInputSize() {
		return getClientsInput().size();
	}

	/**
	 * @return if the given expression has joker in it
	 */
	public boolean expressionHasJoker() {
		if (_expressionSearched == null)
			return false;
		return _expressionSearched.contains(getStringLimitedJoker())
				|| _expressionSearched.contains(getStringUnlimitedJoker());
	}

	/**
	 * Getter of the limited joker char
	 * 
	 * @return the character of the limited Joker
	 */
	public static char getCharLimitedJoker() {
		return LIMITED_JOKER_CHAR.charAt(0);
	}

	/**
	 * Getter of the limited joker String
	 * 
	 * @return the String of the limited Joker
	 */
	public static String getStringLimitedJoker() {
		return LIMITED_JOKER_CHAR.toString();
	}

	/**
	 * Getter of the unlimited joker char
	 * 
	 * @return the character of the unlimited Joker
	 */
	public static char getCharUnlimitedJoker() {
		return UNLIMITED_JOKER_CHAR.charAt(0);
	}

	/**
	 * Getter of the unlimited joker String
	 * 
	 * @return the String of the unlimited Joker
	 */
	public static String getStringUnlimitedJoker() {
		return UNLIMITED_JOKER_CHAR.toString();
	}

    /**
	 * Check if the current builder contains valid arguments to launch a APPLE
	 * research
	 * 
	 * @return if the current context is valid to start a search in APPLE
	 */
	public boolean isValidForApple() {
		if (_modeleNat == null || _versionNat == null)
			return false;
		if (_project == null)
			return false;
		return true;
	}

	/**
	 * Check if the current builder contains valid arguments to launch a APPLE
	 * research Includes Exception. **Mainly for debugging**
	 * 
	 * @return if the current context is valid to start a search in APPLE
	 */
	public boolean isValidForAppleWithExceptions() {
		if (_modeleNat == null || _versionNat == null)
			throw new IllegalArgumentException(
					"No modelNat or versionNat  entered. Please add them all for the APPLE research");
		if (_project == null)
			throw new IllegalArgumentException("No project entered. Please add it for the APPLE research");
		return true;

	}

	public static boolean isWordOnlyJokers(String str) {
		boolean containsOnlyJoker = true;
		for (char c : str.toCharArray()) {
			if (c != UNLIMITED_JOKER_CHAR.charAt(0) || c != LIMITED_JOKER_CHAR.charAt(0)) {
				containsOnlyJoker = false;
				break;
			}
		}
		return containsOnlyJoker;
	}

	/**
	 * Check if the current builder contains valid arguments to launch a PEAR
	 * research
	 * 
	 * @return if the current context is valid to start a search in PEAR
	 */
	public boolean isValidForPear() {
		if (_modeleNat == null || _versionNat == null || _modele == null || _version == null)
			return false;

		if (getClientsInput().isEmpty())
			return false;

		if (getClientsInput().size() == 1 && _project == null)
			return false;
		if (getClientsInput().size() > 1 && _project != "BC") {
			_project = DEFAULT_PROJECT;
		}
		return true;
	}

	/**
	 * Check if the current builder contains valid arguments to launch a PEAR
	 * research Includes Exception. **Mainly for debugging**
	 * 
	 * @return if the current context is valid to start a search in PEAR
	 */
	public boolean isValidForPearWithExceptions() {
		if (_modeleNat == null || _versionNat == null || _modele == null || _version == null)
			throw new IllegalArgumentException(
					"No model or version or modeleNat or versionNat entered. Please add them all for the PEAR research");
		if (getClientsWithSolutionSize() == 0)
			throw new IllegalArgumentException(
					"You must give at least one client in order to launch the PEAR research !");
		if (getClientsWithSolutionSize() == 1 && _project == null)
			throw new IllegalArgumentException(
					"You must give a project in order to launch the PEAR research for only one client !");
		if (getClientsWithSolutionSize() > 1 && !Objects.equals(_project, "BC")) {
			_project = DEFAULT_PROJECT;
		}
		return true;
	}
	
	/**
	 * Return null if there is nothing to do with the current expression, 
	 * Return the same input string if there are no problems
	 * Return a different string if the needed corrections could be used
	 * @param str
	 * @return FeedBackExpression : Code > 500 (Erreur) [501: Que des jokers, 502: Un mot contenant des joker simultané l'un à côté de l'autre] Code 400: (Valide) Code 401: (Valide mais corrigé)
	 */
	public static FeedbackExpression isExpressionValid(String str) {
		ContexteRecherche ctx = new ContexteRecherche();
		String compressedExpression = str.replaceAll("~{2,}", "~");
		if (compressedExpression.contentEquals(UNLIMITED_JOKER_CHAR) || compressedExpression.contentEquals(LIMITED_JOKER_CHAR)) return new FeedbackExpression(501, null);
		
		String[] words = compressedExpression.split(" ");
		String fixedExpression;
		Pattern fullPattern = Pattern.compile("[^~^]");
		if (words.length > 1) {
			// Remove any trailing unlimited joker
			if (words[0].contentEquals(UNLIMITED_JOKER_CHAR)) words = Arrays.copyOfRange(words, 1, words.length);
			if (words[words.length - 1].contentEquals(UNLIMITED_JOKER_CHAR)) words = Arrays.copyOfRange(words, 0, words.length - 1);
			
			fixedExpression = String.join(" ", words);
			
			if (str.trim().length() != fixedExpression.trim().length()) return new FeedbackExpression(401, fixedExpression);
		} else if (!fullPattern.matcher(str).find()) return new FeedbackExpression(502, null);
		
		return new FeedbackExpression(400, str);
	}

	public void setClientsWithBcPj(ConnectionData cnx) {
		if (this.getMainModel() == null)
			throw new IllegalArgumentException("You must set a modele before getting all the client from a model !");

		boolean hasToBeClosed = false;
		try {
			hasToBeClosed = cnx.openDbAccess();

            String query = "SELECT clientid FROM PEAR_CLIENT WHERE modeleid = '"
                    + this.getMainModel() + "' AND versionid = '001'";
			ResultSet result = cnx.getDbAccess().executeSelect(query);

			while (result.next()) {
				this.addClientToInputClients(result.getString(1), DEFAULT_PROJECT);
			}
		} catch (SQLException e) {
			Logger.error("Error while getting all the clients from a model", e);
		} finally {
			if (hasToBeClosed)
				cnx.closeDbAccess();
		}
	}

	public void setClientPj(ConnectionData cnx) {
		setClientPj(cnx, null);
	}

	public void setClientPj(ConnectionData cnx, List<String> clientsId) {
		if (this.getMainModel() == null)
			throw new IllegalArgumentException("You must set a modele before getting all the client from a model !");

		boolean hasToBeClosed = false;
		try {
			hasToBeClosed = cnx.openDbAccess();

			StringBuilder query = new StringBuilder("SELECT clients.clientid, projets.projetid FROM (");
			query.append(" SELECT clientid FROM PEAR_CLIENT WHERE modeleid = '").append(this.getMainModel()).append("' AND versionid = '001' AND clientid IN (");
			query.append(" SELECT clientid FROM PEAR_INFO_PROD_PAC )");
			query.append(" ) clients, PEAR_PROJET projets");
			query.append(" WHERE clients.clientid = projets.clientid AND projets.statut IN ('O', 'T')");
			if (clientsId != null) {
				String clientsIdSql = clientsId.stream().map(clientId -> "'" + clientId + "'")
						.collect(Collectors.joining(", "));

				query.append(" AND clients.clientid IN (").append(clientsIdSql).append(")");
			}

			query.append(" ORDER BY clientid, projetid");

			ResultSet result = cnx.getDbAccess().executeSelect(query.toString());

			HashSet<String> clients = new HashSet<String>();

			while (result.next()) {
				String client = result.getString(1);
				if (!clients.contains(client)) {
					this.addClientToInputClients(client, DEFAULT_PROJECT);
					clients.add(client);
				}
				this.addClientToInputClients(client, result.getString(2));

			}
		} catch (SQLException e) {
			Logger.error("Error while getting all the clients from a model", e);
		} finally {
			if (hasToBeClosed)
				cnx.closeDbAccess();
		}
	}

	public List<String> getAllProjectFromClientWithSolution(String client) {
		if (this.getClientsWithSolution().isEmpty())
			throw new IllegalArgumentException("You must set up all the clients and projects before accessing them");

		List<String> projets = new ArrayList<>();
		for (ClientProjet clientProjet : this.getClientsWithSolution()) {
			if (client.equals(clientProjet.getClientId())) {
				projets.add(clientProjet.getProjetId());
			}
		}

		return projets;
	}

	public HashSet<String> getAllUniqueInputClient() {
		HashSet<String> clients = new HashSet<>();
		for (ClientProjet clientProjet : this.getClientsInput()) {
			clients.add(clientProjet.getClientId());
		}

		return clients;
	}

	public List<ClientProjet> getAllInterstingClientProjet() {
		HashSet<String> clientProjetId = new HashSet<>();
		List<ClientProjet> interstingClients = getClientsWithSolution();

		for (ClientProjet clientProjet : interstingClients) {
			clientProjetId.add(clientProjet.id());
		}

		HashSet<String> uniqueInputClient = getAllUniqueInputClient();
		for (String clientId : uniqueInputClient) {
			ClientProjet newClientProjet = new ClientProjet(clientId, DEFAULT_PROJECT);
			if (!clientProjetId.contains(newClientProjet.id())) {
				interstingClients.add(newClientProjet);
				clientProjetId.add(newClientProjet.id());
			}
		}
		
		return interstingClients;
	}

	public HashSet<String> getAllUniqueClientWithSolution() {
		HashSet<String> clients = new HashSet<>();
		for (ClientProjet clientProjet : this.getClientsWithSolution()) {
			clients.add(clientProjet.getClientId());
		}

		return clients;
	}

	public int getUniqueClientWithSolutionCount() {
		return getAllUniqueClientWithSolution().size();
	}
	
	@Override
	public String toString() {
		String str = "[Expression Recherchée :" + this._expressionSearched + "], [inApple/inPear : " + this._searchInApple + this._searchInPear + "], [searchInComm : " + this._searchInComments + "], [modele/version : " + this._modele + "/" + this._version + "], [modeleNat/versionNat : " + this._modeleNat + "/" + this._versionNat + "]";
		if (this.getClientInputSize() > 0) str += ", [" + this.getClientsInput().stream().map(ClientProjet::toString).collect(Collectors.joining(", ")) + "]";
		return str;
	}

	public static class ClientProjet implements Serializable {
		@Serial
		private static final long serialVersionUID = 7719080479493169181L;

		private final String _clientId;
		private final String _projetId;

		public ClientProjet(String clientId, String projetId) {
			this._clientId = clientId;
			this._projetId = projetId;
		}

		public String getClientId() {
			return _clientId;
		}

		public String getProjetId() {
			return _projetId;
		}

		@Override
		public String toString() {
			return "ClientProjet : [" + _clientId + ", " + _projetId + "]";
		}

		public String id() {
			return _clientId + "#" + _projetId;
		}
	}
	
	public static class FeedbackExpression {
		public int code; // 400, 500
		public String msg;
		
		public FeedbackExpression(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}
		
		public MsgId getMsg() {
            return switch (code) {
                case 401 -> new MsgId("MSG_250_6199");
                case 501 -> new MsgId("MSG_250_6200");
                case 502 -> new MsgId("MSG_250_6201");
                default -> null;
            };
		}
	}
}

record MsgId(String id) {


}
