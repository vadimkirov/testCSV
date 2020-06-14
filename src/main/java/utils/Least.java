package utils;

import model.Product;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Least {
    public static <T extends Comparable<T>> List<T> leastN(Collection<T> input, int maxCount) {
        assert maxCount > 0;
        int count= 0;
        PriorityQueue<T> pq = new PriorityQueue<T> (Collections.reverseOrder());
        for (T t : input) {
            if (count < maxCount) {
                pq.add(t);
                ++count;
            } else {
                assert pq.peek() != null;
                if (pq.peek().compareTo(t) > 0) {
                    pq.poll();
                    pq.add(t);
                }
            }
        }
        List<T> list = new ArrayList<> (pq);
        Collections.sort(list);
        return list;
    }

    public static void checkAndChangeBean(Product p, PriorityBlockingQueue<Product> product, int maxRep, AtomicInteger count){
        if(product.size ()<maxRep){
            product.add(p);
            count.addAndGet (1);
        }else {
            assert product.peek() != null;
            if ( product.peek ().compareTo (p) > 0) {
                product.poll ();
                product.add (p);
            }
        }
    }


}
