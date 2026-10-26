# Monster Girl Companions（魔物娘夥伴）

Minecraft **1.21.1** / **Forge 52.x** 模組（Fabric 兼容視情況再決定）。

## 玩法概要

1. 世界中生成**試煉挑戰結構**。
2. 從戰利品箱中隨機取得**試煉鑰匙**，在結構上使用即可啟動試煉。
3. 試煉中會出現**敵對新實體**攻擊玩家；擊敗後掉落**召喚券**。
4. 使用召喚券可召喚該實體的**友好版本**，並對它下達指令：種田、剪羊毛、戰鬥等工作。

## 開發進度

- [x] 專案骨架（Forge MDK、註冊表、創造模式分頁）
- [ ] 第 1 部分：敵對試煉實體（魔物娘）
  - [x] 共用框架（60 血小 boss、共用人形模型與渲染器）
  - [x] 殭屍娘：自製模型、攻擊力 10、速度 0.3、殭屍增援技能（高度 ±8 格內）
- [x] 第 2 部分：試煉鑰匙與召喚鈴
  - 感染之鑰：地牢戰利品箱每箱 20%；玩家擊殺殭屍 1%（掠奪每級 +0.5%），屍殼、溺屍為殭屍的一半
  - 感染之鈴：擊敗殭屍娘必定掉落；使用後消耗，在玩家面前召喚屬於該玩家的友好殭屍娘
  - 友好殭屍娘（暫定）：跟隨主人、幫主人戰鬥、主人空手右鍵切換待命
- [x] 第 3 部分：試煉祭壇與試煉流程
  - 試煉核心合成：四角紫水晶碎片、上下左右地獄石、中央紅石方塊
  - 祭壇（玩家自己蓋，必須完全在 Y=40 以下）：

    ```
    第 4 層  紅石火把 . 紅石火把     （插在金磚上）
    第 3 層  金磚  試煉核心  金磚     （3×3 的四個角放金磚）
    第 2 層  3×3 紫水晶方塊
    第 1 層  5×5 紫水晶方塊
    ```
  - 對核心使用鑰匙 → 紫色血條「試煉：魔物娘名稱」5 秒內集滿 → 魔物娘出現、血條變成她的血量 → 她死亡時試煉結束
  - 試煉進行中同一個核心不能再開第二場
- [ ] 第 4 部分：友好夥伴與指令系統
- [ ] 第 5 部分：各種工作 AI（種田、剪羊毛、戰鬥……）

## 建置

需要 JDK 21。

```bash
./gradlew build        # 產生 build/libs/monstergirlcompanions-<版本>.jar
./gradlew runClient    # 啟動開發用客戶端
```

Windows 請用 `gradlew.bat`。

自動測試（鑰匙、鈴鐺、掉落物）：`./gradlew runGameTestServer`

## 魔物娘模型（Blockbench）

遊戲直接讀取 `src/main/resources/assets/monstergirlcompanions/bbmodel/<名稱>.bbmodel`（模型＋內嵌材質），
所以在 Blockbench 修改後**只要覆蓋這個檔案再重新建置**，程式碼不用改，碰撞箱高度也會自動跟著模型高度。

需要遵守的規則：

- 群組名稱 `head`、`body`、`right_arm`、`left_arm`、`right_leg`、`left_leg` 要保留，遊戲的走路、轉頭、揮手動作靠它們找部位。
  群組可以放在其他群組裡（例如 `limbs > arms > right_arm`）。
- 每個上述群組的**軸心（Pivot）要放在關節**：頭在脖子、手在肩膀、腳在髖部，否則動作會繞著錯的位置轉。
- `right_item`、`left_item` 是手持物品的掛載點（空群組，放在手掌位置，物品會從這點長出來）。
- 材質要存在 `.bbmodel` 裡（Blockbench 預設就會內嵌），只用第一張材質，並使用 Box UV。
- 隱藏（眼睛關掉）的方塊不會出現在遊戲裡，例如目前殭屍娘的 `skirt`。
- 碰撞箱高度＝最高方塊的頂端；眼睛高度＝`head` 群組的中間。

| 魔物娘 | 檔案 |
|---|---|
| 殭屍娘 | `bbmodel/zombie_girl.bbmodel` |

## LittleMaid 參考模型

`blockbench/littlemaid_models/` 是從 LittleMaidModelLoader（LMML 1.18.2-4.6.7）原樣提取的 12 種女僕模型，
可以當作製作新魔物娘的底稿：

| 檔案 | 身高 | 材質尺寸 | 特色 | 內嵌材質 |
|---|---|---|---|---|
| `Orign.bbmodel` | 1.35 格 | 64×32 | 經典小女僕（預設） | `default/mob_littlemaid_0c` |
| `Archetype.bbmodel` | 1.35 格 | 64×32 | 小女僕原型 | UV 色塊模板 |
| `Aug.bbmodel` | 1.35 格 | 64×32 | 雙馬尾＋呆毛 | `mmm_aug/aug_0c` |
| `SR2.bbmodel` | 1.35 格 | 64×32 | 小女僕變體 | `mmm_sr2/engageoctaver_0c` |
| `RX0.bbmodel` | 1.35 格 | 128×64 | 細節較多的小女僕 | UV 色塊模板 |
| `Elsa5.bbmodel` | 1.75 格 | 64×64 | 高挑女僕 | `e15color_elsa5/contract_c` |
| `Chloe2.bbmodel` | 1.80 格 | 128×64 | 高挑、有胸部 | `b16color_chloe2/contract_c` |
| `Beverly7.bbmodel` | 1.99 格 | 128×64 | 約兩格高、豐滿 | `b16color_beverly7/contract_c` |
| `Classic64.bbmodel` | 1.80 格 | 64×64 | 玩家（Steve）體型 | UV 色塊模板 |
| `Slim64.bbmodel` | 1.80 格 | 64×64 | 玩家（Alex）細手體型 | UV 色塊模板 |
| `Stef.bbmodel` | 1.80 格 | 64×32 | 女性玩家體型（有 bust） | `stef_stef/stef_00` |
| `Steve.bbmodel` | 1.80 格 | 64×32 | 舊版玩家體型 | `steve_steve/steve_00` |

- 姿勢是模型在遊戲中靜止站立時的樣子（例如 Chloe2、Beverly7 原本就是重心在單腳的站姿）。
- `ModelPlate`（薄片零件，例如 Aug 的頭髮）轉成厚度 0 的方塊，用單面 UV。
- `blockbench/littlemaid_textures/` 有 LMML 內建的全部材質包，資料夾名稱是「材質包_模型」，
  例如 `b16color_beverly7` 是 Beverly7 用的材質；`contract_*` 是契約後、`wild_*` 是野生時的顏色。
- 提取工具在 `tools/lmml_extract/`。

## 授權

本模組使用了 LittleMaid 系列（MMM、EMB4、Verclene、firis-games、Sistr）的模型與材質，
依照 [LittleMaid Licence](blockbench/littlemaid_textures/LICENCE.md)，**本模組只能用於非營利目的**。
