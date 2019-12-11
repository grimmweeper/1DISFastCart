package com.drant.FastCartMain;

import java.util.ArrayList;

public interface FirebaseCallback{
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void displayItemsCallback(ArrayList<Item> items);
    void itemValidationCallback(Boolean validItem);
    void onItemCallback(Item item);
    void checkIllopCallback(Boolean illopStatus);
}
