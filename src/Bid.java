import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Bid implements Serializable {
	private String id;
	private Auction auction;
	private String owner;
	private float value;

	public void setAuction(Auction auction) {
		this.auction = auction;
	}

	public Auction getAuction() {
		return auction;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public static Bid fromBytes(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = new ObjectInputStream(bis);
		Bid bid = (Bid)in.readObject();
		bis.close();
		in.close();
		return bid;
	}

	public static byte[] toBytes(Bid bid) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(bid);
  		byte[] byteArray = bos.toByteArray();
		out.flush();
  		bos.close();
  		return byteArray;
	}
}