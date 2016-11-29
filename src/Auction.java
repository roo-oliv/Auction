import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Auction implements Serializable {
	private String name;
	private String product;
	private float startBid;
	private float currentBid;
	private Date startDate;
	private Date endDate;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProduct() {
		return product;
	}

	public void setStartBid(float startBid) {
		this.startBid = startBid;
	}

	public float getStartBid() {
		return startBid;
	}

	public void setCurrentBid(float currentBid) {
		this.currentBid = currentBid;
	}

	public float getCurrentBid() {
		return currentBid;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public static Auction fromByte(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = new ObjectInputStream(bis);
		Auction auction = (Auction)in.readObject();
		bis.close();
		in.close();
		return auction;
	}

	public static byte[] toByte(Auction auction) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(auction);
  		byte[] byteArray = bos.toByteArray();
		out.flush();
  		bos.close();
  		return byteArray;
	}
}