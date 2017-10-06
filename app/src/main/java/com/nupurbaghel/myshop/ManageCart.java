package com.nupurbaghel.myshop;


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.nupurbaghel.myshop.HomeActivity.mycart;

public class ManageCart {
    String fname = "cart.txt";
    public  Map<String,String> LoadCart(Context context) {
        Map<String,String> mycart= new HashMap();
        File file = new File(context.getDir("data", MODE_PRIVATE), fname);

        try {
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                mycart = (HashMap<String, String>) ois.readObject();
            }
            else {
                updateCart(context);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  mycart;
    }

    public void clearCart(Context context){

        File file = new File(context.getDir("data", MODE_PRIVATE), fname);
        ObjectOutputStream outputStream = null;

        try {
            mycart.clear();
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(mycart);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateCart(Context context){

        File file = new File(context.getDir("data", MODE_PRIVATE), fname);
        ObjectOutputStream outputStream = null;

        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(mycart);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void AddToCart(Context context,String prodId,String quantity){
        int qty =1;
        if(mycart.containsKey(prodId)){
            qty = Integer.parseInt(mycart.get(prodId)) + Integer.parseInt(quantity);
        }
        else {
            qty = Integer.parseInt(quantity);
        }
        mycart.put(prodId,Integer.toString(qty));
        updateCart(context);
    }

    public void ReduceQty(Context context,String prodId,String quantity){
        int qty = Integer.parseInt(quantity);
        qty= qty-1;
        if(qty==0){
            mycart.remove(prodId);
        }
        else {
            mycart.put(prodId, Integer.toString(qty));
        }
        updateCart(context);
    }
}
