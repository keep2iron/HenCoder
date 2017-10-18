/*
 * Create bt Keep2iron on 17-7-3 下午3:10
 */

package io.github.keep2iron.hencoder;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by keep2iron on ${Date}.
 * write the powerful code ！
 * website : keep2iron.github.io
 */
public class ViewFragment extends Fragment {
    protected View mContentView;

    public static ViewFragment getInstance(@LayoutRes int layoutRes) {
        Bundle bundle = new Bundle();
        bundle.putInt("layoutRes", layoutRes);

        ViewFragment fragment = new ViewFragment();
        fragment.setArguments(bundle);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int layoutRes = getArguments().getInt("layoutRes");

        mContentView = inflater.inflate(layoutRes, container, false);
        mContentView.setClickable(true);

        return mContentView;
    }

    public View findViewById(@IdRes int id) {
        return mContentView.findViewById(id);
    }
}
