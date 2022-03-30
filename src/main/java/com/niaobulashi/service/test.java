package com.niaobulashi.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author y
 * @date 2022/3/15
 */
public class test {

    public static void analysis() {
        //读取本地文件
        File file = new File("D:\\文档\\工作\\SC\\互联网营销推广系统.PDF");
        //加载PDF文件
        PDFParser pdfParser = null;
        try {
            pdfParser = new PDFParser(new FileInputStream(file));
            pdfParser.parse();
            PDDocument pdDocument = pdfParser.getPDDocument();
            //读取文本内容
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            //设置输出顺序
            pdfTextStripper.setSortByPosition(true);
            //起始页
            pdfTextStripper.setStartPage(1);
            pdfTextStripper.setEndPage(10);
            //文本内容
            String text = pdfTextStripper.getText(pdDocument);
            //放到json中

            //换行符截取
            String[] split = text.split("\n");

//            List<String> splits = Arrays.asList(split);
//            for (int i = 0; i < splits.size(); i++) {
//                System.out.println("123"+splits.get(i));
//            }
            StringBuilder sb = new StringBuilder();
            for (String s : split) {
                String s3 = subStu(s, "[", "]");
                sb.append(s3);
            }
            System.out.println("最后的结果" + sb);

            String sb1 = sb.toString();
            String s1 = subString(sb1, "背景技术", "发明内容");

            String s2 = subString(sb1, "发明内容", "具体实施方式");
            String s3 = subString(sb1, "具体实施方式", "说　明　书　附　图");
            String s4 = subString(sb1, "发明名称", "摘要");
//            String su1 = s4.replace("\r", "").substring(0,7);
            String su1 = s4.replace("\r", "").substring(0, 7);
            String s5 = subString(sb1, "摘要", "技术领域");
            String s6 = subString(sb1, "技术领域", "背景技术");

//            JSONArray jsonArray= new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("背景技术", s1);
            jsonObject.put("发明内容", s2);
            jsonObject.put("具体实施方式", s3);
            jsonObject.put("发明名称", s4);
            jsonObject.put("摘要", s5);
            jsonObject.put("技术领域", s6);
            jsonObject.put("实用新型名称", "null");
            jsonObject.put("实用新型内容", "null");
            System.out.println(jsonObject);

            //在这调用算法
            //传入json
            //输出文件地址

            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 截取字符串str中指定字符 strStart、strEnd之间的字符串
     *
     * @return
     */
    public static String subString(String str, String strStart, String strEnd) {

        /* 找出指定的2个字符在 该字符串里面的 位置 */
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd);

        /* index 为负数 即表示该字符串中 没有该字符 */
        if (strStartIndex < 0) {
            return "字符串 :---->" + str + "<---- 中不存在 " + strStart + ", 无法截取目标字符串";
        }
        if (strEndIndex < 0) {
            return "字符串 :---->" + str + "<---- 中不存在 " + strEnd + ", 无法截取目标字符串";
        }
        /* 开始截取 */
        String result = str.substring(strStartIndex, strEndIndex).substring(strStart.length());
        return result;
    }

    /**
     * 过滤 字符串
     * [0035]
     *
     * @return
     */
    public static String subStu(String str, String strStart, String strEnd) {
        /* 找出指定的2个字符在 该字符串里面的 位置 */
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd);
        if (strStartIndex < 0) {
            return str;
        }
        if (strEndIndex < 0) {
            return str;
        }
        String r1 = str.substring(strStartIndex, strEndIndex + 1).substring(strStart.length() - 1);
        String result = str.replace(r1, "");
        return result;
    }

