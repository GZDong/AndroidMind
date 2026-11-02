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
object PhotoCheatingDetectionPage : BookPage.Article() {
    const val ID = "work-photo-container-cheating-detection"

    override val id = ID
    override val title = "作弊检测方式"
    override val blocks: List<ContentBlock> = listOf(
        Paragraph("四类规则之间是“或”的关系，满足任意一类即可确认；每一类内部要求的组合证据是“且”的关系。确认环境与是否拦截是两步，实际拍照拦截还受总开关、工程师白名单控制。"),
        Heading("第一类：PendingIntent 创建者身份异常（id）"),
        Paragraph("云派工自己创建一个全新的 PendingIntent，再读取 creatorPackage。如果创建者包名不是云派工的固定包名，则发现了身份矛盾。检查的不是通知或其他应用传入的 PendingIntent，避免把别人的创建者身份当成自己的。"),
        Code(
            """
            正常运行：
            云派工申请创建 PendingIntent
            → 系统登记创建者 com.gree.app.dispatch
            → 与云派工固定包名相同

            部分多开环境：
            容器内的云派工申请创建 PendingIntent
            → 容器拦截请求，以宿主身份向系统申请
            → 系统登记创建者 com.paint.yssh
            → 与云派工固定包名不同，命中 id
            """.trimIndent(),
        ),
        Paragraph("原理：目标应用的代码实际寄生在宿主进程中，部分容器会改写请求，以宿主身份向系统申请令牌。它可能伪装了 getPackageName()，却没有处理令牌创建者的返回值，因此暴露宿主。Intent 指向哪个应用，不等于谁创建了 PendingIntent；该接口也不是系统提供的“多开检测开关”。"),
        Paragraph("每次探测使用独立 action，创建后取消令牌，不发送广播，不复用或取消业务令牌。如果对方也伪装了创建者返回值，这一类可能检测不到，需要其他规则补充。"),
        Heading("第二类：宿主虚拟路径＋沙盒引擎库（g+sl）"),
        Paragraph("同时发现：当前云派工位于其他应用的虚拟目录；本进程加载了完整库名 libsandbox_32.so 或 libsandbox_64.so。"),
        Paragraph("原理：虚拟目录表明云派工的数据或代码落在宿主容器内，引擎库提供容器正在本进程工作的佐证。两项组合后确认，不是只要看到一个可疑库名就按这一类拦截。"),
        Heading("第三类：宿主虚拟路径＋VBox 引擎库（g+vb）"),
        Paragraph("同时发现：当前云派工位于其他应用的虚拟目录；本进程加载了完整库名 libvb.so。库名必须精确匹配，libvb.so.bak 等相似名称不算。"),
        Paragraph("原理与第二类相同，区别在于容器引擎特征。单独出现 libvb.so 可以作为原有观测上报的命中特征，但不满足这一类拍照拦截条件，仍需宿主虚拟路径佐证。"),
        Heading("第四类：疑似虚拟路径＋已确认宿主黑名单（g+bl）"),
        Paragraph("只补充原规则判为 suspected 的情况：已有当前云派工的宿主虚拟路径证据，且从路径提取的完整宿主包名，精确命中已确认工具黑名单。既不是检查手机安装了什么，也不是对整条日志做字符串包含匹配。"),
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
        Paragraph("原理：有些容器隐藏了引擎库，或适配了身份接口，但虚拟路径仍暴露宿主。结合已有确认记录中的工具包名，可以补足证据，不必重新增加线程扫描。名单可持续维护，com.paint.yssh.extra 不会因为包含 com.paint.yssh 而命中。"),
        Paragraph("补充命中后不再上传“不确定”事件，改走现有确认事件：账号与工具记录、工具统计、技术诊断；拍照时同样参与拦截并使用现有拍照事件。仅技术证据码增加 bl，避免新增高基数字段或重复上报疑似、确认两套结果。"),
        Heading("时机、性能与判定边界"),
        Paragraph("启动进入首页后的检测用于观测，只上报、不拦截。拍照返回后重新检测，不直接复用启动时的疑似结果；必须在照片数据入库和提交后端之前决定是否放行。确认需要拦截后，照片不进入后续保存流程，并显示拦截弹窗。"),
        Paragraph("启动检查包含类特征、线程等诊断项；拍照检查只保留身份、路径和已加载库，不扫描诊断类和线程。探测在 IO 线程执行；第四类只增加内存中的集合精确匹配，不增加文件读取或网络请求。"),
        Paragraph("单纯读取失败不等于作弊，也不等于环境安全。有独立、有效证据时，其他步骤失败不会抹掉已有证据。普通安装、工作资料和系统多用户目录，不会仅因 userId 不是 0 而命中虚拟路径。客户端检测可能被绕过，测试通过也不等于已经证明所有机型零误判。"),
    )
}
