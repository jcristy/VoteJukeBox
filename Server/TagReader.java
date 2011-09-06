import org.jaudiotagger.audio.mp3.*;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
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
			attempted_read = false;
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
			attempted_read = false;
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
	
	public String getTitle(){
		if(tagsValid()){
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
	
	public String getAlbum(){
		if(tagsValid()){
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
	
	//Initialize Tag
	private void makeTag(){
		short version = checkTagVersion();
		
		if(version == 1){
			tag_v1 = (ID3v1Tag)mp3_file.getTag();
			has_v2_tag = false;
		}
		else if (version == 2){
			tag_v2 = mp3_file.getID3v2TagAsv24();
			has_v2_tag = true;
		}
		else{
			System.out.println("invalid version");
			good_tags = false;
		}
	}
	
	private short checkTagVersion(){
		if(mp3_file.hasID3v1Tag()){
			return 1; 
		}
		else if(mp3_file.hasID3v2Tag()){
			return 2;
		}
		else{
			return 0;
		}
	}
	
}
