# AndroidMind

用 Kotlin、Jetpack Compose 和 Material 3 构建的个人学习手册。内容直接维护在代码里，无数据库、账号或在线编辑功能，支持浅色与深色外观。

## 内容结构

内容采用组合模式：目录和文章统一继承 `BookPage`，目录可以包含文章或其他目录。每个目录、每篇文章都有自己的类和文件，按主题放进独立包中。

以下目录位于 `app/src/main/java/com/gzd/androidmind/`：

```text
content/
├── BookPage.kt                      # 公共目录、文章父类
├── ContentBlock.kt                  # 公共正文块：段落、代码、链接等
├── BookContent.kt                   # 两个首页入口、页面索引与链接校验
├── framework/
│   └── FrameworkDirectory.kt
└── workskill/
    ├── WorkSkillDirectory.kt
    └── photocheating/
        ├── PhotoCheatingOverviewPage.kt
        └── PhotoCheatingDetectionPage.kt
ui/
├── AndroidMindApp.kt                # 公共页面渲染与导航
└── AndroidMindTheme.kt              # 配色及 4dp 圆角
```

`BookContent` 不编写正文，也不逐篇注册文章。它从两个根目录自动遍历下级目录、文章及文章关联页，建立索引，检查重复 ID 和无效链接。正文、主题目录都在各自文件维护。

## 增加内容

例如要增加 `fw → Binder → 调用过程`：

1. 在 `content/framework/binder/` 新建 `BinderDirectory.kt`，继承 `BookPage.Directory`。
2. 在同一包中新建 `BinderCallPage.kt`，继承 `BookPage.Article`。
3. 在 `BinderDirectory.children` 中引用 `BinderCallPage`。
4. 在 `FrameworkDirectory.children` 中引用 `BinderDirectory`。已有的目录直接复用。

文章类示例（新文件须在声明上方添加个人规则要求的作者、日期注释）：

```kotlin
object BinderCallPage : BookPage.Article() {
    const val ID = "framework-binder-call"
    override val id = ID
    override val title = "调用过程"
    override val blocks: List<ContentBlock> = listOf(
        ContentBlock.Heading("问题背景"),
        ContentBlock.Paragraph("在这里编写正文。"),
        ContentBlock.Code("val message = Message.obtain()"),
    )
}
```

子目录同样继承 `BookPage.Directory`，覆写 `id`、`title`、`children`。不设固定层级，目录文件只组合子页面，不内嵌整篇正文。

页面 ID 必须全局唯一并保持稳定，修改标题不影响跳转。`Paragraph.links` 支持行内划线链接，`Link` 和 `LinkButton` 支持独立链接及按钮；目标填写文章的 `ID` 常量。目标页通过目录的 `children` 或文章的 `relatedPages` 声明，无需到 `BookContent` 手动注册。同一个页面对象可以被多处引用，不同页面不能共用 ID。

## 两种内容组织方式

- 目录模式：目录的 `children` 引用多篇文章或子目录，点击条目先看到列表。
- 正文跳转模式：父目录直接引用正文页，正文页用 `relatedPages` 声明独立说明页，再通过正文中的链接或按钮跳转。关联页只进入索引，不作为额外列表项显示。

“图片多开环境作弊”使用正文跳转模式：`WorkSkillDirectory` 直接引用 `PhotoCheatingOverviewPage`，点击后进入原正文；该正文的 `relatedPages` 引用 `PhotoCheatingDetectionPage`，由原有按钮打开。检测内容仍单独维护，不合并到正文。

拆分代码文件不应改变已有阅读结构。是否增加目录层级，应由用户指定，而不是因文章拆成多个文件就增加目录。

## 阅读行为

底部导航只在首页显示，进入子目录或文章后隐藏。返回会恢复上一页及滚动位置，正文支持选择复制、横向滚动代码块和跨页面跳转。

列表左右各留 12dp，相邻条目的垂直间距为 12dp。应用图标为书页与阶梯，使用 Android 矢量资源。
