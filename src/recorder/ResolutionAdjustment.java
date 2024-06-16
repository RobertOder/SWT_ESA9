package recorder;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ResolutionAdjustment {
    private static boolean isRecording = true;

    public static void main(String[] args) {
        // Setze den Pfad zur FFMPEG-Bibliothek (libavcodec, libavformat, libavutil)
        System.setProperty("java.library.path", "C:\\Users\\RabRe\\eclipse-workspace\\RecorderJavaCV\\lib\\dll");
        // Lade die FFMPEG-Bibliothek
        System.loadLibrary("avutil-58");
        System.loadLibrary("avcodec-60");
        System.loadLibrary("avformat-60");

        // Erstelle eine VideoCapture-Instanz für die Kamera
        VideoCapture videoCapture = new VideoCapture(1); // 0 für die Standardkamera, ansonsten den entsprechenden Index verwenden

        // Überprüfe, ob die VideoCapture-Instanz erfolgreich geöffnet wurde
        if (!videoCapture.isOpened()) {
            System.out.println("Fehler beim Öffnen der Kamera!");
            return;
        }

        // Lese die aktuelle Kameraauflösung
        double currentWidth = videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH);
        double currentHeight = videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT);
        System.out.println("Aktuelle Auflösung: " + currentWidth + "x" + currentHeight);

        // Setze die gewünschte Auflösung
        int desiredWidth = 1920;
        int desiredHeight = 1080;
        videoCapture.set(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH, desiredWidth);
        videoCapture.set(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT, desiredHeight);

        // Überprüfe, ob die Auflösung erfolgreich geändert wurde
        double newWidth = videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH);
        double newHeight = videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT);
        System.out.println("Neue Auflösung: " + newWidth + "x" + newHeight);

        // Erfasse und verarbeite Frames
        Mat frame = new Mat();

        JFrame frameWindow = new JFrame("Videoaufnahme");
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameWindow.setSize(desiredWidth, desiredHeight);
        frameWindow.setVisible(true);
        frameWindow.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    isRecording = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        // Erstelle den FFmpegFrameRecorder
        FFmpegFrameRecorder videoRecorder = new FFmpegFrameRecorder("C:/test/output.mp4", desiredWidth, desiredHeight);
        videoRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // Verwende den gewünschten Codec, z.B. AV_CODEC_ID_H264 für H.264
        videoRecorder.setFormat("mp4");

        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            videoRecorder.start();

            while (isRecording && videoCapture.read(frame)) {
                // Verarbeite den Frame
                // ...

                // Konvertiere den Mat-Frame zu einem Frame-Objekt
                Frame convertedFrame = convertMatToFrame(frame);

                // Schreibe den Frame in den VideoRecorder
                videoRecorder.record(convertedFrame);

                // Zeige den Frame im JFrame an
                BufferedImage image = converter.getBufferedImage(convertedFrame);
                frameWindow.getContentPane().getGraphics().drawImage(image, 0, 0, null);

                // Warte für einen kurzen Moment, um die Bildrate zu begrenzen
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            videoRecorder.stop();
            videoRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Schließe die VideoCapture-Instanz
        videoCapture.release();
    }

    // Hilfsmethode zum Konvertieren von OpenCV Mat in Frame
    private static Frame convertMatToFrame(Mat mat) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        return converter.convert(mat);
    }
}
