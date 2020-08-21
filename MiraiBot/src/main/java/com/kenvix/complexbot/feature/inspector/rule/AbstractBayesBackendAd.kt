//--------------------------------------------------
// Class BayesBackendAd
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot.feature.inspector.rule

import com.kenvix.complexbot.feature.inspector.InspectorRule
import org.ahocorasick.trie.Trie

abstract class AbstractBayesBackendAd : InspectorRule {
    companion object Info {
        val blackKeywords = "资料|学习墙|学姐|学习群|PS|免费领|兼职|招聘|网络|QQ|招聘|有意者|到货|本店|代购|扣扣|客服|微店|兼" +
                "职|兼值|淘宝|小姐|妓女|包夜|狼友|技师|推油|胸推|毒龙|口爆|楼凤|足交|口暴|口交|桑拿|吞精|咪咪|婊子|乳方|操逼|全职|" +
                "性伴侣|网购|网络工作|代理|专业|帮忙点一下|帮忙点下|请点击进入|详情请进入|私人侦探|私家侦探|针孔摄象|调查婚外情|信用卡提现" +
                "|无抵押贷款|广告代理|原音铃声|借腹生子|代孕|代生孩子|代开发票|腾讯客服电话|销售热线|免费订购热线|低价出" +
                "售|款到发货|回复可见|连锁加盟|加盟连锁|免费|蚁力神|婴儿汤|售肾|刻章办|买小车|套牌车|玛雅网|营业额" +
                "|电脑传讯|视频来源|下载速度|高清在线|全集在线|在线播放|六位qq|6位qq|位的qq|个qb|送qb|四海帮|足球投注|地下钱庄" +
                "|阿波罗网|六合彩|替考试|出售|答案|救市|股市|圈钱|崩盘|资金|证监会|贷款|秋招|招聘|训练营|首冲|手冲|上线"

        val requiredMatchPattern = Trie.builder()
            .addKeywords(blackKeywords.split('|'))
            .stopOnHit()
            .build()
    }
}