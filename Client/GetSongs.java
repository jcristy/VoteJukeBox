import java.util.ArrayList;
import javax.swing.*;

import java.util.TimerTask;
import java.net.*;
import java.io.*;

public class GetSongs extends TimerTask
	{
		String serverAddress;
		JPanel database;
		int sortBy = 0;
		ButtonGroup group;
		JRadioButton artist_radio,title_radio,votes_radio;
		ArrayList<Song> songs;
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
				toServer = new Socket(serverAddress,JukeBoxClient.MESSAGE);	
				OutputStream dataout = toServer.getOutputStream();
				OutputStream bos = dataout;//new OutputStream(dataout);
				BufferedReader in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
				bos.write(("USER\n").getBytes());
				bos.write((JukeBoxClient.GETSONGS+"\n").getBytes());
				
				String inputLine = "";
				while (!in.ready());
				inputLine = in.readLine();
				//NOW PLAYING
				String artist = getValueFromXML("artist",inputLine);
				String title  = getValueFromXML("title",inputLine);
				String vetoes = getValueFromXML("vetoes",inputLine);
				String users  = getValueFromXML("users",inputLine);
				String votes  = getValueFromXML("votes",inputLine);
				String album  = getValueFromXML("album",inputLine);
				String year   = getValueFromXML("year",inputLine);
				if ( database!=null)
				{
					JukeBoxClient.now_playing_label.setText("<html><h2>Now Playing</h2> "+
						"Title: "+title+"<br>"+
						"Artist: "+artist+"<br>"+
						"Album: " +album+" ("+year+")<br>"+
						"Votes/Users: "+votes+"/"+users);
					JukeBoxClient.veto_btn.setText("VETO "+vetoes+"/"+users);
				}
				else
				{
					System.out.println("Now Playing: "+title+" by "+artist+" on "+album+" ("+votes+"/"+users+")");
				}
				
				inputLine = in.readLine();
				
				songs = new ArrayList<Song>();
				
				//DATABASE
				while (!inputLine.contains("<END>"))
				{
					artist = getValueFromXML("artist",inputLine);
					title  = getValueFromXML("title",inputLine);
					final String filename = getValueFromXML("filename",inputLine);
					votes = getValueFromXML("votes",inputLine);
					album = getValueFromXML("album",inputLine);
					year  = getValueFromXML("year",inputLine);
					if (database!=null)
					{
						Song thisSong = new Song(title,artist,votes,filename,album,year);
						JukeBoxClient.song_database.addOrUpdate(thisSong);
					}
					else
					{
						System.out.println(title+" by "+artist+" in "+filename);
					}
					while (!in.ready());
					inputLine = in.readLine();
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
