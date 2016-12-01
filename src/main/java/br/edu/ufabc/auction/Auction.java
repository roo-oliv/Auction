package br.edu.ufabc.auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

public class Auction implements Serializable {
    private /*final*/ String id;
    private final String product;
    private final Float minimumBid;
    private final Date startDate;
    private final Date endDate;

    private Bid currentBid;

    private static final Logger LOG;
    static {
        LOG = LoggerFactory.getLogger(Auction.class);
    }

    public static class Builder {
        private String id;
        private String product;
        private Float minimumBid;
        private Date startDate;
        private Date endDate;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder product(String product) {
            this.product = product;
            return this;
        }

        public Builder minimumBid(Float minimumBid) {
            this.minimumBid = minimumBid;
            return this;
        }

        public Builder startDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }

        public Auction build() {
            if(/*id==null ||*/ product==null || minimumBid==null || startDate==null || endDate==null) {
                return null;
            }
            return new Auction(this);
        }
    }

    private Auction(Builder builder) {
        id = builder.id;
        product = builder.product;
        minimumBid = builder.minimumBid;
        startDate = builder.startDate;
        endDate = builder.endDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public Float getStartBid() {
        return minimumBid;
    }

    public void setCurrentBid(Bid currentBid) {
        this.currentBid = currentBid;
    }

    public Bid getCurrentBid() {
        return currentBid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
