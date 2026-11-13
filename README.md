# ZoneZero Mech Combat｜零號禁區・機甲戰鬥

**Minecraft 1.21.1 / NeoForge / Sable 2.0.6 / Let It Walk! 0.1.5**

專為《零號禁區》製作的實驗性模組，目標是讓殭屍能攻擊玩家組裝的步行機甲，並逐步加入機甲裝甲耐久、維修與受損回饋。

## 開發狀態

**尚未提供可供正式伺服器安裝的穩定 JAR。** 開發內容先放在獨立分支，經過編譯與遊戲實測後再合併。

### 目標

- 沿用 [Sable Mob Interactions](https://github.com/eviesaurr/sable-mob-interactions) **v1.0.1** 的怪物攻擊活動結構機制
- 只讓含 `let_it_walk:walker_core` 的步行機甲成為攻擊目標，避免其他動態建築被破壞
- 不加入敵對車輛、載具 AI、敵方據點或額外車輛模組依賴
- 兼容現有《零號禁區》殭屍生成、五區強化與安全區
- 後續逐步加入機甲 HP、材質防護與維修

## 依賴

預定以 NeoForge 1.21.1、Java 21、Sable 2.0.6、Create 6.0.10 和 Let It Walk! 0.1.5 作為整合測試目標。**完整編譯與遊戲相容性尚待確認。**

## 原作與授權

以 Evie 的 [Sable Mob Interactions](https://github.com/eviesaurr/sable-mob-interactions) **v1.0.1** 程式碼為基礎。原專案根目錄的 `LICENSE` 採 MIT 授權，衍生版將保留原作者的著作權與授權聲明。原始程式與改動將在開發分支中明確區分。

本專案為社群衍生作品，非 Sable、Create、Let It Walk! 或原作者的官方版本。
