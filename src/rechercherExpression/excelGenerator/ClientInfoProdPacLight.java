package apple.util.rechercherExpression.excelGenerator;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import apple.core.ConnectionData;
import apple.core.DbAccessException;

public class ClientInfoProdPacLight {
	public String portefCode;
	public String portefLibelle;
	public String libelleClient;
	public String ALP;
	public String modeService;
	public String statut;
	public String dernierSaveInit;
	public String ECP;
	
	public ClientInfoProdPacLight(String portefCode, String portefLibelle, String libelleClient, String ALP, String modeService, String statut, String dernierSaveInit, String ECP) {
		this.portefCode = portefCode;
		this.portefLibelle = portefLibelle;
		this.libelleClient = libelleClient;
		this.ALP = ALP;
		this.modeService = modeService;
		this.statut = statut;
		this.dernierSaveInit = dernierSaveInit;
		this.ECP = ECP;
	}
	/**
	 * 
	 * @param clientsId
	 * @return Map<clientId, ClientInfoProdPac>
	 * @throws Exception 
	 * @throws DbAccessException 
	 */
	public static HashMap<String, ClientInfoProdPacLight> getClientsInfoPac(ConnectionData cnx, HashSet<String> clientsId) throws Exception {
		StringBuilder query = new StringBuilder("SELECT clientid, portef_code, portef_libelle, libelle, abon_mpe, mode_serv, stat_pac, dern_si, abon_ecp FROM PEAR_INFO_PROD_PAC");
		String clientIdSql = clientsId.stream().map(clientId -> "'" + clientId + "'").collect(Collectors.joining(", "));
		query.append(" WHERE clientid IN (" + clientIdSql + ")");
		
		HashMap<String, ClientInfoProdPacLight> mapClientInfoProPac = new HashMap<String, ClientInfoProdPacLight>();
		
		boolean dbAcess = false;
		try {
			dbAcess = cnx.openDbAccess();
			ResultSet res = cnx.getDbAccess().executeSelect(query.toString());
			
			String clientId, portefCode, portefLibelle, libelle, alp, modeService, statut, dernierSaveInit, ecp;
			while(res.next()) {
				clientId = res.getString(1);
				portefCode = res.getString(2);
				portefLibelle = res.getString(3);
				libelle = res.getString(4);
				alp = res.getString(5);
				modeService = res.getString(6);
				statut = res.getString(7);
				dernierSaveInit = res.getString(8);
				ecp = res.getString(9);

				mapClientInfoProPac.put(clientId, new ClientInfoProdPacLight(portefCode, portefLibelle, libelle, alp, modeService, statut, dernierSaveInit, ecp));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (dbAcess) cnx.closeDbAccess();
		}
		
		return mapClientInfoProPac;
	}
}