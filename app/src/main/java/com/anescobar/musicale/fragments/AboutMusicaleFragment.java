package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.musicale.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AboutMusicaleFragment.OnAboutMusicaleFragmentInteractionListener} interface
 * to handle interaction home.
 * Use the {@link AboutMusicaleFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AboutMusicaleFragment extends Fragment {
    private static final int SECTION_INDEX = 2; //index that identifies fragment for activity to display correct title
    private OnAboutMusicaleFragmentInteractionListener mListener;

    public AboutMusicaleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param example_arg EXAMPLEEEEE
     * @return A new instance of fragment EventsMapViewFragment.
     */
    public static AboutMusicaleFragment newInstance(String example_arg) {
        AboutMusicaleFragment fragment = new AboutMusicaleFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_musicale, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            setHasOptionsMenu(true); //sets actionbar to display this fragment's specific actionbar
            mListener = (OnAboutMusicaleFragmentInteractionListener) activity;
            mListener.onAttachDisplayTitle(SECTION_INDEX); //tells activity to display correct title
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAboutMusicaleViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnAboutMusicaleFragmentInteractionListener {
        public void onAttachDisplayTitle(int sectionIndex);
    }

}