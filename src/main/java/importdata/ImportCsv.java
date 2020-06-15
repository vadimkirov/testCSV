package importdata;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import model.Product;

import java.io.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Least.checkAndChangeBean;
import static utils.Least.leastN;

public class ImportCsv {

    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {


        String temp = uiAnswer("Введите символ разделителя данных в файле(ах)(пример разделителя  ;    :");
        char delimiter = temp.length()>0? temp.charAt(0): ',';
        lokForDir(delimiter);
        sc.close();

    }

    private static String uiAnswer(String question){
        String answer="";

        while (answer.equals("")){
            System.out.print(question);
            answer = sc.next();
        }

        return answer;
    }


    private static void lokForDir(Character delimiter){
        /*
        File rootDir = new File(root);
List<String> result = new ArrayList<>();
Queue<File> fileTree = new PriorityQueue<>();

Collections.addAll(fileTree, rootDir.listFiles());

while (!fileTree.isEmpty())
{
    File currentFile = fileTree.remove();
    if(currentFile.isDirectory()){
        Collections.addAll(fileTree, currentFile.listFiles());
    } else {
        result.add(currentFile.getAbsolutePath());
    }
}

формирование списка файлов с обходом в глубину


         */





        // Считываем исходный каталог для поиска файлов.

        String temp   = uiAnswer("Введите исходную директорию для поиска файлов:");
        String directoryPath = temp.length()>0? temp: "";
        File directory = new File(directoryPath);
        // Убедимся, что директория найдена и это реально директория, а не файл.
        if (directory.exists() && directory.isDirectory()) {
            processDirectory(directory,delimiter);
        } else {
            System.out.println("Не удалось найти директорию по указанному пути.");
        }

    }




    private static PriorityBlockingQueue<Product> makerToMap(File file, Character delimiter, int maxSizeMap ) throws FileNotFoundException {

        return  leastN(
                new CsvToBeanBuilder(new FileReader(file)).withSeparator(delimiter)
                        .withType(Product.class).build().parse(),

                maxSizeMap);

    }




    private static void processDirectory(final File directory, final Character delimiter) {
        // Получаем список доступных файлов в указанной директории.
        FilenameFilter filter = (file, name)-> name.endsWith(".csv");

        File[] files = directory.listFiles(filter);

//        добавить обход папок в глубину

        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.length);
        }

        int temp = Integer.parseInt(uiAnswer("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):"));
        int waitTime =   temp>0 ? temp : 1000;


        temp = Integer.parseInt(uiAnswer("Введите максимальное количество выводимых данных(строк,по умолчанию 1 000):"));
        int maxSizeMap = temp > 0 ? temp : 1000;

        temp = Integer.parseInt(uiAnswer("Введите максимальное количество элементов с одинаковым ID(строк,по умолчанию 20):"));
        int maxRep =  temp > 0 ? temp : 20;
        System.out.println("Имя файла результатов: work_result.csv");
        String outDirName = uiAnswer("Введите путь для выгрузки результатов работы утилиты:");

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить




        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<> (maxSizeMap,0.5f,treadCount );
        AtomicInteger countSize = new AtomicInteger ( 0 );
        PriorityBlockingQueue<Float> priceQueue = new PriorityBlockingQueue<>(maxSizeMap,Comparator.reverseOrder());

        ExecutorService service = Executors.newFixedThreadPool(treadCount);

        for (final File f : files) {
            service.execute(() -> {
                try {
                    makerFinalList (f,delimiter, mapResult, maxSizeMap, maxRep, countSize, priceQueue);
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

        PriorityQueue<Product> res = new PriorityQueue<>();
        mapResult.forEach((key, value) -> res.addAll(value));
        List<Product> beans = new ArrayList<>();
        for (int i = 0; i < maxSizeMap; i++) {
            beans.add(res.poll());

        }


        Writer writer = null;
        File outFile = new File(outDirName,"work_result.csv");
        try {
            writer = new FileWriter(outFile);
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(beans);
            writer.close();
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }

        //Переделать потом в выгрузку в файл
//        for (int i = 0; i < maxSizeMap; i++) {
//            System.out.println(res.poll());
//
//        }

    }




    private static void makerFinalList(File f, Character delimiter, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeOutFile, int maxRep, AtomicInteger count1, PriorityBlockingQueue<Float> priceQueue) throws FileNotFoundException {
        PriorityBlockingQueue<Product> inputList = makerToMap (f,delimiter,maxSizeOutFile);
        while (!inputList.isEmpty()){
            Product p = inputList.poll();
            if(priceQueue.size() < maxSizeOutFile){
                priceQueue.offer(p.getPrice());
            }else {
                assert priceQueue.peek() != null;
                if (priceQueue.peek().compareTo(p.getPrice()) > 0) {
                    priceQueue.poll();
                    if (mapResult.containsKey(p.getId())) {
                        checkAndChangeBean(p, mapResult.get(p.getId()), maxRep);
                    } else {
                        PriorityBlockingQueue<Product> queue = new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
                        queue.offer(p);
                        mapResult.put(p.getId(), queue);

                    }
                }


            }

        }

    }

}
