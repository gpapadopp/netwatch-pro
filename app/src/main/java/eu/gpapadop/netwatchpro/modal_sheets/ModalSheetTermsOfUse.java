package eu.gpapadop.netwatchpro.modal_sheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import eu.gpapadop.netwatchpro.R;

public class ModalSheetTermsOfUse extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modal_sheet_terms_of_use, container, false);
    }
}
