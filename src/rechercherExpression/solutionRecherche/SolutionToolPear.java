package apple.util.rechercherExpression.solutionRecherche;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apple.core.ConnectionData;
import apple.core.DbAccessException;
import apple.core.Logger;
import apple.core.AppleException;
import apple.util.decoder.DecoderMultiple;
import apple.util.decoder.DecoderMultipleCotis;
import apple.util.decoder.RegleCotisation;
import apple.util.rechercherExpression.ContexteRecherche;
import apple.util.rechercherExpression.RegleResultat;
import apple.util.rechercherExpression.sqlQueryConstructor.SqlQueryResultatsEtendus;

/**
 * ToolBox for the {@link SolutionsContainer} PEAR oriented
 * 
 * <p>
 * Classe implémentant {@link SolutionT}
 * 
 * @author Eric PHILIPPE
 */
public class SolutionToolPEAR extends SolutionToolAPPLE {
	private static final long serialVersionUID = -3021292583974713257L;

	public SolutionToolPEAR() {
		super();
	}

	@Override
	public void extend(ConnectionData cnx, HashMap<RegleResultat, byte[]> reglesAPPLEToDecode, HashMap<RegleResultat, byte[]> reglesPEARToDecode, HashSet<RegleResultat> reglesParamCotisAPPLEToDecode, HashSet<RegleResultat> reglesParamCotisPEARToDecode, SolutionsContainer solutions) {
		ContexteRecherche ctx = solutions.getContexte();
		ResultatsRecherche res = solutions.reglesDecoded;

		boolean dbAccess = false;

		try {
			String[] reglesId = res.getStringTupleClientProjetRegle(ctx.getAllInterstingClientProjet(),
					ctx.getProject());
			
			ArrayList<ResultatRecherche> reglesSolutionsClients = res.getAllRegleIdsFromClients();
			ArrayList<ResultatRecherche> reglesSolutionsModele = res.getAllRegleIdsFromModele();
			
			if (reglesId == null)
				return;

			dbAccess = cnx.openDbAccess();
			
			String query = SqlQueryResultatsEtendus.getQueryEtenduPear(ctx, reglesSolutionsClients, reglesSolutionsModele);
			//System.out.println(query);
			
			ResultSet result = cnx.getDbAccess().executeSelect(query);

			Set<String> objetIds = new HashSet<String>();
			
			// SubProjetId correspond au projetId d'origine la plus haute de la rubrique, projetId correspond à l'étage exact de personnalisation de la rubrique
			String clientId, projetId, objetId, libelle, retro, regleId, selectRegle;
			char phase;
			int cscp;
			byte[] texte;

			while (result.next()) {
				clientId = result.getString(1);
				//subProjetId = result.getString(2);
				projetId = result.getString(12);
				objetId = result.getString(4);
				libelle = result.getString(6);
				retro = result.getString(7);
				cscp = result.getInt(8);
				phase = result.getString(9).charAt(0);
				regleId = result.getString(10);
				texte = result.getBytes(11);
				selectRegle = "C"; // TODO
				
				String rubId = objetId.substring(3);
				//projetId = (ctx.getSearchInApple() && ctx.getClientsWithSolutionSize() > 1) ? subProjetId : projetId;
				
				if (ctx.getUniqueClientWithSolutionCount() > 1) {
					if (objetIds.contains(clientId + projetId + rubId + phase)) continue;
					objetIds.add(clientId + projetId + rubId + phase);
				} else {
					if (objetIds.contains(clientId + rubId + phase)) continue;
					objetIds.add(clientId  + rubId + phase);
				}

				if (regleId == null)
					regleId = "AAAAAA";
				
				boolean isParamCotis = regleId.charAt(3) == 'P';
				boolean inSolution;
				if (regleId.startsWith("Z8_"))
					inSolution = SolutionTool.isSolution(reglesId, SolutionTool.toId(clientId, projetId, regleId));
				else
					inSolution = SolutionTool.isSolution(res.getStringTupleRegleId(), regleId);
				RubriqueResultat newRes = new RubriqueResultat(clientId, projetId, objetId, libelle, 0,
						retro, cscp, regleId, phase, inSolution, selectRegle);
				solutions.setResEtendu(newRes);

				if (regleId.startsWith("Z8_")) {
					if (!isParamCotis) {
						if (!solutions.reglesDecoded.contains(clientId, projetId, regleId)) {
							SolutionsProcessor.addRegleToDecode(reglesPEARToDecode, new RegleResultat(regleId, projetId, clientId, false), texte);

						}
					} else {
						reglesParamCotisPEARToDecode.add(new RegleResultat(regleId, projetId, clientId, false));
					}
				} else if (!regleId.equals("")) {
					if (!isParamCotis) {
						if (!solutions.reglesDecoded.contains(regleId))
							SolutionsProcessor.addRegleToDecode(reglesAPPLEToDecode, new RegleResultat(regleId, true), texte);
					} else {
						reglesParamCotisAPPLEToDecode.add(new RegleResultat(regleId, true));
					}

				}
			}

		} catch (DbAccessException | SQLException e) {
			Logger.error(e);
		} finally {
			if (dbAccess)
				cnx.closeDbAccess();
		}
	}

