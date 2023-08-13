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

package org.gateshipone.odyssey.models;


import android.annotation.SuppressLint;
import android.util.Log;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * This class keeps a HashMap of all artists that are part of a track list (e.g. playlist)
 * and their belonging tracks with the original list position as a pair. This can be used to
 * randomize the playback of the playback equally distributed over all artists of the original
 * track list.
 */
public class TrackRandomGenerator {
    private static final String TAG = TrackRandomGenerator.class.getSimpleName();

    /**
     * Underlying data structure for artist-track buckets used to randomize by artists
     */
    private final ArrayList<List<Integer>> mDataArtists;

    /**
     * Underlying data structure for artist-album buckets used for save the album
     */
    private final ArrayList<List<Integer>> mDataAlbum;

    /**
     * Underlying data structure for albums-tracks buckets used to randomize by album
     */
    private final ArrayList<List<Integer>> mDataAlbumTracks;

    /**
     * Underlying data structure for albums-tracks buckets used to randomize by artists AND album
     */
    private final ArrayList<List<Integer>> mDataSelectedAlbumTracks;

    /**
     * Creates an empty data structure
     */
    public TrackRandomGenerator() {
        mDataArtists = new ArrayList<>();
        mDataAlbum = new ArrayList<>();
        mDataAlbumTracks = new ArrayList<>();
        mDataSelectedAlbumTracks = new ArrayList<>();
    }

    private final BetterPseudoRandomGenerator mRandomGenerator = new BetterPseudoRandomGenerator();

    private List<TrackModel> mOriginalList;
    private List<AlbumModel> mOriginalListAlbum;
    private List<TrackModel> mOriginalListAlbumTracks;

    private int mIntelligenceFactor;

    /**
     * Creates a list of artists and their tracks with position in the original playlist.
     * Also creates a list of albums and tehir tracks with position in the original playlist.
     * @param tracks List of tracks
     */
    @SuppressLint("NewApi")
    public synchronized void fillFromList(List<TrackModel> tracks) {


        // Clear all entries
        mDataArtists.clear();
        mDataAlbumTracks.clear();

        mOriginalList = tracks;
        mOriginalListAlbumTracks = tracks;

        LinkedHashMap<String, List<Integer>> hashMap = new LinkedHashMap<>();
        LinkedHashMap<String, List<Integer>> hashMapAlbums = new LinkedHashMap<>();

        if (tracks == null || tracks.isEmpty()) {
            // Abort for empty data structures
            return;
        }

        // Iterate over the list and add all tracks to their artist lists
        int trackNo = 0;
        for (TrackModel track : tracks) {
            String artistName = track.getTrackArtistName();
            String albumName = track.getTrackAlbumName();

            List<Integer> list = hashMap.computeIfAbsent(artistName,k-> new ArrayList<>());
            List<Integer> listAlbum = hashMapAlbums.computeIfAbsent(albumName, k-> new ArrayList<>());
            list.add(trackNo);
            listAlbum.add(trackNo);

            // Increase the track number (index) of the original playlist
            trackNo++;
        }
        mDataArtists.addAll(hashMap.values());
        Collections.shuffle(mDataArtists);
        mDataAlbumTracks.addAll(hashMapAlbums.values());
        Collections.shuffle(mDataAlbumTracks);
    }
    /**
     * Creates a list of artists and their albums with position in the original playlist used
     * for save the album functionality
     *
     * @param albums List of albums
     */
    @SuppressLint("NewApi")
    public synchronized void fillAlbumFromList(List<AlbumModel> albums) {
        // Clear all entries
        mDataAlbum.clear();

        mOriginalListAlbum = albums;

        LinkedHashMap<String, List<Integer>> hashMap = new LinkedHashMap<>();

        if (albums == null || albums.isEmpty()) {
            // Abort for empty data structures
            return;
        }

        // Iterate over the list and add all tracks to their artist lists
        int trackNo = 0;
        for (AlbumModel album : albums) {
            String artistName = album.getArtistName();
            List<Integer> list = hashMap.computeIfAbsent(artistName, k-> new ArrayList<>());

            // Add pair of position in original playlist and track itself to artists bucket list
            list.add(trackNo);

            // Increase the track number (index) of the original playlist
            trackNo++;
        }


        mDataAlbum.addAll(hashMap.values());
        Collections.shuffle(mDataAlbum);
    }

