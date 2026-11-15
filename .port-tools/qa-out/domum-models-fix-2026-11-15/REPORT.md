# Domum model fix — 2026-11-15

Миграция моделей проверена на MC 26.1.2 / NeoForge 26.1.2.126, Java 25. Запрошенный сценарий завершён: clean compileJava/build, 21 тест, datagen, проверка готового jar, server SAVE+REOPEN, клиент полной цепочки, материалы/tint, GUI настоящего Town Hall, /reload, F3+T и повторный вход. Блокеров и невыполненных пунктов этого сценария нет. Общий runtime не является error-free: внешние и оставшиеся сообщения перечислены ниже.

## Состояние и сохранение работы

HEAD Domum остался `164850c0aa394edd5e722ca19c5dc31998bf1df5`, BlockUI — `a3999dc44f30d9875048d321881a0b75f20f11dd`. BlockUI использован только для QA, его status пуст. Commit/push/roadmap/publish не выполнялись; соседние деревья не изменялись.

Прочитаны корневой AGENTS.md и `/root/.config/opencode/skill/mc-fleet-build-qa/SKILL.md`; локальных AGENTS.md внутри Domum нет. Прочитан предыдущий `blockui-domum-fix-20261115/REPORT.md` и просмотрены артефакты `client-qa-blockui-domum`. Дополнительный snapshot ДО правок: `/root/home/forks/.agent-sandbox/domum-models-fix-20261115/resume-snapshot-203725/`: binary diff HEAD, staged diff, status и 143 существующих dirty/untracked файла. Удалённые файлы представлены в patch. Более ранние inventory, baseline jar и скрипты сохранены в той же песочнице. Предыдущая реализация не перезаписывалась скриптом implement.py.

Старый `clean-build-1.log` оборван без результата; живого Gradle на восстановлении не было. Его нельзя считать PASS. Итерации и неудачные кадры сохранены как история, а не доказательство готовности. [ARTIFACTS.json](ARTIFACTS.json) содержит размеры и SHA 54 файлов доказательств; текущий diff/status и SHA находятся в `evidence/final-evidence/`.

## Подтверждённые ошибки и исправления

Baseline-client воспроизвёл **9 FailedModel Domum**: восемь allbrick использовали `forge:composite`, dynamic timberframe item — незарегистрированный geometry loader `domum_ornamentum:materially_textured`. Полная исходная цепочка и причины — [baseline log](evidence/baseline-client/console.log).

Сохранена и проверена начатая миграция: RegisterBlockStateModels регистрирует custom blockstate codec; RegisterItemModelsEvent отдельно регистрирует item type. Codec сохраняет rotations и optional overlay, зависимости моделей объявлены до bake. Материальная основа и translucent mortar запекаются отдельными частями CompositeBlockModel. Datagen-провайдеры выдают custom item types вместо старых geometry-loader wrappers. Геометрия steep/steep_lower сохранена; оба блока участвуют в datagen.

ItemModel.Unbaked.bake принимает BakingContext и Matrix4fc; update извлекает quads из неизменяемых material data стека, заполняет tintLayers, extents, transform и identity. Это сверено с javap и MachineItemModel из MI, а не принято по факту компиляции. CompositeBlockModel в API 26 делегирует dynamic geometry keys детям. Сохранены AIR-erasure, material/render mapping через IMateriallyTexturedBlockEntity и отдельная connected-geometry карта dynamic timberframe. Загрузка originalTextureData теперь действительно восстанавливает поле из ValueInput. Доказательства: `evidence/*javap.txt`, исходный diff, codec-тесты и повторно прочитанный NBT после перезапуска.

Первый новый runtime обнаружил ещё **11 предупреждений Domum**, хотя FailedModel уже было 0: три отсутствующие ссылки stairs и восемь missing-texture overlay. Исправлены ссылки на существующие `stairs_spec`, `stairs_inner_spec`, `stairs_outer_spec` в AllBrickStairModelProvider. Две настоящие overlay PNG восстановлены побайтно из локального upstream-коммита `a594045`, без рисования заменителей; blob/SHA/размеры — [restored-textures.json](evidence/restored-textures.json). Datagen перегенерировал четыре зависимых JSON. Проверка всех Domum model/texture references внутри финального jar дала пустой список отсутствующих ресурсов.

## Сборка и фактический runtime

`clean-build-final.log`: **BUILD SUCCESSFUL**, clean compileJava build; 21 тест, skipped/failures/errors=0, включая два MaterialModelCodecTest. Gradle работал последовательно, --offline, --no-daemon, --no-configuration-cache, --max-workers=1, heap 768m, ActiveProcessorCount=2, с локальным opcScript. Штатный build cache использован; Javadoc/deprecation warnings остались предупреждениями.

`datagen-resume.log`: успех, повтор предыдущей миграции дал 0 различий JSON. После найденных runtime-ошибок `datagen-fix2.log`: **BUILD SUCCESSFUL**, реально записаны четыре JSON allbrick stairs. Финальный build выполнен после этого datagen. Итоговый jar содержит custom item class, обе overlay PNG, blockstates/items/recipes/loot для обеих steep-форм. [Jar resource audit](evidence/jar-resource-audit-final.json).

