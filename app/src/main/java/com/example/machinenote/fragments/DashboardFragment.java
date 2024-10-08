package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDashboardBinding;
import com.example.machinenote.models.Role;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends BaseFragment {

    public String TAG = "Glavna stran";
    FragmentDashboardBinding binding;
    Context context;
    private final List<MaterialButton> buttonList = new ArrayList<>();

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(Context context) {
        DashboardFragment fragment = new DashboardFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentDashboardBinding.inflate(getLayoutInflater());

        initButtons();

        return binding.getRoot();
    }

    public void initButtons() {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        Role role = sharedPreferencesHelper.getRole();

        {
            if (role.isKnjizenje()) {
                MaterialButton btn = addButtonToLayout("Knjiženje");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(KnjizenjeFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isZastoji()) {
                MaterialButton btn = addButtonToLayout("Zastoji");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(ZastojiFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isRemonti()) {
                MaterialButton btn = addButtonToLayout("Remonti");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(RemontiFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isImenik()) {
                MaterialButton btn = addButtonToLayout("Imenik");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(ImenikFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isNaloge()) {
                MaterialButton btn = addButtonToLayout("Naloge");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(NalogeFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isPreventivniPregledi()) {
                MaterialButton btn = addButtonToLayout("Preventivni pregledi");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    //mainActivity.loadFragment(KnjizenjeFragment.newInstance(context));


                });
            }
        }
        {
            if (role.isOrodja()) {
                MaterialButton btn = addButtonToLayout("Orodja");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(OrodjaFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isRezervniDeli()) {
                MaterialButton btn = addButtonToLayout("Rezervni deli");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(RezervniDeliFragment.newInstance(context));
                });
            }
        }
        {
            if (role.isRegister()) {
                MaterialButton btn = addButtonToLayout("Registracija");
                btn.setOnClickListener(view -> {
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(RegisterFragment.newInstance(context));
                });
            }
        }

        if (getLastLinearLayout().getChildCount() == 1) {
            MaterialButton button = createButton("invisibleBtn");
            getLastLinearLayout().addView(button);
            button.setVisibility(View.INVISIBLE);
        }

    }


    private MaterialButton addButtonToLayout(String name) {
        // Check if the current LinearLayout has exactly two buttons

        if (binding.scrollViewLl.getChildCount() == 0 || getLastLinearLayout().getChildCount() == 2) {
            // Create a new LinearLayout to hold buttons
            LinearLayout newLinearLayout = createNewLinearLayout();

            binding.scrollViewLl.addView(newLinearLayout);
        }

        // Add buttons to the last LinearLayout in scrollViewLl
        MaterialButton button = createButton(name);
        getLastLinearLayout().addView(button);

        return button;
    }

    private LinearLayout createNewLinearLayout() {
        if (context == null) {
            context = requireContext();
        }
        LinearLayout newLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        newLinearLayout.setLayoutParams(layoutParams);
        newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLinearLayout.setGravity(Gravity.CENTER);
        return newLinearLayout;
    }


    private MaterialButton createButton(String text) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.materialButton);
        MaterialButton button = new MaterialButton(contextThemeWrapper);

        // Set text
        button.setText(text);

        // Set margins and width/weight
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, // width (0 means MATCH_PARENT based on weight)
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120,
                        context.getResources().getDisplayMetrics()),
                1 // weight (1 for equal distribution)
        );

        int margins = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,
                context.getResources().getDisplayMetrics()
        );
        layoutParams.setMargins(margins, margins, margins, margins);

        button.setBackgroundDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.background_corners_200)));
        button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primaryColor_200));

        button.setLayoutParams(layoutParams);

        button.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);

        buttonList.add(button);
        //button.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_button));
        return button;
    }


    private LinearLayout getLastLinearLayout() {
        return (LinearLayout) binding.scrollViewLl.getChildAt(binding.scrollViewLl.getChildCount() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        if (fragmentManager.getFragments().size() < 2) {
            mainActivity.showDrawerIcon();
        }
        makeOnline(mainActivity.serverConnection);
    }

    public void makeOnline(boolean online) {
        buttonList.forEach(button -> {
            if (button != null) {
                switch (button.getText().toString()) {
                    case "Zastoji", "Knjiženje", "Remonti", "Preventivni pregledi", "Naloge",
                         "Registracija" -> makeButtonOnline(online, button);
                }
            }
        });

        buttonList.forEach(button -> {
            if (button != null) {
                switch (button.getText().toString()) {
                    case "Naloge" -> getNalogeCount();
                }
            }
        });
    }

    private void getNalogeCount() {
        //todo naredi najprej naloge tab, potem šele to
    }

    private void makeButtonOnline(boolean online, MaterialButton button) {
        button.setClickable(online);
        button.setIcon(online ? null : ContextCompat.getDrawable(context, R.mipmap.signal_wifi_bad));
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
        button.setBackgroundTintList(online ? ContextCompat.getColorStateList(context, R.color.primaryColor_200) : ContextCompat.getColorStateList(context, R.color.primaryColor_100));
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}