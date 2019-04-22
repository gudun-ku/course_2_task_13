package com.elegion.myfirstapplication.albums;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elegion.myfirstapplication.ApiUtils;
import com.elegion.myfirstapplication.App;
import com.elegion.myfirstapplication.R;
import com.elegion.myfirstapplication.album.DetailAlbumFragment;
import com.elegion.myfirstapplication.db.MusicDao;
import com.elegion.myfirstapplication.model.Album;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Azret Magometov
 */

public class AlbumsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefresher;
    private View mErrorView;
    private Disposable mSubscription;

    @NonNull
    private final AlbumsAdapter mAlbumAdapter = new AlbumsAdapter(new AlbumsAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(Album album) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, DetailAlbumFragment.newInstance(album))
                    .addToBackStack(DetailAlbumFragment.class.getSimpleName())
                    .commit();
        }
    });

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_recycler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler);
        mRefresher = view.findViewById(R.id.refresher);
        mRefresher.setOnRefreshListener(this);
        mErrorView = view.findViewById(R.id.errorView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.albums);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAlbumAdapter);

        onRefresh();
    }

    @Override
    public void onRefresh() {
        mRefresher.post(new Runnable() {
            @Override
            public void run() {
                mRefresher.setRefreshing(true);
                getAlbums();
            }
        });
    }

    private void getAlbums() {

        mSubscription = ApiUtils.getApiService()
            .getAlbums()
            .doOnSuccess(new Consumer<List<Album>>() {
                @Override
                public void accept(List<Album> albums) throws Exception {
                    getMusicDao().insertAlbums(albums);
                }
            })
            .onErrorReturn(new Function<Throwable, List<Album>>() {
                @Override
                public List<Album> apply(Throwable throwable) throws Exception {
                    if (ApiUtils.NETWORK_EXCEPTION.contains(throwable.getClass())) {
                        return getMusicDao().getAlbums();
                    } else {
                        return null;
                    }
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    mRefresher.setRefreshing(true);
                }
            })
            .doFinally(new Action() {
                @Override
                public void run() throws Exception {
                    mRefresher.setRefreshing(false);
                }
            })
            .subscribe(new Consumer<List<Album>>() {
                           @Override
                           public void accept(List<Album> albums) throws Exception {
                               mErrorView.setVisibility(View.GONE);
                               mRecyclerView.setVisibility(View.VISIBLE);
                               mAlbumAdapter.addData(albums, true);
                           }
                       },
                       new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                mErrorView.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                            }
                       }
                      );

    }

    @Override
    public void onDestroy() {
        mSubscription.dispose();
        super.onDestroy();
    }

    private MusicDao getMusicDao() {
        return ((App) getActivity().getApplication()).getDatabase().getMusicDao();
    }
}
