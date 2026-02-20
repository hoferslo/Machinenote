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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
// Animation was here AnimationHelper.bounceClick(view);
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDashboardBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.Narocila;
import com.example.machinenote.models.Role;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends BaseFragment {

    public String TAG = "Glavna stran";
    FragmentDashboardBinding binding;
    Context context;
    private ApiManager apiManager;
    private List<Linija> linijeList;
    private int preventivniPreglediCountPonikva = 0;
    private int preventivniPreglediCountSinjaGorica = 0;
    private int preventivniPreglediCountLogatec = 0;
    private int aktivnaNarocilaCount = 0;
    private final Map<String, MaterialButton> buttonMap = new HashMap<>();

    // Button configuration data
    private static class ButtonConfig {
        String name;
        int iconRes;
        int iconColor;
        RoleChecker roleChecker;

        ButtonConfig(String name, int iconRes, int iconColor, RoleChecker roleChecker) {
            this.name = name;
            this.iconRes = iconRes;
            this.iconColor = iconColor;
            this.roleChecker = roleChecker;
        }
    }

    @FunctionalInterface
    interface RoleChecker {
        boolean check(Role role);
    }

    // Define all buttons in one place
    private final ButtonConfig[] buttonConfigs = {
            new ButtonConfig("Knjiženje", R.drawable.book_icon, android.R.color.holo_blue_dark, Role::isKnjizenje),
            new ButtonConfig("Zastoji", R.drawable.schedule_icon, android.R.color.holo_orange_dark, Role::isZastoji),
            new ButtonConfig("Remonti", R.drawable.handyman, android.R.color.holo_red_dark, Role::isRemonti),
            new ButtonConfig("Imenik", R.drawable.contacts, android.R.color.holo_green_dark, Role::isImenik),
            new ButtonConfig("Naloge", R.drawable.assignment_icon, android.R.color.holo_purple, Role::isNaloge),
            new ButtonConfig("Preventivni pregledi", R.drawable.build_icon, android.R.color.darker_gray, Role::isPreventivniPregledi),
            new ButtonConfig("Orodja", R.drawable.quick_reference, android.R.color.holo_blue_light, Role::isOrodja),
            new ButtonConfig("Rezervni deli", R.drawable.home_repair, android.R.color.holo_green_light, Role::isRezervniDeli),
            new ButtonConfig("Registracija", R.drawable.person_add, android.R.color.holo_orange_light, Role::isRegister),
            new ButtonConfig("Naročila", R.drawable.assignment_icon, android.R.color.holo_orange_light, Role::isNarocila),
            new ButtonConfig("Kemikalije", R.drawable.ic_science, android.R.color.holo_blue_light, Role::isKemikalije),
            new ButtonConfig("Dodaj kemikalijo", R.drawable.ic_science, android.R.color.holo_blue_light, Role::isKemikalije)
    };

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(Context context) {
        DashboardFragment fragment = new DashboardFragment();
        fragment.context = context;
        fragment.apiManager = new ApiManager(context);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(getLayoutInflater());

        // Initialize context if null
        if (context == null) {
            context = requireContext();
        }

        fetchLinije();
        fetchNarocila();
        initButtons();
        return binding.getRoot();
    }

    public void initButtons() {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        Role role = sharedPreferencesHelper.getRole();

        Log.d(TAG, "========== initButtons START ==========");
        Log.d(TAG, "Role: " + role);

        for (ButtonConfig config : buttonConfigs) {
            boolean hasPermission = config.roleChecker.check(role);
            Log.d(TAG, config.name + " - has permission: " + hasPermission);

            if (hasPermission) {
                MaterialButton button = addButtonToLayout(config);
                Log.d(TAG, config.name + " button created and added to layout");

                button.setOnClickListener(view-> {
                 // Animation was here AnimationHelper.bounceClick(view);
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.loadFragment(getFragmentForButton(config.name));
                });
            }
        }

        // Add invisible button if last row has only one button
        if (binding.scrollViewLl.getChildCount() > 0 && getLastLinearLayout().getChildCount() == 1) {
            MaterialButton invisibleButton = createButton("invisibleBtn", 0, 0);
            getLastLinearLayout().addView(invisibleButton);
            invisibleButton.setVisibility(View.INVISIBLE);
            Log.d(TAG, "Invisible button added for layout balance");
        }

        Log.d(TAG, "Total buttons in buttonMap: " + buttonMap.size());
        Log.d(TAG, "ButtonMap keys: " + buttonMap.keySet());
        Log.d(TAG, "========== initButtons END ==========");
    }

    private Fragment getFragmentForButton(String buttonName) {
        switch (buttonName) {
            case "Knjiženje": return KnjizenjeFragment.newInstance(context);
            case "Zastoji": return ZastojiFragment.newInstance(context);
            case "Remonti": return RemontiFragment.newInstance(context);
            case "Imenik": return ImenikFragment.newInstance(context);
            case "Naloge": return NalogeFragment.newInstance(context);
            case "Preventivni pregledi": return PreventivniPreglediFragment.newInstance(context);
            case "Orodja": return OrodjaFragment.newInstance(context);
            case "Rezervni deli": return RezervniDeliFragment.newInstance(context);
            case "Registracija": return RegisterFragment.newInstance(context);
            case "Naročila": return NarocilaFragment.newInstance(context);
            case "Kemikalije": return KemikalijeFragment.newInstance(context);
            case "Dodaj kemikalijo": return KemikalijeAddFragment.newInstance(context);
            default: return null;
        }
    }

    private MaterialButton addButtonToLayout(ButtonConfig config) {
        if (binding.scrollViewLl.getChildCount() == 0 || getLastLinearLayout().getChildCount() == 2) {
            LinearLayout newLinearLayout = createNewLinearLayout();
            binding.scrollViewLl.addView(newLinearLayout);
        }

        MaterialButton button = createButton(config.name, config.iconRes, config.iconColor);
        getLastLinearLayout().addView(button);
        buttonMap.put(config.name, button);

        return button;
    }

    private MaterialButton createButton(String text, int iconRes, int iconColor) {
        if (context == null) {
            context = requireContext();
        }

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.secondaryButton);
        MaterialButton button = new MaterialButton(contextThemeWrapper);

        button.setText(text);

        // Set layout params
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, // width (0 with weight = equal distribution)
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120,
                        context.getResources().getDisplayMetrics()),
                1 // weight
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
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
        button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));

        // Set icon if provided
        if (iconRes != 0) {
            button.setIcon(ContextCompat.getDrawable(context, iconRes));
            int iconSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 40,
                    context.getResources().getDisplayMetrics()
            );
            button.setIconSize(iconSizeInPx);
            button.setIconTint(ContextCompat.getColorStateList(context, iconColor));
        }

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

    private LinearLayout getLastLinearLayout() {
        return (LinearLayout) binding.scrollViewLl.getChildAt(binding.scrollViewLl.getChildCount() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showDrawerIcon(); // Dashboard always shows hamburger
        makeOnline(mainActivity.serverConnection);
    }

    public void makeOnline(boolean online) {
        for (MaterialButton button : buttonMap.values()) {
            if (button != null) {
                makeButtonOnline(online, button);
            }
        }

        // Update for specific buttons
        MaterialButton prevPreglediButton = buttonMap.get("Preventivni pregledi");
        if (prevPreglediButton != null) {
            updatePreventivniPregledi();
        }

        MaterialButton narocilaButton = buttonMap.get("Naročila");
        if (narocilaButton != null) {
            updateNarocila();
        }
    }

    private void updatePreventivniPregledi() {
        MaterialButton button = buttonMap.get("Preventivni pregledi");

        if (button == null) {
            Log.e(TAG, "ERROR: Button is NULL - cannot proceed");
            Log.d(TAG, "ButtonMap contents: " + buttonMap.keySet());
            return;
        }

        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        Role role = sharedPreferencesHelper.getRole();
        String userLocation = sharedPreferencesHelper.getLokacija();
        boolean isAdmin = role.isRegister();

        // Wrap button in FrameLayout if not already wrapped
        ViewGroup parent = (ViewGroup) button.getParent();
        FrameLayout container;

        if (parent instanceof FrameLayout) {
            container = (FrameLayout) parent;

            // Remove existing circles (but keep the button!)
            int childCount = container.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = container.getChildAt(i);

                // Only remove TextViews that are NOT MaterialButtons
                if (child instanceof TextView && !(child instanceof MaterialButton)) {
                    container.removeViewAt(i);
                }
            }

        } else if (parent instanceof LinearLayout) {
            LinearLayout linearParent = (LinearLayout) parent;
            int index = linearParent.indexOfChild(button);
            ViewGroup.LayoutParams buttonParams = button.getLayoutParams();

            // Store ALL original properties
            float weight = 0;
            int originalHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            int[] originalMargins = new int[4];

            if (buttonParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) buttonParams;
                weight = llp.weight;
                originalHeight = llp.height;
                originalMargins[0] = llp.leftMargin;
                originalMargins[1] = llp.topMargin;
                originalMargins[2] = llp.rightMargin;
                originalMargins[3] = llp.bottomMargin;
            }

            // Remove button from LinearLayout
            linearParent.removeView(button);

            // Create FrameLayout container
            container = new FrameLayout(context);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    16, // width with weight
                    originalHeight + 16
            );
            containerParams.weight = weight;
            containerParams.setMargins(originalMargins[0], originalMargins[1],
                    originalMargins[2], originalMargins[3]);
            container.setLayoutParams(containerParams);

            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            frameParams.setMargins(8, 8, 8, 8); // Add inner space
            button.setLayoutParams(frameParams);

            button.setBackgroundDrawable(Objects.requireNonNull(
                    ContextCompat.getDrawable(context, R.drawable.bg_button_secondary)));
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
            button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
            button.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);

            // Add button to container
            container.addView(button);

            // Add container back to LinearLayout
            linearParent.addView(container, index);
        } else {
            Log.e(TAG, "ERROR: Parent is neither FrameLayout nor LinearLayout! Parent type: " + parent.getClass().getSimpleName());
            return;
        }

        // Ensure button is visible
        button.setVisibility(View.VISIBLE);
        button.setElevation(4f);

        int circleSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 34,
                context.getResources().getDisplayMetrics()
        );

        if (isAdmin || "Vse Lokacije".equals(userLocation)) {
            if (preventivniPreglediCountPonikva > 0) {
                addCircleToContainer(container, "Pon\n" + String.valueOf(preventivniPreglediCountPonikva),
                        circleSize, Gravity.START | Gravity.TOP, 8, 8, 0, 0,
                        android.R.color.holo_blue_dark);
            }

            if (preventivniPreglediCountLogatec > 0) {
                addCircleToContainer(container, "Log\n" + String.valueOf(preventivniPreglediCountLogatec),
                        circleSize, Gravity.END | Gravity.TOP, 0, 8, 8, 0,
                        android.R.color.holo_orange_dark);
            }

            if (preventivniPreglediCountSinjaGorica > 0) {
                addCircleToContainer(container, "SG\n" + String.valueOf(preventivniPreglediCountSinjaGorica),
                        circleSize, Gravity.START | Gravity.BOTTOM, 8, 0, 0, 8,
                        android.R.color.holo_green_dark);
            }
        } else {
            if ("Ponikva".equalsIgnoreCase(userLocation)) {
                if (preventivniPreglediCountPonikva > 0) {
                    addCircleToContainer(container, "Pon\n" + String.valueOf(preventivniPreglediCountPonikva),
                            circleSize, Gravity.END | Gravity.TOP, 0, 8, 8, 0,
                            android.R.color.holo_blue_dark);
                }
            } else if ("Logatec".equalsIgnoreCase(userLocation) || "Sinja Gorica".equalsIgnoreCase(userLocation)) {
                addCircleToContainer(container, "Log\n" + String.valueOf(preventivniPreglediCountLogatec),
                        circleSize, Gravity.END | Gravity.TOP, 0, 8, 8, 0,
                        android.R.color.holo_orange_dark);
                addCircleToContainer(container, "SG\n" + String.valueOf(preventivniPreglediCountSinjaGorica),
                        circleSize, Gravity.START | Gravity.BOTTOM, 8, 0, 0, 8,
                        android.R.color.holo_green_dark);
            }
        }
        container.requestLayout();
    }

    private void updateNarocila() {
        MaterialButton button = buttonMap.get("Naročila");

        if (button == null) {
            Log.e(TAG, "ERROR: Naročila button is NULL");
            return;
        }

        // Wrap button in FrameLayout if not already wrapped
        ViewGroup parent = (ViewGroup) button.getParent();
        FrameLayout container;

        if (parent instanceof FrameLayout) {
            container = (FrameLayout) parent;

            // Remove existing circles
            int childCount = container.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = container.getChildAt(i);
                if (child instanceof TextView && !(child instanceof MaterialButton)) {
                    container.removeViewAt(i);
                }
            }

        } else if (parent instanceof LinearLayout) {
            LinearLayout linearParent = (LinearLayout) parent;
            int index = linearParent.indexOfChild(button);
            ViewGroup.LayoutParams buttonParams = button.getLayoutParams();

            float weight = 0;
            int originalHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            int[] originalMargins = new int[4];

            if (buttonParams instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) buttonParams;
                weight = llp.weight;
                originalHeight = llp.height;
                originalMargins[0] = llp.leftMargin;
                originalMargins[1] = llp.topMargin;
                originalMargins[2] = llp.rightMargin;
                originalMargins[3] = llp.bottomMargin;
            }

            linearParent.removeView(button);

            container = new FrameLayout(context);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    16,
                    originalHeight + 16
            );
            containerParams.weight = weight;
            containerParams.setMargins(originalMargins[0], originalMargins[1],
                    originalMargins[2], originalMargins[3]);
            container.setLayoutParams(containerParams);

            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            frameParams.setMargins(8, 8, 8, 8);
            button.setLayoutParams(frameParams);

            button.setBackgroundDrawable(Objects.requireNonNull(
                    ContextCompat.getDrawable(context, R.drawable.bg_button_secondary)));
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_light));
            button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
            button.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);

            container.addView(button);
            linearParent.addView(container, index);
        } else {
            Log.e(TAG, "ERROR: Parent is neither FrameLayout nor LinearLayout!");
            return;
        }

        button.setVisibility(View.VISIBLE);
        button.setElevation(4f);

        // Show count badge only if there are active orders
        if (aktivnaNarocilaCount > 0) {
            int circleSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 34,
                    context.getResources().getDisplayMetrics()
            );

            addCircleToContainer(container, String.valueOf(aktivnaNarocilaCount),
                    circleSize, Gravity.END | Gravity.TOP, 0, 8, 8, 0,
                    android.R.color.holo_red_dark);
        }

        container.requestLayout();
    }

    private void addCircleToContainer(FrameLayout container, String text, int size,
                                      int gravity, int left, int top, int right, int bottom,
                                      int colorRes) {

        TextView circle = new TextView(context);
        circle.setText(text);
        circle.setTextColor(context.getResources().getColor(android.R.color.white));
        circle.setTextSize(12);
        circle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        circle.setGravity(Gravity.CENTER);
        circle.setElevation(8f);
        circle.setClickable(false);
        circle.setFocusable(false);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(ContextCompat.getColor(context, colorRes));
        bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        bg.setStroke(2, context.getResources().getColor(android.R.color.white));
        circle.setBackground(bg);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = gravity;

        int leftPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, left, context.getResources().getDisplayMetrics());
        int topPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, top, context.getResources().getDisplayMetrics());
        int rightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, right, context.getResources().getDisplayMetrics());
        int bottomPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottom, context.getResources().getDisplayMetrics());

        params.setMargins(leftPx, topPx, rightPx, bottomPx);

        container.addView(circle, params);
    }

    private void makeButtonOnline(boolean online, MaterialButton button) {
        button.setClickable(online);

        if (online) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setAlpha(1.0f);
            restoreIconColor(button);
        } else {
            button.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.action_secondary));
            button.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
            button.setAlpha(0.3f);
            restoreIconColor(button);
        }

        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_TOP);
    }

    private void restoreIconColor(MaterialButton button) {
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
            case "Naročila":
                button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_orange_light));
                break;
            case "Kemikalije":
                button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_light));
                break;
            case "Dodaj kemikalijo":
                button.setIconTint(ContextCompat.getColorStateList(context, android.R.color.holo_blue_light));
                break;
            default:
                button.setIconTint(ContextCompat.getColorStateList(context, R.color.action_primary));
                break;
        }
    }

    private void fetchLinije() {
        MainActivity mainActivity = (MainActivity) requireActivity();

        if (mainActivity.serverConnection) {
            apiManager.fetchLinije(new ApiManager.LinijeCallback() {
                @Override
                public void onSuccess(List<Linija> response) {
                    linijeList = response;
                    preventivniPreglediCountPonikva = 0;
                    preventivniPreglediCountSinjaGorica = 0;
                    preventivniPreglediCountLogatec = 0;

                    for (Linija linija : linijeList) {
                        if ("Ponikva".equalsIgnoreCase(linija.getLokacija_naziv())) {
                            preventivniPreglediCountPonikva += linija.getStevilo_sklopov();
                        } else if ("Sinja Gorica".equalsIgnoreCase(linija.getLokacija_naziv())) {
                            preventivniPreglediCountSinjaGorica += linija.getStevilo_sklopov();
                        } else if ("Logatec".equalsIgnoreCase(linija.getLokacija_naziv())) {
                            preventivniPreglediCountLogatec += linija.getStevilo_sklopov();
                        } else if ("Vse Lokacije".equalsIgnoreCase(linija.getLokacija_naziv())) {
                            preventivniPreglediCountSinjaGorica += linija.getStevilo_sklopov();
                            preventivniPreglediCountPonikva += linija.getStevilo_sklopov();
                            preventivniPreglediCountLogatec += linija.getStevilo_sklopov();
                        }
                    }

                    updatePreventivniPregledi();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.d(TAG, "onFailure: " + errorMessage);
                }
            });
        } else {
            Log.d(TAG, "fetchLinije: offline");
        }
    }

    private void fetchNarocila() {
        MainActivity mainActivity = (MainActivity) requireActivity();

        if (mainActivity.serverConnection) {
            apiManager.getNarocila(new ApiManager.NarocilaCallback() {
                @Override
                public void onSuccess(List<Narocila> response) {
                    aktivnaNarocilaCount = 0;

                    // Count active orders (novo, v_obdelavi, naroceno)
                    for (Narocila narocilo : response) {
                        String status = narocilo.getStatus();
                        if (status != null) {
                            String statusLower = status.toLowerCase().trim();
                            if ("novo".equals(statusLower) ||
                                    "v_obdelavi".equals(statusLower) ||
                                    "naroceno".equals(statusLower)) {
                                aktivnaNarocilaCount++;
                            }
                        }
                    }

                    Log.d(TAG, "Aktivna naročila count: " + aktivnaNarocilaCount);
                    updateNarocila();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.d(TAG, "fetchNarocila onFailure: " + errorMessage);
                }
            });
        } else {
            Log.d(TAG, "fetchNarocila: offline");
        }
    }
}