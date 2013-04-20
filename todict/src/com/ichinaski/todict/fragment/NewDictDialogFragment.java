package com.ichinaski.todict.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ichinaski.todict.R;

public class NewDictDialogFragment extends DialogFragment {
    public static final String TAG = NewDictDialogFragment.class.getSimpleName();
    // Interface to be implemented by the parent Activity
    public interface INewDictionary {
        public void onNewDictionary(String name);
    }

    private INewDictionary mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the activity implements our interface
        try {
            mCallback = (INewDictionary)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement INewDictionary.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_dict_activity, null, false);
        
        final EditText mNameInput = (EditText)view.findViewById(R.id.dict_name);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.new_dict)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String name = mNameInput.getText().toString();
                        if (!TextUtils.isEmpty(name)) {
                            mCallback.onNewDictionary(name);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), R.string.error_empty_fields, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }

}
