package com.example.machinenote.customFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.R;
import com.example.machinenote.Utility.MailHelper;
import com.example.machinenote.Utility.PhoneCallHelper;
import com.example.machinenote.databinding.FragmentImenikBottomSheetBinding;
import com.example.machinenote.models.Imenik;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ImenikBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentImenikBottomSheetBinding binding;
    private Context context;
    private Imenik imenik;

    public static ImenikBottomSheetFragment newInstance(Context context, Imenik imenik) {
        ImenikBottomSheetFragment fragment = new ImenikBottomSheetFragment();
        fragment.imenik = imenik;
        fragment.context = context;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentImenikBottomSheetBinding.inflate(inflater, container, false);

        if (!imenik.getNazivPodjetja().isEmpty()) {
            binding.companyName.setText(imenik.getNazivPodjetja());
        }
        if (!imenik.getKontaktnaOseba().isEmpty()) {
            binding.contactPerson.setText(imenik.getKontaktnaOseba());
        }
        if (!imenik.getMail().isEmpty()) {
            binding.email.setText(imenik.getMail());
        }
        if (!imenik.getTelefon().isEmpty()) {
            binding.phoneNumber.setText(imenik.getTelefon());
        } else {
            binding.phoneNumberLl.setVisibility(View.GONE);
        }
        if (!imenik.getGsm().isEmpty()) {
            binding.GSM.setText(imenik.getGsm());
        } else {
            binding.gsmLl.setVisibility(View.GONE);
        }

        // Setup view elements and listeners using binding
        binding.buttonEmail.setOnClickListener(v -> {
            if (imenik.getMail().isEmpty()) {
                Toast.makeText(context, getString(R.string.no_email_address), Toast.LENGTH_SHORT).show();
            } else {
                MailHelper.openEmailClient(
                        context,                              // context
                        imenik.getMail(),             // recipient
                        "",            // subject
                        ""                // body
                );
            }
        });

        binding.buttonPhoneNumber.setOnClickListener(v -> {
            if (imenik.getTelefon().isEmpty()) {
                Toast.makeText(context, getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            } else {
                Intent dialIntent = new PhoneCallHelper(context).prepareDialer(imenik.getTelefon());
                if (dialIntent != null) {
                    startActivity(dialIntent);
                }
            }
        });

        binding.buttonGSM.setOnClickListener(v -> {
            if (imenik.getGsm().isEmpty()) {
                Toast.makeText(context, getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            } else {
                Intent dialIntent = new PhoneCallHelper(context).prepareDialer(imenik.getGsm());
                if (dialIntent != null) {
                    startActivity(dialIntent);
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up binding when the view is destroyed to prevent memory leaks
        binding = null;
    }

}
