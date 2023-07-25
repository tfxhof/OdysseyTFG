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

package org.gateshipone.odyssey.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import org.gateshipone.odyssey.R;
import org.gateshipone.odyssey.fragments.MyMusicFragment;


public class OdysseyMenuSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_selector);


        Button bArtists = findViewById(R.id.button_artists);
        Button bAlbums = findViewById(R.id.button_albums);
        Button bPlaylists = findViewById(R.id.button_playlists);
        Button bMyMusic = findViewById(R.id.button_songs);

        bArtists.setOnClickListener(v -> {
            String str = String.valueOf(MyMusicFragment.DEFAULTTAB.ARTISTS);
            startActivity(new Intent(OdysseyMenuSelectorActivity.this, OdysseyMainActivity.class).putExtra("tab", str));
            finish();
        });

        bAlbums.setOnClickListener(v -> {
            String str = String.valueOf(MyMusicFragment.DEFAULTTAB.ALBUMS);
            startActivity(new Intent(OdysseyMenuSelectorActivity.this, OdysseyMainActivity.class).putExtra("tab", str));
            finish();
        });

        bPlaylists.setOnClickListener(v -> {
            String str = "Playlists";
            startActivity(new Intent(OdysseyMenuSelectorActivity.this, OdysseyMainActivity.class).putExtra("tab", str));
            finish();
        });

        bMyMusic.setOnClickListener(v -> {
            String str = String.valueOf(MyMusicFragment.DEFAULTTAB.TRACKS);
            startActivity(new Intent(OdysseyMenuSelectorActivity.this, OdysseyMainActivity.class).putExtra("tab", str));
            finish();
        });
    }
}