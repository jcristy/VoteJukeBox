import org.jaudiotagger.audio.mp3.*;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.id3.*;

import java.io.*;

//generic handler to get tags from an MP3 file
public class TagReader {
	String filename;
	MP3File mp3_file;
	boolean good_tags; 
	boolean attempted_read;
	boolean has_v2_tag;
	
	ID3v24Tag tag_v2;
	ID3v1Tag tag_v1;
	
	String length;
	String bitRate;
	String sampleHz;
	
	TagReader(){
		attempted_read = false;
		good_tags = false; 
	}
	
	TagReader(File inputFile){
		filename = inputFile.getName();
		try{
			attempted_read = true;
			mp3_file = (MP3File)AudioFileIO.read(inputFile);	
		}
		catch(IOException e){
			good_tags = false;
		}
		catch(CannotReadException e){
			good_tags = false;
		}
		catch(InvalidAudioFrameException e){
			good_tags = false;
		}
		catch(ReadOnlyFileException e){
			good_tags = false;
		}
		catch(TagException e){
			System.out.println("Invalid Tags");
			good_tags = false;
		}
		
		MP3AudioHeader header = mp3_file.getMP3AudioHeader();
		
		retrieveInfo(header);
		
		makeTag();
	}
	
	public boolean isSetup(){
		return attempted_read;
	}
	
	public void readFile(File inputFile){
		try{
			attempted_read = true;
			mp3_file = (MP3File)AudioFileIO.read(inputFile);	
		}
		catch(IOException e){
			good_tags = false;
		}
		catch(CannotReadException e){
			good_tags = false;
		}
		catch(InvalidAudioFrameException e){
			good_tags = false;
		}
		catch(ReadOnlyFileException e){
			good_tags = false;
		}
		catch(TagException e){
			System.out.println("Invalid Tags");
			good_tags = false;
		}
		
		MP3AudioHeader header = mp3_file.getMP3AudioHeader();
		
		retrieveInfo(header);
		
		makeTag();
	}
	
	public boolean tagsValid(){
		return good_tags;
	}
	
	//initialize MP3file info
	private void retrieveInfo(MP3AudioHeader header){
		bitRate = header.getBitRate();
		length = header.getTrackLengthAsString();
		sampleHz = header.getSampleRate();
	}
	
	public String getArtist(){
		if(tagsValid()){
			if(tagHasArtistTitle()){
				if(has_v2_tag){
					return tag_v2.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
				}
				else{
					return tag_v1.getArtist().get(0).toString();
				}
			}
			else{
				return "";
			}
		}
		else{
			return "";
		}
	}
	
	public String getTitle(){
		if(tagsValid()){
			if(tagHasArtistTitle()){
				if(has_v2_tag){
					return tag_v2.getFirst(ID3v24Frames.FRAME_ID_TITLE);
				}
				else{
					return tag_v1.getFirstTitle();
				}
			}
			else{
				return filename;
			}
		}
		else{
			return filename;
		}
	}
	
	public String getAlbum(){
		if(tagsValid()){
			if(tagHasArtistTitle()){
				if(has_v2_tag){
					return tag_v2.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
				}
				else{
					return tag_v1.getAlbum().get(0).toString();
				}
			}
			else{
				return "";
			}
		}
		else{
			return "";
		}
	}
	
	public String getArtistTitleName(){
		if(tagHasArtistTitle()){
			return getArtist()+ " - " + getTitle();
		}
		else{
			return getTitle();
		}
	}
	//Only do if you have checked that the tags are valid
	private boolean tagHasArtistTitle(){
		int version = checkTagVersion();
		if(!tagsValid()){
			return false;
		}
		if(version == 1){
			if(tag_v1.getArtist().get(0).toString().equals("") && tag_v1.getFirstTitle().equals("")){
				return false;
			}
			else{
				return true;
			}
		}
		else if(version == 2){
			if(tag_v2.getFirst(ID3v24Frames.FRAME_ID_ARTIST).equals("") && 
					tag_v2.getFirst(ID3v24Frames.FRAME_ID_TITLE).equals("")){
				return false;
			}
			else{
				return true;
			}
		}
		else{
			return false;
		}
	}
	
	//Initialize Tag - sets good_tags to true if successful
	private void makeTag(){
		short version = checkTagVersion();
		System.out.println("Tag Version: " + version);
		if(version == 1){
			tag_v1 = (ID3v1Tag)mp3_file.getTag();
			has_v2_tag = false;
			good_tags = true;
		}
		else if (version == 2){
			tag_v2 = mp3_file.getID3v2TagAsv24();
			has_v2_tag = true;
			good_tags = true;
		}
		else{
			System.out.println("invalid version");
			good_tags = false;
		}
	}
	
	private short checkTagVersion(){
		if(mp3_file.hasID3v1Tag() && !mp3_file.hasID3v2Tag()){
			return 1; 
		}
		else if(mp3_file.hasID3v2Tag()){
			return 2;
		}
		else{
			return 0;
		}
	}
	
	
	/* Testing Main
	public static void main(String argv[]){
		System.out.println("TagReader Test");
		
		File test1 = new File("/Users/kelsongent/Downloads/02 - Hearts On Fire.mp3");
		File test2 = new File("/Users/kelsongent/Downloads/war_room.mp3");
		
		TagReader readerTest1 = new TagReader(test1);
		TagReader readerTest2 = new TagReader(test2);
		
		if(readerTest1.isSetup() && readerTest1.tagsValid()){
			System.out.println("Test 1 Information");
			System.out.println("Other: " + readerTest2.tag_v2.getFieldCount());
			System.out.println("Artist: " + readerTest1.getArtist());
			System.out.println("Title: " + readerTest1.getTitle());
			System.out.println("Album: " + readerTest1.getAlbum());
		}
		else if(readerTest1.isSetup()){
			System.out.println("Test 1 Information");
			System.out.println("No Tags");
			System.out.println("Filename: " + readerTest2.getTitle());
		}
		else{
			System.out.println("NOT INITIALIZED");
		}
		
		if(readerTest2.isSetup() && readerTest2.tagsValid()){
			System.out.println("Test 2 Information");
			System.out.println("Other: " + readerTest2.tag_v2.getFieldCount());
			System.out.println("Artist: " + readerTest2.getArtist());
			System.out.println("Title: " + readerTest2.getTitle());
			System.out.println("Album: " + readerTest2.getAlbum());
		}
		else if(readerTest2.isSetup()){
			System.out.println("Test 2 Information");
			System.out.println("No Tags");
			System.out.println("Filename: " + readerTest2.getTitle());
		}
		else{
			System.out.println("NOT INITIALIZED");
		}
	}
	*/
}
