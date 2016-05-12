package com.platepicks.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.platepicks.R;

/**
 * A fragment that shows a brief instruction of demos.
 */
public class DemoInstructionFragment extends DemoFragmentBase {

    private static final String ARGUMENT_DEMO_FEATURE_NAME = "demo_feature_name";

    public static DemoInstructionFragment newInstance(final String demoFeatureName) {
        DemoInstructionFragment fragment = new DemoInstructionFragment();
        Bundle args = new Bundle();
        args.putString(DemoInstructionFragment.ARGUMENT_DEMO_FEATURE_NAME, demoFeatureName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo_instruction, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle args = getArguments();
        final String demoFeatureName = args.getString(ARGUMENT_DEMO_FEATURE_NAME);
        final DemoConfiguration.DemoFeature demoFeature = DemoConfiguration.getDemoFeatureByName(
                demoFeatureName);

        final TextView tvOverview = (TextView) view.findViewById(R.id.text_demo_feature_overview);
        tvOverview.setText(demoFeature.overviewResId);
        final TextView tvDescription = (TextView) view.findViewById(
                R.id.text_demo_feature_description);
        tvDescription.setText(demoFeature.descriptionResId);
        final TextView tvPoweredBy = (TextView) view.findViewById(
                R.id.text_demo_feature_powered_by);
        tvPoweredBy.setText(demoFeature.poweredByResId);

        final ArrayAdapter<DemoConfiguration.DemoItem> adapter = new ArrayAdapter<DemoConfiguration.DemoItem>(
                getActivity(), R.layout.list_item_icon_text_with_subtitle) {
            @Override
            public View getView(final int position, final View convertView,
                                final ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getActivity().getLayoutInflater()
                            .inflate(R.layout.list_item_demo_button_icon_text, parent, false);
                }
                final DemoConfiguration.DemoItem item = getItem(position);
                final ImageView imageView = (ImageView) view.findViewById(R.id.list_item_icon);
                imageView.setImageResource(item.iconResId);
                final TextView title = (TextView) view.findViewById(R.id.list_item_title);
                title.setText(item.buttonTextResId);
                return view;
            }
        };
        adapter.addAll(demoFeature.demos);
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                final DemoConfiguration.DemoItem item = adapter.getItem(position);
                final AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    final Fragment fragment = Fragment.instantiate(getActivity(), item.fragmentClassName);
                    activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container, fragment, item.fragmentClassName)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                    activity.getSupportActionBar().setTitle(item.titleResId);
                }
            }
        });

        listView.setBackgroundColor(Color.WHITE);
    }
}
