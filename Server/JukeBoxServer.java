

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.FlowLayout;

//import javax.sound.sampled;

public class JukeBoxServer
{
	public static JukeBoxDatabase fileDatabase = new JukeBoxDatabase();
	public static final int MESSAGE = 4411;
	//public static final int MUSIC   = 4451;

	//public static final String = "";
	private static boolean listening = true;
	private static JTextArea statusArea = new JTextArea(10,50);
	private static JTextArea songDatabase = new JTextArea(10,50);
	public static JFrame frame = new JFrame("JukeBox Server Status");
	public static final MusicPlayer mp = new MusicPlayer(1);
	
	public static void main(String[] args)
	{
		//Try to Rebuild the database
		Thread rebuildDatabase = new Thread(new Runnable(){
			public void run()
			{
				File dir = new File("uploads");
				FileFilter filter = new FileFilter()
				{
					public boolean accept(File file){
						return file.getName().endsWith(".mp3");
					}
				};
				File[] files = dir.listFiles(filter);
				for (int i=0; i<files.length; i++)
				{
					//fileDatabase.addSong(new Song(files[i].getName(),"","",""));
					
					TagReader tr = new TagReader(files[i]);
					JukeBoxServer.fileDatabase.addSong(new Song(files[i].getName(),"",tr.getArtist(),tr.getTitle()));
				}
			}
		});
		rebuildDatabase.start();
		//Continue
		FlowLayout fl = new FlowLayout();
		frame.getContentPane().setLayout(fl);
		frame.getContentPane().add(new JScrollPane(songDatabase));
		frame.getContentPane().add(new JScrollPane(statusArea));
		frame.pack();
		frame.setVisible(true);
		
		ServerSocket MessageServerSocket = null;
		Socket clientSocket;
		
		Thread musicPlayer = new Thread(mp);
				
		Runnable Control = new Runnable(){
			public void run()
			{
				try{
					while(true)
					{
						BufferedInputStream bis = new BufferedInputStream(System.in);
						String command = JukeBoxServer.readALine(bis);
						System.out.println("command "+command);
						if (command.equals("quit"))
						{
							shutdown();
						}
						else if (command.equals("printsongs"))
						{
							fileDatabase.printSongs();
						}
						else if (command.equals("nextsong"))
						{
							mp.nextSong();
						}
						else if (command.equals("getplayerstate"))
						{
							mp.getPlayerState();
						}
						else if (command.equals("restart"))
						{
							mp.restart();
						}
						else if (command.contains("remove"))
						{
							String toRemove = command.substring(command.indexOf(" ")+1);
							fileDatabase.remove(toRemove);
						}
						else if (command.contains("connections"))
						{
							System.out.println(JukeBoxMessageHandler.numberOfConnectionsUnsafe());
						}
					}
				}catch(Exception e){e.printStackTrace();}
			}
		};
		Thread control = new Thread(Control);
		control.start();

		musicPlayer.start();
		//0 is reserved for the database
		long thread_id = 10;
		try{
			MessageServerSocket = new ServerSocket(MESSAGE);
			MessageServerSocket.setSoTimeout(1000);
			while (listening)
			{
				try{
					clientSocket = MessageServerSocket.accept();
					//WAITS TIL THERE IS AN INCOMING REQUEST					
					JukeBoxMessageHandler jbmh = new JukeBoxMessageHandler(clientSocket,thread_id);
					thread_id++;
					Thread messageThread = new Thread(jbmh);
					messageThread.start();
					
				}catch(SocketTimeoutException e){printStatuses();}

				
			}
		}catch(Exception e){e.printStackTrace();}
		finally{
			try{
				if (MessageServerSocket!=null) MessageServerSocket.close();
			}catch(Exception e){e.printStackTrace();}
		}
		
	}
	private static ArrayList<clientstatus> statuses = new ArrayList<clientstatus>();
	public synchronized static void printStatuses()
	{
		String toShow = "";
		for (int i=0; i < statuses.size(); i++)
		{
			if (statuses.get(i).id < 1)
				songDatabase.setText(statuses.get(i).status);
			else
				toShow = toShow + statuses.get(i).id +" : "+ statuses.get(i).status +"\n";
		}
		statusArea.setText(toShow);
	}
	public synchronized static void postUpdate(String message,long id)
	{
		boolean add = true;
		for (int i=0; i < statuses.size(); i++)
		{
			if (statuses.get(i).id == id)
			{
				add = false;
				if (message.equalsIgnoreCase(""))
					statuses.remove(i);
				else
					statuses.get(i).status = message;
			}
		}
		if (add){
			clientstatus newstatus = new clientstatus(message,id);
			statuses.add(newstatus);
		}
	}
	public static class clientstatus
	{
		public String status = "";
		public long id;
		public clientstatus(String status, long id)
		{
			this.status = status;
			this.id = id;
		}
	}
	public static void shutdown() throws IOException
	{
		listening = false;
		System.exit(0);
	}
	public static String readALine(BufferedInputStream bis) throws IOException
	{
		String toReturn = "";
		byte[] data = new byte[1];
		do{
			bis.read(data,0,1);
			if (data[0]!= (byte)'\n')
				toReturn = toReturn + (char)data[0];
		}while (data[0] != (byte) '\n');
		return toReturn;
	}
}
