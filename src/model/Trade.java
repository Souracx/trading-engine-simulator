package model;

/**
 * Represents an executed trade between a BUY order and a SELL order.
 *
 * A Trade is created only by the Exchange when: bestBuyPrice >= bestSellPrice
 */
public class Trade {
	
    private final long tradeId; 	//unique id for this specific trade 
    private final String companyCode; //Ticker symbol 
    private final int price;	//execution price 
    private final int quantity; //shares executed 

    private final long buyOrderId; 	//Order ID of BUY order 
    private final String buyBroker;	//name of buyer

    private final long sellOrderId;	//Sell order ID 
    private final String sellBroker; //Seller name 

    private final long executedAtMillis;	//timestamp 

    //constructor 
    public Trade(long tradeId, String companyCode, int price, int quantity,
                 long buyOrderId, String buyBroker,
                 long sellOrderId, String sellBroker) {
    	//Validation 
        if (tradeId <= 0)
            throw new IllegalArgumentException("Trade id must be > 0.");
        if (companyCode == null || companyCode.isBlank())
            throw new IllegalArgumentException("Company code cannot be null/blank.");
        if (price < 1)
            throw new IllegalArgumentException("Price must be >= 1.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0.");

        if (buyOrderId <= 0)
            throw new IllegalArgumentException("Buy order id must be > 0.");
        if (buyBroker == null || buyBroker.isBlank())
            throw new IllegalArgumentException("Buy broker cannot be null/blank.");

        if (sellOrderId <= 0)
            throw new IllegalArgumentException("Sell order id must be > 0.");
        if (sellBroker == null || sellBroker.isBlank())
            throw new IllegalArgumentException("Sell broker cannot be null/blank.");

        this.tradeId = tradeId;
        this.companyCode = companyCode.trim().toUpperCase();
        this.price = price;
        this.quantity = quantity;

        this.buyOrderId = buyOrderId;
        this.buyBroker = buyBroker.trim();

        this.sellOrderId = sellOrderId;
        this.sellBroker = sellBroker.trim();

        // Capture execution time at creation
        this.executedAtMillis = System.currentTimeMillis();
    }

  
    //Getters  
    public long getTradeId() {return tradeId; } 
    public String getCompanyCode() { return companyCode; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public long getBuyOrderId() { return buyOrderId; }
    public String getBuyBroker() { return buyBroker; }

    public long getSellOrderId() { return sellOrderId; }
    public String getSellBroker() { return sellBroker; }

    public long getExecutedAtMillis() { return executedAtMillis; }


    @Override
    public String toString() {
        return "Trade#" + tradeId + " " +
               companyCode + " " +
               quantity + " @ $" + price +
               " | BUY " + buyBroker + "(#" + buyOrderId + ")" +
               " vs SELL " + sellBroker + "(#" + sellOrderId + ")";
    }
}