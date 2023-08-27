/*
 * Copyright (C) 2020 Team Gateship-One
 * (Hendrik Borghorst & Frederik Luetkes)
 *
 * The AUTHORS.md file contains a detailed contributors list:
 * <https://github.com/gateship-one/odyssey/blob/master/AUTHORS.md>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gateshipone.odyssey.fragments;


import static java.lang.Thread.sleep;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.gateshipone.odyssey.R;
import org.gateshipone.odyssey.activities.GenericActivity;
import org.gateshipone.odyssey.adapter.TracksAdapter;
import org.gateshipone.odyssey.models.PlaylistModel;
import org.gateshipone.odyssey.models.TrackModel;
import org.gateshipone.odyssey.models.TrackRandomGenerator;
import org.gateshipone.odyssey.playbackservice.IOdysseyPlaybackService;
import org.gateshipone.odyssey.playbackservice.NowPlayingInformation;
import org.gateshipone.odyssey.playbackservice.PlaybackService;

import org.gateshipone.odyssey.playbackservice.storage.OdysseyDatabaseManager;
import org.gateshipone.odyssey.utils.MusicLibraryHelper;
import org.gateshipone.odyssey.utils.PreferenceHelper;
import org.gateshipone.odyssey.utils.ThemeUtils;
import org.gateshipone.odyssey.viewmodels.GenericViewModel;
import org.gateshipone.odyssey.viewmodels.PlaylistTrackViewModel;
import org.gateshipone.odyssey.views.NowPlayingView;


import java.util.List;

public class PlaylistTracksFragment extends OdysseyFragment<TrackModel> implements AdapterView.OnItemClickListener {

    public static final String TRACK_REMOVED_KEY = PlaylistTracksFragment.class.getSimpleName() + "::" + "trackRemovedKey";
    public static final String TRACK_REMOVED_PLAYLIST_ID = PlaylistTracksFragment.class.getSimpleName() + "::" + "trackRemovedPlaylistId";
    public static final String TRACK_REMOVED_TRACK_POSITION = PlaylistTracksFragment.class.getSimpleName() + "::" + "trackRemovedTrackPosition";

    /**
     * Key values for arguments of the fragment
     */
    private static final String ARG_PLAYLISTMODEL = "playlistmodel";

    /**
     * The information of the displayed playlist
     */
    private PlaylistModel mPlaylistModel;

    /**
     * Action to execute when the user selects an item in the list repeat
     */
    private PreferenceHelper.LIBRARY_TRACK_CLICK_ACTION mClickAction;
    private TrackRandomGenerator mTrackRandomGenerator;


    public static PlaylistTracksFragment newInstance(@NonNull final PlaylistModel playlistModel) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_PLAYLISTMODEL, playlistModel);

        final PlaylistTracksFragment fragment = new PlaylistTracksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.list_refresh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get listview
        mListView = view.findViewById(R.id.list_refresh_listview);

        // get swipe layout
        mSwipeRefreshLayout = view.findViewById(R.id.refresh_layout);
        // set swipe colors
        mSwipeRefreshLayout.setColorSchemeColors(ThemeUtils.getThemeColor(requireContext(), R.attr.colorAccent),
                ThemeUtils.getThemeColor(requireContext(), R.attr.colorPrimary));
        // set swipe refresh listener
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshContent);

        mAdapter = new TracksAdapter(getActivity());

        // Disable sections
        mAdapter.enableSections(false);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);

        // get empty view
        mEmptyView = view.findViewById(R.id.empty_view);

        // set empty view message
        ((TextView) view.findViewById(R.id.empty_view_message)).setText(R.string.empty_tracks_message);

        registerForContextMenu(mListView);

        // activate options menu in toolbar
        setHasOptionsMenu(true);

        Bundle args = requireArguments();

        mPlaylistModel = args.getParcelable(ARG_PLAYLISTMODEL);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mClickAction = PreferenceHelper.getClickAction(sharedPreferences, requireContext());

        // setup observer for the live data
        getViewModel().getData().observe(getViewLifecycleOwner(), this::onDataReady);
        mTrackRandomGenerator = new TrackRandomGenerator();

    }

    @Override
    GenericViewModel<TrackModel> getViewModel() {
        return new ViewModelProvider(this, new PlaylistTrackViewModel.PlaylistTrackViewModelFactory(requireActivity().getApplication(), mPlaylistModel)).get(PlaylistTrackViewModel.class);
    }

    /**
     * Called when the fragment resumes.
     * Reload the data, setup the toolbar and create the PBS connection.
     */
    @Override
    public void onResume() {
        if (mToolbarAndFABCallback != null) {
            // set toolbar behaviour and title
            mToolbarAndFABCallback.setupToolbar(mPlaylistModel.getPlaylistName(), false, false, false);
            // Enable FAB correctly for now, can be disabled later
            mToolbarAndFABCallback.setupFAB(v -> playPlaylist(0));
        }
        super.onResume();
    }

    @Override
    protected void onDataReady(List<TrackModel> model) {
        super.onDataReady(model);

        if (mToolbarAndFABCallback != null) {
            // set up play button
            if (mAdapter.isEmpty()) {
                mToolbarAndFABCallback.setupFAB(null);
            } else {
                mToolbarAndFABCallback.setupFAB(v -> playPlaylist(0));
            }
        }
    }

    /**
     * Play the playlist from the current position. Playlists
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isParty = mPlaylistModel.getPlaylistName().equals("Party Mode");
        if(isParty) {
            try {
                //We stop it to ensure proper state
                ((GenericActivity) requireActivity()).getPlaybackService().togglePause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            int num = 0;
            List<TrackModel> allDifferentTracks = MusicLibraryHelper.getAllTracks("",getContext());
            if(!allDifferentTracks.isEmpty()){
                List<TrackModel> currentTracks = OdysseyDatabaseManager.getInstance(getContext()).getTracksForPlaylist(mPlaylistModel.getPlaylistId());
                Log.d("iden","ss" + Long.toString(mPlaylistModel.getPlaylistId()));
                //Equals is already done, to avoid repeating we remove the tracks that are already in
                if(currentTracks.size() >=40){
                    //if we have less than 40 tracks is better to take all into account or else we might found
                    // that we have 22 and the next refill will be only 2 songs repeated 10 times each
                    allDifferentTracks.removeAll(currentTracks);
                }

                mTrackRandomGenerator.setEnabled(50);
                mTrackRandomGenerator.fillFromList(allDifferentTracks);

                while(num <position) {

                    //When we delete the track number updates itself
                    removeTrackFromPlaylist(0);
                    currentTracks.remove(0);
                    num++;
                    //We randomize for artist and album
                    currentTracks.add(allDifferentTracks.get(mTrackRandomGenerator.getRandomTrackNumber()));
                }
                addTracksToPlaylist(currentTracks);
            }

            position= 0;

        }
        switch (mClickAction) {
            case ACTION_ADD_SONG:
                enqueueTrack(position, false);
                break;
            case ACTION_PLAY_SONG:
                playTrack(position);
                break;
            case ACTION_PLAY_SONG_NEXT:
                enqueueTrack(position, true);
                break;
            case ACTION_CLEAR_AND_PLAY:
                playPlaylist(position);
                break;
        }
        activateRepeat(isParty);
    }

    private void activateRepeat(boolean isParty) {
        if(isParty) {
            try {
                sleep(150);
            } catch (InterruptedException e) {
                Log.e("Sleep","Repeat sleep faield");
                Thread.currentThread().interrupt();
            }
            IOdysseyPlaybackService pbs = null;

            try {

                pbs = ((GenericActivity) requireActivity()).getPlaybackService();
                if (pbs != null) {
                    NowPlayingInformation np;
                    np = pbs.getNowPlayingInformation();
                    if (np != null) {
                        while (!np.getRepeat().equals(PlaybackService.REPEATSTATE.REPEAT_ALL)) {
                            pbs.toggleRepeat();
                            np = pbs.getNowPlayingInformation();
                        }
                        NowPlayingView nowPlayingView = requireActivity().findViewById(R.id.now_playing_layout);
                        nowPlayingView.updateStatus(pbs.getNowPlayingInformation());
                        refreshContent();
                    }
                }
            } catch (RemoteException e) {

                e.printStackTrace();
            }
        }
    }

    /** to do personal
     * add the selected track from the playlist in the mediastore.
     */
    private void addTracksToPlaylist(List<TrackModel> tracks) {

        if (mPlaylistModel.getPlaylistType() == PlaylistModel.PLAYLIST_TYPES.ODYSSEY_LOCAL) {
            OdysseyDatabaseManager.getInstance(getContext()).savePlaylist(mPlaylistModel.getPlaylistName(),tracks);
            // reload data
            refreshContent();
        }
    }

    /**
     * Create the context menu.
     */
    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_playlist_tracks_fragment, menu);

        if (mPlaylistModel.getPlaylistType() != PlaylistModel.PLAYLIST_TYPES.ODYSSEY_LOCAL) {
            // Hide remove track for all non odyssey playlists as it is unsupported
            menu.findItem(R.id.fragment_playlist_tracks_action_remove).setVisible(false);
        }
    }


    /**
     * Hook called when an menu item in the context menu is selected.
     *
     * @param item The menu item that was selected.
     * @return True if the hook was consumed here.
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (info == null) {
            return super.onContextItemSelected(item);
        }

        final int itemId = item.getItemId();

        if (itemId == R.id.fragment_album_tracks_action_play) {
            playTrack(info.position);
            return true;
        } else if (itemId == R.id.fragment_playlist_tracks_action_enqueue) {
            enqueueTrack(info.position, false);
            return true;
        } else if (itemId == R.id.fragment_playlist_tracks_action_enqueueasnext) {
            enqueueTrack(info.position, true);
            return true;
        } else if (itemId == R.id.fragment_playlist_tracks_action_remove) {
            removeTrackFromPlaylist(info.position);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Initialize the options menu.
     * Be sure to call {@link #setHasOptionsMenu} before.
     *
     * @param menu         The container for the custom options menu.
     * @param menuInflater The inflater to instantiate the layout.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.options_menu_playlist_tracks_fragment, menu);

        // get tint color
        int tintColor = ThemeUtils.getThemeColor(requireContext(), R.attr.odyssey_color_text_accent);

        Drawable drawable = menu.findItem(R.id.action_add_playlist_tracks).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, tintColor);
        menu.findItem(R.id.action_add_playlist_tracks).setIcon(drawable);

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    /**
     * Hook called when an menu item in the options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return True if the hook was consumed here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_playlist_tracks) {
            enqueuePlaylist();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Call the PBS to enqueue the entire playlist.
     */
    private void enqueuePlaylist() {
        try {
            // add the playlist
            ((GenericActivity) requireActivity()).getPlaybackService().enqueuePlaylist(mPlaylistModel);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Call the PBS to play the entire playlist and start with the selected track.
     * A previous playlist will be cleared.
     *
     * @param position the position of the selected track in the adapter
     */
    private void playPlaylist(int position) {

        try {
            ((GenericActivity) requireActivity()).getPlaybackService().playPlaylist(mPlaylistModel, position);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Call the PBS to enqueue the selected track and then play it.
     *
     * @param position the position of the selected track in the adapter
     */
    private void playTrack(int position) {
        TrackModel track = mAdapter.getItem(position);

        try {
            ((GenericActivity) requireActivity()).getPlaybackService().playTrack(track, false);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Call the PBS to enqueue the selected track.
     *
     * @param position the position of the selected track in the adapter
     */
    private void enqueueTrack(int position, boolean asNext) {

        TrackModel track = mAdapter.getItem(position);

        try {
            ((GenericActivity) requireActivity()).getPlaybackService().enqueueTrack(track, asNext);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Remove the selected track from the playlist in the mediastore.
     *
     * @param position the position of the selected track in the adapter
     */
    private void removeTrackFromPlaylist(int position) {
        boolean reloadData = false;

        if (mPlaylistModel.getPlaylistType() == PlaylistModel.PLAYLIST_TYPES.ODYSSEY_LOCAL) {
            reloadData = OdysseyDatabaseManager.getInstance(getContext()).removeTrackFromPlaylist(mPlaylistModel.getPlaylistId(), position);
        }

        if (reloadData) {
            // reload data
            refreshContent();

            Bundle result = new Bundle();
            result.putLong(TRACK_REMOVED_PLAYLIST_ID, mPlaylistModel.getPlaylistId());
            result.putInt(TRACK_REMOVED_TRACK_POSITION, position);

            getParentFragmentManager().setFragmentResult(TRACK_REMOVED_KEY, result);
        }
    }

}
