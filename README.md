# 📡 radiOOracle — sito ascoltatori

La web radio pubblica: player, scaletta del giorno e **spazi sponsor gestiti**.
Statico, gratis (GitHub Pages), senza script di terzi.

## Spazi pubblicitari sicuri
- Gli slot (`banner`, `box1`, `box2`, `footer`) si riempiono da `data/sponsors.json`: solo
  **testo + logo (https) + link (https)**. Niente ad-network, niente JavaScript di terzi.
- Una **Content-Security-Policy** blocca script esterni, iframe nascosti e inline injection.
- Box vuoto = "Spazio disponibile" con invito all'inserzionista (`advertise_url`).
- Per vendere uno spazio: aggiorna `data/sponsors.json` (vedi `data/sponsors.example.json`).

## Aggiornare la scaletta
`data/scaletta.json` è esportato dal motore locale (titoli reali, nessun path/dato privato).
Ogni brano può avere un campo opzionale `suno_url` (solo https) che mostra il pulsante "🎵 Suno" nella riga.

## Canali ufficiali (YouTube / Suno / …)
`data/links.json` elenca i canali: compila `url` (solo https) per far comparire il pulsante
nell'header. `url` vuoto = pulsante nascosto. Nessuno script di terzi, solo link.

## Privacy
Online vanno solo titoli pubblici e contenuti promozionali. Mai path, email, cookie o dati personali.
