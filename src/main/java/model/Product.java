package model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Product implements Comparable<Product> {
    @CsvBindByPosition(position = 0)  //@CsvBindByName или  @CsvBindByName(column = "ID", required = true)
            int id;

    @CsvBindByPosition(position = 1)
    String name;

    @CsvBindByPosition(position = 2)
    String condition;

    @CsvBindByPosition(position = 3)
    String state;

    @CsvBindByPosition(position = 4)
    float price;

    public Product(int id, String name, String condition, String state, float price) {
        this.id = id;
        this.name = name;
        this.condition = condition;
        this.state = state;
        this.price = price;
    }

    public Product() {
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public String getState() {
        return state;
    }

    public int getId() {
        return id;
    }

    public float getPrice() {
        return price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass ( ) != o.getClass ( ) ) return false;

        Product product = (Product) o;

        if ( id != product.id ) return false;
        if ( Float.compare (product.price, price) != 0 ) return false;
        if (!Objects.equals(name, product.name)) return false;
        if (!Objects.equals(condition, product.condition)) return false;
        return Objects.equals(state, product.state);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode ( ) : 0);
        result = 31 * result + (condition != null ? condition.hashCode ( ) : 0);
        result = 31 * result + (state != null ? state.hashCode ( ) : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits (price) : 0);
        return result;
    }



    @Override
    public int compareTo(Product o) {
        return Float.compare (this.price,o.price);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", condition='" + condition + '\'' +
                ", state='" + state + '\'' +
                ", price=" + price +
                '}';
    }
}
