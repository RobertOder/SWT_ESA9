package recorder3;

// Import der JavaCV-Klassen
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
// Import der GUI-Klassen und Eventhandler
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class JavaCVRecorderWithAudio {
	
	// Initialisierung der threaduebergreifende Variable
	private static boolean isRecording = true;
	
    public static void main(String[] args) {
    	// Log-Ausgabe fuer den Codec aktivieren
        FFmpegLogCallback.set();
        
        // Swing-Fenster fuer die Live-Bild ausgabe
        JFrame frame1 = new JFrame("Webcam 1 Live-View: Initialisierung...");
        JFrame frame2 = new JFrame("Webcam 2 Live-View: Initialisierung...");
        // Java-Programm beenden bei Schließen eines Swing-Fensters
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Fensteraufloesung - ohne Relevanz, nur damit die Fenster in einer gewissen Groeße angezeigt werden (schonmal zu sehen sind)
        frame1.setSize(640, 480);
        frame2.setSize(640, 480);
        // Fenster anzeigen
        frame1.setVisible(true);
        frame2.setVisible(true);

        // Beginn des Recording-Threads
        Thread recordThread = new Thread(() -> {
        	
            // Initialisierung des FrameGrabbers fuer die Webcams - Nutzt hier MSMF als Webcam-API
            FrameGrabber grabber1 = new OpenCVFrameGrabber(1); // Index 1 fuer "HD Webcam Camera" (FullHD)
            FrameGrabber grabber2 = new OpenCVFrameGrabber(3); // Index 3 fuer "Logitech Webcam 300" (HD)
            //FrameGrabber grabber1 = new DC1394FrameGrabber(1); // DSHOW als MSMF Alternative
            // Audio-FrameGrabbers fuer das Mikrofon (fltp = ueber DirectSound)
            FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber("audio=@device_cm_{33D9A762-90C8-11D0-BD43-00A0C911CE86}");
            audioGrabber.setFormat("s16le"); // Audioformat festlegen
            audioGrabber.setAudioChannels(2);
            audioGrabber.setSampleRate(44100);    // Abtastrate festlegen
            // Setze die gewuenschte Aufloesung, da sonst nur VGA (standard)
            grabber1.setImageWidth(1920);
            grabber1.setImageHeight(1080);
            grabber2.setImageWidth(1280);
            grabber2.setImageHeight(720);
            
            try {
            	// Startet den Zugriff aus die Webcams und Mikrofon
                grabber1.start();
                grabber2.start();
                audioGrabber.start();
                
                // Pruefe die aktuelle Kameraaufloesung
                System.out.println("Aktuelle Auflösung der Webcam 1: " + grabber1.getImageWidth() + "x" + grabber1.getImageHeight());
                System.out.println("Aktuelle Auflösung der Webcam 2: " + grabber2.getImageWidth() + "x" + grabber2.getImageHeight());
                
                // Anpassen der Fenster-Groeße und Fenster-Beschriftung
                frame1.setSize(grabber1.getImageWidth(), grabber1.getImageHeight()); // Fensteraufloesung der Webcam 1
                frame2.setSize(grabber2.getImageWidth(), grabber2.getImageHeight()); // Fensteraufloesung der Webcam 2
                frame1.setTitle("Webcam 1 Live-View: Streaming \"" + VideoInputFrameGrabber.getDeviceDescriptions()[1] + "\"..."); // Fenstertitle
                frame2.setTitle("Webcam 2 Live-View: Streaming \"" + VideoInputFrameGrabber.getDeviceDescriptions()[3] + "\"..."); // Fenstertitle
                
                // Initialisierung des FrameRecorders fuer die Ausgabe-Datei
                //FrameRecorder recorder1 = FrameRecorder.createDefault("C:/test/output_1.mp4", grabber1.getImageWidth(), grabber1.getImageHeight()); // Fuer MP4-Container
                //FrameRecorder recorder2 = FrameRecorder.createDefault("C:/test/output_2.mp4", grabber2.getImageWidth(), grabber2.getImageHeight()); // Fuer Mp4-Container
                FrameRecorder recorder1 = FrameRecorder.createDefault("C:/test/JavaCV_output_1.mkv", grabber1.getImageWidth(), grabber1.getImageHeight()); // Fuer MKV-Container
                FrameRecorder recorder2 = FrameRecorder.createDefault("C:/test/JavaCV_output_2.mkv", grabber2.getImageWidth(), grabber2.getImageHeight()); // Fuer MKV-Container
                
                // Festsetzen des Codecs, Framerate und Containerformat
                recorder1.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder2.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                // Anzahl der Frames pro Sekunde im Video (gleich für beide Kameras)
                recorder1.setFrameRate(10);
                recorder2.setFrameRate(10);
                //recorder1.setFormat("mp4"); // Format auf MP4 festlegen
                //recorder2.setFormat("mp4"); // Format auf MP4 festlegen
                recorder1.setFormat("matroska"); // Format auf MKV festlegen
                recorder2.setFormat("matroska"); // Format auf MKV festlegen
                // Audio-Codierung fuer MPEG4 und Audio-Abtastrate
                recorder1.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder1.setSampleRate(44100);
                
                // Startet der Recorder
                recorder1.start();
                recorder2.start();
                
                // JLabel fuer das JFrame-Fenster, um das Live-Bild anzuzeigen
                JLabel label1 = new JLabel();
                JLabel label2 = new JLabel();
                frame1.add(label1);
                frame2.add(label2);

                // Aufnahme und Live-View starten - pruefen, ob Taste gedrueckt wurde
                while (isRecording) {
                	// Festhalten des Webcam-Bildes fuer die recorder und die GUI
                    Frame capturedFrame1 = grabber1.grab();
                    Frame capturedFrame2 = grabber2.grab();
                    Frame capturedAudioFrame = audioGrabber.grabFrame();
                    // Pruefen, ob auch ein Bild und Ton von der API kommt -> von beiden Webcams und Mikrofon
                    if (capturedFrame1 != null && capturedFrame2 != null && capturedAudioFrame != null) {
                    	// Uebergabe des JavaCV-Frames an den Recorder
                        recorder1.record(capturedFrame1);
                        recorder1.record(capturedAudioFrame);
                        recorder2.record(capturedFrame2);
                        // Uebergabe des JavaCV-Frames an den JLabel vom Swing-Fenster - Benutzte Hilfsmetzhode um den JavaCV-Frame in ein AWT-BufferedImage umzuwandeln
                        label1.setIcon(new ImageIcon(FrameToBufferedImage(capturedFrame1)));
                        label2.setIcon(new ImageIcon(FrameToBufferedImage(capturedFrame2)));
                    }else {
                    	System.out.println("Kein Frame zum Anzeigen oder Aufzeichnen!!!");
                    }
                }

                // Aufnahme beenden und Ressourcen freigeben
                frame1.dispose();
                frame2.dispose();
                recorder1.stop();
                recorder2.stop();
                recorder1.release();
                recorder2.release();
                grabber1.stop();
                grabber2.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Starten des Recording-Threads
        recordThread.start();

        // KeyListener zum Stoppen der Aufnahme beim Druecken der Enter-Taste in einen der Swing-Fenster
        SwingUtilities.invokeLater(() -> {
            frame1.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    	// Boolean aendern damit Aufnahme beendet wird
                        isRecording = false;
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            frame2.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    	// Boolean aendern damit Aufnahme beendet wird
                        isRecording = false;
                    }
                }
                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
        });
    }
    
    // Methode zur Umwandlung eines Frames in ein BufferedImage
    private static BufferedImage FrameToBufferedImage(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.getBufferedImage(frame);
    }
}
