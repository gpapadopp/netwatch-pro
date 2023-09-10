package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import eu.gpapadop.netwatchpro.modal_sheets.ModalSheetTermsOfUse;

public class InitialTermsScreen extends AppCompatActivity {
    private boolean canClickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_terms_screen);

        this.canClickButton = false;

        CheckBox termsOfUseCheckBox = (CheckBox) findViewById(R.id.termsOfUseCheckBox);
        CheckBox privacyPolicyCheckBox = (CheckBox) findViewById(R.id.privacyPolicyCheckBox);
        Button nextButton = (Button) findViewById(R.id.initialTermsOfUseNextButton);
        this.handleTermOfUseCheckBoxText(termsOfUseCheckBox);
        this.handlePrivacyPolicyCheckBoxText(privacyPolicyCheckBox);

        termsOfUseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && privacyPolicyCheckBox.isChecked()){
                    ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.main_blue));
                    nextButton.setBackgroundTintList(colorStateList);
                    canClickButton = true;
                } else {
                    ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_gray));
                    nextButton.setBackgroundTintList(colorStateList);
                    canClickButton = false;
                }
            }
        });

        privacyPolicyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && termsOfUseCheckBox.isChecked()){
                    ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.main_blue));
                    nextButton.setBackgroundTintList(colorStateList);
                    canClickButton = true;
                } else {
                    ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_gray));
                    nextButton.setBackgroundTintList(colorStateList);
                    canClickButton = false;
                }
            }
        });
    }

    private void handleTermOfUseCheckBoxText(CheckBox termsOfUse){
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Prevent CheckBox state from being toggled when link is clicked
                widget.cancelPendingInputEvents();
                ModalSheetTermsOfUse bottomSheetFragment = new ModalSheetTermsOfUse();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        SpannableString linkText = new SpannableString(getString(R.string.terms_of_use));
        linkText.setSpan(clickableSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence cs = TextUtils.expandTemplate(getString(R.string.accept_the_terms_of_use) + " ^1 " + getString(R.string.section), linkText);
        termsOfUse.setText(cs);
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void handlePrivacyPolicyCheckBoxText(CheckBox privacyPolicy){
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Prevent CheckBox state from being toggled when link is clicked
                widget.cancelPendingInputEvents();
                // Do action for link text...
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        SpannableString linkText = new SpannableString(getString(R.string.privacy_policy));
        linkText.setSpan(clickableSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence cs = TextUtils.expandTemplate(getString(R.string.accept_the_privacy_policy) + " ^1 " + getString(R.string.section), linkText);
        privacyPolicy.setText(cs);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }
}