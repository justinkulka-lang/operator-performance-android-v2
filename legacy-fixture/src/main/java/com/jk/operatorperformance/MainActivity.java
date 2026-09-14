package com.jk.operatorperformance;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * CI-only v0.34.0/code43 compatibility fixture.
 *
 * The package/version and persistence namespaces/keys mirror the verified historical app so CI can
 * prove package/signing install-over behavior and historical pointer migration without shipping
 * legacy runtime architecture in the production app.
 */
public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSharedPreferences("operator_performance_state", MODE_PRIVATE)
            .edit()
            .putInt("week_index", 2)
            .putInt("session_index", 4)
            .putString("session_history", "legacy-fixture-row")
            .commit();

        getSharedPreferences("operator_performance_references", MODE_PRIVATE)
            .edit()
            .putString("performance_reference_history_v3", "legacy-reference-row")
            .commit();

        getSharedPreferences("operator_performance_benchmarks", MODE_PRIVATE)
            .edit()
            .putString("legacy_fixture", "legacy-benchmark-row")
            .commit();

        TextView view = new TextView(this);
        view.setText("Operator Performance v0.34 compatibility fixture");
        view.setPadding(32, 32, 32, 32);
        setContentView(view);
    }
}
