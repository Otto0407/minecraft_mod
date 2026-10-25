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
  - [x] 殭屍娘：基本近戰 AI、生怪蛋、測試用材質（特殊技能待設計）
- [ ] 第 2 部分：試煉鑰匙與召喚券物品
- [ ] 第 3 部分：試煉結構與試煉流程
- [ ] 第 4 部分：友好夥伴與指令系統
- [ ] 第 5 部分：各種工作 AI（種田、剪羊毛、戰鬥……）

## 建置

需要 JDK 21。

```bash
./gradlew build        # 產生 build/libs/monstergirlcompanions-<版本>.jar
./gradlew runClient    # 啟動開發用客戶端
```

Windows 請用 `gradlew.bat`。

## 模型與材質（Blockbench）

`blockbench/littlemaid_models/` 是從 LittleMaidModelLoader（LMML 1.18.2-4.6.7）原樣提取的 12 種女僕模型，
可直接用 [Blockbench](https://www.blockbench.net/) 開啟：

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

選好模型、在 Blockbench 修改後，`File > Export > Export Java Entity`（Mojmaps），
把產生的 `.java` 和材質交給 Claude 接進遊戲。

## 授權

本模組使用了 LittleMaid 系列（MMM、EMB4、Verclene、firis-games、Sistr）的模型與材質，
依照 [LittleMaid Licence](blockbench/littlemaid_textures/LICENCE.md)，**本模組只能用於非營利目的**。