	@Override
	public void decode(ConnectionData cnx,
			   HashMap<RegleResultat, byte[]> reglesAPPLEToDecode, HashMap<RegleResultat, byte[]> reglesPEARToDecode,
			   HashSet<RegleResultat> reglesParamCotisAPPLEToDecode, HashSet<RegleResultat> reglesParamCotisPEARToDecode,
			   SolutionsContainer solutions) {
		super.decode(cnx, reglesAPPLEToDecode, reglesPEARToDecode, reglesParamCotisAPPLEToDecode, reglesParamCotisPEARToDecode, solutions);
		HashMap<RegleResultat, byte[]> toDecode = reglesPEARToDecode;
		ResultatsRecherche reglesDecoded = solutions.reglesDecoded;
		ContexteRecherche ctx = solutions.getContexte();
		if (!toDecode.isEmpty()) {
			DecoderMultiple decoder = new DecoderMultiple(reglesDecoded.getCacheAppleMot(),
					reglesDecoded.getCacheTextEnClair());
			TreeMap<RegleResultat, String> decodedSolutions = new TreeMap<>();

			try {
				decodedSolutions = decoder.decodeMultiple(toDecode, cnx, ctx.getMainModel(),
						ctx.getMainVersion(), ctx.getProject(), ctx.getProduit());
			} catch (AppleException e) {
				Logger.error(e);
			}

			for (Map.Entry<RegleResultat, String> result : decodedSolutions.entrySet()) {
				solutions.addRegleToDecoded(result.getKey(), result.getValue());
			}
		}
		
		if (reglesParamCotisPEARToDecode.size() > 0) {
			decodeParamCotisPEAR(cnx, reglesParamCotisPEARToDecode, solutions);
		}
	}
	
	public void decodeParamCotisPEAR(ConnectionData cnx, HashSet<RegleResultat> toDecode, SolutionsContainer solutions) {
		ContexteRecherche ctx = solutions.getContexte();
		TreeMap<String, String> decodedSolutions = new TreeMap<>();

		List<RegleCotisation> reglesCotisations = SolutionToolPEAR.constructReglesIdCotis(cnx, ctx, toDecode);
		DecoderMultipleCotis decoder = new DecoderMultipleCotis();
		try {
			decodedSolutions = decoder.decodeMultiple(reglesCotisations, cnx, ctx.getMainModel(), ctx.getMainVersion(), ctx.getProject());
		} catch (AppleException e) {
			Logger.error(e);
		}
		

		for (Map.Entry<String, String> result : decodedSolutions.entrySet()) {
			solutions.addRegleToDecoded(new RegleResultat(result.getKey(), false), result.getValue());
		}
	}
	
	/**
	 * Build a List of {@link RegleCotisation} from a reglesSet of {@link RegleResultat}
	 *
	 * @param cnx - ConnectionData
	 * @param ctx - Contexte Recherche
	 * @param reglesSet - Set of regle we want to convert / build
	 * @return
	 */
	private static List<RegleCotisation> constructReglesIdCotis(ConnectionData cnx, ContexteRecherche ctx, HashSet<RegleResultat> regles) {
		boolean dbAccess = false;  
		List<RegleCotisation> reglesCotisations = new ArrayList<>();
		
		/**
		 * [
		 * 	['710210', '1-RLY001', 'Z8_P5517'],
		 *  ['710210', 'BC', 'Z8_P5517']
		 * ]
		 */
		ArrayList<String> reglesIdFormated = new ArrayList<String>();
		ArrayList<String> trouple;
		Iterator<RegleResultat> it = regles.iterator();
		while (it.hasNext()) {
			RegleResultat regle = it.next();
			trouple = new ArrayList<String>();
			trouple.addAll(Arrays.asList(regle.getClientId(), regle.getProjetId(), regle.getRegleId()));
			reglesIdFormated.add("('" + regle.getClientId() + "', '" + regle.getProjetId() + "', '" + regle.getRegleId() + "')");
			
			if (!regle.getProjetId().equals(ContexteRecherche.DEFAULT_PROJECT)) {
				reglesIdFormated.add("('" + regle.getClientId() + "', '" + ContexteRecherche.DEFAULT_PROJECT + "', '" + regle.getRegleId() + "')");

			}
		}
		
		// Conversion en tupple
		String[] trouples = reglesIdFormated.toArray(new String[0]);

		try {
			dbAccess = cnx.openDbAccess();
			String query = SqlQueryResultatsEtendus.getQueryEtenduPearCotis(ctx, trouples).toString();
			ResultSet result = cnx.getDbAccess().executeSelect(query);
			String clientId, projetId, regleId, paramreg;
			boolean iterPaieEnvers, iterRegNet; 
			char plage;
			int gestcot;
			
			while (result.next()) {
				clientId = result.getString(1);
				projetId = result.getString(2);
				gestcot = result.getInt(5);
				plage = result.getString(6).charAt(0);
				iterRegNet = result.getInt(7) == 1;
				iterPaieEnvers = result.getInt(8) == 1;
				regleId = result.getString(9);
				paramreg = result.getString(10);
				
				reglesCotisations.add(new RegleCotisation(regleId, paramreg, ctx.getMainModel(), ctx.getMainVersion(), clientId, projetId, gestcot, plage, iterPaieEnvers, iterRegNet));
			}

		} catch (DbAccessException | SQLException e) {
			Logger.error(e);
		} finally {
			if (dbAccess)
				cnx.closeDbAccess();
		}
		
		return reglesCotisations;
	}
}
