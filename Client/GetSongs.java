import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;
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
				if (database!=null)
				{
					database.removeAll();
				}
				String inputLine = "";
				while (!in.ready());
				inputLine = in.readLine();
				String artist = getValueFromXML("artist",inputLine);
				String title  = getValueFromXML("title",inputLine);
				String vetoes = getValueFromXML("vetoes",inputLine);
				String users  = getValueFromXML("users",inputLine);
				String votes  = getValueFromXML("votes",inputLine);
				if ( database!=null)
				{
					JukeBoxClient.now_playing_label.setText("<html><h2>Now Playing</h2> "+
						"Title: "+title+"<br>"+
						"Artist: "+artist+"<br>"+
						"Votes/Users: "+votes+"/"+users);
					JukeBoxClient.veto_btn.setText("VETO "+vetoes+"/"+users);
				}
				else
				{
					System.out.println("Now Playing: "+title+" by "+artist+" ("+votes+"/"+users+")");
				}
				inputLine = in.readLine();
				
				songs = new ArrayList<Song>();
				if (group == null)
				{
					group = new ButtonGroup();
					title_radio = new JRadioButton("<HTML><b>Title</b><HTML>");
					title_radio.setSelected(true);
					artist_radio= new JRadioButton("<HTML><b>Artist</b><HTML>");
					votes_radio = new JRadioButton("<HTML><b>Votes</b><HTML>");
					title_radio.setToolTipText("Sort by title");
					artist_radio.setToolTipText("Sort by artist");
					votes_radio.setToolTipText("Sort by votes");
					group.add(title_radio);
					group.add(artist_radio);
					group.add(votes_radio);
					title_radio.setActionCommand("0");
					artist_radio.setActionCommand("1");
					votes_radio.setActionCommand("2");
					ActionListener sort_listener = new ActionListener(){
						public void actionPerformed(ActionEvent e) 
						{
							sortBy = Integer.parseInt(e.getActionCommand());
						}
					};
					title_radio.addActionListener(sort_listener);
					artist_radio.addActionListener(sort_listener);
					votes_radio.addActionListener(sort_listener);
				}
				database.add(title_radio);
				database.add(artist_radio);
				database.add(votes_radio);
				while (!inputLine.contains("<END>"))
				{
					artist = getValueFromXML("artist",inputLine);
					title  = getValueFromXML("title",inputLine);
					final String filename = getValueFromXML("filename",inputLine);
					votes = getValueFromXML("votes",inputLine);
					System.out.println("title"+title);
					if (database!=null)
					{
						Song thisSong = new Song(title,artist,votes,filename);
						if (songs.size()==0) 
							songs.add(thisSong);
						else
							for (int i=0; i< songs.size(); i++)
							{
								int comparisonResult = 0;
								switch (sortBy)
								{
									case 0://Title
										comparisonResult = thisSong.title.compareTo(songs.get(i).title);
									break;
									case 1://artist
										comparisonResult = thisSong.artist.compareTo(songs.get(i).artist);
									break;
									case 2://votes
										comparisonResult = -1*Integer.decode(thisSong.votes).compareTo(Integer.decode(songs.get(i).votes));
									break;
									default:
								}
								if (comparisonResult < 0)
								{
									songs.add(i,thisSong);
									System.out.println("Adding "+thisSong.title);
									break;
								}
								else if (i==songs.size()-1)
								{
									songs.add(thisSong);
									System.out.println("Adding "+thisSong.title);
									break;
								}
							}

					}
					else
					{
						System.out.println(title+" by "+artist+" in "+filename);
					}
					while (!in.ready());
					inputLine = in.readLine();
				}
				for (int i=0; i<songs.size(); i++)
				{
					final Song thisSong = songs.get(i);
					JButton voteFor = new JButton("VOTE ("+thisSong.votes+")");
					JLabel title_lbl = new JLabel(thisSong.title.length()>30?thisSong.title.substring(0,27)+"...":thisSong.title);
					title_lbl.setToolTipText(thisSong.title);
					database.add(title_lbl);
					JLabel artist_lbl = new JLabel(thisSong.artist.length()>30?thisSong.artist.substring(0,27)+"...":thisSong.artist);
					artist_lbl.setToolTipText(thisSong.artist);
					database.add(artist_lbl);
					database.add(voteFor);
					voteFor.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
								try{
									Voter voter = new Voter(serverAddress, thisSong.filename);
									Thread upload = new Thread(voter);
									upload.start();
								}catch(Exception ee){}					
						}
					});
				}
			}catch(Exception e){e.printStackTrace();}
			if (database!=null) 
			{	
				database.validate();
				JukeBoxClient.mainframe.validate();
			}
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
