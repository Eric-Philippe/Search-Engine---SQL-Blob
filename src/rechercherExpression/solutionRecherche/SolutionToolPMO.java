package apple.util.rechercherExpression.solutionRecherche;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * ToolBox for the {@link SolutionsContainer} APPLE oriented
 * 
 * <p>
 * Classe implémentant {@link SolutionT}
 * 
 * @author Eric PHILIPPE
 */
public class SolutionToolAPPLE extends SolutionTool {
	private static final long serialVersionUID = 7021418249859366322L;

	public SolutionToolAPPLE() {
		super();
	};

	@Override
	public void extend(ConnectionData cnx, HashMap<RegleResultat, byte[]> reglesAPPLEToDecode, HashMap<RegleResultat, byte[]> reglesPEARToDecode, HashSet<RegleResultat> reglesParamCotisAPPLEToDecode, HashSet<RegleResultat> reglesParamCotisPEARToDecode, SolutionsContainer solutions) {
		ContexteRecherche ctx = solutions.getContexte();
		ResultatsRecherche res = solutions.reglesDecoded;

		boolean dbAccess = false;

		try {
			dbAccess = cnx.openDbAccess();
			String[] reglesId = res.getStringTupleRegleId();
			
			String query = SqlQueryResultatsEtendus.getQueryEtenduApple(ctx, reglesId);
			// System.out.println(query);
			
			ResultSet result = cnx.getDbAccess().executeSelect(query);

			String objetId, libelle, regleId, retro, selectRegle;
			char phase;
			int noordre, cscp;
			byte[] texte;

			while (result.next()) {
				objetId = result.getString(1);
				libelle = result.getString(2);
				retro = result.getString(3);
				cscp = result.getInt(4);
				noordre = result.getInt(5);
				selectRegle = result.getString(6);
				phase = result.getString(7).charAt(0);
				regleId = result.getString(8);
				texte = result.getBytes(9);

				if (regleId == null)
					regleId = ContexteRecherche.EMPTY_REGLE;

				boolean isParamCotis = regleId.charAt(3) == 'P';
				boolean inSolution = SolutionTool.isSolution(reglesId, regleId);

				if (objetId == null)
					continue;

				RubriqueResultat newRes = new RubriqueResultat(objetId, libelle, noordre, retro, cscp, regleId, phase,
						inSolution, selectRegle);
				
				solutions.setResEtendu(newRes);
				if (!regleId.equals("")) {
					if (!isParamCotis) {
						if (!solutions.reglesDecoded.contains(regleId)) {
							SolutionsProcessor.addRegleToDecode(reglesAPPLEToDecode, new RegleResultat(regleId, true), texte);
							}
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
		HashMap<RegleResultat, byte[]> toDecode = reglesAPPLEToDecode;
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

		if (reglesParamCotisAPPLEToDecode.size() > 0) {
			decodeParamCotisAPPLE(cnx, reglesParamCotisAPPLEToDecode, solutions);
		}
	}
	
	public void decodeParamCotisAPPLE(ConnectionData cnx, HashSet<RegleResultat> toDecode, SolutionsContainer solutions) {
		ContexteRecherche ctx = solutions.getContexte();
		TreeMap<String, String> decodedSolutions = new TreeMap<>();

		List<RegleCotisation> reglesCotisations = SolutionToolAPPLE.constructReglesIdCotis(cnx, ctx, toDecode);
		DecoderMultipleCotis decoder = new DecoderMultipleCotis();
		try {
			decodedSolutions = decoder.decodeMultiple(reglesCotisations,cnx, ctx.getMainModel(),
					ctx.getMainVersion(), ctx.getProject());
		} catch (AppleException e) {
			Logger.error(e);
		}

		for (Map.Entry<String, String> result : decodedSolutions.entrySet()) {
			solutions.addRegleToDecoded(new RegleResultat(result.getKey(), true), result.getValue());
		}

	}
	
	private static List<RegleCotisation> constructReglesIdCotis(ConnectionData cnx, ContexteRecherche ctx,
			HashSet<RegleResultat> reglesSet) {
		boolean dbAccess = false;
		List<RegleCotisation> reglesCotisations = new ArrayList<>();

		ArrayList<String> reglesIdFormated = new ArrayList<String>();
		Iterator<RegleResultat> it = reglesSet.iterator();
		while (it.hasNext()) {
			RegleResultat regle = it.next();
			reglesIdFormated.add(regle.getRegleId());
		}

		try {
			dbAccess = cnx.openDbAccess();
			String query = SqlQueryResultatsEtendus.getQueryEtenduAppleCotis(ctx,
					reglesIdFormated.toArray(new String[0]));
			ResultSet result = cnx.getDbAccess().executeSelect(query);
			String modeleId, versionId, projetId, regleId, paramreg, plageS;
			boolean iterPaieEnvers, iterRegNet;
			char plage = 0;
			int gestcot;

			while (result.next()) {
				modeleId = result.getString(1);
				versionId = result.getString(2);
				projetId = result.getString(3);
				regleId = result.getString(6);
				paramreg = result.getString(11);
				iterPaieEnvers = result.getInt(10) == 1;
				iterRegNet = result.getInt(9) == 1;
				plageS = result.getString(8);
				gestcot = result.getInt(7);

				if (plageS != null && plageS.trim() != "") {
					plage = plageS.charAt(0);
				}
				reglesCotisations.add(new RegleCotisation(modeleId, versionId, projetId, regleId, paramreg, gestcot,
						plage, iterPaieEnvers, iterRegNet));
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
