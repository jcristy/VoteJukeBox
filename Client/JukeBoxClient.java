// JukeBoxClient.java
//Code by: JOHN CRISTY 9/6/2011
//
import java.io.*;
import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.Container;

import java.awt.GridLayout;
import java.util.Timer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class JukeBoxClient
{
	public static final int MESSAGE = 4411;
	public static final String NOMINATE = "NOMINATE";
	public static final String VOTEFOR  = "VOTEFOR";
	public static final String GETSONGS = "GETSONGS";

	public static int getsongs_rate = 5000;

	public static JFrame mainframe;
	public static JLabel server_address_lbl;
	public static JTextField server_address_tf;
	public static JLabel now_playing_label;
	public static JButton veto_btn;
	public static JSeparator separator;
		public static JScrollPane database_song_table_scrollpane;
		public static JPanel database_song_table;
		
		public static JPanel upload_song_panel;
		public static JLabel  upload_song_header_lbl;
		public static JButton send_song_btn;
		public static JPanel upload_statuses;
	//public static 
	
	public static GetSongs gs;
	public static Timer getSongs;
	public static SongDatabase song_database;
	
	public static void main(String[] args) 
	{
		if (args.length > 0 ) //CMD LINE
		{
			
			if (args.length == 3 && args[0].equals("vote"))
			{
				Thread vote = new Thread(new Voter(args[1],args[2]));
				vote.start();
			}
			else if (args.length >= 3 && args[0].equals("upload"))
			{
				for (int i=2;i<args.length;i++)
				{
					UploadSong terminal = new UploadSong(args[1],args[i]);//UploadSOng(Server,filename)
					Thread upload = new Thread(terminal);
					upload.start();
				}
			}
			else if (args.length == 2 && args[0].equals("listsongs"))
			{
				gs = new GetSongs(args[1]);
				gs.run();
			}
			else
			{
				System.out.println("upload:    java JukeBoxClient upload <server> <filename> (filename...)");
				System.out.println("vote:      java JukeBoxClient vote <server> <filename>");
				System.out.println("listsongs: java JukeBoxClient listsongs <server>");
				System.out.println("or no arguments for GUI");
			}
		}
		else //WITH GUI
		{

			mainframe = new JFrame("VoteBox Client");
			server_address_lbl = new JLabel("Server address:");
			final File config = new File("config");
			String hostIP = "";
			if (config.exists())
			{
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(config)));
					hostIP = br.readLine();
				}catch(Exception e){}
			}
			else
			{
				try{
					config.createNewFile();
				}catch(Exception e){}
			}
			server_address_tf = new JTextField(hostIP,15);
			//server_address_tf = new JTextArea(hostIP,1,15);
			server_address_tf.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    change();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    change();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    change();
			  }
			  public void change()
			  {
			  	try{
			  		FileOutputStream fos = new FileOutputStream(config);
				  	fos.write(server_address_tf.getText().getBytes());
				}catch(Exception e){}
			  	gs = new GetSongs(server_address_tf.getText(),database_song_table);
			  	getSongs.cancel();
				getSongs = new Timer();
				getSongs.schedule(gs,0,getsongs_rate);
			  }
			});
			
			now_playing_label = new JLabel("<html><h2>Now Playing</h2><br>Title:<br>Artist:<br>Votes:");
			veto_btn = new JButton("VETO");
			veto_btn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					try{
						Voter voter = new Voter(server_address_tf.getText(), "VETO");
						Thread upload = new Thread(voter);
						upload.start();
					}catch(Exception ee){}
				}
			});
			separator = new JSeparator();
		
			database_song_table = new JPanel();
			int NUM_COLUMNS = 4;
			GridLayout bl_database_table = new GridLayout(0,NUM_COLUMNS);
			database_song_table.setLayout(bl_database_table);
			for (int i=0; i<NUM_COLUMNS*10; i++);	
				database_song_table.add(new JButton("You Shouldn't See This"));
			song_database = new SongDatabase(database_song_table);
			
			upload_song_panel = new JPanel();
			upload_song_header_lbl = new JLabel("Upload Files");
			send_song_btn = new JButton("Upload Song");
			send_song_btn.setActionCommand("upload file");
			send_song_btn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					JFileChooser fc = new JFileChooser();
					//FileNameExtensionFilter filter = new FileNameExtensionFilter("mp3");
					//fc.addChoosableFileFilter(filter);
					fc.setMultiSelectionEnabled(true);
					if (fc.showOpenDialog(mainframe) == JFileChooser.APPROVE_OPTION)
					{
						try{
							File files[] = fc.getSelectedFiles();
							for (int i=0; i<files.length; i++)
							{
								String filename = files[i].getCanonicalPath()/*+File.separator+fc.getSelectedFile().getName()*/;
								UploadSong terminal = new UploadSong(server_address_tf.getText(),filename,upload_statuses);//UploadSOng(Server,filename)
								Thread upload = new Thread(terminal);
								upload.start();
							}
						}catch(Exception ee){}
					}
					
				}
			});
			upload_statuses = new JPanel();
			BoxLayout bl_upload_statuses = new BoxLayout(upload_statuses, BoxLayout.Y_AXIS);
			upload_statuses.setLayout(bl_upload_statuses);
				
			Container contentpane = mainframe.getContentPane();
			BoxLayout bl = new BoxLayout(contentpane,BoxLayout.Y_AXIS);
			contentpane.setLayout(bl);
			JPanel ServerAddressLine = new JPanel();
				BoxLayout bl_server_address = new BoxLayout(ServerAddressLine,BoxLayout.X_AXIS);
				ServerAddressLine.setLayout(bl_server_address);
				ServerAddressLine.add(server_address_lbl);
				ServerAddressLine.add(server_address_tf);
				ServerAddressLine.add(new JLabel(" "));
			contentpane.add(ServerAddressLine);
			contentpane.add(now_playing_label);
			contentpane.add(veto_btn);
			contentpane.add(separator);
			database_song_table_scrollpane = new JScrollPane(database_song_table,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			contentpane.add(database_song_table_scrollpane);
			contentpane.add(separator);
			contentpane.add(upload_song_header_lbl);
			contentpane.add(send_song_btn);
			contentpane.add(new JScrollPane(upload_statuses,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainframe.pack();
			database_song_table.removeAll();
			
			mainframe.setVisible(true);
			gs = new GetSongs(server_address_tf.getText(),database_song_table);
			getSongs = new Timer();
			getSongs.schedule(gs,0,getsongs_rate);//loads song list every 5 seconds
		}	 
	}
	public static String getServerAddress()
	{
		return	server_address_tf.getText();
	}
}
