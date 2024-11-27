package org.example.watermark.processor;

import org.example.watermark.WaterMarkUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by cw on 2024/11/21.
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

public class ImageWatermarkProcessor extends WatermarkProcessor {

    @Override
    public void addWatermark(InputStream originalImageInputStream, String text, OutputStream outputImageOutputStream) throws Exception {
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(originalImageInputStream);
        BufferedImage watermarkImage = WaterMarkUtil.createWaterMarkImage(text);
        // 保存带水印的图片到输出流
        ImageIO.write(WaterMarkUtil.getCombinedImage(originalImage, watermarkImage), "jpg", outputImageOutputStream);
    }
}
