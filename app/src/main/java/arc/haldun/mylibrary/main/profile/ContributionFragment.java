package arc.haldun.mylibrary.main.profile;

import android.animation.Animator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import java.sql.SQLException;

import arc.haldun.database.database.haldun;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.adapters.BookAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContributionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContributionFragment extends Fragment {

    RecyclerView recyclerView;
    BookAdapter bookAdapter;
    ProgressBar progressBar;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContributionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContributionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContributionFragment newInstance(String param1, String param2) {
        ContributionFragment fragment = new ContributionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contribution, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        final Book[][] books = new Book[1][1];

        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    books[0] = new haldun().selectBook();
                    books[0] = CurrentUser.selectContribution(books[0]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread mainThread = new Thread(new Runnable() {
            @Override
            public void run() {

                networkThread.start();
                try {
                    networkThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);

                        bookAdapter = new BookAdapter(requireActivity(), books[0]);

                        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.item_animation_fall_down);
                        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
                        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setLayoutAnimation(layoutAnimationController);
                        recyclerView.setAdapter(bookAdapter);
                    }
                });
            }
        });
        mainThread.start();
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.contribution_fragment_recyclerview);
        progressBar = view.findViewById(R.id.contribution_fragment_progressbar);
    }
}