    /**
     * Creates a list of albums and their tracks
     *
     * @param tracks List of tracks
     */
    @SuppressLint("NewApi")
    private synchronized void fillArtistsAlbumfromList(List<TrackModel> tracks) {
        LinkedHashMap<String, List<Integer>> hashMap = new LinkedHashMap<>();
        // Clear all entries
        mDataSelectedAlbumTracks.clear();
        if (tracks == null || tracks.isEmpty()) {
            // Abort for empty data structures
            return;
        }

        // Iterate over the list and add all track position to their artist lists
        for (TrackModel track : tracks) {
            String albumName = track.getTrackAlbumName();
            List<Integer> list = hashMap.computeIfAbsent(albumName,k-> new ArrayList<>());


            // Add pair of position in original playlist and track itself to artists bucket list
            list.add(mOriginalList.indexOf(track));

        }


        mDataSelectedAlbumTracks.addAll(hashMap.values());
        Collections.shuffle(mDataSelectedAlbumTracks);
    }

    /**
     * Generates a randomized track number within the original track list, that was used for the call
     * of fillFromList. The random track number should be equally distributed over all artists.
     *
     * @return A random number of a track of the original track list
     */
    public synchronized int getRandomTrackNumber() {
        // Randomize if we randomize by artists or by album
        Integer songNumber;

        if(mIntelligenceFactor == 50){
            //First randomize by artist then by album

            if (mDataArtists.isEmpty()) {
                // Refill list from original list
                fillFromList(mOriginalList);
            }

            // First level random, get artist
            int randomArtistNumber = mRandomGenerator.getLimitedRandomNumber(mDataArtists.size());

            // Get artists bucket list to artist number
            List<Integer> artistsTracks;

            // Get the list of tracks belonging to the selected artist
            artistsTracks = mDataArtists.get(randomArtistNumber);
            List<TrackModel> tracks = new ArrayList<>();
            for(Integer i:artistsTracks) {
               tracks.add( mOriginalList.get(i));
            }
            //Divide all the albums from the artist
            fillArtistsAlbumfromList(tracks);

            //Select a random album
            int randomAlbumNumber = mRandomGenerator.getLimitedRandomNumber(mDataSelectedAlbumTracks.size());
            // Get a random album bucket list
            List<Integer> albumTracks = mDataSelectedAlbumTracks.get(randomAlbumNumber);

            // Check if an album was found
            if (albumTracks == null) {
                return 0;
            }
            //get random track
            int randomTrackNo = mRandomGenerator.getLimitedRandomNumber(albumTracks.size());

            songNumber = albumTracks.get(randomTrackNo);

            // Remove track to prevent double plays
            //first we save it to remove it from the artistsTracks cause there it has another order
            artistsTracks.remove(albumTracks.get(randomTrackNo));
            albumTracks.remove(randomTrackNo);

            // Check if tracks from this artist are left, otherwise remove the artist
            if (artistsTracks.isEmpty()) {
                // No tracks left from artist, remove from map
                mDataArtists.remove(randomArtistNumber);

            }

         }else if(mIntelligenceFactor == 100){
            //Randomize only by artist
            songNumber = getArtistSongNumber();

        }else if(mIntelligenceFactor == 0){
            //randomize only by album
            songNumber = getAlbumSongNumber();

        }else {
            boolean onlyArtists = mRandomGenerator.getLimitedRandomNumber(100) <= mIntelligenceFactor;
            if (onlyArtists) {
                //Randomize only by artist
                songNumber = getArtistSongNumber();
            }else {
                //randomize only by album
                songNumber = getAlbumSongNumber();
            }
        }
        return songNumber;
    }

