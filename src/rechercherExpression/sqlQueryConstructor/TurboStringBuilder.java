package apple.util.rechercherExpression.sqlQueryConstructor;

/**
 * Class permettant un StringBuilder moins punitif quant aux espaces, id�al pour
 * les longs textes sensibles aux espaces (SQL Query etc..)
 * Permet �galement de l'instancier en "debug" pour appliquer un saut de ligne et
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
	 * Cr�� un nouveau String Builder
	 * 
	 * @param text - Premier Text � entrer
	 */
	public TurboStringBuilder(String text) {
		this.stringBuilder = new StringBuilder(text);
	}
	
	/**
	 * Cr�� un nouveau String Builder
	 */
	public TurboStringBuilder() {
		this.stringBuilder = new StringBuilder();
	}
	
	/**
	 * Cr�� un nouveau String Builder
	 * 
	 * @param text - Premier Text � entrer
	 * @param debug - Entrer vrai si on veut passer le builder en debug mode
	 */
	public TurboStringBuilder(String text, boolean debug) {
		this.stringBuilder = new StringBuilder(text);
		this.debug = debug;
	}
	
	/**
	 * Cr�� un nouveau String Builder
	 * 
	 * @param debug - Entrer vrai si on veut passer le builder en debug mode
	 */
	public TurboStringBuilder(boolean debug) {
		this.stringBuilder = new StringBuilder();
		this.debug = debug;
	}
	
	/**
	 * Ajoute le text donn� en param�tre en ajoutant avant lui un espace
	 * En cas de debug rajoute un saut de ligne en plus
	 * 
	 * @param str - Text � ajouter
	 * @return TurboStringBuilder
	 */
	public TurboStringBuilder append(String str) {
		if (this.debug) this.stringBuilder.append("\n" + " " + str);
		else this.stringBuilder.append(" " + str);
		return this;
	}
	
	/**
	 * Retourne le charact�re � l'index donn�
	 * 
	 * @param i - Index cible
	 * @return char
	 */
	public char charAt(int i) {
		return this.stringBuilder.charAt(i);
	}
	
	/**
	 * Ajoute coll� le texte donn� en param�tre sans rien ajouter en plus.
	 * Comportement de nature de la m�thode `append(String str`) de {@link StringBuilder}
	 * 
	 * @param str - Texte � coller � la suite
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