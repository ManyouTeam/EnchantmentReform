# 🧱 方块与世界能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `nearby_block`
* `break_blocks`
* `break_block`
* `prevent_block_break`
* `replace_block`
* `change_block_face`
* `grow_crop`
* `accelerate_crops`
* `accelerate_work_blocks`
* `scan_blocks`
* `locate_structure`
* `locate_biome`

---

## `nearby_block`

**用途：**按照与解析中心的距离顺序，为附近方块执行子能力。

**上下文：**需要能够解析出的世界位置和嵌套能力。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `1` | 三个坐标轴共同使用的默认半径，最小值为 0。 |
| `radius-x / radius-y / radius-z` | `radius` | 各坐标轴分别使用的半径。 |
| `max-blocks` | `0` | 最多处理的方块数量；零表示无限制。 |
| `abilities` | 必填 | 以每个方块作为上下文执行的子能力。 |

### 示例

```yaml
type: nearby_block
radius-x: 3
radius-y: 1
radius-z: 3
max-blocks: 16
abilities:
  grow:
    type: grow_crop
    stages: 1
```

### 行为与限制

* 会按照由近到远的顺序访问长方体范围内的每个方块；此能力本身不会筛选材质。
* 结果会记录执行子能力后实际发生变化的方块数量。

---

## `break_blocks`

**用途：**选择并筛选一组有数量限制的方块，然后分批将其破坏。

**上下文：**需要作为 `breaker` 的玩家、起点方块或位置、工具上下文，以及保护插件许可。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `breaker` | `SOURCE` | 玩家选择器，可填写 `SOURCE`、`SKILL` 或 `TARGET`。 |
| `center` | `EVENT_BLOCK` | 起点选择器，可填写 `EVENT_BLOCK`、实体角色或解析出的位置。 |
| `shape` | `CUBE` | 可填写 `CUBE`、`SPHERE`、`PLANE`、`LAYER`、`TUNNEL`/`LINE` 或 `VEIN`。 |
| `radius` | 由形状决定 | 通用半径，最大限制为 16。 |
| `radius-x/y/z` | `radius` | 立方体各坐标轴的半径。 |
| `vertical-radius` | `0` | `LAYER` 的垂直半径。 |
| `depth` | `0` | `PLANE` 的深度。 |
| `length` | `3` | `TUNNEL` 的长度，最大限制为 32。 |
| `size` | 未设置 | 仅接受奇数的立方体尺寸别名，内部会转换为半径。 |
| `max-blocks` | 全局限制 | 当前能力的数量上限；`VEIN` 默认为 64，并始终受全局配置限制。 |
| `include-origin` | `false` | 是否允许包含起点方块。 |
| `same-type` | `false` | 是否要求候选方块与起点材质相同。 |
| `diagonal` | `true` | 连锁搜索时是否使用 26 个相邻方向，而不是只使用六个正方向。 |
| `load-chunks` | `false` | 是否允许处理尚未加载区块中的方块。 |
| `whitelist / blacklist` | 空 | 由 `BlockPriceUtil` 解析的方块规格。 |
| `use-default-blacklist` | `true` | 是否排除受保护的坚硬方块或容器，例如基岩、屏障、传送门框架、强化深板岩、刷怪笼和箱子。 |
| `require-sneaking` | `false` | 是否要求破坏者正在潜行。 |
| `disable-when-sneaking` | `false` | 破坏者潜行时是否跳过执行。 |
| `mode` | `NATURAL` | `NATURAL` 使用 `breakNaturally(tool)`；`PLAYER` 使用 `Player#breakBlock`。 |
| `blocks-per-tick` | `16` | 每个 Tick 处理的批次数量，最小值为 1。 |
| `before-abilities` | 空 | 每次尝试破坏方块前执行的子能力。 |
| `abilities` | 空 | 每次成功破坏方块后执行的子能力。 |

### 示例

