package org.example.watermark;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import org.example.watermark.factory.WatermarkProcessorFactory;
import org.example.watermark.processor.WatermarkProcessor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

public class WaterMarkUtil {
    private static final String tempFileDir = "temp/";
    private static final int WaterMarkFontSize = 70;
    private static final int WaterMarkSpace = 100;

    public static String getTempFileDir() {
        return tempFileDir;
    }

    public static File createTempFile(String prefix, String suffix) {
        if (!FileUtil.exist(tempFileDir)) {
            FileUtil.mkdir(tempFileDir);
        }

        return new File(getTempFilePath(prefix, suffix));
    }

    public static String getTempFilePath(String prefix, String suffix) {
        return tempFileDir + prefix + System.currentTimeMillis() + "." + suffix;
    }

    public static String getAvailableWaterMarkPath(String sourcePath) {
        sourcePath = sourcePath.replace("\\", "/");
        int colonIndex = sourcePath.indexOf(':');

        // 如果找到冒号
        if (colonIndex != -1) {
            // 在冒号前面插入 \\
            return sourcePath.substring(0, colonIndex) + "\\\\" + sourcePath.substring(colonIndex);
        }

        // 如果没有找到冒号，返回原路径
        return sourcePath;
    }

    public static void addWaterMark(InputStream originalImageInputStream, String text, OutputStream outputImageOutputStream) {
        // 获取水印处理器
        try {
            // 创建 BufferedInputStream 并设置 mark 限制
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = originalImageInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            baos.flush();
            // 从内存中创建新的输入流
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            // 获取文件类型
            String fileType = getSourceType(bais);
            // 重新创建一个新的输入流用于图像处理
            bais = new ByteArrayInputStream(baos.toByteArray());
            WatermarkProcessor processor = WatermarkProcessorFactory.getProcessor(fileType);
            processor.addWatermark(bais, text, outputImageOutputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSourceType(InputStream sourceInputStream) {
        String fileType = FileTypeUtil.getType(sourceInputStream);
        switch (fileType) {
            case "jpg":
            case "png":
            case "gif":
                return "image";
            case "mp4":
            case "avi":
            case "wmv":
            case "wav":
                return "video";
            case "pdf":
                return "pdf";
            default:
                return "unknown";
        }
    }

    public static BufferedImage getCombinedImage(BufferedImage originalImage, BufferedImage watermarkImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int watermarkWidth = watermarkImage.getWidth();
        int watermarkHeight = watermarkImage.getHeight();

        // 创建一个新的BufferedImage来保存带水印的图片
        BufferedImage combinedImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();

        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);

        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // 计算水印图像的旋转角度和缩放比例
        double angle = Math.toRadians(45); // 45度
        double spacing = 50; // 水印之间的间隔
        double effectiveWidth = watermarkWidth + spacing;
        double scaleX = (double) originalWidth / (effectiveWidth * 3); // 确保每行至少有3个水印
        double scaleY = scaleX; // 保持宽高比

        // 创建一个AffineTransform对象来进行旋转和缩放
        AffineTransform at = new AffineTransform();
        at.rotate(angle); // 旋转45度
        // 应用变换
        g2d.setTransform(at);

        // 计算需要绘制的水印数量
        int numWatermarksX = (int) Math.ceil(originalWidth / ((watermarkWidth) + WaterMarkSpace)) + 1;
        int numWatermarksY = (int) Math.ceil(originalHeight / ((watermarkHeight) + WaterMarkSpace)) + 1;

        // 绘制多个水印图片
        for (int i = -numWatermarksX; i <= numWatermarksX; i++) {
            for (int j = -numWatermarksY; j <= numWatermarksY; j++) {
                g2d.drawImage(watermarkImage, i * (watermarkWidth + WaterMarkSpace), j * (watermarkHeight + WaterMarkSpace), null);
            }
        }

        // 清理资源
        g2d.dispose();
        return combinedImage;
    }

    public static BufferedImage createWaterMarkImage(String text) {
        try {
            BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempG2D = tempImage.createGraphics();
            InputStream fontStream = WaterMarkUtil.class.getClassLoader().getResourceAsStream("Alibaba-PuHuiTi-Regular.ttf");
            if (fontStream == null) {
                throw new RuntimeException("Font file not found in resources");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, WaterMarkFontSize);
            fontStream.close();

            tempG2D.setFont(font); // 使用支持中文的字体
            FontMetrics fontMetrics = tempG2D.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(text);
            int textHeight = fontMetrics.getHeight();
            tempG2D.dispose();

            // 计算图片的宽度和高度
            int padding = 20; // 边距
            int width = textWidth + 2 * padding;
            int height = textHeight + 2 * padding;

            // 创建一个透明的BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // 设置背景为透明
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, width, height);

            // 设置文字样式
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 设置透明度
            g2d.setColor(Color.WHITE); // 文字颜色
            g2d.setFont(font); // 使用支持中文的字体

            // 计算文字的位置
            int textX = padding;
            int textY = (height + textHeight) / 2 - fontMetrics.getDescent();

            // 绘制文字
            g2d.drawString(text, textX, textY);

            // 释放资源
            g2d.dispose();

            return image;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    public static void copyFileToOutputStream(File file, OutputStream outputStream) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
        }
    }

}