Финальный Domum main jar:

`/root/home/forks/Domum-Ornamentum/build/libs/domum-ornamentum-1.0.0-local-main.jar`

SHA-256: `cb10cef1b55161af1b21f28d3e23c3d148978180fdc08fd2e875d5b9f39d9744`.

Build jar, server/mods и client/mods имеют этот же SHA. Старый mavenLocal остался с SHA `826bd91565e161e534963f9d153649efd96b1a45a54d9c56145380e811ad794c` и для Domum runtime НЕ использован. Harness удаляет Domum build/classes и build/resources из classpath, убирает MOD_CLASSES/MOD_FOLDERS; загрузчик подтверждает `domum_ornamentum (jar(mods/...-main.jar))`. Manifest и classpath сохранены. SEMI грузится из координатного jar (realpath build-farm m2), SHA указан в manifest.

В Mod List клиента и сервера присутствуют все пять:

| Мод | Версия |
|---|---|
| BlockUI | 0.0.1-26.1.2 |
| Domum Ornamentum | 1.0.0-local |
| MineColonies | 1.1.1397-26.1.2 |
| Structurize | 0.0.1-26.1.2 |
| SEMI (emi) | 1.1.24-semi151a6c88+26.1.2+neoforge |

## Runtime-доказательства

Изолированные harness/world/mods/logs находятся только в своей песочнице. Порты 29361/29362 проверены свободными; финальный сервер привязан к 127.0.0.1. Собственный Xvfb :131 проверен по PID/cwd/cmdline; чужие дисплеи и процессы не останавливались. Собственные серверы остановлены RCON, клиенты — после серверного сохранения проверенным по cwd/cmdline SIGTERM. Собственный Xvfb также остановлен после QA.

Финальные `server/final-smoke.log` и `server/final-reopen.log` содержат Done, штатную остановку и последующее All dimensions are saved. После SAVE+REOPEN `final-reopen-proof.log` подтверждает textureData, primaryBlock/secondaryBlock dynamic timberframe, Town Hall с colony=1, playerGameType=1 и Peaceful. `/reload` и save-all flush подтверждены ответами RCON; F3+T — второй Reloading ResourceManager в клиентском логе. После них и после нового запуска клиента материалы и GUI сохранились.

Кадры лично просмотрены:

- [Steep и steep_lower с материалами](evidence/client/steep-final.png).
- [Allbrick: алмазная ступень, кирпич, mortar-overlay](evidence/client/allbrick-final.png); также виден предмет allbrick в руке на следующем кадре.
- [Dynamic timberframe: кирпич/листва и каменная рама](evidence/client/timber-final.png).
- [GUI здания Town Hall](evidence/client/townhall-building-gui-final.png): колония действительно создана через Covenant, сервер записал New Colony Id: 1 by DomumQA.
- [После /reload и F3+T](evidence/client/after-reload-final.png).
- [Модели после повторного входа](evidence/client/reentry-models-final.png) и [повторно открытый GUI Town Hall](evidence/client/reentry-townhall-gui.png).

Tint подтверждён визуально зелёной oak-листвой в steep/item и заполнении dynamic timberframe; рядом размещена vanilla oak-листва для сравнения. Различные материалы подтверждены NBT команд сцены и кадрами. Это проверка указанной сцены, не исчерпывающий тест всех блоков/биомов/поворотов/ресурспаков.

## Итоговые счётчики и остатки

[Полные счётчики](evidence/final-evidence/log-counters.json): в обоих финальных клиентских логах **FATAL=0, InjectionError/InvalidInjection=0, Domum FailedModel=0, Domum missing model/texture=0**. В финальных серверных логах эти же ошибки отсутствуют. `git diff --check` — 0; контроль Kotlin/JEI/REI/cloth/architectury runtime — пустой. HEAD не менялись.

Остались внешние MineColonies blockrack FailedModels: семь моделей при каждой загрузке ресурсов (14 записей в qa-final из-за дополнительного F3+T, 7 при повторном входе). Они используют старый Domum geometry loader и видны уже в baseline; MineColonies не редактировался. Также есть missing `minecraft:builtin/entity`, сообщения EMI о синтетических/отсутствующих рецептах MineColonies и многочисленных переводах, известные OpenAL/X11/Netty-kqueue/DebugFile сообщения среды. Полные логи и отдельные сырые ERROR-выборки сохранены, не скрыты.

Собственные локализационные остатки Domum: EMI сообщает Untranslated recipe category `domum_ornamentum:architects_cutter`, имя steep в /give выводит ключ перевода. Это не исправлялось в задаче миграции моделей. Отсутствие всех ERROR не заявляется. Полный перебор всех shapes/offsets/biomes и длительный игровой soak не проводились и не подменяются этим QA.
