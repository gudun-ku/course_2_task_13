package com.elegion.myfirstapplication.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.elegion.myfirstapplication.model.Album;
import com.elegion.myfirstapplication.model.Song;

/**
 * @author Azret Magometov
 */

@Entity(foreignKeys = {
        @ForeignKey(entity = Album.class, parentColumns = "id", childColumns = "album_id"),
        @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "song_id")})
public class AlbumSong {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int mId;

    @ColumnInfo(name = "album_id")
    private int mAlbumId;

    @ColumnInfo(name = "song_id")
    private int mSongId;

    public AlbumSong() {
    }

    public AlbumSong(int id, int albumId, int songId) {
        mId = id;
        mAlbumId = albumId;
        mSongId = songId;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(int albumId) {
        mAlbumId = albumId;
    }

    public int getSongId() {
        return mSongId;
    }

    public void setSongId(int songId) {
        mSongId = songId;
    }

    @Override
    public String toString() {
        return "AlbumSong{" + "mId=" + mId +
                ", mAlbumId='" + mAlbumId +
                ", mSongId='" + mSongId + '}';
    }
}
