package com.nupurbaghel.myshop;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import java.util.*;
import java.util.Collections;

import static com.nupurbaghel.myshop.HomeActivity.map;
import static com.nupurbaghel.myshop.ProductActivity.prodIds;

public class FilterClass {
    public ArrayAdapter<CharSequence> CreateAdapter(Context context, String filter_name){

        ArrayAdapter<CharSequence> adapter;
        if(filter_name.equals("criteria")){
            adapter = ArrayAdapter.createFromResource(context, R.array.criteria, R.layout.spinner_item);
        }
        else if(filter_name.equals("Chose Material")){
            adapter = ArrayAdapter.createFromResource(context, R.array.filter_quality, R.layout.spinner_item);
        }
        else {
            String name;
            if(filter_name.equals("Chose Color"))
                name="color";
            else
                name="company";
            LinkedHashSet<String> values = new LinkedHashSet<String>();
            values.add("None");
            for (String prodId : prodIds) {
                String [] temp =map.get("product").get(prodId).get(name).split(",");
                for(String x:temp) {
                    values.add(x);
                }
            }
            adapter = new ArrayAdapter(context, R.layout.spinner_item, values.toArray(new String[values.size()]));
        }
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }


    public String[] findProdId(String filter_name, String matchvalue){

        Log.i("here",filter_name);
        Log.i("here",matchvalue);
        List<String> newProdIds= new ArrayList<String>();

        if(filter_name.equals("price")||filter_name.equals("discount")){
            newProdIds= sort(filter_name);
            if(matchvalue.equals("Highest Price First") || matchvalue.equals("Highest Discount First")){
                Collections.reverse(newProdIds);
            }
        }
        else{

            for (String prodId : prodIds){
                String value[]= map.get("product").get(prodId).get(filter_name).split(",");
                for(String x:value) {
                    if (x.equals(matchvalue)) {
                        newProdIds.add(prodId);
                        break;
                    }
                }
            }
        }
        return newProdIds.toArray(new String[0]);
    }

    public List<String> sort(String filter_name){

        Map<String, Float> myMap = new LinkedHashMap<String, Float>();
        List<String> prodIdss= new ArrayList<String>();
        for (String prodId : prodIds) {
            myMap.put(prodId,Float.parseFloat(map.get("product").get(prodId).get(filter_name)));
        }
        List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(myMap.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>() {

            @Override
            public int compare(Map.Entry<String, Float> obj1, Map.Entry<String, Float> obj2) {
                return obj1.getValue().compareTo(obj2.getValue());
            }
        });
        for(Map.Entry<String,Float> entry: entryList){
            prodIdss.add(entry.getKey());
        }
        return prodIdss;
    }

}
