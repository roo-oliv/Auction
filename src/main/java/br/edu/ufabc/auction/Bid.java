package br.edu.ufabc.auction;

public class Bid {
    private final float value;
    private final Long buyerId;
    private final Long auctionId;

    public static class Builder {
        private float value;
        private Long buyerId;
        private Long auctionId;

        public Builder value(float value) {
            this.value = value;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder auctionId(Long auctionId) {
            this.auctionId = auctionId;
            return this;
        }

        public Bid build() {
            return new Bid(this);
        }
    }

    private Bid(Builder builder) {
        this.value = builder.value;
        this.buyerId = builder.buyerId;
        this.auctionId = builder.auctionId;
    }

    public float getValue() {
        return value;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getAuctionId() {
        return auctionId;
    }
}
