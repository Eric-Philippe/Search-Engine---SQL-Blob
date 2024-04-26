package apple.util.decoder;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import apple.util.dummyClasses.ConnectionData;
import apple.util.dummyClasses.Logger;
import apple.util.dummyClasses.AppleMot;
import apple.util.rechercherExpression.RegleResultat;

/**
 * Class for the Multiple Decoder.
 * 
 * <p>
 * This class allows to use the new {@link Decoder} in Multi-{@link Thread}s
 * 
 * <p>
 * This class also makes use of a cache system avoid too many access to the
 * databse too many times making use of the {@link ConcurrentHashMap} allowing
 * to feed the same Map between all the Threads
 * 
 * @note - We can expect 900 règles decoded / seconde with a 6 threads machine.
 * 
 * @author Eric PHILIPPE
 *
 */
public class DecoderMultiple {
	/** Total of THREADS available */
	private static final int NUM_THREADS = AppleProperties.getInt("decoder.NUM_THREADS", 6); // Attention, si on change cette property, il faut redémarrer le serveur pour qu'elle soit relue
	private ConcurrentHashMap<String, AppleMot> appleMotCache = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> texteEnClairCache = new ConcurrentHashMap<>();

	/**
	 * Constructor for a new empty Decoder Multiple
	 */
	public DecoderMultiple() {
	}

	/**
	 * Constructor for a new Decoder Multiple with an existing cache
	 * 
	 * @param appleMotCache      Cache for AppleMot
	 * @param textEnClairCache Cache for TexteEnClair
	 */
	public DecoderMultiple(ConcurrentHashMap<String, AppleMot> appleMotCache,
			ConcurrentHashMap<String, String> textEnClairCache) {
		this.appleMotCache = appleMotCache;
		this.texteEnClairCache = textEnClairCache;
	}

	public TreeMap<RegleResultat, String> decodeMultiple(Map<RegleResultat, byte[]> texteCode, ConnectionData cnx, String model,
														 String version, String project, String produit) throws AppleException {
		ConcurrentHashMap<RegleResultat, String> solutionsDecoded = new ConcurrentHashMap<>();

		ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

		for (Map.Entry<RegleResultat, byte[]> values : texteCode.entrySet()) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					// Récupération d'une connection dans la pool disponible
					try (PooledCnx pcnx=ConnectionPool.takeConnection()){
						RegleResultat regleResultat = values.getKey();
						byte[] appleTexteCode = values.getValue();

						// Récupération de la classe de recherche de mot
						AppleRechercheMot rechercheMot = Decoder.setAppleRechercheMotStatic(pcnx.getCnxData(), model, version, project,
								produit, regleResultat.getClientId());

						// Récupérations des valeurs et lancement du décodeur
						Decoder decoder = new Decoder(appleTexteCode, rechercheMot, appleMotCache, texteEnClairCache);
						String texte = decoder.decodeTexte(true);
						appleMotCache.putAll(decoder.getAppleMotCache());
						texteEnClairCache.putAll(decoder.gettexteEnClairCache());
						solutionsDecoded.put(regleResultat, texte);
					} catch (Exception e) {
						Logger.error(e.toString(), e);
					} // autoclose du PooledCnx par le pattern try-resource
				}
			});
		}

		executorService.shutdown();
		try {
			// Attend que tous les threads aient terminé avant de renvoyer la TreeMap
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		ConnectionPool.endConnections();

		TreeMap<RegleResultat, String> solMap = new TreeMap<>();
		solMap.putAll(solutionsDecoded);
		return solMap;
	}

	/**
	 * Getter for the current appleMot cache
	 *
	 * @return the current appleMotCache
	 */
	public ConcurrentHashMap<String, AppleMot> getAppleMotCache() {
		return this.appleMotCache;
	}

	/**
	 * Getter for the current texteEnClair Cache
	 * 
	 * @return the current texteEnClair cache
	 */
	public ConcurrentHashMap<String, String> getTexteEnClairCache() {
		return this.texteEnClairCache;
	}
}