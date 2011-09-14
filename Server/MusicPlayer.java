//import javax.media.*;
import javazoom.jl.player.Player;
import java.io.*;
import java.util.ArrayList;
import java.net.URL;
import java.net.URI;
import java.awt.Component;

public class MusicPlayer implements Runnable
{
	public static ArrayList<Song> toPlay;

	public Song nowPlaying;
	public int id;
	private Player player;
	
	public MusicPlayer(int id)
	{
		this.id = id;
		toPlay = new ArrayList<Song>();
	}
	boolean playing = false;
	public void run()
	{
		System.out.println("Up and Running");
		while(true)
		{
		
			JukeBoxServer.postUpdate("Looking for a song to play",id);
			nowPlaying = JukeBoxServer.fileDatabase.getHighestSong();
			while (nowPlaying == null) nowPlaying = JukeBoxServer.fileDatabase.getHighestSong();
			String filename = nowPlaying.getFilename();
			//System.out.println("Will try to play"+filename);
			try{				
				File f = new File("uploads/"+filename);
				URL theURL = f.toURI().toURL();
				//MediaLocator ml = new MediaLocator(theURL);
				//player = Manager.createRealizedPlayer(ml);
				player = new Player(new FileInputStream(f));
				
				/*player.realize();
				player.start();
				player.addControllerListener(new ControllerListener(){
					public void controllerUpdate(ControllerEvent event)
					{
						if (event instanceof EndOfMediaEvent)
						{
							player.stop();
							player.close();
							System.out.println("Finished playing the song");
							nowPlaying.clearVotes();
							playing = false;
						}
						else
						{
							System.out.println("Event: "+event);
						}
						
					}
				});*/
				playing = true;
				
				JukeBoxServer.postUpdate("Playing "+filename,id);
				//boolean end = false;
				//while (playing & !end)
				//{	
					player.play();
					
				//}
				//System.out.println("Done playing");
				//while (!player.isComplete())
				nowPlaying.clearVotes();
				JukeBoxServer.clearVetoes();
				JukeBoxServer.fileDatabase.postUpdate();
				player.close();
			}catch(Exception e){e.printStackTrace();}
			
			
		}
	}
	public void getPlayerState()
	{
	/*
		System.out.println("Media Time: "+player.getMediaTime().getSeconds());
		System.out.print("state: ");
		switch (player.getState())
		{
			case Controller.Prefetched:
				System.out.println("prefetched");
				break;
			case Controller.Prefetching:
				System.out.println("prefetching");
				break;
			case Controller.Realized:
				System.out.println("realized");
				break;
			case Controller.Realizing:
				System.out.println("realizing");
				break;
			case Controller.Started:
				System.out.println("started");
				break;
			case Controller.Unrealized:
				System.out.println("unre");
				break;
		}
	*/
		
	}
	public synchronized void nextSong()
	{
		
		playing = false;
		player.close();
	/*
		
		if (player==null)
			return;
		player.stop();
		player.close();
		*/
	}
	public synchronized void stop()
	{
		
	}
	public synchronized void pause()
	{
		if (player == null)
			return;
		//player.stop();	
	}
	public synchronized void restart()
	{
		if (player == null)
			return;
		//player.start();
	}

	/*public synchronized void addToQueue(Song song)
	{
		toPlay.add(song);
		song.voteFor();
	}*/

	public synchronized String nowPlaying()
	{
		if (nowPlaying!=null)
			return nowPlaying.getTitle();
		else
			return "Nothing is Playing";
	}
}
