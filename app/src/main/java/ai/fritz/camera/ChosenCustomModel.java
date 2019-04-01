package ai.fritz.camera;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChosenCustomModel extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Is this the correct object?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
}



        /*I am going to send stuff to this class when an object is detected,
          this is where voice recognition should come in. Just come up with some methods I can
          use and the logic and I will integrate it. I will sending you an integer which is the
          object that I detected. Here is where the image correspondes to an string

            switch(object){
                            case 0:label.setText("Blender bottle"); break;
                            case 1:label.setText("Glasses");break;
                            case 2:label.setText("Lock");break;
                            case 3:label.setText("Monitor");break;
                            case 4:label.setText("Thermometer");break;
                        }
//         */
//
//
//
//
//    }



