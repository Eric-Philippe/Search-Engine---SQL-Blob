package apple.util.rechercherExpression.solutionRecherche;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import apple.util.rechercherExpression.Recherche;
import apple.util.rechercherExpression.RegleResultat;
import apple.util.rechercherExpression.apple.RechercherExpressionAppleStrict;

/**
 * Resultat Unitaire de la {@link Recherche}
 * 
 * <p>
 * Classe plan d'une solution de recherche contenant ses identifiants de telle
 * sorte : {@link RechercherExpressionAppleStrict}
 * 
 * <pre>
 * REGLE_ID
 * </pre>
 * 
 * {@link RechercherExpressionPearStrict}
 * 
 * <pre>
 * CLIENT_ID / PROJET_ID / REGLE_ID
 * </pre>
 * 
 * <p>
 * L'objet stock �galement o� se trouve sa solution � l'aide des indexs d�clar�s
 * dans {@link PairSolution} trouv�s � l'aide des Regex (Voir :
 * {@link ExpressionMatcher})
 * 
 * <p>
 * 
 * <author> Eric PHILIPPE
 */
public class ResultatRecherche implements Serializable {
	private static final long serialVersionUID = -249152652855641912L;

	/** Identifiant de r�gle o� l'on a trouv� une solution */
	private String regleId = ""; // Z4_
	
	private String modeleId = "";
	private String versionId = "";
	/** Client li� � la r�gle si la r�gle est propre � PEAR */
	private String clientId = null;
	/** Projet li� � la r�gle si la r�gle est propre � PEAR */
	private String projetId = null;
	/** Contenu d�cod� de la r�gle */
	private String decodedText = "";
	/** Si le moy est calcul� (C) ou bien Utilis� (U) ; par d�faut "*" */
	private String utilisation = "*";
	private boolean isFromApple = true;
	/** Liste des indexs o� se trouve la et les solutions dans le texte */
	private List<PairSolution> solutionsIndexees = new ArrayList<>();


	/**
	 * Instancie un nouveau R�sultat Recherche
	 * 
	 * @param regleId
	 * @param decodedText
	 * @param solutionsIndexees
	 */
	public ResultatRecherche(RegleResultat regleRes, String decodedText, List<PairSolution> solutionsIndexees) {
		this.regleId = regleRes.getRegleId();
		this.modeleId = regleRes.getModelId();
		this.versionId = regleRes.getVersionId();
		this.projetId = regleRes.getProjetId();
		this.clientId = regleRes.getClientId();
		this.isFromApple = regleRes.isFromApple();
		this.decodedText = decodedText;
		this.solutionsIndexees = solutionsIndexees;
	}

	/**
	 * Instancie un nouveau R�sultat Recherche en pr�cisant le SelectRegle
	 * 
	 * @param regleId
	 * @param decodedText
	 * @param selectRegle
	 */
	public ResultatRecherche(RegleResultat regleRes, String decodedText) {
		this.regleId = regleRes.getRegleId();
		this.modeleId = regleRes.getModelId();
		this.versionId = regleRes.getVersionId();
		this.projetId = regleRes.getProjetId();
		this.clientId = regleRes.getClientId();
		this.decodedText = decodedText;
		if (regleId == null)
			this.regleId = "";
	}

	/**
	 * Permet de d�composer l'identifiant sous chacuns des param�tres
	 */
//	private void initRegleClientId() {
//		String currentRegleId = this.getRegleId();
//		if (currentRegleId.contains("#")) {
//			String[] splitedInput = currentRegleId.split("#");
//			this.setRegleId(splitedInput[2]);
//			this.setClientId(splitedInput[0]);
//			this.setProjetId(splitedInput[1]);
//
//		}
//	}

	/**
	 * RegleId Setter
	 * 
	 * @param regleId
	 */
	public void setRegleId(String regleId) {
		this.regleId = regleId;
	}

	/**
	 * ClientId Setter
	 * 
	 * @param clientId
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Projet Id Setter
	 * 
	 * @param projetId
	 */
	public void setProjetId(String projetId) {
		this.projetId = projetId;
	}

	/**
	 * Decoded Text Setter
	 * 
	 * @param decodedText
	 */
	void setTexte(String decodedText) {
		this.decodedText = decodedText;
	}

	/**
	 * Utilisation Setter
	 * 
	 * @param utilisation
	 */
	void setUtilisation(String utilisation) {
		this.utilisation = utilisation;
	}

	/**
	 * RegleId Getter
	 * 
	 * @return
	 */
	public String getRegleId() {
		return this.regleId;
	}
	
	public String getModeleId() {
		return this.modeleId;
	}
	
	public String getVersionId() {
		return this.versionId;
	}

	/**
	 * ClientId Getter
	 * 
	 * @return
	 */
	public String getClientId() {
		return this.clientId;
	}

	/**
	 * ProjetId Getter
	 * 
	 * @return
	 */
	public String getProjetId() {
		return this.projetId;
	}

	/**
	 * Texte Getter
	 * 
	 * @return
	 */
	public String getTexte() {
		if (this.decodedText != null) return this.decodedText;
		else return "";
	}

	/**
	 * Utilisation Getter
	 * 
	 * @return
	 */
	public String getUtilisation() {
		return this.utilisation;
	}

	/**
	 * PairSolution Getter
	 * 
	 * @return
	 */
	public List<PairSolution> getSolutionsInText() {
		return this.solutionsIndexees;
	}

	/**
	 * If the solution is from APPLE
	 * 
	 * @return
	 */
	public boolean isFromApple() {
		return this.isFromApple;
	}

	/**
	 * If the solution is from PEAR
	 * 
	 * @return
	 */
	public boolean isFromPear() {
		return !this.isFromApple;
	}

	public boolean isParamCotis() {
		return this.regleId.charAt(3) == 'P';
	}
	
	/**
	 * If the result contains solution
	 * 
	 * @return
	 */
	public boolean containsSolution() {
		return this.solutionsIndexees.size() > 0;
	}
}