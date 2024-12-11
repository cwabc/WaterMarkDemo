package org.example;

import cn.hutool.core.io.FileUtil;
import org.example.watermark.WaterMarkUtil;

import java.io.BufferedInputStream;
import java.io.OutputStream;

public class Main {
    private static final String WATERMARK_TEXT = "张三 4699";

    public static void main(String[] args) throws Exception {
        BufferedInputStream inputStreamVideo = FileUtil.getInputStream("视频.mp4");
        OutputStream outputStreamVideo = FileUtil.getOutputStream("result/视频-加完水印.mp4");
        WaterMarkUtil.addWaterMark(inputStreamVideo, WATERMARK_TEXT, outputStreamVideo);

        BufferedInputStream inputStreamImage = FileUtil.getInputStream("图.jpg");
        OutputStream outputStreamImage = FileUtil.getOutputStream("result/图片-加完水印.jpg");
        WaterMarkUtil.addWaterMark(inputStreamImage, WATERMARK_TEXT, outputStreamImage);
        outputStreamImage.flush();
        outputStreamImage.close();

        BufferedInputStream inputStreamPdf = FileUtil.getInputStream("PDF.pdf");
        OutputStream outputStreamPdf = FileUtil.getOutputStream("result/PDF-加完水印.pdf");
        WaterMarkUtil.addWaterMark(inputStreamPdf, WATERMARK_TEXT, outputStreamPdf);
    }

}