package apple.util.rechercherExpression.solutionRecherche;

import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.RegleResultat;

import java.io.Serializable;
import java.util.ArrayList;

public class SolutionsContainer implements Serializable {
	private static final long serialVersionUID = -5864129234181499908L;

	/** Container principal */
	public ArrayList<RubriqueResultat> resultatsEtendus = new ArrayList<>();

	private ContexteRecherche ctx;
	protected ResultatsRecherche reglesDecoded;
	
	/**
	 * Créé un nouvel objet solution étendu à partir de {@link ResultatsRecherche}
	 * ResultatsRecherche travail sur le paradigme de règle
	 * Alors que Solutions travail sur celui des rubriques
	 * 
	 * @param ctx - ContexteRecherche
	 * @param resRecherche - ResultatRecherche
	 */
	public SolutionsContainer(ContexteRecherche ctx, ResultatsRecherche resRecherche) {
		this.ctx = ctx;
		this.reglesDecoded = resRecherche;

	}
	
	/**
	 * ================================================================
	 * ##### @MISCELLANEOUS_METHODS (Setter, Getter, Contains...) #####
	 * ================================================================
	 */
	
	/**
	 * Return true if the Solutions contains the given key as rubid
	 * @param key
	 * @return
	 */
	public boolean contains(String key) {
		return this.getResEtendu(key) != null;
	}
	
	/**
	 * Getter à partir d'une rubrique id
	 * @param key
	 * @return
	 */
	public RubriqueResultat getResEtendu(String key) {
		for (RubriqueResultat res : this.resultatsEtendus) {
			if (res.getKey().equals(key))
				return res;
		}

		return null;
	}
	
	/**
	 * Getter à partir d'un code objet id
	 * @param code
	 * @return
	 */
	public RubriqueResultat getResEtenduCode(String code) {
		for (RubriqueResultat res : this.resultatsEtendus) {
			if (res.getProperties(ResultatRechercheProperties.COMMON_CODE).equals(code)) {
				return res;
			}
		}

		return null;
	}
	
	public RubriqueResultat getResEtenduFromObjetId(String objetId) {
		for (RubriqueResultat res : this.resultatsEtendus) {
			if (res.getProperties(ResultatRechercheProperties.COMMON_OBJETID).equals(objetId)) {
				return res;
			}
		}

		return null;
	}
	
	public RubriqueResultat getResEtenduFromObjetId(String clientId, String projetId, String objetId) {
		for (RubriqueResultat res : this.resultatsEtendus) {
			if (res.getProperties(ResultatRechercheProperties.COMMON_OBJETID).equals(objetId) && res.getClientId().equals(clientId) && res.getProjetId().equals(projetId)) {
				return res;
			}
		}

		return null;
	}

	public RubriqueResultat getResEtendu(String... keys) {
		return this.getResEtendu(SolutionTool.toId(keys));
	}

	public RubriqueResultat getResEtendu(int index) {
		if (this.resultatsEtendus == null || this.resultatsEtendus.size() == 0)
			return null;
		return this.resultatsEtendus.get(index);
	}

	public ResultatRecherche getRegleDecodee(String... keys) {
		if (keys.length == 1)
			return this.reglesDecoded.getSolution(keys[0]);
		else
			return this.reglesDecoded.getSolution(keys[0], keys[1], keys[2]);
	}

	public int length() {
		return this.resultatsEtendus.size();
	}

	public ContexteRecherche getContexte() {
		return this.ctx;
	}


	protected void addRegleToDecoded(RegleResultat regleRes, String decodedText) {
		ResultatRecherche newRes = new ResultatRecherche(regleRes, decodedText);
		this.reglesDecoded.addResultat(newRes);
	}

	protected void setResEtendu(RubriqueResultat result) {
		RubriqueResultat res = this.getResEtendu(result.getKey());
		boolean exists = res != null;

		if (exists) {
			String regleId = result.getFirstRegleIdFound();

			res.setRubrique(regleId, result.getFirstPhaseFound(), result.getFirstTypeFound());
		} else {
			this.resultatsEtendus.add(result.clone());
		}
	}


}