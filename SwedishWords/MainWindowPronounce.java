/*
This application gives allows the user to choose from the most common Swedish words and hear them 
pronounced by a native speaker. Then the user can record himself and listen and compare between the native pronounciation and the users pronounciation. The classes Capture.java and Playback.java are slightly modified by David Jacobsson, but are basically the classes Capture.java and Playback.java created by Steve Potts in SimpleSoundCapture.java, therefore his preamble has been kept as comments to the classes.
Please use it however you wish, as far as the Java preamble to Capture and Playback allows you (i.e. don't built nuclear facilities with it and don't remake it into a virus targeting Oracle...;)

This program relies on sun.audio.AudioPlayer which is proprietary and may be removed in a future release. The compiler will warn you about this too.:)

The program tries real hard to set the encoding to UTF-8, but the sound files still seem to be read by windows in a funny way when they contain å,ä or ö, so a workaround exists where the sound files instead are named saa.wav if the original was så.wav, sae.wav if the original was sä.wav and soe.wav if the original was sö.wav.
*/



package SwedishWords;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.imageio.*;
import java.lang.Object;
import java.lang.Thread;
import java.util.*;
import sun.audio.*;


public class MainWindowPronounce extends JPanel implements ActionListener {

	static String[] wordStrings;//Contains Swedish words.
	static String[] wordStrings2;
	static ArrayList <String> words;
	static ArrayList <String> words2;//Contains translation in English.
	
	static String word;//The currently selected word.
	static String word2;//The English translation of the currently selected word.
	static JLabel translation;
	static int a;//index of JCombobox
  static AudioInputStream audioInputStream;
  static boolean windows;//Is this a windows os?
  static boolean funnyCharacter;//Does the swedish word we are working with contain å,ä or ö? 
	
	static String localEncoding;//Here the encoding will be set.
	
	Playback playback = new Playback();//Object that plays back captured audio by the user to the user.
	Capture capture = new Capture();//Object audio that captures audio from the user (as it tries to pronounce correctly).
  static JButton playB, captB;//Buttons for playback and capture
  String errStr;
  double duration, seconds;

