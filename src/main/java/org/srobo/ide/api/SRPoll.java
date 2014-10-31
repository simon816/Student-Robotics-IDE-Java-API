package org.srobo.ide.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

public class SRPoll extends Module {
    public static interface PollListener {
        public void dataChanged(SRPoll poll, JSONObject data);
    }

    private final SRTeam team;
    private Timer timer;
    private long delay;
    private List<PollListener> listeners;
    private JSONObject previousData;

    SRPoll(SRTeam team, long delay) {
        super("poll");
        this.team = team;
        addRequiredData("team", team.id);
        this.delay = delay;
        listeners = new ArrayList<PollListener>();
    }

    private JSONObject getPollData() throws SRException {
        JSONObject output = sendCommand("poll");
        JSONObject projects;
        try {
            projects = output.getJSONObject("projects");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return projects;
    }

    @Override
    public String toString() {
        return "SRPoll(" + team + ")";
    }

    public SRTeam getTeam() {
        return team;
    }

    public boolean isSubscribed(PollListener listener) {
        return listeners.contains(listener);
    }

    public void subscribe(PollListener listener) {
        if (isSubscribed(listener))
            return;
        if (timer == null)
            setupTimer();
        listeners.add(listener);
    }

    public void unsubscribe(PollListener listener) {
        if (isSubscribed(listener))
            listeners.remove(listener);
    }

    private void setupTimer() {
        timer = new Timer(true);
        final SRPoll poll = this;
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (listeners.size() == 0)
                    return;
                JSONObject data;
                try {
                    data = getPollData();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                // JSONObject does not implement equals properly, check string
                // instead
                if (data == null || (previousData != null && data.toString().equals(previousData.toString())))
                    return;
                previousData = data;
                for (PollListener listener : listeners) {
                    try {
                        listener.dataChanged(poll, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 0, delay);
    }

    public void stop() {
        if (timer != null)
            timer.cancel();
        timer = null;
    }

    void unsubscribeAll() {
        listeners.clear();
    }
}
