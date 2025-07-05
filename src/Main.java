import java.time.LocalDate;
import java.util.*;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

public class Main
{
    interface Shippable
    {
        String getName();
        double getWeight();
    }
    abstract class Product
    {
        // To not be changed by subclasses, except for the quantity
        private final String name;
        private final int price;
        private int quantity;

        public Product(String name, int price, int quantity)
        {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        // Getters only needed for the fields
        public String getName()
        {
            return name;
        }
        public int getPrice()
        {
            return price;
        }
        public int getQuantity()
        {
            return quantity;
        }
        // deduction of quantity
        public void deductQuantity(int amount)
        {
            if (amount > quantity)
            {
                throw new IllegalArgumentException("Not enough quantity available");
            }
            quantity -= amount;
        }
        // Override function for products
        public void checkExpiration(){};
    }

    public class ExpirableProduct extends Product
    {
        private final LocalDate expirationDate;

        public ExpirableProduct(String name, int price, int quantity, LocalDate expirationDate)
        {
            super(name, price, quantity);
            this.expirationDate = expirationDate;
        }

        @Override
        public void checkExpiration()
        {
            if (LocalDate.now().isAfter(expirationDate))
            {
                throw new IllegalStateException("Product " + getName() + " is expired");
            }
        }
    }

    public class NonExpirableProduct extends Product
    {
        public NonExpirableProduct(String name, int price, int quantity)
        {
            super(name, price, quantity);
        }
    }

    public class ShippableExpirableProduct extends ExpirableProduct implements Shippable
    {
        private final double weight;

        public ShippableExpirableProduct(String name, int price, int quantity, LocalDate expirationDate, double weight)
        {
            super(name, price, quantity, expirationDate);
            this.weight = weight;
        }

        @Override
        public String getName()
        {
            return super.getName();
        }

        @Override
        public double getWeight()
        {
            return weight;
        }
    }

    public class ShippableNonExpirableProduct extends NonExpirableProduct implements Shippable
    {
        private final double weight;

        public ShippableNonExpirableProduct(String name, int price, int quantity, double weight)
        {
            super(name, price, quantity);
            this.weight = weight;
        }

        @Override
        public String getName()
        {
            return super.getName();
        }

        @Override
        public double getWeight()
        {
            return weight;
        }
    }

    class Cart
    {
        private final Map<Product, Integer> products = new LinkedHashMap<>();

        public void addProduct(Product product)
        {
            if (product == null)
            {
                throw new IllegalArgumentException("Product cannot be null");
            }
            if (product.getQuantity() <= 0)
            {
                throw new IllegalArgumentException("Product quantity must be greater than zero");
            }
            products.put(product, product.getQuantity());
        }

        public void addProduct(Product product, int quantity)
        {
            if (product == null)
            {
                throw new IllegalArgumentException("Product cannot be null");
            }
            if (quantity <= 0)
            {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if(quantity > product.getQuantity())
            {
                throw new IllegalArgumentException("Not enough quantity available in the product");
            }
            if (products.containsKey(product))
            {
                product.deductQuantity(quantity);
                products.put(product, products.get(product) + quantity);
            }
            else
            {
                product.deductQuantity(quantity);
                products.put(product, quantity);
            }
        }

        public void removeProduct(Product product)
        {
            products.remove(product);
        }
        public Map<Product, Integer> getProducts()
        {
            return Collections.unmodifiableMap(products);
        }

        public boolean isEmpty()
        {
            return products.isEmpty();
        }
    }
    class Customer
    {
        private final String name;
        private double balance;

        public Customer(String name, double balance)
        {
            this.name = name;
            this.balance = balance;
        }

        public  String getName()
        {
            return name;
        }
        public double getBalance()
        {
            return balance;
        }
        public void addBalance(double amount)
        {
            if (amount < 0)
            {
                throw new IllegalArgumentException("Amount must be non-negative");
            }
            balance += amount;
        }
        public void deductBalance(double amount)
        {
            if (amount < 0)
            {
                throw new IllegalArgumentException("Amount must be non-negative");
            }
            if (amount > balance)
            {
                throw new IllegalArgumentException("Not enough balance available");
            }
            balance -= amount;
        }
    }

    class ShippingService
    {
        public static void ship(List<Shippable> items)
        {
            double totalWeight = 0;
            for (Shippable s : items) {
                System.out.printf("Name: %s Weight: %.0fg%n", s.getName(), s.getWeight() * 1000);
                totalWeight += s.getWeight();
            }
            System.out.printf("Total package weight is: ", totalWeight);
        }
    }

    class EcommercePlatform
    {
        public static void Checkout(Cart cart, Customer customer)
        {
            if (cart.isEmpty())
            {
                throw new IllegalStateException("Cart is empty");
            }
            double subTotal = 0;
            double shippingCost = 0;
            List<Shippable> shippables = new ArrayList<>();
            for(var product : cart.getProducts().entrySet())
            {
                if (product.getValue() <= 0)
                {
                    throw new IllegalArgumentException("Product quantity must be greater than zero");
                }
                Product prd = product.getKey();
                int quantity = product.getValue();
                prd.checkExpiration();

                if(quantity > prd.getQuantity())
                {
                    throw new IllegalArgumentException("Not enough quantity available in the product");
                }
                subTotal += prd.getPrice() * quantity;
                if (prd instanceof Shippable shippable)
                {
                    shippables.add(shippable);
                    // Example shipping cost calculation
                    shippingCost += shippable.getWeight() * 0.1;
                }
            }
            double totalPrice = subTotal + shippingCost;
            if (totalPrice > customer.getBalance())
            {
                throw new IllegalStateException("Not enough balance to complete the purchase");
            }
            // For Deduction of quantity
            for(var product : cart.getProducts().entrySet())
            {
                product.getKey().deductQuantity(product.getValue());
            }
            customer.deductBalance(totalPrice);
            if(!shippables.isEmpty())
            {
                ShippingService.ship(shippables);
            }
            // Printing the checkout summary
            System.out.println("Checkout Reciept:");
            for(var product : cart.getProducts().entrySet())
            {
                System.out.printf("%dx %s %.0f%n", product.getValue(), product.getKey().getName(),
                        product.getKey().getPrice() * product.getValue());
            }
            System.out.println("----------------------");
            System.out.printf("Subtotal         %.0f%n", subTotal);
            System.out.printf("Shipping         %.0f%n", shippingCost);
            System.out.printf("Amount           %.0f%n%n", totalPrice);
            System.out.printf("Customer balance: %.0f%n", customer.getBalance());




//            double totalPrice = cart.calculateTotalPrice();
//            if (totalPrice > customer.getBalance())
//            {
//                throw new IllegalStateException("Not enough balance to complete the purchase");
//            }
//            customer.deductBalance(totalPrice);
//            System.out.println("Checkout successful. Total price: " + totalPrice);
        }

    }

    public static void main(String[] args)
    {

    }
}