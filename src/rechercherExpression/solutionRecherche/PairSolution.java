package apple.util.rechercherExpression.solutionRecherche;

import java.io.Serializable;

import javafx.util.Pair;

/**
 * Surcouche de la classe {@link Pair} de JavaFx de mani�re � la Serialiser
 * 
 * <p>
 * Permet de stocker un couple de deux chiffres indiquant le d�but et la fin de
 * l'�l�ment dans lequel on l'impl�mente.
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
