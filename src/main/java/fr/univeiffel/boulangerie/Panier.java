package fr.univeiffel.boulangerie;

/**
 * Logique métier de la boulangerie : le calcul du panier.
 * Les prix sont en centimes pour éviter les erreurs d'arrondi des nombres à virgule.
 */
public final class Panier {

    /** Prix d'une baguette : 1,20 €. */
    public static final int PRIX_BAGUETTE = 120;

    private Panier() {
    }

    /** Prix total d'une commande, en centimes. */
    public static int total(int quantite, int prixCentimes) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }
        return quantite * prixCentimes;
    }

    /** Affiche un montant en euros, à la française : 360 → "3,60 €". */
    public static String formatEuros(int centimes) {
        return String.format("%d,%02d €", centimes / 100, centimes % 100);
    }
}
