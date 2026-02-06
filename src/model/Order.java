package model;

/**
 * Represents a limit order submitted by a broker.
 * Orders are mutable only for remaining quantity and active state.
 */
public class Order {
	
	//Buy = bid ,SELL = ask 
    public enum Side { BUY, SELL }

    private final long id;                 //order identifier 
    private final String brokerName;	   //name of broker that own the order
    private final String companyCode;	   // Company code (Ticker) 
    
    //Order Details 
    private final Side side;				//Buy or Sell 
    private final int limitPrice;			//max buy price or min sell price 
    private int quantity;					//Remaining quantity 
    
    //Order is still active or not 
    private boolean active;

    //Constructor 
    public Order(long id, String brokerName, String companyCode,
                 Side side, int limitPrice, int quantity) {

        if (id < 0)
            throw new IllegalArgumentException("Order id must be >= 0.");
        if (brokerName == null || brokerName.isBlank())
            throw new IllegalArgumentException("Broker name cannot be null/blank.");
        if (companyCode == null || companyCode.isBlank())
            throw new IllegalArgumentException("Company code cannot be null/blank.");
        if (side == null)
            throw new IllegalArgumentException("Order side cannot be null.");
        if (limitPrice <= 0)
            throw new IllegalArgumentException("Limit price must be > 0.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0.");

        this.id = id;
        this.brokerName = brokerName.trim();
        this.companyCode = companyCode.trim().toUpperCase();
        this.side = side;
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.active = true;	//Order starts active by default 
    }

    //getters 
    public long getId() { return id; }
    public String getBrokerName() { return brokerName; }
    public String getCompanyCode() { return companyCode; }
    public Side getSide() { return side; }
    public int getLimitPrice() { return limitPrice; }
    public int getQuantity() { return quantity; }
    public boolean isActive() { return active; }

    //Cancel the order but it still stays on the heap 
    public void cancel() {
        active = false;
    }

    /**
     * Reduces remaining quantity.
     * Silent if already inactive 
     */
    public void reduceQuantity(int filled) {
        if (!active) return;

        quantity -= filled;
        if (quantity <= 0) {
            quantity = 0;
            active = false;
        }
    }

    @Override
    public String toString() {
        String idStr = (id == 0 ? "UNASSIGNED" : "#" + id);
        return "Order" + idStr + " " + side + " " + quantity + " " + companyCode +
                " @ $" + limitPrice + " (" + brokerName + ")" +
                (active ? "" : " [INACTIVE]");
    }
}