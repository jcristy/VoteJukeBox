import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.event.*;
public class Song
{
	public static final int YEAR = -1;
	public static final int TITLE = 0;
	public static final int ARTIST = 1;
	public static final int ALBUM = 2;
	public static final int VOTES = 3;
	
	public String title;
	public String artist;
	public String votes;
	public final String filename;
	public String album;
	public String year;
	public JLabel title_lbl;
	public JLabel artist_lbl;
	public JLabel album_lbl;
	public JButton votes_btn;
	public Song(String title, String artist, String votes, String filename,String album,String year)
	{
		this.title = title;
		this.artist = artist;
		this.votes = votes;
		this.filename = filename;
		this.album = album;
		this.year = year;
		title_lbl = new JLabel(title.length()<30?title:(title.substring(0,27)+"..."));
		title_lbl.setToolTipText(title);
		artist_lbl= new JLabel(artist.length()<30?artist:(artist.substring(0,27)+"..."));
		artist_lbl.setToolTipText(artist);
		String album_temp = album+" ("+year+")";
		album_lbl = new JLabel(album_temp.length()<30?album_temp:(album_temp.substring(0,27)+"..."));
		album_lbl.setToolTipText(album_temp);
		votes_btn = new JButton("VOTE (0)");
		votes_btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
					try{
						Voter voter = new Voter(JukeBoxClient.getServerAddress(), Song.this.filename);
						Thread upload = new Thread(voter);
						upload.start();
					}catch(Exception ee){}					
			}
		});
	}
	public boolean equals(Song other)
	{
		return (title.equals(other.title) && artist.equals(other.artist) && filename.equals(other.filename) && album.equals(other.album));
	}
	public void setVotes(String votes)
	{
		this.votes = votes;
		votes_btn.setText("VOTE ("+votes+")");
		JukeBoxClient.database_song_table.validate();
	}
	public JComponent[] getGUIComponents()
	{
		JComponent[] toReturn = {title_lbl,artist_lbl,album_lbl,votes_btn};
		return toReturn;
	}
}
