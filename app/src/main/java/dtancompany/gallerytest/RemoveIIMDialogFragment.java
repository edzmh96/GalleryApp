package dtancompany.gallerytest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by david on 2015-07-27.
 */
public class RemoveIIMDialogFragment extends DialogFragment {

    public interface NoticeDialogListener  {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);

    }

    NoticeDialogListener mListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {

            mListener = (NoticeDialogListener) activity;

        }   catch (ClassCastException e )   {
            throw new ClassCastException(activity.toString() + "must implement Notice Dialog Listener");

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
        builder.setMessage(R.string.dialog_remove_iim_message)
                .setTitle(R.string.dialog_remove_iim_title)
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(RemoveIIMDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(RemoveIIMDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
