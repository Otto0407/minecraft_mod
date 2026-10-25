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

魔物娘的身體使用 LittleMaid（女僕模組）預設的「Orign」模型，從 LittleMaidModelLoader 提取而來。

- `blockbench/littlemaid.bbmodel`：原版女僕模型 + 原版材質，用來參考。
- `blockbench/zombie_girl.bbmodel`：殭屍娘（目前是女僕材質把膚色換成綠色）。
- `blockbench/littlemaid_textures/`：女僕模組內建的 16 色女僕材質（64×32），可以當作畫材質的底稿。

用 [Blockbench](https://www.blockbench.net/) 開啟 `.bbmodel` 修改：

- **只改材質**：`File > Export > Export Texture`，覆蓋
  `src/main/resources/assets/monstergirlcompanions/textures/entity/monster_girl/zombie_girl.png`。
- **改了模型形狀**：`File > Export > Export Java Entity`（Mojmaps），把產生的 `.java` 交給 Claude 接上。
  請保留 `head`、`hat`、`body`、`right_arm`、`left_arm`、`right_leg`、`left_leg` 這幾個群組名稱，
  遊戲的動作（走路、揮手）是靠這些名稱找到部位的。

`.bbmodel` 與殭屍娘測試材質由 `python3 tools/gen_maid_assets.py` 產生（需要 Pillow）；
在 Blockbench 修改過後請不要再跑這個腳本，否則會被覆蓋。

## 授權

本模組使用了 LittleMaid 系列（MMM、EMB4、Verclene、firis-games、Sistr）的模型與材質，
依照 [LittleMaid Licence](blockbench/littlemaid_textures/LICENCE.md)，**本模組只能用於非營利目的**。
