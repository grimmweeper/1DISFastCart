package com.drant.FastCartMain;

import java.util.ArrayList;
import java.util.Date;

public interface ShoppingHistCallback {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void updateShoppingHist(ArrayList<Item> itemList, String total_price, Date timeOfTransaction);
}
