# Fork notes — Domum Ornamentum on upstream `port/26.1` (2026-10-09)

Owner decision 2026-10-09: follow ldtteam's own port branch instead of our independent 26.1 port.

- Base: `upstream/port/26.1` @ 71d91c6 ("Part one of the port…", Marc Hermans, 2026-09-27) — their new rendering
  (`RetexturingHandler`, `MateriallyTexturedUnbakedModel`, tint providers), record `MaterialTextureData`,
  `SelfUpgradingBlockItem`, model-provider datagen with split client/server outputs, junit tests.
- Applied as a working-tree 3-way merge (base 247f16a): every conflict resolved to upstream, upstream deletions taken,
  then our old-design leftovers removed (hand-written `items/*.json`, `port26_variants`, our baked model, `datagen26`).
  Our pre-merge tree is archived in `/root/home/forks/_backups/domum-ours-before-upstream-rebase-20261009.tar.gz`.
- Kept from our port: EMI plugin (`emi/`, fleet policy — no JEI), `lang/ru_ru.json`, defensive copy of the texture map in
  `DynamicTimberFrameBlockEntity` (off-thread mesher race), `ArchitectsCutterRecipe#isSpecial()`.
- Build changes vs upstream: Minecraft 26.1.2 / NeoForge 26.1.2.114 (upstream: 26.1 / 26.1.0.8-beta); EMI instead of
  JEI; published Tableau 0.0.107 instead of `includeBuild('../Tableau')`; Gradle heap 2 G; explicit
  `--mod/--all/--output/--existing` for `clientData`/`serverData` (the published Tableau's data runs arrive without them).
- Verified (vdsfi): compileJava, runClientData + runServerData (output identical to upstream's generated tree),
  `build` incl. upstream junit tests, publishToMavenLocal as `com.ldtteam:domum-ornamentum:1.0.0-local`.
- Not yet verified: in-game rendering of retextured blocks, Architect's Cutter UI, EMI plugin at runtime.
