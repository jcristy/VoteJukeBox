import java.util.ArrayList;
import java.util.Random;
import java.io.*;

public class JukeBoxDatabase
{
	private ArrayList<Song> Songs;
	private Random rand;

	public JukeBoxDatabase()
	{
		Songs = new ArrayList<Song>();
		rand = new Random();
	}
	public synchronized void addSong(Song s)
	{
		Songs.add(s);
		postUpdate();
	}
	public synchronized void remove(String s)
	{
		for (int i=0; i<Songs.size();i++)
		{
			if (Songs.get(i).getFilename().equals(s))
				Songs.remove(i);
		}
	}
	public synchronized void postUpdate()
	{
		String message = "Filename       Votes\n";
		for (int i=0; i<Songs.size();i++)
		{
			message = message + Songs.get(i).getFilename()+"  "+Songs.get(i).getVotes()+"\n";
		}
		JukeBoxServer.postUpdate(message,0);
	}
	public synchronized Song getHighestSong()
	{
		if (Songs.size()<1) return null;
		ArrayList<Song> potential = new ArrayList<Song>();
		potential.add(Songs.get(0));
		for (int i=1; i<Songs.size();i++)
		{
			if (Songs.get(i).getVotes() > potential.get(0).getVotes())
			{
				potential = new ArrayList<Song>();
				potential.add(Songs.get(i));
			}
			else if (Songs.get(i).getVotes() == potential.get(0).getVotes())
			{
				potential.add(Songs.get(i));
			}
		}
		//System.out.println("We have something");
		return potential.get(rand.nextInt(potential.size()));
	}
	public void printSongs()
	{
		for (int i=0; i<Songs.size();i++)
		{
			System.out.println(Songs.get(i).getFilename()+" votes:"+Songs.get(i).getVotes()+" title:"+Songs.get(i).getTitle()+" artist:"+Songs.get(i).getArtist());
		}
	}
	public void printSongs(PrintWriter out)
	{
		for (int i=0; i<Songs.size();i++)
		{
			//out.println(Songs.get(i).getFilename()+" "+Songs.get(i).getVotes());
			out.print("<song>");
			out.print("<filename>"+Songs.get(i).getFilename()+"</filename>");
			out.print("<artist>"+Songs.get(i).getArtist()+"</artist>");
			out.print("<title>"+Songs.get(i).getTitle()+"</title>");
			out.print("<votes>"+Songs.get(i).getVotes()+"</votes>");
			out.print("</song>");
			out.println();
		}
	}
	public int voteFor(String filename,String IPAddress)
	{
		for (int i=0; i<Songs.size();i++)
		{
			//System.out.println("Does <" + Songs.get(i).getFilename() + ">==<" + filename+">");
			if (Songs.get(i).getFilename().equals(filename))
			{
				int temp = Songs.get(i).voteFor(IPAddress);
				//System.out.println("Now we have this many votes: "+temp);
				postUpdate();
				return temp;
			}
		}
		//postUpdate();
		return -1;
	}
}
