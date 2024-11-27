package org.example.watermark.factory;

import org.example.watermark.processor.ImageWatermarkProcessor;
import org.example.watermark.processor.PdfWatermarkProcessor;
import org.example.watermark.processor.VideoWatermarkProcessor;
import org.example.watermark.processor.WatermarkProcessor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 10542 on 2024/11/21.
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

public class WatermarkProcessorFactory {
    public static WatermarkProcessor getProcessor(String fileType) {
        switch (fileType.toLowerCase()) {
            case "image":
                return new ImageWatermarkProcessor();
            case "video":
                return new VideoWatermarkProcessor();
            case "pdf":
                return new PdfWatermarkProcessor();
            default:
                return new WatermarkProcessor() {
                    @Override
                    public void addWatermark(InputStream sourceFileStream, String text, OutputStream targetFileStream) throws Exception {

                    }
                };
        }
    }
}
