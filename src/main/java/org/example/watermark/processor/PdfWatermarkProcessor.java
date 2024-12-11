package org.example.watermark.processor;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.example.watermark.WaterMarkUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Created by cw on 2024/11/22.
 * ⠰⢷⢿⠄
 * ⠀⠀⠀⠀⠀⣼⣷⣄
 * ⠀⠀⣤⣿⣇⣿⣿⣧⣿⡄
 * ⢴⠾⠋⠀⠀⠻⣿⣷⣿⣿⡀
 * ○ ⠀⢀⣿⣿⡿⢿⠈⣿
 * ⠀⠀⠀⢠⣿⡿⠁⠀⡊⠀⠙
 * ⠀⠀⠀⢿⣿⠀⠀⠹⣿
 * ⠀⠀⠀⠀⠹⣷⡀⠀⣿⡄
 * ⠀⠀⠀⠀⣀⣼⣿⠀⢈⣧.
 */

public class PdfWatermarkProcessor extends WatermarkProcessor {
    private static final int interval = 20;

    @Override
    public void addWatermark(InputStream sourceFileStream, String text, OutputStream targetFileStream) throws Exception {
        File tempFileSource = WaterMarkUtil.createTempFile("watermarked-pdf-source", "pdf");
        WaterMarkUtil.copyInputStreamToFile(sourceFileStream, tempFileSource);

        try {
            PdfReader reader = new PdfReader(FileUtil.getInputStream(tempFileSource), "pdf".getBytes());
            PdfStamper stamp = new PdfStamper(reader, targetFileStream);
            //请注意，字体这边可能会报错，请替换成当前环境下支持的字体
            Rectangle pageRect = null;
            BaseFont base = BaseFont.createFont("ziti.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.EMBEDDED, FileUtil.readBytes("Alibaba-PuHuiTi-Regular.ttf"), null, true);

            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.3f);
            gs.setStrokeOpacity(0.4f);
            int total = reader.getNumberOfPages() + 1;
            JLabel label = new JLabel();
            FontMetrics metrics;
            int textH = 0;
            int textW = 0;
            label.setText(text);
            metrics = label.getFontMetrics(label.getFont());
            textH = metrics.getHeight();
            textW = metrics.stringWidth(label.getText());
            PdfContentByte under;
            for (int i = 1; i < total; i++) {
                pageRect = reader.getPageSizeWithRotation(i);
                under = stamp.getOverContent(i);
                under.saveState();
                under.setGState(gs);
                under.beginText();
                under.setFontAndSize(base, 14);
                // 水印文字成30度角倾斜
                //你可以随心所欲的改你自己想要的角度
                for (int height = interval + textH; height < pageRect.getHeight();
                     height = height + textH * 9) {
                    for (int width = interval + textW; width < pageRect.getWidth() + textW;
                         width = width + textW * 2) {
                        under.setColorFill(BaseColor.DARK_GRAY);
                        under.showTextAligned(Element.ALIGN_LEFT, text, width - textW, height - textH, 30);
                    }
                }
                // 添加水印文字
                under.endText();
            }

            stamp.close();// 关闭
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            WaterMarkUtil.copyFileToOutputStream(tempFileSource, targetFileStream);
        }finally {
            tempFileSource.delete();
        }
    }

}
