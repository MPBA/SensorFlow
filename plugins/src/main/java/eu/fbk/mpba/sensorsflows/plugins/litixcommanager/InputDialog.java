package eu.fbk.mpba.sensorsflows.plugins.litixcommanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InputDialog {
    public interface ResultCallback<T> {
        void ok(T result);
        void cancel();
    }

    private AlertDialog.Builder alert;
    private Activity context;

    InputDialog(Activity context, String title) {
        this.context = context;
        alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setCancelable(false);
    }

    // TODO: MRU strings
    public static InputDialog makeText(Activity context, final ResultCallback<String> result, String title, String message) {
        InputDialog i = new InputDialog(context, title);
        i.alert.setMessage(message);

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

    public static InputDialog makePassword(Activity context, final ResultCallback<String> result, String title, String message) {
        InputDialog i = new InputDialog(context, title);
        i.alert.setMessage(message);

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

    public static <T> T getChoose(final Activity context, String message, final List<T> choices) {
        final AtomicBoolean repeat = new AtomicBoolean(true);
        final AtomicReference<Integer> result = new AtomicReference<>(-2);

        while (repeat.get()) {
            final InputDialog i = new InputDialog(context, message);

            List<String> x = new ArrayList<>(choices.size());
            for (Object j : choices)
                x.add(j.toString());

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    android.R.layout.select_dialog_singlechoice, x);

            final Semaphore s = new Semaphore(0);

            i.alert.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.set(-1);
                            s.release();
                        }
                    });

            i.alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.set(which);
                    s.release();
                }
            });

            i.show();

            try {
                s.acquire();
            } catch (InterruptedException ignore) {
            }

            if (result.get() < 0)
                return null;

            if (getBool(context, "Confirm selection", choices.get(result.get()).toString(), "Ok", "No, change"))
                repeat.set(false);
        }
        return choices.get(result.get());
    }

    public static boolean getBool(final Activity context, String title, String message, String trueOne, String falseOne) {
        final InputDialog i = new InputDialog(context, title);
        i.alert.setMessage(message);
        final Semaphore s = new Semaphore(0);
        final AtomicReference<Boolean> r = new AtomicReference<>(false);
        i.alert.setPositiveButton(trueOne, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                r.set(true);
                s.release();
            }
        });

        i.alert.setNegativeButton(falseOne, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                r.set(false);
                s.release();
            }
        });

        i.show();

        try {
            s.acquire();
        } catch (InterruptedException ignore) { }
        return r.get();
    }

    public void show() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    alert.show();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}
