# Graph Report - radiOOracle  (2026-07-17)

## Corpus Check
- 6 files · ~1,180 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 14 nodes · 20 edges · 2 communities
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5a3a51a7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- player.js
- 📡 radiOOracle — sito ascoltatori

## God Nodes (most connected - your core abstractions)
1. `init()` - 6 edges
2. `📡 radiOOracle — sito ascoltatori` - 5 edges
3. `safeLink()` - 4 edges
4. `renderSlot()` - 4 edges
5. `renderChannels()` - 3 edges
6. `fmt()` - 2 edges
7. `getJSON()` - 2 edges
8. `safeLogo()` - 2 edges
9. `Spazi pubblicitari sicuri` - 1 edges
10. `Aggiornare la scaletta` - 1 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (2 total, 0 thin omitted)

### Community 0 - "player.js"
Cohesion: 0.54
Nodes (7): fmt(), getJSON(), init(), renderChannels(), renderSlot(), safeLink(), safeLogo()

### Community 1 - "📡 radiOOracle — sito ascoltatori"
Cohesion: 0.33
Nodes (5): Aggiornare la scaletta, Canali ufficiali (YouTube / Suno / …), Privacy, 📡 radiOOracle — sito ascoltatori, Spazi pubblicitari sicuri

## Knowledge Gaps
- **4 isolated node(s):** `Spazi pubblicitari sicuri`, `Aggiornare la scaletta`, `Canali ufficiali (YouTube / Suno / …)`, `Privacy`
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Spazi pubblicitari sicuri`, `Aggiornare la scaletta`, `Canali ufficiali (YouTube / Suno / …)` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._