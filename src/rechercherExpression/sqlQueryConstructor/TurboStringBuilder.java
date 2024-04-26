package apple.util.rechercherExpression.sqlQueryConstructor;

/**
 * Class permettant un StringBuilder moins punitif quant aux espaces, idéal pour
 * les longs textes sensibles aux espaces (SQL Query etc..)
 * Permet également de l'instancier en "debug" pour appliquer un saut de ligne et
 * ainsi faciliter la lecture d'un texte plein.
 * 
 * @author Eric PHILIPPE
 *
 */
class TurboStringBuilder {
	/** Instance du StringBuilder avec lequel on travail */
	private StringBuilder stringBuilder;
	/** Etat permettant d'identifier si le builder est en debug ou non */
	private boolean debug = false;
	
	/**
	 * Créé un nouveau String Builder
	 * 
	 * @param text - Premier Text à entrer
	 */
	public TurboStringBuilder(String text) {
		this.stringBuilder = new StringBuilder(text);
	}
	
	/**
	 * Créé un nouveau String Builder
	 */
	public TurboStringBuilder() {
		this.stringBuilder = new StringBuilder();
	}
	
	/**
	 * Créé un nouveau String Builder
	 * 
	 * @param text - Premier Text à entrer
	 * @param debug - Entrer vrai si on veut passer le builder en debug mode
	 */
	public TurboStringBuilder(String text, boolean debug) {
		this.stringBuilder = new StringBuilder(text);
		this.debug = debug;
	}
	
	/**
	 * Créé un nouveau String Builder
	 * 
	 * @param debug - Entrer vrai si on veut passer le builder en debug mode
	 */
	public TurboStringBuilder(boolean debug) {
		this.stringBuilder = new StringBuilder();
		this.debug = debug;
	}
	
	/**
	 * Ajoute le text donné en paramètre en ajoutant avant lui un espace
	 * En cas de debug rajoute un saut de ligne en plus
	 * 
	 * @param str - Text à ajouter
	 * @return TurboStringBuilder
	 */
	public TurboStringBuilder append(String str) {
		if (this.debug) this.stringBuilder.append("\n" + " " + str);
		else this.stringBuilder.append(" " + str);
		return this;
	}
	
	/**
	 * Retourne le charactère à l'index donné
	 * 
	 * @param i - Index cible
	 * @return char
	 */
	public char charAt(int i) {
		return this.stringBuilder.charAt(i);
	}
	
	/**
	 * Ajoute collé le texte donné en paramètre sans rien ajouter en plus.
	 * Comportement de nature de la méthode `append(String str`) de {@link StringBuilder}
	 * 
	 * @param str - Texte à coller à la suite
	 * @return
	 */
	public TurboStringBuilder add(String str) {
		this.stringBuilder.append(str);
		return this;
	}
	
	/**
	 * Retourne le StringBuilder brut
	 * 
	 * @return StringBuilder
	 */
	public StringBuilder toStringBuilder() {
		return this.stringBuilder;
	}

	@Override
	public String toString() {
		return this.stringBuilder.toString();
	}
}