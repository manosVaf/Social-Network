
/*
Xrhsimopoieitai gia thn epikoinwnia metaxu client server!! 
to pedio PacketCode tha mas deixei ti tupou einai...
an einai -1 shmainei oti 8elei stoixeia gia na graftei ston grafo!
an einai 0 einai oi plhrofories username kai password
an einai 1 einai ACK oti swstos client
an einai 2 einai NACk oti lathos username kai password h oti den vrethike
an einai 3 einai to kainourgio tweet gia grapsimo mazi me to siz tou arxeiou ston client
an einai 4 einai oti zhtaei tous client pou akolouthei
an einai 5 einai ACK me thn lista me tous client pou akolou8ei
an einai 6 einai oti zhtame to profil tou client
an einai 7 einai ACK mazi me String to profile tou
an einai 8 einai aithsh gia upload mazi me onoma arxeiou kai ori8mo bytes
an einai 9 einai Ack oti einai etoimos gia upload
an einai 10 einai oti zhtaei tous client pou ton akolouthoun
an einai 11 einai ACK me thn lista me tous client pou akolou8ei
an einai 12 einai o follower pou 8elei na svhsei o pelaths
an einai 13 einai pelaths pou akolou8ei allon
an einai 14 einai to tragoudi pou psaxnei
an einai 15 einai h lista me tous clients pou to exoun!!
an einai 16 aithsh client gia download
an einai 17 einai ACK gia client gia 3ekinhma download
an einai 18 einai NACK gia client gia 3ekinhma download
an einai 19 einai stoixeia download gia server
an einai 20 einai to paketo me ta dedomena apo ton server
an einai 21 einai ACK tou client gia ta dedomena 
*/

package Both;

import java.io.Serializable;
import java.util.*;

public class Packet implements Serializable {
	private int PacketCode;					// kwdikos gia na 3eroume ti na perimenoume sto paketo
	private String PacketInformation;	// analoga me ton kwdiko perimeneis kai thn antistoixh plhroforia
	private static final long  serialVersionUID = -733857438754387543L;
	private String tweet;
	private String profile;
	private String numberoffollowing;
	private String username;
	private String songname;
	private int followertoDelete;
	private long songsize;
	private ArrayList<Integer> following;
	private ArrayList<Integer> followers;
	private int clients;
	private String songtitle;
	private String ClientsWithSong;
	private ArrayList<Integer> followingwithSong;
	private int DownloadPacket;
	byte[] receivedbytes;
	
	
	public Packet(int code, int numberofPacket, byte[] bytes){
		PacketCode = code;
		DownloadPacket = numberofPacket;
		receivedbytes = bytes;
	}
	
	public Packet(int code, int numberofPacket){
		PacketCode = code;
		DownloadPacket = numberofPacket;
	}

	public Packet(int code, String username, String password){
		if(code == 3){
			PacketCode = 3;
			tweet = password;
			this.username = username;
		}else if(code == 4 || code == 6 || code == 10){
			if(code == 4 || code == 10){
				if(code == 4){
					PacketCode = 4;
					this.username = username;
				}else{
					PacketCode = 10;
					this.username = username;
				}
			}else {
				PacketCode = 6;
				this.username = username;
				numberoffollowing = password;				//einai o ari8mos pou exw dwsei gia na parei to profile tou
			}
		}else if(code == 7){
			PacketCode = 6;
			profile = username;
		}else if(code == 9){
			PacketCode = 9;
		}else if(code == 12){
			PacketCode = 12;
			this.username = username;
			followertoDelete = Integer.parseInt(password);
		}else if(code == 13){
			PacketCode = 13;
			this.username = username;
			profile = password;						
		}else if(code == 14 || code == 19){
			if(code == 14)PacketCode = 14;
			else PacketCode = 19;
			this.username = username;
			songname = password;
		}else{
			setPacketInformation(username, password);
		}
	}
	
	public Packet(int code){
		if(code == -1)PacketCode = -1;
		if(code == 13)PacketCode = 13;
		if(code == 16)PacketCode = 16;
		if(code == 17)PacketCode = 17;
		if(code == 18)PacketCode = 18;
	}
	
	public Packet(int code, ArrayList<Integer> Following, String clientsSong){
		PacketCode = code;
		ClientsWithSong = clientsSong;
		followingwithSong = Following;
	}
	
	public Packet(int code, ArrayList<Integer> list, int sizeofclients){
		if(code == 5){
			PacketCode = code;
			following = list;
			clients = sizeofclients;
		}else if(code == 11){
			PacketCode = code;
			followers = list;
			clients = sizeofclients;
		}
	}
	
	public Packet(int code, String clientName, String songname, long songsize){
		PacketCode = code;
		username = clientName;
		this.songname = songname;
		this.songsize = songsize;
	}

	public int getDownloadPacket() {
		return DownloadPacket;
	}

	public byte[] getReceivedbytes() {
		return receivedbytes;
	}

	public ArrayList<Integer> getFollowingwithSong() {
		return followingwithSong;
	}

	public String getClientsWithSong() {
		return ClientsWithSong;
	}

	public String getSongtitle() {
		return songtitle;
	}
	
	public int getClients() {
		return clients;
	}
	
	public int getFollowertoDelete() {
		return followertoDelete;
	}
	
	public String getSongname() {
		return songname;
	}

	public long getSongsize() {
		return songsize;
	}

	public String getProfile(){
		return profile;
	}
	
	public String getNumberofFollwing(){
		return numberoffollowing;
	}
	

	public ArrayList<Integer> getFollowing() {
		return following;
	}

	public ArrayList<Integer> getFollowers() {
		return followers;
	}

	public String getClientName(){
		return username;
	}
	
	public int getPacketCode() {
		return PacketCode;
	}
	public void setPacketCode(int packetCode) {
		PacketCode = packetCode;
	}
	public String getPacketInformation() {
		return PacketInformation;
	}
	private void setPacketInformation(String username, String password) {
		PacketInformation = "Username = " + username + " Password = " + password;
	}			

	public String getUsername() {
		StringTokenizer token = new StringTokenizer(PacketInformation, " ");
		token.nextToken();
		token.nextToken();
		String username = token.nextToken().trim();
		String temp = token.nextToken().trim();
		if(temp.equalsIgnoreCase("password"))
			return username;
		else
			return username.concat(temp);
	}

	public String getPassword() {
		StringTokenizer token = new StringTokenizer(PacketInformation, " ");
		if(token.countTokens() == 8){
			token.nextToken();
			token.nextToken();
			token.nextToken();
			token.nextToken();
			token.nextToken();
			token.nextToken();
			String password = token.nextToken().trim().concat(token.nextToken());
			return password;
		}else{
			token.nextToken();
			token.nextToken();
			token.nextToken();
			token.nextToken();
			token.nextToken();
			String password = token.nextToken().trim();
			return password;
		}
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String Tweet) {
		tweet = Tweet;
	}
}
