# Oracle Toy — app Android (camera → AI solver → toy Lovense)

App Android nativa che inquadra un **quiz/enigma** con la camera, lo fa risolvere a
un'**AI di visione** (Claude Messages API) e mappa l'esito su comandi Bluetooth a un
**toy Lovense**. Uso consensuale (gioco tra partner d'accordo).

> Progetto **separato** dal sito pubblico radiOOracle. Non fa parte del sito servito da
> GitHub Pages e non deve finire in alcun mirror pubblico. Nessuna chiave o dato
> personale va committato.

## Come parla col toy — reverse engineering, in breve
Il protocollo comandi Lovense è **pubblico**: si scrive una stringa ASCII terminata da
`;` su una caratteristica GATT (`Vibrate:0..20;`, `Battery;`, `DeviceType;`). Non serve
RE da zero. L'unica parte davvero "reverse engineering" è, se il tuo modello differisce,
confermare l'**UUID della caratteristica di scrittura** sul **tuo** dispositivo con una
scansione GATT (es. nRF Connect) e aggiornare `WRITE_CHAR_UUID` in
`toy/LovenseBleController.kt`.

## Sicurezza / consenso (integrati nel prodotto)
- **Gate di consenso** obbligatorio prima di ogni azione (checkbox in UI).
- **STOP** sempre visibile che invia `Vibrate:0;` immediatamente.
- **Cap di intensità** regolabile, sempre ≤ 20 (limite Lovense), applicato in
  `OutcomeMapper` e ri-clampato in ogni backend.
- Stato del toy sempre visibile: niente controllo occulto.

## Architettura
| Modulo | Ruolo |
|---|---|
| `camera/CameraController.kt` | CameraX: anteprima + cattura di un frame JPEG |
| `ai/OracleSolver.kt` | POST all'API Claude (immagine base64 + `output_config.format`) → `{answer, correct, confidence}` |
| `logic/OutcomeMapper.kt` | lettera della risposta → N pulsazioni (A=1, B=2, … Z=26), all'intensità impostata; puro e testato |
| `toy/ToyController.kt` | interfaccia; `LovenseBleController` (BLE reale) + `MockToyController` (dry-run) |
| `ui/OracleViewModel.kt`, `ui/MainScreen.kt` | consenso, STOP, cap, loop capture→solve→drive |

## Setup
1. Aprire `oracle-toy-app/` in Android Studio (o CLI Gradle).
2. `cp local.properties.example local.properties` e inserire `CLAUDE_API_KEY`
   (Android Studio aggiunge anche `sdk.dir`). `local.properties` è gitignorato.
3. Build ed esegui su un dispositivo fisico (BLE + camera non funzionano in emulatore).

> La chiave viaggia nell'APK: accettabile per un build personale. Per una diffusione più
> ampia, metti un piccolo backend proxy tra app e API invece di spedire la chiave.

## Verifica
- **Senza hardware:** in `MainActivity` imposta `useMockToy = true` → i comandi vengono
  loggati (Logcat) invece di pilotare il BLE. Esegui il flusso completo
  camera→solver→azione.
- **Unit test:** `./gradlew :app:testDebugUnitTest` esegue `OutcomeMapperTest`
  (cap intensità, esiti giusto/sbagliato/ignoto).
- **Con hardware:** `useMockToy = false`, "Connetti toy", verifica stato/batteria, poi
  "Chiedi all'oracolo" inquadrando un quiz; verifica che **STOP** interrompa subito.
- Prima di ogni commit: `git status` non deve mostrare `local.properties` né chiavi.
