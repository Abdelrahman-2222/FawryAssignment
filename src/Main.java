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
        private final List<Product> products = new ArrayList<>();

        public void addProduct(Product product)
        {
            products.add(product);
        }

        public void addProduct(Product product, int quantity)
        {
            if (quantity <= 0)
            {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if(quantity > product.getQuantity())
            {
                throw new IllegalArgumentException("Not enough quantity available");
            }
            product.deductQuantity(quantity);
            products.add(new Product(product.getName(), product.getPrice(), quantity) {});
        }

        public void removeProduct(Product product)
        {
            products.remove(product);
        }

        public List<Product> getProducts()
        {
            return Collections.unmodifiableList(products);
        }

        public double calculateTotalPrice()
        {
            return products.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        }

        public boolean isEmpty()
        {
            return products.isEmpty();
        }
    }

    public static void main(String[] args)
    {

    }
}