    /**
     * Generates a randomized track number inside the buckets album-tracks
     * @return the number of a track inside the buckets album-tracks
     */
    private synchronized int getAlbumSongNumber(){
        int songNumber = 0;

        if (mDataAlbumTracks.isEmpty()) {
            // Refill list from original list
            fillFromList(mOriginalListAlbumTracks);
        }

        // First level random, get album
        int randomAlbumNumber = mRandomGenerator.getLimitedRandomNumber(mDataAlbumTracks.size());

        // Get artist bucket to album number
        List<Integer> albumTracks;

        // Get the list of tracks belonging to the selected album
        albumTracks = mDataAlbumTracks.get(randomAlbumNumber);

        // Check if an album was found
        if (albumTracks == null) {
            return 0;
        }

        int randomTrackNo = mRandomGenerator.getLimitedRandomNumber(albumTracks.size());

        songNumber = albumTracks.get(randomTrackNo);

        // Remove track to prevent double plays
        albumTracks.remove(randomTrackNo);

        // Check if tracks from this artist are left, otherwise remove the artist
        if (albumTracks.isEmpty()) {
            // No tracks left from artist, remove from map
            mDataAlbumTracks.remove(randomAlbumNumber);

        }

        return songNumber;
    }

    /**
     * Generates a randomized track number inside the buckets artist-tracks
     * @return the number of a random track inside the buckets artist-tracks
     */
    private synchronized int getArtistSongNumber(){
        int songNumber = 0;

        if (mDataArtists.isEmpty()) {
            // Refill list from original list
            fillFromList(mOriginalList);
        }

        // First level random, get artist
        int randomArtistNumber = mRandomGenerator.getLimitedRandomNumber(mDataArtists.size());

        // Get artists bucket list to artist number
        List<Integer> artistsTracks;


        // Get the list of tracks belonging to the selected artist
        artistsTracks = mDataArtists.get(randomArtistNumber);

        // Check if an artist was found
        if (artistsTracks == null) {
            return 0;
        }

        int randomTrackNo = mRandomGenerator.getLimitedRandomNumber(artistsTracks.size());

        songNumber = artistsTracks.get(randomTrackNo);

        // Remove track to prevent double plays
        artistsTracks.remove(randomTrackNo);


        // Check if tracks from this artist are left, otherwise remove the artist
        if (artistsTracks.isEmpty()) {
            // No tracks left from artist, remove from map
            mDataArtists.remove(randomArtistNumber);

        }

        return songNumber;
    }

    /**
     * Generates a randomized album number within the original track list, that was used for the call
     * of fillFromList. The random track number should be equally distributed over all artists.
     *
     * @return A random number of a track of the original track list
     */
    public synchronized int getRandomAlbumNumber() {


        if (mDataAlbum.isEmpty()) {
            // Refill list from original list
            fillAlbumFromList(mOriginalListAlbum);
        }

        // First level random, get artist
        int randomArtistNumber = mRandomGenerator.getLimitedRandomNumber(mDataAlbum.size());

        // Get artists bucket list to artist number
        List<Integer> artistsAlbums;


        // Get the list of albums belonging to the selected artist
        artistsAlbums = mDataAlbum.get(randomArtistNumber);

        // Check if an artist was found
        if (artistsAlbums == null) {
            return 0;
        }

        int randomAlbumNo = mRandomGenerator.getLimitedRandomNumber(artistsAlbums.size());

        Integer albumNumber = artistsAlbums.get(randomAlbumNo);

        // Remove album to prevent double plays
        artistsAlbums.remove(randomAlbumNo);


        // Check if albums from this artist are left, otherwise remove the artist
        if (artistsAlbums.isEmpty()) {
            // No albums left from artist, remove from map
            mDataAlbum.remove(randomArtistNumber);

        }

        // Get random album number
        return albumNumber;
    }

