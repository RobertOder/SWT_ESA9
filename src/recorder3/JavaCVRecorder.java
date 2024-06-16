package recorder3;

// Import der externen JavaCV-Klassen
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.bytedeco.javacv.*;
// Import der Java eigenen Audio-Bibliotheken
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
// Import der GUI-Klassen und Eventhandler
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class JavaCVRecorder {
	
	// Initialisierung der threaduebergreifende Variablen
	private static boolean isRecording = true;
	private static long startTime = 0;
    private static long videoTime = 0;
	
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
            // Setze die gewuenschte Aufloesung, da sonst nur VGA (standard)
            grabber1.setImageWidth(1920);
            grabber1.setImageHeight(1080);
            grabber2.setImageWidth(1280);
            grabber2.setImageHeight(720);
            
            // Initialisierung des Mikrofons mit Hilfe der javax.sound Bibliothek
            AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            
            try {
            	// Startet den Zugriff aus die Webcams
                grabber1.start();
                grabber2.start();
                
                // Startet den Zugriff auf das Mikrofon
                TargetDataLine line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
                line.open(audioFormat);
                line.start();
                // Aktivierung des Audio-Buffers
                byte[] audioBuffer = new byte[(int) audioFormat.getSampleRate() * audioFormat.getChannels()];
                
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
                FFmpegFrameRecorder recorder1 = FFmpegFrameRecorder.createDefault("C:/test/JavaCV_output_1.mkv", grabber1.getImageWidth(), grabber1.getImageHeight()); // Fuer MKV-Container
                FFmpegFrameRecorder recorder2 = FFmpegFrameRecorder.createDefault("C:/test/JavaCV_output_2.mkv", grabber2.getImageWidth(), grabber2.getImageHeight()); // Fuer MKV-Container
                
                // Festlegung der Video-Codecs, -Frameraten und Containerformate
                recorder1.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder2.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
                recorder1.setFrameRate(30);// Anzahl der Frames pro Sekunde im Video (gleich für beide Kameras)
                recorder2.setFrameRate(30);
                //recorder1.setFormat("mp4"); // Format auf MP4 festlegen
                //recorder2.setFormat("mp4"); // Format auf MP4 festlegen
                recorder1.setFormat("matroska"); // Format auf MKV festlegen
                recorder2.setFormat("matroska"); // Format auf MKV festlegen
                // Videoqualitaet steigern, druch erhoehung der Bitrate, ohne Angabe sind 400 kb/s Standard
                //recorder1.setVideoBitrate(25000000);
                //recorder2.setVideoBitrate(25000000);
                
                // Festlegung der Audio-Codecs, -Frameraten und Containerformate fuer den ersten Rekorder
                recorder1.setAudioQuality(0); // hoechste Qualitaet
                recorder1.setAudioBitrate(192000); // 192 Kbps
                recorder1.setSampleRate(44100); // Abtastrate 44100 fuer Frequenzen bis 44,1 kHz
                recorder1.setAudioChannels(2); // Stereo
                recorder1.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // Advanced Audio Coding von MPEG entwickelt
                
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
                	// Mikrofon grabben
                	int nBytesRead = 0;
                    while (nBytesRead == 0) {
                        nBytesRead = line.read(audioBuffer, 0, line.available());
                    }
                    // Konvertierung von Byte in Short - Zwingend Notwendig zum Samplen - hat eine Groeße von 16bit
                    int nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];
                    // Byte-Array 'audioBuffer' wird in ein ByteBuffer-Objekt umgerechnet und deren Byte-Reihung 'Little-Endian'
                    ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    ShortBuffer shortBuffer = ShortBuffer.wrap(samples, 0, nSamplesRead);
                	                   
                    // Festehalten des Startzeitstempels, wenn noch nicht geschehen
                	if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }
                	// Festhalten des Webcam-Bildes fuer die recorder und die GUI
                    Frame capturedFrame1 = grabber1.grab();
                    Frame capturedFrame2 = grabber2.grab();
                    // Festhalten des Framezeitstempels
                    videoTime = 1000 * (System.currentTimeMillis() - startTime);
                    
                    // Pruefen, ob auch ein Bild von der API kommt -> von beiden Webcams
                    if (capturedFrame1 != null && capturedFrame2 != null) {
                  	
                        // Prueft den Zeitunterschied von Recorder 1 und Frameaufnahem
                        if (videoTime > recorder1.getTimestamp()) {
                            System.out.println(videoTime + " Framezeit - "  + recorder1.getTimestamp() + " Rekorderzeit = " + (videoTime - recorder1.getTimestamp()) + " Differenz");
                            // Ueberschreiben des Rekorderzeitstempels mit der Aufnahmezeit des Frames
                            recorder1.setTimestamp(videoTime);
                        }
                        // Uebergabe des JavaCV-Frames an den Recorder 1
                        recorder1.record(capturedFrame1);
                        // Ubergabe des Short-Buffers an den Rekorder 1 zum samplen
                        recorder1.recordSamples((int) audioFormat.getSampleRate(), audioFormat.getChannels(), shortBuffer);

                        // Prueft den Zeitunterschied von Recorder 2 und Frameaufnahem
                        if (videoTime > recorder2.getTimestamp()) {
                            System.out.println(videoTime + " Framezeit - "  + recorder2.getTimestamp() + " Rekorderzeit = " + (videoTime - recorder2.getTimestamp()) + " Differenz");
                            // Ueberschreiben des Rekorderzeitstempels mit der Aufnahmezeit des Frames
                            recorder2.setTimestamp(videoTime);
                        }
                        // Uebergabe des JavaCV-Frames an den Recorder 1
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
