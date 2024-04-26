package apple.util.rechercherExpression.solutionRecherche;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static apple.util.rechercherExpression.solutionRecherche.RubriqueResultat.RegleTypeEnum.*;

/**
 * Classe étendant implicitement la classe {@link ResultatRecherche}
 * 
 * <p>
 * Le résultat est ici non plus à l'échelle de la règle comme
 * {@link ResultatRecherche} mais de la rubrique (identifiée par {@link #key}
 * qui se base sur l'objetId)
 * 
 * <p>
 * Une rubrique peut contenir de 1 à 5 règles chacune dans des sous-rubriques
 * particulières (Filtre, Calcul, ParamCotis, Simulation, Destination)
 * 
 * @author Eric PHILIPPE
 *
 */
public class RubriqueResultat implements Serializable {
	private static final long serialVersionUID = 8188433411196756092L;

	public static final int RUB_FILTRE = 0;
	public static final int RUB_CALCUL = 1;
	public static final int RUB_PARAMCOTIS = 2;
	public static final int RUB_SIMULATION = 3;
	public static final int RUB_DESTINATION = 4;

	public static enum RegleTypeEnum {
		SOLUTION, NEIGHBOR, UNREADABLE, NONE
	}

	/** Identifiant principal pour une règle unitaire */
	private String key = null;
	/** Voir {@link ORIGIN} (APPLE|PEAR) */
	private ORIGIN origin = null;

	/**
	 * Eviter de tomber dans le polymorphisme, les propriétées sont alors dynamiques
	 * pour économiser des ressources
	 */
	private HashMap<ResultatRechercheProperties, String> properties = new HashMap<ResultatRechercheProperties, String>();
	/**
	 * Témoin renseignant quelle sous-rubriques sont présentes dans la rubrique
	 * principale Respectivement correspondant à Filtre, Calcul, ParamCotis,
	 * Simulation, Destination
	 */
	private RegleTypeEnum[] containsRub = { NONE, NONE, NONE, NONE, NONE };

	/**
	 * Instancie un ResultatRechercheEtendu vide pour APPLE
	 * 
	 * @param objetId
	 * @param libelle
	 * @param noordre
	 * @param retro
	 * @param cscp
	 */
	public RubriqueResultat(String objetId, String libelle, int noordre, String retro, int cscp) {
		this.setCommonProperties(objetId, libelle, noordre, retro, cscp);
	}

	/**
	 * Instancie un ResultatRechercheEtendu pour APPLE en renseignant une règleId
	 * appartenant à la rubrique mère
	 * 
	 * @param objetId
	 * @param libelle
	 * @param noordre
	 * @param retro
	 * @param cscp
	 * @param regleId
	 * @param phase
	 * @param inPhase
	 */
	public RubriqueResultat(String objetId, String libelle, int noordre, String retro, int cscp, String regleId,
			char phase, boolean inPhase, String selectRegle) {
		this.setCommonProperties(objetId, libelle, noordre, retro, cscp);
		this.setRubrique(regleId, phase, getRegleTypeEnum(regleId, selectRegle, inPhase));
		this.key = SolutionTool.toId(objetId);
		this.setOrigin(ORIGIN.APPLE);
	}

	/**
	 * Instancie un ResultatRechercheEtendu pour PEAR en renseignant une règleId
	 * appartenant à la rubrique mère
	 * 
	 * @param clientId
	 * @param projetId
	 * @param objetId
	 * @param libelle
	 * @param noordre
	 * @param retro
	 * @param cscp
	 * @param regleId
	 * @param phase
	 * @param inPhase
	 */
	public RubriqueResultat(String clientId, String projetId, String objetId, String libelle, int noordre,
			String retro, int cscp, String regleId, char phase, boolean inPhase, String selectRegle) {
		this.setCommonProperties(objetId, libelle, noordre, retro, cscp);
		this.setProperties(ResultatRechercheProperties.PEAR_CLIENT, clientId);
		this.setProperties(ResultatRechercheProperties.PEAR_PROJET, projetId);
		this.key = SolutionTool.toId(clientId, projetId, objetId);
		this.setOrigin(ORIGIN.PEAR);

		this.setRubrique(regleId, phase, getRegleTypeEnum(regleId, selectRegle, inPhase));
	}
	
	public RubriqueResultat(String clientId, String libelle) {
		this.setProperties(ResultatRechercheProperties.PEAR_CLIENT, clientId);
		this.setProperties(ResultatRechercheProperties.COMMON_LIBELLE, libelle);
		this.setOrigin(ORIGIN.PEAR);
	}

