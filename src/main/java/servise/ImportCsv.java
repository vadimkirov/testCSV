package servise;

import model.Product;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static utils.Least.*;
import static utils.UI.*;

public class ImportCsv {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        String temp = uiAnswer("Введите символ разделителя данных в файле(ах)(пример разделителя  ;    :");
        char delimiter = temp.length()>0? temp.charAt(0): ',';
        lokForDir(delimiter);
        sc.close();

    }


    //запрос-ответ в текстовом формате
    public static String uiAnswer(String question){
        String answer="";
        while (answer.equals("")){
            System.out.print(question);
            answer = sc.next();
        }
        return answer;
    }


    //запрос-ответ в числовом формате
    private static int uiNumberAnswer(String question, int defaultValue){
        int temp;
        try{
            temp = Integer.parseInt(uiAnswer(question));
        }catch (NumberFormatException nfe){
            temp = -1;
        }
        return    temp>0 ? temp : defaultValue;
    }


    //создаем фиксированный пул, так как потоки "долгие"
    // помещаем список файлов в пул и обрабатываем
    public static void processDirectory(final File directory, final Character delimiter) {
        // Получаем список доступных файлов в указанной директории.
        List<Path> files = null;
        try {
            files = filesList(".csv",directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.size());
        }
        int waitTime =  uiNumberAnswer("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):",1000);

        int maxSizeMap = uiNumberAnswer("Введите максимальное количество выводимых данных(строк,по умолчанию 1 000):",1000);

        int maxRep = uiNumberAnswer("Введите максимальное количество элементов с одинаковым ID(строк,по умолчанию 20):",20);

        System.out.println("Имя файла результатов: work_result.csv");
        String outDirName = uiAnswer("Введите путь для выгрузки результатов работы утилиты:");

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить

        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<> (maxSizeMap,0.5f,treadCount );

        PriorityBlockingQueue<Float> priceQueue = new PriorityBlockingQueue<>(maxSizeMap,Comparator.reverseOrder());

        ExecutorService service = Executors.newFixedThreadPool(treadCount);

        for (Path f : files) {
            service.execute(() -> {
                try {
                    makerFinalList (f,delimiter, mapResult, maxSizeMap, maxRep, priceQueue);
                } catch (FileNotFoundException e) {
                    e.printStackTrace ( );
                }
            });
        }
        // Новые задачи более не принимаем, выполняем только оставшиеся.
        service.shutdown();
        // Ждем завершения выполнения потоков не более waitTime минут.
        try {
            service.awaitTermination(waitTime, TimeUnit.MINUTES); //Максимальное время обработки
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Product> beans= resultList(mapResult,maxSizeMap);
        String report = dataOutput(outDirName,beans)? "Операция выполнена!":"!!! Операция НЕ выполнена";

        System.out.println(report);
    }


    //заполнение "очереди цен" и
    //согласно её("очереди цен") данным, результирующей map
    private static void makerFinalList(Path f, Character delimiter, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeOutFile, int maxRep, PriorityBlockingQueue<Float> priceQueue) throws FileNotFoundException {
        PriorityBlockingQueue<Product> inputList = makerToMap (f,delimiter,maxSizeOutFile);
        while (!inputList.isEmpty()){
            Product p = inputList.poll();
            if(priceQueue.size() < maxSizeOutFile){
                priceQueue.offer(p.getPrice());
                mapResultFillUp(p,maxRep,mapResult);
            }else {
                assert priceQueue.peek() != null;
                if (priceQueue.peek().compareTo(p.getPrice()) > 0) {
                    priceQueue.poll();
                    priceQueue.offer(p.getPrice());
                    mapResultFillUp(p,maxRep,mapResult);
                }
            }
        }
    }
}
