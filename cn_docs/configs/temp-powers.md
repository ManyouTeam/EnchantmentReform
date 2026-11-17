# ⏳ 临时能力

`temp_powers/` 中每个 YAML 文件定义一种可通过指令授予玩家的能力。文件名（不含 `.yml`）为 ID，只能包含小写字母、数字、`_` 和 `-`。支持子目录，但 ID 不包含子目录名，因此不能重复。

```yaml
enabled: true
remember: false
default-level: 1
execution-priority: 0

variables:
  heal_amount: '1 + {level}'

powers:
  on-activate:
    abilities:
      message:
        type: send_message
        mode: target-player
        message: '&a已激活临时能力，等级：{level}'
```

配置加载后不会自动激活。通过 `/er temppower set Steve example 3` 授予后，玩家会以三级执行 `example.yml` 的能力。省略等级时使用 `default-level`（默认 `1`）；配置和指令中的等级都必须为正整数。重复设置相同 ID 会替换等级，不会叠加实例；相同等级重复设置不会重复执行激活事件。

`powers` 使用现有的触发器、条件、修饰器、限制、abilities 和等级变量格式。临时能力绑定玩家，不依赖装备，也不依赖技能模块。不要配置 `active-slots`。`on-activate` 在授予、改级后的激活，以及登录或重载后恢复时执行；`on-deactivate` 在撤销、清空、改级前的停用以及退出时执行，并清理该来源的持续属性效果。

## 数据保留

| `remember` | 插件重载或禁用后重新启用 | 玩家重新进入服务器 | 服务器重启 |
| --- | --- | --- | --- |
| `false`（默认） | 保留当前登录会话的授权及等级 | 清空 | 清空 |
| `true` | 保留 | 保留 | 保留 |

记忆只针对授权和玩家等级，冷却、计数等运行状态仍使用原有规则。数据存放在玩家 PDC；不记忆的数据附带服务器和玩家登录会话标记，即使玩家在插件被禁用期间重新登录，或服务器在未执行退出清理时重启，也不能恢复旧会话的临时授权。死亡、切换世界不会主动移除授权。

运行 `/er reload` 后，仍存在的能力保留玩家等级并使用新配置；删除或禁用的能力停止执行，并清理在线玩家的对应数据，离线玩家的数据在下次登录时清理。修改 `default-level` 不改变已经授予玩家的等级。修改 `remember` 时，当前登录会话中的有效授权按新策略保存；过期的非记忆授权不会因为改为 `true` 而恢复，离线的旧记忆授权也不会在改为 `false` 后恢复。

## 指令

```text
/er temppower set <玩家> <ID> [等级]
/er temppower remove <玩家> <ID>
/er temppower clear <玩家>
/er temppower list <玩家>
```

权限：`enchantmentreform.temppower`，默认仅管理员拥有。支持玩家、控制台、命令方块及其他插件派发命令，目标玩家必须在线。`list` 显示当前激活的 ID、等级和 `remember` 值。所有操作均支持对应参数补全。

例如任务插件开始任务时执行 `er temppower set Steve example 3`，任务结束时执行 `er temppower remove Steve example`。
