package apple.util.rechercherExpression.solutionRecherche;

import apple.core.ConnectionData;
import apple.core.Logger;
import apple.core.AppleException;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.ContexteRecherche.ClientProjet;
import apple.util.rechercherExpression.RegleResultat;
import ppc.daospecific.AccesReferentiel;
import ppc.daospecific.SHRubrique;
import ppc.daospecific.stateholders.SHListeOrderedRub;

import java.io.Serializable;
import java.util.*;

public class SolutionsProcessor implements Serializable {
    private static final long serialVersionUID = -5864129234181499908L;

    /** Map contenant la liste des règles APPLE à décoder, chacune liée à son text brut */
    transient protected HashMap<RegleResultat, byte[]> reglesAPPLEToDecode = new HashMap<>();
    /** Map contenant la liste des règles PEAR à décoder, chacune liée à son text brut */
    transient protected HashMap<RegleResultat, byte[]> reglesPEARToDecode = new HashMap<>();

    /** Map contenant la liste des règles APPLE ParamCotis à décoder, sans son text */
    transient protected HashSet<RegleResultat> reglesParamCotisAPPLEToDecode = new HashSet<>();
    /** Map contenant la liste des règles PEAR ParamCotis à décoder, sans son text */
    transient protected HashSet<RegleResultat> reglesParamCotisPEARToDecode = new HashSet<>();

    /** Container principal */
    private ArrayList<RubriqueResultat> resultatsEtendus = new ArrayList<>();

    private ContexteRecherche ctx;
    private ConnectionData cnx;
    protected ResultatsRecherche reglesDecoded;

    /**
     * Créé un nouvel objet solution étendu à partir de {@link ResultatsRecherche}
     * ResultatsRecherche travail sur le paradigme de règle
     * Alors que Solutions travail sur celui des rubriques
     *
     * @param ctx - ContexteRecherche
     * @param cnx - ConnectionData
     * @param resRecherche - ResultatRecherche
     */
    public SolutionsProcessor(ContexteRecherche ctx, ConnectionData cnx, ResultatsRecherche resRecherche) {
        this.ctx = ctx;
        this.cnx = cnx;
        this.reglesDecoded = resRecherche;

    }

