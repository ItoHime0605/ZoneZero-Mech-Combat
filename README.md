# ZoneZero Mech Combat｜零號禁區・機甲戰鬥

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Minecraft 1.21.1 / NeoForge / Sable 2.0.6 / Let It Walk! 0.1.5**

專為《零號禁區》製作的實驗性模組，目標是讓殭屍能攻擊玩家組裝的步行機甲，並逐步加入機甲裝甲耐久、維修與受損回饋。

## 開發狀態

**v0.2.1-alpha 正在加入駕駛用右上角 HP 圖示，仍屬實驗性版本。** Java/Gradle 編譯結果與遊戲內相容性需以最新版 GitHub Actions 與實測為準；勿直接替換正式世界。詳見 [BUILDING.md](BUILDING.md)。

### 目標

- 沿用 [Sable Mob Interactions](https://github.com/eviesaurr/sable-mob-interactions) **v1.0.1** 的怪物攻擊活動結構機制
- 只讓含 `let_it_walk:walker_core` 的步行機甲成為攻擊目標，避免其他動態建築被破壞
- 不加入敵對車輛、載具 AI、敵方據點或額外車輛模組依賴
- 兼容現有《零號禁區》殭屍生成、五區強化與安全區
- 已加入按足部材料加總 HP、共用血條、受擊火花、移速限制與維修工具的開發實作

## 依賴

預定以 NeoForge 1.21.1、Java 21、Sable 2.0.6、Create 6.0.10 和 Let It Walk! 0.1.5 作為整合測試目標。**完整編譯與遊戲相容性尚待確認。**

## 原作與授權

以 Evie 的 [Sable Mob Interactions](https://github.com/eviesaurr/sable-mob-interactions) **v1.0.1** 程式碼為基礎。原專案根目錄的 `LICENSE` 採 MIT 授權，衍生版將保留原作者的著作權與授權聲明。原始程式與改動將在開發分支中明確區分。

本專案為社群衍生作品，非 Sable、Create、Let It Walk! 或原作者的官方版本。

## v0.2 機甲共用 HP — 開發規格

| 狀態 | 行為 |
|---|---|
| HP 50% 以上 | 機甲正常 |
| HP 低於 50% | 機體中心冒出電火花 |
| HP 低於 20% | 機甲步行速度乘 0.45（可調） |
| HP 等於 0 | 輸入、衝刺、飛行及推進停止，保留機甲零件 |
| 持維修工具右鍵 | 每次恢復 30 HP，消耗 1 點工具耐久 |

每條腿所有已載入的 Sable 子結構方塊都會累加最大 HP，包含一般足、噴射足、攀爬足、鉸鏈及材質方塊。不同材質可透過 `config/zonezero_mechcombat-common.toml` 的 `health.materialHp` 設定。**不計算駕駛座或機身方塊**；攻擊任何屬於同一機甲的段落皆扣同一份 HP。拆裝／增減零件會按原有 HP 百分比重新換算，不會免費修好機甲。

已通過 Java 21 + NeoForge Gradle 編譯，但仍需要遊戲內驗證：

- Sable 多個活動子結構的識別和 HP 存檔
- 動態方塊的滑鼠右鍵是否能觸發維修工具
- 歸零後的實際站姿／落地物理表現（目前為強制停止操作，尚未保證會翻倒）
- 多殭屍下的區塊掃描成本與 Mixin 注入是否正確

## v0.2.1 — 右上角機甲 HP 狀態圖示

- 顯示時機：使用 Let It Walk! 駕駛／遠端操作機甲，從伺服器控制封包同步**正在操作的那台**機甲 HP。
- 外觀：右上角、接近藥水效果圖示的小型原創像素機械盾牌；左側小字顯示剩餘 HP 百分比。
- 76–100%：綠；51–75%：黃；21–50%：橘；0–20%：紅（包含 0%）。
- 未操作機甲、遊戲 HUD 隱藏或超過約 2.5 秒沒有收到操作同步訊息時自動隱藏。
- 對外只同步當前 HP／最大 HP 的兩個整數；顏色、外觀和座標由玩家端繪製。
- 這是獨立顯示 UI，不會調整殭屍傷害、裝甲材質 HP 或維修量。

_編譯成功也不代表 Mixin、客戶端圖層與專用伺服器同時驗收完成，需遊戲實測。_

## v0.2.2-alpha — Zombie swarm durability and 0-HP target fix

- Material-based max HP is now **4×** (new `health.maxHpMultiplier=4.0`), retaining any existing per-block material values. Previously damaged walkers keep their HP *percentage* when migrated.
- Incoming hit damage is additionally multiplied by **0.6** (new `health.incomingDamageScale`), with a **15 HP cap per hit** (new `health.maxDamagePerHit`).
- Zombie attack cooldown is now **20 ticks** instead of 10 (new `zombie.attackCooldownTicks`).
- **HP 0 disables mob aggression to that walker**: search, cached target selection, ongoing goals, navigation, native dummy targets and damage handler all check remaining HP.
- Once a player repairs the mech above 0 HP, it can be targeted normally again.
- No changes to zombie health, damage against players, spawning, five-zone difficulty scaling, or protected base zones.
- Changing the defaults does not overwrite the existing `health.damageMultiplier` and `health.materialHp` values in the user's existing TOML.

**Upgrade note:** back up your world and configuration before updating the client and server JAR. This is a compile-tested experimental mod, not yet validated in live multiplayer.