```yaml
type: break_blocks
breaker: SOURCE
center: EVENT_BLOCK
shape: VEIN
same-type: true
diagonal: true
include-origin: false
max-blocks: 32
blocks-per-tick: 8
whitelist:
  - DIAMOND_ORE
  - DEEPSLATE_DIAMOND_ORE
abilities:
  particles:
    type: particle
    particle: END_ROD
```

### 行为与限制

* 每个候选方块都会经过保护插件挂钩检查；空气和无效方块会被跳过。
* 全局配置 `powers.break-block.max-blocks-per-activation` 的默认上限为 512。
* 在 Folia 上，自然破坏会按照方块所在区域调度；玩家模式使用内部破坏递归标记。
* 每次自然破坏都会克隆工具。

---

## `break_block`

**用途：**使用自然破坏或玩家破坏行为，破坏当前上下文方块。

**上下文：**需要 `BLOCK`、作为破坏者的玩家、上下文物品或工具，以及保护插件许可。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `breaker` | `SOURCE` | 玩家选择器，可填写 `SOURCE`、`SKILL` 或 `TARGET`。 |
| `mode` | `NATURAL` | 自然破坏别名或玩家破坏别名。 |

### 示例

```yaml
type: break_block
breaker: SOURCE
mode: PLAYER
```

### 行为与限制

* 自然模式会使用克隆后的上下文物品调用 `breakNaturally`。
* 玩家模式使用 `Player#breakBlock` 和内部递归标记。
* 破坏前会检查保护插件挂钩。

---

## `replace_block`

**用途：**根据按顺序检查的“来源方块到目标方块”映射，替换当前上下文方块。

**上下文：**需要 `BLOCK` 和非空的 `replacements` 部分。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `replacements` | 必填 | 映射的键为来源方块规格，值为目标方块规格。 |

### 示例

```yaml
type: replace_block
replacements:
  WHEAT: AIR
  '#minecraft:logs': OAK_PLANKS
```

### 行为与限制

* 使用 `BlockPriceUtil` 选择第一个匹配的来源规格。
* 无效、不是字符串或内容为空的映射条目会被忽略。

---

## `change_block_face`

**用途：**修改方向型或可旋转方块数据的朝向或旋转角度。

**上下文：**需要 `BLOCK`；存在玩家时会检查保护插件的使用许可。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `mode` | `FIXED` | `FIXED` 使用 `face`；`NEXT`、`CYCLE` 和 `NEXT_AVAILABLE` 会旋转到下一个受支持的方向。 |
| `face` | `NORTH` | 固定模式使用的 Bukkit `BlockFace`。 |
| `apply-physics` | `false` | 设置方块数据时是否应用物理更新。 |
| `abilities` | 空 | 成功修改后执行的子能力。 |
| `failure-abilities / else-abilities` | 空 | 无法应用修改时执行的子能力。 |

### 示例

```yaml
type: change_block_face
mode: NEXT
apply-physics: false
abilities:
  sound:
    type: sound
    sound: BLOCK_LEVER_CLICK
```

### 行为与限制

* 方向型方块只接受其支持的朝向；可旋转方块使用 16 个水平方向旋转值。
* 成功修改会增加已更改方块结果计数器。

---

## `grow_crop`

**用途：**让实现了年龄数据的上下文方块向最大生长阶段推进。

**上下文：**需要 `BLOCK`，且其方块数据必须实现 Bukkit `Ageable`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `stages` | `1` | 增加的生长阶段数量，最小值为 1。 |

### 示例

```yaml
type: grow_crop
stages: 2
```

### 行为与限制

* 生长阶段不会超过该方块的最大年龄。
* 不支持年龄数据的方块会被跳过。

---

## `accelerate_crops`

**用途：**按距离扫描附近方块，并使尚未成熟的年龄型作物生长。