	/**
	 * Méthode commune pour pouvoir stocker les propriétés communes
	 * 
	 * @param objetId
	 * @param libelle
	 * @param noordre
	 * @param retro
	 * @param cscp
	 */
	private void setCommonProperties(String objetId, String libelle, int noordre, String retro, int cscp) {
		this.setProperties(ResultatRechercheProperties.COMMON_OBJETID, objetId);
		this.setProperties(ResultatRechercheProperties.COMMON_LIBELLE, libelle);
		this.setProperties(ResultatRechercheProperties.COMMON_INT_NOORDRE, noordre);
		this.setProperties(ResultatRechercheProperties.COMMON_RETRO, retro);
		this.setProperties(ResultatRechercheProperties.COMMON_BOOL_CSCP, cscp == 0 ? false : true);
		this.setProperties(ResultatRechercheProperties.COMMON_CODE, objetId.substring(objetId.length() - 4));
	}

	/**
	 * ResultatRechercheEtendu clone
	 * 
	 * @param properties
	 * @param inC
	 * @param inD
	 * @param inF
	 * @param key
	 * @param origin
	 */
	private RubriqueResultat(HashMap<ResultatRechercheProperties, String> properties, RegleTypeEnum[] rubs,
			String key, ORIGIN origin) {
		this.properties = properties;
		this.containsRub = rubs;
		this.key = key;
		this.origin = origin;
	}

	/**
	 * Clone Method
	 */
	public RubriqueResultat clone() {
		return new RubriqueResultat(properties, this.containsRub, key, origin);
	}

	private static RegleTypeEnum getRegleTypeEnum(String regleId, String selectRegle, boolean inSolution) {
		RegleTypeEnum regleType = NEIGHBOR;
		if (selectRegle == null)
			return NEIGHBOR;
		if (inSolution)
			regleType = SOLUTION;
		if ((selectRegle.charAt(0) == 'M') || (selectRegle.charAt(0) == ' ' && regleId == null))
			regleType = UNREADABLE;

		return regleType;
	}

	protected RegleTypeEnum[] getContainsRub() {
		return this.containsRub;
	}

	/**
	 * Rubrique Setter
	 * 
	 * @param regleId
	 * @param phase
	 * @param inPhase
	 */
	public void setRubrique(String regleId, char phase, RegleTypeEnum type) {
		switch (phase) {
		case 'F':
			this.setProperties(ResultatRechercheProperties.RUB_FILTRE, regleId);
			this.containsRub[RUB_FILTRE] = type;
			break;
		case 'C':
			this.setProperties(ResultatRechercheProperties.RUB_CALCUL, regleId);
			this.containsRub[RUB_CALCUL] = type;
			break;
		case 'P':
			this.setProperties(ResultatRechercheProperties.RUB_PARAMCOTIS, regleId);
			this.containsRub[RUB_PARAMCOTIS] = type;
			break;
		case 'S':
			this.setProperties(ResultatRechercheProperties.RUB_SIMULATION, regleId);
			this.containsRub[RUB_SIMULATION] = type;
			break;
		case 'D':
			this.setProperties(ResultatRechercheProperties.RUB_DESTINATION, regleId);
			this.containsRub[RUB_DESTINATION] = type;
			break;
		}
	}

	/**
	 * RegleId Getter à partir de la phase
	 * 
	 * @param phase
	 * @return
	 */
	public String getRegleIdFromPhase(char phase) {
		switch (phase) {
		case 'F':
			return this.getProperties(ResultatRechercheProperties.RUB_FILTRE);
		case 'C':
			return this.getProperties(ResultatRechercheProperties.RUB_CALCUL);
		case 'P':
			return this.getProperties(ResultatRechercheProperties.RUB_PARAMCOTIS);
		case 'S':
			return this.getProperties(ResultatRechercheProperties.RUB_SIMULATION);
		case 'D':
			return this.getProperties(ResultatRechercheProperties.RUB_DESTINATION);

		default:
			return "";

		}
	}

	public RegleTypeEnum getTypeFromPhase(char phase) {
		switch (phase) {
		case 'F':
			return this.containsRub[RUB_FILTRE];
		case 'C':
			return this.containsRub[RUB_CALCUL];
		case 'P':
			return this.containsRub[RUB_PARAMCOTIS];
		case 'S':
			return this.containsRub[RUB_SIMULATION];
		case 'D':
			return this.containsRub[RUB_DESTINATION];

		default:
			return NONE;

		}
	}

	/**
	 * Retourne la première regleId trouvée dans la rubrique parente
	 * 
	 * @return
	 */
	public String getFirstRegleIdFound() {
		if (this.containsRub[RUB_FILTRE] != NONE)
			return this.getProperties(ResultatRechercheProperties.RUB_FILTRE);
		else if (this.containsRub[RUB_CALCUL] != NONE)
			return this.getProperties(ResultatRechercheProperties.RUB_CALCUL);
		else if (this.containsRub[RUB_PARAMCOTIS] != NONE)
			return this.getProperties(ResultatRechercheProperties.RUB_PARAMCOTIS);
		else if (this.containsRub[RUB_SIMULATION] != NONE)
			return this.getProperties(ResultatRechercheProperties.RUB_SIMULATION);
		else if (this.containsRub[RUB_DESTINATION] != NONE)
			return this.getProperties(ResultatRechercheProperties.RUB_DESTINATION);

		return null;
	}

