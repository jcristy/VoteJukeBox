import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

public class UploadSong implements Runnable
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
		Socket toServer;
		try{
			toServer = new Socket(serverAddress,JukeBoxClient.MESSAGE);	
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
			bos.write((JukeBoxClient.NOMINATE+"\n").getBytes());

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
					panel.validate();
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
						//System.out.print("\r"+status);
						if (panel!=null)
						{
							progress.setValue(Integer.parseInt(status.substring(0,status.indexOf(' '))));
							progress.setString(f.getName()+" "+status.substring(status.indexOf(')')+1));
							if (firsttime) panel.validate();
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
						panel.validate();
					}
					public void mousePressed (MouseEvent e){};
					public void mouseEntered (MouseEvent e){};
					public void mouseReleased (MouseEvent e){};
					public void mouseExited (MouseEvent e){};
				});
					panel.validate();
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
							panel.validate();
						}
						public void mousePressed (MouseEvent e){};
						public void mouseEntered (MouseEvent e){};
						public void mouseReleased (MouseEvent e){};
						public void mouseExited (MouseEvent e){};
					});
					panel.add(info);
					panel.validate();
				}
			}			
			//NOW SEND THE BYTES OF THE FILE
			dataout.close();
			bos.close();
			in.close();
			toServer.close();
			System.out.println("\nComplete");
		}catch(Exception e){System.out.println(e.toString());}
	}
}