**上下文：**需要能够解析出的世界位置；存在玩家上下文时可检查保护插件的使用权限。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `1` | 三个坐标轴共同使用的默认半径，最大为 16。 |
| `radius-x / radius-y / radius-z` | `radius` | 各坐标轴分别使用的半径，最大为 16。 |
| `chance` | `100` | 每个候选作物生长的百分比概率，限制在 0 到 100。 |
| `stages` | `1` | 成功时增加的生长阶段，最小为 1。 |
| `max-blocks` | `128` | 单次最多处理的匹配作物数；`0` 禁用，负数表示无限制。 |
| `load-chunks` | `false` | 是否允许扫描尚未加载的区块。 |
| `check-protection` | `true` | 存在玩家时，是否要求保护插件允许玩家使用该方块。 |

### 示例

```yaml
type: accelerate_crops
radius: 4
radius-y: 1
chance: 25
stages: 1
max-blocks: 128
```

### 行为与限制

* 支持小麦、胡萝卜、马铃薯、甜菜根、下界疣、可可豆、甜浆果丛、火把花作物和瓶子草作物，并跳过已经成熟的方块。
* 不模拟树苗、甘蔗、仙人掌或竹子等植物的随机刻行为。
* 实际改变的作物数量会写入 changed-block 结果。

---

## `accelerate_work_blocks`

**用途：**按距离扫描附近的熔炉类方块和酿造台，并推进其当前工作进度。

**上下文：**需要能够解析出的世界位置；存在玩家上下文时可检查保护插件的使用权限。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `1` | 三个坐标轴共同使用的默认半径，最大为 16。 |
| `radius-x / radius-y / radius-z` | `radius` | 各坐标轴分别使用的半径，最大为 16。 |
| `extra-ticks` | `1` | 每次执行额外推进的工作 Tick；`0` 禁用。 |
| `max-blocks` | `128` | 单次最多处理的已启用工作方块数；`0` 禁用，负数表示无限制。 |
| `load-chunks` | `false` | 是否允许扫描尚未加载的区块。 |
| `check-protection` | `true` | 存在玩家时，是否要求保护插件允许玩家使用该方块。 |
| `require-active` | `true` | 熔炉类需要燃料正在燃烧，酿造台需要拥有燃料。 |
| `furnace` | `true` | 是否处理普通熔炉。 |
| `smoker` | `true` | 是否处理烟熏炉。 |
| `blast-furnace` | `true` | 是否处理高炉。 |
| `brewing-stand` | `true` | 是否处理酿造台。 |

### 示例

```yaml
type: accelerate_work_blocks
radius: 5
radius-y: 2
extra-ticks: '{level}'
max-blocks: 128
```

### 行为与限制

* 支持熔炉、烟熏炉、高炉和酿造台；各类型可分别关闭。
* 能力只把进度推进至完成前一个 Tick，由服务端执行正常的配方结算。
* 不补充或延长燃料，也不会直接创建输出物品。
* 实际推进的工作方块数量会写入 changed-block 结果。

---

## `scan_blocks`

**用途：**扫描上下文方块或来源玩家周围的立方体，并可选择高亮匹配方块。

**上下文：**需要作为 `SOURCE` 的玩家；存在 `BLOCK` 时以该方块为中心，否则以玩家所在方块为中心。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `5` | 立方体半径，最小值为 1。 |
| `same-type` | `false` | 是否匹配中心方块材质，而不是使用默认的矿石和远古残骸规则。 |
| `max-results` | `256` | 最多高亮的匹配数量，最小值为 1。 |
| `highlight` | `true` | 是否在每个匹配方块处生成一个 `END_ROD` 粒子。 |

### 示例

```yaml
type: scan_blocks
radius: 6
same-type: false
max-results: 128
highlight: true
```

### 行为与限制

* 默认匹配材质名称以 `_ORE` 结尾的方块，以及 `ANCIENT_DEBRIS`。
* 扫描范围是完整立方体，因此计算成本会随半径的三次方增长。

---

## `locate_structure`

**用途：**寻找距离最近的已配置结构类型，并发送格式化结果消息。