    public void setEnabled(int factor) {
        if (mIntelligenceFactor == 0 && factor != 0) {
            // Redo track buckets
            fillFromList(mOriginalList);
            fillAlbumFromList(mOriginalListAlbum);
        } else if (mIntelligenceFactor != 0 && factor == 0) {
            // Remove track buckets
            fillFromList(null);
            fillAlbumFromList(null);
        }
        mIntelligenceFactor = factor;
    }

    public List<List<Integer>> getmDataArtists() {
        return mDataArtists;
    }

    public List<List<Integer>> getmDataAlbum() {
        return mDataAlbum;
    }

    public List<List<Integer>> getmDataAlbumTracks() {
        return mDataAlbumTracks;
    }

    public List<List<Integer>> getmDataSelectedAlbumTracks() {
        return mDataSelectedAlbumTracks;
    }

    public BetterPseudoRandomGenerator getmRandomGenerator() {
        return mRandomGenerator;
    }

    public List<TrackModel> getmOriginalList() {
        return mOriginalList;
    }

    public List<AlbumModel> getmOriginalListAlbum() {
        return mOriginalListAlbum;
    }

    public List<TrackModel> getmOriginalListAlbumTracks() {
        return mOriginalListAlbumTracks;
    }

    public int getmIntelligenceFactor() {
        return mIntelligenceFactor;
    }

    private static class BetterPseudoRandomGenerator {
        /**
         * Timeout in ns (1 second)
         */
        private static final long TIMEOUT_NS = 10000000000L;
        private final Random mJavaGenerator;

        private static final int RAND_MAX = Integer.MAX_VALUE;

        /**
         * Value after how many random numbers a reseed is done
         */
        private static final int RESEED_COUNT = 20;

        private int mNumbersGiven = 0;

        private int mInternalSeed;


        private BetterPseudoRandomGenerator() {
            mJavaGenerator = new Random();

            // Initialize internal seed
            mInternalSeed = mJavaGenerator.nextInt();


            // Do a quick check
            //testDistribution(20,20);
        }

        private int getInternalRandomNumber() {
            /*
             * Marsaglia, "Xorshift RNGs"
             */
            int newSeed = mInternalSeed;

            newSeed ^= newSeed << 13;
            newSeed ^= newSeed >> 17;
            newSeed ^= newSeed << 5;

            mNumbersGiven++;
            if (mNumbersGiven == RESEED_COUNT) {
                mInternalSeed = mJavaGenerator.nextInt();
                mNumbersGiven = 0;
            } else {
                mInternalSeed = newSeed;
            }
            return Math.abs(newSeed);
        }

        int getLimitedRandomNumber(int limit) {
            if (limit == 0) {
                return 0;
            }
            int r, d = RAND_MAX / limit;
            limit *= d;
            long startTime = System.nanoTime();
            do {
                r = getInternalRandomNumber();
                if ((System.nanoTime() - startTime) > TIMEOUT_NS) {
                    // Fallback to java generator
                    return mJavaGenerator.nextInt(limit);
                }
            } while (r >= limit);
            return r / d;
        }


        public void testDistribution(int numberLimit, int runs) {
            int[] numberCount = new int[numberLimit];

            for (int i = 0; i < runs; i++) {
                numberCount[getLimitedRandomNumber(numberLimit)]++;
            }

            // Print distribution and calculate mean
            int arithmeticMean = 0;
            for (int i = 0; i < numberLimit; i++) {
                Log.v(TAG, "Number: " + i + " = " + numberCount[i]);
                arithmeticMean += numberCount[i];
            }

            arithmeticMean /= numberLimit;
            Log.v(TAG, "Mean value: " + arithmeticMean);

            int variance = 0;
            for (int i = 0; i < numberLimit; i++) {
                variance += Math.pow((numberCount[i] - arithmeticMean), 2);
            }
            Log.v(TAG, "Variance: " + variance);
            double sd = Math.sqrt(variance);
            Log.v(TAG, "Standard deviation: " + sd);
            double rsd = sd / arithmeticMean;
            Log.v(TAG, "Relative standard deviation: " + rsd + " %");

        }


    }
}
