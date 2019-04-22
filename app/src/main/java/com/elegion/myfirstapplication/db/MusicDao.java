package com.elegion.myfirstapplication.db;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import com.elegion.myfirstapplication.model.Album;
import com.elegion.myfirstapplication.model.Song;

import java.util.List;

/**
 * @author Azret Magometov
 */

@Dao
public interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAlbums(List<Album> albums);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongs(List<Song> songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setLinksAlbumSongs(List<AlbumSong> linksAlbumSongs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAlbum(Album albums);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(Song songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setLinkAlbumSongs(AlbumSong linkAlbumSongs);


    @Query("select * from album")
    List<Album> getAlbums();

    @Query("select * from song")
    List<Song> getSongs();

    @Query("select * from albumsong")
    List<AlbumSong> getAlbumSongs();

    @Query("select * from album")
    Cursor getAlbumsCursor();

    @Query("select * from song")
    Cursor getSongsCursor();

    @Query("select * from albumsong")
    Cursor getAlbumSongsCursor();

    @Query("select * from album where id = :Id")
    Cursor getAlbumWithIdCursor(int Id);

    @Query("select * from album where id = :Id")
    Album getAlbumWithId(int Id);

    @Query("select * from song where id = :Id")
    Cursor getSongWithIdCursor(int Id);

    @Query("select * from albumsong where id = :Id")
    Cursor getAlbumSongWithIdCursor(int Id);

    @Delete
    void deleteAlbum(Album album);

    @Delete
    void deleteSong(Song song);

    @Delete
    void deleteAlbumSong(AlbumSong albumsong);

    //получить список песен переданного id альбома
    @Query("select song.* from song inner join albumsong on song.id = albumsong.song_id where album_id = :albumId")
    List<Song> getSongsFromAlbum(int albumId);

    //обновить информацию об альбоме
    @Update
    int updateAlbumInfo(Album album);

    //обновить информацию о песне
    @Update
    int updateSongInfo(Song song);

    //обновить информацию о связи альбома с песней
    @Update
    int updateAlbumSongInfo(AlbumSong albumsong);

    //удалить альбом по id
    @Query("DELETE FROM album where id = :albumId")
    int deleteAlbumById(int albumId);

    //удалить песню по id
    @Query("DELETE FROM song where id = :songId")
    int deleteSongById(int songId);

    //удалить связь альбома с песней по id
    @Query("DELETE FROM albumsong where id = :albumsongId")
    int deleteAlbumSongById(int albumsongId);

    //удалить связь альбома с песней по album id
    @Query("DELETE FROM albumsong where album_id = :albumId")
    int deleteAlbumSongsByAlbumId(int albumId);

}
