# PvP ability progression — draft awaiting user choices

Requested while integrating the five supplied models: one ability per weapon,
progressively improved by one level for each player kill. Special activation
moves from right-click to swap-hands, which the user can bind from F to G without
a client mod. Keep uploaded models and animation integration moving forward.

Pending text answers: which weapons the user will design personally; whether
progress belongs to each player/weapon or to the physical weapon; maximum level
(5/3/10). Do not invent the user's reserved ability or choose persistence semantics
before those answers arrive. Proposed level 0 is locked, first eligible PvP kill
unlocks level 1, and each subsequent kill improves the same ability up to the cap.
Mobs and self kills do not count. Activation must respect existing cooldowns,
transaction locks, teams and region protection.

Research: public community recreations, not verified original-server code:
- https://modrinth.com/plugin/altar-smp-there-is-no-arc-version : Bloodlust kill
  scaling, Shadow Leap/Shadow Daggers, Mind Leap/Mindweaver Gust, Paladin's Battle
  Axe Earthshatter/Stalwart Absorption.
- https://modrinth.com/plugin/altar-smp-arc-1-plugin : Bloodlust kill count and
  configurable bleeding; documents itself as a replica.
- https://modrinth.com/plugin/unknowns-altar-smp : Bloodlust personal kill
  progression, tracking and Bloodhook; also a recreation, not an official spec.

Candidate themes to discuss, not yet replacements for live powers:
- Bloodlust: blood tether/bleeding, inspired by Bloodhook; improve range and
  duration rather than adding unrelated abilities at each level.
- Shadow Blade: a fan of shadow daggers, inspired by Shadow Daggers; improve
  coverage/range while keeping one consistent action.
- Windweaver: one directed gust with lift/knockback, inspired by Mindweaver Gust;
  improve reach/control, retaining a readable escape window.
- Earthbreaker: one ground fissure/shockwave inspired by Earthshatter; improve
  reach/width. Preserve the user's fixed two-heart true-damage rule unless they
  explicitly replace it.
- Thunder Spear: no matching entry found in these sources; reserve for the user's
  own design rather than assigning a fabricated Altar SMP ability.

Implementation of kill progression and power replacement is pending those user
choices. The independent models/animations and swap-hands activation can be
implemented and tested without deciding this data contract.

## Confirmed choices

The user confirmed: Thunder Spear and Windweaver are reserved for their own
abilities; progression is per player and weapon, retained after death; the cap
is five levels. Implement Bloodlust blood tether, Shadow daggers and Earthbreaker
ground fissure from the researched themes. The reserved two retain their prior
powers provisionally and accumulate levels, without fabricated new scaling.
The first eligible PvP kill unlocks level 1. Every subsequent kill adds one level
until 5. Credit the weapon used at damage time, not whatever is held at death.

## Corrected reference scope

User explicitly corrected generic inspirations: Bloodlust must use Bloodlust's
own Altar progression, Shadow Blade its own Shadow powers, and Earthbreaker the
Paladin Battle Axe powers. Confirmed the original Bloodlust progression over the
earlier proposal of one improved ability: base Infection/bleeding; level 1 Speed
II; 2 private Blood Tracker; 3 Blood Trail; 4 Strength I; 5 Blood Hook. Public
Arc 1 tooltips and the corresponding messages.yml corroborate these stages.
Source: https://modrinth.com/plugin/altar-smp-arc-1-plugin and
https://github.com/Kingscraft-Studios/AltarSMP/blob/9a09c1731dabc908364445dc54a4220c02b7c25b/src/main/resources/messages.yml

Shadow primary: shadow daggers with Slowness/Blindness, plus a secondary Shadow
Leap. Paladin primary: giant axe hologram and radial Earthshatter, plus secondary
Stalwart Absorption/limited reflection. Use G for primary and sneak+G for secondary.
Preserve user balance constraints: axe shock is exactly four health points once
per victim; Shadow burst is capped at two hearts before reductions. Windweaver and
Thunder Spear are reserved for user-designed powers and retain prior powers
provisionally. Cooldowns must remain persistent and must not become bypassable
by switching primary/secondary; honor legacy stored cooldowns when upgrading.

## Latest authoritative specification and implementation

The supplied `standalone/specs/user-elemental-abilities.md` supersedes the earlier
G-only proposal for Thunder Spear and Windweaver. Each now has five cumulative
unlocks with independent persistent cooldowns and the specified click/Shift/swap
gestures. G remains a vanilla rebind of swap-hands, not a client-only custom key.
The two weapon mechanics are separated into Thunder/Wind classes, with shared
protection, ownership, hit budgets, input routing and native damage attribution.

Bloodlust follows the explicitly confirmed Altar progression: Infection, held
Speed II at 1, private Tracker at 2, Trail at 3, held Strength I at 4, Hook at 5.
Shadow uses daggers/leap with capped damage; Earthbreaker uses Earthshatter and
Stalwart Absorption with the user's exact four-health-point shock. Old generic
prototype powers, crater destruction and the old spear paralysis are superseded.
Persistent identifiers and receipt recovery remain compatible. No uploaded source
or valuable historical model was deleted.

Validation results are recorded in standalone/docs/user-abilities-native-2.0.15.json
and standalone/VERIFY.md, including limitations of the native fixture.
