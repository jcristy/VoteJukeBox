import javax.swing.*;
import java.net.*;
import java.io.*;

public class Voter implements Runnable
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
			toServer = new Socket(serverAddress,JukeBoxClient.MESSAGE);	
			OutputStream dataout = toServer.getOutputStream();
			OutputStream bos = dataout;//new OutputStream(dataout);
			BufferedReader in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
			bos.write("USER\n".getBytes());
			bos.write((JukeBoxClient.VOTEFOR+"\n").getBytes());
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
			String artist = getValueFromXML("artist",inputLine);
			String title  = getValueFromXML("title",inputLine);
			String votes  = getValueFromXML("votes",inputLine);
			String album  = getValueFromXML("album",inputLine);
			String year   = getValueFromXML("year",inputLine);
			JukeBoxClient.song_database.addOrUpdate(new Song(title,artist,votes,filename,album,year));
			JukeBoxClient.song_database.fillThis();
			if (button!=null)
			{
				//System.out.println(inputLine);
				//button.setText("Vote For "+inputLine);
			}
			else
			{
				System.out.println(inputLine);
			}
		}catch(Exception e){e.printStackTrace();}
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

