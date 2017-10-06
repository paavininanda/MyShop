package com.nupurbaghel.myshop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by paavini on 21/04/17.
 */

public class LogoutAlert {

    public void lA(final Context context){
        Log.i("Clicked","logout");
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please select some items first!");
        builder1.setCancelable(true);

        builder1.setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Logout l=new Logout();
                        l.logout(context);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

}
