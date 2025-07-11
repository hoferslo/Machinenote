package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
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
    public int iconResourceId = 0;;

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
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.secondaryButton);
        MaterialButton button = new MaterialButton(contextThemeWrapper);

        // Set text
        button.setText(text);

        // Set icon based on button text
        setButtonIcon(button, text);

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

        button.setBackgroundDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.bg_button_secondary)));
        button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));

        button.setLayoutParams(layoutParams);

        button.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);

        // Set icon gravity to show icon above text
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);

        button.setIconTint(ContextCompat.getColorStateList(context, R.color.action_primary));
        button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));

        buttonList.add(button);
        return button;
    }

    private void setButtonIcon(MaterialButton button, String text) {
        int iconResourceId = 0;
        Log.d("TAG", "setButtonIcon: " + text);
        switch (text) {
            case "Knjiženje":
                iconResourceId = R.drawable.book_icon;
                break;
            case "Zastoji":
                iconResourceId = R.drawable.schedule_icon;
                break;
            case "Remonti":
                iconResourceId = R.drawable.handyman;
                break;
            case "Imenik":
                iconResourceId = R.drawable.contacts;
                break;
            case "Naloge":
                iconResourceId = R.drawable.assignment_icon;
                break;
            case "Preventivni pregledi":
                iconResourceId = R.drawable.build_icon;
                break;
            case "Orodja":
                iconResourceId = R.drawable.quick_reference;
                break;
            case "Rezervni deli":
                iconResourceId = R.drawable.home_repair;
                break;
            case "Registracija":
                iconResourceId = R.drawable.person_add;
                break;
            default:
                // Ni ikone za neznane gumbove
                break;
        }

        if (iconResourceId != 0) {
            button.setIcon(ContextCompat.getDrawable(context, iconResourceId));
            int iconSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 40,
                    context.getResources().getDisplayMetrics()
            );
            button.setIconSize(iconSizeInPx);

            // Set unique color for each icon
            switch (text) {
                case "Knjiženje":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark));
                    break;
                case "Zastoji":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_dark));
                    break;
                case "Remonti":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
                    break;
                case "Imenik":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
                    break;
                case "Naloge":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_purple));
                    break;
                case "Preventivni pregledi":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
                    break;
                case "Orodja":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_light));
                    break;
                case "Rezervni deli":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_light));
                    break;
                case "Registracija":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_light));
                    break;
                default:
                    button.setIconTint(ContextCompat.getColorStateList(context, R.color.action_primary));
                    break;
            }
        }
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
                         "Registracija", "Rezervni deli", "Imenik", "Orodja" -> makeButtonOnline(online, button);
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

        if (online) {
            // Remove gray overlay by restoring original colors and full opacity
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setAlpha(1.0f); // Full opacity

            // Restore original icon color
            String text = button.getText().toString();
            switch (text) {
                case "Knjiženje":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark));
                    break;
                case "Zastoji":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_dark));
                    break;
                case "Remonti":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
                    break;
                case "Imenik":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
                    break;
                case "Naloge":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_purple));
                    break;
                case "Preventivni pregledi":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
                    break;
                case "Orodja":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_light));
                    break;
                case "Rezervni deli":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_light));
                    break;
                case "Registracija":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_light));
                    break;
                default:
                    button.setIconTint(ContextCompat.getColorStateList(context, R.color.action_primary));
                    break;
            }
        } else {
            // Apply subtle gray overlay effect - keep original colors but reduce opacity
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setAlpha(0.3f); // Reduced opacity creates gray overlay effect

            // Keep the original icon color when offline too
            String text = button.getText().toString();
            switch (text) {
                case "Knjiženje":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark));
                    break;
                case "Zastoji":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_dark));
                    break;
                case "Remonti":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
                    break;
                case "Imenik":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
                    break;
                case "Naloge":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_purple));
                    break;
                case "Preventivni pregledi":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
                    break;
                case "Orodja":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_light));
                    break;
                case "Rezervni deli":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_green_light));
                    break;
                case "Registracija":
                    button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_light));
                    break;
                default:
                    button.setIconTint(ContextCompat.getColorStateList(context, R.color.action_primary));
                    break;
            }
        }

        // Keep the original icon (no need to change it)
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
    }

    private void restoreOriginalIconColor(MaterialButton button) {
        String text = button.getText().toString();
        int iconColorResourceId = 0;

        switch (text) {
            case "Knjiženje":
                iconColorResourceId = android.R.color.holo_blue_dark;
                break;
            case "Zastoji":
                iconColorResourceId = android.R.color.holo_orange_dark;
                break;
            case "Remonti":
                iconColorResourceId = android.R.color.holo_red_dark;
                break;
            case "Imenik":
                iconColorResourceId = android.R.color.holo_green_dark;
                break;
            case "Naloge":
                iconColorResourceId = android.R.color.holo_purple;
                break;
            case "Preventivni pregledi":
                iconColorResourceId = android.R.color.darker_gray;
                break;
            case "Orodja":
                iconColorResourceId = android.R.color.holo_blue_light;
                break;
            case "Rezervni deli":
                iconColorResourceId = android.R.color.holo_green_light;
                break;
            case "Registracija":
                iconColorResourceId = android.R.color.holo_orange_light;
                break;
            default:
                iconColorResourceId = R.color.action_primary; // Fallback to original color
                break;
        }

        if (iconColorResourceId != 0) {
            button.setIconTint(ContextCompat.getColorStateList(context, iconColorResourceId));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}