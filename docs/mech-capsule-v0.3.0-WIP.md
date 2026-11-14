# ZoneZero Mech Combat — 專用機甲收納器 / Capsule (0.3.0 WIP)

**階段：開發用原始碼（非可安裝模組）**。現行 `0.2.3-alpha-StompFix-TEST.jar` 尚未加入機甲收納功能。請勿將此 source-only 工程放入 Minecraft `mods` 資料夾。

## 方案 B 的目標

- 道具 `zonezero_mechcombat:mech_capsule`：空收納器右鍵機甲核心收納，已裝載的收納器右鍵地面部署；數量上限 1。
- 保留完整連結結構：主機身、左右腳、機械方塊、方塊實體、儲物箱資料、液體與 `user_data`（HP）。
- 僅允許停車、落地、不在移動、沒有乘員且沒有抓取外部貨物的有效機甲收納。
- 由**伺服器**持有權威性的機甲存檔；物品只持有一次性的 capsule UUID，複製道具不能複製機甲。
- 收納及部署採交易狀態機，不成功就拒絕或隔離等待管理員確認；不能偷偷清空資料。

## 已做好的程式碼

- `CapsuleLedger.java`：純 Java 交易狀態機、擁有者驗證、重複 ID 防護、重複部署防護、I/O 失敗封閉與隔離機制。
- `CapsuleLedgerTest.java`：21 項 Java 21 測試已通過。
- `CapsuleDiskStore.java`：伺服器世界資料夾中的原子式快照／交易檔寫入、CRC32 校驗、跨重啟讀取與單次 ID 防覆寫。
- `CapsuleDiskStoreTest.java`：7 項 Java 21 磁碟持久化測試已通過（總計 28 項）。
- `SableSnapshotCollector.java`：根據你現有 0.2.3 的 `WalkerLocator.Walker`，讀取 Sable 物理連結群組及完整 `SubLevelSerializer` 快照（**尚未於 NeoForge 環境編譯測試**）。只讀取、不移除任何方塊。

## 尚未完成；不得直接發行遊戲版 JAR

1. **連結持久化寫入與完整 Sable NBT 壓縮／解壓的遊戲端流程**，目前底層儲存及校驗已完成，但還沒有與模組掛鉤。
2. 將多個 Sable plot 的載入、放置與物理連結安全還原；必須解決重新部署時原 plot 座標已被其他物件佔用的問題。
3. 完整處理不在 Sable plot NBT 裡的外部資料、乘客／附著實體與跨維度情況。跨維度初版禁止。
4. 操作前完成玩家／乘客、空間、世界邊界、物件大小、伺服器重啟等驗證。
5. 接入 NeoForge 物品註冊、互動事件、合成配方、介面文字與 16×16 / 32×32 像素材質。
6. 以 NeoForge 1.21.1 + Create 6.0.10 + Let It Walk! 0.1.5 + Sable 2.0.6 完整編譯，於副本世界測試收納後重登、斷電與部署。

## 核心安全規則

依序「資料快照 → 寫入持久檔／交易紀錄 → 驗證存檔 → 移除全部機械子結構 → 確認不存在 → 給予已裝載收納器」。部署則「確認唯一有效收納器 → 預留位置 → 啟動部署交易 → 恢復全部子結構 → 驗證關係、內容和 HP → 將交易標記為 DEPLOYED → 把收納器改回空的」。失敗則隔離，不允許玩家藉由重複使用同一張道具生成複本。

## 執行交易測試

```bash
mkdir -p build/classes
javac -d build/classes src/main/java/zonezero/mechcombat/capsule/CapsuleLedger.java src/main/java/zonezero/mechcombat/capsule/CapsuleDiskStore.java src/test/java/zonezero/mechcombat/capsule/CapsuleLedgerTest.java src/test/java/zonezero/mechcombat/capsule/CapsuleDiskStoreTest.java
java -cp build/classes zonezero.mechcombat.capsule.CapsuleLedgerTest
java -cp build/classes zonezero.mechcombat.capsule.CapsuleDiskStoreTest
```

## 2026-11-14 — 0.3.1-alpha ToolsPreview TEST

實作：
- 新增 `mech_capsule` 物品與 32×32 原創材質，安全預覽模式：目前右鍵僅顯示提示，不會拆機甲、收納或部署。
- 保留既有 `repair_tool` ID；改為無耐久、右鍵持續動作、每 60 ticks +150 HP、最遠 1.5 格，使用 BRUSH 及每 12 ticks 揮手作為棘輪動作預覽。
- 加入獨立創造分頁「零號禁區・機甲戰鬥」及原創橘黑徽章，收納器與板手皆可從分頁取得。
- 既有殭屍攻擊、HP、HUD、踩踏類別不更動。
- 新增獨立開發用收納政策：4,096 blocks / 32 SubLevels / 32 blocks per axis / 16 MiB archive，部署前不可覆蓋世界方塊。
- 已通過純 Java 測試：維修 10、交易 21、磁碟 7、收納／空間 14，合計 52 個斷言。JAR 只完成離線編譯與 ZIP 結構檢查，尚未於 NeoForge 實際遊戲啟動驗證。

⚠️ **尚未完成地上收納器 GUI、可用的多 SubLevel 收納／還原、真正客製的轉螺絲手部骨骼動畫；不可把本階段稱為完整收納模組。**

開發原始碼與測試包已作為本次對話附件交付，GitHub 本分支目前記錄設計與驗證進度，尚未同步所有生成的 Java source。


## TaCZ 1.2.0 NPC bullet damage — integration requirement (2026-11-14)
- **Current 0.3.1 status: NOT IMPLEMENTED.** Bytecode review: `SubLevelDummyTargetEntity.hurt(DamageSource,float)` delegates to `BlockBreakTracker.registerHit(subLevel,localPos,attacker)`, which uses the attacking entity's `Attributes.ATTACK_DAMAGE` rather than the TaCZ bullet's float damage; this does **not** establish correct ranged weapon damage.
- Exact user build inspected: `tacz-neoforge-1.21.1-1.2.0-ZoneZeroBalance-TagFix(3)(4).jar`; TaCZ exposes `com.tacz.guns.api.event.server.AmmoHitBlockEvent` (with `getAmmo()`, `getHitResult()`, `getLevel()`), `EntityHurtByGunEvent.Pre` (with `getBullet()`, `getHurtEntity()`, `getBaseAmount()`), and `EntityKineticBullet.getDamage(Vec3)`.
- Intended: apply server-validated gun hit on **only** Sable sub-levels belonging to `let_it_walk:walker_core`, resolve through `WalkerLocator`, then call `MechHealth.damage` once per projectile / walker to the shared HP pool; reject duplicate hits, inactive/broken mechs, self/ally fire if configured; preserve 6 tick hit cooldown semantics and include a configurable incoming gun damage multiplier and explosive rules.
- Important unknown requiring in-game verification: whether moving Sable sub-level impacts emit `AmmoHitBlockEvent` (TaCZ only fires it in its `EntityKineticBullet.onHitBlock` path). Do not assume static-world block hit callbacks include physical moving mech geometry; consider Sable raycast against sub-levels if not.
- **Do not ship or advertise working gun damage until integration & actual TaCZ NPC firing tests succeed**. Regression checks: distinct shots, shotgun pellets, burst fire, piercing ammo, NPC vs player/ally shooter, explosive damage, unloaded/reloading mech, already-dead mech, and performance.
