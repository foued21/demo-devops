package fr.univeiffel.boulangerie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * Génère le site de la boulangerie (la « prod ») dans le dossier site/.
 * Tous les prix affichés sur le site sont calculés ici, en Java, par la classe Panier.
 * Usage : java -cp target/classes fr.univeiffel.boulangerie.GenerateurSite site
 */
public final class GenerateurSite {

    /** Le texte de l'annonce orange en haut du site : à modifier pendant la démo. */
    static final String ANNONCE = "🥐 les croissants sont laaaaa ! ";

    private static final int QUANTITE_MAX = 20;

    private GenerateurSite() {
    }

    public static void main(String[] args) throws IOException {
        Path dossier = Path.of(args.length > 0 ? args[0] : "site");
        Files.createDirectories(dossier);
        Files.writeString(dossier.resolve("index.html"), genererPage());
        System.out.println("Site généré dans " + dossier.toAbsolutePath());
    }

    static String genererPage() {
        // Prix de 0 à 20 baguettes, calculés par la logique métier Java
        StringJoiner prix = new StringJoiner("\", \"", "[\"", "\"]");
        for (int q = 0; q <= QUANTITE_MAX; q++) {
            prix.add(Panier.formatEuros(Panier.total(q, Panier.PRIX_BAGUETTE)));
        }
        String sha = System.getenv().getOrDefault("GITHUB_SHA", "local");
        String version = sha.length() > 7 ? sha.substring(0, 7) : sha;
        String date = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm:ss"));

        return MODELE
                .replace("{{ANNONCE}}", ANNONCE)
                .replace("{{PRIX_UNITAIRE}}", Panier.formatEuros(Panier.PRIX_BAGUETTE))
                .replace("{{TABLE_PRIX}}", prix.toString())
                .replace("{{QUANTITE_MAX}}", String.valueOf(QUANTITE_MAX))
                .replace("{{VERSION}}", version)
                .replace("{{DATE}}", date);
    }

    private static final String MODELE = """
            <!doctype html>
            <html lang="fr">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>La Boulangerie du Groupe 2</title>
              <style>
                :root { --violet: #5B3FD9; --orange: #E86A33; --encre: #1E1B3A; --doux: #F4F1FF; }
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
                       background: linear-gradient(160deg, #FBFAFF 0%, #EFEAFF 60%, #FFE9DF 100%);
                       color: var(--encre); min-height: 100vh; display: flex; flex-direction: column;
                       align-items: center; padding: 40px 16px; }
                .annonce { background: var(--orange); color: #fff; font-weight: 700; padding: 10px 22px;
                           border-radius: 40px; font-size: 1.05rem; text-align: center; }
                h1 { font-size: clamp(2rem, 6vw, 3.4rem); margin: 22px 0 6px; text-align: center; }
                .sous-titre { color: #6B6790; margin-bottom: 30px; text-align: center; }
                .carte { background: #fff; border-radius: 26px; padding: 30px; width: 100%; max-width: 440px;
                         box-shadow: 0 14px 40px rgba(91, 63, 217, .15); }
                .produit { display: flex; align-items: center; gap: 16px; font-size: 1.2rem; font-weight: 600; }
                .produit .emoji { font-size: 2.6rem; }
                .prix { color: #6B6790; font-weight: 400; font-size: 1rem; }
                .ligne { display: flex; align-items: center; justify-content: space-between; margin-top: 24px; gap: 12px; }
                .quantite { display: flex; align-items: center; gap: 10px; }
                button { width: 44px; height: 44px; border-radius: 50%; border: none; background: var(--doux);
                         color: var(--violet); font-size: 1.5rem; font-weight: 700; cursor: pointer; }
                button:hover { background: var(--violet); color: #fff; }
                #qte { font-size: 1.4rem; font-weight: 700; min-width: 28px; text-align: center; }
                .total { font-size: 1.8rem; font-weight: 800; color: var(--violet); }
                .commander { margin-top: 26px; width: 100%; height: 54px; border-radius: 16px;
                             background: var(--violet); color: #fff; font-size: 1.1rem; }
                .commander:hover { background: var(--encre); }
                footer { margin-top: 34px; font-size: .9rem; color: #6B6790; text-align: center; line-height: 1.6; }
                footer b { color: var(--violet); }
              </style>
            </head>
            <body>
              <div class="annonce">{{ANNONCE}}</div>
              <h1>La Boulangerie du Groupe 2</h1>
              <p class="sous-titre">Commandez votre pain en ligne, retirez-le en boutique.</p>

              <main class="carte">
                <div class="produit"><span class="emoji">🥖</span>
                  <div>Baguette tradition<div class="prix">{{PRIX_UNITAIRE}} l'unité</div></div></div>
                <div class="ligne">
                  <div class="quantite">
                    <button id="moins" aria-label="Retirer une baguette">−</button>
                    <span id="qte">1</span>
                    <button id="plus" aria-label="Ajouter une baguette">+</button>
                  </div>
                  <div class="total" id="total"></div>
                </div>
                <button class="commander" id="commander">Commander</button>
              </main>

              <footer>
                ☕ Généré en Java · 🚀 déployé automatiquement par le pipeline CI/CD<br>
                Version <b>{{VERSION}}</b> · mise en ligne le <b>{{DATE}}</b>
              </footer>

              <script>
                // Prix calculés côté Java (classe Panier) au moment de la construction du site
                const PRIX = {{TABLE_PRIX}};
                const MAX = {{QUANTITE_MAX}};
                let qte = 1;
                const maj = () => {
                  document.getElementById("qte").textContent = qte;
                  document.getElementById("total").textContent = PRIX[qte];
                };
                document.getElementById("plus").onclick = () => { if (qte < MAX) qte++; maj(); };
                document.getElementById("moins").onclick = () => { if (qte > 0) qte--; maj(); };
                document.getElementById("commander").onclick = () =>
                  alert("Commande de " + qte + " baguette(s) : " + PRIX[qte] + ". Merci !");
                maj();
              </script>
            </body>
            </html>
            """;
}
