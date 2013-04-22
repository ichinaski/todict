package com.ichinaski.todict.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ichinaski.todict.R;
import com.ichinaski.todict.util.Prefs;

public class EditDictDialogFragment extends DialogFragment implements OnClickListener {
    public static final String TAG = EditDictDialogFragment.class.getSimpleName();
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_NAME = "name";
    
    private EditText mNameInput;
    
    public static EditDictDialogFragment instantiate(long dictID, String name) {
        EditDictDialogFragment fragment = new EditDictDialogFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_ID, dictID);
        args.putString(EXTRA_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }
    
    // Interface to be implemented by the parent Activity
    public interface IDictionaryHandler {
        public void newDictionary(String name);
        public void editDictionary(String name);
        public void deleteDictRequest();
    }

    private IDictionaryHandler mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure the activity implements our interface
        try {
            mCallback = (IDictionaryHandler)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IDictionaryHandler.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_dict_activity, null, false);
        mNameInput = (EditText)view.findViewById(R.id.dict_name);
        mNameInput.setText(getArguments().getString(EXTRA_NAME));
        
        if (getArguments().getLong(EXTRA_ID) == Prefs.DICT_NONE) {
	        return new AlertDialog.Builder(getActivity())
	                .setView(view)
	                .setTitle(R.string.new_dict)
	                .setPositiveButton(R.string.save, this)
	                .setNegativeButton(android.R.string.cancel, this)
	                .create();
        } else {
	        return new AlertDialog.Builder(getActivity())
	                .setView(view)
	                .setTitle(R.string.edit_dict)
	                .setPositiveButton(R.string.save, this)
	                .setNegativeButton(R.string.delete, this)
	                .create();
        }
    }
    
    private void onDeleteDictionary() {
        // If the dictID is valid, delete it
        if (getArguments().getLong(EXTRA_ID) != Prefs.DICT_NONE) {
            mCallback.deleteDictRequest();
        }
    }
    
    private void onSaveDictionary(String name) {
        if (getArguments().getLong(EXTRA_ID) == Prefs.DICT_NONE) {
            mCallback.newDictionary(name);
        } else {
            mCallback.editDictionary(name);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                final String name = mNameInput.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    onSaveDictionary(name);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), R.string.error_empty_fields, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                onDeleteDictionary();
                dialog.dismiss();
                break;
        }
    }

}
