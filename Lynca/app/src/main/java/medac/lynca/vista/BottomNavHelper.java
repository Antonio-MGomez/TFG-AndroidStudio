package medac.lynca.vista;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

import medac.lynca.R;

public class BottomNavHelper {

    public static void setup(Activity activity, String activeTab) {

        LinearLayout navHome     = activity.findViewById(R.id.navHome);
        LinearLayout navSearch   = activity.findViewById(R.id.navSearch);
        LinearLayout navBookings = activity.findViewById(R.id.navBookings);
        LinearLayout navProfile  = activity.findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!activeTab.equals("home")) {
                    activity.startActivity(
                            new Intent(activity, HomeActivity.class));
                    activity.finish();
                }
            });
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                if (!activeTab.equals("search")) {
                    activity.startActivity(
                            new Intent(activity, SearchActivity.class));
                    activity.finish();
                }
            });
        }

        if (navBookings != null) {
            navBookings.setOnClickListener(v -> {
                if (!activeTab.equals("bookings")) {
                    activity.startActivity(
                            new Intent(activity, ProfileActivity.class));
                    activity.finish();
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (!activeTab.equals("profile")) {
                    activity.startActivity(
                            new Intent(activity, PerfilActivity.class));
                    activity.finish();
                }
            });
        }
    }
}