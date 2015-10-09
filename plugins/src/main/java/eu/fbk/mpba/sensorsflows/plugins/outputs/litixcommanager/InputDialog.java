package eu.fbk.mpba.sensorsflows.plugins.outputs.litixcommanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class InputDialog {
    public interface ResultCallback<T> {
        void ok(T result);
        void cancel();
    }

    private AlertDialog.Builder alert;

    InputDialog(Context context, String title, String message) {
        alert = new AlertDialog.Builder(context);

        alert.setTitle(title);
        alert.setMessage(message);
    }

    // TODO: MRU strings
    public static InputDialog makeText(Context context, final ResultCallback<String> result, String title, String message) {
        InputDialog i = new InputDialog(context, title, message);

        final EditText input = new EditText(context);
        i.alert.setView(input);

        i.alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                result.ok(input.getText().toString());
            }
        });

        i.alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                result.cancel();
            }
        });
        return i;
    }

    public static InputDialog makePassword(Context context, final ResultCallback<String> result, String title, String message) {
        InputDialog i = new InputDialog(context, title, message);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        i.alert.setView(input);

        i.alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                result.ok(input.getText().toString());
            }
        });

        i.alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                result.cancel();
            }
        });
        return i;
    }

    public static <T> InputDialog makeChooser(final Context context, final ResultCallback<T> result, String title, String message,
                                          final List<T> choices) {
        InputDialog i = new InputDialog(context, title, message);

        List<String> x = new ArrayList<>(choices.size());
        for (Object j : choices)
            x.add(j.toString());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice, x);

        i.alert.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });

        i.alert.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                        builderInner.setMessage(strName);
                        builderInner.setTitle("Confirm");
                        builderInner.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichOne) {
                                        result.ok(choices.get(which));
                                    }
                                });
                        builderInner.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        builderInner.show();
                    }
                });
        return i;
    }

    public void show() {
        alert.show();
    }
}
