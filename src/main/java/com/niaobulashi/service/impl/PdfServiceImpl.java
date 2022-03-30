package com.niaobulashi.service.impl;

import com.niaobulashi.service.PdfService;
import net.sf.json.JSONObject;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author y
 * @date 2022/3/15
 *
 * 解析Pdf  获得json数据  发送http请求
 *
 */
public class PdfServiceImpl implements PdfService {

    @Override
    public void analysis() {
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
                String s3 = subStu(s, "[","]");
                sb.append(s3);
            }
            System.out.println("最后的结果"+sb);

            String sb1 = sb.toString();
            String s1 = subString(sb1, "背景技术", "发明内容");
            String s2 = subString(sb1, "发明内容", "具体实施方式");
            String s3 = subString(sb1, "具体实施方式", "说　明　书　附　图");
//            JSONArray jsonArray= new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("背景技术",s1);
            jsonObject.put("发明内容",s2);
            jsonObject.put("具体实施方式",s3);
            System.out.println(jsonObject);

            //在这调用算法
            //传入json
            //输出文件地址

            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void analysisWord() {

    }

    /**
     * 截取字符串str中指定字符 strStart、strEnd之间的字符串
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
     * CN 111476594 A 说　明　书 3/5 页
     * [0035]
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
        String r1 = str.substring(strStartIndex, strEndIndex+1).substring(strStart.length()-1);
        String result = str.replace(r1, "");
        return result;
    }

    public static String testStringBuilder(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for(String s : strings) {
            sb=sb.append(s);
        }
        return sb.toString();
    }



}
