package recorder2;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class OpenCVBackendExample {
    private static boolean isRecording = true;

    public static void main(String[] args) {
        // Erstelle eine VideoCapture-Instanz für die Kamera
        VideoCapture videoCapture = new VideoCapture(1); // 0 für die Standardkamera, ansonsten den entsprechenden Index verwenden

        // Überprüfe, ob die VideoCapture-Instanz erfolgreich geöffnet wurde
        if (!videoCapture.isOpened()) {
            System.out.println("Fehler beim Öffnen der Kamera!");
            return;
        }
        
     // Ausgabe des verwendeten OpenCV-Backends
        System.out.println("Verwendetes OpenCV-Backend: " + videoCapture.getBackendName());

        // Lese die aktuelle Kameraauflösung
        double currentWidth = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        double currentHeight = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
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
        VideoWriter videoWriter = new VideoWriter("C:/test/output.mp4", 0, 30,
                new org.bytedeco.opencv.opencv_core.Size(1920, 1080)); // Beispielwerte für Auflösung und Framerate

        JFrame frameWindow = new JFrame("Videoaufnahme");
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameWindow.setSize(1920, 1080);
        frameWindow.setVisible(true);
        frameWindow.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                isRecording = false;
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        while (isRecording && videoCapture.read(frame)) {
            // Verarbeite den Frame
            // ...

            // Schreibe den Frame in die VideoWriter
            videoWriter.write(frame);

            // Zeige den Frame im JFrame an
            frameWindow.getContentPane().getGraphics().drawImage(toBufferedImage(frame), 0, 0, null);
        }

        videoCapture.release();
        videoWriter.release();
    }

    // Hilfsmethode zum Konvertieren von OpenCV Mat in BufferedImage
    private static BufferedImage toBufferedImage(Mat mat) {
        // Konvertiere das Mat-Objekt zu einem Byte-Array
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];
        mat.data().get(data);

        // Erstelle ein BufferedImage mit den Mat-Daten
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, data);

        return image;
    }
}
