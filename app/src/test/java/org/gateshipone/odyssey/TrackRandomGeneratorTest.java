/*
 * Copyright (C) 2023 Team Gateship-One
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

package org.gateshipone.odyssey;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


import android.util.Log;

import org.gateshipone.odyssey.models.AlbumModel;
import org.gateshipone.odyssey.models.TrackModel;
import org.gateshipone.odyssey.models.TrackRandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TrackRandomGeneratorTest {

    private TrackRandomGenerator randomGenerator;
    private List<TrackModel> tracks;
    private List<AlbumModel> albums;
    private int numArtists = 5;
    private int totalAlbums = 0;

    @Before
    public void setUp() {

        randomGenerator = new TrackRandomGenerator();
        tracks = new ArrayList<>();
        albums = new ArrayList<>();

        // Generar 5 artistas distintos con sus álbumes y canciones
        for (int artistIndex = 1; artistIndex <= numArtists; artistIndex++) {
            String artistName = "Artist " + artistIndex;
            long artistId = artistIndex;

            int numAlbums = artistIndex == numArtists ? numArtists : 2;
            for (int albumIndex = 1; albumIndex <= numAlbums; albumIndex++) {
                String albumName = "Album " + albumIndex+artistIndex*10;
                long albumId = (artistIndex * 10) + albumIndex;

                int numSongs = albumIndex == 1 && artistIndex == numArtists ? 15 : numArtists;
                if (numSongs == 5 && albumIndex == 1 && artistIndex == 2) {
                    numSongs = 10;
                }

                for (int songIndex = 1; songIndex <= numSongs; songIndex++) {
                    // Crear y agregar objetos TrackModel a la lista
                    tracks.add(new TrackModel(
                            "Song " + songIndex,
                            artistName,
                            artistId,
                            albumName,
                            albumId,
                            180, // Duración en segundos (ejemplo)
                            songIndex,
                            null, // Uri (ejemplo)
                            (artistIndex * 1000) + (albumIndex * 100) + songIndex // ID único
                    ));
                }
                totalAlbums++;
            }
        }

        //generate the albums
        int albumIdCounter = 1;
        int dateAddedCounter = 100; // Solo un valor de ejemplo para dateAdded
        String[] artistNames = { "Artist1", "Artist2", "Artist3", "Artist4", "Artist5" };
        int[] numAlbumsPerArtist = { 1, 3, 4, 2, 10 };

        for (int artistIndex = 0; artistIndex < artistNames.length; artistIndex++) {
            String artistName = artistNames[artistIndex];
            int numAlbums = numAlbumsPerArtist[artistIndex];

            for (int albumIndex = 1; albumIndex <= numAlbums; albumIndex++) {
                String albumName = "Album " + albumIdCounter;
                String albumArtURL = "url"; // Cambia esto según tus necesidades
                long albumId = albumIdCounter;
                int dateAdded = dateAddedCounter;

                albums.add(new AlbumModel(albumName, albumArtURL, artistName, albumId, dateAdded));

                albumIdCounter++;
                dateAddedCounter++;
            }
        }

    }

    @Test
    public void fillFromList() {
        randomGenerator.fillFromList(tracks);

        //Check lists are not empty
        assertTrue(!randomGenerator.getmDataArtists().isEmpty());
        assertTrue(!randomGenerator.getmDataAlbumTracks().isEmpty());

        // Verifica que la cantidad de elementos en mDataArtists coincida con la cantidad de artistas únicos en la lista de tracks
        assertEquals(randomGenerator.getmDataArtists().size(), numArtists);
        // Verifica que la cantidad de canciones en mDataArtists coincida con la cantidad de canciones únicos en la lista de tracks
        int numCanciones= 0;
        for(List<Integer> list : randomGenerator.getmDataArtists() ) {
            for(Integer i : list) {
                numCanciones++;
            }
        }
        assertEquals(tracks.size(), numCanciones);
        // Verifica que la cantidad de elementos en mDataAlbumTracks coincida con la cantidad de álbumes únicos en la lista de tracks
        assertEquals(totalAlbums, randomGenerator.getmDataAlbumTracks().size());
        // Verifica que la cantidad de canciones en getmDataAlbumTracks coincida con la cantidad de canciones únicos en la lista de tracks
        numCanciones= 0;
        for(List<Integer> list : randomGenerator.getmDataAlbumTracks() ) {
            for(Integer i : list) {
                numCanciones++;
            }
        }
        assertEquals(tracks.size(), numCanciones);

        randomGenerator.fillFromList(null);
        //Check lists are  empty
        assertTrue(randomGenerator.getmDataArtists().isEmpty());
        assertTrue(randomGenerator.getmDataAlbumTracks().isEmpty());
    }

    @Test
    public void fillAlbumFromList() {
        randomGenerator.fillAlbumFromList(albums);
        //Check lists is not empty
        assertTrue(!randomGenerator.getmDataAlbum().isEmpty());
        // Verifica que la cantidad de artistas en getmDataAlbum coincida con la cantidad de  artistas únicos en la lista de albums
        assertEquals(randomGenerator.getmDataAlbum().size(), numArtists);

        // Verifica que la cantidad de albums getmDataAlbum coincida con la cantidad de albumes únicos en la lista de albums
        int numAlbums= 0;
        for(List<Integer> list : randomGenerator.getmDataAlbum() ) {
            for(Integer i : list) {
                numAlbums++;
            }
        }
        assertEquals(numAlbums, albums.size());


        randomGenerator.fillAlbumFromList(null);
        //Check list is  empty
        assertTrue(randomGenerator.getmDataAlbum().isEmpty());

    }

    @Test
    public void getRandomTrackNumberOnlyArtists() {
        List<Integer> listNumber = new ArrayList<Integer>();
        //Make sure we use our random generator
        randomGenerator.setEnabled(100);
        randomGenerator.fillFromList(tracks);
        //We have generated 80 tracks so we run it 80 times and we should ran out of tracks
        for(int i=0; i<=79; i++) {
            listNumber.add(randomGenerator.getRandomTrackNumber());
        }
        int numTracks= 0;
        for(List<Integer> list : randomGenerator.getmDataArtists() ) {
            for(Integer i : list) {
                numTracks++;
            }
        }
        assertEquals(numTracks, 0);
        //check that all elements are different
        assertTrue(areAllElementsUnique(listNumber));

        //test that if teh tracks are null there are no problems
        randomGenerator.setEnabled(0);
        randomGenerator.fillFromList(null);
        //Check list is  empty
        assertTrue(randomGenerator.getmDataArtists().isEmpty());
    }

    @Test
    public void getRandomTrackNumberOnlyAlbums() {
        List<Integer> listNumber = new ArrayList<Integer>();
        //Make sure we use our random generator
        randomGenerator.setEnabled(0);
        randomGenerator.fillFromList(tracks);
        //We have generated 80 tracks so we run it 80 times and we should ran out of tracks
        for(int i=0; i<=79; i++) {
            listNumber.add(randomGenerator.getRandomTrackNumber());
        }
        int numTracks= 0;
        for(List<Integer> list : randomGenerator.getmDataAlbumTracks() ) {
            for(Integer i : list) {
                numTracks++;
            }
        }
        assertEquals(numTracks, 0);
        //check that all elements are different
        assertTrue(areAllElementsUnique(listNumber));


        //test that if teh tracks are null there are no problems
        randomGenerator.setEnabled(0);
        randomGenerator.fillFromList(null);
        //Check list is  empty
        assertTrue(randomGenerator.getmDataAlbumTracks().isEmpty());
    }

    @Test
    public void getRandomTrackNumberArtistsAndAlbum() {
        List<Integer> listNumber = new ArrayList<Integer>();
        //Make sure we use our random generator
        randomGenerator.setEnabled(50);
        randomGenerator.fillFromList(tracks);
        //We have generated 80 tracks so we run it 80 times and we should ran out of tracks in both lists
        for(int i=0; i< tracks.size(); i++) {
            listNumber.add(randomGenerator.getRandomTrackNumber());
        }
        int numTracks= 0;
        for(List<Integer> list : randomGenerator.getmDataArtists() ) {
            for(Integer i : list) {
                numTracks++;
            }
        }
        assertEquals(numTracks, 0);

        //check that all elements are different
        assertTrue(areAllElementsUnique(listNumber));

        //test that if teh tracks are null there are no problems
        randomGenerator.setEnabled(0);
        randomGenerator.fillFromList(null);
        //Check list is  empty
        assertTrue(randomGenerator.getmDataAlbumTracks().isEmpty());
    }
    @Test
    public void getRandomAlbumNumberSmartRandom() {
        List<Integer> listNumber = new ArrayList<Integer>();
        //Make sure we use our random generator
        randomGenerator.setEnabled(100);
        randomGenerator.fillAlbumFromList(albums);
        //We have generated 20 albums so we run it 20 times and we should ran out of albums
        for(int i=0; i<=19; i++) {
            listNumber.add(randomGenerator.getRandomAlbumNumber());
        }
        int numAlbums= 0;
        for(List<Integer> list : randomGenerator.getmDataAlbum() ) {
            for(Integer i : list) {
                numAlbums++;
            }
        }
        assertEquals(numAlbums, 0);
        //check that all elements are different
        assertTrue(areAllElementsUnique(listNumber));

    }

    @Test
    public void getRandomAlbumNumberTraditionalRandom() {
        List<Integer> listNumber = new ArrayList<Integer>();
        //Make sure we use our random generator
        randomGenerator.setEnabled(0);
        randomGenerator.fillAlbumFromList(albums);
        //We have generated 20 albums so we run it 20 times but in traditional we don't eliminate
        for(int i=0; i<=19; i++) {
            listNumber.add(randomGenerator.getRandomAlbumNumber());
        }
        int numAlbums= 0;
        for(List<Integer> list : randomGenerator.getmDataAlbum() ) {
            for(Integer i : list) {
                numAlbums++;
            }
        }
        assertEquals(numAlbums, 20);
        //check that there are probably repetitions
        assertFalse(areAllElementsUnique(listNumber));

    }

    private boolean areAllElementsUnique(List<Integer> array) {
        HashSet<Integer> uniqueSet = new HashSet<>();

        for (int num : array) {
            if (!uniqueSet.add(num)) {
                return false; // Duplicate found
            }
        }

        return true; // All elements are unique
    }
}