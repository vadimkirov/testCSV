package model;

public class CurrentProcessingData {
    private int currentLine;
    private boolean endFile;
    private float price;

    public CurrentProcessingData(int numberLineInFile, boolean endFile, float price) {
        this.currentLine = numberLineInFile;
        this.endFile = endFile;
        this.price = price;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public boolean isEndFile() {
        return endFile;
    }

    public float getPrice() {
        return price;
    }

    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
    }

    public void setEndFile(boolean endFile) {
        this.endFile = endFile;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass ( ) != o.getClass ( ) ) return false;

        CurrentProcessingData that = (CurrentProcessingData) o;

        if ( currentLine != that.currentLine ) return false;
        if ( endFile != that.endFile ) return false;
        return Float.compare (that.price, price) == 0;
    }

    @Override
    public int hashCode() {
        int result = currentLine;
        result = 31 * result + (endFile ? 1 : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits (price) : 0);
        return result;
    }
}
