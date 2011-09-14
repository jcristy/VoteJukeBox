// JukeBoxClient.java
//Code by: JOHN CRISTY 9/6/2011
//
import java.net.*;
import java.io.*;
import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.Container;

import java.awt.GridLayout;
import java.util.TimerTask;
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
	public static JLabel now_playing_lbl;
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
			server_address_tf = new JTextField(hostIP);
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
			
			now_playing_lbl = new JLabel("Now Playing");
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
			GridLayout bl_database_table = new GridLayout(10,0);
			database_song_table.setLayout(bl_database_table);
			for (int i=0; i<8; i++);	
				database_song_table.add(new JButton("You Shouldn't See This"));
			
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
			contentpane.add(server_address_lbl);
			contentpane.add(server_address_tf);
			contentpane.add(now_playing_lbl);
			contentpane.add(veto_btn);
			contentpane.add(separator);
			database_song_table_scrollpane = new JScrollPane(database_song_table,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			contentpane.add(database_song_table_scrollpane);
			contentpane.add(separator);
			contentpane.add(upload_song_header_lbl);
			contentpane.add(send_song_btn);
			contentpane.add(separator);
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

	public static class GetSongs extends TimerTask
	{
		String serverAddress;
		JPanel database;
		public GetSongs(String address, JPanel db)
		{
			serverAddress = address;
			database = db;
		}
		public GetSongs(String address)
		{
			serverAddress = address;
			database = null;
		}
		public void run()
		{
			Socket toServer;
			try{
				toServer = new Socket(serverAddress,MESSAGE);	
				OutputStream dataout = toServer.getOutputStream();
				OutputStream bos = dataout;//new OutputStream(dataout);
				BufferedReader in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
				bos.write(("USER\n").getBytes());
				bos.write((GETSONGS+"\n").getBytes());
				if (database!=null)
				{
					database.removeAll();
				}
				String inputLine = "";
				while (!in.ready());
				inputLine = in.readLine();
				if ( database!=null)
				{
					now_playing_lbl.setText(inputLine);
				}
				else
				{
					System.out.println("Now Playing: "+inputLine);
				}
				inputLine = in.readLine();
				while (!inputLine.contains("<END>"))
				{
					String artist = getValueFromXML("artist",inputLine);
					String title  = getValueFromXML("title",inputLine);
					final String filename = getValueFromXML("filename",inputLine);
					String votes = getValueFromXML("votes",inputLine);
					if (database!=null)
					{
						final JButton voteFor = new JButton(title+" by "+artist+"("+votes+")");
						database.add(voteFor);
						voteFor.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e)
						{
								try{
									Voter voter = new Voter(serverAddress, filename);
									Thread upload = new Thread(voter);
									upload.start();
								}catch(Exception ee){}					
						}
						});
					}
					else
					{
						System.out.println(title+" by "+artist+" in "+filename);
					}
					while (!in.ready());
					inputLine = in.readLine();
					
					
				}
				
			}catch(Exception e){e.printStackTrace();}
			if (database!=null) mainframe.validate();
		}
		public String getValueFromXML(String tag, String xml)
		{
			int begin;
			int end;
			begin = xml.indexOf("<"+tag+">")+tag.length()+2;
			end   = xml.indexOf("</"+tag+">");
			return xml.substring(begin,end);
		}
	}
	
	public static class Voter implements Runnable
	{
		String filename;
		JButton button;
		String serverAddress;
		
		public Voter(String ServerAddress, String fn)
		{
			filename = fn;
			serverAddress = ServerAddress;
		}
		public Voter(String ServerAddress, JButton btn)
		{
			button = btn;
			serverAddress = ServerAddress;
		}
		public void run()
		{
			Socket toServer;
			String inputLine;
			try{
				toServer = new Socket(serverAddress,MESSAGE);	
				OutputStream dataout = toServer.getOutputStream();
				OutputStream bos = dataout;//new OutputStream(dataout);
				BufferedReader in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
				bos.write("USER\n".getBytes());
				bos.write((VOTEFOR+"\n").getBytes());
				//System.out.println("VOTE FOR");
				if (button!=null)
				{
					String text = button.getText();
					//System.out.println("before:"+text);
					text = text.substring(9,text.lastIndexOf(" "));
					bos.write(text.getBytes());
					bos.write("\n".getBytes());
					//System.out.println(">"+text);					
				}
				else
				{
					bos.write((filename +"\n").getBytes());
				}
				//System.out.println("Waiting for ACK");
				//Wait for Response			
				while (!in.ready());
				inputLine = in.readLine();
				if (button!=null)
				{
					//System.out.println(inputLine);
					button.setText("Vote For "+inputLine);
				}
				else
				{
					System.out.println(inputLine);
				}
			}catch(Exception e){e.printStackTrace();}
		}
	}
	public static class UploadSong implements Runnable
	{
		String serverAddress;
		String Filename;
		JPanel panel;
		public UploadSong(String server, String filename, JPanel Panel)
		{
			serverAddress = server;
			Filename = filename;
			panel = Panel;
		}
		public UploadSong(String server, String filename)
		{
			panel = null;
			serverAddress = server;
			Filename = filename;
		}
		public void run()
		{
			System.out.println("upload song");
			Socket toServer;
			try{
				toServer = new Socket(serverAddress,MESSAGE);	
				OutputStream dataout = toServer.getOutputStream();
				OutputStream bos = dataout;//new OutputStream(dataout);
				BufferedReader in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
				File f = new File(Filename);
				//bos.write((args[0]+"\n").getBytes());
				//bos.write("<END>\n".getBytes());
				//out.println(args[0]);
				//out.println("<END>");
				//bos.flush();
				bos.write("USER\n".getBytes());
				bos.write((NOMINATE+"\n").getBytes());

				//out.close();
				String inputLine = "";
				//while (!in.ready());
		
				//String reply = ;		
				//out.println("test<BREAK>");
				bos.write((f.getName()+"\n").getBytes());
		
				inputLine = in.readLine();
				//System.out.println(inputLine);
				final JProgressBar progress;
				progress = new JProgressBar(0,100);
				progress.setValue(0);
				progress.setStringPainted(true);
				if (panel!=null)
				{
					panel.add(progress);	
				}
				if (inputLine.contains("WAIT"))
				{
					if (panel!=null)
					{
						panel.add(progress);
						progress.setValue(0);
						progress.setString("Server has Maximum Connections, Please Wait");
						mainframe.validate();
					}
					else
					{
						System.out.println("Server has Maximum Connections, Please Wait");
					}	
					inputLine = in.readLine();
				}
				if (inputLine.contains("SENDFILE"))
				{
					boolean firsttime = true;
					
					FileInputStream fis = new FileInputStream(f);
					//out.println(fis.available()+"<BREAK>");
					bos.write((fis.available()+"\n").getBytes());
					while (fis.available()!=0)
					{
						byte data = (byte)fis.read();
						bos.write(data);
						if (in.ready())
						{
							String status = in.readLine();
							System.out.print("\r"+status);
							if (panel!=null)
							{
								progress.setValue(Integer.parseInt(status.substring(0,status.indexOf(' '))));
								progress.setString(f.getName()+" "+status.substring(status.indexOf(')')+1));
								if (firsttime) mainframe.validate();
								firsttime=false;
							}
						}
					}
					if (panel != null)
					{
						progress.setValue(100);
						progress.setString("Completed Transfer of "+f.getName());
						progress.addMouseListener(new MouseListener(){
						public void mouseClicked(MouseEvent e)
						{
							panel.remove(progress);
						}
						public void mousePressed (MouseEvent e){};
						public void mouseEntered (MouseEvent e){};
						public void mouseReleased (MouseEvent e){};
						public void mouseExited (MouseEvent e){};
					});
						mainframe.validate();
					}
					
				}
				else
				{
					System.out.println("OK");
					if (panel!=null)
					{
						final JLabel info = new JLabel(f.getName()+" is already on the server");
						info.addMouseListener(new MouseListener(){
							public void mouseClicked(MouseEvent e)
							{
								panel.remove(info);
							}
							public void mousePressed (MouseEvent e){};
							public void mouseEntered (MouseEvent e){};
							public void mouseReleased (MouseEvent e){};
							public void mouseExited (MouseEvent e){};
						});
						panel.add(info);
						mainframe.validate();
					}
				}			
				//NOW SEND THE BYTES OF THE FILE
				dataout.close();
				bos.close();
				in.close();
				toServer.close();
				System.out.println("\nComplete");
				gs.run();
			}catch(Exception e){System.out.println(e.toString());}
		}
	}
}
