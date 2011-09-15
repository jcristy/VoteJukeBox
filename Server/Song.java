import java.util.GregorianCalendar;
import java.util.ArrayList;

public class Song
{
	private String filename;
	private String uploader;
	private String artist;
	private String title;
	private String album;
	private String album_year;
	private ArrayList<String> votes;
	private int downvotes;
	private long lastChange;
	private GregorianCalendar lastPlay;
	private GregorianCalendar uploaded;
	public Song(String filename, String uploader, String artist, String title,String album,String album_year)
	{
		this.filename = filename;
		this.uploader = uploader;
		this.artist = artist;
		this.title = title;
		this.album = album;
		this.album_year = album_year;
		votes = new ArrayList<String>();
		downvotes = 0;
		uploaded = new GregorianCalendar();
		lastChange = System.currentTimeMillis();
	}
	public int voteFor(String IPAddress)
	{
		for (int i=0; i<votes.size();i++)
		{
			if (votes.get(i).equals(IPAddress)) return votes.size();
		}
		votes.add(IPAddress);
		lastChange = System.currentTimeMillis();
		return votes.size();
	}
	public int getVotes()
	{
		return votes.size();
	}
	public void clearVotes()
	{
		lastChange = System.currentTimeMillis();
		votes.clear();
	}
	/*public long getLastChange() TODO: give the latest time of change to the database.
	{
		return lastChange();
	}*/	
	public String getTitle()
	{
		return title;
	}
	public String getFilename()
	{
		return filename;
	}
	public String getArtist()
	{
		return artist;	
	}
	public String getAlbum()
	{
		return album;
	}
	public String getYear()
	{
		return album_year;
	}
}
