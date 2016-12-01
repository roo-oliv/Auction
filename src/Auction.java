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
	private String id;
	private String product;
	private Bid startBid;
	//private Bid currentBid;
	private Date startDate;
	private Date endDate;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProduct() {
		return product;
	}

	public void setStartBid(Bid startBid) {
		this.startBid = startBid;
	}

	public Bid getStartBid() {
		return startBid;
	}
//
//	public void setCurrentBid(Bid currentBid) {
//		this.currentBid = currentBid;
//	}
//
//	public Bid getCurrentBid() {
//		return currentBid;
//	}

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
}