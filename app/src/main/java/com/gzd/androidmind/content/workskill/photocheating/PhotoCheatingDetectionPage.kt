package com.gzd.androidmind.content.workskill.photocheating

import com.gzd.androidmind.content.BookPage
import com.gzd.androidmind.content.ContentBlock
import com.gzd.androidmind.content.ContentBlock.Code
import com.gzd.androidmind.content.ContentBlock.Heading
import com.gzd.androidmind.content.ContentBlock.Paragraph

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object PhotoCheatingDetectionPage : BookPage.Article() {
    const val ID = "work-photo-container-cheating-detection"

    override val id = ID
    override val title = "作弊检测方式"
    override val blocks: List<ContentBlock> = listOf(
        Paragraph("四类之间是“或”，满足任意一类即可确认；每类内部的组合条件是“且”。实际拍照拦截还需总开关开启，且工程师不在白名单中。"),
        Heading("第一类：PendingIntent 创建者身份异常（id）"),
        Paragraph("云派工自己创建全新的 PendingIntent，读取 creatorPackage，与云派工固定包名比较。查自己的令牌，不查通知或其他应用传来的令牌，避免认错创建者。"),
        Code(
            """
            正常：云派工申请创建令牌
            → 创建者 com.gree.app.dispatch，一致

            部分多开：请求被改为宿主身份
            → 创建者 com.paint.yssh，不一致
            → 命中 id
            """.trimIndent(),
        ),
        Paragraph("原理：部分容器借宿主身份向系统申请令牌，虽然伪装了应用包名，却漏掉了创建者返回值，因此暴露宿主。Intent 指向谁不等于谁创建了令牌；这不是系统直接识别了多开。"),
        Paragraph("探针使用独立 action，完成后取消，不发送广播、不影响业务令牌。如果对方连创建者返回值也伪装，这一类可能漏检。"),
        Heading("第二类：宿主虚拟路径＋沙盒引擎库（g+sl）"),
        Paragraph("同时发现：当前云派工位于其他应用的虚拟目录；本进程加载了完整库名 libsandbox_32.so 或 libsandbox_64.so。"),
        Paragraph("原理：宿主目录说明云派工“住在别人家”，引擎库说明容器代码已加载进本进程。通过应用路径和 /proc/self/maps 取证，两项组合确认，不凭一个库名拦截。"),
        Heading("第三类：宿主虚拟路径＋VBox 引擎库（g+vb）"),
        Paragraph("同时发现：当前云派工位于其他应用的虚拟目录；本进程加载了完整库名 libvb.so。库名必须精确匹配，libvb.so.bak 等相似名称不算。"),
        Paragraph("原理与第二类相同，只是换了一类引擎。单独发现 libvb.so 可按原有规则观测上报，但这条拍照拦截规则必须同时有宿主虚拟路径。"),
        Heading("第四类：疑似虚拟路径＋已确认宿主黑名单（g+bl）"),
        Paragraph("仅补充原本 suspected 的结果：已有云派工的宿主虚拟路径，提取出的完整宿主包名又精确命中已确认工具黑名单。不查安装列表，不做字符串包含匹配。"),
        Code(
            """
            原始证据：
            guest_path = true
            host = com.paint.yssh
            原判定 = suspected

            完整宿主包名命中黑名单
            → 判定升级为 matched
            → 技术记录示例：
            M-g+bl-com.paint.yssh-f0-vivo_V1936AL_A30
            """.trimIndent(),
        ),
        Paragraph("原理：引擎库可能被隐藏，但路径仍暴露宿主。结合已确认工具的包名补足证据，不增加线程扫描。名单可持续维护；com.paint.yssh.extra 不会因包含 com.paint.yssh 而命中。"),
        Paragraph("命中后不再上报“不确定”，改走现有确认事件；拍照时同样参与拦截和拍照事件上报。技术记录增加 bl，业务格式不变。"),
        Heading("时机、性能与判定边界"),
        Paragraph("启动进入首页后只检测上报；拍照返回后重新检查，在照片入库、提交前决定放行或拦截，不直接复用启动时的疑似结果。"),
        Paragraph("启动做完整诊断；拍照只查身份、路径、已加载库，不扫描类和线程。探测在 IO 线程执行，第四类只额外做内存匹配。"),
        Paragraph("读取失败不等于作弊，也不等于安全；已有有效证据不会因其他步骤失败而消失。普通安装、工作资料、系统多用户目录，不会仅因 userId 非 0 而命中。客户端检测仍可能被绕过，不能承诺全机型零误判。"),
    )
}
