package org.example.watermark.processor;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.example.watermark.WaterMarkUtil;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

//import org.jcodec.api.FrameGrab;
//import org.jcodec.api.awt.AWTSequenceEncoder;
//import org.jcodec.common.io.NIOUtils;
//import org.jcodec.common.io.SeekableByteChannel;
//import org.jcodec.common.model.Picture;
//import org.jcodec.common.model.Rational;
//import org.jcodec.scale.AWTUtil;



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

public class VideoWatermarkProcessor extends WatermarkProcessor {

    @Override
    public void addWatermark(InputStream originalVideoInputStream, String text, OutputStream outputVideoOutputStream) throws Exception {
        //使用javacv实现
        File tempFileSource = WaterMarkUtil.createTempFile("watermarked-video-source", ".mp4");
        File tempFileTarget = WaterMarkUtil.createTempFile("watermarked-video", ".mp4");
        copyInputStreamToFile(originalVideoInputStream, tempFileSource);
        BufferedImage watermarkImage = WaterMarkUtil.createWaterMarkImage(text);
        File tempFileWatermark = WaterMarkUtil.createTempFile("watermark", "png");
        ImageIO.write(watermarkImage, "png", tempFileWatermark);

        try {
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(tempFileSource);
            frameGrabber.start();
            int width = frameGrabber.getImageWidth();
            int height = frameGrabber.getImageHeight();
            int channels = frameGrabber.getAudioChannels();
            FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(tempFileTarget, width, height, channels);
            int frameRate = (int)frameGrabber.getFrameRate();
            frameRecorder.setFrameRate(frameRate);
            frameRecorder.setSampleRate(frameGrabber.getSampleRate());
            frameRecorder.setAudioBitrate(frameGrabber.getAudioBitrate());
            frameRecorder.setVideoBitrate(frameGrabber.getVideoBitrate());
            //yuv420p颜色空间生成mp4文件
            frameRecorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            frameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            frameRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            String watermark = String.format("movie=%s[watermark];[in][watermark]overlay=W-w-15:H-h-15:format=rgb[out]", WaterMarkUtil.getAvailableVideoPath(tempFileWatermark.getAbsolutePath()));
            FFmpegFrameFilter frameFilter = new FFmpegFrameFilter(watermark, width, height);
            frameFilter.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
            frameFilter.start();

            frameRecorder.start();
            while (true) {
                Frame frame = frameGrabber.grab();
                if (frame != null) {
                    frameFilter.push(frame);
                    Frame filteredFrame = frameFilter.pull();
                    frameRecorder.record(filteredFrame);
                } else {
                    break;
                }
            }

            frameRecorder.setMetadata(frameGrabber.getMetadata());
            frameRecorder.stop();
            frameRecorder.release();
            frameFilter.stop();
            frameFilter.release();
            frameGrabber.stop();
            frameGrabber.release();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            copyFileToOutputStream(tempFileTarget, outputVideoOutputStream);
            tempFileSource.delete();
            tempFileTarget.delete();
            tempFileWatermark.delete();
        }

        //使用ffmpeg命令行实现方法如下
        /*

        File tempFileSource = WaterMarkUtil.createTempFile("watermark-source","mp4");
        File tempFileWatermark = WaterMarkUtil.createTempFile("watermark", "png");
        copyInputStreamToFile(originalVideoInputStream, tempFileSource);
        BufferedImage watermarkImage = WaterMarkUtil.createWaterMarkImage(text);
        ImageIO.write(watermarkImage, "png", tempFileWatermark);
        String tempFileTargetPath = WaterMarkUtil.getTempFilePath("watermark-target","mp4");

        // FFmpeg命令
        String command = String.format("ffmpeg -i %s -i %s -filter_complex \"overlay=10:10\" -codec:a copy %s",
                tempFileSource.getAbsoluteFile(), tempFileWatermark.getAbsoluteFile(), tempFileTargetPath);

        try {
            // 启动进程
            Process process = Runtime.getRuntime().exec(command);

            // 读取标准输出
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            stdoutThread.start();

            // 读取标准错误
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            stderrThread.start();

            // 等待进程结束
            int exitCode = process.waitFor();
            System.out.println("\n程序结束，退出代码：" + exitCode);

            // 等待标准输出和标准错误线程结束
            stdoutThread.join();
            stderrThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 将结果输出到输出流
            File tempFileTarget = new File(tempFileTargetPath);
            copyFileToOutputStream(tempFileTarget, outputVideoOutputStream);
            tempFileTarget.delete();
            tempFileSource.delete();
            tempFileWatermark.delete();
        }

        */
    }



    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private void copyFileToOutputStream(File file, OutputStream outputStream) throws IOException {
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
