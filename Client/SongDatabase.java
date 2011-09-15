import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.event.*;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
//import javax.swing.JPanel;

public class SongDatabase
{
	ArrayList<Song> songs;
	int[] sortBy = {Song.ARTIST,Song.YEAR,Song.ALBUM,Song.TITLE};
	JPanel panel;
	JRadioButton artist_radio,album_radio,title_radio,votes_radio;
	ButtonGroup group;
	public SongDatabase(JPanel panel)
	{
		songs = new ArrayList<Song>();
		this.panel = panel;
	}
	public void addOrUpdate(Song s)
	{
		for (int i=0; i< songs.size(); i++)
		{
			if (songs.get(i).equals(s))
			{
				songs.get(i).setVotes(s.votes);
				//CALL THE SONG LIST REDRAW
				return;	
			}
		}
		songs.add(s);
		fillThis();
		panel.validate();
	}
	public void fillThis()
	{
		ArrayList<Song> sorted = new ArrayList<Song>();
		for (int ii=0; ii < songs.size(); ii++)
		{
			Song thisSong = songs.get(ii);
			if (sorted.size()==0) 
			{
				sorted.add(thisSong);
			}
			else
			{
				for (int i=0; i< sorted.size(); i++)
				{
					int comparisonResult = 0;
					int sortByIndex = 0;
					while (comparisonResult==0 && sortBy.length>sortByIndex)
					{
						switch (sortBy[0])
						{
							case Song.TITLE://Title
								comparisonResult = thisSong.title.compareTo(sorted.get(i).title);
							break;
							case Song.ARTIST://artist
								comparisonResult = thisSong.artist.compareTo(sorted.get(i).artist);
							break;
							case Song.VOTES://votes
								comparisonResult = -1*Integer.decode(thisSong.votes).compareTo(Integer.decode(sorted.get(i).votes));
							break;
							case Song.YEAR:
								comparisonResult = Integer.decode(thisSong.year).compareTo(Integer.decode(sorted.get(i).year));
							break;
							case Song.ALBUM:
								comparisonResult = thisSong.album.compareTo(sorted.get(i).album);
							break;
							default:
						}
						sortByIndex ++;
					}
					if (comparisonResult < 0)
					{
						sorted.add(i,thisSong);
						break;
					}
					else if (i==sorted.size()-1)
					{
						sorted.add(thisSong);
						break;
					}
				}
			}
		}
		//Now build the GUI
		//ALWAYS THE SAME
		panel.removeAll();
		if (group == null)
		{
			group = new ButtonGroup();
			title_radio = new JRadioButton("<HTML><b>Title</b><HTML>");
			title_radio.setToolTipText("Sort by title");
			group.add(title_radio);
			title_radio.setActionCommand(""+Song.TITLE);
			title_radio.setSelected(true);
									
			artist_radio= new JRadioButton("<HTML><b>Artist</b><HTML>");
			artist_radio.setToolTipText("Sort by artist");
			group.add(artist_radio);
			artist_radio.setActionCommand(""+Song.ARTIST);

			album_radio = new JRadioButton("<HTML><b>Album</b><HTML>");
			album_radio.setToolTipText("Sort by album");
			group.add(album_radio);
			album_radio.setActionCommand(""+Song.ALBUM);
									
			votes_radio = new JRadioButton("<HTML><b>Votes</b><HTML>");
			votes_radio.setToolTipText("Sort by votes");
			group.add(votes_radio);
			votes_radio.setActionCommand(""+Song.VOTES);
			

			
			ActionListener sort_listener = new ActionListener(){
				public void actionPerformed(ActionEvent e) 
				{
					sortBy[0] = Integer.parseInt(e.getActionCommand());
					fillThis();
				}
			};
			title_radio.addActionListener(sort_listener);
			artist_radio.addActionListener(sort_listener);
			votes_radio.addActionListener(sort_listener);
			album_radio.addActionListener(sort_listener);
		}
		panel.add(title_radio);
		panel.add(artist_radio);
		panel.add(album_radio);
		panel.add(votes_radio);
		//END ALWAYS THE SAME
		for (int i=0; i<sorted.size();i++)
		{
			JComponent[] components = sorted.get(i).getGUIComponents();
			for (int j=0; j< components.length; j++)
				panel.add(components[j]);
		}
		panel.validate();
	}
}
