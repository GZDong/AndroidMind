# AndroidMind

用 Kotlin、Jetpack Compose 和 Material 3 构建的个人学习手册。内容直接维护在代码里，无数据库、账号或在线编辑功能。支持浅色与深色外观。

## 目录结构

- `content/BookPage.kt`：目录、文章与正文块模型。
- `content/BookContent.kt`：Framework、WorkSkill 的内容入口及页面索引。
- `ui/AndroidMindApp.kt`：目录、阅读页面、标签切换与阅读历史。
- `ui/AndroidMindTheme.kt`：应用配色。

上述 Kotlin 文件位于 `app/src/main/java/com/gzd/androidmind/`。

## 增加内容

在 `BookContent` 对应的 `children` 中添加条目即可。现有内容为演示章节，可直接替换。

```kotlin
Directory(
    id = "threading",
    title = "线程与消息机制",
    children = listOf(
        Article(
            id = "handler",
            title = "Handler 机制",
            blocks = listOf(
                Heading("问题背景"),
                Paragraph("在这里编写正文。"),
                Code("val message = Message.obtain()"),
                Paragraph(
                    "延伸阅读：Binder 学习路线。",
                    links = mapOf("Binder 学习路线" to "binder-roadmap"),
                ),
                Link("查看完整路线", "binder-roadmap"),
                LinkButton("继续阅读", "binder-roadmap"),
            ),
        ),
    ),
)
```

`Directory.children` 可同时放文章与子目录，不设固定层级。页面 `id` 必须全局唯一，跳转使用目标页面的 `id`，因此修改标题不会破坏链接。应用初始化时会检查重复 ID、无效跳转与不存在的正文链接文字。

每个底部标签保存独立的阅读历史；返回会回到跳转前的页面与滚动位置。正文支持可复制的段落、标题、横向滚动代码块、行内划线链接、独立链接与按钮。

列表条目左右各留 12dp，相邻条目之间的垂直间距为 12dp。图标为书页与阶梯，使用 Android 矢量资源，便于后续调整。
