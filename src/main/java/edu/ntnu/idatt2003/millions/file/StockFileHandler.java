package edu.ntnu.idatt2003.millions.file;

import edu.ntnu.idatt2003.millions.model.Stock;

import java.nio.file.Path;
import java.util.List;

public interface StockFileHandler {

    List<Stock> readStocks(Path path);

    void writeStocks(List<Stock> stocks, Path path);
}
