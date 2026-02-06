package core;

import java.util.*;


import ds.BinaryHeap;
import model.Company;
import model.Order;
import model.Order.Side;
import model.Trade;

/**
 * Exchange is the core matching engine 
 */

public class Exchange {

    private final String name;	//Exchange name 

    private final Map<String, Company> companies = new HashMap<>();	//Listed companies 
    private final List<Broker> brokers = new ArrayList<>();			//Registered broker
    private final List<String> announcements = new ArrayList<>();	//Event log 
    private final List<Trade> tradeHistory = new ArrayList<>();		//Permanent trade history 
    private final Map<String, Book> books = new HashMap<>();		//Order books per company

    private long nextOrderId = 1;
    private long nextTradeId = 1;

    public Exchange(String name) {
        this.name = name;
    }

   
    /**
     * Lists a company into the Exchange 
     * 
     * @param company
     */

    public void listCompany(Company company) {		
        companies.put(company.getCode(), company);		//Registers the company 
        books.put(company.getCode(), new Book());		//Creates empty order book 
        announcements.add("LISTED: " + company);		//Announces the listing 
    }

    /**
     * Register a broker into the exchange 
     * @param broker
     */

    public void registerBroker(Broker broker) {
        brokers.add(broker);
        announcements.add("BROKER REGISTERED: " + broker.getName());
    }

    /**
     * Pulls all pending orders from all registered brokers, assigns IDs 
     * inserts them into correct company order book 
     * @return no. of accepted orders 
     */

    public int ingestOrdersFromBrokers() {
        int count = 0;
        
        //Loop all registered brokers
        for (Broker b : brokers) {
        	//Pull all pending Intent orders
            for (Order o : b.getPendingOrders()) {
            	
            	//Safety check that broker can't assign an ID 
                if (o.getId() != 0)
                    throw new IllegalStateException("Broker orders must be unassigned");
                
                Book book = books.get(o.getCompanyCode());
                if (book == null) continue;	//If company is not listed 

                //Creates New order with exchanged assigned ID 
                Order assigned = new Order( 
                        nextOrderId++, o.getBrokerName(), o.getCompanyCode(), o.getSide(), o.getLimitPrice(), o.getQuantity()
                );

                book.add(assigned);	//Insert in BUY or SELL heap 
                announcements.add("ORDER ACCEPTED: " + assigned); //log the accepted order 
                count++;
            }
            b.clearPendingOrders(); //clear so that orders aren't ingested twice
        }

        return count;
    }

    /**
     * Attempts to match all orders 
     * @return total trades executed 
     */

    public int matchAll() {
        int trades = 0;
        
        // run matching loop for all listed ticker
        for (String code : books.keySet())
            trades += matchCompany(code);
        return trades;
    }

    private int matchCompany(String code) {
        Book book = books.get(code);
        Company company = companies.get(code);
        int executed = 0;

        while (true) {
            Order buy = book.peekBestBuy();
            Order sell = book.peekBestSell();

            if (buy == null || sell == null) break;
            if (buy.getLimitPrice() < sell.getLimitPrice()) break;

            buy = book.pollBestBuy();
            sell = book.pollBestSell();

            int qty = Math.min(buy.getQuantity(), sell.getQuantity());

            // Price-time priority: execution at resting (sell) price
            int price = sell.getLimitPrice();

            Trade trade = new Trade(
                    nextTradeId++, code, price, qty,
                    buy.getId(), buy.getBrokerName(),
                    sell.getId(), sell.getBrokerName()
            );

            tradeHistory.add(trade);
            announcements.add(trade.toString());

            company.adjustPrice(price - company.getPrice());

            buy.reduceQuantity(qty);
            sell.reduceQuantity(qty);

            if (buy.getQuantity() > 0) book.add(buy);
            if (sell.getQuantity() > 0) book.add(sell);

            executed++;
        }

        return executed;
    }

    //getters 
    public List<String> getAnnouncements() {
        return new ArrayList<>(announcements);
    }
    public List<Trade> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }

    /**
     * Book represents the per-company limit order book 
     * 
     */
    private static class Book {
        private final BinaryHeap<Order> buys;	//buy-side priority queue (max-heap) 
        private final BinaryHeap<Order> sells;	//sell-side priority queue (min-heap) 

        Book() {
        	//buy comparator
        	//higher price first and if similar smaller id first
            buys = new BinaryHeap<>((a, b) -> {
                int p = Integer.compare(b.getLimitPrice(), a.getLimitPrice());
                return p != 0 ? p : Long.compare(a.getId(), b.getId());
            });
            
            //sell comparator 
            //lower price first and if similar smaller id first 
            sells = new BinaryHeap<>((a, b) -> {
                int p = Integer.compare(a.getLimitPrice(), b.getLimitPrice());
                return p != 0 ? p : Long.compare(a.getId(), b.getId());
            });
        }
        
        //adds an order
        void add(Order o) {
            if (o.getSide() == Side.BUY) buys.add(o);
            else sells.add(o);
        }

        Order peekBestBuy() {
            return skipInactivePeek(buys);
        }

        Order peekBestSell() {
            return skipInactivePeek(sells);
        }

        Order pollBestBuy() {
            return skipInactivePoll(buys);
        }

        Order pollBestSell() {
            return skipInactivePoll(sells);
        }
        
        
        //Lazy deletion helper for peak 
        private Order skipInactivePeek(BinaryHeap<Order> heap) {
            while (true) {
                Order o = heap.peek();
                if (o == null || o.isActive()) return o;
                heap.poll();
            }
        }
        
        
        //Lazy deletion helper for poll 
        private Order skipInactivePoll(BinaryHeap<Order> heap) {
            while (!heap.isEmpty()) {
                Order o = heap.poll();
                if (o.isActive()) return o;
            }
            return null;
        }
    }
}