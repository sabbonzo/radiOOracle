# Graph Report - .  (2026-07-16)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 12 nodes · 15 edges · 2 communities
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5a0d5c27`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- player.js
- 📡 radiOOracle — sito ascoltatori

## God Nodes (most connected - your core abstractions)
1. `renderSlot()` - 4 edges
2. `init()` - 4 edges
3. `📡 radiOOracle — sito ascoltatori` - 4 edges
4. `fmt()` - 2 edges
5. `getJSON()` - 2 edges
6. `safeLink()` - 2 edges
7. `safeLogo()` - 2 edges
8. `Spazi pubblicitari sicuri` - 1 edges
9. `Aggiornare la scaletta` - 1 edges
10. `Privacy` - 1 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (2 total, 0 thin omitted)

### Community 0 - "player.js"
Cohesion: 0.52
Nodes (6): fmt(), getJSON(), init(), renderSlot(), safeLink(), safeLogo()

### Community 1 - "📡 radiOOracle — sito ascoltatori"
Cohesion: 0.40
Nodes (4): Aggiornare la scaletta, Privacy, 📡 radiOOracle — sito ascoltatori, Spazi pubblicitari sicuri

## Knowledge Gaps
- **3 isolated node(s):** `Spazi pubblicitari sicuri`, `Aggiornare la scaletta`, `Privacy`
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Spazi pubblicitari sicuri`, `Aggiornare la scaletta`, `Privacy` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._