	/**
	 * Retourne la première phase trouvée dans la rubrique parente
	 * 
	 * @return
	 */
	public char getFirstPhaseFound() {
		if (this.containsRub[RUB_FILTRE] != NONE)
			return 'F';
		else if (this.containsRub[RUB_CALCUL] != NONE)
			return 'C';
		else if (this.containsRub[RUB_PARAMCOTIS] != NONE)
			return 'P';
		else if (this.containsRub[RUB_SIMULATION] != NONE)
			return 'S';
		else if (this.containsRub[RUB_DESTINATION] != NONE)
			return 'D';

		return 'F';
	}

	public int getFirstSolution() {
		if (this.containsRub[RUB_FILTRE] == SOLUTION)
			return RUB_FILTRE;
		else if (this.containsRub[RUB_CALCUL] == SOLUTION)
			return RUB_CALCUL;
		else if (this.containsRub[RUB_PARAMCOTIS] == SOLUTION)
			return RUB_PARAMCOTIS;
		else if (this.containsRub[RUB_SIMULATION] == SOLUTION)
			return RUB_SIMULATION;
		else if (this.containsRub[RUB_DESTINATION] == SOLUTION)
			return RUB_DESTINATION;

		return 0;
	}

	/**
	 * Retourne la première phase trouvée dans la rubrique parente
	 * 
	 * @return
	 */
	public RegleTypeEnum getFirstTypeFound() {
		if (this.containsRub[RUB_FILTRE] != NONE)
			return this.containsRub[RUB_FILTRE];
		else if (this.containsRub[RUB_CALCUL] != NONE)
			return this.containsRub[RUB_CALCUL];
		else if (this.containsRub[RUB_PARAMCOTIS] != NONE)
			return this.containsRub[RUB_PARAMCOTIS];
		else if (this.containsRub[RUB_SIMULATION] != NONE)
			return this.containsRub[RUB_SIMULATION];
		else if (this.containsRub[RUB_DESTINATION] != NONE)
			return this.containsRub[RUB_DESTINATION];

		return NONE;
	}

	public List<Integer> getRubHasNone() {
		List<Integer> values = new ArrayList<>();
		int i = 0;
		for (RegleTypeEnum type : this.containsRub) {
			if (type == RegleTypeEnum.NONE) {
				values.add(i);
			}
			i++;
		}

		return values;
	}
	
	/**
	 * Return all the rub that contains none in the specific order given in inValues
	 * @param inValues
	 * @return
	 */
	public List<Integer> getRubHasNoneIn(List<Integer> inValues) {
		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < inValues.size(); i++) {
			if (this.containsRub[inValues.get(i)] == RegleTypeEnum.NONE) {
				values.add(i);
			}
		}

