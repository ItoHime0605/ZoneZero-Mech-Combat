# ZoneZero Mech Combat｜零號禁區・機甲戰鬥

**Minecraft 1.21.1 / NeoForge 21.1.x / Java 21 / Create 6.0.10 / Sable 2.0.6 / Let It Walk! 0.1.5**

《零號禁區》的二足步行機械戰鬥擴充。以 [Sable Mob Interactions v1.0.1](https://github.com/eviesaurr/sable-mob-interactions/tree/v1.0.1)（MIT）為起點，只針對已組裝的 Let It Walk! 步行機甲添加受敵對生物攻擊、共用耐久、維修、受損顯示及步行腿部碰撞。

## 開發狀態（2026-11-14）

- **最新已找回的測試成品：`ZoneZero-Mech-Combat-0.2.3-alpha-StompFix-TEST.jar`**（JAR 內部版本 `0.2.3-alpha`）。
- 已從 JAR 確認程式碼包含下方功能；以 Java 21 直接對實際 JAR 執行 14 項純數值 smoke tests 全部通過。
- **尚未完成 Minecraft 多人伺服器實測；此版本僅供測試，不應視為穩定版。**
- **重要：目前 GitHub repository 只有開發說明，尚未同步先前編譯所使用的完整 Java／Gradle 專案。** 請勿將此 README 當作可直接從 GitHub 編譯 JAR 的保證。

## 現有功能（0.2.3 JAR 靜態確認）

- 只識別以 `let_it_walk` Walker Core 組裝的機甲，不攻擊所有 Sable 移動結構。
- 依足部結構方塊材質計算整台機甲的**共用 HP**，預設最大 HP 倍率 **×4**（有上限）。
- 同一機甲的傷害共用 **6 ticks** 受擊保護；原版殭屍攻擊間隔預設 **20 ticks**（1 秒）。
- **HP <50%**：火花；**HP <20%**：機動速度降低；**HP =0**：停機報廢並取消被追擊的攻擊目標；維修工具可恢復 HP。
- 右上角的機甲 HP HUD 顯示四級圖示，從綠、黃、橘到紅。
- **移動腿部**掃過敵對怪物時，可造成傷害與擊退；非移動、過期取樣、同一敵人的連續踩踏有防重複處理機制。
- 主要數值可透過 NeoForge 設定檔調整。

## 安裝與測試注意

需在**伺服器與玩家端同時安裝**本模組及相容的必要前置模組。若已安裝舊版 `ZoneZero-Mech-Combat`，請先移除舊檔，不能與此版並存。

待遊戲實測：
1. 兩足、四足、多種腿部材質的 HP 加總，重組或更換方塊後是否正確更新。
2. 大量殭屍圍攻時的共用冷卻、耐久、索敵，以及 0 HP 後取消目標。
3. 機甲開始步行、停步、原地轉向、跨坡及腿部移動踩踏時的碰撞和擊退。
4. 客戶端 HUD、連線重登及維修後血量恢復。
5. 與 Create Big Cannons、Fire Control 等其他武裝模組的**可選整合**；現階段尚未直接支援自動炮塔或 TaCZ 自動射擊。

## 不納入範圍

不新增敵對車輛、額外載具生成、載具 AI 駕駛員或敵方據點，並保留《零號禁區》原有的殭屍生成、安全區與分區強化機制。

## 原作與授權

衍生自 Evie 的 [Sable Mob Interactions v1.0.1](https://github.com/eviesaurr/sable-mob-interactions/tree/v1.0.1)，MIT 授權。最終發布完整源碼時需一併保留原 LICENSE 與作者權利聲明。

此專案為非官方社群作品，與 Sable、Create、Let It Walk! 及其作者無官方關係。
