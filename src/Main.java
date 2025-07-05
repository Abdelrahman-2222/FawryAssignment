import java.time.LocalDate;
import java.util.*;

interface Shippable {
    String getName();

    double getWeight();
}

abstract class Product {
    // To not be changed by subclasses, except for the quantity
    private final String name;
    private final int price;
    private int quantity;

    public Product(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters only needed for the fields
    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // deduction of quantity
    public void deductQuantity(int amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("Not enough quantity available");
        }
        quantity -= amount;
    }

    // Override function for products
    public void checkExpiration() {
    }

    ;
}

class ExpirableProduct extends Product {
    private final LocalDate expirationDate;

    public ExpirableProduct(String name, int price, int quantity, LocalDate expirationDate) {
        super(name, price, quantity);
        this.expirationDate = expirationDate;
    }

    @Override
    public void checkExpiration() {
        if (LocalDate.now().isAfter(expirationDate)) {
            throw new IllegalStateException("Product " + getName() + " is expired");
        }
    }
}

class NonExpirableProduct extends Product {
    public NonExpirableProduct(String name, int price, int quantity) {
        super(name, price, quantity);
    }
}

class ShippableExpirableProduct extends ExpirableProduct implements Shippable {
    private final double weight;

    public ShippableExpirableProduct(String name, int price, int quantity, LocalDate expirationDate, double weight) {
        super(name, price, quantity, expirationDate);
        this.weight = weight;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class ShippableNonExpirableProduct extends NonExpirableProduct implements Shippable {
    private final double weight;

    public ShippableNonExpirableProduct(String name, int price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class Cart {
    private final Map<Product, Integer> products = new LinkedHashMap<>();

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getQuantity() <= 0) {
            throw new IllegalArgumentException("Product quantity must be greater than zero");
        }
        products.put(product, product.getQuantity());
    }

    public void addProduct(Product product, int quantity) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
        if (quantity > product.getQuantity()) throw new IllegalArgumentException("Not enough quantity available in the product");
        products.put(product, products.getOrDefault(product, 0) + quantity);
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public Map<Product, Integer> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }
}

class Customer {
    private final String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        balance += amount;
    }

    public void deductBalance(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Not enough balance available");
        }
        balance -= amount;
    }
}

class ShippingService {
    public static void Ship(Map<Shippable, Integer> itemsToShip) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        for (Map.Entry<Shippable, Integer> entry : itemsToShip.entrySet()) {
            Shippable product = entry.getKey();
            int quantity = entry.getValue();
            double weight = product.getWeight() * quantity;
            totalWeight += weight;
            System.out.printf("%dx %-15s %.0fg%n", quantity, product.getName(), product.getWeight() * 1000);
        }
        System.out.printf("Total package weight %.1fkg%n%n", totalWeight);
    }
}


class ECommerceSystem {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) throw new IllegalStateException("Cart is empty");

        double subtotal = 0;
        double shippingFee = 0;
        List<Shippable> toShip = new ArrayList<>();

        // Validate each item
        for (var entry : cart.getProducts().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();

            // check expired
            p.checkExpiration();

            // check stock
            if (qty > p.getQuantity()) {
                throw new IllegalStateException("Product out of stock: " + p.getName());
            }

            subtotal += p.getPrice() * qty;

            // collect shippable
            if (p instanceof Shippable) {
                Shippable shp = (Shippable) p;
                // add one instance per unit
                for (int i = 0; i < qty; i++) {
                    toShip.add(shp);
                    shippingFee += 10; // flat 10 per item for demo
                }
            }
        }

        double total = subtotal + shippingFee;
        if (customer.getBalance() < total) {
            throw new IllegalStateException("Insufficient balance: need " + total + ", have " + customer.getBalance());
        }

        // Deduct stock and balance
        for (var entry : cart.getProducts().entrySet()) {
            entry.getKey().deductQuantity(entry.getValue());
        }
        customer.deductBalance(total);

        // Ship if needed
        if (!toShip.isEmpty()) {
            // Convert List<Shippable> to Map<Shippable, Integer> (count occurrences)
            Map<Shippable, Integer> shipMap = new LinkedHashMap<>();
            for (Shippable s : toShip) {
                shipMap.put(s, shipMap.getOrDefault(s, 0) + 1);
            }
            ShippingService.Ship(shipMap);
        }

        // Print receipt
        System.out.println("** Checkout receipt **");
        for (var entry : cart.getProducts().entrySet()) {
            System.out.printf("%dx %-10s %.0f%n",
                    entry.getValue(), entry.getKey().getName(), (double)(entry.getKey().getPrice() * entry.getValue()));
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal         %.0f%n", subtotal);
        System.out.printf("Shipping         %.0f%n", shippingFee);
        System.out.printf("Amount           %.0f%n%n", total);
        System.out.printf("Customer balance: %.0f%n", customer.getBalance());
    }
}

public class Main {
    public static void main(String[] args) {
        // Test the implementation
        ShippableExpirableProduct cheese = new ShippableExpirableProduct("Cheese",
                300, 70, LocalDate.of(2028,
                12, 31), 0.5);
        ShippableExpirableProduct biscuits = new ShippableExpirableProduct("Biscuits",
                200, 50, LocalDate.of(2026,
                1, 15), 0.10);
        ShippableNonExpirableProduct tv = new ShippableNonExpirableProduct("TV",
                2000, 1, 10.0);
        ShippableNonExpirableProduct scratchCard = new ShippableNonExpirableProduct("Scratch Card",
                100, 50, 0.01);
        ShippableExpirableProduct hamada = new ShippableExpirableProduct("Hamada",
                500, 10, LocalDate.of(2027,
                12, 31), 0.5);

//        // Expired product test
//        Customer customer = new Customer("Ahmed", 2500);
//        Cart cart = new Cart();
//        //cart.addProduct(cheese);
//        cart.addProduct(biscuits, 2);
//        cart.addProduct(scratchCard, 10);
//
//        try
//        {
//            ECommerceSystem.checkout(customer, cart);
//        }catch(Exception e)
//        {
//            System.out.println("Checkout failed: " + e.getMessage());
//        }
//
//        // Insufficient balance test
//        Customer customer2 = new Customer("Ziad", 1500);
//        Cart cart2 = new Cart();
//        cart2.addProduct(tv, 1);
//        try
//        {
//            ECommerceSystem.checkout(customer2, cart2);
//        }catch (Exception e)
//        {
//            System.out.println("Checkout failed: " + e.getMessage());
//        }

        // Correct Example handling
        Customer customer3 = new Customer("Sara", 20000);
        Cart cart3 = new Cart();
        cart3.addProduct(cheese, 2);
        //cart3.addProduct(tv, 1);
        cart3.addProduct(biscuits, 1);
        try {
            ECommerceSystem.checkout(customer3, cart3);
        } catch (Exception e) {
            System.out.println("Checkout failed: " + e.getMessage());
        }
    }
}