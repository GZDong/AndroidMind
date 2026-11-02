package com.gzd.androidmind.content.workskill.photocheating

import com.gzd.androidmind.content.BookPage
import com.gzd.androidmind.content.ContentBlock
import com.gzd.androidmind.content.ContentBlock.Code
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
        Paragraph("云派工采集页不允许从相册选择照片，只允许现场拍照，但线上出现照片主体相同、时间和地址水印却不同的情况。后续已通过第三方多开工具复现：用户先在工具中选好本地照片，再从工具内启动云派工，拍照时返回的画面可以被替换为预选图片。"),
        Paragraph("因此，“入口只允许拍照”不能单独证明返回的就是现场相机画面。正常的采集回调、水印绘制和上传流程，也可能处理到一张被运行环境替换的旧图。不同水印本身不能说明替换发生在哪一层，需要结合复现和运行证据分析。"),
        Heading("作弊原理：宿主容器接管运行环境"),
        Paragraph("这类免 Root 多开通常不是给手机再安装一个完整 Android 系统，而是在普通应用内实现用户态容器。多开软件作为宿主，启动自己的占位组件和进程，再加载云派工的代码、资源等，让云派工寄生运行。系统看到的是宿主组件，容器内的云派工则可能看到被伪装的应用身份。"),
        Paragraph("容器通过 Hook 拦截部分系统接口，改写组件启动、包信息和文件访问等行为，并把目标应用的读写重定向到宿主私有目录。具有图片替换能力的工具，还会干预相机相关调用、预览或拍照结果，让云派工收到预选图片。不同工具的具体替换位置可能不同，不能只凭现象断言都 Hook 了同一个接口。"),
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
        Paragraph("形象地说，正常运行是“住在自己家”，多开运行是“住在宿主家里划出的房间”。检测依据来自当前应用路径或本进程内存映射中的路径，不是看到手机装了某软件，也不是任意目录名包含 virtual 就判断作弊。"),
        Heading("作弊检测方式"),
        Paragraph("检测重点是寻找宿主身份、虚拟路径和容器引擎等运行证据。项目将确认运行在这类虚拟空间视为禁止环境，不要求先证明某一张照片已经被替换。"),
        LinkButton("查看四类检测与拦截方法", PhotoCheatingDetectionPage.ID),
        Heading("误判兜底：总开关与工程师白名单"),
        Paragraph("采集拦截总开关为 enableCollectionEnvironmentBlock，默认开启。关闭后不执行采集拍照拦截，但检测和观测上报保留，用于出现集中异常时线上止损。"),
        Paragraph("工程师白名单为 containerEnvironmentEngineerWhitelist，类型是工程师编号列表。按完整编号精确匹配，命中后免拦截；白名单不改变环境检测结论，也不关闭确认事件上报，便于对个别用户放行后继续观察。"),
        Paragraph("总开关、工程师白名单与宿主黑名单不是一回事：总开关控制是否拦截，工程师白名单指定谁免拦截，宿主黑名单则为第四类规则补充已确认工具的包名证据。"),
    )
}
