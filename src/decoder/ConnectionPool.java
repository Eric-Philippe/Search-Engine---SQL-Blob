package apple.util.decoder;

import java.util.ArrayList;
import java.util.List;

import apple.core.ConnectionData;
import apple.core.DbAccessException;
import apple.core.Logger;
import apple.core.AppleProperties;

/**
 * Connection Pool System.
 * 
 * <p>
 * Load multiples {@link ConnectionData} to avoid SQL conflict
 * 
 * <p>
 * Allows a multi-threading system to work with database access
 * 
 * @author Eric PHILIPPE
 */
public class ConnectionPool {
	private static final int INITIAL_POOL_SIZE = 0; // ne pas se réserver d'emblée des connexions, trop coûteux.
	private static final int MAX_POOL_SIZE = AppleProperties.getInt("decoder.ConnectionPool.MAX_POOL_SIZE", 6); //  // Attention, si on change cette property, il faut redémarrer le serveur pour qu'elle soit relue

	private static List<PooledCnx> connections = new ArrayList<>();
	private static String _sLastStateMsg=null;

	static {
		fillPool();
	}

	public static synchronized PooledCnx takeConnection() {
		if (connections.isEmpty()) {
			try {
				return new PooledCnx();
			} catch (DbAccessException e) {
				Logger.error(e.toString(), e);
				return null;
			}
		} else {
			return connections.remove(0);
		}
	}
	
	private static int getCountOpenCnx() {
		int nOpen=0;
		for (PooledCnx conn : connections) {
			if (conn.isOpen())
				nOpen++;
		}
		return nOpen;
	}

	/**
	 * Appelé par le close de PooledCnx.
	 * @param conn
	 */
	protected static synchronized void giveBackConnection(PooledCnx conn) {
		connections.add(conn);
		String sStateMsg=getCountOpenCnx()+"/"+connections.size();
		if (hasChanged(sStateMsg))
			Logger.debug("Connexions dans le pool: "+sStateMsg+" (ouvertes / nb total)");
		if (connections.size()>MAX_POOL_SIZE)
			Logger.info("On dépasse le nb max de connexions dans le pool ("+MAX_POOL_SIZE+") - On va nettoyer");
		
		// nettoyage pour ne pas dépasser le nb max de cnx ouvertes gardées en pool
		while (connections.size()>MAX_POOL_SIZE) {
			try {
				PooledCnx pcnx=connections.remove(0);
				if (pcnx.isOpen())
					pcnx.getCnxData().closeDbAccess();
			}
			catch (DbAccessException e) {
				Logger.error(e.toString(), e);
			}
		}
	}

	private static boolean hasChanged(String sStateMsg) {
		if (_sLastStateMsg==null || !_sLastStateMsg.equals(sStateMsg)) {
			_sLastStateMsg=sStateMsg;
			return true;
		}
		return false;
	}

	/**
	 * Termine et ferme toutes les connections restant actuellement dans le pool
	 */
	public static synchronized void endConnections() {
		Logger.info("Nettoyage des connexions de ConnectionPool");
		for (PooledCnx conn : connections) {
			if (conn.isOpen()) {
				try {
					conn.getCnxData().closeDbAccess();
				}
				catch (DbAccessException e) {
					Logger.error(e.toString(), e);
				}
			}
		}
		connections.clear();
	}

	private static void fillPool() {
		if (connections.size() != 0) 
			endConnections();
		connections = new ArrayList<>();
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			try {
				connections.add(new PooledCnx());
			} catch (DbAccessException e) {
				Logger.error(e.toString(), e);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (getCountOpenCnx()>0) {
			Logger.error("Dans apple.util.decoder.ConnectionPool.finalize(), "+getCountOpenCnx()+" non fermées dans le pool, qui contient "+connections.size()+" PooledCnx");
			for (PooledCnx pcnx: connections) {
				Logger.debug(pcnx.toString());
			}
		}
	}
}

