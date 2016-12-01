package br.edu.ufabc.auction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Seller {
    static final String HOST = "localhost";
    static final String DATE_PATTERN = "dd/MM/yyyy hh:mm:ss";

    Broker broker;
    Auction auction;

    public Seller(Broker broker, Auction auction) {
        this.broker = broker;
        this.auction = auction;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // recebe os valores para criar uma Auction
        System.out.println("Leiloar produto");
        System.out.printf("Nome do produto: ");
        String produto = br.readLine();
        System.out.printf("Lance inicial (R$): ");
        float startValue = Float.parseFloat(br.readLine());
        System.out.printf("Data de início (%s): ", DATE_PATTERN);
        Date startDate = new SimpleDateFormat(DATE_PATTERN).parse(br.readLine());
        System.out.printf("Prazo (minutos): ");
        int deadline = Integer.parseInt(br.readLine());
        Date endDate = new Date(startDate.getTime() + (deadline * 60000));

        // cria uma Auction
        Auction.Builder auctionBuilder = new Auction.Builder();
        auctionBuilder.product(produto);
        auctionBuilder.minimumBid(startValue);
        auctionBuilder.startDate(startDate);
        auctionBuilder.endDate(endDate);

        Broker broker = new Broker(HOST);
        broker.connect();

        long secondsRemaining = Math.abs(startDate.getTime() - new Date().getTime()) / 1000;
        System.out.printf("Aguarde. O leilão irá começar em %s\n", Util.formatTime(secondsRemaining));
        // cria o znode da auction
        broker.createAuction(auctionBuilder);
        System.out.printf("Leilão iniciado\n");

        while (endDate.after(new Date())) {
            Bid bid = broker.pollBid();
            Bid bestBid = broker.getBestBid();

            if (bestBid==null || bid.getValue() > bestBid.getValue()) {
                broker.updateBestBid(bid);
                System.out.printf("Melhor lance: %s - R$ %.2f\n", bid.getBuyerId(), bid.getValue());
            }
        }
    }

    public Auction getAuction() {
        return auction;
    }
}