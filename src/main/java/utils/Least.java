package utils;

import model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Least {

    //отбираем из коллекции( 1 загруженный файл)только те элементы, которые входят в
    //заданное число самых дешевых
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

    //В результирующей map(по ключу ID) проверяем разрешенное число повторов ID,
    //при превышении сравнивыем цену и при необходимости меняем самый дорогой элемент на текущий
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


    //выгрузка данных из результирущей map в list для операции вывода
    public static List<Product> resultList(ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeMap){
        PriorityQueue<Product> res = new PriorityQueue<>();
        mapResult.forEach((key, value) -> res.addAll(value));
        List<Product> beans = new ArrayList<>();
        int sizeOutputData = Math.min(res.size(), maxSizeMap);
        for (int i = 0; i < sizeOutputData; i++) {
            beans.add(res.poll());
        }
        return beans;
    }

    //заполнение результирующей map
    public static void mapResultFillUp(Product p, int maxRep, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult){
        if (mapResult.containsKey(p.getId())) {
            checkAndChangeBean(p, mapResult.get(p.getId()), maxRep);
        } else {
            PriorityBlockingQueue<Product> queue = new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
            queue.offer(p);
            mapResult.put(p.getId(), queue);
        }
    }

}
