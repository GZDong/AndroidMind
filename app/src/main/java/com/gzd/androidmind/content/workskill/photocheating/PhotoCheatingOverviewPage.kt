package com.gzd.androidmind.content.workskill.photocheating

import com.gzd.androidmind.content.BookPage
import com.gzd.androidmind.content.ContentBlock
import com.gzd.androidmind.content.ContentBlock.Code
import com.gzd.androidmind.content.ContentBlock.FlowDiagram
import com.gzd.androidmind.content.ContentBlock.Heading
import com.gzd.androidmind.content.ContentBlock.LinkButton
import com.gzd.androidmind.content.ContentBlock.Paragraph

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object PhotoCheatingOverviewPage : BookPage.Article() {
    const val ID = "work-photo-container-cheating"

    override val id = ID
    override val title = "图片多开环境作弊"
    override val relatedPages: List<BookPage> = listOf(PhotoCheatingDetectionPage)
    override val blocks: List<ContentBlock> = listOf(
        Heading("背景：只允许拍照，为什么还能复用旧图？"),
        Paragraph("采集页只允许拍照，不开放相册，但线上出现了主体相同、水印不同的旧照片。后续通过第三方多开工具复现：先选择本地旧图，再从工具里打开云派工，拍照结果就能被换成这张旧图。"),
        Paragraph("本次复现不需要打开云派工相册：应用收到的“拍照结果”已经被运行环境替换。"),
        Heading("多开为什么能运行另一个应用？"),
        Paragraph("先分清文件和进程：APK 是 ZIP 格式的程序包，不是一个会自己运行的进程。正常启动应用后，是进程内的 Android 运行时 ART 加载、执行 APK 里的 DEX 代码。一个进程并没有“只能执行自己 APK 代码”的限制，类加载器也能加载其他 DEX。"),
        Code(
            """
            云派工 APK 内部：
            AndroidManifest.xml  → 二进制 XML，声明应用、页面及启动入口
            classes*.dex         → 编译后的类、方法和执行指令
            resources.arsc       → 资源 ID 与实际资源的对应表
            res/                 → 编译后的布局、图片等资源
            lib/                 → 对应架构的本地 so 库
            """.trimIndent(),
        ),
        Paragraph("宿主先取得可读取的云派工 APK，例如用户导入的安装包。通过 DexClassLoader 一类的动态代码加载能力，把目标 DEX 纳入当前进程的类加载环境，再由 ART 按需加载和执行。它不需要把代码反编译成 Java，也不需要识别每个方法是什么业务。加载目标代码不会让进程获得目标应用的系统身份或权限，仍受宿主身份约束。"),
        FlowDiagram(
            steps = listOf(
                "1. 解析 APK 入口\n从 Manifest 找到 Application，以及声明 MAIN / LAUNCHER 的启动 Activity。",
                "2. 加载 DEX 代码\n把目标 DEX 加入类加载环境，由 ART 按原有类和方法引用执行。",
                "3. 配置资源环境\n为目标配置 Resources、AssetManager 和 Context，让资源 ID 指向目标 APK。",
                "4. 接管组件启动\n系统先启动宿主登记的占位组件，容器再装配目标 Activity、生命周期，并代理后续系统交互。",
            ),
        ),
        Paragraph("以项目为例，Manifest 指定了 com.gree.app.App 和 com.gree.app.ui.activity.SplashActivity。容器按组件声明找到入口，从这里启动，后面的页面跳转、点击回调和网络逻辑仍由原来的 DEX 指令驱动，不是容器重新理解并编写了一遍云派工业务。"),
        Heading("有混淆，为什么还能找到并执行？"),
        Paragraph("普通混淆主要改名和优化，不是把程序变成无法执行的密文。假设 checkLogin 被改成 a，DEX 中调用 checkLogin 的位置也会改为引用 a；ART 仍按引用找到方法并执行，不需要知道 a 的中文含义是“检查登录”。容器同样不需要恢复原方法名，使用最终 APK 中的组件声明和 DEX 即可。"),
        Paragraph("布局和图片也不用靠猜。代码使用的资源 ID，会由目标 APK 的 resources.arsc 映射到对应布局或图片，再由资源系统读取。关键是把资源环境绑定到云派工 APK，而不是误用宿主自己的资源表。加密壳与普通混淆不同，遇到加固还需兼容壳的解密、加载流程，不能保证所有 APK 都能直接运行。"),
        Heading("是不是只调用 startActivity 就够了？"),
        Paragraph("不是。下面以“云派工从启动页跳到登录页”为例，拆开常见占位组件方案的每一步。前提是云派工的代码已经被容器加载，正在宿主进程里运行；StubActivity 是宿主预先登记的占位页面示意类名。"),
        Paragraph("1. 云派工准备显示登录页。“请求打开 LoginActivity”就是云派工执行下面的代码，请系统显示自己的登录页面。LoginActivity 是页面类，不是另一份 APK。"),
        Code("startActivity(Intent(this, LoginActivity::class.java))"),
        Paragraph("2. 容器改写交给系统的目标。它拦住这次调用，记住真正想打开的是云派工 LoginActivity，但把交给系统的 Intent 改成宿主 StubActivity。"),
        Code(
            """
            原目标：云派工 / LoginActivity
            交给系统的目标：多开宿主 / StubActivity
            容器自己保留的原目标：LoginActivity
            """.trimIndent(),
        ),
        Paragraph("为什么要改？系统按目标组件所属应用安排页面启动。若直接指向正常安装的云派工，系统会按云派工自己的身份和进程安排启动，不会自动留在宿主容器里。改成宿主组件，是为了让这次启动走宿主的运行环境。"),
        Paragraph("3. 系统安排启动宿主页面。系统看到的是宿主已经在 Manifest 登记的 StubActivity，检查通过后，安排宿主进程创建这个页面。此时系统处理的启动目标仍是宿主组件，还没有替容器把云派工页面装配好。"),
        Paragraph("4. 容器替换实际创建的页面对象。宿主进程接下来本应创建 StubActivity 对象，容器在这个创建环节插手：恢复记住的原目标，用云派工类加载器找到 DEX 中的 LoginActivity，实际创建它，并配置云派工的 Context 和资源。"),
        Code(
            """
            原本准备创建：宿主的 StubActivity 对象
            容器实际改为：云派工的 LoginActivity 对象
            页面读取资源：指向云派工 APK 的资源表和布局
            """.trimIndent(),
        ),
        Paragraph("5. 执行云派工原有页面逻辑。启动链路继续调用 LoginActivity.onCreate()、onResume()。加载布局时读取的是云派工的登录输入框、按钮等资源；点击登录按钮时执行的也是云派工原有回调，不需要容器重新编写登录业务。后续打开其他页面，容器继续处理同类跳转。"),
        Paragraph("关键区别：交给系统登记、安排启动的是宿主组件；进程内实际创建、执行的却是云派工页面对象。这需要启动请求改写、对象创建替换和资源环境配置共同完成，不是一句 startActivity 就能做到。"),
        Heading("什么是宿主虚拟目录？"),
        Code(
            """
            正常运行，云派工使用自己的目录：
            /data/user/0/com.gree.app.dispatch/files/

            多开运行，云派工位于宿主划出的目录：
            /data/user/0/com.paint.yssh/virtual/data/user/0/com.gree.app.dispatch/files/

            com.paint.yssh：宿主包名
            virtual：容器目录路径段
            com.gree.app.dispatch：容器内的云派工
            """.trimIndent(),
        ),
        Paragraph("正常运行是“住在自己家”，多开运行是“住在宿主划出的房间”。文件读写还可以被重定向：云派工以为写的是自己的目录，实际写进宿主目录。检测读取当前应用路径或本进程内存映射，不是扫描手机有没有安装某软件。"),
        Heading("图片是怎么被替换的？"),
        Paragraph("把云派工放进宿主运行，就是为了让工具有机会在同一进程内 Hook 相机调用、预览数据或渲染环节。正常直接打开云派工时，它运行在自己的进程中，另一个普通 App 不能直接改它收到的相机数据。"),
        Paragraph("本次实际观察到：打开拍照页，尚未点击拍照，预览就已经固定为工具中选中的照片。因此替换至少影响了预览显示，不只是拍完以后偷偷换文件。"),
        Paragraph("再看我们的代码：CameraCaptureActivity 调用下面的方法。CameraView 依赖源码明确说明，它抓取的是“当前相机预览的快照”，不同于普通的 takePicture()。"),
        Code("binding.viewCamera.takePictureSnapshot()"),
        Paragraph("结合这个拍照方式，与现象相符的替换链路是："),
        FlowDiagram(
            steps = listOf(
                "1. 预先准备旧照片\n使用者在工具里选好要冒充现场照片的图片。",
                "2. 预览显示旧照片\n工具干预预览使用的图像数据，云派工显示的不再是镜头当前画面。",
                "3. 点击拍照取快照\ntakePictureSnapshot() 从预览取图，结果也可能就是这张旧照片。",
                "4. 业务照常处理\n后续仍绘制本次时间、地址水印，再保存、上传，形成旧画面搭配新水印。",
            ),
        ),
        Paragraph("因此，控制住预览使用的图像数据，就可能连我们的拍照结果一起控制，不必再单独替换最终文件。固定画面可以通过重复提供同一图片的帧、替换预览纹理等方式实现；仅凭画面，仍不能确定工具具体 Hook 了哪个方法。"),
        Heading("尝试过的其他思路：加速度计检测"),
        Paragraph("AntiFakeCameraHelper 检测的是 Sensor.TYPE_ACCELEROMETER：手机加速度计的 X、Y、Z 数据，不是相机自带的传感器。这条思路利用部分虚拟、模拟环境无法提供正常运动数据的现象，判断是否像真实手持拍照。"),
        Code(
            """
            onResume：注册加速度监听（SENSOR_DELAY_UI）
            点击拍照：checkEnvironment()

            未监测 / 无加速度计 → 501
            累计事件不足 3 次，或超过 1.2 秒无更新 → 503
            取最近最多 10 条 XYZ 数据，至少 5 条才计算波动
            相邻读数的距离累加 > 0.05 → 200，判为正常手持
            否则提示轻晃，等待 3 秒再验证
            窗口内数据不足 → 503；波动达标 → 201；仍不达标 → 403
            验证期间监听停止 → 502
            """.trimIndent(),
        ),
        Paragraph("工具还先检查 Build 型号、硬件和指纹等模拟器特征（401），以及 filesDir 中 vmos、parallel、dualapp、multiapp 等关键词（402）。该埋点混合了多类结果，不是每个异常码都表示传感器缺失。"),
        Heading("这条思路的局限"),
        Paragraph("本次工具只需替换预览画面，并不需要停用真机加速度计，运动数据仍可正常返回。因此“虚拟摄像头必然没有传感器”不成立；模拟环境也能模拟事件。加速度计测的是手机运动，不是照片来源；501、503 或低波动只能说明监听、数据时效或运动量不满足阈值，不能证明照片被替换。"),
        Paragraph("此前线上埋点反馈大量误判。正常静止、监听被页面生命周期打断、系统兼容或硬件异常，都可能与这类阈值结果混在一起。因此没有把它作为当前的强拦截依据，而是改用宿主身份、虚拟路径和引擎等运行证据。"),
        Heading("作弊检测方式"),
        Paragraph("四类规则分别检查身份矛盾、虚拟路径与引擎组合、以及已确认宿主黑名单。启动只观测上报，拍照后、入库前决定是否拦截。"),
        LinkButton("查看四类检测与拦截方法", PhotoCheatingDetectionPage.ID),
        Heading("误判兜底：总开关与工程师白名单"),
        Paragraph("总开关 enableCollectionEnvironmentBlock 默认开启。关闭后不拦截采集拍照，但保留检测上报，便于集中异常时止损。"),
        Paragraph("工程师白名单 containerEnvironmentEngineerWhitelist 是完整工程师编号列表，命中后免拦截，检测结论和上报仍保留。它负责“放行谁”；宿主黑名单负责“补充确认哪种工具”，二者用途不同。"),
    )
}
