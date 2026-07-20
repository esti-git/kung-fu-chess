# KungFu Chess

Real-time chess variant (pieces move async with cooldowns, no turns) — Java + Swing, school project based on CTD slides.

## Language
Everything is Java. Never suggest Python or any other language.

## Structure (src/)
- `engine/` — GameEngine, core game loop
- `rules/` — RuleEngine, PieceFactory, PawnPromotion, `pieces/` per-piece move rules
- `model/` — Board, Piece, GameState, Position, pending move/jump/rest records
- `events/` — EventBus + Event types (move made, piece captured, game started/ended)
- `view/` — Swing rendering: BoardRenderer, snapshots, animation, score/move-history tracking
- `protocol/` — shared wire format: MoveCommand, JumpCommand, NetworkState, StateCodec, PieceCodes
- `server/` — GameServer, ServerMain (authoritative game logic)
- `client/` — GameClient, ClientMain, ClientView (thin display)
- `io/` — BoardParser
- `config/` — GameConfig

## Architecture
Two entry points:
- `Main.java` — old single-process local version.
- `ServerMain` + `ClientMain` — WebSocket client-server mode.

In client-server mode, all game logic runs **only** on the server; clients are thin displays that render server state. `protocol/` is the shared wire format between them. Each client has a local `EventBus` fed by events the server sends, driving score/log/sound.

## Running
Start `ServerMain` first, then run `ClientMain` once per player.

## Working conventions
- Minimal changes to existing files; prefer adding new files over rewriting.
- Reuse existing engine/rules code — never reimplement game rules.
- No tests or READMEs unless explicitly asked.
- Always show a short plan before coding.
