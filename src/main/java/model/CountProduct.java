package model;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

public class CountProduct {


    private PriorityBlockingQueue<Product> blockingQueue = new PriorityBlockingQueue<> ((Collection<? extends Product>) Collections.reverseOrder ());

    public CountProduct(PriorityBlockingQueue<Product> blockingQueue) {

        this.blockingQueue = blockingQueue;
    }

    public CountProduct() {
    }



    public PriorityBlockingQueue<Product> getBlockingQueue() {
        return blockingQueue;
    }

    public void setBlockingQueue(PriorityBlockingQueue<Product> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }


}