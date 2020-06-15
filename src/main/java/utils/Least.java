package utils;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import model.Product;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
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


    public static PriorityBlockingQueue<Product> makerToMap(Path path, Character delimiter, int maxSizeMap) throws FileNotFoundException {

        return  leastN(
                new CsvToBeanBuilder(new FileReader(path.toFile())).withSeparator(delimiter)
                        .withType(Product.class).build().parse(),

                maxSizeMap);

    }


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


    public static boolean dataOutput(String outDirName, List<Product> beans){
    boolean flag = false;

    File outFile = new File(outDirName,"work_result.csv");
        try {
        Writer writer = new FileWriter(outFile);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(beans);
        writer.close();
        flag = true;
    } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
        e.printStackTrace();
    }
        return flag;
    }

}
