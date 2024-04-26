package apple.util.rechercherExpression.solutionRecherche;

import java.io.Serializable;

import javafx.util.Pair;

/**
 * Surcouche de la classe {@link Pair} de JavaFx de manière à la Serialiser
 * 
 * <p>
 * Permet de stocker un couple de deux chiffres indiquant le début et la fin de
 * l'élément dans lequel on l'implémente.
 * 
 * <p>
 * 
 * @author Eric PHILIPPE
 *
 */
public class PairSolution implements Serializable {
	private static final long serialVersionUID = 8505642764591751615L;
	public int start;
	public int end;

	public PairSolution(int start, int end) {
		this.start = start;
		this.end = end;
	}
}
