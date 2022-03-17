//package com.niaobulashi.service.impl;
//
//import com.niaobulashi.service.PdfService;
//import org.apache.pdfbox.pdfparser.PDFParser;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.util.PDFTextStripper;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
///**
// * @author y
// * @date 2022/3/15
// */
//public class PdfServiceImpl implements PdfService {
//    @Override
//    public static void analysis() {
//        //读取本地文件
//        File file = new File("D:\文档\工作\SC\\LINUX_SHELL.pdf");
//        //加载PDF文件
//        PDFParser pdfParser = null;
//        try {
//            pdfParser = new PDFParser(new FileInputStream(file));
//            pdfParser.parse();
//            PDDocument pdDocument = pdfParser.getPDDocument();
//            //读取文本内容
//            PDFTextStripper pdfTextStripper = new PDFTextStripper();
//            //设置输出顺序
//            pdfTextStripper.setSortByPosition(true);
//            //起始页
//            pdfTextStripper.setStartPage(1);
//            pdfTextStripper.setEndPage(10);
//            //文本内容
//            String text = pdfTextStripper.getText(pdDocument);
//            //换行符截取
//            String[] split = text.split("\n");
//            for (String s : split) {
//                System.out.println("s = " + s);
//            }
//            pdDocument.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