	public MainWindowPronounce(){ //Constructor.
		super (new GridBagLayout());//Use Gridbaglayout to place elements of application.
		GridBagLayout gridbag = (GridBagLayout)getLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		ClassLoader cl = this.getClass().getClassLoader();
		try {			
			Icon playAudioIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("playbutton.png")));					
			Icon recordIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("media_record.png")));
			Icon stopIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("stop_button_small.png")));			
			
			JLabel l = new JLabel(":)");
			l.setFont(new Font("arial",Font.BOLD,34));
			c.gridx = 1;
			c.gridy = 0;
			gridbag.setConstraints(l,c);
			add(l,c);
			l.setVisible(true);

			JLabel l2 = new JLabel("Choose a word to pronounce: ");
			l2.setFont(new Font("arial",Font.BOLD,16));
			c.gridx = 0;
			c.gridy = 1;
			gridbag.setConstraints(l2,c);
			add(l2,c);
			l2.setVisible(true);

			JComboBox cb = new JComboBox(wordStrings);
			c.gridx = 1;
			c.gridy = 1;
			gridbag.setConstraints(cb,c);
			cb.setVisible(true);
			add(cb,c);
			cb.addActionListener(this);
		
			JLabel l3 = new JLabel("Listen to the word: ");
			l3.setFont(new Font("arial",Font.BOLD,16));
			c.gridx = 2;
			c.gridy = 1;
			gridbag.setConstraints(l3,c);
			add(l3,c);
			l3.setVisible(true);		
		
			JButton playAudio = new JButton(playAudioIcon);			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 3;
			c.gridy = 1;
			gridbag.setConstraints(playAudio, c);
			playAudio.setBorder(BorderFactory.createEmptyBorder());		
			playAudio.setContentAreaFilled(false);		
			add(playAudio,c);		
			playAudio.setVisible(true);	
			playAudio.addActionListener(this);
			playAudio.setActionCommand("play audio");
			
			translation = new JLabel(" ");
			translation.setFont(new Font("arial",Font.ITALIC,20));
			c.gridx = 1;
			c.gridy = 2;
			gridbag.setConstraints(translation,c);
			add(translation,c);
			translation.setVisible(true);				
			
			JLabel l4 = new JLabel("Record yourself saying the word: ");
			l4.setFont(new Font("arial",Font.BOLD,16));
			c.gridx = 0;
			c.gridy = 3;
			gridbag.setConstraints(l4,c);
			add(l4,c);
			l4.setVisible(true);			
			
			captB = new JButton(recordIcon);		
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = 3;
			gridbag.setConstraints(captB, c);
			captB.setBorder(BorderFactory.createEmptyBorder());		
			captB.setContentAreaFilled(false);		
			add(captB,c);		
			captB.setVisible(true);		
			captB.addActionListener(this);
			captB.setActionCommand("Record");
			
			JLabel l5 = new JLabel("After recording, listen to yourself and compare: ");
			l5.setFont(new Font("arial",Font.BOLD,16));
			c.gridx = 2;
			c.gridy = 3;
			gridbag.setConstraints(l5,c);
			add(l5,c);
			l5.setVisible(true);
			
			playB = new JButton(playAudioIcon);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 3;
			c.gridy = 3;
			gridbag.setConstraints(playB, c);
			playB.setBorder(BorderFactory.createEmptyBorder());		
			playB.setContentAreaFilled(false);		
			add(playB,c);		
			playB.setVisible(true);		
			playB.addActionListener(this);
			playB.setActionCommand("Play");
			
			JLabel l6 = new JLabel("<- Press this to stop recording.",stopIcon,JLabel.CENTER);
			l6.setFont(new Font("arial",Font.BOLD,12));
			c.gridx = 1;
			c.gridy = 4;
			gridbag.setConstraints(l6,c);
			add(l6,c);
			l6.setVisible(true); 
		} catch (Exception e) {}
		

	}

	public static void createAndShowGUI() {//Needs to be static in order to be called from the main method.
		JFrame frame = new JFrame("Tala svenska som en papegoja.");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JComponent newContentPane = new MainWindowPronounce();
		Color c = new Color(255,216,200);//kind of light peach
		newContentPane.setOpaque(true);
		newContentPane.setBackground(c); 
		frame.setContentPane(newContentPane);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(new Dimension(screenSize.width-50,screenSize.height-50));
		frame.pack();
		frame.setVisible(true);
	}	

	public static void readLinesToArray() {//Reads the textfile containing the wordlist to the arraylist words.
		try{	
			InputStream in = MainWindowPronounce.class.getResourceAsStream("svenska_ord"); 
			Closeable stream = in;
			try {
				localEncoding="UTF-8";
				Reader reader = new InputStreamReader(in,localEncoding);
				stream = reader;
     		StringBuilder inputBuilder = new StringBuilder();
      	char[] buffer = new char[1024];
      	while (true) {
        	int readCount = reader.read(buffer);
        	if (readCount < 0) {
          	break;
        	}
        inputBuilder.append(buffer, 0, readCount);
				String[] lines = inputBuilder.toString().split("\\n");
				words = new ArrayList <String>(); 
      	for (String i : lines){
      		words.add(i);
      	}	  				
        wordStrings = new String[words.size()];
				words.toArray(wordStrings);
			}


			for(int i=0;i<words.size();i++){
				System.out.println(words.get(i));
			}
						}catch(Exception e) {}
		} catch (Exception e) {}
		
			try{
			InputStream inputStream2 = MainWindowPronounce.class.getResourceAsStream("engelska_ord"); 	
			Closeable stream2 = inputStream2;
			try {
				localEncoding="UTF-8";
				Reader reader2 = new InputStreamReader(inputStream2,"UTF-8");
				stream2 = reader2;
     		StringBuilder inputBuilder2 = new StringBuilder();
      	char[] buffer = new char[1024];
      	while (true) {
        	int readCount = reader2.read(buffer);
        	if (readCount < 0) {
          	break;
        	}
        inputBuilder2.append(buffer, 0, readCount);
				String[] lines = inputBuilder2.toString().split("\\n");
				words2 = new ArrayList <String>(); 
      	for (String i : lines){
      		words2.add(i);
      	}	  				
        wordStrings2 = new String[words2.size()];
				words2.toArray(wordStrings2);
			}
			}catch(Exception e) {}	

			for(int i=0;i<words2.size();i++){
				System.out.println(words2.get(i));
			}
		} catch (Exception e) {}
	}
	
	public static void getOS() {//This was used in earlier versions to try to adress encoding issues, and it does no damage, so it was left in the code. 
		System.out.println(System.getProperty("os.name"));
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows")){
			System.out.println("A Windows computer.");
			windows=true;
		}
		if (os.startsWith("Linux")){
			System.out.println("A Linux computer."); 
		}
		
		String localEncodingShow = System.getProperty("file.encoding");
		System.out.println("This system uses "+localEncodingShow+" encoding.");
	}
	

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			if ("play audio".equals(e.getActionCommand())) { 	
				try{		
				System.out.println("The system will play audio now.");
				System.out.println("playing "+word+".wav");
				for (int i=0; i<word.length();i++){
					char c = word.charAt(i);
					System.out.println(c);
					if (c=='å'){
						funnyCharacter=true;
					}									
					if (c=='ä'){
						funnyCharacter=true;
					}
					if (c=='ö'){
						funnyCharacter=true;
					}					
				}
				if (funnyCharacter){
					word=word.replaceAll("ö","oe");//Because of problems with these three special swedish characters and different character encodings in Windows and Linux,
					word=word.replaceAll("ä","ae");//the .wav file for a word such as 'så' is named 'saa.wav' instead of så.wav.
					word=word.replaceAll("å","aa");//and analagous for ä and ö.
					System.out.println(word);
				}
				InputStream in = MainWindowPronounce.class.getResourceAsStream(word+".wav"); 							
				AudioStream as = new AudioStream(in);         
				AudioPlayer.player.start(as);
				} catch (Exception x) {}
			}
    Object obj = e.getSource();
    if (obj.equals(playB)) {
      	if ("Play".equals(e.getActionCommand())) {
        playback.start();
        captB.setEnabled(false);
        System.out.println("Attempting to play back recording...");
        playB.setIcon(new ImageIcon(getClass().getResource("stop_button.png")));
				playB.setBorder(BorderFactory.createEmptyBorder());		
				playB.setContentAreaFilled(false);	        
        playB.setActionCommand("stop");
      } else {
        playback.stop();
        captB.setEnabled(true);
        playB.setActionCommand("Play");
        playB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("playbutton.png"))));
        playB.setBorder(BorderFactory.createEmptyBorder());		
				playB.setContentAreaFilled(false);		
      }
    } else if (obj.equals(captB)) {
      if ("Record".equals(e.getActionCommand())) {
        capture.start();
        playB.setEnabled(false);
        System.out.println("Attempting recording...");
        captB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("stop_button.png"))));
				captB.setBorder(BorderFactory.createEmptyBorder());		
				captB.setContentAreaFilled(false);	        
        captB.setActionCommand("stop");
      } else {
        capture.stop();
        playB.setEnabled(true);
        playB.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("playbutton.png"))));
        captB.setActionCommand("Record");
        captB.setIcon(new ImageIcon(getClass().getResource("media_record.png")));
        captB.setBorder(BorderFactory.createEmptyBorder());		
				captB.setContentAreaFilled(false);
      }
      

    }
		}
		if (e.getSource() instanceof JComboBox) {
			System.out.println("the JComboBox is doing things.");
			JComboBox cb = (JComboBox)e.getSource();
			a = cb.getSelectedIndex();
			System.out.println("This is index of CB: "+a);
		  word2 = words2.get(a);
			word = (String)cb.getSelectedItem();
			word = word.trim();
			System.out.println("this is word: "+word);
			System.out.println("this is word2: "+word2);
			translation.setText("Simple translation : "+word2);
		}
	}
	
	public static void main(String[] args) {
		    //Schedule a job for the event-dispatching thread:
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		System.setProperty("file.encoding", "UTF-8");
					getOS();
			readLinesToArray();//Get list of common Swedish words.
			createAndShowGUI();
			}
		});
	}

}
