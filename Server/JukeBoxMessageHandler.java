import java.net.*;
import java.io.*;
//import org.farng.mp3;

public class JukeBoxMessageHandler implements Runnable
{
	public static final String NOMINATE = "NOMINATE";
	public static final String VOTEFOR  = "VOTEFOR";
	public static final String GETSONGS = "GETSONGS";

	public static final int maxUploadConnections = 8;
	public static int incomingConnections = 0;
	public static final boolean DEBUG = false;

	public Socket clientSocket;
	public long id;
	
	public JukeBoxMessageHandler(Socket socket,long id)
	{
		clientSocket = socket;
		this.id = id;
	}
	public void run()
	{
		try{
			//MessageServerSocket = new ServerSocket(MESSAGE);
			//int i=0;
		
			//i++;
			//clientSocket = MessageServerSocket.accept();
			
			//BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
			PrintWriter os = new PrintWriter(clientSocket.getOutputStream(),true);
			
			String IPAddress = clientSocket.getInetAddress().toString();
			
			if (DEBUG) System.out.println("Start reading message");
			
			String username = JukeBoxServer.readALine(bis);
			if (DEBUG) System.out.println("User: "+username);
			
			String inputLine = JukeBoxServer.readALine(bis);
			if (DEBUG) System.out.println("Action: "+inputLine);
			if (inputLine.equals(GETSONGS))
			{
				os.println(JukeBoxServer.mp.nowPlaying.getFilename());
				JukeBoxServer.fileDatabase.printSongs(os);
				os.println("<END>");
			}
			else if (inputLine.equals(VOTEFOR))
			{
				inputLine = JukeBoxServer.readALine(bis);
				//System.out.println("VOTEFOR    "+inputLine);
				os.println(inputLine+" "+JukeBoxServer.fileDatabase.voteFor(inputLine,IPAddress));
			}
			else if (inputLine.equals(NOMINATE))
			{
				String Filename;
				//while ((Filename = in.readLine())!=null)
				Filename = JukeBoxServer.readALine(bis);
				if (DEBUG) System.out.println("Filename: "+Filename);
				long length = 0;
				String temp;
				//while ((temp = in.readLine())!=null)
		

		
				File d = new File("uploads/");
				if (!d.exists())
					d.mkdir();
		
				File f;
		
				f = new File("uploads/"+Filename);
				if (f.exists())
				{
					String reply = "GOTIT<END>";
					os.println(reply);
				}
				else
				{
				   String reply;
					if (!askForConnection())
					{
						reply = "WAIT";
						os.println(reply);
						while (!askForConnection())
						{
							Thread.sleep(15000);//wait 15 seconds before checking again
						}
					}
					
					try
					{
						reply = "SENDFILE<END>";
						os.println(reply);
						temp = JukeBoxServer.readALine(bis);
			
						length = Long.parseLong(temp);
						if (DEBUG) System.out.println("Length:"+length);	

						if (DEBUG) System.out.println(f.getAbsolutePath());
		
						long startTime = System.currentTimeMillis();
		
						f.createNewFile();
						FileOutputStream fos = new FileOutputStream(f);
		
		
						//byte data;
						long j=0;
						long time = 0;
		
						while (j<length)
						{
							j++;
							byte[] data = new byte[1];
							bis.read(data,0,1);
			
							if ((j&0xFFFF) == 0x8000)
							{
								long current = System.currentTimeMillis();
								long delta = (current - time)/32;
								double rate = 1/(delta/1000.0);
								int eta  = (int)(((length-j)/1024.0)/rate);
								String rateS = String.format("%.4f",rate);
								//System.out.print("\r"+((int)((double)j/length)*100)+"%  ("+j+") rate:"+rateS+" KiB/s  Time Remaining "+(eta<60?eta+"s":eta/60+" m "+eta%60+" s")+"     ");
								int percentage = (int) ((((double)j)/length)*100);
								String status = Filename+" "+percentage+"%  ("+j+") rate:"+rateS+" KiB/s  Time Remaining "+(eta<60?eta+"s":eta/60+" m "+eta%60+" s")+" from: "+clientSocket.getInetAddress().toString();
								JukeBoxServer.postUpdate(status,id);
								os.println(percentage+" %  ("+j+") rate:"+rateS+" KiB/s  Time Remaining "+(eta<60?eta+"s":eta/60+" m "+eta%60+" s")+"     ");
								time = current;
							}
				
							//System.out.println(data[0] + " >>> "+data[0]);
							fos.write(data[0]);
						}
						if (DEBUG) System.out.println("\nReceived "+Filename+" in "+((System.currentTimeMillis()-startTime)/1000)+" s");
						String status = ("Received "+Filename+" in "+((System.currentTimeMillis()-startTime)/1000)+" s");
						JukeBoxServer.postUpdate(status,id);
						os.println(status);
						fos.close();
						//Thread.sleep(60000);
						//JukeBoxServer.postUpdate("",id);
					}
					catch(Exception ee)
					{
						ee.printStackTrace();
						
					}
					finally
					{
						finishConnection();
					}
					TagReader tr = new TagReader(f);
					
					JukeBoxServer.fileDatabase.addSong(new Song(Filename,username,tr.getArtist(),tr.getTitle()));
					JukeBoxServer.fileDatabase.voteFor(Filename,IPAddress);
				}

			}
			bis.close();
			
			
			os.flush();
			os.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
			//System.exit(0);
		}
		//System.out.println("Done");
	}
	public synchronized static boolean askForConnection()
	{
		if (incomingConnections<maxUploadConnections)
		{
			incomingConnections++;
			return true;
		}
		else
		{
			return false;
		}
	}
	public synchronized static void finishConnection()
	{
		incomingConnections--;
	}
	public static int numberOfConnectionsUnsafe()
	{
		return incomingConnections;
	}
}