**上下文：**需要作为 `SOURCE` 的玩家和 Paper 注册表访问能力。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `structures` | 空 | 结构类型键；未填写命名空间的值会使用 `minecraft`。 |
| `radius` | `32` | 传递给 Bukkit 的搜索半径，最小值为 1。 |
| `find-unexplored` | `false` | 是否只搜索尚未探索的结构。 |
| `found` | 空 | 成功消息，可使用 `{distance}`、`{direction}`、`{x}`、`{y}`、`{z}`、`{structure}` 和 `{structure_key}`。 |
| `not-found` | 空 | 搜索失败时的消息。 |
| `structure-names.<key>` | 注册表键 | 结构显示名称映射。 |
| `directions.north/south/east/west` | 英文键名 | 本地化方向文本。 |

### 示例

```yaml
type: locate_structure
structures:
  - minecraft:village
  - minecraft:ancient_city
radius: 100
find-unexplored: false
found: '<green>{structure} is {distance} blocks {direction} at {x}, {z}.'
not-found: '<red>No structure found.'
```

### 行为与限制

* 会分别搜索每一种配置的结构，并选择距离最近的结果。
* 世界生成搜索可能消耗较多性能，建议配置冷却时间。

---

## `locate_biome`

**用途：**寻找距离最近的已配置生物群系，并发送格式化结果消息。

**上下文：**需要作为 `SOURCE` 的玩家和 Paper 注册表访问能力。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `biomes` | 空 | 生物群系注册表键。 |
| `radius` | `1024` | 最大搜索半径，最小值为 1。 |
| `horizontal-interval` | `32` | 水平采样间隔，最小值为 1。 |
| `vertical-interval` | `64` | 垂直采样间隔，最小值为 1。 |
| `found` | 空 | 成功消息，可使用距离、方向、坐标、生物群系名称和键。 |
| `not-found` | 空 | 搜索失败时的消息。 |
| `biome-names.<key>` | 注册表键 | 生物群系显示名称映射。 |
| `directions.*` | 英文键名 | 本地化方向文本。 |

### 示例

```yaml
type: locate_biome
biomes:
  - minecraft:cherry_grove
radius: 1024
horizontal-interval: 32
vertical-interval: 64
found: '<green>{biome} is {distance} blocks {direction}.'
not-found: '<red>No biome found.'
```

### 行为与限制

* 无效或重复的生物群系键会被忽略。
* 大范围搜索可能消耗较多性能，建议配置冷却时间。

---

---

## `prevent_block_break`

**用途：**在指定 tick 数内阻止玩家破坏指定位置的方块，可用于补种、临时建筑及其他放置能力。只有显式执行该能力才会创建保护。

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `location` | `CONTEXT` | 使用通用位置选择器及 `location.offset.x/y/z` 指定保护位置。 |
| `duration` | `20` | 保护时长，单位为服务器 tick，支持动态数值；必须大于 0。 |
| `scope` | `PLAYER` | `PLAYER` 仅阻止 `target` 选中的玩家；`ALL` 阻止所有玩家。 |
| `target` | `SOURCE` | `PLAYER` 范围的玩家选择器。 |
| `match-block` | `STATE` | `STATE` 在类型或方块数据变化后失效；`TYPE` 允许相同类型的方块数据改变。 |
| `abilities` | 空 | 注册保护后，以保护位置作为上下文执行的子能力。 |
| `failure-abilities` | 空 | 位置、玩家、参数无效或目标为空气时执行的子能力。 |

在 `use_on.success-abilities` 中，上下文位置是被点击的方块。补种时点击耕地或灵魂沙，因此使用 `location.offset.y: 1` 保护上方的新苗：

```yaml
success-abilities:
  protect-seedling:
    type: prevent_block_break
    target: SOURCE
    scope: PLAYER
    duration: 2
    match-block: STATE
    location:
      target: CONTEXT
      offset:
        y: 1
```

此能力取消保护期间匹配的 `BlockBreakEvent`，也包括玩家主动发起的破坏；不会区分重复数据包与新点击。保护不会阻止爆炸、踩踏、流体或物理更新，也不会撤销已经发生的破坏。相同位置、玩家及匹配方式的重叠保护不会缩短既有保护时间。保护到期自动清理，能力配置重载或插件关闭时全部清理。