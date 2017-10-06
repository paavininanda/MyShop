package com.nupurbaghel.myshop;


import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import static com.nupurbaghel.myshop.CheckOutActivity.details;
import static com.nupurbaghel.myshop.HomeActivity.map;
import static com.nupurbaghel.myshop.HomeActivity.mycart;
import static com.nupurbaghel.myshop.AllOrdersActivity.ordersDB;

public class GenerateMail {
    float TotalCost=0;
    int i=1;
    public String generate(Context context, String Uid){
        ManageCart manageCart= new ManageCart();
        HashMap<String,String>mycart =new HashMap<>();
        mycart = (HashMap<String, String>) manageCart.LoadCart(context);
        String msg="<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table {\n" +
                "    font-family: arial, sans-serif;\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;\n" +
                "}\n" +
                "\n" +
                "td, th {\n" +
                "    border: 1px solid #dddddd;\n" +
                "    text-align: left;\n" +
                "    padding: 8px;\n" +
                "}\n" +
                "\n" +
                "tr:nth-child(even) {\n" +
                "    background-color: #dddddd;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" ;

        return msg;
    }

    public String generateOrder(Context context, String Uid, String orderId){

        String msg = generate(context , Uid);
         msg = msg + "<p>\n" +" <h2> User Details </h2>"+
                "<b> Name &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: &nbsp;</b> "+details.get("name") +" <br>\n" +
                "<b> Address : </b>&nbsp;&nbsp;&nbsp; "+ details.get("address") +" <br>\n" +
                "<b> Email ID &nbsp;&nbsp;: </b>&nbsp; "+ details.get("email") +" <br>\n" +
                "<b> Contact &nbsp;&nbsp;: </b>&nbsp; "+ details.get("phone") +" <br>\n" +
                "</p>\n" +
                "<h2>Order Details : "+orderId+"</h2><table>\n" +
                "  <tr>\n" +
                "    <th>S.No</th>\n" +
                "    <th>Product Name</th>\n" +
                "    <th>Product Id</th>\n" +
                "    <th>Price </th>\n" +
                "    <th>Discount </th>\n" +
                "    <th>Net Price Per Item</th>\n" +
                "    <th>Quantity </th>\n" +
                "    <th>Total </th>\n" +
                "  </tr>\n" ;

        for(Map.Entry cartItem: mycart.entrySet()) {
            Map<String,String> mymap = map.get("product").get(cartItem.getKey());

            String price=mymap.get("price");
            String discount=mymap.get("discount");
            String quantity=cartItem.getValue().toString();
            String cost= findCost(price,discount,quantity);
            String total= Float.toString(Float.parseFloat(cost)*Float.parseFloat(quantity));
            msg= msg+ "  <tr>\n" +
                    "    <td>"+ Integer.toString(i) +"</td>\n" +
                    "    <td>"+ mymap.get("name") +"</td>\n" +
                    "    <td>"+ cartItem.getKey() +"</td>\n" +
                    "    <td> Rs "+ price +"</td>\n" +
                    "    <td>"+  discount +"%</td>\n" +
                    "    <td> Rs "+ cost +"</td>\n" +
                    "    <td>"+ quantity +" </td>\n" +
                    "    <td> Rs "+ total +" </td>\n" +
                    "  </tr>\n" ;
            i=i+1;
        }

        msg= msg + "<tr><th></th>\n"+
                "    <th></th>\n" +
                "    <th></th>\n" +
                "    <th></th>\n" +
                "    <th></th>\n" +
                "    <th></th>\n" +
                "<th>Grand Total</th><th> Rs "+ Float.toString(TotalCost) +" </th></tr>\n" +
                "</table>\n" + "</body>\n" +
                "</html>\n" ;

        return msg;
    }
    public String findCost(String price,String discount, String quantity){
        float Price = Float.parseFloat(price);
        float Discount = Float.parseFloat(discount);

        float cost = (float) (Price - Discount * 0.01 * Price);
        TotalCost = TotalCost + cost * (float)Integer.parseInt(quantity);
        return Float.toString(cost);
    }

    public String generateOrderCancel(Context context, String Uid,String orderid){
        String msg= generate(context,Uid);
        msg = msg +" <h2> Order Cancelled </h2> "
                +"<b> Order Id : </b> "+ orderid +"<br>"
                +"<b>Name : </b>"+ordersDB.get(orderid).get("name") +"<br>"
                +"<b>Email : </b>"+ordersDB.get(orderid).get("email") +"<br>"
                +"<b>Address : </b>"+ordersDB.get(orderid).get("address") +"<br>"
                +"<b>Date : </b>"+ordersDB.get(orderid).get("dateTime") + "<br>"
                 ;
        msg= msg+ "</body>\n" +
                "</html>\n" ;
        return msg;
    }
}