    /**
     *
     * CN 111476594 A 说　明　书 3/5 页   &  /r
     *
     * @param str
     * @return
     */
    public static String subStuPage(String str) {
        /* 找出指定的2个字符在 该字符串里面的 位置 */
//        String str1 = "\\r 为解决上述技术问题，本发明提出了一种基于SAAS的互联网营销平台，链接场景、\\r打通平台、数据驱动、统一运营为核心，为中小微商户提供丰富的轻量级产品及专业的各类\\r增值企业服务。\\r 根据本发明的实施例，本发明提出了一种基于SAAS的互联网营销平台，该平台包\\r括：智慧POS系统、营销推广系统、数字化管理系统、线下流量控制系统、企业信息管理系统，\\r其中，\\r 所述智慧POS系统，用于为平台商户提供多种支付方式，通过预设的方案获取并分\\r析客户消费数据，以提取出有价值的客流数据；\\r 所述营销推广系统，用于通过对客户数据以及客流数据进行分析，获取客户位置\\r4\\rCN 111476594 A 说　明　书 2/5 页\\r信息，以进行实时营销数据推送；\\r 所述数字化管理系统，用于对客户以及商户的信息管理，为客户消费数据的输出\\r提供支撑；\\r 所述线下流量控制系统，用于基于线下门店销售数据，获取线下门店运营状态，从\\r而为互联网营销平台筛选出优质商户；\\r 所述企业信息管理系统，用于获取并分析平台中的商户经营数据，以向商户提供\\r经营策略支持。\\r 进一步的，所述智慧POS系统包括：\\r 综合收银模块，设置有基于微信、支付宝、翼支付、和包以及银行卡的多种支付接\\r口以实现多种类型支付服务；\\r 劵码验证码模块，用于基于线上线下的劵码统一验证模式以支持优惠券的线下核\\r销；\\r 电子发票模块，用于基于客户的扫码信息获取基础发票信息并自动开具电子发票\\r发送至支付宝或微信卡包中，以实现客户的不限时间及地域的实时获取；\\r 会员积分模块，用于基于手机短信的积分兑换推送以提醒积分状态，并分析当前\\r积分数据获取客户消费能力和消费习惯数据；\\r WIFI探针模块，用于实时客流数据统计和分析，以获取有价值的客流数据；\\r 热点广告模块，用于向商场或门店的WIFI客户的广告推送，定时分析广告推送数\\r据以获取广告收益，并将收益发送至与其对应的商场或门店。\\r 进一步的，所述营销推广系统包括：\\r 数据交互模块，用于与与外部网络环境的数据交互，以实现不同网络环境下的数\\r据交互互通；\\r 内部营销模块，用于为商户提供模板和个性化线上营销产品；\\r 推广选择模块，客户为商户提供多种推广渠道模式供客户选择；\\r 劵码营销模块，用于提供劵码管理，为商户提供活动配置、劵码管理能力，其中劵\\r码类型包括单品券、折扣券、抵扣券、满减券。\\r 进一步的，所述数字化管理系统包括：\\r 会员权益管理模块，用于基于会员信息实时更新会员积分、会员等级以及会员权\\r益数据；\\r 商户管理模块，用于根据商户交易及评价数据进行商户业务侧办理，以及获取商\\r户经营基础信息、经营状态信息，从而分析出当前商户的整体状态数据以作为优质商户评\\r选提供数据支撑；\\r 客户管理模块，用于管理客户基础信息，并且定时获取并分析用户消费数据从而\\r进行客户画像，以获取客户特征、消费偏好、消费能力数据，从而为营销推广系统提供客户\\r数据支撑。\\r 进一步的，所述线下门店销售数据包括销售收入数据、用户画像数据以及客流分\\r析数据。\\r 进一步的，所述经营策略支持包括通过商户经营数据提取出异常数据以及高效数\\r据，以作为产生经营策略的数据支撑，其中，所述异常数据为经营下滑所反映的数据，所述\\r5\\rCN 111476594 A 说　明　书 3/5 页\\r高效数据为经营状态良好所反映的数据。\\r 进一步的，所述外部网络环境数据包括满减、满赠、折扣数据。\\r 进一步的，所述活动配置、劵码管理包括消费送券、会员生日送券、到期提醒。\\r 进一步的，所述会员权益管理模块还用于，建立完善的用户体系，互通收银系统、\\r停车系统、CRM系统、ERP系统，并实现对接微信公众号、小程序、APP、支付宝工具，实现线上\\r线下会员价值的数据深挖。\\r 进一步的，所述整体状态数据包括门店交易趋势、客流量、复购率，员工销售能力\\r数据。\\r 本发明提出的互联网营销平台能够带来全新的开放视野，让企业商家拥有长时间\\r盈利的能力，从而产生持续的投资价值。\\r (1)管理成本降低效应，以最直观的企业云服务后台获取入驻商户经营成果，企业\\r流转速度加快，提高管理效率，入驻商户可直接通过站内信或即时通讯等方式联系商管反\\r馈问题，在线沟通降低沟通成本\\r (2)沟通成本降低效应，通过统一后台入口获取信息流反馈及资金流监控，减少第\\r三方支付乱入造成的资金流失。\\r (3)利润扩大效应，通过统一后台入口获取信息流反馈及资金流监控，减少第三方\\r支付乱入造成的资金流失。\\r (4)品牌提升效应，统一为入驻商户开通企业云服务后台，提供专项营销活动及数\\r据平台，汇聚匠心品牌提升。\\r (5)智慧商业创新效应，通过不断植入的智慧化模块，以创新的合作方式产生商业\\r亮点，实现整体商业的智慧化转型。\\r (6)风险防范效应，实时在线监控系统运行，数据异常实时报警\\r附图说明\\r 图1为本发明提出的基于SAAS的互联网营销平台框架图；\\r";
//        str =str1+ "\\r 为解决上述技术问题，本发明提出了一种基于SAAS的互联网营销平台，链接场景、\\r打通平台、数据驱动、统一运营为核心，为中小微商户提供丰富的轻量级产品及专业的各类\\r增值企业服务。\\r 根据本发明的实施例，本发明提出了一种基于SAAS的互联网营销平台，该平台包\\r括：智慧POS系统、营销推广系统、数字化管理系统、线下流量控制系统、企业信息管理系统，\\r其中，\\r 所述智慧POS系统，用于为平台商户提供多种支付方式，通过预设的方案获取并分\\r析客户消费数据，以提取出有价值的客流数据；\\r 所述营销推广系统，用于通过对客户数据以及客流数据进行分析，获取客户位置\\r4\\rCN 111476594 A 说　明　书 2/5 页\\r信息，以进行实时营销数据推送；\\r 所述数字化管理系统，用于对客户以及商户的信息管理，为客户消费数据的输出\\r提供支撑；\\r 所述线下流量控制系统，用于基于线下门店销售数据，获取线下门店运营状态，从\\r而为互联网营销平台筛选出优质商户；\\r 所述企业信息管理系统，用于获取并分析平台中的商户经营数据，以向商户提供\\r经营策略支持。\\r 进一步的，所述智慧POS系统包括：\\r 综合收银模块，设置有基于微信、支付宝、翼支付、和包以及银行卡的多种支付接\\r口以实现多种类型支付服务；\\r 劵码验证码模块，用于基于线上线下的劵码统一验证模式以支持优惠券的线下核\\r销；\\r 电子发票模块，用于基于客户的扫码信息获取基础发票信息并自动开具电子发票\\r发送至支付宝或微信卡包中，以实现客户的不限时间及地域的实时获取；\\r 会员积分模块，用于基于手机短信的积分兑换推送以提醒积分状态，并分析当前\\r积分数据获取客户消费能力和消费习惯数据；\\r WIFI探针模块，用于实时客流数据统计和分析，以获取有价值的客流数据；\\r 热点广告模块，用于向商场或门店的WIFI客户的广告推送，定时分析广告推送数\\r据以获取广告收益，并将收益发送至与其对应的商场或门店。\\r 进一步的，所述营销推广系统包括：\\r 数据交互模块，用于与与外部网络环境的数据交互，以实现不同网络环境下的数\\r据交互互通；\\r 内部营销模块，用于为商户提供模板和个性化线上营销产品；\\r 推广选择模块，客户为商户提供多种推广渠道模式供客户选择；\\r 劵码营销模块，用于提供劵码管理，为商户提供活动配置、劵码管理能力，其中劵\\r码类型包括单品券、折扣券、抵扣券、满减券。\\r 进一步的，所述数字化管理系统包括：\\r 会员权益管理模块，用于基于会员信息实时更新会员积分、会员等级以及会员权\\r益数据；\\r 商户管理模块，用于根据商户交易及评价数据进行商户业务侧办理，以及获取商\\r户经营基础信息、经营状态信息，从而分析出当前商户的整体状态数据以作为优质商户评\\r选提供数据支撑；\\r 客户管理模块，用于管理客户基础信息，并且定时获取并分析用户消费数据从而\\r进行客户画像，以获取客户特征、消费偏好、消费能力数据，从而为营销推广系统提供客户\\r数据支撑。\\r 进一步的，所述线下门店销售数据包括销售收入数据、用户画像数据以及客流分\\r析数据。\\r 进一步的，所述经营策略支持包括通过商户经营数据提取出异常数据以及高效数\\r据，以作为产生经营策略的数据支撑，其中，所述异常数据为经营下滑所反映的数据，所述\\r5\\rCN 111476594 A 说　明　书 3/5 页\\r高效数据为经营状态良好所反映的数据。\\r 进一步的，所述外部网络环境数据包括满减、满赠、折扣数据。\\r 进一步的，所述活动配置、劵码管理包括消费送券、会员生日送券、到期提醒。\\r 进一步的，所述会员权益管理模块还用于，建立完善的用户体系，互通收银系统、\\r停车系统、CRM系统、ERP系统，并实现对接微信公众号、小程序、APP、支付宝工具，实现线上\\r线下会员价值的数据深挖。\\r 进一步的，所述整体状态数据包括门店交易趋势、客流量、复购率，员工销售能力\\r数据。\\r 本发明提出的互联网营销平台能够带来全新的开放视野，让企业商家拥有长时间\\r盈利的能力，从而产生持续的投资价值。\\r (1)管理成本降低效应，以最直观的企业云服务后台获取入驻商户经营成果，企业\\r流转速度加快，提高管理效率，入驻商户可直接通过站内信或即时通讯等方式联系商管反\\r馈问题，在线沟通降低沟通成本\\r (2)沟通成本降低效应，通过统一后台入口获取信息流反馈及资金流监控，减少第\\r三方支付乱入造成的资金流失。\\r (3)利润扩大效应，通过统一后台入口获取信息流反馈及资金流监控，减少第三方\\r支付乱入造成的资金流失。\\r (4)品牌提升效应，统一为入驻商户开通企业云服务后台，提供专项营销活动及数\\r据平台，汇聚匠心品牌提升。\\r (5)智慧商业创新效应，通过不断植入的智慧化模块，以创新的合作方式产生商业\\r亮点，实现整体商业的智慧化转型。\\r (6)风险防范效应，实时在线监控系统运行，数据异常实时报警\\r附图说明\\r 图1为本发明提出的基于SAAS的互联网营销平台框架图；\\r";

        String replace = str.replace("\\r", "");

        return replace.replaceAll("CN.*?页", "");

    }

    public static String testStringBuilder(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb = sb.append(s);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String s = readWord("D:\\文档\\工作\\SC\\test.doc");
        System.out.println(s);
    }

    //读取 word 中的内容

    public static String readWord(String path){
        String buffer = "";
        try {
            if (path.endsWith(".doc")){
                InputStream is = new FileInputStream(new File(path));
                WordExtractor ex = new WordExtractor(is);
                buffer = ex.getText();
            }else if (path.endsWith(".docx")){
             /* 如果poi版本在4.0.0及以上那么可以使用注释的方式
             	OPCPackage opcPackage = POIXMLDocument.openPackage(path);
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                buffer = extractor.getText();*/
                FileInputStream fs = new FileInputStream(new File(path));
                XWPFDocument xdoc = new XWPFDocument(fs);
                XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
                buffer = extractor.getText();
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e);
        }
        return buffer;
    }


}
