package com.drant.FastCartMain;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public interface UpdateUserCallback {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void updateLocalItems(ArrayList<Item> itemList);
    void updateShoppingHist(ArrayList<Item> itemList, String total_price, Date timeOfTransaction);
}
