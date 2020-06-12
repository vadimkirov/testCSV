package importdata;

public class CurrentProcessingData {
    private int currentLine;
    private boolean endFile;
    private float price;

    public CurrentProcessingData(int numberLineInFile, boolean endFile, float price) {
        this.currentLine = numberLineInFile;
        this.endFile = endFile;
        this.price = price;
    }

}