    /**
     * Etend la totalité des règles et convertissant le paradigme de la regle vers
     * les rubriques, en attrapant donc toutes les rubriques contenant les règles précédentes
     */
    public SolutionsContainer runExtends() {
        SolutionsContainer solutions=new SolutionsContainer(ctx, this.reglesDecoded);

        boolean inApple = ctx.getSearchInApple();
        boolean inPear = ctx.getSearchInPear();
        SolutionTool appleTool = new SolutionToolAPPLE();
        SolutionTool ppcTool = new SolutionToolPEAR();

        if (inApple && !inPear && this.reglesDecoded.getStringTupleRegleId().length != 0)
            appleTool.extend(cnx, reglesAPPLEToDecode, reglesPEARToDecode, reglesParamCotisAPPLEToDecode, reglesParamCotisPEARToDecode, solutions);
        if (inPear) // TODO: Regarder si on a au moins une règle
            ppcTool.extend(cnx, reglesAPPLEToDecode, reglesPEARToDecode, reglesParamCotisAPPLEToDecode, reglesParamCotisPEARToDecode, solutions);
        
        this.resultatsEtendus = solutions.resultatsEtendus;

        // Retire de la liste des règles à décoder les règles qui l'ont déjà été
        this.sortRegleAlreadyDecoded();

        /**
         * Trie la totalité des rubriques trouvée, en éliminant celles n'appartenant
         * pas au client si sur PEAR
         */
        this.sorts();
        
        solutions.resultatsEtendus = this.resultatsEtendus;
        
        if (this.ctx.getDecodeNeighborText()) {
	        if (!inPear) appleTool.decode(cnx, reglesAPPLEToDecode, reglesPEARToDecode, reglesParamCotisAPPLEToDecode, reglesParamCotisPEARToDecode, solutions);
	        else ppcTool.decode(cnx, reglesAPPLEToDecode, reglesPEARToDecode, reglesParamCotisAPPLEToDecode, reglesParamCotisPEARToDecode, solutions);
        }

        // Vide la totalité de l'objet Solutions afin de l'alléger des éléments n'étant plus nécessaires
        this.reglesAPPLEToDecode = new HashMap<>();
        this.reglesPEARToDecode = new HashMap<>();
        this.reglesDecoded.emptyCache();
        this.cnx = null;

        return solutions;
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
     * Add Regle to the Apple ones that need to be decoded
     * @param res
     * @param text
     */
    public static void addRegleToDecode(HashMap<RegleResultat, byte[]> reglesToDecode , RegleResultat res, byte[] text) {
        boolean found = false;
        for (Map.Entry<RegleResultat, byte[]> entry : reglesToDecode.entrySet()) {
            if (entry.getKey().getRegleId().equals(res.getRegleId())) {
                found = true;
                break;
            }
        }

        if (found) return; // Epêche les doublons
        reglesToDecode.put(res, text);
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
     * @param key
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

    public ConnectionData getConnection() {
        return this.cnx;
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

    private void sortRegleAlreadyDecoded() {
        HashMap<RegleResultat, byte[]> newMap = new HashMap<>();
        if (!this.reglesAPPLEToDecode.isEmpty()) {
            for (Map.Entry<RegleResultat, byte[]> couple : this.reglesAPPLEToDecode.entrySet()) {
                if (!this.reglesDecoded.contains(couple.getKey().getRegleId()))
                    newMap.put(couple.getKey(), couple.getValue());
            }
            this.reglesAPPLEToDecode = newMap;
        }

        HashMap<RegleResultat, byte[]> newMapPEAR = new HashMap<>();
        if (!this.reglesPEARToDecode.isEmpty()) {
            for (Map.Entry<RegleResultat, byte[]> couple : this.reglesPEARToDecode.entrySet()) {
                if (!this.reglesDecoded.contains(couple.getKey().getRegleId()))
                    newMapPEAR.put(couple.getKey(), couple.getValue());
            }
            this.reglesPEARToDecode = newMapPEAR;
        }
    }

    private void sorts() {
        // New Container
        ArrayList<RubriqueResultat> resultatsEtendusSorted = new ArrayList<>();

        // On filtre tous les résultats sans solutions et ou vide
        for (RubriqueResultat res : this.resultatsEtendus) {
            if (!res.isEmptySolution() && !res.hasNoSolutions())
                resultatsEtendusSorted.add(res);
        }

        this.resultatsEtendus = resultatsEtendusSorted;

        if (this.ctx.getSearchInPear() && this.ctx.getClientsWithSolutionSize() > 0) {

            for (RubriqueResultat res : this.resultatsEtendus) {
                res.setProperties(ResultatRechercheProperties.COMMON_INT_NOORDRE, -1);
            }

            for (ClientProjet client : this.ctx.getAllInterstingClientProjet()) {
                try {
                    SHListeOrderedRub shListeRub = AccesReferentiel.listeTableRubriqueOrdered(this.cnx,
                            client.getClientId(), client.getProjetId(), new String[] { "GENERAL" });
                    int i = 0;
                    for (SHRubrique shRub : shListeRub._lstOrderedRub) {
                        i++;
                        RubriqueResultat res = this.getResEtenduFromObjetId(client.getClientId(), client.getProjetId(), shRub.getObjetId());
                        if (res == null) res = this.getResEtenduFromObjetId(client.getClientId(), "BC", shRub.getObjetId());
                        if (res != null) {
                            if (!((shRub.isLibre() && !shRub.isAlloueInit())
                                    || (!shRub.isLibre() && !shRub.isRetenuInit()))) {
                                res.setProperties(ResultatRechercheProperties.COMMON_INT_NOORDRE, i);
                            } else {
                                res.setProperties(ResultatRechercheProperties.COMMON_INT_NOORDRE, -1);
                            }
                        }
                    }

                } catch (AppleException e) {
    				Logger.error(e);
                }
            }

            HashSet<String> ppcRegleToDecode = new HashSet<>();
            HashSet<String> appleRegleToDecode = new HashSet<>();
            Iterator<RubriqueResultat> itrRubriqueRes = resultatsEtendusSorted.iterator();
            while (itrRubriqueRes.hasNext()) {
                RubriqueResultat rubriqueResultat = itrRubriqueRes.next();
                if (rubriqueResultat.getIntProp(ResultatRechercheProperties.COMMON_INT_NOORDRE) == -1) {
                    itrRubriqueRes.remove();
                } else {
                    List<String> allReglesFromRubrique = rubriqueResultat.getAllReglesId();

                    for (int i = 0; i < allReglesFromRubrique.size(); i++) {

                        String regleRub = allReglesFromRubrique.get(i);
                        if (regleRub.contains("Z8")) {
                            ppcRegleToDecode.add(regleRub);
                        } else appleRegleToDecode.add(regleRub);
                    }
                }
            }

            HashMap<RegleResultat, byte[]> mapAppleRegleToDecode = new HashMap<>();
            // Pour chacunes des règles actuel à décoder
            for (Map.Entry<RegleResultat, byte[]> regleAppleToDecode : this.reglesAPPLEToDecode.entrySet()) {
                RegleResultat regle = regleAppleToDecode.getKey();
                String key = regle.getRegleId();
                // Si elle est trouvée dans les règles à décoder
                if (appleRegleToDecode.contains(key)) {
                    mapAppleRegleToDecode.put(regle, regleAppleToDecode.getValue());
                }
            }
            this.reglesAPPLEToDecode = mapAppleRegleToDecode;


            HashMap<RegleResultat, byte[]> mapPearRegleToDecode = new HashMap<>();
            // Pour chacunes des règles actuel à décoder
            for (Map.Entry<RegleResultat, byte[]> reglePearToDecode : this.reglesPEARToDecode.entrySet()) {
                RegleResultat regle = reglePearToDecode.getKey();
                String key = regle.getClientId() + "#" + regle.getProjetId() + "#" + regle.getRegleId();
                // Si elle est trouvée dans les règles à décoder
                if (ppcRegleToDecode.contains(key)) {
                    mapPearRegleToDecode.put(regle, reglePearToDecode.getValue());
                }
            }
            this.reglesPEARToDecode = mapPearRegleToDecode;

        }

        this.resultatsEtendus = resultatsEtendusSorted;
        if (this.ctx.getClientsWithSolutionSize() > 1) {
            Collections.sort(this.resultatsEtendus, Comparator.comparing(RubriqueResultat::getClientId)
                    .thenComparing(Comparator.comparing(RubriqueResultat::getProjetId)
                            .thenComparing(Comparator.comparingInt(RubriqueResultat::getNoordre))));
        }
        else Collections.sort(this.resultatsEtendus, Comparator.comparingInt(RubriqueResultat::getNoordre));
    }
}