# ZoneZero Mech Combat｜零號禁區・機甲戰鬥

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Minecraft 1.21.1 / NeoForge / Sable 2.0.6 / Let It Walk! 0.1.5**

專為《零號禁區》製作的實驗性模組，目標是讓殭屍能攻擊玩家組裝的步行機甲，並逐步加入機甲裝甲耐久、維修與受損回饋。

## 開發狀態

**v0.2 共用機甲 HP 原始碼已加入，但尚未提供可安裝的 JAR。** 此分支未經 Gradle 編譯與遊戲實測，請勿放入正式伺服器。詳見 [BUILDING.md](BUILDING.md)。

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

這是待編譯原始碼。特別需要遊戲內驗證：

- Sable 多個活動子結構的識別和 HP 存檔
- 動態方塊的滑鼠右鍵是否能觸發維修工具
- 歸零後的實際站姿／落地物理表現（目前為強制停止操作，尚未保證會翻倒）
- 多殭屍下的區塊掃描成本與 Mixin 注入是否正確
