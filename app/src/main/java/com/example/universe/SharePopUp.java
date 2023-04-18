package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.universe.Models.Event;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;

import java.util.List;
import java.util.stream.Collectors;


public class SharePopUp extends Fragment implements ShareUserAdapter.IShareListRecyclerAction{
    private LayoutInflater inflater;
    private PopupWindow popupWindow;
    private View popupView;

    private RecyclerView recyclerView;
    private Button shareButton;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ShareUserAdapter shareUserAdapter;

    private List<User> friendList;

    private List<String> friendIDList;

    private Util util;

    private Event event;


    @SuppressLint("InflateParams")
    public void showPopupWindow(View view, User user, Event event) {

        inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.fragment_share_pop_up, null);

        popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.FIRST_SUB_WINDOW,
                WindowManager.LayoutParams.WRAP_CONTENT, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        this.event = event;

        util = Util.getInstance();

        recyclerView = popupView.findViewById(R.id.sharePopup_recyclerView);

        shareButton = popupView.findViewById(R.id.sharePopup_button_share);

        recyclerViewLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);


        friendIDList = user.getFollowingIdList().stream()
                .filter(s -> !event.getParticipantsAndCandidates().contains(s) && !s.equals(event.getHostId()))
                .collect(Collectors.toList());

        util.getUsersByIdList(friendIDList, users -> {
            friendList = users;
            shareUserAdapter = new ShareUserAdapter(view,friendList, this);
            recyclerView.setAdapter(shareUserAdapter);
        }, Util.DEFAULT_F_LISTENER);

        shareButton.setOnClickListener(v ->
                util.sendMessage(shareUserAdapter.getSelectedUser().getUid(),
                        new Message(util.getCurrentUser(),
                        "Your friend share an event with you.Click Here to view the event!" + event.getUid(),
                        null, null),
                unused -> {
                    popupWindow.dismiss();
                    Toast.makeText(v.getContext(),"Share Successful!", Toast.LENGTH_SHORT).show();
                }, Util.DEFAULT_F_LISTENER));

        popupWindow.setOutsideTouchable(true);

        popupWindow.setOnDismissListener(() -> view.getRootView().findViewById(R.id.event_cover).setVisibility(View.INVISIBLE));
    }

    @Override
    public void enableButton() {
        shareButton.setEnabled(true);
    }
}