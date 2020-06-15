package utils;

import model.Product;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

public class Least {

    public static PriorityBlockingQueue<Product> leastN(Collection<Product> input, int maxCount) {
        assert maxCount > 0;
        int count= 0;
        PriorityBlockingQueue<Product> pq = new PriorityBlockingQueue<>(maxCount, (x,y) -> Float.compare (y.getPrice(),x.getPrice()));
        for (Product t : input) {
            if (count < maxCount) {
                pq.offer(t);
                ++count;
            } else {
                assert pq.peek() != null;
                if (pq.peek().compareTo(t) > 0) {
                    pq.poll();
                    pq.offer(t);
                }
            }
        }
        return pq;
    }

    public static void checkAndChangeBean(Product p, PriorityBlockingQueue<Product> product, int maxRep){
        if(product.size ()<maxRep){
            product.offer(p);

        }else {
            assert product.peek() != null;
            if ( product.peek ().compareTo (p) > 0) {
                product.poll ();
                product.offer (p);
            }
        }
    }

}