		return values;
	}

	public List<String> getAllReglesId() {
		List<String> result = new ArrayList<>();

		if (this.containsRub[RUB_FILTRE] != NONE)
			result.add(getRegleKey(this.getProperties(ResultatRechercheProperties.RUB_FILTRE)));
		if (this.containsRub[RUB_CALCUL] != NONE)
			result.add(getRegleKey(this.getProperties(ResultatRechercheProperties.RUB_CALCUL)));
		if (this.containsRub[RUB_PARAMCOTIS] != NONE)
			result.add(getRegleKey(this.getProperties(ResultatRechercheProperties.RUB_PARAMCOTIS)));
		if (this.containsRub[RUB_SIMULATION] != NONE)
			result.add(getRegleKey(this.getProperties(ResultatRechercheProperties.RUB_SIMULATION)));
		if (this.containsRub[RUB_DESTINATION] != NONE)
			result.add(getRegleKey(this.getProperties(ResultatRechercheProperties.RUB_DESTINATION)));

		return result;
	}

	private String getRegleKey(String regleId) {
		if (regleId.contains("Z8")) {
			String clientId = this.getProperties(ResultatRechercheProperties.PEAR_CLIENT);
			String projetId = this.getProperties(ResultatRechercheProperties.PEAR_PROJET);

			return clientId + "#" + projetId + "#" + regleId;
		} else
			return regleId;
	}
	
	public String getClientId() {
		String clientId = this.getProperties(ResultatRechercheProperties.PEAR_CLIENT);
		if (clientId.equals("")) return null;
		return clientId;
	}
	
	public String getProjetId() {
		String projetId = this.getProperties(ResultatRechercheProperties.PEAR_PROJET);
		if (projetId.equals("")) return null;
		return projetId;
	}

	/**
	 * Retourne l'identifiant du résultat
	 * 
	 * @return
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Origin Setter
	 * 
	 * @param origin
	 */
	protected void setOrigin(ORIGIN origin) {
		this.origin = origin;
	}

	/**
	 * Origin Getter
	 * 
	 * @return
	 */
	public ORIGIN getOrigin() {
		return this.origin;
	}

	/**
	 * PROPERTIES MANAGER
	 * 
	 * <p>
	 * Permet de stocker dynamiquement les propriétés dans l'objet en faisant
	 * profiter du polymorphisme en restant propre
	 * 
	 * <p>
	 * Voir {@link ResultatRechercheProperties}
	 */

	/**
	 * MAIN SETTER [String Version]
	 * 
	 * @param prop - Key
	 * @param str  - Value
	 */
	public void setProperties(ResultatRechercheProperties prop, String str) {
		if (str == null)
			return;
		this.properties.put(prop, str);
	}

	/**
	 * SETTER [Boolean Version]
	 * 
	 * @param prop - Key
	 * @param bool - Value
	 */
	public void setProperties(ResultatRechercheProperties prop, boolean bool) {
		String str = bool ? "true" : "false";
		this.setProperties(prop, str);
	}

	/**
	 * SETTER [Int Version]
	 * 
	 * @param prop - Key
	 * @param num  - Value
	 */
	public void setProperties(ResultatRechercheProperties prop, int num) {
		this.setProperties(prop, Integer.toString(num));
	}

	/**
	 * MAIN GETTER
	 * 
	 * @param prop
	 * @return
	 */
	public String getProperties(ResultatRechercheProperties prop) {
		String val = this.properties.get(prop);
		if (val != null)
			return val;
		else
			return "";
	}

	/**
	 * Boolean Getter
	 * 
	 * @param prop
	 * @return
	 */
	public boolean getBooleanProp(ResultatRechercheProperties prop) {
		String res = this.getProperties(prop);
		if (res == null || !res.equals("true"))
			return false;
		else
			return true;
	}

	/**
	 * Int Getter
	 * 
	 * @param prop
	 * @return
	 */
	public int getIntProp(ResultatRechercheProperties prop) {
		String res = this.getProperties(prop);
		if (res == null)
			return -1;
		else
			return Integer.parseInt(res);
	}
	
	public List<String> getAllSolutionPhase() {
		List<String> res = new ArrayList<>();
		if (inF()) res.add("F");
		if (inC()) res.add("C");
		if (inD()) res.add("D");
		if (inS()) res.add("S");
		if (inP()) res.add("P");
		
		return res;
	}

	/**
	 * Retourne le numéro d'ordre
	 * 
	 * @return
	 */
	public int getNoordre() {
		return this.getIntProp(ResultatRechercheProperties.COMMON_INT_NOORDRE);
	}

	/**
	 * Si la solution ne contient aucune règle solution
	 * 
	 * @return
	 */
	public boolean isEmptySolution() {
		return this.containsRub[RUB_FILTRE] == NONE && this.containsRub[RUB_CALCUL] == NONE
				&& this.containsRub[RUB_PARAMCOTIS] == NONE && this.containsRub[RUB_SIMULATION] == NONE
				&& this.containsRub[RUB_DESTINATION] == NONE;
	}
	
	public boolean hasNoSolutions() {
		return this.containsRub[RUB_FILTRE] != SOLUTION && this.containsRub[RUB_CALCUL] != SOLUTION
				&& this.containsRub[RUB_PARAMCOTIS] != SOLUTION && this.containsRub[RUB_SIMULATION] != SOLUTION
				&& this.containsRub[RUB_DESTINATION] != SOLUTION;
	}

	/**
	 * Si la solution provient de PEAR
	 * 
	 * @return
	 */
	public boolean isPear() {
		return this.origin == ORIGIN.PEAR;
	}

	/**
	 * Si la solution provient de APPLE
	 * 
	 * @return
	 */
	public boolean isApple() {
		return this.origin == ORIGIN.APPLE;
	}

	public boolean inF() {
		return this.containsRub[RUB_FILTRE] == SOLUTION;
	}

	public boolean inC() {
		return this.containsRub[RUB_CALCUL] == SOLUTION;
	}

	public boolean inP() {
		return this.containsRub[RUB_PARAMCOTIS] == SOLUTION;
	}

	public boolean inS() {
		return this.containsRub[RUB_SIMULATION] == SOLUTION;
	}

	public boolean inD() {
		return this.containsRub[RUB_DESTINATION] == SOLUTION;
	}

}

enum ORIGIN {
	PEAR, APPLE
}